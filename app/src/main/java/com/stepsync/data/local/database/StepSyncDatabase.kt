package com.stepsync.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.stepsync.data.local.dao.*
import com.stepsync.data.local.entities.*

/**
 * Main Room database for StepSync app
 */
@Database(
    entities = [
        UserEntity::class,
        StepRecordEntity::class,
        ActivityEntity::class,
        GoalEntity::class,
        FriendEntity::class,
        AchievementEntity::class,
        ChallengeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StepSyncDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun stepRecordDao(): StepRecordDao
    abstract fun activityDao(): ActivityDao
    abstract fun goalDao(): GoalDao
    abstract fun friendDao(): FriendDao
    abstract fun achievementDao(): AchievementDao
    abstract fun challengeDao(): ChallengeDao
}
