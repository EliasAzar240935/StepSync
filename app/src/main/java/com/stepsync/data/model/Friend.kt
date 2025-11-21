package com.stepsync.data.model

/**
 * Data model for Friend
 * All properties have default values for Firebase Firestore deserialization
 */
data class Friend(
    val id: Long = 0,
    val userId: Long = 0,
    val friendUserId: Long = 0,
    val friendName: String = "",
    val friendEmail: String = "",
    val status: String = "",
    val createdAt: Long = 0
)