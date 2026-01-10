package com.workouttracker.app.data.model

enum class WorkoutType(val displayName: String) {
    CARDIO("Cardio"),
    STRENGTH("Strength"),
    YOGA("Yoga"),
    SPORTS("Sports"),
    WALKING("Walking"),
    RUNNING("Running"),
    CYCLING("Cycling"),
    SWIMMING("Swimming"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): WorkoutType {
            return values().find { it.name == value } ?: OTHER
        }
    }
}
