package com.stepsync.presentation.home

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepsync.data.model.StepRecord
import com.stepsync.data.model.User
import com.stepsync.domain.repository.StepRecordRepository
import com.stepsync.domain.repository.UserRepository
import com.stepsync.util.Constants
import com.stepsync.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home screen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val stepRecordRepository: StepRecordRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val userId = sharedPreferences.getLong(Constants.KEY_USER_ID, 0L)

    val currentUser: StateFlow<User?> = userRepository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todaySteps: StateFlow<StepRecord?> = flow {
        val today = DateUtils.getCurrentDate()
        emit(stepRecordRepository.getStepRecordByDate(userId, today))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentSteps: StateFlow<List<StepRecord>> = stepRecordRepository
        .getRecentStepRecords(userId, 7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshSteps() {
        viewModelScope.launch {
            val today = DateUtils.getCurrentDate()
            stepRecordRepository.getStepRecordByDate(userId, today)
        }
    }
}
