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
class MorningExerciseViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<MorningExerciseState, MorningExerciseEvent>() {

    fun loadChallengeData() {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = { apiService.getMorningExerciseChallenge() },
                callback = { result ->
                    result.doIfSuccess { data ->
                        updateState(DataResult.Success(
                            MorningExerciseState.ChallengeData(
                                streak = data.streak ?: 0,
                                totalDays = data.totalDays ?: 0,
                                thisWeekCount = data.thisWeekCount ?: 0,
                                checkedInToday = data.checkedInToday ?: false,
                                motivationalQuote = getMotivationalQuote(data.streak ?: 0)
                            )
                        ))
                    }
                    result.doIfFailure { error ->
                        // Use mock data if API fails
                        updateState(DataResult.Success(
                            MorningExerciseState.ChallengeData(
                                streak = 0,
                                totalDays = 0,
                                thisWeekCount = 0,
                                checkedInToday = false,
                                motivationalQuote = "Hãy bắt đầu hành trình của bạn ngay hôm nay!"
                            )
                        ))
                    }
                }
            )
        }
    }

    fun checkIn() {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = { apiService.checkInMorningExercise() },
                callback = { result ->
                    result.doIfSuccess { data ->
                        updateState(DataResult.Success(
                            MorningExerciseState.CheckInSuccess(
                                newStreak = data.newStreak ?: 1
                            )
                        ))
                    }
                    result.doIfFailure { error ->
                    }
                }
            )
        }
    }

    private fun getMotivationalQuote(streak: Int): String {
        return when {
            streak == 0 -> "Hãy bắt đầu hành trình của bạn ngay hôm nay! 💪"
            streak < 7 -> "Bạn đang làm rất tốt! Hãy tiếp tục phát huy! 🔥"
            streak < 30 -> "Xuất sắc! Bạn đã tạo thói quen tuyệt vời! ⭐"
            streak < 100 -> "Không thể tin được! Bạn là nguồn cảm hứng! 🏆"
            else -> "Huyền thoại! Bạn đã chứng minh sức mạnh của ý chí! 👑"
        }
    }

    override fun onTriggerEvent(event: MorningExerciseEvent) {
    }
}

sealed class MorningExerciseState {
    data class ChallengeData(
        val streak: Int,
        val totalDays: Int,
        val thisWeekCount: Int,
        val checkedInToday: Boolean,
        val motivationalQuote: String
    ) : MorningExerciseState()
    
    data class CheckInSuccess(
        val newStreak: Int
    ) : MorningExerciseState()
}

sealed class MorningExerciseEvent
