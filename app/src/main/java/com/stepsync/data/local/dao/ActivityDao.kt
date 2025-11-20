package com.stepsync.data.local.dao

import androidx.room.*
import com.stepsync.data.local.entities.ActivityEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Activity operations
 */
@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: Long): ActivityEntity?

    @Query("SELECT * FROM activities WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllActivities(userId: Long): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE userId = :userId AND activityType = :type ORDER BY startTime DESC")
    fun getActivitiesByType(userId: Long, type: String): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE userId = :userId AND startTime BETWEEN :startTime AND :endTime ORDER BY startTime DESC")
    fun getActivitiesBetweenTimes(userId: Long, startTime: Long, endTime: Long): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE userId = :userId ORDER BY startTime DESC LIMIT :limit")
    fun getRecentActivities(userId: Long, limit: Int): Flow<List<ActivityEntity>>

    @Query("SELECT SUM(calories) FROM activities WHERE userId = :userId AND startTime BETWEEN :startTime AND :endTime")
    suspend fun getTotalCaloriesBetweenTimes(userId: Long, startTime: Long, endTime: Long): Float?

    @Query("SELECT SUM(distance) FROM activities WHERE userId = :userId AND startTime BETWEEN :startTime AND :endTime")
    suspend fun getTotalDistanceBetweenTimes(userId: Long, startTime: Long, endTime: Long): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    @Query("DELETE FROM activities WHERE userId = :userId")
    suspend fun deleteAllActivities(userId: Long)
}
