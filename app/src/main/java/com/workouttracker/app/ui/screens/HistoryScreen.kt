package com.workouttracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workouttracker.app.data.local.entity.Workout
import com.workouttracker.app.data.model.WorkoutType
import com.workouttracker.app.ui.viewmodel.WorkoutUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: WorkoutUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Weekly trends
            item {
                WeeklyTrendsCard(
                    workouts = uiState.workouts,
                    year = uiState.selectedYear,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Monthly trends
            item {
                MonthlyTrendsCard(
                    workouts = uiState.workouts,
                    year = uiState.selectedYear,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Workout list header
            item {
                Text(
                    text = "All Workouts (${uiState.workouts.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Workout list
            items(uiState.workouts.sortedByDescending { it.dateTime }) { workout ->
                WorkoutListItem(
                    workout = workout,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (uiState.workouts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No workouts logged yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyTrendsCard(
    workouts: List<Workout>,
    year: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Weekly Trends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val weeklyData = workouts.groupBy { workout ->
                val weekFields = WeekFields.of(Locale.getDefault())
                workout.dateTime.toLocalDate().get(weekFields.weekOfYear())
            }

            val currentWeek = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfYear())
            val last4Weeks = (currentWeek - 3..currentWeek).toList()

            last4Weeks.forEach { weekNumber ->
                val workoutCount = weeklyData[weekNumber]?.size ?: 0
                val workoutDays = weeklyData[weekNumber]?.map { it.dateTime.toLocalDate() }?.toSet()?.size ?: 0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Week $weekNumber",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$workoutDays days",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = (workoutDays / 7f).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MonthlyTrendsCard(
    workouts: List<Workout>,
    year: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Monthly Trends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val monthlyData = workouts.groupBy { workout ->
                workout.dateTime.month
            }

            val currentMonth = LocalDate.now().month
            val monthsToShow = listOf(
                currentMonth.minus(2),
                currentMonth.minus(1),
                currentMonth
            )

            monthsToShow.forEach { month ->
                val workoutDays = monthlyData[month]?.map { it.dateTime.toLocalDate() }?.toSet()?.size ?: 0
                val daysInMonth = when (month) {
                    java.time.Month.FEBRUARY -> if (year % 4 == 0) 29 else 28
                    java.time.Month.APRIL, java.time.Month.JUNE, java.time.Month.SEPTEMBER, java.time.Month.NOVEMBER -> 30
                    else -> 31
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = month.toString().lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$workoutDays / $daysInMonth days",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = (workoutDays.toFloat() / daysInMonth).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
fun WorkoutListItem(
    workout: Workout,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on workout type
            Icon(
                imageVector = when (WorkoutType.fromString(workout.workoutType)) {
                    WorkoutType.CARDIO, WorkoutType.RUNNING -> Icons.Default.DirectionsRun
                    WorkoutType.STRENGTH -> Icons.Default.FitnessCenter
                    WorkoutType.YOGA -> Icons.Default.SelfImprovement
                    WorkoutType.CYCLING -> Icons.Default.DirectionsBike
                    WorkoutType.SWIMMING -> Icons.Default.Pool
                    else -> Icons.Default.FitnessCenter
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = WorkoutType.fromString(workout.workoutType).displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = workout.dateTime.format(dateFormatter) + " at " + workout.dateTime.format(timeFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (workout.notes.isNotEmpty()) {
                    Text(
                        text = workout.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
