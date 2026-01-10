package com.workouttracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateTime: LocalDateTime,
    val endTime: LocalDateTime = dateTime.plusHours(1), // Default 1 hour duration
    val workoutType: String,
    val notes: String = "",
    val year: Int = dateTime.year
)
