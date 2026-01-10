package com.workouttracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workouttracker.app.data.backup.GoogleDriveBackupService
import com.workouttracker.app.data.backup.GoogleCalendarService
import com.workouttracker.app.data.local.entity.Workout
import com.workouttracker.app.data.local.entity.YearlyGoal
import com.workouttracker.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year

data class WorkoutUiState(
    val selectedYear: Int = Year.now().value,
    val workouts: List<Workout> = emptyList(),
    val workoutDates: Set<LocalDate> = emptySet(),
    val currentGoal: YearlyGoal? = null,
    val workoutDayCount: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSyncing: Boolean = false,
    val lastSyncStatus: String? = null,
    val isSignedIn: Boolean = false,
    val userEmail: String? = null,
    val userName: String? = null
)

class WorkoutViewModel(
    private val repository: WorkoutRepository,
    private val backupService: GoogleDriveBackupService,
    private val calendarService: GoogleCalendarService
) : ViewModel() {

    private val _selectedYear = MutableStateFlow(Year.now().value)

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        observeSelectedYear()
        updateSignInStatus()
    }

    fun updateSignInStatus() {
        val isSignedIn = backupService.isSignedIn()
        val (email, name) = backupService.getSignedInAccount()

        _uiState.update {
            it.copy(
                isSignedIn = isSignedIn,
                userEmail = email,
                userName = name,
                lastSyncStatus = if (!isSignedIn) "Not signed in to Google" else it.lastSyncStatus
            )
        }
    }

    fun signOut() {
        backupService.signOut()
        updateSignInStatus()
    }

    private fun observeSelectedYear() {
        viewModelScope.launch {
            _selectedYear.collect { year ->
                loadWorkoutsForYear(year)
            }
        }
    }

    private fun loadWorkoutsForYear(year: Int) {
        _uiState.update { it.copy(isLoading = true, selectedYear = year) }

        // Collect workouts
        viewModelScope.launch {
            try {
                repository.getWorkoutsByYear(year).collect { workouts ->
                    _uiState.update { it.copy(workouts = workouts) }

                    // Update workout dates and streaks when workouts change
                    val dates = workouts.map { it.dateTime.toLocalDate() }.toSet()
                    val streaks = calculateStreaks(dates)

                    _uiState.update {
                        it.copy(
                            workoutDates = dates,
                            currentStreak = streaks.first,
                            longestStreak = streaks.second,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error loading workouts: ${e.message}"
                    )
                }
            }
        }

        // Collect workout day count
        viewModelScope.launch {
            try {
                repository.getWorkoutDayCountForYear(year).collect { count ->
                    _uiState.update { it.copy(workoutDayCount = count) }
                }
            } catch (e: Exception) {
                // Silently fail for count
            }
        }

        // Collect goal
        viewModelScope.launch {
            try {
                repository.getGoalForYear(year).collect { goal ->
                    _uiState.update { it.copy(currentGoal = goal) }
                }
            } catch (e: Exception) {
                // Silently fail for goal
            }
        }
    }

    fun selectYear(year: Int) {
        _selectedYear.value = year
    }

    fun addWorkout(dateTime: LocalDateTime, workoutType: String, notes: String) {
        viewModelScope.launch {
            try {
                val workout = Workout(
                    dateTime = dateTime,
                    workoutType = workoutType,
                    notes = notes
                )
                repository.insertWorkout(workout)

                // Auto-sync to Google Drive
                syncToGoogleDrive()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error adding workout: ${e.message}")
                }
            }
        }
    }

    fun updateWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                repository.updateWorkout(workout)
                syncToGoogleDrive()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error updating workout: ${e.message}")
                }
            }
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                repository.deleteWorkout(workout)
                syncToGoogleDrive()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error deleting workout: ${e.message}")
                }
            }
        }
    }

    fun setYearlyGoal(year: Int, goalDays: Int) {
        viewModelScope.launch {
            try {
                repository.setGoalForYear(year, goalDays)
                syncToGoogleDrive()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error setting goal: ${e.message}")
                }
            }
        }
    }

    fun syncToGoogleDrive() {
        viewModelScope.launch {
            if (!backupService.isSignedIn()) {
                _uiState.update { it.copy(lastSyncStatus = "Not signed in to Google") }
                return@launch
            }

            _uiState.update { it.copy(isSyncing = true) }

            try {
                val allWorkouts = mutableListOf<Workout>()
                repository.getAllWorkouts().first().let { workouts ->
                    allWorkouts.addAll(workouts)
                }

                val allGoals = mutableListOf<YearlyGoal>()
                repository.getAllGoals().first().let { goals ->
                    allGoals.addAll(goals)
                }

                val driveResult = backupService.backupToGoogleDrive(allWorkouts, allGoals)
                val calendarResult = calendarService.syncWorkoutsToCalendar(allWorkouts)

                if (driveResult.isSuccess && calendarResult.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastSyncStatus = "Synced successfully to Drive and Calendar"
                        )
                    }
                } else {
                    val errors = mutableListOf<String>()
                    if (driveResult.isFailure) {
                        errors.add("Drive: ${driveResult.exceptionOrNull()?.message}")
                    }
                    if (calendarResult.isFailure) {
                        errors.add("Calendar: ${calendarResult.exceptionOrNull()?.message}")
                    }
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastSyncStatus = "Sync failed: ${errors.joinToString(", ")}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncStatus = "Sync failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun restoreFromGoogleDrive() {
        viewModelScope.launch {
            if (!backupService.isSignedIn()) {
                _uiState.update { it.copy(lastSyncStatus = "Not signed in to Google") }
                return@launch
            }

            _uiState.update { it.copy(isSyncing = true) }

            try {
                val result = backupService.restoreFromGoogleDrive()

                if (result.isSuccess) {
                    val backupData = result.getOrNull()!!

                    // Restore workouts
                    backupData.workouts.forEach { workoutBackup ->
                        repository.insertWorkout(workoutBackup.toWorkout())
                    }

                    // Restore goals
                    backupData.goals.forEach { goal ->
                        repository.setGoalForYear(goal.year, goal.goalDays)
                    }

                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastSyncStatus = "Restored successfully"
                        )
                    }

                    // Reload current year data
                    loadWorkoutsForYear(_selectedYear.value)
                } else {
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastSyncStatus = "Restore failed: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncStatus = "Restore failed: ${e.message}"
                    )
                }
            }
        }
    }

    private fun calculateStreaks(workoutDates: Set<LocalDate>): Pair<Int, Int> {
        if (workoutDates.isEmpty()) return Pair(0, 0)

        val sortedDates = workoutDates.sorted()
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 1

        // Calculate longest streak
        for (i in 1 until sortedDates.size) {
            val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(sortedDates[i - 1], sortedDates[i])
            if (daysDiff == 1L) {
                tempStreak++
            } else {
                longestStreak = maxOf(longestStreak, tempStreak)
                tempStreak = 1
            }
        }
        longestStreak = maxOf(longestStreak, tempStreak)

        // Calculate current streak
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        if (sortedDates.contains(today) || sortedDates.contains(yesterday)) {
            currentStreak = 1
            var checkDate = if (sortedDates.contains(today)) today.minusDays(1) else yesterday.minusDays(1)

            while (sortedDates.contains(checkDate)) {
                currentStreak++
                checkDate = checkDate.minusDays(1)
            }
        }

        return Pair(currentStreak, longestStreak)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
