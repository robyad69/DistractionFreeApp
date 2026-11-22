package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.TaskEntity
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.components.AdaptiveContentBox
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientProgressBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatBadgePill
import com.example.ui.components.localizedSubjectName
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.viewmodel.DashboardUiState
import com.example.util.DateUtils

@Composable
fun DashboardScreen(
  state: DashboardUiState,
  onNavigateFocus: () -> Unit,
  onNavigateTasks: () -> Unit,
  onNavigateNotes: () -> Unit,
  onNavigateHabits: () -> Unit,
  onNavigateSsc: () -> Unit,
  onNavigateReports: () -> Unit,
  onNavigateBadges: () -> Unit,
  onToggleTask: (TaskEntity) -> Unit,
  onToggleHabit: (HabitEntity) -> Unit,
) {
  val strings = LocalAppStrings.current
  val language = LocalAppLanguage.current
  val ext = LocalExtendedColors.current

  val greeting =
    remember(strings) {
      when (DateUtils.currentHourOfDay()) {
        in 5..11 -> strings.goodMorning
        in 12..16 -> strings.goodAfternoon
        else -> strings.goodEvening
      }
    }

  AdaptiveContentBox {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Hero Welcome Banner
      item(key = "hero_banner") {
        val heroShape = RoundedCornerShape(24.dp)
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .clip(heroShape)
              .clickable { onNavigateBadges() }
              .testTag("dashboard_hero_card")
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_dashboard_hero),
            contentDescription = strings.appName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
          )
          Box(
            modifier =
              Modifier.matchParentSize()
                .background(
                  Brush.verticalGradient(
                    colors =
                      listOf(
                        Color(0xB3070A16),
                        Color(0xDC0E1326),
                      )
                  )
                )
          )
          Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = strings.levelLabel,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                  )
                  Text(
                    text =
                      "${strings.levelLabel} ${state.levelInfo.level} · ${state.levelInfo.localizedTitle(language)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }

              StatBadgePill(
                icon = Icons.Filled.LocalFireDepartment,
                value = "${state.currentStreak}",
                label = strings.daysLabel,
                tint = ext.warning,
              )
            }

            Spacer(Modifier.height(14.dp))
            Text(
              text = "$greeting, ${state.userName}",
              style = MaterialTheme.typography.headlineMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
              text = strings.dashboardSubtitle,
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.82f),
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              StatBadgePill(
                icon = Icons.Filled.Bolt,
                value = "${state.xp}",
                label = strings.xpLabel,
                tint = MaterialTheme.colorScheme.primary,
              )
              StatBadgePill(
                icon = Icons.Filled.AutoAwesome,
                value = "${state.aura}",
                label = strings.auraLabel,
                tint = ext.cyanAccent,
              )
            }
          }
        }
      }

      // Today's Study Time & SSC Countdown Row
      item(key = "study_progress_card") {
        GlassCard(
          accentBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
          onClick = onNavigateFocus,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.todaysStudyTime,
                style = MaterialTheme.typography.labelLarge,
                color = ext.mutedText,
              )
              Spacer(Modifier.height(4.dp))
              Text(
                text = DateUtils.formatDurationMinutes(state.todayStudyMinutes, language),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
              )
            }
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
              modifier = Modifier.clickable { onNavigateSsc() }.testTag("dashboard_ssc_pill"),
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(
                  text = "${state.sscDaysRemaining}",
                  style = MaterialTheme.typography.titleLarge,
                  color = MaterialTheme.colorScheme.secondary,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = strings.daysLeft,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
              }
            }
          }
          Spacer(Modifier.height(14.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = strings.dailyProgress,
              style = MaterialTheme.typography.labelMedium,
              color = ext.mutedText,
            )
            Text(
              text =
                "${(state.dailyProgressFraction * 100).toInt()}% · ${strings.dailyGoalOf} ${DateUtils.formatDurationMinutes(state.dailyGoalMinutes, language)}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
            )
          }
          Spacer(Modifier.height(8.dp))
          GradientProgressBar(progress = state.dailyProgressFraction, heightDp = 10)
        }
      }

      // Quick Actions Grid
      item(key = "quick_actions") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          SectionHeader(title = strings.quickActions)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            QuickActionButton(
              icon = Icons.Filled.PlayArrow,
              label = strings.actionStartFocus,
              tint = MaterialTheme.colorScheme.primary,
              onClick = onNavigateFocus,
              testTag = "quick_action_focus",
              modifier = Modifier.weight(1f),
            )
            QuickActionButton(
              icon = Icons.Filled.AddTask,
              label = strings.actionAddTask,
              tint = MaterialTheme.colorScheme.secondary,
              onClick = onNavigateTasks,
              testTag = "quick_action_tasks",
              modifier = Modifier.weight(1f),
            )
            QuickActionButton(
              icon = Icons.AutoMirrored.Filled.NoteAdd,
              label = strings.actionQuickNote,
              tint = ext.cyanAccent,
              onClick = onNavigateNotes,
              testTag = "quick_action_notes",
              modifier = Modifier.weight(1f),
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            QuickActionButton(
              icon = Icons.Filled.School,
              label = strings.actionSscCenter,
              tint = MaterialTheme.colorScheme.primary,
              onClick = onNavigateSsc,
              testTag = "quick_action_ssc",
              modifier = Modifier.weight(1f),
            )
            QuickActionButton(
              icon = Icons.Filled.TrackChanges,
              label = strings.actionHabits,
              tint = ext.success,
              onClick = onNavigateHabits,
              testTag = "quick_action_habits",
              modifier = Modifier.weight(1f),
            )
            QuickActionButton(
              icon = Icons.Filled.BarChart,
              label = strings.actionReports,
              tint = ext.warning,
              onClick = onNavigateReports,
              testTag = "quick_action_reports",
              modifier = Modifier.weight(1f),
            )
          }
        }
      }

      // Today's Tasks Preview
      item(key = "todays_tasks_section") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SectionHeader(
            title = strings.todaysTasks,
            actionLabel = strings.viewAll,
            onAction = onNavigateTasks,
            actionTestTag = "dashboard_view_tasks",
          )
          if (state.pendingTasksToday.isEmpty()) {
            EmptyStateCard(
              icon = Icons.Filled.CheckCircle,
              title = strings.emptyTasksTitle,
              body = strings.noTasksToday,
            )
          } else {
            GlassCard {
              state.pendingTasksToday.forEachIndexed { idx, task ->
                Row(
                  modifier =
                    Modifier.fillMaxWidth()
                      .minimumInteractiveComponentSize()
                      .clip(RoundedCornerShape(12.dp))
                      .clickable { onToggleTask(task) }
                      .padding(vertical = 8.dp, horizontal = 4.dp)
                      .testTag("dashboard_task_${task.id}"),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    imageVector =
                      if (task.isCompleted) Icons.Filled.CheckCircle
                      else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = task.title,
                    tint =
                      if (task.isCompleted) ext.success else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                  )
                  Spacer(Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = task.title,
                      style = MaterialTheme.typography.titleSmall,
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                      text = localizedSubjectName(task.subjectTag),
                      style = MaterialTheme.typography.labelSmall,
                      color = ext.mutedText,
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Daily Habits Preview
      item(key = "todays_habits_section") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SectionHeader(
            title = strings.todaysHabits,
            actionLabel = strings.viewAll,
            onAction = onNavigateHabits,
            actionTestTag = "dashboard_view_habits",
          )
          if (state.habits.isEmpty()) {
            EmptyStateCard(
              icon = Icons.Filled.TrackChanges,
              title = strings.emptyHabitsTitle,
              body = strings.noHabitsYet,
            )
          } else {
            GlassCard {
              state.habits.forEach { habit ->
                val doneToday = state.todayHabitIds.contains(habit.id)
                Row(
                  modifier =
                    Modifier.fillMaxWidth()
                      .minimumInteractiveComponentSize()
                      .clip(RoundedCornerShape(12.dp))
                      .clickable { onToggleHabit(habit) }
                      .padding(vertical = 8.dp, horizontal = 4.dp)
                      .testTag("dashboard_habit_${habit.id}"),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    imageVector =
                      if (doneToday) Icons.Filled.CheckCircle
                      else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = habit.title,
                    tint = if (doneToday) ext.success else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(22.dp),
                  )
                  Spacer(Modifier.width(12.dp))
                  Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                  )
                  StatBadgePill(
                    icon = Icons.Filled.LocalFireDepartment,
                    value = "${habit.currentStreak}",
                    label = strings.daysLabel,
                    tint = ext.warning,
                  )
                }
              }
            }
          }
        }
      }

      // Daily Focus Quote
      item(key = "quote_card") {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
              modifier =
                Modifier.size(36.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
            }
            Text(
              text = strings.dailyQuote,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }

      item(key = "bottom_spacer") { Spacer(Modifier.height(16.dp)) }
    }
  }
}

@Composable
private fun QuickActionButton(
  icon: ImageVector,
  label: String,
  tint: Color,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  val ext = LocalExtendedColors.current
  Surface(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .clip(RoundedCornerShape(18.dp))
        .clickable(onClick = onClick)
        .testTag(testTag),
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, ext.subtleBorder.copy(alpha = 0.5f)),
    tonalElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(
        modifier =
          Modifier.size(40.dp).clip(CircleShape).background(tint.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = tint,
          modifier = Modifier.size(20.dp),
        )
      }
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
