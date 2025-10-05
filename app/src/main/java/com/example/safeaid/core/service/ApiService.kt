package com.example.safeaid.core.service

import QuizCategoryResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("/api/quiz-categories/with-quizzes")
    suspend fun getCategoryQuiz(): Response<QuizCategoryResponse>
}