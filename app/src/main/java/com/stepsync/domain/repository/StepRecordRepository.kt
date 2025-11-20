package com.stepsync.domain.repository

import com.stepsync.data.model.StepRecord
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for StepRecord operations (Domain layer)
 */
interface StepRecordRepository {
    suspend fun getStepRecordByDate(userId: Long, date: String): StepRecord?
    fun getAllStepRecords(userId: Long): Flow<List<StepRecord>>
    fun getStepRecordsBetweenDates(userId: Long, startDate: String, endDate: String): Flow<List<StepRecord>>
    fun getRecentStepRecords(userId: Long, limit: Int): Flow<List<StepRecord>>
    suspend fun getTotalStepsBetweenDates(userId: Long, startDate: String, endDate: String): Int
    suspend fun insertOrUpdateStepRecord(userId: Long, date: String, steps: Int, distance: Float, calories: Float)
    suspend fun updateSteps(userId: Long, date: String, steps: Int)
}
