package com.stepsync.data.model

/**
 * Data model for Step Record
 * All properties have default values for Firebase Firestore deserialization
 */
data class StepRecord(
    val id: Long = 0,
    val userId: Long = 0,
    val date: String = "",
    val steps: Int = 0,
    val distance: Float = 0f,
    val calories: Float = 0f,
    val timestamp: Long = 0
)
