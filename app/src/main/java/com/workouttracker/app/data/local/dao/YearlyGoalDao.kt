package com.workouttracker.app.data.local.dao

import androidx.room.*
import com.workouttracker.app.data.local.entity.YearlyGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface YearlyGoalDao {
    @Query("SELECT * FROM yearly_goals WHERE year = :year")
    fun getGoalForYear(year: Int): Flow<YearlyGoal?>

    @Query("SELECT * FROM yearly_goals ORDER BY year DESC")
    fun getAllGoals(): Flow<List<YearlyGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: YearlyGoal)

    @Delete
    suspend fun deleteGoal(goal: YearlyGoal)

    @Query("DELETE FROM yearly_goals")
    suspend fun deleteAllGoals()
}
