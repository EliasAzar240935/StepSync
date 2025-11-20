package com.stepsync.data.local.dao

import androidx.room.*
import com.stepsync.data.local.entities.AchievementEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Achievement operations
 */
@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedAt DESC")
    fun getAllAchievements(userId: Long): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE userId = :userId AND achievementType = :type LIMIT 1")
    suspend fun getAchievementByType(userId: Long, type: String): AchievementEntity?

    @Query("SELECT COUNT(*) FROM achievements WHERE userId = :userId")
    suspend fun getAchievementsCount(userId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity): Long

    @Delete
    suspend fun deleteAchievement(achievement: AchievementEntity)

    @Query("DELETE FROM achievements WHERE userId = :userId")
    suspend fun deleteAllAchievements(userId: Long)
}
