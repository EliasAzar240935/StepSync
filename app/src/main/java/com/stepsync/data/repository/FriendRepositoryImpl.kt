package com.stepsync.data.repository

import com.stepsync.data.local.dao.FriendDao
import com.stepsync.data.local.entities.FriendEntity
import com.stepsync.data.model.Friend
import com.stepsync.domain.repository.FriendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of FriendRepository
 */
class FriendRepositoryImpl @Inject constructor(
    private val friendDao: FriendDao
) : FriendRepository {

    override fun getAllFriends(userId: Long): Flow<List<Friend>> {
        return friendDao.getAllFriends(userId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getPendingFriendRequests(userId: Long): Flow<List<Friend>> {
        return friendDao.getPendingFriendRequests(userId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun addFriend(userId: Long, friendEmail: String, friendName: String): Long {
        // In a real app, this would look up the friend by email from a backend
        // For now, we'll create a mock friend ID
        val friendId = friendEmail.hashCode().toLong()
        val friend = FriendEntity(
            userId = userId,
            friendId = friendId,
            friendName = friendName,
            friendEmail = friendEmail,
            status = "pending"
        )
        return friendDao.insertFriend(friend)
    }

    override suspend fun acceptFriendRequest(userId: Long, friendId: Long) {
        friendDao.updateFriendshipStatus(userId, friendId, "accepted")
    }

    override suspend fun removeFriend(userId: Long, friendId: Long) {
        friendDao.deleteFriendship(userId, friendId)
    }

    override suspend fun getFriendsCount(userId: Long): Int {
        return friendDao.getFriendsCount(userId)
    }

    private fun FriendEntity.toDomainModel(): Friend {
        return Friend(
            id = id,
            userId = userId,
            friendId = friendId,
            friendName = friendName,
            friendEmail = friendEmail,
            status = status
        )
    }
}
