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

    fun getGoalForYear(year: Int): Flow<YearlyGoal?> {
        return yearlyGoalDao.getGoalForYear(year)
    }

    fun getAllGoals(): Flow<List<YearlyGoal>> {
        return yearlyGoalDao.getAllGoals()
    }

    suspend fun setGoalForYear(year: Int, goalDays: Int) {
        yearlyGoalDao.insertGoal(YearlyGoal(year, goalDays))
    }

    /**
     * Replace all local data with data from Firestore.
     */
    suspend fun replaceAllWorkouts(workouts: List<Workout>) {
        workoutDao.deleteAllWorkouts()
        workouts.forEach { workoutDao.insertWorkout(it) }
    }

    suspend fun replaceAllGoals(goals: List<YearlyGoal>) {
        yearlyGoalDao.deleteAllGoals()
        goals.forEach { yearlyGoalDao.insertGoal(it) }
    }
}
