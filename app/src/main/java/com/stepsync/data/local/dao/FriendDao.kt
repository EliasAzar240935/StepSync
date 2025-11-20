package com.stepsync.data.local.dao

import androidx.room.*
import com.stepsync.data.local.entities.FriendEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Friend operations
 */
@Dao
interface FriendDao {
    @Query("SELECT * FROM friends WHERE userId = :userId AND status = 'accepted' ORDER BY createdAt DESC")
    fun getAllFriends(userId: Long): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE userId = :userId AND status = 'pending' ORDER BY createdAt DESC")
    fun getPendingFriendRequests(userId: Long): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE userId = :userId AND friendId = :friendId LIMIT 1")
    suspend fun getFriendship(userId: Long, friendId: Long): FriendEntity?

    @Query("SELECT COUNT(*) FROM friends WHERE userId = :userId AND status = 'accepted'")
    suspend fun getFriendsCount(userId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity): Long

    @Update
    suspend fun updateFriend(friend: FriendEntity)

    @Delete
    suspend fun deleteFriend(friend: FriendEntity)

    @Query("DELETE FROM friends WHERE userId = :userId AND friendId = :friendId")
    suspend fun deleteFriendship(userId: Long, friendId: Long)

    @Query("UPDATE friends SET status = :status WHERE userId = :userId AND friendId = :friendId")
    suspend fun updateFriendshipStatus(userId: Long, friendId: Long, status: String)
}
