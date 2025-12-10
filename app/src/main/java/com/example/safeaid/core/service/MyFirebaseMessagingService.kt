package com.example.safeaid.core.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.dermatology.R
import com.example.safeaid.MainActivity
import com.example.safeaid.core.request.FCMTokenRequest
import com.example.safeaid.pref.AppPreference
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var appPreference: AppPreference

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID = "medicine_reminder_channel"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "=== 📨 Nhận Thông Báo FCM ===")
        Log.d(TAG, "Từ: ${message.from}")
        Log.d(TAG, "Message ID: ${message.messageId}")
        Log.d(TAG, "Notification: ${message.notification}")
        Log.d(TAG, "Data: ${message.data}")

        // Lấy thông tin từ data payload
        val medicineName = message.data["medicine_name"]
        val dosage = message.data["dosage"]
        val unit = message.data["unit"]
        val mealTiming = message.data["meal_timing"]
        val notes = message.data["notes"]
        val reminderId = message.data["reminder_id"] ?: "0"

        // Xây dựng nội dung thông báo
        val title = message.notification?.title ?: "💊 Nhắc Nhở Uống Thuốc"
        val body = buildNotificationBody(medicineName, dosage, unit, mealTiming, notes)

        // Hiển thị thông báo
        try {
            showNotification(title, body, reminderId)
            Log.d(TAG, "✅ Đã hiển thị thông báo!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Lỗi hiển thị thông báo", e)
        }
    }

    private fun buildNotificationBody(
        medicineName: String?,
        dosage: String?,
        unit: String?,
        mealTiming: String?,
        notes: String?
    ): String {
        val parts = mutableListOf<String>()

        if (medicineName != null) {
            parts.add("Đến giờ uống $medicineName!")

            if (dosage != null && unit != null) {
                val timing = if (mealTiming != null) " ($mealTiming)" else ""
                parts.add("- Liều lượng: $dosage $unit$timing")
            }

            if (notes != null) {
                parts.add("💡 $notes")
            }
        } else {
            parts.add("Đến giờ uống thuốc của bạn!")
        }

        return parts.joinToString("\n")
    }

    private fun showNotification(title: String, body: String, reminderId: String) {
        Log.d(TAG, "Hiển thị thông báo - ID: $reminderId")

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent để mở app khi click vào notification
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("reminder_id", reminderId)
            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSmallIcon(R.drawable.ic_medicine)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val notificationId = reminderId.toIntOrNull() ?: System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)

        Log.d(TAG, "✅ Notification đã gửi với ID: $notificationId")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔄 FCM Token mới: $token")

        serviceScope.launch {
            try {
                appPreference.saveFCMToken(token)
                registerTokenWithBackend(token)
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi lưu token mới", e)
            }
        }
    }

    private suspend fun registerTokenWithBackend(token: String) {
        try {
            val request = FCMTokenRequest(token)
            val response = apiService.registerFCMToken(request)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Token mới đã đăng ký với backend")
            } else {
                Log.e(TAG, "❌ Lỗi đăng ký token: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception khi đăng ký token", e)
        }
    }
}