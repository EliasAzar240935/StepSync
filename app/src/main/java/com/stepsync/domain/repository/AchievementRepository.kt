package com.stepsync.domain.repository

import com.stepsync.data.model.Achievement
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Achievement operations (Domain layer)
 */
interface AchievementRepository {
    fun getAllAchievements(userId: Long): Flow<List<Achievement>>
    suspend fun unlockAchievement(userId: Long, achievementType: String, title: String, description: String): Long
    suspend fun getAchievementsCount(userId: Long): Int
    suspend fun checkAndUnlockAchievements(userId: Long, totalSteps: Int, consecutiveDays: Int)
}
