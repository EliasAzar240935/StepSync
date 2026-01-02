package com.stepsync.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google. firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stepsync.data.model.Achievement
import com. stepsync.domain.repository.AchievementRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks. await
import javax.inject. Inject

/**
 * Firebase implementation of AchievementRepository
 */
class FirebaseAchievementRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AchievementRepository {

    private val achievementsCollection = firestore.collection("achievements")

    override fun getAllAchievements(userId: String): Flow<List<Achievement>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = achievementsCollection
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("unlockedAt", Query.Direction. DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val achievements = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Achievement::class.java)?. copy(
                        id = document. id. hashCode().toLong(),
                        userId = currentUser. uid
                    )
                } ?: emptyList()

                trySend(achievements)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun unlockAchievement(
        userId: String,
        achievementType: String,
        title: String,
        description: String
    ) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            // Check if achievement already exists
            val existingQuery = achievementsCollection
                . whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("achievementType", achievementType)
                .limit(1)
                .get()
                .await()

            if (existingQuery.isEmpty) {
                val achievementData = hashMapOf(
                    "userId" to currentUser.uid,
                    "achievementType" to achievementType,
                    "title" to title,
                    "description" to description,
                    "iconName" to "trophy",
                    "unlockedAt" to System.currentTimeMillis()
                )

                achievementsCollection.add(achievementData).await()
            }
        } catch (e: Exception) {
            throw Exception("Failed to unlock achievement: ${e.message}")
        }
    }

    override suspend fun getAchievementsCount(userId: String): Int {
        val currentUser = auth.currentUser ?: return 0

        return try {
            val querySnapshot = achievementsCollection
                .whereEqualTo("userId", currentUser.uid)
                . get()
                .await()

            querySnapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun checkAndUnlockAchievements(
        userId: String,
        totalSteps: Int,
        consecutiveDays: Int
    ) {
        val currentUser = auth.currentUser ?: return

        try {
            // Get existing achievements
            val existingAchievements = achievementsCollection
                . whereEqualTo("userId", currentUser.uid)
                .get()
                .await()
                . documents
                .mapNotNull { it.getString("achievementType") }
                .toSet()

            // Check for step milestones
            if (totalSteps >= 100 && ! existingAchievements.contains("first_100")){
                unlockAchievement(userId, "first_100", "First 100", "Baby Steps")
            }
            if (totalSteps >= 1000 && ! existingAchievements.contains("first_1k")) {
                unlockAchievement(userId, "first_1k", "First 1K", "Walk your first 1,000 steps")
            }
            if (totalSteps >= 10000 && !existingAchievements.contains("first_10k")) {
                unlockAchievement(userId, "first_10k", "First 10K", "Reach 10,000 steps in a day")
            }
            if (totalSteps >= 50000 && !existingAchievements.contains("marathon")) {
                unlockAchievement(userId, "marathon", "Marathon Walker", "Walk 50,000 steps")
            }

            // Check for streak achievements
            if (consecutiveDays >= 7 && !existingAchievements.contains("streak_7")) {
                unlockAchievement(userId, "streak_7", "Week Warrior", "7-day step streak")
            }
            if (consecutiveDays >= 30 && !existingAchievements.contains("streak_30")) {
                unlockAchievement(userId, "streak_30", "Monthly Master", "30-day step streak")
            }
        } catch (e: Exception) {
            // Silently fail - achievement checking is not critical
        }
    }
}