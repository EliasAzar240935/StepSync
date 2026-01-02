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
import android.content.Context
import com.stepsync.domain.repository.AchievementRepository
import com.stepsync.util.NotificationHelper
import javax.inject.Inject

/**
 * Firebase implementation of StepRecordRepository
 */
class FirebaseStepRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val context: Context,
    private val achievementRepository: AchievementRepository
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

    override suspend fun insertStepRecord(stepRecord: StepRecord): Long {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val recordData = hashMapOf(
                "userId" to currentUser.uid,
                "date" to stepRecord.date,
                "steps" to stepRecord. steps,
                "distance" to stepRecord.distance,
                "calories" to stepRecord.calories,
                "timestamp" to System.currentTimeMillis()
            )

            val documentRef = stepRecordsCollection.add(recordData).await()

            // Update goal progress after inserting
            updateGoalProgress(currentUser.uid)

            // === 1. COLLECT ALL STEP RECORDS FOR USER ===
            val stepRecords = stepRecordsCollection
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(StepRecord::class.java) }

// === 2. CALCULATE TOTAL STEPS ===
            val totalSteps = stepRecords.sumOf { it.steps }

            // === 3. CALCULATE CONSECUTIVE DAY STREAK ===
            fun calculateStreak(records: List<StepRecord>): Int {
                if (records.isEmpty()) return 0
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd")
                val dates = records.map { sdf.parse(it.date)!! }.sortedDescending()
                var streak = 1
                for (i in 1 until dates.size) {
                    val diff = ((dates[i-1].time - dates[i].time) / (1000 * 60 * 60 * 24)).toInt()
                    if (diff == 1) streak++
                    else break
                }
                return streak
            }
            val consecutiveDays = calculateStreak(stepRecords)

// === 4. CHECK ACHIEVEMENTS ===
            achievementRepository.checkAndUnlockAchievements(
                currentUser.uid, totalSteps, consecutiveDays
            )

            return documentRef. id. hashCode().toLong()
        } catch (e: Exception) {
            throw Exception("Failed to insert step record: ${e.message}")
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

            updateGoalProgress(currentUser.uid)

            // Update goal progress after inserting
            updateGoalProgress(currentUser.uid)

// === Begin Achievements block ===
            val stepRecords = stepRecordsCollection
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(StepRecord::class.java) }

            val totalSteps = stepRecords.sumOf { it.steps }

            fun calculateStreak(records: List<StepRecord>): Int {
                if (records.isEmpty()) return 0
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd")
                val dates = records.map { sdf.parse(it.date)!! }.sortedDescending()
                var streak = 1
                for (i in 1 until dates.size) {
                    val diff = ((dates[i-1].time - dates[i].time) / (1000 * 60 * 60 * 24)).toInt()
                    if (diff == 1) streak++
                    else break
                }
                return streak
            }
            val consecutiveDays = calculateStreak(stepRecords)

            achievementRepository.checkAndUnlockAchievements(
                currentUser.uid, totalSteps, consecutiveDays
            )
// === End Achievements block ===

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

    private suspend fun updateGoalProgress(userId: String) {
        try {
            android.util.Log.d("StepRepository", "🔄 Updating goal progress for user: $userId")

            // Get all active goals
            val goalsCollection = firestore.collection("goals")
            val activeGoals = goalsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isCompleted", false)
                .get()
                .await()

            android.util.Log.d("StepRepository", "Found ${activeGoals.documents.size} active goals")

            for (goalDoc in activeGoals.documents) {
                try {
                    val goalType = goalDoc.getString("goalType") ?: continue
                    val goalName = goalDoc.getString("name") ?: "Goal"
                    val targetSteps = goalDoc.getLong("targetSteps")?.toInt() ?: continue
                    val startDate = goalDoc.getLong("startDate") ?: 0L
                    val endDate = goalDoc.getLong("endDate") ?: 0L
                    val previousSteps = goalDoc.getLong("currentSteps")?.toInt() ?: 0
                    val wasCompleted = goalDoc.getBoolean("isCompleted") ?: false

                    // Calculate date range based on goal type
                    val dateStrings = when (goalType) {
                        "DAILY" -> listOf(getTodayDateString())
                        "WEEKLY" -> getLast7DaysStrings()
                        "MONTHLY" -> getLast30DaysStrings()
                        "CUSTOM" -> getDateRangeStrings(startDate, endDate)
                        else -> listOf(getTodayDateString())
                    }

                    // Sum steps for all dates in range
                    var totalSteps = 0
                    for (dateStr in dateStrings) {
                        val stepDocs = stepRecordsCollection
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("date", dateStr)
                            .get()
                            .await()

                        totalSteps += stepDocs.documents.sumOf { it.getLong("steps")?.toInt() ?: 0 }
                    }

                    android.util.Log. d("StepRepository", "Goal ${goalDoc.id}:  $totalSteps / $targetSteps steps")

                    // Update goal's current steps
                    goalsCollection.document(goalDoc.id)
                        .update("currentSteps", totalSteps)
                        .await()

                    // Check if goal was just completed (not previously completed)
                    val isNowCompleted = totalSteps >= targetSteps

                    if (! wasCompleted && isNowCompleted) {
                        android. util.Log.d("StepRepository", "🎉 Goal ${goalDoc.id} completed!")

                        // Mark as completed
                        goalsCollection.document(goalDoc.id)
                            .update(mapOf(
                                "isCompleted" to true,
                                "completedAt" to System.currentTimeMillis()
                            ))
                            .await()

                        // Send notification
                        try {
                            NotificationHelper.sendGoalCompletedNotification(
                                context,
                                goalName,
                                totalSteps
                            )
                            android.util.Log.d("StepRepository", "📬 Sent completion notification for $goalName")
                        } catch (e: Exception) {
                            android.util.Log.e("StepRepository", "Failed to send notification", e)
                        }
                    }

                } catch (e: Exception) {
                    android.util.Log. e("StepRepository", "Error updating goal ${goalDoc.id}", e)
                }
            }

        } catch (e: Exception) {
            // Don't throw - step recording should still succeed
            android.util.Log.e("StepRepository", "Failed to update goal progress: ${e. message}")
        }
    }

    // Helper functions for date strings
    private fun getTodayDateString(): String {
        val calendar = java.util.Calendar.getInstance()
        return formatDateString(calendar.timeInMillis)
    }

    private fun getLast7DaysStrings(): List<String> {
        val dates = mutableListOf<String>()
        val calendar = java.util.Calendar. getInstance()
        for (i in 0 until 7) {
            dates.add(formatDateString(calendar.timeInMillis))
            calendar.add(java. util.Calendar.DAY_OF_YEAR, -1)
        }
        return dates
    }

    private fun getLast30DaysStrings(): List<String> {
        val dates = mutableListOf<String>()
        val calendar = java.util.Calendar.getInstance()
        for (i in 0 until 30) {
            dates.add(formatDateString(calendar.timeInMillis))
            calendar.add(java.util.Calendar. DAY_OF_YEAR, -1)
        }
        return dates
    }

    private fun getDateRangeStrings(startMillis: Long, endMillis:  Long): List<String> {
        val dates = mutableListOf<String>()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = startMillis

        val endCal = java.util.Calendar.getInstance()
        endCal.timeInMillis = endMillis

        while (calendar.timeInMillis <= endCal.timeInMillis) {
            dates.add(formatDateString(calendar. timeInMillis))
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        return dates
    }

    private fun formatDateString(timeInMillis: Long): String {
        val calendar = java.util. Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val year = calendar. get(java.util.Calendar. YEAR)
        val month = calendar.get(java.util. Calendar.MONTH) + 1
        val day = calendar.get(java.util. Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }
}