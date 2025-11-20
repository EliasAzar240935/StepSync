package com.stepsync.di

import android.content.Context
import androidx.room.Room
import com.stepsync.data.local.dao.*
import com.stepsync.data.local.database.StepSyncDatabase
import com.stepsync.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database instances
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StepSyncDatabase {
        return Room.databaseBuilder(
            context,
            StepSyncDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: StepSyncDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideStepRecordDao(database: StepSyncDatabase): StepRecordDao {
        return database.stepRecordDao()
    }

    @Provides
    @Singleton
    fun provideActivityDao(database: StepSyncDatabase): ActivityDao {
        return database.activityDao()
    }

    @Provides
    @Singleton
    fun provideGoalDao(database: StepSyncDatabase): GoalDao {
        return database.goalDao()
    }

    @Provides
    @Singleton
    fun provideFriendDao(database: StepSyncDatabase): FriendDao {
        return database.friendDao()
    }

    @Provides
    @Singleton
    fun provideAchievementDao(database: StepSyncDatabase): AchievementDao {
        return database.achievementDao()
    }

    @Provides
    @Singleton
    fun provideChallengeDao(database: StepSyncDatabase): ChallengeDao {
        return database.challengeDao()
    }
}
