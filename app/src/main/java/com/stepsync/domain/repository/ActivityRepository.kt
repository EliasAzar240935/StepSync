package com.stepsync.domain.repository

import com.stepsync.data.model.Activity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Activity operations (Domain layer)
 */
interface ActivityRepository {
    fun getAllActivities(userId: Long): Flow<List<Activity>>
    fun getActivitiesByType(userId: Long, type: String): Flow<List<Activity>>
    fun getRecentActivities(userId: Long, limit: Int): Flow<List<Activity>>
    suspend fun getTotalCaloriesBetweenTimes(userId: Long, startTime: Long, endTime: Long): Float
    suspend fun getTotalDistanceBetweenTimes(userId: Long, startTime: Long, endTime: Long): Float
    suspend fun insertActivity(activity: Activity): Long
    suspend fun updateActivity(activity: Activity)
    suspend fun deleteActivity(activityId: Long)
}
