package com.example.safeaid.core.service

import QuizCategoryResponse
import com.example.safeaid.core.request.LoginRequest
import com.example.safeaid.core.request.RefreshTokenRequest
import com.example.safeaid.core.request.RegisterRequest
import com.example.safeaid.core.response.LoginResponse
import com.example.safeaid.core.response.PredictResponse
import com.example.safeaid.core.response.RegisterResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @GET("/api/quiz-categories/with-quizzes")
    suspend fun getCategoryQuiz(): Response<QuizCategoryResponse>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("auth/test-token")
    suspend fun verifyToken(
    ): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<LoginResponse>

    @Multipart
    @POST("prediction/predict")
    suspend fun predictImage(
        @Part file: MultipartBody.Part
    ): Response<PredictResponse>

}