package com.stepsync.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Activity entity for tracking various fitness activities
 */
@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val activityType: String, // "walking", "running", "cycling", "gym", "swimming"
    val startTime: Long,
    val endTime: Long,
    val duration: Long, // in seconds
    val distance: Float, // in meters
    val calories: Float,
    val steps: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
