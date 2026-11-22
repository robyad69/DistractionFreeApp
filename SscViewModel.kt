package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.SscChapterEntity
import com.example.data.local.entity.SscSubjectEntity
import com.example.data.repository.ProductivityRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SscSubjectWithChapters(
  val subject: SscSubjectEntity,
  val chapters: List<SscChapterEntity>,
  val completedCount: Int,
  val totalCount: Int,
  val progressFraction: Float,
)

data class SscUiState(
  val daysRemaining: Long = 90L,
  val examEpochDay: Long = DateUtils.defaultSscExamEpochDay(),
  val dailyGoalMinutes: Int = 120,
  val todayStudyMinutes: Int = 0,
  val totalChaptersCompleted: Int = 0,
  val totalChapters: Int = 0,
  val overallProgressFraction: Float = 0f,
  val subjects: List<SscSubjectWithChapters> = emptyList(),
)

class SscViewModel(private val repository: ProductivityRepository) : ViewModel() {

  init {
    viewModelScope.launch { runCatching { repository.ensureInitialized() } }
  }

  val uiState: StateFlow<SscUiState> =
    combine(
        repository.sscSubjectsFlow,
        repository.sscChaptersFlow,
        repository.userStatsFlow,
        repository.focusSessionsFlow,
      ) { subjects, chapters, stats, sessions ->
        val today = DateUtils.todayEpochDay()
        val todayMinutes =
          sessions
            .filter { it.epochDay == today && it.mode != "SHORT_BREAK" }
            .sumOf { (it.actualSecondsCompleted / 60).coerceAtLeast(1) }

        val grouped = chapters.groupBy { it.subjectId }
        val subjectModels =
          subjects.map { subj ->
            val chaps = grouped[subj.id].orEmpty()
            val done = chaps.count { it.isCompleted }
            val total = chaps.size
            val frac = if (total > 0) done.toFloat() / total.toFloat() else 0f
            SscSubjectWithChapters(
              subject = subj,
              chapters = chaps,
              completedCount = done,
              totalCount = total,
              progressFraction = frac,
            )
          }

        val totalDone = chapters.count { it.isCompleted }
        val totalAll = chapters.size
        val overallFrac = if (totalAll > 0) totalDone.toFloat() / totalAll.toFloat() else 0f

        SscUiState(
          daysRemaining = (stats.sscExamEpochDay - today).coerceAtLeast(0L),
          examEpochDay = stats.sscExamEpochDay,
          dailyGoalMinutes = stats.dailyStudyGoalMinutes,
          todayStudyMinutes = todayMinutes,
          totalChaptersCompleted = totalDone,
          totalChapters = totalAll,
          overallProgressFraction = overallFrac,
          subjects = subjectModels,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SscUiState(),
      )

  fun toggleChapter(chapter: SscChapterEntity) {
    viewModelScope.launch { runCatching { repository.toggleChapterCompleted(chapter) } }
  }

  fun addCustomChapter(subjectId: Long, title: String, nextOrder: Int) {
    viewModelScope.launch { runCatching { repository.addCustomChapter(subjectId, title, nextOrder) } }
  }

  fun updateExamEpochDay(epochDay: Long) {
    viewModelScope.launch { runCatching { repository.updateSscExamEpochDay(epochDay) } }
  }

  fun updateDailyGoal(minutes: Int) {
    viewModelScope.launch { runCatching { repository.updateDailyStudyGoal(minutes) } }
  }
}
