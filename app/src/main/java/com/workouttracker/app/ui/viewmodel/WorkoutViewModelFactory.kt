package com.workouttracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.workouttracker.app.data.backup.FirestoreService
import com.workouttracker.app.data.repository.WorkoutRepository

class WorkoutViewModelFactory(
    private val repository: WorkoutRepository,
    private val firestoreService: FirestoreService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(repository, firestoreService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
