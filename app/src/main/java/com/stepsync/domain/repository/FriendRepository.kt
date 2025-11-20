package com.stepsync.domain.repository

import com.stepsync.data.model.Friend
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Friend operations (Domain layer)
 */
interface FriendRepository {
    fun getAllFriends(userId: Long): Flow<List<Friend>>
    fun getPendingFriendRequests(userId: Long): Flow<List<Friend>>
    suspend fun addFriend(userId: Long, friendEmail: String, friendName: String): Long
    suspend fun acceptFriendRequest(userId: Long, friendId: Long)
    suspend fun removeFriend(userId: Long, friendId: Long)
    suspend fun getFriendsCount(userId: Long): Int
}
