package com.workouttracker.app.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.workouttracker.app.ui.components.GoalSettingDialog
import com.workouttracker.app.ui.viewmodel.WorkoutViewModel
import java.time.LocalDate

@Composable
fun WorkoutTrackerApp(
    viewModel: WorkoutViewModel,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showGoalDialog by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    if (showHistory) {
        HistoryScreen(
            uiState = uiState,
            onBack = { showHistory = false }
        )
    } else {
        DashboardScreen(
            uiState = uiState,
            onDateClick = { date ->
                // Date click is handled in DashboardScreen to show workout dialog
            },
            onAddWorkout = { dateTime, workoutType, notes, durationMinutes ->
                viewModel.addWorkout(dateTime, workoutType, notes, durationMinutes)
            },
            onUpdateWorkout = { workout ->
                viewModel.updateWorkout(workout)
            },
            onDeleteWorkout = { workout ->
                viewModel.deleteWorkout(workout)
            },
            onYearChange = { year ->
                viewModel.selectYear(year)
            },
            onMonthChange = { month ->
                // Month change is handled in DashboardScreen
            },
            onSetGoalClick = {
                showGoalDialog = true
            },
            onSyncClick = {
                viewModel.syncToGoogleDrive()
            },
            onSignInClick = onSignInClick,
            onSignOutClick = {
                viewModel.signOut()
            },
            onHistoryClick = {
                showHistory = true
            },
            modifier = modifier
        )
    }

    // Goal setting dialog
    if (showGoalDialog) {
        GoalSettingDialog(
            year = uiState.selectedYear,
            currentGoal = uiState.currentGoal?.goalDays,
            onDismiss = { showGoalDialog = false },
            onSave = { goalDays ->
                viewModel.setYearlyGoal(uiState.selectedYear, goalDays)
            }
        )
    }
}
