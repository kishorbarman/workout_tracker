package com.workouttracker.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workouttracker.app.data.backup.FirestoreService
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
    private val calendarService: GoogleCalendarService,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _selectedYear = MutableStateFlow(Year.now().value)

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        observeSelectedYear()
        updateSignInStatus()
        // If already signed into Firebase, pull latest from Firestore
        if (firestoreService.isSignedIn()) {
            syncFromFirestore()
        }
    }

    fun updateSignInStatus() {
        // Check both Play Services (for Drive/Calendar) and Firebase Auth (for Firestore)
        val playSignedIn = backupService.isSignedIn()
        val firebaseSignedIn = firestoreService.isSignedIn()
        val isSignedIn = playSignedIn || firebaseSignedIn

        val email = firestoreService.getUserEmail() ?: backupService.getSignedInAccount().first
        val name = firestoreService.getUserName() ?: backupService.getSignedInAccount().second

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
        firestoreService.signOut()
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

        viewModelScope.launch {
            try {
                repository.getWorkoutsByYear(year).collect { workouts ->
                    _uiState.update { it.copy(workouts = workouts) }

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

        viewModelScope.launch {
            try {
                repository.getWorkoutDayCountForYear(year).collect { count ->
                    _uiState.update { it.copy(workoutDayCount = count) }
                }
            } catch (_: Exception) {}
        }

        viewModelScope.launch {
            try {
                repository.getGoalForYear(year).collect { goal ->
                    _uiState.update { it.copy(currentGoal = goal) }
                }
            } catch (_: Exception) {}
        }
    }

    fun selectYear(year: Int) {
        _selectedYear.value = year
    }

    fun addWorkout(dateTime: LocalDateTime, workoutType: String, notes: String, durationMinutes: Int = 60) {
        viewModelScope.launch {
            try {
                val workout = Workout(
                    dateTime = dateTime,
                    endTime = dateTime.plusMinutes(durationMinutes.toLong()),
                    workoutType = workoutType,
                    notes = notes
                )
                repository.insertWorkout(workout)
                syncAfterMutation()
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
                syncAfterMutation()
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
                syncAfterMutation()
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
                syncAfterMutation()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error setting goal: ${e.message}")
                }
            }
        }
    }

    /**
     * After any local mutation, push all data to Firestore + Drive + Calendar.
     */
    private fun syncAfterMutation() {
        viewModelScope.launch {
            try {
                val allWorkouts = repository.getAllWorkouts().first()
                val allGoals = repository.getAllGoals().first()

                // Sync to Firestore (primary shared data store)
                if (firestoreService.isSignedIn()) {
                    firestoreService.syncWorkoutsToFirestore(allWorkouts)
                    firestoreService.syncGoalsToFirestore(allGoals)
                }

                // Sync to Drive and Calendar (secondary backups)
                if (backupService.isSignedIn()) {
                    backupService.backupToGoogleDrive(allWorkouts, allGoals)
                    calendarService.syncWorkoutsToCalendar(allWorkouts)
                }
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Sync after mutation failed", e)
            }
        }
    }

    /**
     * Pull latest data from Firestore and replace local Room database.
     * Called on sign-in and on manual sync.
     */
    fun syncFromFirestore() {
        viewModelScope.launch {
            if (!firestoreService.isSignedIn()) return@launch

            _uiState.update { it.copy(isSyncing = true) }

            try {
                val workoutsResult = firestoreService.fetchAllWorkouts()
                val goalsResult = firestoreService.fetchAllGoals()

                if (workoutsResult.isSuccess && goalsResult.isSuccess) {
                    val workouts = workoutsResult.getOrDefault(emptyList())
                    val goals = goalsResult.getOrDefault(emptyList())

                    // Replace local data with Firestore data
                    repository.replaceAllWorkouts(workouts)
                    repository.replaceAllGoals(goals)

                    // Reload current view
                    loadWorkoutsForYear(_selectedYear.value)

                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastSyncStatus = "Synced from cloud (${workouts.size} workouts)"
                        )
                    }
                } else {
                    val error = workoutsResult.exceptionOrNull()?.message
                        ?: goalsResult.exceptionOrNull()?.message
                        ?: "Unknown error"
                    _uiState.update {
                        it.copy(isSyncing = false, lastSyncStatus = "Sync failed: $error")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSyncing = false, lastSyncStatus = "Sync failed: ${e.message}")
                }
            }
        }
    }

    /**
     * Legacy manual sync button — now syncs bidirectionally.
     * Pulls from Firestore first, then pushes to Drive/Calendar.
     */
    fun syncToGoogleDrive() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }

            try {
                // Pull from Firestore first (source of truth)
                if (firestoreService.isSignedIn()) {
                    val workoutsResult = firestoreService.fetchAllWorkouts()
                    val goalsResult = firestoreService.fetchAllGoals()

                    if (workoutsResult.isSuccess && goalsResult.isSuccess) {
                        repository.replaceAllWorkouts(workoutsResult.getOrDefault(emptyList()))
                        repository.replaceAllGoals(goalsResult.getOrDefault(emptyList()))
                        loadWorkoutsForYear(_selectedYear.value)
                    }
                }

                // Then push to Drive and Calendar
                val allWorkouts = repository.getAllWorkouts().first()
                val allGoals = repository.getAllGoals().first()

                if (backupService.isSignedIn()) {
                    val driveResult = backupService.backupToGoogleDrive(allWorkouts, allGoals)
                    val calendarResult = calendarService.syncWorkoutsToCalendar(allWorkouts)

                    if (driveResult.isSuccess && calendarResult.isSuccess) {
                        _uiState.update {
                            it.copy(
                                isSyncing = false,
                                lastSyncStatus = "Synced: ${allWorkouts.size} workouts"
                            )
                        }
                    } else {
                        val errors = mutableListOf<String>()
                        if (driveResult.isFailure) errors.add("Drive: ${driveResult.exceptionOrNull()?.message}")
                        if (calendarResult.isFailure) errors.add("Calendar: ${calendarResult.exceptionOrNull()?.message}")
                        _uiState.update {
                            it.copy(isSyncing = false, lastSyncStatus = "Partial sync: ${errors.joinToString(", ")}")
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastSyncStatus = "Synced from cloud (${allWorkouts.size} workouts)"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSyncing = false, lastSyncStatus = "Sync failed: ${e.message}")
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

                    backupData.workouts.forEach { workoutBackup ->
                        repository.insertWorkout(workoutBackup.toWorkout())
                    }
                    backupData.goals.forEach { goal ->
                        repository.setGoalForYear(goal.year, goal.goalDays)
                    }

                    // Push restored data to Firestore
                    if (firestoreService.isSignedIn()) {
                        val allWorkouts = repository.getAllWorkouts().first()
                        val allGoals = repository.getAllGoals().first()
                        firestoreService.syncWorkoutsToFirestore(allWorkouts)
                        firestoreService.syncGoalsToFirestore(allGoals)
                    }

                    _uiState.update {
                        it.copy(isSyncing = false, lastSyncStatus = "Restored successfully")
                    }
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
                    it.copy(isSyncing = false, lastSyncStatus = "Restore failed: ${e.message}")
                }
            }
        }
    }

    private fun calculateStreaks(workoutDates: Set<LocalDate>): Pair<Int, Int> {
        if (workoutDates.isEmpty()) return Pair(0, 0)

        val sortedDates = workoutDates.sorted()
        var longestStreak = 0
        var tempStreak = 1

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

        var currentStreak = 0
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
