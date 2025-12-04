package com.stepsync.domain.repository

import com.stepsync. data.model.Goal
import kotlinx.coroutines.flow. Flow

/**
 * Repository interface for Goal operations (Domain layer)
 */
interface GoalRepository {
    fun getAllGoals(userId: String): Flow<List<Goal>>
    fun getActiveGoals(userId: String): Flow<List<Goal>>
    fun getCompletedGoals(userId: String): Flow<List<Goal>>
    suspend fun insertGoal(goal: Goal): Long
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(goalId: Long)
    suspend fun updateGoalProgress(goalId: Long, currentValue: Int)
    suspend fun markGoalAsCompleted(goalId: Long)
}