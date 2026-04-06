package com.workouttracker.app.data.backup

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.workouttracker.app.data.local.entity.Workout
import com.workouttracker.app.data.local.entity.YearlyGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String? = auth.currentUser?.uid

    private fun workoutsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("workouts")

    private fun goalsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("goals")

    fun isSignedIn(): Boolean = auth.currentUser != null

    fun getUserEmail(): String? = auth.currentUser?.email

    fun getUserName(): String? = auth.currentUser?.displayName

    suspend fun syncWorkoutsToFirestore(workouts: List<Workout>): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("Not signed in"))

                val collection = workoutsCollection(userId)

                // Delete existing workouts in Firestore and replace with local data
                val existing = collection.get().await()
                val batch = firestore.batch()
                existing.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()

                // Upload all workouts
                workouts.forEach { workout ->
                    val data = hashMapOf(
                        "dateTime" to workout.dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "endTime" to workout.endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "workoutType" to workout.workoutType,
                        "notes" to workout.notes,
                        "year" to workout.year
                    )
                    collection.add(data).await()
                }

                Result.success("Synced ${workouts.size} workouts to Firestore")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun syncGoalsToFirestore(goals: List<YearlyGoal>): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("Not signed in"))

                val collection = goalsCollection(userId)

                goals.forEach { goal ->
                    val data = hashMapOf(
                        "year" to goal.year,
                        "goalDays" to goal.goalDays
                    )
                    collection.document(goal.year.toString()).set(data).await()
                }

                Result.success("Synced ${goals.size} goals to Firestore")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun restoreWorkoutsFromFirestore(): Result<List<Workout>> =
        withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("Not signed in"))

                val snapshot = workoutsCollection(userId).get().await()
                val workouts = snapshot.documents.mapNotNull { doc ->
                    try {
                        val dateTime = LocalDateTime.parse(
                            doc.getString("dateTime"),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        )
                        val endTime = LocalDateTime.parse(
                            doc.getString("endTime") ?: doc.getString("dateTime"),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        )
                        Workout(
                            dateTime = dateTime,
                            endTime = endTime,
                            workoutType = doc.getString("workoutType") ?: "OTHER",
                            notes = doc.getString("notes") ?: "",
                            year = (doc.getLong("year") ?: dateTime.year.toLong()).toInt()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                Result.success(workouts)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun restoreGoalsFromFirestore(): Result<List<YearlyGoal>> =
        withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("Not signed in"))

                val snapshot = goalsCollection(userId).get().await()
                val goals = snapshot.documents.mapNotNull { doc ->
                    try {
                        YearlyGoal(
                            year = (doc.getLong("year") ?: return@mapNotNull null).toInt(),
                            goalDays = (doc.getLong("goalDays") ?: return@mapNotNull null).toInt()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                Result.success(goals)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun signOut() {
        auth.signOut()
    }
}
