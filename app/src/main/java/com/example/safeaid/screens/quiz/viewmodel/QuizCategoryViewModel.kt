package com.example.safeaid.screens.quiz.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.service.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizCategoryViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<QuizCategoryState, QuizCategoryEvent>() {

    fun getQuizCategory() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = apiService.getCategoryQuiz()
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Log.i("hihihi", "${body}")
                }
            } else {
                Log.e("hihihi", "Error code: ${response.code()}")
            }
        }
    }

    override fun onTriggerEvent(event: QuizCategoryEvent) {
    }
}

sealed class QuizCategoryState {}
sealed class QuizCategoryEvent {}