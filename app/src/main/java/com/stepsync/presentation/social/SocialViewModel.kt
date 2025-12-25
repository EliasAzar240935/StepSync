package com.stepsync.presentation.social

import android.content.SharedPreferences
import androidx.lifecycle. ViewModel
import androidx.lifecycle. viewModelScope
import com.stepsync.data.model.Friend
import com.stepsync. data.model.User
import com.stepsync.domain.repository.FriendRepository
import com.stepsync.domain.repository.UserRepository
import com.stepsync.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx. coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.stepsync.data.model.LeaderboardEntry
import com.stepsync.domain.repository.ChallengeRepository
import javax.inject.Inject

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val challengeRepository: ChallengeRepository,
    private val userRepository:  UserRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val userId = sharedPreferences.getString(Constants.KEY_USER_ID, "") ?: ""

    // Current user flow
    val currentUser: StateFlow<User? > = userRepository
        .getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val friends: StateFlow<List<Friend>> = friendRepository
        .getAllFriends(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRequests: StateFlow<List<Friend>> = friendRepository
        .getPendingRequests(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalLeaderboard: StateFlow<List<LeaderboardEntry>> = challengeRepository
        .getGlobalLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun addFriendByCode(friendCode: String) {
        viewModelScope.launch {
            try {
                friendRepository.addFriendByCode(userId, friendCode)
                _message.value = "Friend request sent!"
            } catch (e: Exception) {
                _message.value = e.message ?: "Failed to send friend request"
            }
        }
    }

    fun removeFriend(friendId: Long) {
        viewModelScope. launch {
            try {
                friendRepository.removeFriend(friendId)
                _message.value = "Friend removed"
            } catch (e: Exception) {
                _message.value = e.message ?: "Failed to remove friend"
            }
        }
    }

    fun acceptFriendRequest(friendId: Long) {
        viewModelScope.launch {
            try {
                friendRepository.acceptFriendRequest(friendId)
                _message.value = "Friend request accepted!"
            } catch (e: Exception) {
                _message.value = e.message ?:  "Failed to accept request"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}