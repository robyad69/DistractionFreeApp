package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.components.AdaptiveContentBox
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatBadgePill
import com.example.ui.components.localizedSubjectName
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.viewmodel.FocusTimerMode
import com.example.ui.viewmodel.FocusUiState
import com.example.util.DateUtils
import java.util.Locale

@Composable
fun FocusScreen(
  state: FocusUiState,
  onSelectMode: (FocusTimerMode) -> Unit,
  onSetCustomMinutes: (Int) -> Unit,
  onSelectSubject: (String) -> Unit,
  onStartOrResume: () -> Unit,
  onPause: () -> Unit,
  onReset: () -> Unit,
  onFinishEarly: () -> Unit,
) {
  val strings = LocalAppStrings.current
  val language = LocalAppLanguage.current
  val ext = LocalExtendedColors.current

  val rawProgress =
    if (state.totalSeconds > 0) {
      state.remainingSeconds.toFloat() / state.totalSeconds.toFloat()
    } else 1f
  val animatedProgress by
    animateFloatAsState(
      targetValue = rawProgress.coerceIn(0f, 1f),
      animationSpec = tween(durationMillis = 300),
      label = "focus_ring_progress",
    )

  val mins = state.remainingSeconds / 60
  val secs = state.remainingSeconds % 60
  val timeText = String.format(Locale.ENGLISH, "%02d:%02d", mins, secs)

  val primaryColor = MaterialTheme.colorScheme.primary
  val secondaryColor = MaterialTheme.colorScheme.secondary
  val trackColor = MaterialTheme.colorScheme.surfaceVariant

  AdaptiveContentBox {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Mode Selector
      item(key = "mode_selector") {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
          val modes =
            listOf(
              FocusTimerMode.POMODORO to strings.modePomodoro,
              FocusTimerMode.SHORT_BREAK to strings.modeShortBreak,
              FocusTimerMode.CUSTOM to strings.modeCustom,
            )
          modes.forEachIndexed { idx, (mode, label) ->
            SegmentedButton(
              selected = state.mode == mode,
              onClick = { onSelectMode(mode) },
              enabled = !state.isRunning,
              shape = SegmentedButtonDefaults.itemShape(index = idx, count = modes.size),
              modifier = Modifier.testTag("focus_mode_${mode.name.lowercase()}"),
            ) {
              Text(text = label, maxLines = 1)
            }
          }
        }
      }

      // Custom Duration Controls (when CUSTOM selected and not running)
      if (state.mode == FocusTimerMode.CUSTOM) {
        item(key = "custom_duration_card") {
          GlassCard {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = strings.customDurationLabel,
                style = MaterialTheme.typography.titleSmall,
              )
              Text(
                text = DateUtils.formatDurationMinutes(state.customMinutes, language),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
            }
            Spacer(Modifier.height(6.dp))
            Slider(
              value = state.customMinutes.toFloat(),
              onValueChange = { onSetCustomMinutes(it.toInt()) },
              valueRange = 5f..120f,
              steps = 22,
              enabled = !state.isRunning && !state.isPaused,
              modifier = Modifier.testTag("custom_duration_slider"),
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              listOf(15, 30, 45, 60, 90).forEach { preset ->
                FilterChip(
                  selected = state.customMinutes == preset,
                  onClick = { onSetCustomMinutes(preset) },
                  enabled = !state.isRunning && !state.isPaused,
                  label = { Text("${preset}${strings.minutesShort}") },
                  modifier = Modifier.weight(1f),
                )
              }
            }
          }
        }
      }

      // Subject Tag Picker
      if (state.mode != FocusTimerMode.SHORT_BREAK) {
        item(key = "subject_picker") {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = strings.selectSubjectTag,
              style = MaterialTheme.typography.labelLarge,
              color = ext.mutedText,
            )
            val tags = listOf("General") + state.subjects.map { it.nameEn }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(tags, key = { it }) { tag ->
                FilterChip(
                  selected = state.selectedSubjectEn == tag,
                  onClick = { onSelectSubject(tag) },
                  label = { Text(localizedSubjectName(tag)) },
                  modifier = Modifier.testTag("focus_subject_$tag"),
                )
              }
            }
          }
        }
      }

      // Circular Timer Ring Card
      item(key = "timer_ring_card") {
        GlassCard(accentBorderColor = primaryColor.copy(alpha = 0.4f)) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            val statusText =
              when {
                state.isRunning -> strings.focusRunningStatus
                state.isPaused -> strings.focusPausedStatus
                else -> strings.focusReadyStatus
              }
            Surface(
              shape = RoundedCornerShape(50),
              color =
                if (state.isRunning) primaryColor.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant,
            ) {
              Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge,
                color = if (state.isRunning) primaryColor else ext.mutedText,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
              )
            }

            Spacer(Modifier.height(14.dp))

            Box(modifier = Modifier.size(188.dp), contentAlignment = Alignment.Center) {
              Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = 14.dp.toPx()
                val diameter = size.minDimension - strokePx
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)

                drawArc(
                  color = trackColor,
                  startAngle = -90f,
                  sweepAngle = 360f,
                  useCenter = false,
                  topLeft = topLeft,
                  size = arcSize,
                  style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )

                drawArc(
                  brush = Brush.sweepGradient(listOf(primaryColor, secondaryColor, primaryColor)),
                  startAngle = -90f,
                  sweepAngle = 360f * animatedProgress,
                  useCenter = false,
                  topLeft = topLeft,
                  size = arcSize,
                  style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
              }

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = timeText,
                  style = MaterialTheme.typography.displayLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.testTag("focus_timer_display"),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                  text =
                    if (state.mode == FocusTimerMode.SHORT_BREAK) strings.modeShortBreak
                    else localizedSubjectName(state.selectedSubjectEn),
                  style = MaterialTheme.typography.labelMedium,
                  color = ext.mutedText,
                )
              }
            }

            Spacer(Modifier.height(16.dp))

            // Primary Controls
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              OutlinedButton(
                onClick = onReset,
                modifier = Modifier.minimumInteractiveComponentSize().height(50.dp).testTag("focus_reset_btn"),
                shape = RoundedCornerShape(16.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.Refresh,
                  contentDescription = strings.resetFocus,
                  modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(strings.resetFocus)
              }

              Button(
                onClick = { if (state.isRunning) onPause() else onStartOrResume() },
                modifier =
                  Modifier.weight(1f)
                    .minimumInteractiveComponentSize()
                    .height(50.dp)
                    .testTag("focus_start_pause_btn"),
                shape = RoundedCornerShape(16.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor =
                      if (state.isRunning) secondaryColor else primaryColor
                  ),
              ) {
                Icon(
                  imageVector =
                    if (state.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                  contentDescription = null,
                  modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                  text =
                    when {
                      state.isRunning -> strings.pauseFocus
                      state.isPaused -> strings.resumeFocus
                      else -> strings.startFocus
                    },
                  style = MaterialTheme.typography.titleMedium,
                )
              }
            }

            val elapsed = state.totalSeconds - state.remainingSeconds
            if (elapsed >= 30 && (state.isRunning || state.isPaused)) {
              Spacer(Modifier.height(8.dp))
              TextButton(
                onClick = onFinishEarly,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("focus_finish_early_btn"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = strings.finishEarly,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(strings.finishEarly)
              }
            }
          }
        }
      }

      // Focus Session History
      item(key = "history_header") {
        SectionHeader(title = strings.sessionHistory)
      }

      if (state.sessionHistory.isEmpty()) {
        item(key = "history_empty") {
          EmptyStateCard(
            icon = Icons.Filled.Timer,
            title = strings.sessionHistory,
            body = strings.noSessionsYet,
          )
        }
      } else {
        items(state.sessionHistory, key = { it.id }) { session ->
          val minsCompleted = (session.actualSecondsCompleted / 60).coerceAtLeast(1)
          GlassCard {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text =
                    if (session.mode == "SHORT_BREAK") strings.modeShortBreak
                    else localizedSubjectName(session.subjectTag),
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                  text =
                    "${DateUtils.formatDurationMinutes(minsCompleted, language)} · ${DateUtils.formatTimeStamp(session.completedAtMillis, language)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = ext.mutedText,
                )
              }
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatBadgePill(
                  icon = Icons.Filled.Bolt,
                  value = "+${session.xpEarned}",
                  label = strings.xpLabel,
                  tint = primaryColor,
                )
                StatBadgePill(
                  icon = Icons.Filled.AutoAwesome,
                  value = "+${session.auraEarned}",
                  label = strings.auraLabel,
                  tint = ext.cyanAccent,
                )
              }
            }
          }
        }
      }

      item(key = "focus_bottom_spacer") { Spacer(Modifier.height(16.dp)) }
    }
  }
}
