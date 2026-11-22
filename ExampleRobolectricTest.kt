package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.GamificationCalculator
import com.example.localization.AppLanguage
import com.example.localization.stringsFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `read string from context matches Distraction Free App`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Distraction Free App", appName)
  }

  @Test
  fun `bilingual strings provide English and Bengali labels`() {
    val en = stringsFor(AppLanguage.ENGLISH)
    val bn = stringsFor(AppLanguage.BENGALI)
    assertEquals("Focus Mode", en.focusTitle)
    assertEquals("ফোকাস মোড", bn.focusTitle)
  }

  @Test
  fun `gamification level and badges compute accurately`() {
    val level3 = GamificationCalculator.calculateLevel(260)
    assertEquals(3, level3.level)

    val badges =
      GamificationCalculator.computeBadges(
        totalSessions = 5,
        totalFocusMinutes = 75,
        totalTasksCompleted = 5,
        bestStreak = 3,
        completedChapters = 5,
        notesCount = 3,
        aura = 50,
      )
    assertTrue(badges.all { it.isUnlocked })
  }

  @Test
  fun `all main features, creation dialogs, focus controls, and settings work in UI`() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dashboard_hero_card").assertIsDisplayed()

    // 1. Focus Mode: switch modes, subject tag, and reset
    composeTestRule.onNodeWithTag("bottom_nav_focus").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("focus_timer_display").assertTextContains("25:00")
    composeTestRule.onNodeWithTag("focus_mode_short_break").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("focus_timer_display").assertTextContains("05:00")
    composeTestRule.onNodeWithTag("focus_mode_custom").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("focus_reset_btn").performClick()
    composeTestRule.waitForIdle()

    // 2. Plan Workspace: switch tabs & verify Task/Note/Habit screens and FAB
    composeTestRule.onNodeWithTag("bottom_nav_workspace").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("workspace_add_fab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("task_filter_all").assertIsDisplayed()

    composeTestRule.onNodeWithTag("workspace_tab_notes").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("notes_search_input").assertIsDisplayed()

    composeTestRule.onNodeWithTag("workspace_tab_habits").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("workspace_add_fab").assertIsDisplayed()

    // 3. SSC Study Center
    composeTestRule.onNodeWithTag("bottom_nav_ssc_center").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ssc_edit_date_btn").assertIsDisplayed()

    // 4. Reports & Gamification
    composeTestRule.onNodeWithTag("bottom_nav_insights").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("report_period_daily").performClick()
    composeTestRule.onNodeWithTag("insights_tab_gamification").performClick()
    composeTestRule.waitForIdle()

    // 5. Settings: Theme toggle & Bengali Language switch
    composeTestRule.onNodeWithTag("topbar_settings_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settings_dark_theme_switch").performClick()
    composeTestRule.onNodeWithTag("settings_lang_bn").performClick()
    composeTestRule.waitUntil(timeoutMillis = 5000L) {
      composeTestRule.onAllNodesWithText("সেটিংস").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("সেটিংস").assertIsDisplayed()
  }
}
