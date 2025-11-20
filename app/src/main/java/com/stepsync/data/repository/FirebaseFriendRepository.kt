package com.stepsync.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stepsync.data.model.Friend
import com.stepsync.domain.repository.FriendRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firebase implementation of FriendRepository
 */
class FirebaseFriendRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : FriendRepository {

    private val friendsCollection = firestore.collection("friends")
    private val usersCollection = firestore.collection("users")

    override fun getAllFriends(userId: Long): Flow<List<Friend>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        
        val registration = friendsCollection
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("status", "accepted")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val friends = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Friend::class.java)?.copy(
                        id = document.id.hashCode().toLong(),
                        userId = userId
                    )
                } ?: emptyList()
                
                trySend(friends)
            }
        
        awaitClose { registration.remove() }
    }

    override fun getPendingFriendRequests(userId: Long): Flow<List<Friend>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        
        val registration = friendsCollection
            .whereEqualTo("friendUserId", currentUser.uid)
            .whereEqualTo("status", "pending")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val requests = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Friend::class.java)?.copy(
                        id = document.id.hashCode().toLong(),
                        userId = userId
                    )
                } ?: emptyList()
                
                trySend(requests)
            }
        
        awaitClose { registration.remove() }
    }

    override suspend fun addFriend(userId: Long, friendEmail: String, friendName: String): Long {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")
        
        try {
            // Look up friend by email
            val friendQuery = usersCollection
                .whereEqualTo("email", friendEmail)
                .limit(1)
                .get()
                .await()
            
            if (friendQuery.isEmpty) {
                throw Exception("User with email $friendEmail not found")
            }
            
            val friendDoc = friendQuery.documents[0]
            val friendUserId = friendDoc.id
            
            // Check if friendship already exists
            val existingQuery = friendsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("friendUserId", friendUserId)
                .limit(1)
                .get()
                .await()
            
            if (!existingQuery.isEmpty) {
                throw Exception("Friend request already exists")
            }
            
            val friendData = hashMapOf(
                "userId" to currentUser.uid,
                "friendUserId" to friendUserId,
                "friendId" to friendUserId.hashCode().toLong(),
                "friendName" to friendName,
                "friendEmail" to friendEmail,
                "status" to "pending",
                "createdAt" to System.currentTimeMillis()
            )
            
            val documentRef = friendsCollection.add(friendData).await()
            return documentRef.id.hashCode().toLong()
        } catch (e: Exception) {
            throw Exception("Failed to add friend: ${e.message}")
        }
    }

    override suspend fun acceptFriendRequest(userId: Long, friendId: Long) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")
        
        try {
            // Find the friend request
            val querySnapshot = friendsCollection
                .whereEqualTo("friendUserId", currentUser.uid)
                .whereEqualTo("status", "pending")
                .get()
                .await()
            
            val document = querySnapshot.documents.find { 
                (it.getLong("friendId") ?: 0L) == friendId 
            } ?: throw Exception("Friend request not found")
            
            friendsCollection.document(document.id)
                .update(
                    mapOf(
                        "status" to "accepted",
                        "acceptedAt" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to accept friend request: ${e.message}")
        }
    }

    override suspend fun removeFriend(userId: Long, friendId: Long) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")
        
        try {
            // Find and delete the friendship
            val querySnapshot = friendsCollection
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()
            
            val document = querySnapshot.documents.find { 
                (it.getLong("friendId") ?: 0L) == friendId 
            } ?: throw Exception("Friendship not found")
            
            friendsCollection.document(document.id).delete().await()
        } catch (e: Exception) {
            throw Exception("Failed to remove friend: ${e.message}")
        }
    }

    override suspend fun getFriendsCount(userId: Long): Int {
        val currentUser = auth.currentUser ?: return 0
        
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
