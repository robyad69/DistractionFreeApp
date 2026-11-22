package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.InsightsTab
import com.example.ui.screens.PlanWorkspaceScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SscStudyCenterScreen
import com.example.ui.screens.WorkspaceTab
import com.example.ui.viewmodel.AppViewModelFactory
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.FocusTimerEvent
import com.example.ui.viewmodel.FocusViewModel
import com.example.ui.viewmodel.InsightsViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.SscViewModel
import com.example.ui.viewmodel.WorkspaceViewModel
import com.example.util.NotificationHelper

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_FOCUS = "focus"
const val ROUTE_WORKSPACE = "workspace"
const val ROUTE_SSC = "ssc_center"
const val ROUTE_INSIGHTS = "insights"
const val ROUTE_SETTINGS = "settings"

private data class TopNavItem(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistractionFreeRoot(
  factory: AppViewModelFactory,
  settingsViewModel: SettingsViewModel,
) {
  val strings = LocalAppStrings.current
  val context = LocalContext.current
  val prefs by settingsViewModel.preferences.collectAsStateWithLifecycle()
  val stats by settingsViewModel.userStats.collectAsStateWithLifecycle()

  val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
  val focusViewModel: FocusViewModel = viewModel(factory = factory)
  val workspaceViewModel: WorkspaceViewModel = viewModel(factory = factory)
  val sscViewModel: SscViewModel = viewModel(factory = factory)
  val insightsViewModel: InsightsViewModel = viewModel(factory = factory)

  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route ?: ROUTE_DASHBOARD

  var workspaceTab by rememberSaveable { mutableStateOf(WorkspaceTab.TASKS) }
  var insightsTab by rememberSaveable { mutableStateOf(InsightsTab.REPORTS) }
  val snackbarHostState = remember { SnackbarHostState() }

  // Ensure notification channel exists with localized text
  LaunchedEffect(prefs.language) {
    NotificationHelper.createNotificationChannel(
      context = context,
      name = strings.notifChannelName,
      description = strings.notifChannelDesc,
    )
  }

  // Handle Focus completion events (notifications, haptics, snackbar)
  LaunchedEffect(focusViewModel) {
    focusViewModel.events.collect { event ->
      when (event) {
        is FocusTimerEvent.Completed -> {
          if (prefs.soundVibrationEnabled) {
            NotificationHelper.triggerHapticPulse(context)
          }
          if (prefs.notificationsEnabled && prefs.focusAlertsEnabled) {
            NotificationHelper.showFocusCompleteNotification(
              context = context,
              title = strings.notifFocusDoneTitle,
              body = strings.notifFocusDoneBody,
            )
          }
          snackbarHostState.showSnackbar(
            "${strings.focusCompletedToast} +${event.xpEarned} ${strings.xpLabel}, +${event.auraEarned} ${strings.auraLabel}"
          )
        }
        is FocusTimerEvent.Error -> {
          snackbarHostState.showSnackbar(event.message)
        }
      }
    }
  }

  LaunchedEffect(workspaceViewModel) {
    workspaceViewModel.errors.collect { msg -> snackbarHostState.showSnackbar(msg) }
  }

  LaunchedEffect(settingsViewModel) {
    settingsViewModel.events.collect { msg -> snackbarHostState.showSnackbar(msg) }
  }

  val navItems =
    listOf(
      TopNavItem(
        route = ROUTE_DASHBOARD,
        label = strings.navDashboard,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
      ),
      TopNavItem(
        route = ROUTE_FOCUS,
        label = strings.navFocus,
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer,
      ),
      TopNavItem(
        route = ROUTE_WORKSPACE,
        label = strings.navWorkspace,
        selectedIcon = Icons.Filled.CheckBox,
        unselectedIcon = Icons.Outlined.CheckBox,
      ),
      TopNavItem(
        route = ROUTE_SSC,
        label = strings.navSscCenter,
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School,
      ),
      TopNavItem(
        route = ROUTE_INSIGHTS,
        label = strings.navInsights,
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
      ),
    )

  fun navigateTopLevel(route: String) {
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  val topTitle =
    when (currentRoute) {
      ROUTE_FOCUS -> strings.focusTitle
      ROUTE_WORKSPACE ->
        when (workspaceTab) {
          WorkspaceTab.TASKS -> strings.tabTasks
          WorkspaceTab.NOTES -> strings.tabNotes
          WorkspaceTab.HABITS -> strings.tabHabits
        }
      ROUTE_SSC -> strings.sscTitle
      ROUTE_INSIGHTS ->
        when (insightsTab) {
          InsightsTab.REPORTS -> strings.tabReports
          InsightsTab.GAMIFICATION -> strings.tabGamification
        }
      ROUTE_SETTINGS -> strings.settingsTitle
      else -> strings.appName
    }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useRail = maxWidth >= 600.dp

    Scaffold(
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      containerColor = MaterialTheme.colorScheme.background,
      topBar = {
        CenterAlignedTopAppBar(
          colors =
            TopAppBarDefaults.centerAlignedTopAppBarColors(
              containerColor = MaterialTheme.colorScheme.background
            ),
          navigationIcon = {
            if (currentRoute == ROUTE_SETTINGS) {
              IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.minimumInteractiveComponentSize().testTag("topbar_back_btn"),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = strings.close,
                )
              }
            }
          },
          title = {
            Text(
              text = topTitle,
              style = MaterialTheme.typography.titleLarge,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          },
          actions = {
            // Instant Bilingual Language Toggle
            Surface(
              onClick = { settingsViewModel.toggleLanguage() },
              shape = RoundedCornerShape(50),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
              modifier = Modifier.minimumInteractiveComponentSize().testTag("topbar_lang_toggle"),
            ) {
              Text(
                text = if (prefs.language == AppLanguage.ENGLISH) "বাংলা" else "EN",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              )
            }
            Spacer(Modifier.width(4.dp))
            IconButton(
              onClick = {
                insightsTab = InsightsTab.GAMIFICATION
                navigateTopLevel(ROUTE_INSIGHTS)
              },
              modifier = Modifier.minimumInteractiveComponentSize().testTag("topbar_badges_btn"),
            ) {
              Icon(
                imageVector = Icons.Outlined.EmojiEvents,
                contentDescription = strings.tabGamification,
              )
            }
            IconButton(
              onClick = {
                if (currentRoute != ROUTE_SETTINGS) {
                  navController.navigate(ROUTE_SETTINGS) { launchSingleTop = true }
                }
              },
              modifier = Modifier.minimumInteractiveComponentSize().testTag("topbar_settings_btn"),
            ) {
              Icon(
                imageVector =
                  if (currentRoute == ROUTE_SETTINGS) Icons.Filled.Settings
                  else Icons.Outlined.Settings,
                contentDescription = strings.navSettings,
              )
            }
          },
        )
      },
      bottomBar = {
        if (!useRail && currentRoute != ROUTE_SETTINGS) {
          NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            navItems.forEach { item ->
              val selected = currentRoute == item.route
              NavigationBarItem(
                selected = selected,
                onClick = { navigateTopLevel(item.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                  )
                },
                label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                modifier = Modifier.testTag("bottom_nav_${item.route}"),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (useRail && currentRoute != ROUTE_SETTINGS) {
          NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
            navItems.forEach { item ->
              val selected = currentRoute == item.route
              NavigationRailItem(
                selected = selected,
                onClick = { navigateTopLevel(item.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                  )
                },
                label = { Text(item.label, maxLines = 1) },
                modifier = Modifier.testTag("rail_nav_${item.route}"),
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
          NavHost(navController = navController, startDestination = ROUTE_DASHBOARD) {
            composable(ROUTE_DASHBOARD) {
              val dashState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
              DashboardScreen(
                state = dashState,
                onNavigateFocus = { navigateTopLevel(ROUTE_FOCUS) },
                onNavigateTasks = {
                  workspaceTab = WorkspaceTab.TASKS
                  navigateTopLevel(ROUTE_WORKSPACE)
                },
                onNavigateNotes = {
                  workspaceTab = WorkspaceTab.NOTES
                  navigateTopLevel(ROUTE_WORKSPACE)
                },
                onNavigateHabits = {
                  workspaceTab = WorkspaceTab.HABITS
                  navigateTopLevel(ROUTE_WORKSPACE)
                },
                onNavigateSsc = { navigateTopLevel(ROUTE_SSC) },
                onNavigateReports = {
                  insightsTab = InsightsTab.REPORTS
                  navigateTopLevel(ROUTE_INSIGHTS)
                },
                onNavigateBadges = {
                  insightsTab = InsightsTab.GAMIFICATION
                  navigateTopLevel(ROUTE_INSIGHTS)
                },
                onToggleTask = dashboardViewModel::toggleTask,
                onToggleHabit = dashboardViewModel::toggleHabit,
              )
            }

            composable(ROUTE_FOCUS) {
              val focusState by focusViewModel.uiState.collectAsStateWithLifecycle()
              FocusScreen(
                state = focusState,
                onSelectMode = focusViewModel::selectMode,
                onSetCustomMinutes = focusViewModel::setCustomMinutes,
                onSelectSubject = focusViewModel::selectSubject,
                onStartOrResume = focusViewModel::startOrResume,
                onPause = focusViewModel::pause,
                onReset = focusViewModel::reset,
                onFinishEarly = focusViewModel::finishEarlyAndSave,
              )
            }

            composable(ROUTE_WORKSPACE) {
              val workState by workspaceViewModel.uiState.collectAsStateWithLifecycle()
              PlanWorkspaceScreen(
                selectedTab = workspaceTab,
                onSelectTab = { workspaceTab = it },
                state = workState,
                onTaskFilterChange = workspaceViewModel::setTaskFilter,
                onSaveTask = workspaceViewModel::saveTask,
                onUpdateTask = workspaceViewModel::updateExistingTask,
                onToggleTask = workspaceViewModel::toggleTask,
                onDeleteTask = workspaceViewModel::deleteTask,
                onNoteFilterChange = workspaceViewModel::setNoteFilter,
                onNoteSearchChange = workspaceViewModel::setNoteSearchQuery,
                onSaveNote = workspaceViewModel::saveNote,
                onTogglePinNote = workspaceViewModel::togglePinNote,
                onDeleteNote = workspaceViewModel::deleteNote,
                onCreateHabit = workspaceViewModel::createHabit,
                onToggleHabitToday = workspaceViewModel::toggleHabitToday,
                onDeleteHabit = workspaceViewModel::deleteHabit,
              )
            }

            composable(ROUTE_SSC) {
              val sscState by sscViewModel.uiState.collectAsStateWithLifecycle()
              SscStudyCenterScreen(
                state = sscState,
                onToggleChapter = sscViewModel::toggleChapter,
                onAddChapter = sscViewModel::addCustomChapter,
                onUpdateExamDate = sscViewModel::updateExamEpochDay,
                onUpdateDailyGoal = sscViewModel::updateDailyGoal,
              )
            }

            composable(ROUTE_INSIGHTS) {
              val insightsState by insightsViewModel.uiState.collectAsStateWithLifecycle()
              InsightsScreen(
                selectedTab = insightsTab,
                onSelectTab = { insightsTab = it },
                state = insightsState,
                onSelectPeriod = insightsViewModel::setPeriod,
              )
            }

            composable(ROUTE_SETTINGS) {
              SettingsScreen(
                preferences = prefs,
                stats = stats,
                onSetDarkTheme = settingsViewModel::setDarkTheme,
                onSetLanguage = settingsViewModel::setLanguage,
                onSetNotificationsEnabled = settingsViewModel::setNotificationsEnabled,
                onSetFocusAlertsEnabled = settingsViewModel::setFocusAlertsEnabled,
                onSetSoundVibrationEnabled = settingsViewModel::setSoundVibrationEnabled,
                onUpdateUserName = settingsViewModel::updateUserName,
                onUpdateDailyGoal = settingsViewModel::updateDailyGoal,
                onUpdateSscExamDate = settingsViewModel::updateSscExamDate,
                onResetAllData = { settingsViewModel.resetAllData(strings.dataResetDone) },
              )
            }
          }
        }
      }
    }
  }
}
