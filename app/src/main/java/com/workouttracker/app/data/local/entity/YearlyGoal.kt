package com.workouttracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "yearly_goals")
data class YearlyGoal(
    @PrimaryKey
    val year: Int,
    val goalDays: Int
)
