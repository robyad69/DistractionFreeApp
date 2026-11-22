package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.AchievementBadge
import com.example.domain.model.DailyReportPoint
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
import com.example.ui.viewmodel.InsightsUiState
import com.example.ui.viewmodel.ReportPeriod
import com.example.util.DateUtils

enum class InsightsTab {
  REPORTS,
  GAMIFICATION,
}

@Composable
fun InsightsScreen(
  selectedTab: InsightsTab,
  onSelectTab: (InsightsTab) -> Unit,
  state: InsightsUiState,
  onSelectPeriod: (ReportPeriod) -> Unit,
) {
  val strings = LocalAppStrings.current

  AdaptiveContentBox {
    Column(modifier = Modifier.fillMaxSize()) {
      SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        val tabs =
          listOf(
            InsightsTab.REPORTS to strings.tabReports,
            InsightsTab.GAMIFICATION to strings.tabGamification,
          )
        tabs.forEachIndexed { idx, (tab, label) ->
          SegmentedButton(
            selected = selectedTab == tab,
            onClick = { onSelectTab(tab) },
            shape = SegmentedButtonDefaults.itemShape(index = idx, count = tabs.size),
            modifier = Modifier.testTag("insights_tab_${tab.name.lowercase()}"),
          ) {
            Text(label, maxLines = 1)
          }
        }
      }

      when (selectedTab) {
        InsightsTab.REPORTS -> ReportsSubScreen(state = state, onSelectPeriod = onSelectPeriod)
        InsightsTab.GAMIFICATION -> GamificationSubScreen(state = state)
      }
    }
  }
}

@Composable
private fun ReportsSubScreen(
  state: InsightsUiState,
  onSelectPeriod: (ReportPeriod) -> Unit,
) {
  val strings = LocalAppStrings.current
  val language = LocalAppLanguage.current
  val ext = LocalExtendedColors.current

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item(key = "period_chips") {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
          selected = state.period == ReportPeriod.DAILY,
          onClick = { onSelectPeriod(ReportPeriod.DAILY) },
          label = { Text(strings.reportDaily) },
          modifier = Modifier.testTag("report_period_daily"),
        )
        FilterChip(
          selected = state.period == ReportPeriod.WEEKLY,
          onClick = { onSelectPeriod(ReportPeriod.WEEKLY) },
          label = { Text(strings.reportWeekly) },
          modifier = Modifier.testTag("report_period_weekly"),
        )
      }
    }

    // KPI Grid
    item(key = "kpi_metrics") {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          MetricTile(
            title = strings.todaysStudyTime,
            value = DateUtils.formatDurationMinutes(state.periodStudyMinutes, language),
            accent = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
          )
          MetricTile(
            title = strings.totalSessions,
            value = "${state.periodSessionsCount}",
            accent = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          MetricTile(
            title = strings.avgSessionLength,
            value = DateUtils.formatDurationMinutes(state.avgSessionMinutes, language),
            accent = ext.cyanAccent,
            modifier = Modifier.weight(1f),
          )
          MetricTile(
            title = strings.tasksCompletedCount,
            value = "${state.tasksCompletedTotal}",
            accent = ext.success,
            modifier = Modifier.weight(1f),
          )
        }
      }
    }

    // Weekly Study Canvas Bar Chart
    item(key = "weekly_chart_card") {
      GlassCard {
        SectionHeader(title = strings.weeklyStudyChart)
        Spacer(Modifier.height(14.dp))
        if (state.weeklyChartPoints.all { it.minutes == 0 }) {
          Text(
            text = strings.noChartDataYet,
            style = MaterialTheme.typography.bodyMedium,
            color = ext.mutedText,
          )
        } else {
          WeeklyBarChart(points = state.weeklyChartPoints)
        }
      }
    }

    // Subject Breakdown
    item(key = "subject_breakdown_card") {
      GlassCard {
        SectionHeader(title = strings.subjectBreakdown)
        Spacer(Modifier.height(10.dp))
        if (state.subjectBreakdown.isEmpty()) {
          Text(
            text = strings.noChartDataYet,
            style = MaterialTheme.typography.bodyMedium,
            color = ext.mutedText,
          )
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.subjectBreakdown.forEach { subj ->
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    text = localizedSubjectName(subj.subjectTag),
                    style = MaterialTheme.typography.labelLarge,
                  )
                  Text(
                    text = DateUtils.formatDurationMinutes(subj.totalMinutes, language),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                  )
                }
                GradientProgressBar(progress = subj.fraction, heightDp = 8)
              }
            }
          }
        }
      }
    }

    item(key = "reports_bottom_spacer") { Spacer(Modifier.height(16.dp)) }
  }
}

@Composable
private fun WeeklyBarChart(points: List<DailyReportPoint>) {
  val primary = MaterialTheme.colorScheme.primary
  val secondary = MaterialTheme.colorScheme.secondary
  val track = MaterialTheme.colorScheme.surfaceVariant
  val ext = LocalExtendedColors.current
  val maxMins = (points.maxOfOrNull { it.minutes } ?: 1).coerceAtLeast(30)

  Column(modifier = Modifier.fillMaxWidth()) {
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
      val count = points.size.coerceAtLeast(1)
      val slotWidth = size.width / count
      val barWidth = (slotWidth * 0.48f).coerceAtMost(36.dp.toPx())
      val corner = CornerRadius(8.dp.toPx(), 8.dp.toPx())

      points.forEachIndexed { idx, pt ->
        val left = idx * slotWidth + (slotWidth - barWidth) / 2f
        // Background track bar
        drawRoundRect(
          color = track,
          topLeft = Offset(left, 0f),
          size = Size(barWidth, size.height),
          cornerRadius = corner,
        )
        if (pt.minutes > 0) {
          val h = (pt.minutes.toFloat() / maxMins.toFloat()).coerceIn(0.06f, 1f) * size.height
          drawRoundRect(
            brush = Brush.verticalGradient(listOf(primary, secondary)),
            topLeft = Offset(left, size.height - h),
            size = Size(barWidth, h),
            cornerRadius = corner,
          )
        }
      }
    }
    Spacer(Modifier.height(8.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceAround,
    ) {
      points.forEach { pt ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = pt.dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = ext.mutedText,
          )
          Text(
            text = "${pt.minutes}m",
            style = MaterialTheme.typography.labelSmall,
            color = if (pt.minutes > 0) primary else ext.mutedText.copy(alpha = 0.6f),
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }
}

@Composable
private fun MetricTile(
  title: String,
  value: String,
  accent: androidx.compose.ui.graphics.Color,
  modifier: Modifier = Modifier,
) {
  val ext = LocalExtendedColors.current
  GlassCard(modifier = modifier, accentBorderColor = accent.copy(alpha = 0.35f)) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelMedium,
      color = ext.mutedText,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Spacer(Modifier.height(6.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.onSurface,
      fontWeight = FontWeight.Bold,
    )
  }
}

@Composable
private fun GamificationSubScreen(state: InsightsUiState) {
  val strings = LocalAppStrings.current
  val language = LocalAppLanguage.current
  val ext = LocalExtendedColors.current

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // Rank & XP Card
    item(key = "rank_card") {
      GlassCard(accentBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = strings.levelProgressTitle,
              style = MaterialTheme.typography.labelLarge,
              color = ext.mutedText,
            )
            Spacer(Modifier.height(4.dp))
            Text(
              text =
                "${strings.levelLabel} ${state.levelInfo.level} · ${state.levelInfo.localizedTitle(language)}",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold,
            )
          }
          Box(
            modifier =
              Modifier.size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.EmojiEvents,
              contentDescription = strings.levelLabel,
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        }
        Spacer(Modifier.height(12.dp))
        GradientProgressBar(progress = state.levelInfo.progressFraction, heightDp = 10)
        Spacer(Modifier.height(6.dp))
        Text(
          text =
            "${state.levelInfo.currentLevelXp} / ${state.levelInfo.nextLevelXp} ${strings.xpToNextLevel}",
          style = MaterialTheme.typography.labelMedium,
          color = ext.mutedText,
        )
        Spacer(Modifier.height(14.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
          StatBadgePill(
            icon = Icons.Filled.LocalFireDepartment,
            value = "${state.currentStreak}",
            label = strings.streakLabel,
            tint = ext.warning,
          )
        }
      }
    }

    item(key = "badges_header") {
      SectionHeader(title = strings.achievementBadges)
    }

    items(state.badges, key = { it.id }) { badge ->
      BadgeCard(badge = badge)
    }

    item(key = "gamification_bottom_spacer") { Spacer(Modifier.height(16.dp)) }
  }
}

@Composable
private fun BadgeCard(badge: AchievementBadge) {
  val strings = LocalAppStrings.current
  val language = LocalAppLanguage.current
  val ext = LocalExtendedColors.current
  val tint =
    if (badge.isUnlocked) MaterialTheme.colorScheme.primary else ext.mutedText.copy(alpha = 0.55f)

  GlassCard(
    accentBorderColor =
      if (badge.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else null
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
        modifier =
          Modifier.size(46.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector =
            if (badge.isUnlocked) badgeIconFor(badge.iconKey) else Icons.Filled.Lock,
          contentDescription = badge.title(language),
          tint = tint,
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = badge.title(language),
          style = MaterialTheme.typography.titleMedium,
          color =
            if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else ext.mutedText,
        )
        Text(
          text = badge.description(language),
          style = MaterialTheme.typography.bodySmall,
          color = ext.mutedText,
        )
      }
      Surface(
        shape = RoundedCornerShape(10.dp),
        color =
          if (badge.isUnlocked) ext.success.copy(alpha = 0.16f)
          else MaterialTheme.colorScheme.surfaceVariant,
      ) {
        Text(
          text = if (badge.isUnlocked) strings.unlockedBadge else badge.progressText,
          style = MaterialTheme.typography.labelMedium,
          color = if (badge.isUnlocked) ext.success else ext.mutedText,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
      }
    }
  }
}

private fun badgeIconFor(key: String): ImageVector =
  when (key) {
    "bolt" -> Icons.Filled.Bolt
    "timer" -> Icons.Filled.Timer
    "hourglass" -> Icons.Filled.HourglassBottom
    "check" -> Icons.Filled.CheckCircle
    "flame" -> Icons.Filled.LocalFireDepartment
    "school" -> Icons.Filled.School
    "note" -> Icons.AutoMirrored.Filled.MenuBook
    else -> Icons.Filled.AutoAwesome
  }
