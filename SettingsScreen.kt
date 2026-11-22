package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.UserPreferences
import com.example.data.local.entity.UserStatsEntity
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.components.AdaptiveContentBox
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.LocalExtendedColors
import com.example.util.DateUtils
import com.example.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  preferences: UserPreferences,
  stats: UserStatsEntity,
  onSetDarkTheme: (Boolean) -> Unit,
  onSetLanguage: (AppLanguage) -> Unit,
  onSetNotificationsEnabled: (Boolean) -> Unit,
  onSetFocusAlertsEnabled: (Boolean) -> Unit,
  onSetSoundVibrationEnabled: (Boolean) -> Unit,
  onUpdateUserName: (String) -> Unit,
  onUpdateDailyGoal: (Int) -> Unit,
  onUpdateSscExamDate: (Long) -> Unit,
  onResetAllData: () -> Unit,
) {
  val strings = LocalAppStrings.current
  val ext = LocalExtendedColors.current
  val context = LocalContext.current

  var hasNotifPerm by remember {
    mutableStateOf(NotificationHelper.hasNotificationPermission(context))
  }
  val permissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      hasNotifPerm = granted
      if (granted) {
        onSetNotificationsEnabled(true)
      }
    }

  var showNameDialog by remember { mutableStateOf(false) }
  var showGoalDialog by remember { mutableStateOf(false) }
  var showDatePicker by remember { mutableStateOf(false) }
  var showResetConfirm by remember { mutableStateOf(false) }

  AdaptiveContentBox {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Appearance & Language
      item(key = "section_appearance") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SectionHeader(title = strings.sectionAppearance)
          GlassCard {
            SettingSwitchRow(
              icon = Icons.Filled.DarkMode,
              title = strings.darkThemeLabel,
              subtitle = strings.darkThemeDesc,
              checked = preferences.isDarkTheme,
              onCheckedChange = onSetDarkTheme,
              testTag = "settings_dark_theme_switch",
            )
            Spacer(Modifier.height(14.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Filled.Language,
                contentDescription = strings.languageLabel,
                tint = MaterialTheme.colorScheme.primary,
              )
              Spacer(Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(strings.languageLabel, style = MaterialTheme.typography.titleMedium)
                Text(
                  strings.languageDesc,
                  style = MaterialTheme.typography.bodySmall,
                  color = ext.mutedText,
                )
              }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              AppLanguage.entries.forEach { lang ->
                FilterChip(
                  selected = preferences.language == lang,
                  onClick = { onSetLanguage(lang) },
                  label = { Text(lang.nativeName) },
                  modifier = Modifier.testTag("settings_lang_${lang.code}"),
                )
              }
            }
          }
        }
      }

      // Notifications
      item(key = "section_notifications") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SectionHeader(title = strings.sectionNotifications)
          GlassCard {
            SettingSwitchRow(
              icon = Icons.Filled.Notifications,
              title = strings.notificationsMaster,
              subtitle = strings.notificationsMasterDesc,
              checked = preferences.notificationsEnabled,
              onCheckedChange = { enabled ->
                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPerm) {
                  permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                  onSetNotificationsEnabled(enabled)
                }
              },
              testTag = "settings_notifications_switch",
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPerm) {
              Spacer(Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = strings.permissionNeededHint,
                  style = MaterialTheme.typography.bodySmall,
                  color = ext.warning,
                  modifier = Modifier.weight(1f),
                )
                TextButton(
                  onClick = {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  }
                ) {
                  Text(strings.grantPermission)
                }
              }
            }
            Spacer(Modifier.height(12.dp))
            SettingSwitchRow(
              icon = Icons.Filled.NotificationsActive,
              title = strings.focusAlertsLabel,
              subtitle = strings.focusAlertsDesc,
              checked = preferences.focusAlertsEnabled,
              onCheckedChange = onSetFocusAlertsEnabled,
              testTag = "settings_focus_alerts_switch",
            )
            Spacer(Modifier.height(12.dp))
            SettingSwitchRow(
              icon = Icons.Filled.Vibration,
              title = strings.soundVibrationLabel,
              subtitle = strings.soundVibrationDesc,
              checked = preferences.soundVibrationEnabled,
              onCheckedChange = onSetSoundVibrationEnabled,
              testTag = "settings_haptic_switch",
            )
          }
        }
      }

      // Profile & Goals
      item(key = "section_profile") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SectionHeader(title = strings.sectionProfileGoals)
          GlassCard {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = strings.displayNameLabel,
                tint = MaterialTheme.colorScheme.primary,
              )
              Spacer(Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(strings.displayNameLabel, style = MaterialTheme.typography.labelMedium, color = ext.mutedText)
                Text(preferences.userName, style = MaterialTheme.typography.titleMedium)
              }
              IconButton(
                onClick = { showNameDialog = true },
                modifier = Modifier.minimumInteractiveComponentSize().testTag("settings_edit_name_btn"),
              ) {
                Icon(Icons.Filled.Edit, contentDescription = strings.edit)
              }
            }
            Spacer(Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = strings.sscExamDateLabel,
                tint = MaterialTheme.colorScheme.secondary,
              )
              Spacer(Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(strings.sscExamDateLabel, style = MaterialTheme.typography.labelMedium, color = ext.mutedText)
                Text(
                  DateUtils.formatEpochDay(stats.sscExamEpochDay, preferences.language),
                  style = MaterialTheme.typography.titleMedium,
                )
              }
              IconButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.minimumInteractiveComponentSize().testTag("settings_edit_date_btn"),
              ) {
                Icon(Icons.Filled.Edit, contentDescription = strings.edit)
              }
            }
            Spacer(Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  strings.dailyGoalMinutesLabel,
                  style = MaterialTheme.typography.labelMedium,
                  color = ext.mutedText,
                )
                Text(
                  DateUtils.formatDurationMinutes(stats.dailyStudyGoalMinutes, preferences.language),
                  style = MaterialTheme.typography.titleMedium,
                )
              }
              IconButton(
                onClick = { showGoalDialog = true },
                modifier = Modifier.minimumInteractiveComponentSize().testTag("settings_edit_goal_btn"),
              ) {
                Icon(Icons.Filled.Edit, contentDescription = strings.edit)
              }
            }
          }
        }
      }

      // Offline Privacy & Reset
      item(key = "section_privacy") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SectionHeader(title = strings.sectionPrivacyData)
          GlassCard(accentBorderColor = ext.success.copy(alpha = 0.4f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = strings.offlineBadgeTitle,
                tint = ext.success,
              )
              Spacer(Modifier.width(12.dp))
              Text(
                text = strings.offlineBadgeTitle,
                style = MaterialTheme.typography.titleMedium,
                color = ext.success,
                fontWeight = FontWeight.Bold,
              )
            }
            Spacer(Modifier.height(6.dp))
            Text(
              text = strings.offlineBadgeDesc,
              style = MaterialTheme.typography.bodySmall,
              color = ext.mutedText,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
              onClick = { showResetConfirm = true },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = ext.danger),
              modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("settings_reset_btn"),
            ) {
              Icon(Icons.Filled.DeleteForever, contentDescription = strings.resetAllData)
              Spacer(Modifier.width(8.dp))
              Text(strings.resetAllData)
            }
          }
        }
      }

      item(key = "settings_bottom_spacer") { Spacer(Modifier.height(16.dp)) }
    }
  }

  if (showNameDialog) {
    var nameInput by remember { mutableStateOf(preferences.userName) }
    AlertDialog(
      onDismissRequest = { showNameDialog = false },
      title = { Text(strings.displayNameLabel) },
      text = {
        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it },
          label = { Text(strings.editNameHint) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onUpdateUserName(nameInput)
            showNameDialog = false
          }
        ) {
          Text(strings.save)
        }
      },
      dismissButton = { TextButton(onClick = { showNameDialog = false }) { Text(strings.cancel) } },
    )
  }

  if (showGoalDialog) {
    var goalSlider by remember { mutableFloatStateOf(stats.dailyStudyGoalMinutes.toFloat()) }
    AlertDialog(
      onDismissRequest = { showGoalDialog = false },
      title = { Text(strings.dailyStudyGoal) },
      text = {
        Column {
          Text(
            DateUtils.formatDurationMinutes(goalSlider.toInt(), preferences.language),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
          )
          Spacer(Modifier.height(8.dp))
          Slider(
            value = goalSlider,
            onValueChange = { goalSlider = it },
            valueRange = 30f..480f,
            steps = 29,
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onUpdateDailyGoal(goalSlider.toInt())
            showGoalDialog = false
          }
        ) {
          Text(strings.save)
        }
      },
      dismissButton = { TextButton(onClick = { showGoalDialog = false }) { Text(strings.cancel) } },
    )
  }

  if (showDatePicker) {
    val pickerState =
      rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.millisFromEpochDay(stats.sscExamEpochDay)
      )
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        Button(
          onClick = {
            pickerState.selectedDateMillis?.let { millis ->
              onUpdateSscExamDate(DateUtils.epochDayFromMillis(millis))
            }
            showDatePicker = false
          }
        ) {
          Text(strings.save)
        }
      },
      dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(strings.cancel) } },
    ) {
      DatePicker(state = pickerState)
    }
  }

  if (showResetConfirm) {
    AlertDialog(
      onDismissRequest = { showResetConfirm = false },
      title = { Text(strings.resetAllData) },
      text = { Text(strings.resetAllDataConfirm) },
      confirmButton = {
        Button(
          onClick = {
            onResetAllData()
            showResetConfirm = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ext.danger),
        ) {
          Text(strings.confirm)
        }
      },
      dismissButton = { TextButton(onClick = { showResetConfirm = false }) { Text(strings.cancel) } },
    )
  }
}

@Composable
private fun SettingSwitchRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String,
) {
  val ext = LocalExtendedColors.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = title,
      tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.titleMedium)
      Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = ext.mutedText)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.testTag(testTag),
    )
  }
}
