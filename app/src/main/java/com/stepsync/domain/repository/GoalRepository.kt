package com.stepsync.domain.repository

import com.stepsync.data.model.Goal
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Goal operations (Domain layer)
 */
interface GoalRepository {
    fun getAllGoals(userId: Long): Flow<List<Goal>>
    fun getActiveGoals(userId: Long): Flow<List<Goal>>
    fun getCompletedGoals(userId: Long): Flow<List<Goal>>
    suspend fun insertGoal(goal: Goal): Long
    suspend fun updateGoal(goal: Goal)
    suspend fun updateGoalProgress(goalId: Long, value: Float)
    suspend fun markGoalAsCompleted(goalId: Long)
    suspend fun deleteGoal(goalId: Long)
}
