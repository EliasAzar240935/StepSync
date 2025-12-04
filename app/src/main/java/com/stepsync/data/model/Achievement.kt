package com.stepsync.data. model

/**
 * Data model for Achievement
 * All properties have default values for Firebase Firestore deserialization
 */
data class Achievement(
    val id: Long = 0,
    val userId: String = "",  // Changed from Long to String
    val achievementType: String = "",
    val title: String = "",
    val description: String = "",
    val iconName: String = "",
    val unlockedAt: Long = 0
)