package com.stepsync.data.repository

import com. google.firebase.auth.FirebaseAuth
import com.google.firebase. firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stepsync.data.model.Goal
import com.stepsync. domain.repository.GoalRepository
import kotlinx.coroutines. channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseGoalRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : GoalRepository {

    private val goalsCollection = firestore.collection("goals")

    override suspend fun createGoal(goal: Goal) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        val goalData = hashMapOf(
            "userId" to currentUser.uid,
            "title" to goal.title,
            "description" to goal. description,
            "targetSteps" to goal.targetSteps,
            "currentSteps" to 0,
            "goalType" to goal.goalType.name,
            "startDate" to goal.startDate,
            "endDate" to goal.endDate,
            "isCompleted" to false,
            "createdAt" to System.currentTimeMillis(),
            "completedAt" to null
        )

        goalsCollection.add(goalData).await()
    }

    override suspend fun updateGoal(goal: Goal) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        if (goal.id.isEmpty()) throw Exception("Goal ID is required")

        val goalData = hashMapOf(
            "title" to goal.title,
            "description" to goal.description,
            "targetSteps" to goal.targetSteps,
            "goalType" to goal.goalType. name,
            "startDate" to goal.startDate,
            "endDate" to goal.endDate
        )

        goalsCollection. document(goal.id)
            .update(goalData as Map<String, Any>)
            .await()
    }

    override suspend fun deleteGoal(goalId: String) {
        goalsCollection.document(goalId).delete().await()
    }

    override suspend fun getGoalById(goalId: String): Goal? {
        return try {
            val doc = goalsCollection.document(goalId).get().await()
            doc.toObject(Goal::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    override fun getUserGoals(userId:  String): Flow<List<Goal>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = goalsCollection
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("createdAt", Query. Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Goal::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(goals)
            }

        awaitClose { listener.remove() }
    }

    override fun getActiveGoals(userId:  String): Flow<List<Goal>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = goalsCollection
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("isCompleted", false)
            .orderBy("endDate", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val now = System.currentTimeMillis()
                val goals = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Goal::class.java)?.copy(id = doc.id)
                }?.filter { it.endDate >= now } ?: emptyList()

                trySend(goals)
            }

        awaitClose { listener.remove() }
    }

    override fun getCompletedGoals(userId: String): Flow<List<Goal>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = goalsCollection
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("isCompleted", true)
            .orderBy("completedAt", Query.Direction. DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { doc ->
                    doc. toObject(Goal::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(goals)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updateGoalProgress(goalId: String, steps: Int) {
        val goal = getGoalById(goalId) ?: return
        val newSteps = goal.currentSteps + steps

        val updates = hashMapOf<String, Any>(
            "currentSteps" to newSteps
        )

        // Check if goal is now completed
        if (newSteps >= goal.targetSteps && !goal.isCompleted) {
            updates["isCompleted"] = true
            updates["completedAt"] = System. currentTimeMillis()
        }

        goalsCollection.document(goalId).update(updates).await()
    }

    override suspend fun markGoalAsCompleted(goalId:  String) {
        goalsCollection.document(goalId)
            .update(
                mapOf(
                    "isCompleted" to true,
                    "completedAt" to System. currentTimeMillis()
                )
            )
            .await()
    }
}