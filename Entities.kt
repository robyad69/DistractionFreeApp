package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val description: String = "",
  val subjectTag: String = "General",
  val priority: Int = 1, // 0 = Low, 1 = Medium, 2 = High
  val isCompleted: Boolean = false,
  val dueDateEpochDay: Long,
  val createdAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "notes")
data class NoteEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val content: String,
  val category: String = "Study", // Study, Formula, Idea
  val colorAccentIndex: Int = 0,
  val isPinned: Boolean = false,
  val updatedAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "habits")
data class HabitEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val iconKey: String = "book",
  val currentStreak: Int = 0,
  val bestStreak: Int = 0,
  val lastCompletedEpochDay: Long = -1L,
  val totalCompletions: Int = 0,
  val createdAtMillis: Long = System.currentTimeMillis(),
)

@Entity(
  tableName = "habit_completions",
  foreignKeys =
    [
      ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index(value = ["habitId", "epochDay"], unique = true)],
)
data class HabitCompletionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val habitId: Long,
  val epochDay: Long,
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val mode: String, // POMODORO, SHORT_BREAK, CUSTOM
  val durationMinutes: Int,
  val actualSecondsCompleted: Int,
  val subjectTag: String,
  val completedAtMillis: Long = System.currentTimeMillis(),
  val epochDay: Long,
  val xpEarned: Int,
  val auraEarned: Int,
)

@Entity(tableName = "ssc_subjects")
data class SscSubjectEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val nameEn: String,
  val nameBn: String,
  val accentIndex: Int = 0,
  val studyMinutes: Int = 0,
)

@Entity(
  tableName = "ssc_chapters",
  foreignKeys =
    [
      ForeignKey(
        entity = SscSubjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index(value = ["subjectId"])],
)
data class SscChapterEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val subjectId: Long,
  val titleEn: String,
  val titleBn: String,
  val isCompleted: Boolean = false,
  val chapterOrder: Int = 0,
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
  @PrimaryKey val id: Int = 1,
  val xp: Int = 0,
  val aura: Int = 0,
  val currentStreak: Int = 0,
  val bestStreak: Int = 0,
  val lastActiveEpochDay: Long = -1L,
  val totalFocusMinutes: Int = 0,
  val totalSessionsCompleted: Int = 0,
  val totalTasksCompleted: Int = 0,
  val totalHabitsCompleted: Int = 0,
  val sscExamEpochDay: Long = 0L,
  val dailyStudyGoalMinutes: Int = 120,
)
