package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.FocusSessionDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.SscDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.UserStatsDao
import com.example.data.local.entity.FocusSessionEntity
import com.example.data.local.entity.HabitCompletionEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.SscChapterEntity
import com.example.data.local.entity.SscSubjectEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserStatsEntity

@Database(
  entities =
    [
      TaskEntity::class,
      NoteEntity::class,
      HabitEntity::class,
      HabitCompletionEntity::class,
      FocusSessionEntity::class,
      SscSubjectEntity::class,
      SscChapterEntity::class,
      UserStatsEntity::class,
    ],
  version = 1,
  exportSchema = false,
)
abstract class DistractionFreeDatabase : RoomDatabase() {
  abstract fun taskDao(): TaskDao

  abstract fun noteDao(): NoteDao

  abstract fun habitDao(): HabitDao

  abstract fun focusSessionDao(): FocusSessionDao

  abstract fun sscDao(): SscDao

  abstract fun userStatsDao(): UserStatsDao

  companion object {
    @Volatile private var INSTANCE: DistractionFreeDatabase? = null

    fun getInstance(context: Context): DistractionFreeDatabase {
      return INSTANCE
        ?: synchronized(this) {
          val instance =
            Room.databaseBuilder(
                context.applicationContext,
                DistractionFreeDatabase::class.java,
                "distraction_free_db",
              )
              .build()
          INSTANCE = instance
          instance
        }
    }
  }
}
