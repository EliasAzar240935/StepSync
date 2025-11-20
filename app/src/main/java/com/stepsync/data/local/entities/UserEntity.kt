package com.stepsync.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User entity representing a user in the app
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val name: String,
    val age: Int,
    val weight: Float, // in kg
    val height: Float, // in cm
    val fitnessGoal: String, // "weight_loss", "muscle_gain", "fitness", "health"
    val dailyStepGoal: Int = 10000,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
