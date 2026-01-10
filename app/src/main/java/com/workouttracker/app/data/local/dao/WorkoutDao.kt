package com.workouttracker.app.data.local.dao

import androidx.room.*
import com.workouttracker.app.data.local.entity.Workout
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE year = :year ORDER BY dateTime DESC")
    fun getWorkoutsByYear(year: Int): Flow<List<Workout>>

    @Query("SELECT * FROM workouts ORDER BY dateTime DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Query("SELECT DISTINCT date(dateTime) as workout_date FROM workouts WHERE year = :year")
    suspend fun getWorkoutDatesForYear(year: Int): List<String>

    @Query("SELECT COUNT(DISTINCT date(dateTime)) FROM workouts WHERE year = :year")
    fun getWorkoutDayCountForYear(year: Int): Flow<Int>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): Workout?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()
}
