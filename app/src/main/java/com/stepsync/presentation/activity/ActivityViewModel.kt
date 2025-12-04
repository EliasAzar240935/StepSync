package com.stepsync.presentation. activity

import android.content.SharedPreferences
import androidx.lifecycle. ViewModel
import androidx.lifecycle. viewModelScope
import com.stepsync.data.model.Activity
import com.stepsync. domain.repository.ActivityRepository
import com.stepsync.util. Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow. stateIn
import kotlinx. coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val userId = sharedPreferences.getString(Constants.KEY_USER_ID, "") ?: ""

    val activities: StateFlow<List<Activity>> = activityRepository
        .getRecentActivities(userId, 20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addActivity(activity: Activity) {
        viewModelScope.launch {
            activityRepository.insertActivity(activity)
        }
    }

    fun deleteActivity(activityId: Long) {
        viewModelScope.launch {
            activityRepository.deleteActivity(activityId)
        }
    }
}