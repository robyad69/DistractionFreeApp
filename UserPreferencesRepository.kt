package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.localization.AppLanguage
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "distraction_free_prefs")

data class UserPreferences(
  val isDarkTheme: Boolean = true,
  val language: AppLanguage = AppLanguage.ENGLISH,
  val notificationsEnabled: Boolean = true,
  val focusAlertsEnabled: Boolean = true,
  val soundVibrationEnabled: Boolean = true,
  val userName: String = "Scholar",
  val customFocusMinutes: Int = 45,
)

class UserPreferencesRepository(private val context: Context) {

  private object Keys {
    val DARK_THEME = booleanPreferencesKey("dark_theme")
    val LANGUAGE_CODE = stringPreferencesKey("language_code")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val FOCUS_ALERTS_ENABLED = booleanPreferencesKey("focus_alerts_enabled")
    val SOUND_VIBRATION_ENABLED = booleanPreferencesKey("sound_vibration_enabled")
    val USER_NAME = stringPreferencesKey("user_name")
    val CUSTOM_FOCUS_MINUTES = intPreferencesKey("custom_focus_minutes")
  }

  val preferencesFlow: Flow<UserPreferences> =
    context.userPreferencesDataStore.data
      .catch { exception ->
        if (exception is IOException) {
          emit(emptyPreferences())
        } else {
          throw exception
        }
      }
      .map { prefs ->
        UserPreferences(
          isDarkTheme = prefs[Keys.DARK_THEME] ?: true,
          language = AppLanguage.fromCode(prefs[Keys.LANGUAGE_CODE] ?: "en"),
          notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
          focusAlertsEnabled = prefs[Keys.FOCUS_ALERTS_ENABLED] ?: true,
          soundVibrationEnabled = prefs[Keys.SOUND_VIBRATION_ENABLED] ?: true,
          userName = prefs[Keys.USER_NAME] ?: "Scholar",
          customFocusMinutes = prefs[Keys.CUSTOM_FOCUS_MINUTES] ?: 45,
        )
      }

  suspend fun setDarkTheme(enabled: Boolean) {
    context.userPreferencesDataStore.edit { it[Keys.DARK_THEME] = enabled }
  }

  suspend fun setLanguage(language: AppLanguage) {
    context.userPreferencesDataStore.edit { it[Keys.LANGUAGE_CODE] = language.code }
  }

  suspend fun setNotificationsEnabled(enabled: Boolean) {
    context.userPreferencesDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
  }

  suspend fun setFocusAlertsEnabled(enabled: Boolean) {
    context.userPreferencesDataStore.edit { it[Keys.FOCUS_ALERTS_ENABLED] = enabled }
  }

  suspend fun setSoundVibrationEnabled(enabled: Boolean) {
    context.userPreferencesDataStore.edit { it[Keys.SOUND_VIBRATION_ENABLED] = enabled }
  }

  suspend fun setUserName(name: String) {
    val clean = name.trim().take(28).ifEmpty { "Scholar" }
    context.userPreferencesDataStore.edit { it[Keys.USER_NAME] = clean }
  }

  suspend fun setCustomFocusMinutes(minutes: Int) {
    context.userPreferencesDataStore.edit {
      it[Keys.CUSTOM_FOCUS_MINUTES] = minutes.coerceIn(5, 120)
    }
  }
}
