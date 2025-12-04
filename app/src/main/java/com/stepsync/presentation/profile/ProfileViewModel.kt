package com.stepsync.presentation.profile

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepsync.data.model.User
import com. stepsync.domain.repository. AchievementRepository
import com. stepsync.domain.repository. UserRepository
import com.stepsync.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow. MutableStateFlow
import kotlinx. coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val achievementRepository: AchievementRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val userId = sharedPreferences.getString(Constants.KEY_USER_ID, "") ?: ""

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _achievementCount = MutableStateFlow(0)
    val achievementCount: StateFlow<Int> = _achievementCount.asStateFlow()

    init {
        loadUserProfile()
        loadAchievementCount()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userRepository.getCurrentUser(). collect { user ->
                _user.value = user
            }
        }
    }

    private fun loadAchievementCount() {
        viewModelScope.launch {
            val count = achievementRepository.getAchievementsCount(userId)
            _achievementCount.value = count
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            userRepository.updateUser(user)
        }
    }

    fun logout() {
        viewModelScope. launch {
            // Sign out from Firebase Auth
            userRepository.logout()
            // Clear local session
            sharedPreferences.edit().apply {
                clear()
                apply()
            }
        }
    }
}