package com.stepsync.data.local.dao

import androidx.room.*
import com.stepsync.data.local.entities.GoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Goal operations
 */
@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Long): GoalEntity?

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllGoals(userId: Long): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveGoals(userId: Long): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND isCompleted = 1 ORDER BY updatedAt DESC")
    fun getCompletedGoals(userId: Long): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND period = :period AND isCompleted = 0")
    fun getGoalsByPeriod(userId: Long, period: String): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteAllGoals(userId: Long)

    @Query("UPDATE goals SET currentValue = :value, updatedAt = :timestamp WHERE id = :goalId")
    suspend fun updateGoalProgress(goalId: Long, value: Float, timestamp: Long)

    @Query("UPDATE goals SET isCompleted = 1, updatedAt = :timestamp WHERE id = :goalId")
    suspend fun markGoalAsCompleted(goalId: Long, timestamp: Long)
}
