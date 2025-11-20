package com.stepsync.data.repository

import com.stepsync.data.local.dao.StepRecordDao
import com.stepsync.data.local.entities.StepRecordEntity
import com.stepsync.data.model.StepRecord
import com.stepsync.domain.repository.StepRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of StepRecordRepository
 */
class StepRecordRepositoryImpl @Inject constructor(
    private val stepRecordDao: StepRecordDao
) : StepRecordRepository {

    override suspend fun getStepRecordByDate(userId: Long, date: String): StepRecord? {
        return stepRecordDao.getStepRecordByDate(userId, date)?.toDomainModel()
    }

    override fun getAllStepRecords(userId: Long): Flow<List<StepRecord>> {
        return stepRecordDao.getAllStepRecords(userId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getStepRecordsBetweenDates(
        userId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<StepRecord>> {
        return stepRecordDao.getStepRecordsBetweenDates(userId, startDate, endDate).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getRecentStepRecords(userId: Long, limit: Int): Flow<List<StepRecord>> {
        return stepRecordDao.getRecentStepRecords(userId, limit).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun getTotalStepsBetweenDates(
        userId: Long,
        startDate: String,
        endDate: String
    ): Int {
        return stepRecordDao.getTotalStepsBetweenDates(userId, startDate, endDate) ?: 0
    }

    override suspend fun insertOrUpdateStepRecord(
        userId: Long,
        date: String,
        steps: Int,
        distance: Float,
        calories: Float
    ) {
        val existingRecord = stepRecordDao.getStepRecordByDate(userId, date)
        if (existingRecord != null) {
            val updated = existingRecord.copy(
                steps = steps,
                distance = distance,
                calories = calories,
                timestamp = System.currentTimeMillis()
            )
            stepRecordDao.updateStepRecord(updated)
        } else {
            val newRecord = StepRecordEntity(
                userId = userId,
                date = date,
                steps = steps,
                distance = distance,
                calories = calories
            )
            stepRecordDao.insertStepRecord(newRecord)
        }
    }

    override suspend fun updateSteps(userId: Long, date: String, steps: Int) {
        val existingRecord = stepRecordDao.getStepRecordByDate(userId, date)
        if (existingRecord != null) {
            val updated = existingRecord.copy(
                steps = steps,
                timestamp = System.currentTimeMillis()
            )
            stepRecordDao.updateStepRecord(updated)
        }
    }

    private fun StepRecordEntity.toDomainModel(): StepRecord {
        return StepRecord(
            id = id,
            userId = userId,
            date = date,
            steps = steps,
            distance = distance,
            calories = calories
        )
    }
}
