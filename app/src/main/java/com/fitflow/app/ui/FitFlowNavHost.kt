package com.fitflow.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitflow.app.MainActivity
import com.fitflow.app.data.WorkoutLibrary
import com.fitflow.app.data.WorkoutRoutine
import com.fitflow.app.ui.screens.dashboard.DashboardScreen
import com.fitflow.app.ui.screens.habits.HabitsScreen
import com.fitflow.app.ui.screens.onboarding.PermissionRequestScreen
import com.fitflow.app.ui.screens.onboarding.WelcomeScreen
import com.fitflow.app.ui.screens.settings.SettingsScreen
import com.fitflow.app.ui.screens.steps.StepsScreen
import com.fitflow.app.ui.screens.timeline.TimelineScreen
import com.fitflow.app.ui.screens.workout.WorkoutListScreen
import com.fitflow.app.ui.screens.workout.WorkoutSessionScreen

private sealed class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Tab("dashboard", "Home", Icons.Filled.Home)
    object Steps : Tab("steps", "Steps", Icons.Filled.DirectionsWalk)
    object Workout : Tab("workout_list", "Workout", Icons.Filled.FitnessCenter)
    object Habits : Tab("habits", "Habits", Icons.Filled.CheckCircle)
}

private val bottomTabs = listOf(Tab.Dashboard, Tab.Steps, Tab.Workout, Tab.Habits)

@Composable
fun FitFlowNavHost(
    viewModel: AppViewModel,
    onboardingViewModel: OnboardingViewModel,
    settingsViewModel: SettingsViewModel,
    activity: MainActivity,
    onOnboardingFinished: () -> Unit
) {
    val navController = rememberNavController()
    val onboardingComplete by onboardingViewModel.isOnboardingComplete.collectAsStateWithLifecycle()

    // Wait until we know the persisted onboarding state before deciding the
    // start destination, so returning users never flash the Welcome screen.
    if (onboardingComplete == null) return

    NavHost(
        navController = navController,
        startDestination = if (onboardingComplete == true) "main" else "onboarding_welcome"
    ) {
        composable("onboarding_welcome") {
            WelcomeScreen(
                onContinueAsGuest = {
                    onboardingViewModel.continueAsGuest()
                    navController.navigate("onboarding_permission") {
                        popUpTo("onboarding_welcome") { inclusive = true }
                    }
                }
            )
        }
        composable("onboarding_permission") {
            PermissionRequestScreen(
                onContinue = {
                    onOnboardingFinished() // triggers the real Android permission dialogs
                    onboardingViewModel.completeOnboarding()
                    navController.navigate("main") {
                        popUpTo("onboarding_permission") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainScaffold(
                viewModel = viewModel,
                settingsViewModel = settingsViewModel,
                activity = activity
            )
        }
    }
}

@Composable
private fun MainScaffold(
    viewModel: AppViewModel,
    settingsViewModel: SettingsViewModel,
    activity: MainActivity
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            val showBottomBar = bottomTabs.any { tab ->
                currentDestination?.hierarchy?.any { it.route == tab.route } == true
            }
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Tab.Dashboard.route) {
                DashboardScreen(
                    state = uiState,
                    onToggleHabit = viewModel::toggleHabit,
                    onStartWorkout = {
                        navController.navigate(Tab.Workout.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onOpenTimeline = {
                        navController.navigate("timeline")
                    },
                    onOpenSettings = {
                        navController.navigate("settings")
                    }
                )
            }
            composable("timeline") {
                TimelineScreen(
                    days = uiState.timeline,
                    weekTotalSteps = uiState.weekTotalSteps,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    state = settingsState,
                    onThemeModeChange = settingsViewModel::setThemeMode,
                    onNotificationsToggle = settingsViewModel::setNotificationsEnabled,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Tab.Steps.route) {
                StepsScreen(state = uiState, onGoalChange = viewModel::setStepGoal)
            }
            composable(Tab.Workout.route) {
                WorkoutListScreen(
                    onSelectWorkout = { routine ->
                        navController.navigate("workout_session/${routine.id}")
                    }
                )
            }
            composable("workout_session/{routineId}") { backStackEntry ->
                val routineId = backStackEntry.arguments?.getString("routineId")
                val routine: WorkoutRoutine = WorkoutLibrary.all.find { it.id == routineId }
                    ?: WorkoutLibrary.sevenMinuteWorkout
                WorkoutSessionScreen(
                    routine = routine,
                    onFinished = { duration, calories ->
                        viewModel.logWorkout(routine.title, duration, calories)
                        activity.interstitialAdManager.showIfReady(activity) {
                            navController.popBackStack(Tab.Workout.route, inclusive = false)
                        }
                    },
                    onExit = {
                        navController.popBackStack(Tab.Workout.route, inclusive = false)
                    }
                )
            }
            composable(Tab.Habits.route) {
                HabitsScreen(
                    habits = uiState.habits,
                    onAddHabit = viewModel::addHabit,
                    onDeleteHabit = viewModel::deleteHabit,
                    onToggleHabit = viewModel::toggleHabit
                )
            }
        }
    }
}
