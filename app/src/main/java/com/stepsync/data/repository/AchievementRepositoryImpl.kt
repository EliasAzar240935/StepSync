package com.stepsync.data.repository

import com.stepsync.data.local.dao.AchievementDao
import com.stepsync.data.local.entities.AchievementEntity
import com.stepsync.data.model.Achievement
import com.stepsync.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of AchievementRepository
 */
class AchievementRepositoryImpl @Inject constructor(
    private val achievementDao: AchievementDao
) : AchievementRepository {

    override fun getAllAchievements(userId: Long): Flow<List<Achievement>> {
        return achievementDao.getAllAchievements(userId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun unlockAchievement(
        userId: Long,
        achievementType: String,
        title: String,
        description: String
    ): Long {
        // Check if achievement already exists
        val existing = achievementDao.getAchievementByType(userId, achievementType)
        if (existing != null) {
            return existing.id
        }

        val achievement = AchievementEntity(
            userId = userId,
            achievementType = achievementType,
            title = title,
            description = description,
            iconName = achievementType
        )
        return achievementDao.insertAchievement(achievement)
    }

    override suspend fun getAchievementsCount(userId: Long): Int {
        return achievementDao.getAchievementsCount(userId)
    }

    override suspend fun checkAndUnlockAchievements(
        userId: Long,
        totalSteps: Int,
        consecutiveDays: Int
    ) {
        // Check for step milestones
        when {
            totalSteps >= 1000 && achievementDao.getAchievementByType(userId, "first_1k") == null -> {
                unlockAchievement(userId, "first_1k", "First 1K", "Walk your first 1,000 steps")
            }
            totalSteps >= 10000 && achievementDao.getAchievementByType(userId, "first_10k") == null -> {
                unlockAchievement(userId, "first_10k", "First 10K", "Reach 10,000 steps in a day")
            }
            totalSteps >= 50000 && achievementDao.getAchievementByType(userId, "marathon") == null -> {
                unlockAchievement(userId, "marathon", "Marathon Walker", "Walk 50,000 steps")
            }
        }

        // Check for streak achievements
        when {
            consecutiveDays >= 7 && achievementDao.getAchievementByType(userId, "streak_7") == null -> {
                unlockAchievement(userId, "streak_7", "Week Warrior", "7-day step streak")
            }
            consecutiveDays >= 30 && achievementDao.getAchievementByType(userId, "streak_30") == null -> {
                unlockAchievement(userId, "streak_30", "Monthly Master", "30-day step streak")
            }
        }
    }

    private fun AchievementEntity.toDomainModel(): Achievement {
        return Achievement(
            id = id,
            userId = userId,
            achievementType = achievementType,
            title = title,
            description = description,
            iconName = iconName,
            unlockedAt = unlockedAt
        )
    }
}
