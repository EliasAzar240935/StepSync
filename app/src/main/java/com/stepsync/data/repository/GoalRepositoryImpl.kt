package com.stepsync.data.repository

import com.stepsync.data.local.dao.GoalDao
import com.stepsync.data.local.entities.GoalEntity
import com.stepsync.data.model.Goal
import com.stepsync.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of GoalRepository
 */
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
) : GoalRepository {

    override fun getAllGoals(userId: Long): Flow<List<Goal>> {
        return goalDao.getAllGoals(userId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getActiveGoals(userId: Long): Flow<List<Goal>> {
        return goalDao.getActiveGoals(userId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getCompletedGoals(userId: Long): Flow<List<Goal>> {
        return goalDao.getCompletedGoals(userId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun insertGoal(goal: Goal): Long {
        val entity = goal.toEntity()
        return goalDao.insertGoal(entity)
    }

    override suspend fun updateGoal(goal: Goal) {
        val entity = goal.toEntity()
        goalDao.updateGoal(entity)
    }

    override suspend fun updateGoalProgress(goalId: Long, value: Float) {
        goalDao.updateGoalProgress(goalId, value, System.currentTimeMillis())
    }

    override suspend fun markGoalAsCompleted(goalId: Long) {
        goalDao.markGoalAsCompleted(goalId, System.currentTimeMillis())
    }

    override suspend fun deleteGoal(goalId: Long) {
        val goal = goalDao.getGoalById(goalId)
        if (goal != null) {
            goalDao.deleteGoal(goal)
        }
    }

    private fun GoalEntity.toDomainModel(): Goal {
        return Goal(
            id = id,
            userId = userId,
            goalType = goalType,
            targetValue = targetValue,
            currentValue = currentValue,
            period = period,
            startDate = startDate,
            endDate = endDate,
            isCompleted = isCompleted
        )
    }

    private fun Goal.toEntity(): GoalEntity {
        return GoalEntity(
            id = id,
            userId = userId,
            goalType = goalType,
            targetValue = targetValue,
            currentValue = currentValue,
            period = period,
            startDate = startDate,
            endDate = endDate,
            isCompleted = isCompleted
        )
    }
}
