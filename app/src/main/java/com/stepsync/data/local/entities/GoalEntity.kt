package com.stepsync.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Goal entity for tracking user fitness goals
 */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val goalType: String, // "steps", "distance", "calories", "activities"
    val targetValue: Float,
    val currentValue: Float = 0f,
    val period: String, // "daily", "weekly", "monthly"
    val startDate: String, // Format: yyyy-MM-dd
    val endDate: String, // Format: yyyy-MM-dd
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
