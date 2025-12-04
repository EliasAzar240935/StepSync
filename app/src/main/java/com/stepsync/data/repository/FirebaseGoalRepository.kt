package com.stepsync.data. repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stepsync.data.model.Goal
import com. stepsync.domain.repository. GoalRepository
import kotlinx. coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow. callbackFlow
import kotlinx. coroutines.tasks.await
import javax.inject.Inject

/**
 * Firebase implementation of GoalRepository
 */
class FirebaseGoalRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : GoalRepository {

    private val goalsCollection = firestore. collection("goals")

    override fun getAllGoals(userId: String): Flow<List<Goal>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = goalsCollection
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("createdAt", Query.Direction. DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Goal::class.java)?.copy(
                        id = document.id.hashCode().toLong(),
                        userId = currentUser.uid
                    )
                } ?: emptyList()

                trySend(goals)
            }

        awaitClose { registration.remove() }
    }

    override fun getActiveGoals(userId: String): Flow<List<Goal>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = goalsCollection
            .whereEqualTo("userId", currentUser. uid)
            .whereEqualTo("isCompleted", false)
            .orderBy("createdAt", Query. Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Goal::class.java)?.copy(
                        id = document.id.hashCode().toLong(),
                        userId = currentUser.uid
                    )
                } ?: emptyList()

                trySend(goals)
            }

        awaitClose { registration.remove() }
    }

    override fun getCompletedGoals(userId: String): Flow<List<Goal>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = goalsCollection
            .whereEqualTo("userId", currentUser. uid)
            .whereEqualTo("isCompleted", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Goal::class.java)?. copy(
                        id = document.id.hashCode().toLong(),
                        userId = currentUser.uid
                    )
                } ?: emptyList()

                trySend(goals)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun insertGoal(goal: Goal): Long {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val goalData = hashMapOf(
                "userId" to currentUser.uid,
                "goalType" to goal.goalType,
                "targetValue" to goal.targetValue,
                "currentValue" to goal.currentValue,
                "period" to goal.period,
                "startDate" to goal.startDate,
                "endDate" to goal.endDate,
                "isCompleted" to goal.isCompleted,
                "createdAt" to System.currentTimeMillis()
            )

            val documentRef = goalsCollection.add(goalData).await()
            return documentRef.id.hashCode().toLong()
        } catch (e: Exception) {
            throw Exception("Failed to insert goal: ${e.message}")
        }
    }

    override suspend fun updateGoal(goal: Goal) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val querySnapshot = goalsCollection
                .whereEqualTo("userId", currentUser.uid)
                . get()
                .await()

            val document = querySnapshot.documents.find {
                it.id.hashCode().toLong() == goal.id
            } ?: throw Exception("Goal not found")

            val goalData = hashMapOf(
                "userId" to currentUser. uid,
                "goalType" to goal.goalType,
                "targetValue" to goal.targetValue,
                "currentValue" to goal.currentValue,
                "period" to goal.period,
                "startDate" to goal.startDate,
                "endDate" to goal.endDate,
                "isCompleted" to goal.isCompleted,
                "createdAt" to goal.createdAt
            )

            goalsCollection. document(document.id).update(goalData as Map<String, Any>).await()
        } catch (e: Exception) {
            throw Exception("Failed to update goal: ${e.message}")
        }
    }

    override suspend fun deleteGoal(goalId: Long) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val querySnapshot = goalsCollection
                .whereEqualTo("userId", currentUser. uid)
                .get()
                .await()

            val document = querySnapshot.documents. find {
                it.id.hashCode().toLong() == goalId
            } ?: throw Exception("Goal not found")

            goalsCollection. document(document.id).delete().await()
        } catch (e: Exception) {
            throw Exception("Failed to delete goal: ${e. message}")
        }
    }

    override suspend fun updateGoalProgress(goalId: Long, currentValue: Int) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val querySnapshot = goalsCollection
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val document = querySnapshot.documents.find {
                it. id.hashCode().toLong() == goalId
            } ?: throw Exception("Goal not found")

            goalsCollection.document(document.id)
                .update("currentValue", currentValue)
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to update goal progress: ${e. message}")
        }
    }

    override suspend fun markGoalAsCompleted(goalId: Long) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val querySnapshot = goalsCollection
                . whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val document = querySnapshot. documents.find {
                it.id.hashCode().toLong() == goalId
            } ?: throw Exception("Goal not found")

            goalsCollection.document(document.id)
                .update("isCompleted", true)
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to mark goal as completed: ${e. message}")
        }
    }
}