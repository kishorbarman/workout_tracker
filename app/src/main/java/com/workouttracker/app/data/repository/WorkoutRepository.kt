package com.workouttracker.app.data.repository

import com.workouttracker.app.data.local.dao.WorkoutDao
import com.workouttracker.app.data.local.dao.YearlyGoalDao
import com.workouttracker.app.data.local.entity.Workout
import com.workouttracker.app.data.local.entity.YearlyGoal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val yearlyGoalDao: YearlyGoalDao
) {
    // Workout operations
    fun getWorkoutsByYear(year: Int): Flow<List<Workout>> {
        return workoutDao.getWorkoutsByYear(year)
    }

    fun getAllWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAllWorkouts()
    }

    suspend fun getWorkoutDatesForYear(year: Int): List<LocalDate> {
        return workoutDao.getWorkoutDatesForYear(year).map { LocalDate.parse(it) }
    }

    fun getWorkoutDayCountForYear(year: Int): Flow<Int> {
        return workoutDao.getWorkoutDayCountForYear(year)
    }

    suspend fun insertWorkout(workout: Workout): Long {
        return workoutDao.insertWorkout(workout)
    }

    suspend fun updateWorkout(workout: Workout) {
        workoutDao.updateWorkout(workout)
    }

    suspend fun deleteWorkout(workout: Workout) {
        workoutDao.deleteWorkout(workout)
    }

    // Yearly goal operations
    fun getGoalForYear(year: Int): Flow<YearlyGoal?> {
        return yearlyGoalDao.getGoalForYear(year)
    }

    fun getAllGoals(): Flow<List<YearlyGoal>> {
        return yearlyGoalDao.getAllGoals()
    }

    suspend fun setGoalForYear(year: Int, goalDays: Int) {
        yearlyGoalDao.insertGoal(YearlyGoal(year, goalDays))
    }

    // Helper methods
    suspend fun getWorkoutCountByMonth(year: Int, month: Int): Int {
        val workouts = workoutDao.getWorkoutsByYear(year)
        // This should be calculated in a more efficient way, but for now we'll do it in memory
        // In a production app, you'd want to add a Room query for this
        return 0 // TODO: Implement inViewModel
    }

    suspend fun getCurrentStreak(year: Int): Int {
        // Calculate current streak - to be implemented in ViewModel
        return 0
    }

    suspend fun getLongestStreak(year: Int): Int {
        // Calculate longest streak - to be implemented in ViewModel
        return 0
    }
}
