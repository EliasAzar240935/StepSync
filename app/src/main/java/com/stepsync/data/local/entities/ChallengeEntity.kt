package com.stepsync.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Challenge entity for group challenges
 */
@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val challengeType: String, // "steps", "distance", "activities"
    val targetValue: Float,
    val startDate: String, // Format: yyyy-MM-dd
    val endDate: String, // Format: yyyy-MM-dd
    val participantIds: String, // Comma-separated user IDs
    val creatorId: Long,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
