package com.stepsync.data.local.dao

import androidx.room.*
import com.stepsync.data.local.entities.StepRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for StepRecord operations
 */
@Dao
interface StepRecordDao {
    @Query("SELECT * FROM step_records WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getStepRecordByDate(userId: Long, date: String): StepRecordEntity?

    @Query("SELECT * FROM step_records WHERE userId = :userId ORDER BY date DESC")
    fun getAllStepRecords(userId: Long): Flow<List<StepRecordEntity>>

    @Query("SELECT * FROM step_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getStepRecordsBetweenDates(userId: Long, startDate: String, endDate: String): Flow<List<StepRecordEntity>>

    @Query("SELECT * FROM step_records WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    fun getRecentStepRecords(userId: Long, limit: Int): Flow<List<StepRecordEntity>>

    @Query("SELECT SUM(steps) FROM step_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalStepsBetweenDates(userId: Long, startDate: String, endDate: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStepRecord(stepRecord: StepRecordEntity): Long

    @Update
    suspend fun updateStepRecord(stepRecord: StepRecordEntity)

    @Delete
    suspend fun deleteStepRecord(stepRecord: StepRecordEntity)

    @Query("DELETE FROM step_records WHERE userId = :userId")
    suspend fun deleteAllStepRecords(userId: Long)
}
