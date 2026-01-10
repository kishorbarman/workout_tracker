package com.workouttracker.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.api.services.calendar.CalendarScopes
import com.workouttracker.app.data.backup.GoogleDriveBackupService
import com.workouttracker.app.data.backup.GoogleCalendarService
import com.workouttracker.app.data.local.database.WorkoutDatabase
import com.workouttracker.app.data.repository.WorkoutRepository
import com.workouttracker.app.ui.screens.WorkoutTrackerApp
import com.workouttracker.app.ui.theme.WorkoutTrackerTheme
import com.workouttracker.app.ui.viewmodel.WorkoutViewModel
import com.workouttracker.app.ui.viewmodel.WorkoutViewModelFactory

class MainActivity : ComponentActivity() {
    private var viewModel: WorkoutViewModel? = null

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle sign-in result if needed
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Signed in to Google Drive", Toast.LENGTH_SHORT).show()
            // Update sign-in status in ViewModel
            viewModel?.updateSignInStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database and repository
        val database = WorkoutDatabase.getDatabase(applicationContext)
        val repository = WorkoutRepository(
            database.workoutDao(),
            database.yearlyGoalDao()
        )

        // Initialize backup services
        val backupService = GoogleDriveBackupService(applicationContext)
        val calendarService = GoogleCalendarService(applicationContext)

        // Don't prompt for sign-in automatically - let user do it from settings
        // Google Sign-In can be enabled later via the sync button

        setContent {
            WorkoutTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val vm: WorkoutViewModel = viewModel(
                        factory = WorkoutViewModelFactory(repository, backupService, calendarService)
                    )
                    // Assign to class property so signInLauncher can access it
                    this@MainActivity.viewModel = vm

                    WorkoutTrackerApp(
                        viewModel = vm,
                        onSignInClick = { promptGoogleSignIn() }
                    )
                }
            }
        }
    }

    private fun promptGoogleSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope(DriveScopes.DRIVE_FILE),
                Scope(CalendarScopes.CALENDAR)
            )
            .build()

        val client = GoogleSignIn.getClient(this, signInOptions)
        signInLauncher.launch(client.signInIntent)
    }
}
