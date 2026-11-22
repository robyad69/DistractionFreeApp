package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.entity.FocusSessionEntity
import com.example.data.local.entity.SscSubjectEntity
import com.example.data.repository.ProductivityRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FocusTimerMode(val dbKey: String, val defaultMinutes: Int) {
  POMODORO("POMODORO", 25),
  SHORT_BREAK("SHORT_BREAK", 5),
  CUSTOM("CUSTOM", 45),
}

data class FocusUiState(
  val mode: FocusTimerMode = FocusTimerMode.POMODORO,
  val plannedMinutes: Int = 25,
  val customMinutes: Int = 45,
  val remainingSeconds: Int = 25 * 60,
  val totalSeconds: Int = 25 * 60,
  val isRunning: Boolean = false,
  val isPaused: Boolean = false,
  val selectedSubjectEn: String = "General",
  val subjects: List<SscSubjectEntity> = emptyList(),
  val sessionHistory: List<FocusSessionEntity> = emptyList(),
)

sealed class FocusTimerEvent {
  data class Completed(val xpEarned: Int, val auraEarned: Int, val isBreak: Boolean) : FocusTimerEvent()

  data class Error(val message: String) : FocusTimerEvent()
}

class FocusViewModel(
  private val repository: ProductivityRepository,
  private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

  private val timerState = MutableStateFlow(FocusUiState())
  private var tickerJob: Job? = null

  private val _events = MutableSharedFlow<FocusTimerEvent>()
  val events: SharedFlow<FocusTimerEvent> = _events.asSharedFlow()

  val uiState: StateFlow<FocusUiState> =
    combine(
        timerState,
        repository.sscSubjectsFlow,
        repository.focusSessionsFlow,
        preferencesRepository.preferencesFlow,
      ) { timer, subjects, history, prefs ->
        val effectiveCustom = prefs.customFocusMinutes
        val updatedTotal =
          if (!timer.isRunning && !timer.isPaused && timer.mode == FocusTimerMode.CUSTOM) {
            effectiveCustom * 60
          } else {
            timer.totalSeconds
          }
        val updatedRemaining =
          if (!timer.isRunning && !timer.isPaused && timer.mode == FocusTimerMode.CUSTOM) {
            effectiveCustom * 60
          } else {
            timer.remainingSeconds
          }
        timer.copy(
          customMinutes = effectiveCustom,
          plannedMinutes = if (timer.mode == FocusTimerMode.CUSTOM) effectiveCustom else timer.plannedMinutes,
          totalSeconds = updatedTotal,
          remainingSeconds = updatedRemaining,
          subjects = subjects,
          sessionHistory = history,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FocusUiState(),
      )

  fun selectMode(mode: FocusTimerMode) {
    if (timerState.value.isRunning) return
    val mins =
      when (mode) {
        FocusTimerMode.POMODORO -> 25
        FocusTimerMode.SHORT_BREAK -> 5
        FocusTimerMode.CUSTOM -> uiState.value.customMinutes
      }
    tickerJob?.cancel()
    timerState.update {
      it.copy(
        mode = mode,
        plannedMinutes = mins,
        totalSeconds = mins * 60,
        remainingSeconds = mins * 60,
        isRunning = false,
        isPaused = false,
      )
    }
  }

  fun setCustomMinutes(minutes: Int) {
    val clamped = minutes.coerceIn(5, 120)
    viewModelScope.launch { runCatching { preferencesRepository.setCustomFocusMinutes(clamped) } }
    if (!timerState.value.isRunning && !timerState.value.isPaused && timerState.value.mode == FocusTimerMode.CUSTOM) {
      timerState.update {
        it.copy(
          customMinutes = clamped,
          plannedMinutes = clamped,
          totalSeconds = clamped * 60,
          remainingSeconds = clamped * 60,
        )
      }
    }
  }

  fun selectSubject(subjectNameEn: String) {
    timerState.update { it.copy(selectedSubjectEn = subjectNameEn) }
  }

  fun startOrResume() {
    if (timerState.value.isRunning) return
    timerState.update { it.copy(isRunning = true, isPaused = false) }
    tickerJob?.cancel()
    tickerJob =
      viewModelScope.launch {
        while (timerState.value.isRunning && timerState.value.remainingSeconds > 0) {
          delay(1000L)
          if (!timerState.value.isRunning) break
          var reachedZero = false
          timerState.update { cur ->
            val next = (cur.remainingSeconds - 1).coerceAtLeast(0)
            if (next == 0) reachedZero = true
            cur.copy(remainingSeconds = next)
          }
          if (reachedZero) {
            onTimerReachedZero()
            break
          }
        }
      }
  }

  fun pause() {
    tickerJob?.cancel()
    timerState.update { it.copy(isRunning = false, isPaused = true) }
  }

  fun reset() {
    tickerJob?.cancel()
    timerState.update { cur ->
      val mins =
        when (cur.mode) {
          FocusTimerMode.POMODORO -> 25
          FocusTimerMode.SHORT_BREAK -> 5
          FocusTimerMode.CUSTOM -> uiState.value.customMinutes
        }
      cur.copy(
        plannedMinutes = mins,
        totalSeconds = mins * 60,
        remainingSeconds = mins * 60,
        isRunning = false,
        isPaused = false,
      )
    }
  }

  fun finishEarlyAndSave() {
    val cur = timerState.value
    val elapsedSeconds = (cur.totalSeconds - cur.remainingSeconds).coerceAtLeast(0)
    if (elapsedSeconds < 30) {
      reset()
      return
    }
    tickerJob?.cancel()
    saveSession(
      mode = cur.mode,
      plannedMinutes = cur.plannedMinutes,
      actualSeconds = elapsedSeconds,
      subjectEn = cur.selectedSubjectEn,
      autoCycleNext = false,
    )
  }

  private fun onTimerReachedZero() {
    val cur = timerState.value
    saveSession(
      mode = cur.mode,
      plannedMinutes = cur.plannedMinutes,
      actualSeconds = cur.totalSeconds,
      subjectEn = cur.selectedSubjectEn,
      autoCycleNext = true,
    )
  }

  private fun saveSession(
    mode: FocusTimerMode,
    plannedMinutes: Int,
    actualSeconds: Int,
    subjectEn: String,
    autoCycleNext: Boolean,
  ) {
    viewModelScope.launch {
      runCatching {
        repository.recordFocusSession(
          mode = mode.dbKey,
          plannedMinutes = plannedMinutes,
          actualSeconds = actualSeconds,
          subjectTag = subjectEn,
        )
      }.onSuccess { (xp, aura) ->
        if (autoCycleNext && mode == FocusTimerMode.POMODORO) {
          selectMode(FocusTimerMode.SHORT_BREAK)
        } else if (autoCycleNext && mode == FocusTimerMode.SHORT_BREAK) {
          selectMode(FocusTimerMode.POMODORO)
        } else {
          reset()
        }
        _events.emit(FocusTimerEvent.Completed(xp, aura, mode == FocusTimerMode.SHORT_BREAK))
      }.onFailure { err ->
        reset()
        _events.emit(FocusTimerEvent.Error(err.localizedMessage ?: "Failed to save session"))
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    tickerJob?.cancel()
  }
}
