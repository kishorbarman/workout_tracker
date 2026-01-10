package com.workouttracker.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.workouttracker.app.data.local.converter.Converters
import com.workouttracker.app.data.local.dao.WorkoutDao
import com.workouttracker.app.data.local.dao.YearlyGoalDao
import com.workouttracker.app.data.local.entity.Workout
import com.workouttracker.app.data.local.entity.YearlyGoal

@Database(
    entities = [Workout::class, YearlyGoal::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun yearlyGoalDao(): YearlyGoalDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                )
                    .fallbackToDestructiveMigration() // For simplicity, recreate DB on schema change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
