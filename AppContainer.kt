package com.example.di

import android.content.Context
import com.example.data.local.DistractionFreeDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.ProductivityRepository

class AppContainer(context: Context) {
  val database: DistractionFreeDatabase by lazy { DistractionFreeDatabase.getInstance(context) }
  val preferencesRepository: UserPreferencesRepository by lazy {
    UserPreferencesRepository(context.applicationContext)
  }
  val productivityRepository: ProductivityRepository by lazy { ProductivityRepository(database) }
}
