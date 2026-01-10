package com.workouttracker.app.data.backup

import com.workouttracker.app.data.local.entity.Workout
import com.workouttracker.app.data.local.entity.YearlyGoal
import java.time.LocalDateTime

data class BackupData(
    val workouts: List<WorkoutBackup>,
    val goals: List<YearlyGoal>,
    val lastBackupTime: String = LocalDateTime.now().toString()
)

data class WorkoutBackup(
    val id: Long,
    val dateTime: String,
    val endTime: String? = null, // Made nullable for backward compatibility
    val workoutType: String,
    val notes: String,
    val year: Int
) {
    companion object {
        fun fromWorkout(workout: Workout): WorkoutBackup {
            return WorkoutBackup(
                id = workout.id,
                dateTime = workout.dateTime.toString(),
                endTime = workout.endTime.toString(),
                workoutType = workout.workoutType,
                notes = workout.notes,
                year = workout.year
            )
        }
    }

    fun toWorkout(): Workout {
        val startTime = LocalDateTime.parse(dateTime)
        return Workout(
            id = id,
            dateTime = startTime,
            endTime = endTime?.let { LocalDateTime.parse(it) } ?: startTime.plusHours(1),
            workoutType = workoutType,
            notes = notes,
            year = year
        )
    }
}
