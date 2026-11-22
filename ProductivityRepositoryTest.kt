package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DistractionFreeDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.ProductivityRepository
import com.example.localization.AppLanguage
import com.example.util.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProductivityRepositoryTest {

  private lateinit var context: Context
  private lateinit var db: DistractionFreeDatabase
  private lateinit var repo: ProductivityRepository

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db =
      Room.inMemoryDatabaseBuilder(context, DistractionFreeDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    repo = ProductivityRepository(db)
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `repository seeds 8 SSC subjects and awards XP on focus and task completion`() = runTest {
    repo.ensureInitialized()
    val subjects = repo.sscSubjectsFlow.first()
    assertEquals(8, subjects.size)

    val task =
      TaskEntity(
        title = "Solve Physics Ch 2",
        subjectTag = "Physics",
        dueDateEpochDay = DateUtils.todayEpochDay(),
      )
    repo.saveTask(task)
    val savedTask = repo.tasksFlow.first().first()
    repo.toggleTaskCompleted(savedTask)

    val (xp, aura) =
      repo.recordFocusSession(
        mode = "POMODORO",
        plannedMinutes = 25,
        actualSeconds = 1500,
        subjectTag = "Physics",
      )
    assertTrue(xp >= 50)
    assertTrue(aura >= 5)

    val stats = repo.userStatsFlow.first()
    assertEquals(15 + xp, stats.xp)
    assertEquals(1, stats.totalTasksCompleted)
    assertEquals(1, stats.totalSessionsCompleted)
    assertEquals(25, stats.totalFocusMinutes)
  }

  @Test
  fun `habit completion toggle on off on keeps accurate streak and count`() = runTest {
    repo.ensureInitialized()
    repo.createHabit("Revise Formulas", "book")
    val habit = repo.habitsFlow.first().first()

    repo.toggleHabitToday(habit)
    val afterOn = repo.habitsFlow.first().first()
    assertEquals(1, afterOn.currentStreak)
    assertEquals(1, afterOn.totalCompletions)

    repo.toggleHabitToday(afterOn)
    val afterOff = repo.habitsFlow.first().first()
    assertEquals(0, afterOff.currentStreak)
    assertEquals(0, afterOff.totalCompletions)

    repo.toggleHabitToday(afterOff)
    val afterSecondOn = repo.habitsFlow.first().first()
    assertEquals(1, afterSecondOn.currentStreak)
    assertEquals(1, afterSecondOn.totalCompletions)
  }

  @Test
  fun `notes, SSC chapters, and resetAllData work reliably`() = runTest {
    repo.ensureInitialized()
    repo.saveNote(NoteEntity(title = "Newton Laws", content = "F = ma", category = "Formula"))
    val note = repo.notesFlow.first().first()
    repo.toggleNotePin(note)
    assertTrue(repo.notesFlow.first().first().isPinned)

    val chapter = repo.sscChaptersFlow.first().first()
    repo.toggleChapterCompleted(chapter)
    assertTrue(repo.sscChaptersFlow.first().first().isCompleted)

    repo.resetAllData()
    assertTrue(repo.notesFlow.first().isEmpty())
    assertTrue(repo.tasksFlow.first().isEmpty())
    assertEquals(8, repo.sscSubjectsFlow.first().size)
    assertFalse(repo.sscChaptersFlow.first().first().isCompleted)
  }

  @Test
  fun `data and preferences persist across simulated app restart`() = runTest {
    val dbName = "restart_persistence_test_db"
    context.deleteDatabase(dbName)

    // Session 1: write data and close DB
    val db1 =
      Room.databaseBuilder(context, DistractionFreeDatabase::class.java, dbName)
        .allowMainThreadQueries()
        .build()
    val repo1 = ProductivityRepository(db1)
    repo1.ensureInitialized()
    repo1.saveTask(
      TaskEntity(title = "Persist Me", dueDateEpochDay = DateUtils.todayEpochDay())
    )
    repo1.saveNote(NoteEntity(title = "Saved Note", content = "Offline persistence"))
    repo1.createHabit("Morning Revision", "book")
    val habit = repo1.habitsFlow.first().first()
    repo1.toggleHabitToday(habit)
    repo1.recordFocusSession("POMODORO", 25, 1500, "General Math")
    repo1.updateDailyStudyGoal(180)
    db1.close()

    val prefsRepo1 = UserPreferencesRepository(context)
    prefsRepo1.setDarkTheme(false)
    prefsRepo1.setLanguage(AppLanguage.BENGALI)
    prefsRepo1.setUserName("Aroby")

    // Session 2: reopen DB & preferences after restart
    val db2 =
      Room.databaseBuilder(context, DistractionFreeDatabase::class.java, dbName)
        .allowMainThreadQueries()
        .build()
    val repo2 = ProductivityRepository(db2)
    repo2.ensureInitialized()

    assertEquals("Persist Me", repo2.tasksFlow.first().first().title)
    assertEquals("Saved Note", repo2.notesFlow.first().first().title)
    assertEquals(1, repo2.habitsFlow.first().first().currentStreak)
    assertEquals(1, repo2.focusSessionsFlow.first().size)
    assertEquals(180, repo2.userStatsFlow.first().dailyStudyGoalMinutes)

    val prefsRepo2 = UserPreferencesRepository(context)
    val prefs = prefsRepo2.preferencesFlow.first()
    assertFalse(prefs.isDarkTheme)
    assertEquals(AppLanguage.BENGALI, prefs.language)
    assertEquals("Aroby", prefs.userName)

    db2.close()
    context.deleteDatabase(dbName)
  }
}
