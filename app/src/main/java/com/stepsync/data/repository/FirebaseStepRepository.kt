package com.stepsync.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase. firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stepsync. data.model.StepRecord
import com.stepsync.domain.repository.StepRecordRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow. Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks. await
import javax.inject.Inject

/**
 * Firebase implementation of StepRecordRepository
 */
class FirebaseStepRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : StepRecordRepository {

    private val stepRecordsCollection = firestore.collection("stepRecords")

    override suspend fun getStepRecordByDate(userId: String, date: String): StepRecord? {
        val currentUser = auth.currentUser ?: return null

        return try {
            val querySnapshot = stepRecordsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("date", date)
                .limit(1)
                .get()
                .await()

            if (! querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                document.toObject(StepRecord::class.java)?. copy(
                    id = document. id. hashCode().toLong(),
                    userId = currentUser.uid
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun observeStepRecordByDate(userId: String, date: String): Flow<StepRecord?> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val registration = stepRecordsCollection
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("date", date)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val record = snapshot?.documents?.firstOrNull()?. let { document ->
                    document. toObject(StepRecord::class.java)?.copy(
                        id = document.id.hashCode().toLong(),
                        userId = currentUser.uid
                    )
                }

                trySend(record)
            }

        awaitClose { registration.remove() }
    }

    override fun getAllStepRecords(userId: String): Flow<List<StepRecord>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = stepRecordsCollection
            . whereEqualTo("userId", currentUser.uid)
            .orderBy("date", Query.Direction. DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val records = snapshot?.documents?.mapNotNull { document ->
                    document. toObject(StepRecord::class.java)?.copy(
                        id = document.id.hashCode().toLong(),
                        userId = currentUser.uid
                    )
                } ?: emptyList()

                trySend(records)
            }

        awaitClose { registration.remove() }
    }

    override fun getStepRecordsBetweenDates(
        userId: String,
        startDate: String,
        endDate: String
    ): Flow<List<StepRecord>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = stepRecordsCollection
            .whereEqualTo("userId", currentUser.uid)
            . whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val records = snapshot?.documents?.mapNotNull { document ->
                    document. toObject(StepRecord::class.java)?.copy(
                        id = document.id.hashCode().toLong(),
                        userId = currentUser.uid
                    )
                } ?: emptyList()

                trySend(records)
            }

        awaitClose { registration.remove() }
    }

    override fun getRecentStepRecords(userId: String, limit: Int): Flow<List<StepRecord>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = stepRecordsCollection
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("date", Query.Direction. DESCENDING)
            .limit(limit. toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val records = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(StepRecord::class. java)?.copy(
                        id = document.id.hashCode().toLong(),
                        userId = currentUser.uid
                    )
                } ?: emptyList()

                trySend(records)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getTotalStepsBetweenDates(
        userId: String,
        startDate: String,
        endDate: String
    ): Int {
        val currentUser = auth. currentUser ?: return 0

        return try {
            val querySnapshot = stepRecordsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .await()

            querySnapshot.documents.sumOf { document ->
                document.getLong("steps")?.toInt() ?: 0
            }
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun insertOrUpdateStepRecord(
        userId: String,
        date: String,
        steps: Int,
        distance: Float,
        calories: Float
    ) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val existingQuery = stepRecordsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("date", date)
                .limit(1)
                .get()
                .await()

            val recordData = hashMapOf(
                "userId" to currentUser.uid,
                "date" to date,
                "steps" to steps,
                "distance" to distance,
                "calories" to calories,
                "timestamp" to System.currentTimeMillis()
            )

            // Calculate the step difference to add to challenges
            val stepDifference = if (! existingQuery.isEmpty) {
                val oldSteps = existingQuery.documents[0].getLong("steps")?.toInt() ?: 0
                steps - oldSteps  // Only add the NEW steps
            } else {
                steps  // All steps are new
            }

            if (! existingQuery.isEmpty) {
                val documentId = existingQuery.documents[0].id
                stepRecordsCollection.document(documentId).update(recordData as Map<String, Any>).await()
            } else {
                stepRecordsCollection.add(recordData).await()
            }

            // ADD THIS:  Update challenge progress with the new steps
            if (stepDifference > 0) {
                updateChallengeProgress(currentUser.uid, stepDifference)
            }

        } catch (e: Exception) {
            throw Exception("Failed to save step record: ${e.message}")
        }
    }

    override suspend fun updateSteps(userId: String, date: String, steps: Int) {
        val currentUser = auth. currentUser ?: throw Exception("No authenticated user")

        try {
            val querySnapshot = stepRecordsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("date", date)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val documentId = querySnapshot.documents[0].id
                val oldSteps = querySnapshot.documents[0].getLong("steps")?.toInt() ?: 0
                val stepDifference = steps - oldSteps

                stepRecordsCollection.document(documentId)
                    .update(
                        mapOf(
                            "steps" to steps,
                            "timestamp" to System.currentTimeMillis()
                        )
                    )
                    .await()

                // ADD THIS:  Update challenge progress with the new steps
                if (stepDifference > 0) {
                    updateChallengeProgress(currentUser. uid, stepDifference)
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to update steps: ${e.message}")
        }
    }

    private suspend fun updateChallengeProgress(userId:  String, steps: Int) {
        try {
            // Get all active participations for this user
            val participations = firestore.collection("challenge_participations")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            // Update each participation
            participations.documents.forEach { doc ->
                val currentSteps = doc.getLong("currentSteps")?.toInt() ?: 0
                firestore.collection("challenge_participations")
                    .document(doc. id)
                    .update(
                        mapOf(
                            "currentSteps" to (currentSteps + steps),
                            "lastUpdated" to System.currentTimeMillis()
                        )
                    )
                    .await()
            }
        } catch (e: Exception) {
            // Log error but don't throw - step recording should still succeed
            android.util.Log.e("StepRepository", "Failed to update challenge progress:  ${e.message}")
        }
    }
}