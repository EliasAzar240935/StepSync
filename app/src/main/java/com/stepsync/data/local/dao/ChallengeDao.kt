package com.stepsync.data.local.dao

import androidx.room.*
import com.stepsync.data.local.entities.ChallengeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Challenge operations
 */
@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges WHERE id = :challengeId")
    suspend fun getChallengeById(challengeId: Long): ChallengeEntity?

    @Query("SELECT * FROM challenges WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE creatorId = :userId ORDER BY createdAt DESC")
    fun getChallengesByCreator(userId: Long): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE participantIds LIKE '%' || :userId || '%' ORDER BY createdAt DESC")
    fun getChallengesByParticipant(userId: Long): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: ChallengeEntity): Long

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Delete
    suspend fun deleteChallenge(challenge: ChallengeEntity)

    @Query("UPDATE challenges SET isActive = 0 WHERE id = :challengeId")
    suspend fun deactivateChallenge(challengeId: Long)
}
