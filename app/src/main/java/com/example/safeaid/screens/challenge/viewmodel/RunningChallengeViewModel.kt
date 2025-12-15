package com.example.safeaid.screens.challenge.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.data.RunningDataManager
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.ErrorResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunningChallengeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseViewModel<RunningChallengeState, RunningChallengeEvent>() {

    private val dataManager = RunningDataManager(context)

    fun loadChallengeData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val todayStats = dataManager.getTodayStats()
                val (weeklyDistance, weeklyGoal) = dataManager.getWeeklyStats()
                val (totalDistance, totalRuns, totalCalories) = dataManager.getTotalStats()

                updateState(
                    DataResult.Success(
                        RunningChallengeState.ChallengeData(
                            totalDistance = totalDistance,
                            totalRuns = totalRuns,
                            weeklyDistance = weeklyDistance,
                            weeklyGoalDistance = weeklyGoal,
                            todaySteps = todayStats.totalSteps,
                            todayDistance = todayStats.totalDistance,
                            todayCalories = todayStats.totalCalories
                        )
                    )
                )
            } catch (e: Exception) {
                updateState(
                    DataResult.Error(
                        ErrorResponse(
                            errorType = "ERROR",
                            message = "Không thể lấy dữ liệu"
                        )
                    )
                )
            }
        }
    }

    fun saveRunningSession(steps: Int, distance: Double, durationMinutes: Long, calories: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataManager.saveSession(steps, distance, durationMinutes, calories)
                updateState(DataResult.Success(RunningChallengeState.SaveSuccess))
            } catch (e: Exception) {
                updateState(
                    DataResult.Error(
                        ErrorResponse(
                            errorType = "ERROR",
                            message = "Không thể lưu hoạt động"
                        )
                    )
                )
            }
        }
    }

    fun getAchievements(): List<RunningDataManager.Achievement> {
        return dataManager.getAchievements()
    }

    fun getHistory(): List<RunningDataManager.RunningSession> {
        return dataManager.getAllSessions().sortedByDescending { it.timestamp }
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
