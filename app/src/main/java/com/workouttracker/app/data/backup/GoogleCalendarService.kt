package com.workouttracker.app.data.backup

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.workouttracker.app.data.local.entity.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class GoogleCalendarService(private val context: Context) {

    private val calendarName = "Kishor's workout schedule"

    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null
    }

    private fun getCalendarService(): Calendar? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null

        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(CalendarScopes.CALENDAR)
        )
        credential.selectedAccount = account.account

        return Calendar.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Workout Tracker")
            .build()
    }

    suspend fun testCalendarAccess(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val calendarService = getCalendarService()
                ?: return@withContext Result.failure(Exception("Not signed in"))

            // Try to list calendars to verify access
            val calendarList = calendarService.calendarList().list().execute()
            Result.success("Calendar access working. Found ${calendarList.items?.size ?: 0} calendars")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncWorkoutsToCalendar(workouts: List<Workout>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val calendarService = getCalendarService()
                ?: return@withContext Result.failure(Exception("Not signed in to Google"))

            // Get or create the workout calendar
            val calendarId = getOrCreateWorkoutCalendar(calendarService)

            // Clear existing events in the calendar
            clearCalendarEvents(calendarService, calendarId)

            // Add all workouts as events
            workouts.forEach { workout ->
                createCalendarEvent(calendarService, calendarId, workout)
            }

            Result.success("Synced ${workouts.size} workouts to Google Calendar")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getOrCreateWorkoutCalendar(calendarService: Calendar): String {
        // Search for existing calendar
        val calendarList = calendarService.calendarList().list().execute()
        val existingCalendar = calendarList.items?.find { it.summary == calendarName }

        if (existingCalendar != null) {
            return existingCalendar.id
        }

        // Create new calendar
        val calendar = com.google.api.services.calendar.model.Calendar().apply {
            summary = calendarName
            description = "Workout tracking calendar synced from Workout Tracker app"
            timeZone = ZoneId.systemDefault().id
        }

        val createdCalendar = calendarService.calendars().insert(calendar).execute()
        return createdCalendar.id
    }

    private fun clearCalendarEvents(calendarService: Calendar, calendarId: String) {
        try {
            // Get all events from the calendar
            val events = calendarService.events().list(calendarId).execute()

            // Delete each event
            events.items?.forEach { event ->
                try {
                    calendarService.events().delete(calendarId, event.id).execute()
                } catch (e: Exception) {
                    // Continue even if delete fails
                }
            }
        } catch (e: Exception) {
            // Calendar might be empty or doesn't exist yet
        }
    }

    private fun createCalendarEvent(
        calendarService: Calendar,
        calendarId: String,
        workout: Workout
    ) {
        val event = Event().apply {
            summary = "Workout: ${workout.workoutType}"
            description = if (workout.notes.isNotEmpty()) workout.notes else "Workout session"

            // Convert LocalDateTime to RFC3339 format
            val startDateTime = EventDateTime().apply {
                dateTime = com.google.api.client.util.DateTime(
                    workout.dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                timeZone = ZoneId.systemDefault().id
            }

            val endDateTime = EventDateTime().apply {
                dateTime = com.google.api.client.util.DateTime(
                    workout.endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                timeZone = ZoneId.systemDefault().id
            }

            start = startDateTime
            end = endDateTime

            // Set color based on workout type (optional)
            colorId = when (workout.workoutType) {
                "CARDIO" -> "9" // Blue
                "STRENGTH" -> "11" // Red
                "FLEXIBILITY" -> "10" // Green
                "SPORTS" -> "5" // Yellow
                else -> "1" // Lavender (default)
            }
        }

        calendarService.events().insert(calendarId, event).execute()
    }

    companion object {
        fun getSignInOptions(): GoogleSignInOptions {
            return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(
                    com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR),
                    com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.file")
                )
                .build()
        }
    }
}
