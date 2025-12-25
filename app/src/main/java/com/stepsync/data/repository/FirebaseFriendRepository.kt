package com.stepsync.data. repository

import com.google.firebase.auth.FirebaseAuth
import com. google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stepsync.data.model.Friend
import com.stepsync. domain.repository.FriendRepository
import com.stepsync.util.FriendCodeGenerator
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
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
                    document. toObject(Friend::class.java)?. copy(
                        id = document.id. hashCode().toLong(),
                        userId = currentUser.uid
                    )
                } ?:  emptyList()

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
            . whereEqualTo("friendUserId", currentUser.uid)
            .whereEqualTo("status", "pending")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val friends = snapshot?.documents?.mapNotNull { document ->
                    document. toObject(Friend::class.java)?.copy(
                        id = document.id.hashCode().toLong()
                    )
                } ?: emptyList()

                trySend(friends)
            }

        awaitClose { registration. remove() }
    }

    override suspend fun addFriendByCode(userId: String, friendCode: String) {
        val currentUser = auth.currentUser ?:  throw Exception("No authenticated user")

        try {
            // Validate and format friend code
            val formattedCode = FriendCodeGenerator.formatFriendCode(friendCode)

            if (!FriendCodeGenerator.isValidFriendCode(formattedCode)) {
                throw Exception("Invalid friend code format.  Use:  STEP-XXXXXX")
            }

            // Find user by friend code
            val userQuery = usersCollection
                .whereEqualTo("friendCode", formattedCode)
                .limit(1)
                .get()
                .await()

            if (userQuery.isEmpty) {
                throw Exception("No user found with code: $formattedCode")
            }

            val friendDoc = userQuery.documents[0]
            val friendUserId = friendDoc.id
            val friendName = friendDoc.getString("name") ?: "Unknown"
            val friendEmail = friendDoc.getString("email") ?: ""

            // Check if trying to add yourself
            if (friendUserId == currentUser.uid) {
                throw Exception("You cannot add yourself as a friend!")
            }

            // Check if already friends or request exists
            val existingFriend = friendsCollection
                . whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("friendUserId", friendUserId)
                .limit(1)
                .get()
                .await()

            if (! existingFriend.isEmpty) {
                val status = existingFriend.documents[0].getString("status")
                when (status) {
                    "accepted" -> throw Exception("You are already friends with $friendName")
                    "pending" -> throw Exception("Friend request already sent to $friendName")
                }
            }

            // Check if they already sent you a request
            val reverseFriend = friendsCollection
                .whereEqualTo("userId", friendUserId)
                .whereEqualTo("friendUserId", currentUser.uid)
                .whereEqualTo("status", "pending")
                .limit(1)
                .get()
                .await()

            if (!reverseFriend.isEmpty) {
                throw Exception("$friendName has already sent you a friend request!  Check your Requests tab.")
            }

            // Create friend request
            val friendData = hashMapOf(
                "userId" to currentUser.uid,
                "friendUserId" to friendUserId,
                "friendName" to friendName,
                "friendEmail" to friendEmail,
                "status" to "pending",
                "createdAt" to System.currentTimeMillis()
            )

            friendsCollection.add(friendData).await()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun acceptFriendRequest(friendId:  Long) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val querySnapshot = friendsCollection
                . whereEqualTo("friendUserId", currentUser.uid)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            val document = querySnapshot.documents.find {
                it.id. hashCode().toLong() == friendId
            } ?: throw Exception("Friend request not found")

            // Update the original request to accepted
            friendsCollection.document(document.id)
                .update("status", "accepted")
                .await()

            // Create the reverse friendship
            val requesterId = document.getString("userId") ?: ""
            val requesterDoc = usersCollection. document(requesterId).get().await()
            val requesterName = requesterDoc.getString("name") ?: ""
            val requesterEmail = requesterDoc.getString("email") ?: ""

            val reverseFriendData = hashMapOf(
                "userId" to currentUser. uid,
                "friendUserId" to requesterId,
                "friendName" to requesterName,
                "friendEmail" to requesterEmail,
                "status" to "accepted",
                "createdAt" to System.currentTimeMillis()
            )

            friendsCollection.add(reverseFriendData).await()
        } catch (e:  Exception) {
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

            val document = querySnapshot.documents. find {
                it.id. hashCode().toLong() == friendId
            } ?: throw Exception("Friend not found")

            val friendUserId = document.getString("friendUserId") ?: ""

            // Delete the friendship document
            friendsCollection. document(document.id).delete().await()

            // Also delete the reverse friendship if it exists
            val reverseFriendship = friendsCollection
                .whereEqualTo("userId", friendUserId)
                .whereEqualTo("friendUserId", currentUser.uid)
                .get()
                .await()

            reverseFriendship.documents.forEach { doc ->
                friendsCollection.document(doc.id).delete().await()
            }
        } catch (e: Exception) {
            throw Exception("Failed to remove friend: ${e.message}")
        }
    }

    override suspend fun getFriendsCount(userId: String): Int {
        val currentUser = auth.currentUser ?: return 0

        return try {
            val querySnapshot = friendsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            querySnapshot. size()
        } catch (e: Exception) {
            0
        }
    }
}