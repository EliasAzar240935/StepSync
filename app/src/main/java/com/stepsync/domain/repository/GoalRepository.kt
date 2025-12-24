package com.stepsync.domain.repository

import com.stepsync.data.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    suspend fun createGoal(goal: Goal)
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(goalId: String)
    suspend fun getGoalById(goalId: String): Goal?
    fun getUserGoals(userId: String): Flow<List<Goal>>
    fun getActiveGoals(userId: String): Flow<List<Goal>>
    fun getCompletedGoals(userId: String): Flow<List<Goal>>
    suspend fun updateGoalProgress(goalId: String, steps: Int)
    suspend fun markGoalAsCompleted(goalId: String)
}