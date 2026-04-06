package com.workouttracker.app

import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.api.services.calendar.CalendarScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.workouttracker.app.data.backup.FirestoreService
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
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Sign into Firebase Auth with the Google ID token
                signInToFirebase(account)
            } catch (e: ApiException) {
                Log.e("MainActivity", "Google sign-in failed", e)
                Toast.makeText(this, "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInToFirebase(account: GoogleSignInAccount) {
        val idToken = account.idToken
        if (idToken == null) {
            Toast.makeText(this, "Signed in to Google Drive (no Firebase token)", Toast.LENGTH_SHORT).show()
            viewModel?.updateSignInStatus()
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                Log.d("MainActivity", "Firebase Auth success: ${firebaseAuth.currentUser?.uid}")
                Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show()
                viewModel?.updateSignInStatus()
                // Pull latest data from Firestore
                viewModel?.syncFromFirestore()
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Firebase Auth failed", e)
                Toast.makeText(this, "Signed in to Drive (Firebase sync unavailable)", Toast.LENGTH_SHORT).show()
                viewModel?.updateSignInStatus()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = WorkoutDatabase.getDatabase(applicationContext)
        val repository = WorkoutRepository(
            database.workoutDao(),
            database.yearlyGoalDao()
        )

        val backupService = GoogleDriveBackupService(applicationContext)
        val calendarService = GoogleCalendarService(applicationContext)
        val firestoreService = FirestoreService()

        setContent {
            WorkoutTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val vm: WorkoutViewModel = viewModel(
                        factory = WorkoutViewModelFactory(
                            repository, backupService, calendarService, firestoreService
                        )
                    )
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
        // Request ID token for Firebase Auth, plus Drive and Calendar scopes
        val webClientId = getString(R.string.default_web_client_id)

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
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
