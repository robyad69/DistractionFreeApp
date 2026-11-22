package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.di.AppContainer
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.localization.stringsFor
import com.example.ui.navigation.DistractionFreeRoot
import com.example.ui.theme.DistractionFreeTheme
import com.example.ui.viewmodel.AppViewModelFactory
import com.example.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val appContainer = AppContainer(applicationContext)
    val factory = AppViewModelFactory(appContainer)

    setContent {
      val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
      val prefs by settingsViewModel.preferences.collectAsStateWithLifecycle()

      LaunchedEffect(prefs.isDarkTheme) {
        enableEdgeToEdge(
          statusBarStyle =
            if (prefs.isDarkTheme) {
              SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            } else {
              SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
              )
            },
          navigationBarStyle =
            if (prefs.isDarkTheme) {
              SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            } else {
              SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
              )
            },
        )
      }

      val localizedStrings = stringsFor(prefs.language)
      CompositionLocalProvider(
        LocalAppStrings provides localizedStrings,
        LocalAppLanguage provides prefs.language,
      ) {
        DistractionFreeTheme(darkTheme = prefs.isDarkTheme, language = prefs.language) {
          DistractionFreeRoot(factory = factory, settingsViewModel = settingsViewModel)
        }
      }
    }
  }
}
