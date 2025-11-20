package com.stepsync.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Step record entity for tracking daily step counts
 */
@Entity(tableName = "step_records")
data class StepRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val date: String, // Format: yyyy-MM-dd
    val steps: Int,
    val distance: Float, // in meters
    val calories: Float,
    val timestamp: Long = System.currentTimeMillis()
)
