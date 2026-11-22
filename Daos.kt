package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FocusSessionEntity
import com.example.data.local.entity.HabitCompletionEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.SscChapterEntity
import com.example.data.local.entity.SscSubjectEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
  @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, dueDateEpochDay ASC, id DESC")
  fun observeAllTasks(): Flow<List<TaskEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTask(task: TaskEntity): Long

  @Update suspend fun updateTask(task: TaskEntity)

  @Delete suspend fun deleteTask(task: TaskEntity)

  @Query("DELETE FROM tasks") suspend fun clearAll()
}

@Dao
interface NoteDao {
  @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAtMillis DESC")
  fun observeAllNotes(): Flow<List<NoteEntity>>

  @Query("SELECT COUNT(*) FROM notes") fun observeNotesCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNote(note: NoteEntity): Long

  @Update suspend fun updateNote(note: NoteEntity)

  @Delete suspend fun deleteNote(note: NoteEntity)

  @Query("DELETE FROM notes") suspend fun clearAll()
}

@Dao
interface HabitDao {
  @Query("SELECT * FROM habits ORDER BY id ASC")
  fun observeAllHabits(): Flow<List<HabitEntity>>

  @Query("SELECT * FROM habit_completions WHERE epochDay >= :sinceEpochDay")
  fun observeRecentCompletions(sinceEpochDay: Long): Flow<List<HabitCompletionEntity>>

  @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND epochDay = :epochDay LIMIT 1")
  suspend fun getCompletionForDay(habitId: Long, epochDay: Long): HabitCompletionEntity?

  @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY epochDay DESC")
  suspend fun getCompletionsForHabit(habitId: Long): List<HabitCompletionEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHabit(habit: HabitEntity): Long

  @Update suspend fun updateHabit(habit: HabitEntity)

  @Delete suspend fun deleteHabit(habit: HabitEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertCompletion(completion: HabitCompletionEntity)

  @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND epochDay = :epochDay")
  suspend fun deleteCompletion(habitId: Long, epochDay: Long)

  @Query("DELETE FROM habits") suspend fun clearAllHabits()
}

@Dao
interface FocusSessionDao {
  @Query("SELECT * FROM focus_sessions ORDER BY completedAtMillis DESC")
  fun observeAllSessions(): Flow<List<FocusSessionEntity>>

  @Query("SELECT * FROM focus_sessions WHERE epochDay >= :sinceEpochDay ORDER BY completedAtMillis DESC")
  fun observeSessionsSince(sinceEpochDay: Long): Flow<List<FocusSessionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: FocusSessionEntity): Long

  @Query("DELETE FROM focus_sessions") suspend fun clearAll()
}

@Dao
interface SscDao {
  @Query("SELECT * FROM ssc_subjects ORDER BY id ASC")
  fun observeAllSubjects(): Flow<List<SscSubjectEntity>>

  @Query("SELECT COUNT(*) FROM ssc_subjects") suspend fun getSubjectCount(): Int

  @Query("SELECT * FROM ssc_chapters ORDER BY subjectId ASC, chapterOrder ASC, id ASC")
  fun observeAllChapters(): Flow<List<SscChapterEntity>>

  @Query("SELECT * FROM ssc_chapters WHERE subjectId = :subjectId ORDER BY chapterOrder ASC, id ASC")
  fun observeChaptersForSubject(subjectId: Long): Flow<List<SscChapterEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSubject(subject: SscSubjectEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChapters(chapters: List<SscChapterEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChapter(chapter: SscChapterEntity): Long

  @Update suspend fun updateChapter(chapter: SscChapterEntity)

  @Query("UPDATE ssc_subjects SET studyMinutes = studyMinutes + :minutes WHERE nameEn = :subjectNameEn")
  suspend fun addStudyMinutesByName(subjectNameEn: String, minutes: Int)

  @Query("DELETE FROM ssc_subjects") suspend fun clearAllSubjects()
}

@Dao
interface UserStatsDao {
  @Query("SELECT * FROM user_stats WHERE id = 1")
  fun observeStats(): Flow<UserStatsEntity?>

  @Query("SELECT * FROM user_stats WHERE id = 1")
  suspend fun getStatsOnce(): UserStatsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertStats(stats: UserStatsEntity)
}
