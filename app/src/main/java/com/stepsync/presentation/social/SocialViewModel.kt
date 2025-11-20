package com.stepsync.presentation.social

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepsync.data.model.Friend
import com.stepsync.domain.repository.FriendRepository
import com.stepsync.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val userId = sharedPreferences.getLong(Constants.KEY_USER_ID, 0L)

    val friends: StateFlow<List<Friend>> = friendRepository
        .getAllFriends(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRequests: StateFlow<List<Friend>> = friendRepository
        .getPendingFriendRequests(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFriend(email: String, name: String) {
        viewModelScope.launch {
            friendRepository.addFriend(userId, email, name)
        }
    }

    fun removeFriend(friendId: Long) {
        viewModelScope.launch {
            friendRepository.removeFriend(userId, friendId)
        }
    }

    fun acceptFriendRequest(friendId: Long) {
        viewModelScope.launch {
            friendRepository.acceptFriendRequest(userId, friendId)
        }
    }
}
