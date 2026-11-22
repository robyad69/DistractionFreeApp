package com.example.domain.model

import androidx.compose.runtime.Immutable
import com.example.localization.AppLanguage

@Immutable
data class LevelInfo(
  val level: Int,
  val titleEn: String,
  val titleBn: String,
  val currentLevelXp: Int,
  val nextLevelXp: Int,
  val progressFraction: Float,
) {
  fun localizedTitle(language: AppLanguage): String =
    if (language == AppLanguage.BENGALI) titleBn else titleEn
}

@Immutable
data class AchievementBadge(
  val id: String,
  val titleEn: String,
  val titleBn: String,
  val descEn: String,
  val descBn: String,
  val iconKey: String,
  val isUnlocked: Boolean,
  val progressText: String,
) {
  fun title(language: AppLanguage): String =
    if (language == AppLanguage.BENGALI) titleBn else titleEn

  fun description(language: AppLanguage): String =
    if (language == AppLanguage.BENGALI) descBn else descEn
}

@Immutable
data class DailyReportPoint(
  val epochDay: Long,
  val dayLabel: String,
  val minutes: Int,
)

@Immutable
data class SubjectStudySummary(
  val subjectTag: String,
  val totalMinutes: Int,
  val fraction: Float,
)

object GamificationCalculator {

  private val levelThresholds =
    listOf(
      0 to ("Novice Seeker" to "নবীন শিক্ষার্থী"),
      100 to ("Calm Beginner" to "শান্ত অভিযাত্রী"),
      250 to ("Focus Apprentice" to "মনোযোগী শিক্ষানবিস"),
      500 to ("Deep Worker" to "গভীর মনোযোগী"),
      850 to ("Streak Guardian" to "ধারাবাহিক রক্ষক"),
      1300 to ("SSC Strategist" to "এসএসসি কৌশলী"),
      1900 to ("Scholar Knight" to "জ্ঞানের যোদ্ধা"),
      2600 to ("Focus Master" to "ফোকাস মাস্টার"),
      3500 to ("Aura Sage" to "অরা জ্ঞানী"),
      5000 to ("Grandmaster Scholar" to "গ্র্যান্ডমাস্টার স্কলার"),
    )

  fun calculateLevel(xp: Int): LevelInfo {
    var currentIdx = 0
    for (i in levelThresholds.indices) {
      if (xp >= levelThresholds[i].first) {
        currentIdx = i
      } else {
        break
      }
    }
    val levelNumber = currentIdx + 1
    val floorXp = levelThresholds[currentIdx].first
    val titlePair = levelThresholds[currentIdx].second
    val nextFloorXp =
      if (currentIdx + 1 < levelThresholds.size) levelThresholds[currentIdx + 1].first
      else floorXp + 1500
    val span = (nextFloorXp - floorXp).coerceAtLeast(1)
    val inLevelXp = (xp - floorXp).coerceAtLeast(0)
    val fraction =
      if (currentIdx == levelThresholds.lastIndex) 1f
      else (inLevelXp.toFloat() / span.toFloat()).coerceIn(0f, 1f)

    return LevelInfo(
      level = levelNumber,
      titleEn = titlePair.first,
      titleBn = titlePair.second,
      currentLevelXp = inLevelXp,
      nextLevelXp = span,
      progressFraction = fraction,
    )
  }

  fun computeBadges(
    totalSessions: Int,
    totalFocusMinutes: Int,
    totalTasksCompleted: Int,
    bestStreak: Int,
    completedChapters: Int,
    notesCount: Int,
    aura: Int,
  ): List<AchievementBadge> {
    return listOf(
      AchievementBadge(
        id = "first_focus",
        titleEn = "First Spark",
        titleBn = "প্রথম স্ফুলিঙ্গ",
        descEn = "Complete 1 focus session",
        descBn = "১টি ফোকাস সেশন সম্পন্ন করুন",
        iconKey = "bolt",
        isUnlocked = totalSessions >= 1,
        progressText = "${totalSessions.coerceAtMost(1)}/1",
      ),
      AchievementBadge(
        id = "pomodoro_pro",
        titleEn = "Pomodoro Pro",
        titleBn = "পোমোডোরো প্রো",
        descEn = "Complete 5 focus sessions",
        descBn = "৫টি ফোকাস সেশন সম্পন্ন করুন",
        iconKey = "timer",
        isUnlocked = totalSessions >= 5,
        progressText = "${totalSessions.coerceAtMost(5)}/5",
      ),
      AchievementBadge(
        id = "deep_hour",
        titleEn = "Deep Hour",
        titleBn = "গভীর ১ ঘণ্টা",
        descEn = "Log 60 minutes of focus study",
        descBn = "মোট ৬০ মিনিট মনোযোগ দিয়ে পড়ুন",
        iconKey = "hourglass",
        isUnlocked = totalFocusMinutes >= 60,
        progressText = "${totalFocusMinutes.coerceAtMost(60)}/60m",
      ),
      AchievementBadge(
        id = "task_crusher",
        titleEn = "Task Crusher",
        titleBn = "টাস্ক বিজয়ী",
        descEn = "Complete 5 to-do tasks",
        descBn = "৫টি কাজ সম্পন্ন করুন",
        iconKey = "check",
        isUnlocked = totalTasksCompleted >= 5,
        progressText = "${totalTasksCompleted.coerceAtMost(5)}/5",
      ),
      AchievementBadge(
        id = "streak_flame",
        titleEn = "Streak Flame",
        titleBn = "স্ট্রিক শিখা",
        descEn = "Reach a 3-day streak",
        descBn = "৩ দিনের স্ট্রিক অর্জন করুন",
        iconKey = "flame",
        isUnlocked = bestStreak >= 3,
        progressText = "${bestStreak.coerceAtMost(3)}/3",
      ),
      AchievementBadge(
        id = "ssc_explorer",
        titleEn = "SSC Explorer",
        titleBn = "এসএসসি অভিযাত্রী",
        descEn = "Finish 5 SSC syllabus chapters",
        descBn = "এসএসসি সিলেবাসের ৫টি অধ্যায় শেষ করুন",
        iconKey = "school",
        isUnlocked = completedChapters >= 5,
        progressText = "${completedChapters.coerceAtMost(5)}/5",
      ),
      AchievementBadge(
        id = "note_keeper",
        titleEn = "Note Architect",
        titleBn = "নোট নির্মাতা",
        descEn = "Save 3 study notes offline",
        descBn = "অফলাইনে ৩টি নোট সংরক্ষণ করুন",
        iconKey = "note",
        isUnlocked = notesCount >= 3,
        progressText = "${notesCount.coerceAtMost(3)}/3",
      ),
      AchievementBadge(
        id = "aura_ascendant",
        titleEn = "Aura Ascendant",
        titleBn = "অরা মাস্টার",
        descEn = "Collect 50 Aura points",
        descBn = "৫০টি অরা পয়েন্ট সংগ্রহ করুন",
        iconKey = "auto_awesome",
        isUnlocked = aura >= 50,
        progressText = "${aura.coerceAtMost(50)}/50",
      ),
    )
  }
}
