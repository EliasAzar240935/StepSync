package com.stepsync.domain.repository

import com.stepsync.data.model.Achievement
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Achievement operations (Domain layer)
 */
interface AchievementRepository {
    fun getAllAchievements(userId: String): Flow<List<Achievement>>
    suspend fun unlockAchievement(userId: String, achievementType: String, title: String, description: String)
    suspend fun getAchievementsCount(userId: String): Int
    suspend fun checkAndUnlockAchievements(userId: String, totalSteps: Int, consecutiveDays: Int)
}