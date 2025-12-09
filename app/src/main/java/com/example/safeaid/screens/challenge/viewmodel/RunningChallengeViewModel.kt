package com.example.safeaid.screens.challenge.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.core.utils.ApiCaller
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunningChallengeViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<RunningChallengeState, RunningChallengeEvent>() {

    fun loadChallengeData() {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = { apiService.getRunningChallenge() },
                callback = { result ->
                    result.doIfSuccess { data ->
                        updateState(DataResult.Success(
                            RunningChallengeState.ChallengeData(
                                totalDistance = data.totalDistance ?: 0.0,
                                totalRuns = data.totalRuns ?: 0,
                                weeklyDistance = data.weeklyDistance ?: 0.0,
                                weeklyGoalDistance = data.weeklyGoalDistance ?: 10.0,
                                todaySteps = data.todaySteps ?: 0,
                                todayDistance = data.todayDistance ?: 0.0,
                                todayCalories = data.todayCalories ?: 0
                            )
                        ))
                    }
                    result.doIfFailure { error ->
                        // Use mock data if API fails
                        updateState(DataResult.Success(
                            RunningChallengeState.ChallengeData(
                                totalDistance = 0.0,
                                totalRuns = 0,
                                weeklyDistance = 0.0,
                                weeklyGoalDistance = 10.0,
                                todaySteps = 0,
                                todayDistance = 0.0,
                                todayCalories = 0
                            )
                        ))
                    }
                }
            )
        }
    }

    fun saveRunningSession(steps: Int, distance: Double, durationMinutes: Long, calories: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = { 
                    apiService.saveRunningSession(
                        steps = steps,
                        distance = distance,
                        durationMinutes = durationMinutes,
                        calories = calories
                    )
                },
                callback = { result ->
                    result.doIfSuccess {
                        updateState(DataResult.Success(RunningChallengeState.SaveSuccess))
                    }
                    result.doIfFailure { error ->
                    }
                }
            )
        }
    }

    override fun onTriggerEvent(event: RunningChallengeEvent) {
    }
}

sealed class RunningChallengeState {
    data class ChallengeData(
        val totalDistance: Double,
        val totalRuns: Int,
        val weeklyDistance: Double,
        val weeklyGoalDistance: Double,
        val todaySteps: Int,
        val todayDistance: Double,
        val todayCalories: Int
    ) : RunningChallengeState()
    
    object SaveSuccess : RunningChallengeState()
}

sealed class RunningChallengeEvent
