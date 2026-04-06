package com.workouttracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workouttracker.app.ui.components.CalendarView
import com.workouttracker.app.ui.components.WorkoutLogDialog
import com.workouttracker.app.ui.viewmodel.WorkoutUiState
import java.time.LocalDate
import java.time.Year

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: WorkoutUiState,
    onDateClick: (LocalDate) -> Unit,
    onAddWorkout: (java.time.LocalDateTime, String, String, Int) -> Unit,
    onUpdateWorkout: (com.workouttracker.app.data.local.entity.Workout) -> Unit,
    onDeleteWorkout: (com.workouttracker.app.data.local.entity.Workout) -> Unit,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onSetGoalClick: () -> Unit,
    onSyncClick: () -> Unit,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showWorkoutDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedDateWorkouts by remember { mutableStateOf<List<com.workouttracker.app.data.local.entity.Workout>>(emptyList()) }
    var currentMonth by remember { mutableStateOf(LocalDate.now().monthValue) }

    // Update month when year changes
    LaunchedEffect(uiState.selectedYear) {
        if (uiState.selectedYear != Year.now().value) {
            currentMonth = 1
        } else {
            currentMonth = LocalDate.now().monthValue
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Tracker") },
                actions = {
                    // History button
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = "View History")
                    }

                    // Sync status indicator
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }

                    IconButton(onClick = onSyncClick) {
                        Icon(Icons.Default.CloudSync, contentDescription = "Sync")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Year selector
            YearSelector(
                selectedYear = uiState.selectedYear,
                onYearChange = onYearChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Progress card
            ProgressCard(
                uiState = uiState,
                onSetGoalClick = onSetGoalClick,
                modifier = Modifier.padding(16.dp)
            )

            // Calendar
            CalendarView(
                selectedYear = uiState.selectedYear,
                selectedMonth = currentMonth,
                workoutDates = uiState.workoutDates,
                onDateClick = { date ->
                    selectedDate = date
                    // Find workouts for this date
                    selectedDateWorkouts = uiState.workouts.filter { workout ->
                        workout.dateTime.toLocalDate() == date
                    }
                    showWorkoutDialog = true
                },
                onMonthChange = { newMonth ->
                    currentMonth = newMonth
                    // If navigating to different year
                    if (newMonth == 12 && currentMonth == 1) {
                        onYearChange(uiState.selectedYear - 1)
                    } else if (newMonth == 1 && currentMonth == 12) {
                        onYearChange(uiState.selectedYear + 1)
                    }
                }
            )

            // Streak info
            StreakCard(
                currentStreak = uiState.currentStreak,
                longestStreak = uiState.longestStreak,
                modifier = Modifier.padding(16.dp)
            )

            // Google account card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isSignedIn)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (uiState.isSignedIn) Icons.Default.AccountCircle else Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(end = 12.dp),
                            tint = if (uiState.isSignedIn)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.isSignedIn) "Google Account" else "Cloud Sync",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.isSignedIn) {
                                    uiState.userName ?: uiState.userEmail ?: "Signed in"
                                } else {
                                    "Sign in to enable cloud backup"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.isSignedIn)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            if (uiState.isSignedIn && uiState.userName != null && uiState.userEmail != null) {
                                Text(
                                    text = uiState.userEmail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (uiState.isSignedIn) {
                        OutlinedButton(
                            onClick = onSignOutClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Sign out")
                        }
                    } else {
                        Button(
                            onClick = onSignInClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Sign in with Google")
                        }
                    }
                }
            }

            // Sync status
            if (uiState.lastSyncStatus != null && uiState.lastSyncStatus != "Not signed in to Google") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = uiState.lastSyncStatus,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    // Workout log dialog
    if (showWorkoutDialog) {
        WorkoutLogDialog(
            selectedDate = selectedDate,
            existingWorkouts = selectedDateWorkouts,
            onDismiss = { showWorkoutDialog = false },
            onSave = onAddWorkout,
            onUpdate = onUpdateWorkout,
            onDelete = onDeleteWorkout
        )
    }
}

@Composable
fun YearSelector(
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onYearChange(selectedYear - 1) }) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous year")
        }

        Text(
            text = selectedYear.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        IconButton(onClick = { onYearChange(selectedYear + 1) }) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next year")
        }
    }
}

@Composable
fun ProgressCard(
    uiState: WorkoutUiState,
    onSetGoalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.selectedYear} Goal Progress",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onSetGoalClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit goal")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.currentGoal != null) {
                val progress = uiState.workoutDayCount.toFloat() / uiState.currentGoal.goalDays.toFloat()
                val percentage = (progress * 100).toInt()

                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${uiState.workoutDayCount} / ${uiState.currentGoal.goalDays} days",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$percentage% complete",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val remaining = uiState.currentGoal.goalDays - uiState.workoutDayCount
                        Text(
                            text = "$remaining days",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "remaining",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val today = LocalDate.now()
                val currentYear = Year.now().value

                if (uiState.selectedYear == currentYear) {
                    val totalDaysInYear = if (Year.of(uiState.selectedYear).isLeap) 366 else 365
                    val dayOfYear = today.dayOfYear
                    val expectedByNow = uiState.currentGoal.goalDays.toFloat() * dayOfYear / totalDaysInYear
                    val isOnTrack = uiState.workoutDayCount >= expectedByNow
                    val daysRemainingInYear = totalDaysInYear - dayOfYear
                    val workoutsNeeded = maxOf(0, uiState.currentGoal.goalDays - uiState.workoutDayCount)
                    val avgPerWeek = if (daysRemainingInYear > 0) workoutsNeeded.toFloat() / daysRemainingInYear * 7f else 0f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (isOnTrack)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (isOnTrack) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isOnTrack)
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    else
                                        MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOnTrack) "On Track" else "Off Track",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOnTrack)
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    else
                                        MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val frequencyLabel = when {
                                workoutsNeeded == 0 -> "Goal achieved!"
                                daysRemainingInYear <= 0 -> "Year ended"
                                avgPerWeek > 7f -> "Goal unreachable"
                                else -> "%.1f days/week needed".format(avgPerWeek)
                            }
                            Text(
                                text = frequencyLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (workoutsNeeded > 0 && daysRemainingInYear > 0 && avgPerWeek <= 7f) {
                                Text(
                                    text = "to reach goal",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                } else if (uiState.selectedYear < currentYear) {
                    val goalMet = uiState.workoutDayCount >= uiState.currentGoal.goalDays
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (goalMet)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (goalMet) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (goalMet)
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (goalMet) "Goal Achieved!" else "Goal Not Met",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (goalMet)
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No goal set for ${uiState.selectedYear}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onSetGoalClick) {
                    Text("Set Goal")
                }
            }
        }
    }
}

@Composable
fun StreakCard(
    currentStreak: Int,
    longestStreak: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentStreak.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Current Streak",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(80.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = longestStreak.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Longest Streak",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
