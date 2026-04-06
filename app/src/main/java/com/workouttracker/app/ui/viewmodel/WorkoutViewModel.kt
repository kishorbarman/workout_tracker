package com.workouttracker.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workouttracker.app.data.backup.FirestoreService
import com.workouttracker.app.data.local.entity.Workout
import com.workouttracker.app.data.local.entity.YearlyGoal
import com.workouttracker.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val firestoreService: FirestoreService
) : ViewModel() {

    // Serializes all Firestore operations so push and pull don't overlap
    private val firestoreMutex = Mutex()

    private val _selectedYear = MutableStateFlow(Year.now().value)

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        observeSelectedYear()
        updateSignInStatus()
        if (firestoreService.isSignedIn()) {
            syncFromFirestore()
        }
    }

    fun updateSignInStatus() {
        val isSignedIn = firestoreService.isSignedIn()
        val email = firestoreService.getUserEmail()
        val name = firestoreService.getUserName()

        _uiState.update {
            it.copy(
                isSignedIn = isSignedIn,
                userEmail = email,
                userName = name,
                lastSyncStatus = if (!isSignedIn) "Not signed in" else it.lastSyncStatus
            )
        }
    }

    fun signOut() {
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
                    val dates = workouts.map { it.dateTime.toLocalDate() }.toSet()
                    val streaks = calculateStreaks(dates)
                    _uiState.update {
                        it.copy(
                            workouts = workouts,
                            workoutDates = dates,
                            currentStreak = streaks.first,
                            longestStreak = streaks.second,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error loading workouts: ${e.message}")
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
                pushToFirestore()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error adding workout: ${e.message}") }
            }
        }
    }

    fun updateWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                repository.updateWorkout(workout)
                pushToFirestore()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error updating workout: ${e.message}") }
            }
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                repository.deleteWorkout(workout)
                pushToFirestore()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error deleting workout: ${e.message}") }
            }
        }
    }

    fun setYearlyGoal(year: Int, goalDays: Int) {
        viewModelScope.launch {
            try {
                repository.setGoalForYear(year, goalDays)
                pushToFirestore()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error setting goal: ${e.message}") }
            }
        }
    }

    /**
     * Push all local data to Firestore after a mutation.
     * Uses mutex so this completes before any pull can start.
     */
    private fun pushToFirestore() {
        viewModelScope.launch {
            if (!firestoreService.isSignedIn()) return@launch
            firestoreMutex.withLock {
                try {
                    val allWorkouts = repository.getAllWorkouts().first()
                    val allGoals = repository.getAllGoals().first()
                    firestoreService.syncWorkoutsToFirestore(allWorkouts)
                    firestoreService.syncGoalsToFirestore(allGoals)
                } catch (e: Exception) {
                    Log.e("WorkoutViewModel", "Firestore push failed", e)
                }
            }
        }
    }

    /**
     * Pull data from Firestore and replace local Room DB.
     * Uses mutex so this waits for any in-flight push to finish first.
     */
    fun syncFromFirestore() {
        viewModelScope.launch {
            if (!firestoreService.isSignedIn()) return@launch

            _uiState.update { it.copy(isSyncing = true) }

            firestoreMutex.withLock {
                try {
                    val workoutsResult = firestoreService.fetchAllWorkouts()
                    val goalsResult = firestoreService.fetchAllGoals()

                    if (workoutsResult.isSuccess && goalsResult.isSuccess) {
                        val workouts = workoutsResult.getOrDefault(emptyList())
                        val goals = goalsResult.getOrDefault(emptyList())

                        repository.replaceAllWorkouts(workouts)
                        repository.replaceAllGoals(goals)
                        loadWorkoutsForYear(_selectedYear.value)

                        _uiState.update {
                            it.copy(
                                isSyncing = false,
                                lastSyncStatus = "Synced (${workouts.size} workouts)"
                            )
                        }
                    } else {
                        val error = workoutsResult.exceptionOrNull()?.message
                            ?: goalsResult.exceptionOrNull()?.message ?: "Unknown error"
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
    }

    /**
     * Manual sync button: pull from Firestore (source of truth).
     */
    fun sync() {
        syncFromFirestore()
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
