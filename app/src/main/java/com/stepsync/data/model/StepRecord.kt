package com.stepsync.data.model

/**
 * Data model for Step Record
 */
data class StepRecord(
    val id: Long = 0,
    val userId: Long,
    val date: String,
    val steps: Int,
    val distance: Float,
    val calories: Float
)
