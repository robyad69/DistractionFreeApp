package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.di.AppContainer

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return when {
      modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
        SettingsViewModel(container.preferencesRepository, container.productivityRepository) as T
      modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
        DashboardViewModel(container.productivityRepository, container.preferencesRepository) as T
      modelClass.isAssignableFrom(FocusViewModel::class.java) ->
        FocusViewModel(container.productivityRepository, container.preferencesRepository) as T
      modelClass.isAssignableFrom(WorkspaceViewModel::class.java) ->
        WorkspaceViewModel(container.productivityRepository) as T
      modelClass.isAssignableFrom(SscViewModel::class.java) ->
        SscViewModel(container.productivityRepository) as T
      modelClass.isAssignableFrom(InsightsViewModel::class.java) ->
        InsightsViewModel(container.productivityRepository, container.preferencesRepository) as T
      else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}
