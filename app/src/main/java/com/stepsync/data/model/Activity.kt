package com.stepsync.data.model

/**
 * Data model for Activity
 */
data class Activity(
    val id: Long = 0,
    val userId: Long,
    val activityType: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val distance: Float,
    val calories: Float,
    val steps: Int = 0,
    val notes: String = ""
)
