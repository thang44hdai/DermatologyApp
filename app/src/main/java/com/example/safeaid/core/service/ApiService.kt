package com.example.safeaid.core.service

import QuizCategoryResponse
import com.example.safeaid.core.request.ChatRequest
import com.example.safeaid.core.request.CreateReminderRequest
import com.example.safeaid.core.request.FCMTokenRequest
import com.example.safeaid.core.request.UpdateReminderStatusRequest
import com.example.safeaid.core.response.ListMedicineResponse
import com.example.safeaid.core.response.ListPharmacyResponse
import com.example.safeaid.core.request.GoogleLoginRequest
import com.example.safeaid.core.request.LoginRequest
import com.example.safeaid.core.request.RefreshTokenRequest
import com.example.safeaid.core.request.RegisterRequest
import com.example.safeaid.core.response.CategoryResponse
import com.example.safeaid.core.response.ChatResponse
import com.example.safeaid.core.response.ConversationResponse
import com.example.safeaid.core.response.CreateReminderResponse
import com.example.safeaid.core.response.DetailConversationResponse
import com.example.safeaid.core.response.DetectBoundaryResponse
import com.example.safeaid.core.response.HistoryResponse
import com.example.safeaid.core.response.LoginResponse
import com.example.safeaid.core.response.PharmacyDetailResponse
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.core.response.PredictResponse
import com.example.safeaid.core.response.RegisterResponse
import com.example.safeaid.core.response.UserResponse
import com.example.safeaid.core.response.ReminderCalendarResponse
import com.example.safeaid.core.response.ReminderDayDetailResponse
import com.example.safeaid.core.response.ReminderTabResponse
import com.example.safeaid.core.response.MorningExerciseChallengeResponse
import com.example.safeaid.core.response.CheckInResponse
import com.example.safeaid.core.response.FCMTokenResponse
import com.example.safeaid.core.response.RunningChallengeResponse
import com.example.safeaid.core.response.SaveRunningSessionResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    @POST("auth/google")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
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

    @POST("chat")
    suspend fun postChat(
        @Body request: ChatRequest
    ): Response<ChatResponse>

    @GET("chat/sessions")
    suspend fun getListConversation(
    ): Response<ConversationResponse>

    @GET("chat/sessions/{session_id}/messages")
    suspend fun getDetailChatBot(
        @Path("session_id") session: String,
        @Query("limit") limit: String? = null,
        @Query("offset") offset: String? = null,
    ): Response<DetailConversationResponse>

    @GET("reminders/calendar")
    suspend fun getReminderCalendar(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<ReminderCalendarResponse>

    @GET("reminders/calendar/{target_date}")
    suspend fun getReminderDayDetail(
        @Path("target_date") targetDate: String
    ): Response<ReminderDayDetailResponse>

    @POST("reminders/")
    suspend fun createReminder(
        @Body request: CreateReminderRequest
    ): Response<CreateReminderResponse>

    @GET("categories/")
    suspend fun getCategories(): Response<List<CategoryResponse>>

    @GET("reminders/")
    suspend fun getReminderTabs(): Response<ReminderTabResponse>

    @POST("reminders/{reminder_id}/toggle-taken")
    suspend fun updateReminderStatus(
        @Path("reminder_id") reminderId: String,
        @Body request: UpdateReminderStatusRequest
    ): Response<Any>

    @GET("challenges/morning-exercise")
    suspend fun getMorningExerciseChallenge(): Response<MorningExerciseChallengeResponse>

    @POST("challenges/morning-exercise/check-in")
    suspend fun checkInMorningExercise(): Response<CheckInResponse>

    @GET("challenges/running")
    suspend fun getRunningChallenge(): Response<RunningChallengeResponse>

    @POST("challenges/running/session")
    suspend fun saveRunningSession(
        @Query("steps") steps: Int,
        @Query("distance") distance: Double,
        @Query("duration_minutes") durationMinutes: Long,
        @Query("calories") calories: Int
    ): Response<SaveRunningSessionResponse>
    @POST("users/fcm-token")
    suspend fun registerFCMToken(
        @Body request: FCMTokenRequest
    ): Response<FCMTokenResponse>

    @DELETE("users/fcm-token")
    suspend fun deleteFCMToken(): Response<FCMTokenResponse>

    @POST("users/test-notification")
    suspend fun sendTestNotification(): Response<FCMTokenResponse>
}