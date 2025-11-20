package com.stepsync.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Friend entity for managing friend relationships
 */
@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val friendId: Long,
    val friendName: String,
    val friendEmail: String,
    val status: String, // "pending", "accepted", "blocked"
    val createdAt: Long = System.currentTimeMillis()
)
