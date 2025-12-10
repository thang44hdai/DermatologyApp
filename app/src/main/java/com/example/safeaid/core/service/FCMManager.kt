package com.example.safeaid.core.service

import android.util.Log
import com.example.safeaid.core.request.FCMTokenRequest
import com.example.safeaid.pref.AppPreference
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMManager @Inject constructor(
    private val apiService: ApiService,
    private val appPreference: AppPreference
) {
    companion object {
        private const val TAG = "FCMManager"
    }

    /**
     * Lấy FCM token và đăng ký với backend
     */
    suspend fun initializeFCMToken(): Result<String> {
        return try {
            // Lấy token từ Firebase
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM Token: $token")

            // Đăng ký với backend
            val storedToken = appPreference.getFCMToken().firstOrNull()
            if (token != storedToken) {
                Log.d(TAG, "Token mới, đăng ký với backend...")
                registerToken(token)
            } else {
                // Re-register để đảm bảo backend có token
                Log.d(TAG, "Re-registering token với backend...")
                registerToken(token)
            }

            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lấy FCM token", e)
            Result.failure(e)
        }
    }

    /**
     * Đăng ký FCM token với backend
     */
    private suspend fun registerToken(token: String): Result<String> {
        return try {
            val request = FCMTokenRequest(token)
            val response = apiService.registerFCMToken(request)

            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Token đã đăng ký"
                Log.d(TAG, "✅ Đăng ký thành công: $message")

                // Lưu token vào local
                appPreference.saveFCMToken(token)

                Result.success(message)
            } else {
                val error = "Lỗi ${response.code()}: ${response.message()}"
                Log.e(TAG, "❌ Đăng ký thất bại: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception khi đăng ký token", e)
            Result.failure(e)
        }
    }

    /**
     * Gửi test notification
     */
    suspend fun sendTestNotification(): Result<String> {
        return try {
            val response = apiService.sendTestNotification()

            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Đã gửi thông báo test"
                Result.success(message)
            } else {
                Result.failure(Exception("Lỗi ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}