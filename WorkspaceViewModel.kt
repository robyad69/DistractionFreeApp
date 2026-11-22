package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.HabitCompletionEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.ProductivityRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter {
  ALL,
  TODAY,
  PENDING,
  COMPLETED,
}

enum class NoteFilter {
  ALL,
  STUDY,
  FORMULA,
  IDEA,
  PINNED,
}

data class WorkspaceUiState(
  val tasks: List<TaskEntity> = emptyList(),
  val taskFilter: TaskFilter = TaskFilter.ALL,
  val notes: List<NoteEntity> = emptyList(),
  val noteFilter: NoteFilter = NoteFilter.ALL,
  val noteSearchQuery: String = "",
  val habits: List<HabitEntity> = emptyList(),
  val recentHabitCompletions: List<HabitCompletionEntity> = emptyList(),
)

class WorkspaceViewModel(private val repository: ProductivityRepository) : ViewModel() {

  private val taskFilterFlow = MutableStateFlow(TaskFilter.ALL)
  private val noteFilterFlow = MutableStateFlow(NoteFilter.ALL)
  private val noteSearchFlow = MutableStateFlow("")

  private val _errors = MutableSharedFlow<String>()
  val errors: SharedFlow<String> = _errors.asSharedFlow()

  val uiState: StateFlow<WorkspaceUiState> =
    combine(
        combine(repository.tasksFlow, taskFilterFlow) { tasks, filter -> tasks to filter },
        combine(repository.notesFlow, noteFilterFlow, noteSearchFlow) { notes, filter, query ->
          Triple(notes, filter, query)
        },
        combine(repository.habitsFlow, repository.observeRecentHabitCompletions(7)) { habits, completions ->
          habits to completions
        },
      ) { (tasks, tFilter), (notes, nFilter, query), (habits, completions) ->
        val today = DateUtils.todayEpochDay()
        val filteredTasks =
          when (tFilter) {
            TaskFilter.ALL -> tasks
            TaskFilter.TODAY -> tasks.filter { it.dueDateEpochDay == today }
            TaskFilter.PENDING -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
          }
        val q = query.trim().lowercase()
        val filteredNotes =
          notes.filter { note ->
            val matchesFilter =
              when (nFilter) {
                NoteFilter.ALL -> true
                NoteFilter.STUDY -> note.category == "Study"
                NoteFilter.FORMULA -> note.category == "Formula"
                NoteFilter.IDEA -> note.category == "Idea"
                NoteFilter.PINNED -> note.isPinned
              }
            val matchesQuery =
              q.isEmpty() ||
                note.title.lowercase().contains(q) ||
                note.content.lowercase().contains(q)
            matchesFilter && matchesQuery
          }
        WorkspaceUiState(
          tasks = filteredTasks,
          taskFilter = tFilter,
          notes = filteredNotes,
          noteFilter = nFilter,
          noteSearchQuery = query,
          habits = habits,
          recentHabitCompletions = completions,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkspaceUiState(),
      )

  fun setTaskFilter(filter: TaskFilter) {
    taskFilterFlow.value = filter
  }

  fun setNoteFilter(filter: NoteFilter) {
    noteFilterFlow.value = filter
  }

  fun setNoteSearchQuery(query: String) {
    noteSearchFlow.value = query
  }

  fun saveTask(
    id: Long = 0L,
    title: String,
    description: String,
    subjectTag: String,
    priority: Int,
    dueDateEpochDay: Long,
  ) {
    val cleanTitle = title.trim()
    if (cleanTitle.isEmpty()) return
    viewModelScope.launch {
      runCatching {
        repository.saveTask(
          TaskEntity(
            id = id,
            title = cleanTitle,
            description = description.trim(),
            subjectTag = subjectTag,
            priority = priority,
            dueDateEpochDay = dueDateEpochDay,
          )
        )
      }.onFailure { _errors.emit(it.localizedMessage ?: "Failed to save task") }
    }
  }

  fun updateExistingTask(task: TaskEntity) {
    viewModelScope.launch {
      runCatching { repository.saveTask(task) }
        .onFailure { _errors.emit(it.localizedMessage ?: "Failed to update task") }
    }
  }

  fun toggleTask(task: TaskEntity) {
    viewModelScope.launch {
      runCatching { repository.toggleTaskCompleted(task) }
        .onFailure { _errors.emit(it.localizedMessage ?: "Failed to update task") }
    }
  }

  fun deleteTask(task: TaskEntity) {
    viewModelScope.launch {
      runCatching { repository.deleteTask(task) }
        .onFailure { _errors.emit(it.localizedMessage ?: "Failed to delete task") }
    }
  }

  fun saveNote(
    id: Long = 0L,
    title: String,
    content: String,
    category: String,
    colorAccentIndex: Int,
    isPinned: Boolean = false,
  ) {
    val cleanTitle = title.trim()
    val cleanContent = content.trim()
    if (cleanTitle.isEmpty() && cleanContent.isEmpty()) return
    viewModelScope.launch {
      runCatching {
        repository.saveNote(
          NoteEntity(
            id = id,
            title = cleanTitle.ifEmpty { "Untitled" },
            content = cleanContent,
            category = category,
            colorAccentIndex = colorAccentIndex,
            isPinned = isPinned,
          )
        )
      }.onFailure { _errors.emit(it.localizedMessage ?: "Failed to save note") }
    }
  }

  fun togglePinNote(note: NoteEntity) {
    viewModelScope.launch {
      runCatching { repository.toggleNotePin(note) }
    }
  }

  fun deleteNote(note: NoteEntity) {
    viewModelScope.launch {
      runCatching { repository.deleteNote(note) }
    }
  }

  fun createHabit(title: String, iconKey: String) {
    val clean = title.trim()
    if (clean.isEmpty()) return
    viewModelScope.launch {
      runCatching { repository.createHabit(clean, iconKey) }
        .onFailure { _errors.emit(it.localizedMessage ?: "Failed to create habit") }
    }
  }

  fun toggleHabitToday(habit: HabitEntity) {
    viewModelScope.launch {
      runCatching { repository.toggleHabitToday(habit) }
        .onFailure { _errors.emit(it.localizedMessage ?: "Failed to update habit") }
    }
  }

  fun deleteHabit(habit: HabitEntity) {
    viewModelScope.launch {
      runCatching { repository.deleteHabit(habit) }
    }
  }
}
