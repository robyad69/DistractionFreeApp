package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DistractionFreeDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.ProductivityRepository
import com.example.ui.viewmodel.FocusTimerMode
import com.example.ui.viewmodel.FocusViewModel
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FocusViewModelTest {

  private val mainExecutor = Executors.newSingleThreadExecutor()
  private val mainDispatcher = mainExecutor.asCoroutineDispatcher()
  private lateinit var db: DistractionFreeDatabase
  private lateinit var viewModel: FocusViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(mainDispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DistractionFreeDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val repo = ProductivityRepository(db)
    val prefs = UserPreferencesRepository(context)
    viewModel = FocusViewModel(repo, prefs)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    mainExecutor.shutdownNow()
    db.close()
  }

  @Test
  fun `pomodoro timer counts down, pauses, and resets`() = runBlocking {
    viewModel.selectMode(FocusTimerMode.POMODORO)
    viewModel.selectSubject("Physics")
    viewModel.startOrResume()

    val runningState = withTimeout(5000L) { viewModel.uiState.first { it.isRunning } }
    assertTrue(runningState.isRunning)

    val tickedState =
      withTimeout(5000L) { viewModel.uiState.first { it.remainingSeconds < 25 * 60 } }
    assertTrue(tickedState.remainingSeconds < 25 * 60)

    viewModel.pause()
    val pausedState = withTimeout(5000L) { viewModel.uiState.first { !it.isRunning && it.isPaused } }
    assertFalse(pausedState.isRunning)
    assertTrue(pausedState.isPaused)

    viewModel.reset()
    val resetState =
      withTimeout(5000L) {
        viewModel.uiState.first { !it.isPaused && it.remainingSeconds == 25 * 60 }
      }
    assertEquals(25 * 60, resetState.remainingSeconds)
  }

  @Test
  fun `custom duration updates remaining seconds when idle`() = runBlocking {
    viewModel.selectMode(FocusTimerMode.CUSTOM)
    viewModel.setCustomMinutes(60)
    val customState =
      withTimeout(5000L) {
        viewModel.uiState.first { it.mode == FocusTimerMode.CUSTOM && it.remainingSeconds == 60 * 60 }
      }
    assertEquals(60 * 60, customState.remainingSeconds)
  }
}
