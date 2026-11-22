package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.localization.AppLanguage

@Immutable
data class ExtendedColors(
  val cardElevated: Color,
  val subtleBorder: Color,
  val mutedText: Color,
  val purpleAccent: Color,
  val blueAccent: Color,
  val cyanAccent: Color,
  val success: Color,
  val warning: Color,
  val danger: Color,
  val isDark: Boolean,
)

private val DarkExtendedColors =
  ExtendedColors(
    cardElevated = DarkSurfaceElevated,
    subtleBorder = DarkOutline,
    mutedText = DarkOnSurfaceMuted,
    purpleAccent = SoftViolet,
    blueAccent = VividBlue,
    cyanAccent = CyanAura,
    success = EmeraldSuccess,
    warning = AmberStreak,
    danger = RoseAlert,
    isDark = true,
  )

private val LightExtendedColors =
  ExtendedColors(
    cardElevated = Color(0xFFFFFFFF),
    subtleBorder = LightOutline,
    mutedText = LightOnSurfaceMuted,
    purpleAccent = LightPurplePrimary,
    blueAccent = LightBlueSecondary,
    cyanAccent = LightCyanTertiary,
    success = Color(0xFF059669),
    warning = Color(0xFFD97706),
    danger = Color(0xFFE11D48),
    isDark = false,
  )

val LocalExtendedColors = staticCompositionLocalOf { DarkExtendedColors }

private val MidnightDarkColorScheme =
  darkColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = DeepPurpleContainer,
    onPrimaryContainer = SoftViolet,
    secondary = VividBlue,
    onSecondary = Color.White,
    secondaryContainer = DeepBlueContainer,
    onSecondaryContainer = SkyBlueAccent,
    tertiary = CyanAura,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = DarkOutline,
    error = RoseAlert,
    onError = Color.White,
  )

private val DaylightLightColorScheme =
  lightColorScheme(
    primary = LightPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = LightPurpleContainer,
    onPrimaryContainer = Color(0xFF3B0764),
    secondary = LightBlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightBlueContainer,
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = LightCyanTertiary,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceMuted,
    outline = LightOutline,
    error = Color(0xFFE11D48),
    onError = Color.White,
  )

@Composable
fun DistractionFreeTheme(
  darkTheme: Boolean = true,
  language: AppLanguage = AppLanguage.ENGLISH,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) MidnightDarkColorScheme else DaylightLightColorScheme
  val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors
  val typography = buildAppTypography(isBengali = language == AppLanguage.BENGALI)

  CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
    MaterialTheme(colorScheme = colorScheme, typography = typography, content = content)
  }
}
