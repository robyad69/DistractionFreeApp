package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.components.AdaptiveContentBox
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.StatBadgePill
import com.example.ui.components.localizedSubjectName
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.viewmodel.NoteFilter
import com.example.ui.viewmodel.TaskFilter
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.util.DateUtils

enum class WorkspaceTab {
  TASKS,
  NOTES,
  HABITS,
}

@Composable
fun PlanWorkspaceScreen(
  selectedTab: WorkspaceTab,
  onSelectTab: (WorkspaceTab) -> Unit,
  state: WorkspaceUiState,
  onTaskFilterChange: (TaskFilter) -> Unit,
  onSaveTask: (id: Long, title: String, desc: String, subject: String, priority: Int, dueDay: Long) -> Unit,
  onUpdateTask: (TaskEntity) -> Unit,
  onToggleTask: (TaskEntity) -> Unit,
  onDeleteTask: (TaskEntity) -> Unit,
  onNoteFilterChange: (NoteFilter) -> Unit,
  onNoteSearchChange: (String) -> Unit,
  onSaveNote: (id: Long, title: String, content: String, category: String, accent: Int, pinned: Boolean) -> Unit,
  onTogglePinNote: (NoteEntity) -> Unit,
  onDeleteNote: (NoteEntity) -> Unit,
  onCreateHabit: (title: String, iconKey: String) -> Unit,
  onToggleHabitToday: (HabitEntity) -> Unit,
  onDeleteHabit: (HabitEntity) -> Unit,
) {
  val strings = LocalAppStrings.current
  val ext = LocalExtendedColors.current

  var showTaskDialog by remember { mutableStateOf(false) }
  var editingTask by remember { mutableStateOf<TaskEntity?>(null) }

  var showNoteDialog by remember { mutableStateOf(false) }
  var editingNote by remember { mutableStateOf<NoteEntity?>(null) }

  var showHabitDialog by remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxSize()) {
    AdaptiveContentBox(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Segmented Sub-Tab Switcher
        SingleChoiceSegmentedButtonRow(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
          val tabs =
            listOf(
              WorkspaceTab.TASKS to strings.tabTasks,
              WorkspaceTab.NOTES to strings.tabNotes,
              WorkspaceTab.HABITS to strings.tabHabits,
            )
          tabs.forEachIndexed { idx, (tab, label) ->
            SegmentedButton(
              selected = selectedTab == tab,
              onClick = { onSelectTab(tab) },
              shape = SegmentedButtonDefaults.itemShape(index = idx, count = tabs.size),
              modifier = Modifier.testTag("workspace_tab_${tab.name.lowercase()}"),
            ) {
              Text(label, maxLines = 1)
            }
          }
        }

        when (selectedTab) {
          WorkspaceTab.TASKS ->
            TasksSubScreen(
              state = state,
              onFilterChange = onTaskFilterChange,
              onToggleTask = onToggleTask,
              onEditTask = { task ->
                editingTask = task
                showTaskDialog = true
              },
              onDeleteTask = onDeleteTask,
            )
          WorkspaceTab.NOTES ->
            NotesSubScreen(
              state = state,
              onFilterChange = onNoteFilterChange,
              onSearchChange = onNoteSearchChange,
              onEditNote = { note ->
                editingNote = note
                showNoteDialog = true
              },
              onTogglePin = onTogglePinNote,
              onDeleteNote = onDeleteNote,
            )
          WorkspaceTab.HABITS ->
            HabitsSubScreen(
              state = state,
              onToggleHabitToday = onToggleHabitToday,
              onDeleteHabit = onDeleteHabit,
            )
        }
      }
    }

    FloatingActionButton(
      onClick = {
        when (selectedTab) {
          WorkspaceTab.TASKS -> {
            editingTask = null
            showTaskDialog = true
          }
          WorkspaceTab.NOTES -> {
            editingNote = null
            showNoteDialog = true
          }
          WorkspaceTab.HABITS -> {
            showHabitDialog = true
          }
        }
      },
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier =
        Modifier.align(Alignment.BottomEnd)
          .padding(20.dp)
          .minimumInteractiveComponentSize()
          .testTag("workspace_add_fab"),
    ) {
      Icon(
        imageVector = Icons.Filled.Add,
        contentDescription = strings.add,
      )
    }
  }

  if (showTaskDialog) {
    TaskEditorDialog(
      existing = editingTask,
      onDismiss = { showTaskDialog = false },
      onSave = { title, desc, subject, priority, dueDay ->
        val existing = editingTask
        if (existing == null) {
          onSaveTask(0L, title, desc, subject, priority, dueDay)
        } else {
          onUpdateTask(
            existing.copy(
              title = title.trim(),
              description = desc.trim(),
              subjectTag = subject,
              priority = priority,
              dueDateEpochDay = dueDay,
            )
          )
        }
        showTaskDialog = false
      },
    )
  }

  if (showNoteDialog) {
    NoteEditorDialog(
      existing = editingNote,
      onDismiss = { showNoteDialog = false },
      onSave = { title, content, category, accent ->
        val existing = editingNote
        onSaveNote(
          existing?.id ?: 0L,
          title,
          content,
          category,
          accent,
          existing?.isPinned ?: false,
        )
        showNoteDialog = false
      },
    )
  }

  if (showHabitDialog) {
    HabitCreatorDialog(
      onDismiss = { showHabitDialog = false },
      onCreate = { title, iconKey ->
        onCreateHabit(title, iconKey)
        showHabitDialog = false
      },
    )
  }
}

@Composable
private fun TasksSubScreen(
  state: WorkspaceUiState,
  onFilterChange: (TaskFilter) -> Unit,
  onToggleTask: (TaskEntity) -> Unit,
  onEditTask: (TaskEntity) -> Unit,
  onDeleteTask: (TaskEntity) -> Unit,
) {
  val strings = LocalAppStrings.current
  val language = LocalAppLanguage.current
  val ext = LocalExtendedColors.current

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item(key = "task_filters") {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val filters =
          listOf(
            TaskFilter.ALL to strings.all,
            TaskFilter.TODAY to strings.today,
            TaskFilter.PENDING to strings.pending,
            TaskFilter.COMPLETED to strings.completed,
          )
        items(filters, key = { it.first.name }) { (filter, label) ->
          FilterChip(
            selected = state.taskFilter == filter,
            onClick = { onFilterChange(filter) },
            label = { Text(label) },
            modifier = Modifier.testTag("task_filter_${filter.name.lowercase()}"),
          )
        }
      }
    }

    if (state.tasks.isEmpty()) {
      item(key = "tasks_empty") {
        EmptyStateCard(
          icon = Icons.Filled.TaskAlt,
          title = strings.emptyTasksTitle,
          body = strings.emptyTasksBody,
        )
      }
    } else {
      items(state.tasks, key = { it.id }) { task ->
        val priorityColor =
          when (task.priority) {
            2 -> ext.danger
            1 -> ext.warning
            else -> ext.success
          }
        GlassCard(accentBorderColor = if (task.isCompleted) null else priorityColor.copy(alpha = 0.35f)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = { onToggleTask(task) },
              modifier = Modifier.minimumInteractiveComponentSize().testTag("task_toggle_${task.id}"),
            ) {
              Icon(
                imageVector =
                  if (task.isCompleted) Icons.Filled.CheckCircle
                  else Icons.Filled.RadioButtonUnchecked,
                contentDescription = task.title,
                tint = if (task.isCompleted) ext.success else MaterialTheme.colorScheme.primary,
              )
            }
            Column(
              modifier = Modifier.weight(1f).clickable { onEditTask(task) }.padding(vertical = 2.dp)
            ) {
              Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                color =
                  if (task.isCompleted) ext.mutedText else MaterialTheme.colorScheme.onSurface,
                textDecoration =
                  if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
              if (task.description.isNotEmpty()) {
                Text(
                  text = task.description,
                  style = MaterialTheme.typography.bodySmall,
                  color = ext.mutedText,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              Spacer(Modifier.height(6.dp))
              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                ) {
                  Text(
                    text = localizedSubjectName(task.subjectTag),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  )
                }
                Text(
                  text = DateUtils.formatEpochDay(task.dueDateEpochDay, language),
                  style = MaterialTheme.typography.labelSmall,
                  color = ext.mutedText,
                )
              }
            }
            IconButton(
              onClick = { onEditTask(task) },
              modifier = Modifier.minimumInteractiveComponentSize().testTag("task_edit_${task.id}"),
            ) {
              Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = strings.edit,
                tint = ext.mutedText,
              )
            }
            IconButton(
              onClick = { onDeleteTask(task) },
              modifier = Modifier.minimumInteractiveComponentSize().testTag("task_delete_${task.id}"),
            ) {
              Icon(
                imageVector = Icons.Filled.DeleteOutline,
                contentDescription = strings.delete,
                tint = ext.danger,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun NotesSubScreen(
  state: WorkspaceUiState,
  onFilterChange: (NoteFilter) -> Unit,
  onSearchChange: (String) -> Unit,
  onEditNote: (NoteEntity) -> Unit,
  onTogglePin: (NoteEntity) -> Unit,
  onDeleteNote: (NoteEntity) -> Unit,
) {
  val strings = LocalAppStrings.current
  val language = LocalAppLanguage.current
  val ext = LocalExtendedColors.current

  val accentColors =
    listOf(
      MaterialTheme.colorScheme.primary,
      MaterialTheme.colorScheme.secondary,
      ext.cyanAccent,
      ext.success,
    )

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item(key = "notes_search") {
      OutlinedTextField(
        value = state.noteSearchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text(strings.searchHint) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = strings.searchHint) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("notes_search_input"),
      )
    }

    item(key = "notes_filters") {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val filters =
          listOf(
            NoteFilter.ALL to strings.all,
            NoteFilter.STUDY to strings.catStudy,
            NoteFilter.FORMULA to strings.catFormula,
            NoteFilter.IDEA to strings.catIdea,
            NoteFilter.PINNED to strings.pinNote,
          )
        items(filters, key = { it.first.name }) { (filter, label) ->
          FilterChip(
            selected = state.noteFilter == filter,
            onClick = { onFilterChange(filter) },
            label = { Text(label) },
            modifier = Modifier.testTag("note_filter_${filter.name.lowercase()}"),
          )
        }
      }
    }

    if (state.notes.isEmpty()) {
      item(key = "notes_empty") {
        EmptyStateCard(
          icon = Icons.Outlined.Description,
          title = strings.emptyNotesTitle,
          body = strings.emptyNotesBody,
        )
      }
    } else {
      items(state.notes, key = { it.id }) { note ->
        val stripe = accentColors[note.colorAccentIndex % accentColors.size]
        GlassCard(
          onClick = { onEditNote(note) },
          accentBorderColor = stripe.copy(alpha = 0.45f),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f),
            ) {
              Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(stripe))
              Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Row {
              IconButton(
                onClick = { onTogglePin(note) },
                modifier = Modifier.minimumInteractiveComponentSize(),
              ) {
                Icon(
                  imageVector =
                    if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                  contentDescription = if (note.isPinned) strings.unpinNote else strings.pinNote,
                  tint = if (note.isPinned) stripe else ext.mutedText,
                )
              }
              IconButton(
                onClick = { onDeleteNote(note) },
                modifier = Modifier.minimumInteractiveComponentSize().testTag("note_delete_${note.id}"),
              ) {
                Icon(
                  imageVector = Icons.Filled.DeleteOutline,
                  contentDescription = strings.delete,
                  tint = ext.danger,
                )
              }
            }
          }
          if (note.content.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
              text = note.content,
              style = MaterialTheme.typography.bodyMedium,
              color = ext.mutedText,
              maxLines = 4,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Spacer(Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            val catText =
              when (note.category) {
                "Formula" -> strings.catFormula
                "Idea" -> strings.catIdea
                else -> strings.catStudy
              }
            Text(
              text = catText,
              style = MaterialTheme.typography.labelSmall,
              color = stripe,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = DateUtils.formatTimeStamp(note.updatedAtMillis, language),
              style = MaterialTheme.typography.labelSmall,
              color = ext.mutedText,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun HabitsSubScreen(
  state: WorkspaceUiState,
  onToggleHabitToday: (HabitEntity) -> Unit,
  onDeleteHabit: (HabitEntity) -> Unit,
) {
  val strings = LocalAppStrings.current
  val ext = LocalExtendedColors.current
  val today = DateUtils.todayEpochDay()
  val completionsByHabit = remember(state.recentHabitCompletions) {
    state.recentHabitCompletions.groupBy { it.habitId }.mapValues { (_, list) ->
      list.map { it.epochDay }.toSet()
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    if (state.habits.isEmpty()) {
      item(key = "habits_empty") {
        EmptyStateCard(
          icon = Icons.Filled.TrackChanges,
          title = strings.emptyHabitsTitle,
          body = strings.emptyHabitsBody,
        )
      }
    } else {
      items(state.habits, key = { it.id }) { habit ->
        val daySet = completionsByHabit[habit.id].orEmpty()
        val doneToday = daySet.contains(today)

        GlassCard(
          accentBorderColor =
            if (doneToday) ext.success.copy(alpha = 0.55f) else null
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = { onToggleHabitToday(habit) },
              modifier = Modifier.minimumInteractiveComponentSize().testTag("habit_toggle_${habit.id}"),
            ) {
              Icon(
                imageVector =
                  if (doneToday) Icons.Filled.CheckCircle else habitIconFor(habit.iconKey),
                contentDescription = habit.title,
                tint = if (doneToday) ext.success else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
              )
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
              Text(
                text = habit.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Spacer(Modifier.height(4.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatBadgePill(
                  icon = Icons.Filled.LocalFireDepartment,
                  value = "${habit.currentStreak}",
                  label = strings.daysLabel,
                  tint = ext.warning,
                )
                Text(
                  text = "${strings.bestStreak}: ${habit.bestStreak} · ${strings.totalDone}: ${habit.totalCompletions}",
                  style = MaterialTheme.typography.labelSmall,
                  color = ext.mutedText,
                  modifier = Modifier.align(Alignment.CenterVertically),
                )
              }
            }
            IconButton(
              onClick = { onDeleteHabit(habit) },
              modifier = Modifier.minimumInteractiveComponentSize().testTag("habit_delete_${habit.id}"),
            ) {
              Icon(
                imageVector = Icons.Filled.DeleteOutline,
                contentDescription = strings.delete,
                tint = ext.danger,
              )
            }
          }
          Spacer(Modifier.height(10.dp))
          // 7-day mini completion strip
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            (6 downTo 0).forEach { daysAgo ->
              val d = today - daysAgo
              val checked = daySet.contains(d)
              Surface(
                shape = RoundedCornerShape(8.dp),
                color =
                  if (checked) ext.success.copy(alpha = 0.25f)
                  else MaterialTheme.colorScheme.surfaceVariant,
                border =
                  BorderStroke(
                    1.dp,
                    if (checked) ext.success else ext.subtleBorder.copy(alpha = 0.4f),
                  ),
                modifier = Modifier.size(32.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector =
                      if (checked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (checked) ext.success else ext.mutedText.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

private fun habitIconFor(key: String): ImageVector =
  when (key) {
    "water" -> Icons.Filled.WaterDrop
    "fitness" -> Icons.Filled.FitnessCenter
    "calm" -> Icons.Filled.SelfImprovement
    else -> Icons.AutoMirrored.Filled.MenuBook
  }

@Composable
private fun TaskEditorDialog(
  existing: TaskEntity?,
  onDismiss: () -> Unit,
  onSave: (title: String, desc: String, subject: String, priority: Int, dueDay: Long) -> Unit,
) {
  val strings = LocalAppStrings.current
  var title by remember { mutableStateOf(existing?.title ?: "") }
  var desc by remember { mutableStateOf(existing?.description ?: "") }
  var subject by remember { mutableStateOf(existing?.subjectTag ?: "General") }
  var priority by remember { mutableIntStateOf(existing?.priority ?: 1) }
  val today = DateUtils.todayEpochDay()
  var dueDay by remember { mutableLongStateOf(existing?.dueDateEpochDay ?: today) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (existing == null) strings.addTaskTitle else strings.editTaskTitle) },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(strings.taskTitleHint) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("task_dialog_title_input"),
        )
        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          label = { Text(strings.taskDescHint) },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth(),
        )
        Text(strings.priorityLabel, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(0 to strings.priorityLow, 1 to strings.priorityMedium, 2 to strings.priorityHigh)
            .forEach { (p, label) ->
              FilterChip(
                selected = priority == p,
                onClick = { priority = p },
                label = { Text(label) },
              )
            }
        }
        Text(strings.dueDateLabel, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = dueDay == today,
            onClick = { dueDay = today },
            label = { Text(strings.today) },
          )
          FilterChip(
            selected = dueDay == today + 1,
            onClick = { dueDay = today + 1 },
            label = { Text(strings.tomorrow) },
          )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          val subjects =
            listOf("General", "Physics", "Chemistry", "General Math", "Higher Math", "Biology", "English", "Bangla", "ICT")
          items(subjects) { subj ->
            FilterChip(
              selected = subject == subj,
              onClick = { subject = subj },
              label = { Text(localizedSubjectName(subj)) },
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(title, desc, subject, priority, dueDay) },
        enabled = title.trim().isNotEmpty(),
        modifier = Modifier.testTag("task_dialog_save_btn"),
      ) {
        Text(strings.save)
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
  )
}

@Composable
private fun NoteEditorDialog(
  existing: NoteEntity?,
  onDismiss: () -> Unit,
  onSave: (title: String, content: String, category: String, accent: Int) -> Unit,
) {
  val strings = LocalAppStrings.current
  var title by remember { mutableStateOf(existing?.title ?: "") }
  var content by remember { mutableStateOf(existing?.content ?: "") }
  var category by remember { mutableStateOf(existing?.category ?: "Study") }
  var accent by remember { mutableIntStateOf(existing?.colorAccentIndex ?: 0) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (existing == null) strings.addNoteTitle else strings.editNoteTitle) },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(strings.noteTitleHint) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("note_dialog_title_input"),
        )
        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text(strings.noteContentHint) },
          minLines = 3,
          maxLines = 6,
          modifier = Modifier.fillMaxWidth().testTag("note_dialog_content_input"),
        )
        Text(strings.noteCategoryLabel, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("Study" to strings.catStudy, "Formula" to strings.catFormula, "Idea" to strings.catIdea)
            .forEachIndexed { idx, (key, label) ->
              FilterChip(
                selected = category == key,
                onClick = {
                  category = key
                  accent = idx
                },
                label = { Text(label) },
              )
            }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(title, content, category, accent) },
        enabled = title.trim().isNotEmpty() || content.trim().isNotEmpty(),
        modifier = Modifier.testTag("note_dialog_save_btn"),
      ) {
        Text(strings.save)
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
  )
}

@Composable
private fun HabitCreatorDialog(
  onDismiss: () -> Unit,
  onCreate: (title: String, iconKey: String) -> Unit,
) {
  val strings = LocalAppStrings.current
  var title by remember { mutableStateOf("") }
  var iconKey by remember { mutableStateOf("book") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.addHabitTitle) },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(strings.habitTitleHint) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("habit_dialog_title_input"),
        )
        Text(strings.selectIcon, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          listOf("book", "water", "fitness", "calm").forEach { key ->
            FilterChip(
              selected = iconKey == key,
              onClick = { iconKey = key },
              label = {
                Icon(
                  imageVector = habitIconFor(key),
                  contentDescription = key,
                  modifier = Modifier.size(18.dp),
                )
              },
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onCreate(title, iconKey) },
        enabled = title.trim().isNotEmpty(),
        modifier = Modifier.testTag("habit_dialog_save_btn"),
      ) {
        Text(strings.save)
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
  )
}
