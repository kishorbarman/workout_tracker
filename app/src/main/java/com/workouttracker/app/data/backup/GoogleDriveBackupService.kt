package com.workouttracker.app.data.backup

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import com.workouttracker.app.data.local.entity.Workout
import com.workouttracker.app.data.local.entity.YearlyGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter

class GoogleDriveBackupService(private val context: Context) {
    private val gson = Gson()
    private val fileName = "workout_tracker_backup.json"
    private val folderName = "WorkoutTrackerBackup"

    companion object {
        fun getGoogleSignInOptions(): GoogleSignInOptions {
            return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE))
                .build()
        }
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Workout Tracker")
            .build()
    }

    suspend fun backupToGoogleDrive(
        workouts: List<Workout>,
        goals: List<YearlyGoal>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
                ?: return@withContext Result.failure(Exception("Not signed in to Google"))

            val driveService = getDriveService(account)

            // Create backup data
            val backupData = BackupData(
                workouts = workouts.map { WorkoutBackup.fromWorkout(it) },
                goals = goals
            )

            val jsonContent = gson.toJson(backupData)

            // Find or create folder
            val folderId = findOrCreateFolder(driveService)

            // Find existing backup file
            val existingFileId = findBackupFile(driveService, folderId)

            if (existingFileId != null) {
                // Update existing file
                updateFile(driveService, existingFileId, jsonContent)
            } else {
                // Create new file
                createFile(driveService, folderId, jsonContent)
            }

            Result.success("Backup successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromGoogleDrive(): Result<BackupData> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
                ?: return@withContext Result.failure(Exception("Not signed in to Google"))

            val driveService = getDriveService(account)

            // Find folder
            val folderId = findOrCreateFolder(driveService)

            // Find backup file
            val fileId = findBackupFile(driveService, folderId)
                ?: return@withContext Result.failure(Exception("No backup file found"))

            // Download file content
            val content = downloadFile(driveService, fileId)

            // Parse JSON
            val backupData = gson.fromJson(content, BackupData::class.java)

            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun findOrCreateFolder(driveService: Drive): String {
        // Search for folder
        val query = "mimeType='application/vnd.google-apps.folder' and name='$folderName' and trashed=false"
        val result = driveService.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        return if (result.files.isNotEmpty()) {
            result.files[0].id
        } else {
            // Create folder
            val folderMetadata = File().apply {
                name = folderName
                mimeType = "application/vnd.google-apps.folder"
            }
            val folder = driveService.files().create(folderMetadata)
                .setFields("id")
                .execute()
            folder.id
        }
    }

    private fun findBackupFile(driveService: Drive, folderId: String): String? {
        val query = "'$folderId' in parents and name='$fileName' and trashed=false"
        val result = driveService.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        return if (result.files.isNotEmpty()) {
            result.files[0].id
        } else {
            null
        }
    }

    private fun createFile(driveService: Drive, folderId: String, content: String) {
        val fileMetadata = File().apply {
            name = fileName
            parents = listOf(folderId)
        }

        val contentStream = content.byteInputStream()
        driveService.files().create(fileMetadata, com.google.api.client.http.InputStreamContent("application/json", contentStream))
            .setFields("id")
            .execute()
    }

    private fun updateFile(driveService: Drive, fileId: String, content: String) {
        val contentStream = content.byteInputStream()
        driveService.files().update(
            fileId,
            null,
            com.google.api.client.http.InputStreamContent("application/json", contentStream)
        ).execute()
    }

    private fun downloadFile(driveService: Drive, fileId: String): String {
        val outputStream = ByteArrayOutputStream()
        driveService.files().get(fileId)
            .executeMediaAndDownloadTo(outputStream)
        return outputStream.toString("UTF-8")
    }

    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    fun getSignedInAccount(): Pair<String?, String?> {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return Pair(account?.email, account?.displayName)
    }

    fun signOut() {
        val client = GoogleSignIn.getClient(context, getGoogleSignInOptions())
        client.signOut()
    }
}
