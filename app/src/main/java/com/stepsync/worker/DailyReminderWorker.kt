package com.stepsync.worker

import android.content.Context
import android.content. SharedPreferences
import androidx.hilt.work.HiltWorker
import androidx.work. CoroutineWorker
import androidx.work.WorkerParameters
import com.stepsync.domain.repository.StepRecordRepository
import com.stepsync.domain.repository.UserRepository
import com.stepsync.util.Constants
import com.stepsync.util.DateUtils
import com.stepsync. util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow. first

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val stepRecordRepository: StepRecordRepository,
    private val userRepository: UserRepository,
    private val sharedPreferences: SharedPreferences
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val userId = sharedPreferences.getString(Constants.KEY_USER_ID, "") ?: ""
            if (userId. isEmpty()) return Result.success()

            val currentUser = userRepository.getCurrentUser().first()
            val todaySteps = stepRecordRepository.observeStepRecordByDate(userId, DateUtils.getCurrentDate()).first()

            val currentSteps = todaySteps?. steps ?: 0
            val goalSteps = currentUser?. dailyStepGoal ?: Constants.DEFAULT_DAILY_STEP_GOAL

            NotificationHelper.sendDailyReminder(applicationContext, currentSteps, goalSteps)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}