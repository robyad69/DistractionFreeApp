package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserStatsEntity
import com.example.data.repository.ProductivityRepository
import com.example.domain.model.GamificationCalculator
import com.example.domain.model.LevelInfo
import com.example.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
  val userName: String = "Scholar",
  val todayStudyMinutes: Int = 0,
  val dailyGoalMinutes: Int = 120,
  val dailyProgressFraction: Float = 0f,
  val xp: Int = 0,
  val aura: Int = 0,
  val currentStreak: Int = 0,
  val levelInfo: LevelInfo = GamificationCalculator.calculateLevel(0),
  val pendingTasksToday: List<TaskEntity> = emptyList(),
  val habits: List<HabitEntity> = emptyList(),
  val todayHabitIds: Set<Long> = emptySet(),
  val sscDaysRemaining: Long = 90L,
  val sscCompletionFraction: Float = 0f,
  val isLoading: Boolean = true,
)

class DashboardViewModel(
  private val repository: ProductivityRepository,
  preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

  init {
    viewModelScope.launch { runCatching { repository.ensureInitialized() } }
  }

  private val baseFlow =
    combine(
      preferencesRepository.preferencesFlow,
      repository.userStatsFlow,
      repository.focusSessionsFlow,
      repository.tasksFlow,
    ) { prefs, stats, sessions, tasks ->
      Quad(prefs, stats, sessions, tasks)
    }

  val uiState: StateFlow<DashboardUiState> =
    combine(
        baseFlow,
        repository.habitsFlow,
        repository.observeRecentHabitCompletions(daysBack = 1),
        repository.sscChaptersFlow,
      ) { quad, habits, todayHabitCompletions, chapters ->
        val (prefs, stats, sessions, tasks) = quad
        val today = DateUtils.todayEpochDay()
        val todayMinutes =
          sessions
            .filter { it.epochDay == today && it.mode != "SHORT_BREAK" }
            .sumOf { (it.actualSecondsCompleted / 60).coerceAtLeast(1) }
        val goal = stats.dailyStudyGoalMinutes.coerceAtLeast(15)
        val progress = (todayMinutes.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
        val pendingToday = tasks.filter { !it.isCompleted }.take(4)
        val doneHabitIds = todayHabitCompletions.filter { it.epochDay == today }.map { it.habitId }.toSet()
        val completedChapters = chapters.count { it.isCompleted }
        val totalChapters = chapters.size.coerceAtLeast(1)
        val examDaysLeft = (stats.sscExamEpochDay - today).coerceAtLeast(0L)

        DashboardUiState(
          userName = prefs.userName,
          todayStudyMinutes = todayMinutes,
          dailyGoalMinutes = goal,
          dailyProgressFraction = progress,
          xp = stats.xp,
          aura = stats.aura,
          currentStreak = stats.currentStreak,
          levelInfo = GamificationCalculator.calculateLevel(stats.xp),
          pendingTasksToday = pendingToday,
          habits = habits.take(4),
          todayHabitIds = doneHabitIds,
          sscDaysRemaining = examDaysLeft,
          sscCompletionFraction = (completedChapters.toFloat() / totalChapters.toFloat()).coerceIn(0f, 1f),
          isLoading = false,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(),
      )

  fun toggleTask(task: TaskEntity) {
    viewModelScope.launch { runCatching { repository.toggleTaskCompleted(task) } }
  }

  fun toggleHabit(habit: HabitEntity) {
    viewModelScope.launch { runCatching { repository.toggleHabitToday(habit) } }
  }

  private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
