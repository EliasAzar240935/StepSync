package com.stepsync.data.repository

import com.stepsync.data.local.dao.UserDao
import com.stepsync.data.local.entities.UserEntity
import com.stepsync.data.model.User
import com.stepsync.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Implementation of UserRepository
 */
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> {
        return userDao.getCurrentUser().map { it?.toDomainModel() }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toDomainModel()
    }

    override suspend fun createUser(
        email: String,
        password: String,
        name: String,
        age: Int,
        weight: Float,
        height: Float,
        fitnessGoal: String
    ): Long {
        val passwordHash = hashPassword(password)
        val user = UserEntity(
            email = email,
            passwordHash = passwordHash,
            name = name,
            age = age,
            weight = weight,
            height = height,
            fitnessGoal = fitnessGoal
        )
        return userDao.insertUser(user)
    }

    override suspend fun updateUser(user: User) {
        val existingUser = userDao.getUserByEmail(user.email)
        if (existingUser != null) {
            val updatedUser = existingUser.copy(
                name = user.name,
                age = user.age,
                weight = user.weight,
                height = user.height,
                fitnessGoal = user.fitnessGoal,
                dailyStepGoal = user.dailyStepGoal,
                updatedAt = System.currentTimeMillis()
            )
            userDao.updateUser(updatedUser)
        }
    }

    override suspend fun updateDailyStepGoal(userId: Long, goal: Int) {
        userDao.updateDailyStepGoal(userId, goal)
    }

    override suspend fun authenticateUser(email: String, password: String): User? {
        val user = userDao.getUserByEmail(email)
        return if (user != null && user.passwordHash == hashPassword(password)) {
            user.toDomainModel()
        } else {
            null
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun UserEntity.toDomainModel(): User {
        return User(
            id = id,
            email = email,
            name = name,
            age = age,
            weight = weight,
            height = height,
            fitnessGoal = fitnessGoal,
            dailyStepGoal = dailyStepGoal
        )
    }
}
