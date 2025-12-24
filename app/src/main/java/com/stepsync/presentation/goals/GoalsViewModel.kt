package com.stepsync.presentation.goals

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com. google.firebase.auth.FirebaseAuth
import com.stepsync.data.model.Goal
import com.stepsync. data.model.GoalType
import com.stepsync. domain.repository.GoalRepository
import dagger.hilt.android. lifecycle.HiltViewModel
import kotlinx.coroutines.flow. MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val userId:  String
        get() = auth.currentUser?. uid ?: ""

    private val _activeGoals = MutableStateFlow<List<Goal>>(emptyList())
    val activeGoals: StateFlow<List<Goal>> = _activeGoals.asStateFlow()

    private val _completedGoals = MutableStateFlow<List<Goal>>(emptyList())
    val completedGoals: StateFlow<List<Goal>> = _completedGoals.asStateFlow()

    private val _uiState = MutableStateFlow<GoalUiState>(GoalUiState. Idle)
    val uiState: StateFlow<GoalUiState> = _uiState. asStateFlow()

    init {
        Log.d("GoalsViewModel", "Initializing with userId: $userId")
        loadGoals()
    }

    private fun loadGoals() {
        if (userId.isEmpty()) {
            Log.e("GoalsViewModel", "User not authenticated!")
            _uiState.value = GoalUiState.Error("User not authenticated")
            return
        }

        viewModelScope.launch {
            goalRepository.getActiveGoals(userId)
                .catch { e ->
                    Log.e("GoalsViewModel", "Error loading active goals", e)
                    _uiState.value = GoalUiState.Error("Failed to load goals:  ${e.message}")
                }
                .collect { goals ->
                    Log.d("GoalsViewModel", "Active goals loaded: ${goals.size}")
                    _activeGoals.value = goals
                }
        }

        viewModelScope.launch {
            goalRepository.getCompletedGoals(userId)
                .catch { e ->
                    Log.e("GoalsViewModel", "Error loading completed goals", e)
                }
                .collect { goals ->
                    Log.d("GoalsViewModel", "Completed goals loaded:  ${goals.size}")
                    _completedGoals.value = goals
                }
        }
    }

    fun createGoal(
        title: String,
        description: String,
        targetSteps: Int,
        goalType: GoalType
    ) {
        if (userId.isEmpty()) {
            _uiState.value = GoalUiState.Error("User not authenticated")
            return
        }

        if (title.isBlank()) {
            _uiState.value = GoalUiState.Error("Goal title cannot be empty")
            return
        }

        if (targetSteps <= 0) {
            _uiState.value = GoalUiState.Error("Target steps must be greater than 0")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = GoalUiState.Loading

                val now = System.currentTimeMillis()
                val (startDate, endDate) = calculateGoalDates(goalType)

                val goal = Goal(
                    userId = userId,
                    title = title,
                    description = description,
                    targetSteps = targetSteps,
                    goalType = goalType,
                    startDate = startDate,
                    endDate = endDate,
                    createdAt = now
                )

                Log.d("GoalsViewModel", "Creating goal:  $goal")
                goalRepository. createGoal(goal)
                _uiState.value = GoalUiState.Success("Goal created successfully!")
            } catch (e: Exception) {
                Log.e("GoalsViewModel", "Error creating goal", e)
                _uiState.value = GoalUiState.Error(e.message ?: "Failed to create goal")
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = GoalUiState.Loading
                goalRepository.deleteGoal(goalId)
                _uiState.value = GoalUiState.Success("Goal deleted")
            } catch (e: Exception) {
                Log.e("GoalsViewModel", "Error deleting goal", e)
                _uiState.value = GoalUiState.Error(e.message ?: "Failed to delete goal")
            }
        }
    }

    fun markGoalAsCompleted(goalId: String) {
        viewModelScope.launch {
            try {
                goalRepository.markGoalAsCompleted(goalId)
                _uiState.value = GoalUiState.Success("Goal completed!  🎉")
            } catch (e: Exception) {
                Log.e("GoalsViewModel", "Error completing goal", e)
                _uiState.value = GoalUiState.Error(e.message ?: "Failed to complete goal")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = GoalUiState.Idle
    }

    private fun calculateGoalDates(goalType: GoalType): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val startDate = calendar.timeInMillis

        when (goalType) {
            GoalType.DAILY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
            }
            GoalType. WEEKLY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 7)
                calendar. set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar. MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
            }
            GoalType.MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar. set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
            }
            GoalType.CUSTOM -> {
                calendar.add(Calendar.DAY_OF_YEAR, 30)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar. set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
            }
        }

        return Pair(startDate, calendar.timeInMillis)
    }
}