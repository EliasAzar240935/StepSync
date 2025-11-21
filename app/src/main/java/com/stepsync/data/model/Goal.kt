package com.stepsync.data.model

/**
 * Data model for Goal
 * All properties have default values for Firebase Firestore deserialization
 */
data class Goal(
    val id: Long = 0,
    val userId: Long = 0,
    val goalType: String = "",
    val targetValue: Int = 0,
    val currentValue: Int = 0,
    val period: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = 0
)