package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.ProductivityRepository
import com.example.domain.model.AchievementBadge
import com.example.domain.model.DailyReportPoint
import com.example.domain.model.GamificationCalculator
import com.example.domain.model.LevelInfo
import com.example.domain.model.SubjectStudySummary
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ReportPeriod {
  DAILY,
  WEEKLY,
}

data class InsightsUiState(
  val period: ReportPeriod = ReportPeriod.WEEKLY,
  val periodStudyMinutes: Int = 0,
  val periodSessionsCount: Int = 0,
  val avgSessionMinutes: Int = 0,
  val tasksCompletedTotal: Int = 0,
  val habitsCheckedTotal: Int = 0,
  val weeklyChartPoints: List<DailyReportPoint> = emptyList(),
  val subjectBreakdown: List<SubjectStudySummary> = emptyList(),
  val xp: Int = 0,
  val aura: Int = 0,
  val currentStreak: Int = 0,
  val bestStreak: Int = 0,
  val levelInfo: LevelInfo = GamificationCalculator.calculateLevel(0),
  val badges: List<AchievementBadge> = emptyList(),
)

class InsightsViewModel(
  repository: ProductivityRepository,
  preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

  private val periodFlow = MutableStateFlow(ReportPeriod.WEEKLY)

  private val countsFlow =
    combine(
      repository.sscChaptersFlow,
      repository.notesCountFlow,
      preferencesRepository.preferencesFlow,
    ) { chapters, notesCount, prefs ->
      Triple(chapters.count { it.isCompleted }, notesCount, prefs.language)
    }

  val uiState: StateFlow<InsightsUiState> =
    combine(
        periodFlow,
        repository.focusSessionsFlow,
        repository.userStatsFlow,
        countsFlow,
      ) { period, sessions, stats, (completedChapters, notesCount, language) ->
        val today = DateUtils.todayEpochDay()
        val startDay = if (period == ReportPeriod.DAILY) today else today - 6L
        val studySessions =
          sessions.filter { it.epochDay in startDay..today && it.mode != "SHORT_BREAK" }

        val periodMinutes =
          studySessions.sumOf { (it.actualSecondsCompleted / 60).coerceAtLeast(1) }
        val periodSessionsCount = studySessions.size
        val avgSession =
          if (periodSessionsCount > 0) periodMinutes / periodSessionsCount else 0

        val chartPoints =
          (0..6).map { offset ->
            val day = today - (6 - offset)
            val mins =
              sessions
                .filter { it.epochDay == day && it.mode != "SHORT_BREAK" }
                .sumOf { (it.actualSecondsCompleted / 60).coerceAtLeast(1) }
            DailyReportPoint(
              epochDay = day,
              dayLabel = DateUtils.formatShortDayLabel(day, language),
              minutes = mins,
            )
          }

        val bySubjectRaw =
          studySessions
            .groupBy { it.subjectTag }
            .mapValues { (_, list) ->
              list.sumOf { (it.actualSecondsCompleted / 60).coerceAtLeast(1) }
            }
        val maxSubjMins = (bySubjectRaw.values.maxOrNull() ?: 1).coerceAtLeast(1)
        val subjectBreakdown =
          bySubjectRaw.entries
            .sortedByDescending { it.value }
            .map { (tag, mins) ->
              SubjectStudySummary(
                subjectTag = tag,
                totalMinutes = mins,
                fraction = (mins.toFloat() / maxSubjMins.toFloat()).coerceIn(0.05f, 1f),
              )
            }

        val levelInfo = GamificationCalculator.calculateLevel(stats.xp)
        val badges =
          GamificationCalculator.computeBadges(
            totalSessions = stats.totalSessionsCompleted,
            totalFocusMinutes = stats.totalFocusMinutes,
            totalTasksCompleted = stats.totalTasksCompleted,
            bestStreak = stats.bestStreak,
            completedChapters = completedChapters,
            notesCount = notesCount,
            aura = stats.aura,
          )

        InsightsUiState(
          period = period,
          periodStudyMinutes = periodMinutes,
          periodSessionsCount = periodSessionsCount,
          avgSessionMinutes = avgSession,
          tasksCompletedTotal = stats.totalTasksCompleted,
          habitsCheckedTotal = stats.totalHabitsCompleted,
          weeklyChartPoints = chartPoints,
          subjectBreakdown = subjectBreakdown,
          xp = stats.xp,
          aura = stats.aura,
          currentStreak = stats.currentStreak,
          bestStreak = stats.bestStreak,
          levelInfo = levelInfo,
          badges = badges,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState(),
      )

  fun setPeriod(period: ReportPeriod) {
    periodFlow.value = period
  }
}
