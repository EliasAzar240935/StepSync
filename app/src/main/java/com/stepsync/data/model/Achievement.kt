package com.stepsync.data.model

/**
 * Data model for Achievement
 */
data class Achievement(
    val id: Long = 0,
    val userId: Long,
    val achievementType: String,
    val title: String,
    val description: String,
    val iconName: String,
    val unlockedAt: Long
)
