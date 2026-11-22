package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.entity.SscChapterEntity
import com.example.localization.AppLanguage
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.components.AdaptiveContentBox
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientProgressBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.viewmodel.SscSubjectWithChapters
import com.example.ui.viewmodel.SscUiState
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SscStudyCenterScreen(
  state: SscUiState,
  onToggleChapter: (SscChapterEntity) -> Unit,
  onAddChapter: (subjectId: Long, title: String, nextOrder: Int) -> Unit,
  onUpdateExamDate: (Long) -> Unit,
  onUpdateDailyGoal: (Int) -> Unit,
) {
  val strings = LocalAppStrings.current
  val language = LocalAppLanguage.current
  val ext = LocalExtendedColors.current

  var selectedSubjectId by remember { mutableLongStateOf(-1L) }
  val activeSubject: SscSubjectWithChapters? =
    remember(state.subjects, selectedSubjectId) {
      state.subjects.firstOrNull { it.subject.id == selectedSubjectId }
    }

  var showDatePicker by remember { mutableStateOf(false) }
  var showGoalDialog by remember { mutableStateOf(false) }

  AdaptiveContentBox {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // SSC Countdown Hero Banner
      item(key = "ssc_countdown_card") {
        val shape = RoundedCornerShape(24.dp)
        Box(modifier = Modifier.fillMaxWidth().clip(shape)) {
          Image(
            painter = painterResource(id = R.drawable.img_ssc_banner),
            contentDescription = strings.sscTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
          )
          Box(
            modifier =
              Modifier.matchParentSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color(0xA6080B18), Color(0xE010152B))
                  )
                )
          )
          Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.School,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(22.dp),
                )
                Text(
                  text = strings.sscCountdownTitle,
                  style = MaterialTheme.typography.titleMedium,
                  color = Color.White,
                  maxLines = 2,
                )
              }
              OutlinedButton(
                onClick = { showDatePicker = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.minimumInteractiveComponentSize().testTag("ssc_edit_date_btn"),
              ) {
                Icon(
                  imageVector = Icons.Filled.CalendarMonth,
                  contentDescription = strings.editExamDate,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(strings.editExamDate, color = Color.White, maxLines = 1)
              }
            }
            Spacer(Modifier.height(10.dp))
            Text(
              text =
                if (state.daysRemaining > 0) "${state.daysRemaining} ${strings.daysLeft}"
                else strings.examTodayOrPassed,
              style = MaterialTheme.typography.headlineLarge,
              color = Color.White,
              fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
              text =
                "${DateUtils.formatEpochDay(state.examEpochDay, language)} · ${strings.sscCountdownSubtitle}",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(14.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = strings.overallReadiness,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f),
              )
              Text(
                text =
                  "${state.totalChaptersCompleted}/${state.totalChapters} ${strings.chaptersCompletedCount}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
              )
            }
            Spacer(Modifier.height(6.dp))
            GradientProgressBar(progress = state.overallProgressFraction, heightDp = 10)
          }
        }
      }

      // Daily Study Goal Card
      item(key = "ssc_daily_goal") {
        val goalProgress =
          (state.todayStudyMinutes.toFloat() / state.dailyGoalMinutes.coerceAtLeast(1).toFloat())
            .coerceIn(0f, 1f)
        GlassCard {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.dailyStudyGoal,
                style = MaterialTheme.typography.titleMedium,
              )
              Spacer(Modifier.height(2.dp))
              Text(
                text =
                  "${DateUtils.formatDurationMinutes(state.todayStudyMinutes, language)} / ${DateUtils.formatDurationMinutes(state.dailyGoalMinutes, language)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
              )
            }
            IconButton(
              onClick = { showGoalDialog = true },
              modifier = Modifier.minimumInteractiveComponentSize().testTag("ssc_edit_goal_btn"),
            ) {
              Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = strings.edit,
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
          Spacer(Modifier.height(8.dp))
          GradientProgressBar(progress = goalProgress, heightDp = 8)
          if (goalProgress >= 1f) {
            Spacer(Modifier.height(6.dp))
            Text(
              text = strings.sscGoalMet,
              style = MaterialTheme.typography.labelMedium,
              color = ext.success,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }

      item(key = "subjects_header") {
        SectionHeader(title = strings.subjectProgress)
      }

      items(state.subjects, key = { it.subject.id }) { item ->
        val accentList =
          listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            ext.cyanAccent,
            ext.success,
          )
        val accent = accentList[item.subject.accentIndex % accentList.size]
        val title =
          if (language == AppLanguage.BENGALI) item.subject.nameBn else item.subject.nameEn

        GlassCard(
          onClick = { selectedSubjectId = item.subject.id },
          accentBorderColor = accent.copy(alpha = 0.4f),
          modifier = Modifier.testTag("ssc_subject_${item.subject.id}"),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Spacer(Modifier.height(2.dp))
              Text(
                text =
                  "${item.completedCount}/${item.totalCount} ${strings.chaptersCompletedCount} · ${DateUtils.formatDurationMinutes(item.subject.studyMinutes, language)}",
                style = MaterialTheme.typography.bodySmall,
                color = ext.mutedText,
              )
            }
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = accent.copy(alpha = 0.16f),
            ) {
              Text(
                text = "${(item.progressFraction * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              )
            }
          }
          Spacer(Modifier.height(10.dp))
          GradientProgressBar(progress = item.progressFraction, heightDp = 7)
        }
      }

      item(key = "ssc_bottom_spacer") { Spacer(Modifier.height(16.dp)) }
    }
  }

  // Chapter Completion Tracker Bottom Sheet
  if (activeSubject != null) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newChapterTitle by remember { mutableStateOf("") }

    ModalBottomSheet(
      onDismissRequest = { selectedSubjectId = -1L },
      sheetState = sheetState,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        val subjTitle =
          if (language == AppLanguage.BENGALI) activeSubject.subject.nameBn
          else activeSubject.subject.nameEn
        Text(
          text = "$subjTitle · ${strings.chaptersTitle}",
          style = MaterialTheme.typography.titleLarge,
        )
        Text(
          text =
            "${activeSubject.completedCount}/${activeSubject.totalCount} ${strings.chaptersCompletedCount}",
          style = MaterialTheme.typography.bodySmall,
          color = ext.mutedText,
        )

        LazyColumn(
          modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          items(activeSubject.chapters, key = { it.id }) { chapter ->
            val chTitle =
              if (language == AppLanguage.BENGALI) chapter.titleBn else chapter.titleEn
            Surface(
              shape = RoundedCornerShape(14.dp),
              color =
                if (chapter.isCompleted) ext.success.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              modifier =
                Modifier.fillMaxWidth()
                  .minimumInteractiveComponentSize()
                  .clip(RoundedCornerShape(14.dp))
                  .clickable { onToggleChapter(chapter) }
                  .testTag("ssc_chapter_${chapter.id}"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector =
                    if (chapter.isCompleted) Icons.Filled.CheckCircle
                    else Icons.Filled.RadioButtonUnchecked,
                  contentDescription = chTitle,
                  tint =
                    if (chapter.isCompleted) ext.success else MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                  text = chTitle,
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            value = newChapterTitle,
            onValueChange = { newChapterTitle = it },
            placeholder = { Text(strings.chapterNameHint) },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          Button(
            onClick = {
              onAddChapter(
                activeSubject.subject.id,
                newChapterTitle,
                activeSubject.chapters.size + 1,
              )
              newChapterTitle = ""
            },
            enabled = newChapterTitle.trim().isNotEmpty(),
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Icon(Icons.Filled.Add, contentDescription = strings.addChapter)
          }
        }
      }
    }
  }

  // Date Picker for SSC Exam Date
  if (showDatePicker) {
    val pickerState =
      rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.millisFromEpochDay(state.examEpochDay)
      )
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        Button(
          onClick = {
            pickerState.selectedDateMillis?.let { millis ->
              onUpdateExamDate(DateUtils.epochDayFromMillis(millis))
            }
            showDatePicker = false
          }
        ) {
          Text(strings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) { Text(strings.cancel) }
      },
    ) {
      DatePicker(state = pickerState)
    }
  }

  // Daily Goal Dialog
  if (showGoalDialog) {
    var goalSlider by remember { mutableFloatStateOf(state.dailyGoalMinutes.toFloat()) }
    AlertDialog(
      onDismissRequest = { showGoalDialog = false },
      title = { Text(strings.dailyStudyGoal) },
      text = {
        Column {
          Text(
            text = DateUtils.formatDurationMinutes(goalSlider.toInt(), language),
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
      dismissButton = {
        TextButton(onClick = { showGoalDialog = false }) { Text(strings.cancel) }
      },
    )
  }
}
