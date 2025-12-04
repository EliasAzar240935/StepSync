package com.stepsync.data.repository

import com.google. firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stepsync.data.model.Friend
import com.stepsync.domain.repository.FriendRepository
import kotlinx.coroutines. channels.awaitClose
import kotlinx.coroutines.flow. Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks. await
import javax.inject. Inject

/**
 * Firebase implementation of FriendRepository
 */
class FirebaseFriendRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : FriendRepository {

    private val friendsCollection = firestore.collection("friends")
    private val usersCollection = firestore.collection("users")

    override fun getAllFriends(userId: String): Flow<List<Friend>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = friendsCollection
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("status", "accepted")
            .orderBy("createdAt", Query. Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val friends = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Friend::class.java)?.copy(
                        id = document.id.hashCode().toLong(),
                        userId = currentUser.uid
                    )
                } ?: emptyList()

                trySend(friends)
            }

        awaitClose { registration.remove() }
    }

    override fun getPendingRequests(userId: String): Flow<List<Friend>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = friendsCollection
            .whereEqualTo("friendUserId", currentUser.uid)
            .whereEqualTo("status", "pending")
            .orderBy("createdAt", Query. Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val friends = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Friend::class.java)?.copy(
                        id = document.id.hashCode().toLong()
                    )
                } ?: emptyList()

                trySend(friends)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun addFriend(userId: String, friendEmail: String) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            // Find user by email
            val userQuery = usersCollection
                .whereEqualTo("email", friendEmail)
                .limit(1)
                .get()
                . await()

            if (userQuery.isEmpty) {
                throw Exception("User not found with email: $friendEmail")
            }

            val friendDoc = userQuery.documents[0]
            val friendUserId = friendDoc.id
            val friendName = friendDoc.getString("name") ?: ""

            // Check if already friends
            val existingFriend = friendsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("friendUserId", friendUserId)
                .limit(1)
                .get()
                .await()

            if (! existingFriend.isEmpty) {
                throw Exception("Already friends or request pending")
            }

            val friendData = hashMapOf(
                "userId" to currentUser. uid,
                "friendUserId" to friendUserId,
                "friendName" to friendName,
                "friendEmail" to friendEmail,
                "status" to "pending",
                "createdAt" to System.currentTimeMillis()
            )

            friendsCollection.add(friendData).await()
        } catch (e: Exception) {
            throw Exception("Failed to add friend: ${e.message}")
        }
    }

    override suspend fun acceptFriendRequest(friendId: Long) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val querySnapshot = friendsCollection
                .whereEqualTo("friendUserId", currentUser.uid)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            val document = querySnapshot. documents.find {
                it.id.hashCode().toLong() == friendId
            } ?: throw Exception("Friend request not found")

            friendsCollection.document(document.id)
                .update("status", "accepted")
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to accept friend request: ${e.message}")
        }
    }

    override suspend fun removeFriend(friendId: Long) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val querySnapshot = friendsCollection
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val document = querySnapshot. documents.find {
                it.id.hashCode().toLong() == friendId
            } ?: throw Exception("Friend not found")

            friendsCollection.document(document.id). delete().await()
        } catch (e: Exception) {
            throw Exception("Failed to remove friend: ${e.message}")
        }
    }

    override suspend fun getFriendsCount(userId: String): Int {
        val currentUser = auth. currentUser ?: return 0

        return try {
            val querySnapshot = friendsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            querySnapshot.size()
        } catch (e: Exception) {
            0
        }
    }
}