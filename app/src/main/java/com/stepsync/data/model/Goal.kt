package com.stepsync.data.model

/**
 * Data model for Goal
 */
data class Goal(
    val id: Long = 0,
    val userId: Long,
    val goalType: String,
    val targetValue: Float,
    val currentValue: Float = 0f,
    val period: String,
    val startDate: String,
    val endDate: String,
    val isCompleted: Boolean = false
)
