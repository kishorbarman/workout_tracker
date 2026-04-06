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

    /**
     * Upload all local workouts to Firestore using batched writes.
     * Deletes all existing docs and writes new ones atomically.
     */
    suspend fun syncWorkoutsToFirestore(workouts: List<Workout>): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("Not signed in"))

                val collection = workoutsCollection(userId)

                // Get existing docs to delete
                val existing = collection.get().await()

                // Firestore batches support max 500 operations.
                // Process in chunks: each chunk deletes old + writes new.
                val docsToDelete = existing.documents
                val allOps = mutableListOf<suspend () -> Unit>()

                // Build batched deletes + writes in groups of 450
                // (leave headroom under the 500 limit)
                val batchSize = 450
                var deleteIndex = 0
                var writeIndex = 0

                while (deleteIndex < docsToDelete.size || writeIndex < workouts.size) {
                    val batch = firestore.batch()
                    var opsInBatch = 0

                    // Add deletes
                    while (deleteIndex < docsToDelete.size && opsInBatch < batchSize) {
                        batch.delete(docsToDelete[deleteIndex].reference)
                        deleteIndex++
                        opsInBatch++
                    }

                    // Add writes
                    while (writeIndex < workouts.size && opsInBatch < batchSize) {
                        val workout = workouts[writeIndex]
                        val data = hashMapOf(
                            "dateTime" to workout.dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            "endTime" to workout.endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            "workoutType" to workout.workoutType,
                            "notes" to workout.notes,
                            "year" to workout.year
                        )
                        val newDocRef = collection.document()
                        batch.set(newDocRef, data)
                        writeIndex++
                        opsInBatch++
                    }

                    batch.commit().await()
                }

                Result.success("Synced ${workouts.size} workouts to Firestore")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Upload all local goals to Firestore using batched writes.
     */
    suspend fun syncGoalsToFirestore(goals: List<YearlyGoal>): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("Not signed in"))

                val collection = goalsCollection(userId)
                val batch = firestore.batch()

                goals.forEach { goal ->
                    val data = hashMapOf(
                        "year" to goal.year,
                        "goalDays" to goal.goalDays
                    )
                    batch.set(collection.document(goal.year.toString()), data)
                }

                batch.commit().await()
                Result.success("Synced ${goals.size} goals to Firestore")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Pull all workouts from Firestore.
     */
    suspend fun fetchAllWorkouts(): Result<List<Workout>> =
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

    /**
     * Pull all goals from Firestore.
     */
    suspend fun fetchAllGoals(): Result<List<YearlyGoal>> =
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
