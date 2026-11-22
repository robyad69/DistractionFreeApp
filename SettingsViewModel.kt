package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.entity.UserStatsEntity
import com.example.data.repository.ProductivityRepository
import com.example.localization.AppLanguage
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
  private val preferencesRepository: UserPreferencesRepository,
  private val productivityRepository: ProductivityRepository,
) : ViewModel() {

  init {
    viewModelScope.launch {
      runCatching { productivityRepository.ensureInitialized() }
    }
  }

  val preferences: StateFlow<UserPreferences> =
    preferencesRepository.preferencesFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UserPreferences(),
    )

  val userStats: StateFlow<UserStatsEntity> =
    productivityRepository.userStatsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UserStatsEntity(sscExamEpochDay = DateUtils.defaultSscExamEpochDay()),
    )

  private val _events = MutableSharedFlow<String>()
  val events: SharedFlow<String> = _events.asSharedFlow()

  fun setDarkTheme(enabled: Boolean) {
    viewModelScope.launch {
      runCatching { preferencesRepository.setDarkTheme(enabled) }
    }
  }

  fun toggleLanguage() {
    viewModelScope.launch {
      val next =
        if (preferences.value.language == AppLanguage.ENGLISH) AppLanguage.BENGALI
        else AppLanguage.ENGLISH
      runCatching { preferencesRepository.setLanguage(next) }
    }
  }

  fun setLanguage(language: AppLanguage) {
    viewModelScope.launch {
      runCatching { preferencesRepository.setLanguage(language) }
    }
  }

  fun setNotificationsEnabled(enabled: Boolean) {
    viewModelScope.launch {
      runCatching { preferencesRepository.setNotificationsEnabled(enabled) }
    }
  }

  fun setFocusAlertsEnabled(enabled: Boolean) {
    viewModelScope.launch {
      runCatching { preferencesRepository.setFocusAlertsEnabled(enabled) }
    }
  }

  fun setSoundVibrationEnabled(enabled: Boolean) {
    viewModelScope.launch {
      runCatching { preferencesRepository.setSoundVibrationEnabled(enabled) }
    }
  }

  fun updateUserName(name: String) {
    viewModelScope.launch {
      runCatching { preferencesRepository.setUserName(name) }
    }
  }

  fun updateDailyGoal(minutes: Int) {
    viewModelScope.launch {
      runCatching { productivityRepository.updateDailyStudyGoal(minutes) }
    }
  }

  fun updateSscExamDate(epochDay: Long) {
    viewModelScope.launch {
      runCatching { productivityRepository.updateSscExamEpochDay(epochDay) }
    }
  }

  fun resetAllData(doneMessage: String) {
    viewModelScope.launch {
      runCatching { productivityRepository.resetAllData() }
        .onSuccess { _events.emit(doneMessage) }
        .onFailure { _events.emit(it.localizedMessage ?: "Reset failed") }
    }
  }
}
