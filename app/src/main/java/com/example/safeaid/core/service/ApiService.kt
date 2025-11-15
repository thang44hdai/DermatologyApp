package com.example.safeaid.core.service

import QuizCategoryResponse
import com.example.safeaid.core.response.ListMedicineResponse
import com.example.safeaid.core.response.ListPharmacyResponse
import com.example.safeaid.core.request.LoginRequest
import com.example.safeaid.core.request.RefreshTokenRequest
import com.example.safeaid.core.request.RegisterRequest
import com.example.safeaid.core.response.DetectBoundaryResponse
import com.example.safeaid.core.response.HistoryResponse
import com.example.safeaid.core.response.LoginResponse
import com.example.safeaid.core.response.PharmacyDetailResponse
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.core.response.PredictResponse
import com.example.safeaid.core.response.RegisterResponse
import com.example.safeaid.core.response.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

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


    @GET("pharmacies/nearby/search")
    suspend fun getPharmaciesNearBy(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("radius_km") radiusKm: String?,
        @Query("limit") limit: String?,
    ): Response<List<PharmacyResponse>>

    @GET("medicines/")
    suspend fun getMedicines(): Response<ListMedicineResponse>

    @GET("pharmacies/")
    suspend fun getPharmacies(): Response<ListPharmacyResponse>

    @GET("medicines/pharmacy/{pharmacy_id}/medicines")
    suspend fun getPharmacyMedicines(
        @Path("pharmacy_id") pharmacyId: String
    ): Response<PharmacyDetailResponse>

    @GET("prediction/history")
    suspend fun getHistoryList(
    ): Response<HistoryResponse>

    @GET("auth/me")
    suspend fun getUserInfo(): Response<UserResponse>

    @Multipart
    @POST("prediction/detect-boundary")
    suspend fun detectBoundary(
        @Part file: MultipartBody.Part
    ): Response<DetectBoundaryResponse>

}