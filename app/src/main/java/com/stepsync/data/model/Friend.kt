package com.stepsync.data.model

/**
 * Data model for Friend
 */
data class Friend(
    val id: Long = 0,
    val userId: Long,
    val friendId: Long,
    val friendName: String,
    val friendEmail: String,
    val status: String
)
