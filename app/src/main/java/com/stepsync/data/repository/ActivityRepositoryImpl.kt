package com.stepsync.data.repository

import com.stepsync.data.local.dao.ActivityDao
import com.stepsync.data.local.entities.ActivityEntity
import com.stepsync.data.model.Activity
import com.stepsync.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of ActivityRepository
 */
class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao
) : ActivityRepository {

    override fun getAllActivities(userId: Long): Flow<List<Activity>> {
        return activityDao.getAllActivities(userId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getActivitiesByType(userId: Long, type: String): Flow<List<Activity>> {
        return activityDao.getActivitiesByType(userId, type).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getRecentActivities(userId: Long, limit: Int): Flow<List<Activity>> {
        return activityDao.getRecentActivities(userId, limit).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun getTotalCaloriesBetweenTimes(
        userId: Long,
        startTime: Long,
        endTime: Long
    ): Float {
        return activityDao.getTotalCaloriesBetweenTimes(userId, startTime, endTime) ?: 0f
    }

    override suspend fun getTotalDistanceBetweenTimes(
        userId: Long,
        startTime: Long,
        endTime: Long
    ): Float {
        return activityDao.getTotalDistanceBetweenTimes(userId, startTime, endTime) ?: 0f
    }

    override suspend fun insertActivity(activity: Activity): Long {
        val entity = activity.toEntity()
        return activityDao.insertActivity(entity)
    }

    override suspend fun updateActivity(activity: Activity) {
        val entity = activity.toEntity()
        activityDao.updateActivity(entity)
    }

    override suspend fun deleteActivity(activityId: Long) {
        val activity = activityDao.getActivityById(activityId)
        if (activity != null) {
            activityDao.deleteActivity(activity)
        }
    }

    private fun ActivityEntity.toDomainModel(): Activity {
        return Activity(
            id = id,
            userId = userId,
            activityType = activityType,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            distance = distance,
            calories = calories,
            steps = steps,
            notes = notes
        )
    }

    private fun Activity.toEntity(): ActivityEntity {
        return ActivityEntity(
            id = id,
            userId = userId,
            activityType = activityType,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            distance = distance,
            calories = calories,
            steps = steps,
            notes = notes
        )
    }
}
