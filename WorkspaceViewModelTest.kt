package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DistractionFreeDatabase
import com.example.data.repository.ProductivityRepository
import com.example.ui.viewmodel.NoteFilter
import com.example.ui.viewmodel.TaskFilter
import com.example.ui.viewmodel.WorkspaceViewModel
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WorkspaceViewModelTest {

  private val mainDispatcher = UnconfinedTestDispatcher()
  private lateinit var db: DistractionFreeDatabase
  private lateinit var viewModel: WorkspaceViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(mainDispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DistractionFreeDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    viewModel = WorkspaceViewModel(ProductivityRepository(db))
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    db.close()
  }

  @Test
  fun `tasks, notes, and habits CRUD and filters work in WorkspaceViewModel`() = runBlocking {
    val today = DateUtils.todayEpochDay()
    viewModel.saveTask(0L, "Physics Ch 4", "Solve exercises", "Physics", 2, today)
    val withTask = withTimeout(5000L) { viewModel.uiState.first { it.tasks.isNotEmpty() } }
    assertEquals("Physics Ch 4", withTask.tasks.first().title)

    viewModel.toggleTask(withTask.tasks.first())
    viewModel.setTaskFilter(TaskFilter.COMPLETED)
    val completedFiltered =
      withTimeout(5000L) {
        viewModel.uiState.first {
          it.taskFilter == TaskFilter.COMPLETED && it.tasks.any { t -> t.isCompleted }
        }
      }
    assertEquals(1, completedFiltered.tasks.size)

    viewModel.deleteTask(completedFiltered.tasks.first())
    val emptyTasks = withTimeout(5000L) { viewModel.uiState.first { it.tasks.isEmpty() } }
    assertTrue(emptyTasks.tasks.isEmpty())

    viewModel.saveNote(0L, "Ohm Law", "V = IR", "Formula", 1, false)
    withTimeout(5000L) { viewModel.uiState.first { it.notes.isNotEmpty() } }
    viewModel.setNoteFilter(NoteFilter.FORMULA)
    viewModel.setNoteSearchQuery("Ohm")
    val filteredNotes =
      withTimeout(5000L) {
        viewModel.uiState.first { it.notes.size == 1 && it.noteFilter == NoteFilter.FORMULA }
      }
    assertEquals("Ohm Law", filteredNotes.notes.first().title)

    viewModel.createHabit("Daily 20 MCQs", "book")
    val withHabit = withTimeout(5000L) { viewModel.uiState.first { it.habits.isNotEmpty() } }
    viewModel.toggleHabitToday(withHabit.habits.first())
    val streaked =
      withTimeout(5000L) { viewModel.uiState.first { it.habits.firstOrNull()?.currentStreak == 1 } }
    assertEquals(1, streaked.habits.first().currentStreak)
  }
}
