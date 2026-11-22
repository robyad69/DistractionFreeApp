package com.example.data.repository

import com.example.data.local.DistractionFreeDatabase
import com.example.data.local.entity.FocusSessionEntity
import com.example.data.local.entity.HabitCompletionEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.SscChapterEntity
import com.example.data.local.entity.SscSubjectEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserStatsEntity
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProductivityRepository(private val db: DistractionFreeDatabase) {

  private val initMutex = Mutex()
  @Volatile private var seeded = false

  val tasksFlow: Flow<List<TaskEntity>> = db.taskDao().observeAllTasks()
  val notesFlow: Flow<List<NoteEntity>> = db.noteDao().observeAllNotes()
  val notesCountFlow: Flow<Int> = db.noteDao().observeNotesCount()
  val habitsFlow: Flow<List<HabitEntity>> = db.habitDao().observeAllHabits()
  val focusSessionsFlow: Flow<List<FocusSessionEntity>> = db.focusSessionDao().observeAllSessions()
  val sscSubjectsFlow: Flow<List<SscSubjectEntity>> = db.sscDao().observeAllSubjects()
  val sscChaptersFlow: Flow<List<SscChapterEntity>> = db.sscDao().observeAllChapters()

  val userStatsFlow: Flow<UserStatsEntity> =
    db.userStatsDao().observeStats().map { stats ->
      stats ?: UserStatsEntity(sscExamEpochDay = DateUtils.defaultSscExamEpochDay())
    }

  fun observeRecentHabitCompletions(daysBack: Int = 7): Flow<List<HabitCompletionEntity>> {
    val since = DateUtils.todayEpochDay() - (daysBack - 1).coerceAtLeast(0)
    return db.habitDao().observeRecentCompletions(since)
  }

  suspend fun ensureInitialized() {
    if (seeded) return
    initMutex.withLock {
      if (seeded) return
      if (db.userStatsDao().getStatsOnce() == null) {
        db.userStatsDao().upsertStats(
          UserStatsEntity(sscExamEpochDay = DateUtils.defaultSscExamEpochDay())
        )
      }
      if (db.sscDao().getSubjectCount() == 0) {
        seedSscCurriculum()
      }
      seeded = true
    }
  }

  private suspend fun seedSscCurriculum() {
    for (seed in SscSeedData.subjects) {
      val subjectId = db.sscDao().insertSubject(SscSeedData.toSubjectEntity(seed))
      db.sscDao().insertChapters(SscSeedData.toChapterEntities(subjectId, seed))
    }
  }

  // --- Tasks ---
  suspend fun saveTask(task: TaskEntity) {
    ensureInitialized()
    if (task.id == 0L) {
      db.taskDao().insertTask(task)
    } else {
      db.taskDao().updateTask(task)
    }
  }

  suspend fun toggleTaskCompleted(task: TaskEntity) {
    ensureInitialized()
    val newCompleted = !task.isCompleted
    db.taskDao().updateTask(task.copy(isCompleted = newCompleted))
    if (newCompleted) {
      awardStats(xpDelta = 15, auraDelta = 3, tasksCompletedDelta = 1)
    }
  }

  suspend fun deleteTask(task: TaskEntity) {
    db.taskDao().deleteTask(task)
  }

  // --- Notes ---
  suspend fun saveNote(note: NoteEntity) {
    ensureInitialized()
    val stamped = note.copy(updatedAtMillis = System.currentTimeMillis())
    if (note.id == 0L) {
      db.noteDao().insertNote(stamped)
      awardStats(xpDelta = 5, auraDelta = 1)
    } else {
      db.noteDao().updateNote(stamped)
    }
  }

  suspend fun toggleNotePin(note: NoteEntity) {
    db.noteDao().updateNote(note.copy(isPinned = !note.isPinned))
  }

  suspend fun deleteNote(note: NoteEntity) {
    db.noteDao().deleteNote(note)
  }

  // --- Habits ---
  suspend fun createHabit(title: String, iconKey: String) {
    ensureInitialized()
    db.habitDao().insertHabit(HabitEntity(title = title.trim(), iconKey = iconKey))
  }

  suspend fun deleteHabit(habit: HabitEntity) {
    db.habitDao().deleteHabit(habit)
  }

  suspend fun toggleHabitToday(habit: HabitEntity) {
    ensureInitialized()
    val today = DateUtils.todayEpochDay()
    val existing = db.habitDao().getCompletionForDay(habit.id, today)
    if (existing != null) {
      db.habitDao().deleteCompletion(habit.id, today)
      val remaining = db.habitDao().getCompletionsForHabit(habit.id)
      val recalculated = computeStreakFromDays(remaining.map { it.epochDay }, today)
      val lastDay = remaining.firstOrNull()?.epochDay ?: -1L
      db.habitDao().updateHabit(
        habit.copy(
          currentStreak = recalculated,
          lastCompletedEpochDay = lastDay,
          totalCompletions = (habit.totalCompletions - 1).coerceAtLeast(0),
        )
      )
    } else {
      db.habitDao().insertCompletion(HabitCompletionEntity(habitId = habit.id, epochDay = today))
      val allDays = db.habitDao().getCompletionsForHabit(habit.id).map { it.epochDay }
      val newStreak = computeStreakFromDays(allDays, today)
      val newBest = maxOf(habit.bestStreak, newStreak)
      db.habitDao().updateHabit(
        habit.copy(
          currentStreak = newStreak,
          bestStreak = newBest,
          lastCompletedEpochDay = today,
          totalCompletions = habit.totalCompletions + 1,
        )
      )
      awardStats(xpDelta = 20, auraDelta = 5, habitsCompletedDelta = 1)
    }
  }

  private fun computeStreakFromDays(sortedDescDays: List<Long>, today: Long): Int {
    if (sortedDescDays.isEmpty()) return 0
    val daySet = sortedDescDays.toSet()
    var cursor = if (daySet.contains(today)) today else if (daySet.contains(today - 1)) today - 1 else return 0
    var streak = 0
    while (daySet.contains(cursor)) {
      streak++
      cursor--
    }
    return streak
  }

  // --- Focus Sessions ---
  suspend fun recordFocusSession(
    mode: String,
    plannedMinutes: Int,
    actualSeconds: Int,
    subjectTag: String,
  ): Pair<Int, Int> {
    ensureInitialized()
    val minutesCompleted = (actualSeconds / 60).coerceAtLeast(if (actualSeconds >= 30) 1 else 0)
    if (minutesCompleted <= 0) return 0 to 0

    val isBreak = mode == "SHORT_BREAK"
    val xpEarned = if (isBreak) 5 else (minutesCompleted * 2).coerceAtLeast(5)
    val auraEarned = if (isBreak) 1 else (minutesCompleted / 5).coerceAtLeast(1)
    val today = DateUtils.todayEpochDay()

    db.focusSessionDao().insertSession(
      FocusSessionEntity(
        mode = mode,
        durationMinutes = plannedMinutes,
        actualSecondsCompleted = actualSeconds,
        subjectTag = subjectTag,
        epochDay = today,
        xpEarned = xpEarned,
        auraEarned = auraEarned,
      )
    )

    if (!isBreak) {
      db.sscDao().addStudyMinutesByName(subjectTag, minutesCompleted)
    }

    awardStats(
      xpDelta = xpEarned,
      auraDelta = auraEarned,
      focusMinutesDelta = if (isBreak) 0 else minutesCompleted,
      sessionsDelta = 1,
    )
    return xpEarned to auraEarned
  }

  // --- SSC Study Center ---
  suspend fun toggleChapterCompleted(chapter: SscChapterEntity) {
    ensureInitialized()
    val next = !chapter.isCompleted
    db.sscDao().updateChapter(chapter.copy(isCompleted = next))
    if (next) {
      awardStats(xpDelta = 25, auraDelta = 5)
    }
  }

  suspend fun addCustomChapter(subjectId: Long, title: String, nextOrder: Int) {
    ensureInitialized()
    val clean = title.trim()
    if (clean.isEmpty()) return
    db.sscDao().insertChapter(
      SscChapterEntity(
        subjectId = subjectId,
        titleEn = clean,
        titleBn = clean,
        isCompleted = false,
        chapterOrder = nextOrder,
      )
    )
  }

  suspend fun updateSscExamEpochDay(epochDay: Long) {
    ensureInitialized()
    val current =
      db.userStatsDao().getStatsOnce()
        ?: UserStatsEntity(sscExamEpochDay = DateUtils.defaultSscExamEpochDay())
    db.userStatsDao().upsertStats(current.copy(sscExamEpochDay = epochDay))
  }

  suspend fun updateDailyStudyGoal(minutes: Int) {
    ensureInitialized()
    val current =
      db.userStatsDao().getStatsOnce()
        ?: UserStatsEntity(sscExamEpochDay = DateUtils.defaultSscExamEpochDay())
    db.userStatsDao().upsertStats(current.copy(dailyStudyGoalMinutes = minutes.coerceIn(15, 600)))
  }

  // --- Gamification & Stats ---
  private suspend fun awardStats(
    xpDelta: Int = 0,
    auraDelta: Int = 0,
    focusMinutesDelta: Int = 0,
    sessionsDelta: Int = 0,
    tasksCompletedDelta: Int = 0,
    habitsCompletedDelta: Int = 0,
  ) {
    val today = DateUtils.todayEpochDay()
    val current =
      db.userStatsDao().getStatsOnce()
        ?: UserStatsEntity(sscExamEpochDay = DateUtils.defaultSscExamEpochDay())

    val newStreak =
      when {
        current.lastActiveEpochDay == today -> maxOf(1, current.currentStreak)
        current.lastActiveEpochDay == today - 1 -> current.currentStreak + 1
        else -> 1
      }
    val newBestStreak = maxOf(current.bestStreak, newStreak)

    db.userStatsDao().upsertStats(
      current.copy(
        xp = (current.xp + xpDelta).coerceAtLeast(0),
        aura = (current.aura + auraDelta).coerceAtLeast(0),
        currentStreak = newStreak,
        bestStreak = newBestStreak,
        lastActiveEpochDay = today,
        totalFocusMinutes = current.totalFocusMinutes + focusMinutesDelta,
        totalSessionsCompleted = current.totalSessionsCompleted + sessionsDelta,
        totalTasksCompleted = current.totalTasksCompleted + tasksCompletedDelta,
        totalHabitsCompleted = current.totalHabitsCompleted + habitsCompletedDelta,
      )
    )
  }

  suspend fun resetAllData() {
    initMutex.withLock {
      db.taskDao().clearAll()
      db.noteDao().clearAll()
      db.habitDao().clearAllHabits()
      db.focusSessionDao().clearAll()
      db.sscDao().clearAllSubjects()
      db.userStatsDao().upsertStats(
        UserStatsEntity(sscExamEpochDay = DateUtils.defaultSscExamEpochDay())
      )
      seedSscCurriculum()
      seeded = true
    }
  }
}
