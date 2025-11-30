package com.stepsync.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.hardware. Sensor
import android.hardware.SensorEvent
import android.hardware. SensorEventListener
import android. hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.stepsync.MainActivity
import com.stepsync. R
import com.stepsync.domain.repository.StepRecordRepository
import com.stepsync.util. CalculationUtils
import com.stepsync.util.Constants
import com.stepsync. util.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * Foreground service for tracking steps in the background
 */
@AndroidEntryPoint
class StepCounterService : Service(), SensorEventListener {

    @Inject
    lateinit var stepRecordRepository: StepRecordRepository

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    // Step tracking variables
    private var initialStepCount = -1
    private var currentSteps = 0
    private var dailySteps = 0

    // Track the current date to detect day changes
    private var currentDate: String = ""

    // Store the step count at the start of the day
    private var stepsAtStartOfDay = 0

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        currentDate = DateUtils.getCurrentDate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_SERVICE -> startStepCounting()
            Constants.ACTION_STOP_SERVICE -> stopStepCounting()
        }
        return START_STICKY
    }

    private fun startStepCounting() {
        createNotificationChannel()
        val notification = createNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES. Q) {
            startForeground(
                Constants.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(Constants.NOTIFICATION_ID, notification)
        }

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Load today's steps from database
        loadTodaySteps()

        // Start periodic checks for date change and notification updates
        startPeriodicUpdates()
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor. type == Sensor.TYPE_STEP_COUNTER) {
                val totalStepsFromSensor = it.values[0].toInt()

                // Check if the date has changed (new day)
                val newDate = DateUtils.getCurrentDate()
                if (newDate != currentDate) {
                    // New day started!  Reset for the new day
                    handleDayChange(newDate, totalStepsFromSensor)
                }

                // Initialize on first sensor reading
                if (initialStepCount == -1) {
                    initialStepCount = totalStepsFromSensor
                    stepsAtStartOfDay = totalStepsFromSensor
                }

                // Calculate steps for today
                // Steps today = (current sensor value - sensor value at start of day) + steps already saved today
                currentSteps = totalStepsFromSensor - stepsAtStartOfDay
                dailySteps = currentSteps + getStoredStepsForToday()

                // Save to database
                saveSteps()

                // Update notification
                updateNotification()
            }
        }
    }

    /**
     * Handle the transition to a new day
     */
    private fun handleDayChange(newDate: String, currentSensorValue: Int) {
        // Save the current sensor value as the start of the new day
        stepsAtStartOfDay = currentSensorValue

        // Reset counters for the new day
        currentSteps = 0
        dailySteps = 0

        // Update the current date
        currentDate = newDate

        // Clear the stored steps since it's a new day
        clearStoredStepsForToday()
    }

    /**
     * Get steps already stored for today (from previous app sessions)
     */
    private fun getStoredStepsForToday(): Int {
        return sharedPreferences.getInt("stored_steps_$currentDate", 0)
    }

    /**
     * Save current steps locally for recovery
     */
    private fun saveStoredStepsForToday(steps: Int) {
        sharedPreferences.edit(). putInt("stored_steps_$currentDate", steps).apply()
    }

    /**
     * Clear stored steps (called on new day)
     */
    private fun clearStoredStepsForToday() {
        sharedPreferences.edit().remove("stored_steps_$currentDate").apply()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    private fun loadTodaySteps() {
        val userId = sharedPreferences.getLong(Constants.KEY_USER_ID, 0L)
        currentDate = DateUtils.getCurrentDate()

        serviceScope.launch {
            val record = stepRecordRepository.getStepRecordByDate(userId, currentDate)
            val storedSteps = record?.steps ?: 0
            dailySteps = storedSteps

            // Save to local storage for recovery
            saveStoredStepsForToday(storedSteps)
        }
    }

    private fun saveSteps() {
        val userId = sharedPreferences.getLong(Constants.KEY_USER_ID, 0L)
        if (userId > 0) {
            serviceScope.launch {
                val today = DateUtils.getCurrentDate()
                // Get user weight and height for calculations (using defaults for now)
                val weight = 70f
                val height = 170f

                val distance = CalculationUtils.calculateDistance(dailySteps)
                val calories = CalculationUtils.calculateCaloriesFromSteps(dailySteps, weight, height)

                stepRecordRepository.insertOrUpdateStepRecord(
                    userId = userId,
                    date = today,
                    steps = dailySteps,
                    distance = distance,
                    calories = calories
                )

                // Update local storage
                saveStoredStepsForToday(dailySteps)
            }
        }
    }

    private fun startPeriodicUpdates() {
        serviceScope.launch {
            while (isActive) {
                // Check for date change
                val newDate = DateUtils.getCurrentDate()
                if (newDate != currentDate) {
                    // Trigger day change handling on next sensor event
                    // Or force a reset if no sensor events are coming
                    handleDayChange(newDate, initialStepCount + currentSteps)
                    loadTodaySteps()
                }

                updateNotification()
                delay(60000) // Check every minute
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ). apply {
                description = getString(R.string.notification_channel_description)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager. createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            . setContentTitle(getString(R. string.notification_title))
            .setContentText(getString(R.string. notification_text, dailySteps))
            .setSmallIcon(R.drawable. ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }
}