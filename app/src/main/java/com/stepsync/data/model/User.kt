package com.stepsync.data.model

/**
 * Data model for User
 */
data class User(
    val id: Long = 0,
    val email: String,
    val name: String,
    val age: Int,
    val weight: Float,
    val height: Float,
    val fitnessGoal: String,
    val dailyStepGoal: Int = 10000
)
