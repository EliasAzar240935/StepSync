package com.stepsync.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Achievement entity for tracking unlocked achievements
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val achievementType: String, // "first_1k", "first_10k", "streak_7", "streak_30", etc.
    val title: String,
    val description: String,
    val iconName: String,
    val unlockedAt: Long = System.currentTimeMillis()
)
