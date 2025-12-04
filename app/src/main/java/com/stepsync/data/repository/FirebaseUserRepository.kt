package com.stepsync.data.repository

import com. google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore. FirebaseFirestore
import com. stepsync.data.model. User
import com.stepsync.domain.repository.UserRepository
import kotlinx.coroutines. channels.awaitClose
import kotlinx.coroutines.flow. Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines. tasks.await
import javax.inject.Inject

/**
 * Firebase implementation of UserRepository
 */
class FirebaseUserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override fun getCurrentUser(): Flow<User? > = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Listen to user document changes
                val docRef = usersCollection.document(firebaseUser.uid)
                docRef. addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(null)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val user = snapshot.toObject(User::class. java)?.copy(
                            id = firebaseUser.uid  // Changed: use uid directly as String
                        )
                        trySend(user)
                    } else {
                        trySend(null)
                    }
                }
            } else {
                trySend(null)
            }
        }

        auth.addAuthStateListener(authStateListener)

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("email", email)
                .limit(1)
                . get()
                .await()

            if (!querySnapshot. isEmpty) {
                val document = querySnapshot.documents[0]
                document.toObject(User::class.java)?.copy(
                    id = document.id  // Changed: use document id directly as String
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createUser(
        email: String,
        password: String,
        name: String,
        age: Int,
        weight: Float,
        height: Float,
        fitnessGoal: String
    ): String {  // Changed return type from Long to String
        try {
            // Create Firebase Auth user
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            // Update profile with name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                . build()
            firebaseUser. updateProfile(profileUpdates).await()

            // Create user document in Firestore
            val userDoc = hashMapOf(
                "email" to email,
                "name" to name,
                "age" to age,
                "weight" to weight,
                "height" to height,
                "fitnessGoal" to fitnessGoal,
                "dailyStepGoal" to 10000,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            usersCollection.document(firebaseUser.uid).set(userDoc). await()

            return firebaseUser.uid  // Changed: return uid directly as String
        } catch (e: FirebaseAuthException) {
            // Handle specific Firebase Auth errors
            val errorMessage = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered"
                "ERROR_WEAK_PASSWORD" -> "Password is too weak. Use at least 6 characters"
                "ERROR_INVALID_EMAIL" -> "Invalid email format"
                else -> "Registration failed: ${e.message}"
            }
            throw Exception(errorMessage)
        } catch (e: Exception) {
            throw Exception("Registration failed: ${e.message}")
        }
    }

    override suspend fun updateUser(user: User) {
        val currentUser = auth. currentUser ?: throw Exception("No authenticated user")

        try {
            val userDoc = hashMapOf(
                "email" to user.email,
                "name" to user.name,
                "age" to user.age,
                "weight" to user.weight,
                "height" to user.height,
                "fitnessGoal" to user.fitnessGoal,
                "dailyStepGoal" to user.dailyStepGoal,
                "updatedAt" to System.currentTimeMillis()
            )

            usersCollection.document(currentUser.uid).update(userDoc as Map<String, Any>). await()
        } catch (e: Exception) {
            throw Exception("Failed to update user: ${e.message}")
        }
    }

    override suspend fun updateDailyStepGoal(userId: String, goal: Int) {  // Changed: Long to String
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            usersCollection.document(currentUser.uid)
                .update(
                    mapOf(
                        "dailyStepGoal" to goal,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to update goal: ${e.message}")
        }
    }

    override suspend fun authenticateUser(email: String, password: String): User? {
        return try {
            // Sign in with Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password). await()
            val firebaseUser = authResult.user ?: throw Exception("Authentication failed")

            // Get user document from Firestore
            val document = usersCollection. document(firebaseUser.uid). get().await()

            if (document.exists()) {
                document.toObject(User::class. java)?.copy(
                    id = firebaseUser.uid  // Changed: use uid directly as String
                )
            } else {
                throw Exception("User profile not found in database")
            }
        } catch (e: FirebaseAuthException) {
            // Propagate the actual Firebase error
            throw Exception("Login failed: ${e.message}")
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }
}