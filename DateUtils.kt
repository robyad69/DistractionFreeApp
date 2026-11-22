package com.example.util

import com.example.localization.AppLanguage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

  fun todayEpochDay(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    // Convert local midnight to deterministic day index using offset-adjusted millis
    val offset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)
    return TimeUnit.MILLISECONDS.toDays(cal.timeInMillis + offset)
  }

  fun epochDayFromMillis(millis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val offset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)
    return TimeUnit.MILLISECONDS.toDays(cal.timeInMillis + offset)
  }

  fun millisFromEpochDay(epochDay: Long): Long {
    val cal = Calendar.getInstance()
    cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.add(Calendar.DAY_OF_YEAR, epochDay.toInt())
    return cal.timeInMillis
  }

  fun defaultSscExamEpochDay(): Long {
    // Default 90 days ahead from today so user always has a meaningful countdown to customize
    return todayEpochDay() + 90L
  }

  private fun localeFor(language: AppLanguage): Locale =
    if (language == AppLanguage.BENGALI) {
      Locale.Builder().setLanguage("bn").setRegion("BD").build()
    } else {
      Locale.ENGLISH
    }

  fun formatEpochDay(epochDay: Long, language: AppLanguage): String {
    val millis = millisFromEpochDay(epochDay)
    val formatter = SimpleDateFormat("dd MMM yyyy", localeFor(language))
    return formatter.format(Date(millis))
  }

  fun formatShortDayLabel(epochDay: Long, language: AppLanguage): String {
    val millis = millisFromEpochDay(epochDay)
    val formatter = SimpleDateFormat("EEE", localeFor(language))
    return formatter.format(Date(millis))
  }

  fun formatTimeStamp(millis: Long, language: AppLanguage): String {
    val formatter = SimpleDateFormat("dd MMM, hh:mm a", localeFor(language))
    return formatter.format(Date(millis))
  }

  fun formatDurationMinutes(totalMinutes: Int, language: AppLanguage): String {
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return if (language == AppLanguage.BENGALI) {
      if (hours > 0) "${hours}ঘ. ${mins}মি." else "${mins} মি."
    } else {
      if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }
  }

  fun currentHourOfDay(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
}
