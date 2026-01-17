# 📱 Hướng Dẫn Tích Hợp Thông Báo FCM - Ứng Dụng Nhắc Nhở Uống Thuốc

## 📋 Mục Lục

1. [Tổng Quan](#tổng-quan)
2. [Cài Đặt Firebase](#cài-đặt-firebase)
3. [Cấu Hình Ứng Dụng Android](#cấu-hình-ứng-dụng-android)
4. [Đăng Ký FCM Token](#đăng-ký-fcm-token)
5. [Nhận Và Hiển Thị Thông Báo](#nhận-và-hiển-thị-thông-báo)
6. [Tích Hợp Backend](#tích-hợp-backend)
7. [Kiểm Tra Và Debug](#kiểm-tra-và-debug)
8. [Xử Lý Lỗi Thường Gặp](#xử-lý-lỗi-thường-gặp)

---

## 📖 Tổng Quan

### Hệ Thống Thông Báo Hoạt Động Như Thế Nào?

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────────┐
│   Backend   │────▶│   Firebase   │────▶│   Android   │────▶│   Hiển Thị   │
│   Server    │     │Cloud Messaging│     │     App     │     │  Thông Báo   │
└─────────────┘     └──────────────┘     └─────────────┘     └──────────────┘
     1. Gửi              2. Chuyển           3. Nhận             4. Hiển thị
```

### Luồng Hoạt Động Chi Tiết:

1. **App khởi động** → Lấy FCM Token từ Firebase
2. **Đăng ký Token** → Gửi token lên Backend (API)
3. **Backend lưu Token** → Lưu vào database với thông tin user
4. **Scheduler chạy** → Kiểm tra reminder đến giờ → Gửi notification qua Firebase
5. **Firebase gửi** → Push notification đến thiết bị
6. **App nhận** → Service xử lý và hiển thị thông báo

---

## 🔥 Cài Đặt Firebase

### Bước 1: Tạo Project Firebase

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"** (Thêm dự án)
3. Đặt tên project: `dermatology-app` (hoặc tên bạn muốn)
4. Bỏ tích **Google Analytics** nếu không cần
5. Click **"Create project"** (Tạo dự án)

### Bước 2: Thêm Android App

1. Trong Firebase Console, click biểu tượng **Android** để thêm app
2. Điền thông tin:
   ```
   Android package name: com.example.dermatology
   App nickname: DermatologyApp (tùy chọn)
   Debug signing certificate SHA-1: (để trống)
   ```
3. Click **"Register app"** (Đăng ký ứng dụng)

### Bước 3: Download google-services.json

1. Sau khi đăng ký, click **"Download google-services.json"**
2. Copy file này vào thư mục:
   ```
   DermatologyApp/app/google-services.json
   ```
3. **LƯU Ý QUAN TRỌNG:** 
   - File này chứa thông tin bí mật
   - **KHÔNG** commit vào Git public
   - Thêm vào `.gitignore`:
     ```
     google-services.json
     ```

### Bước 4: Kích Hoạt Cloud Messaging

1. Trong Firebase Console, vào menu bên trái
2. Click **"Cloud Messaging"**
3. Đảm bảo **Cloud Messaging API** đã enabled
4. Nếu chưa, click **"Enable"**

---

## ⚙️ Cấu Hình Ứng Dụng Android

### Bước 1: Thêm Dependencies

**File: `build.gradle.kts` (Project level)**

```kotlin
plugins {
    // ... các plugin khác
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

**File: `app/build.gradle.kts`**

```kotlin
plugins {
    // ... các plugin khác
    id("com.google.gms.google-services")
}

dependencies {
    // ... các dependencies khác
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Coroutines cho Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}
```

### Bước 2: Sync Gradle

Trong Android Studio:
1. Click **"Sync Now"** ở banner trên
2. Hoặc: **File → Sync Project with Gradle Files**
3. Đợi sync hoàn tất (có thể mất vài phút)

### Bước 3: Cập Nhật AndroidManifest.xml

**File: `app/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Thêm permission cho notification (Android 13+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".App"
        ...>
        
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <!-- ... intent filters ... -->
        </activity>

        <!-- Đăng ký Firebase Messaging Service -->
        <service
            android:name=".core.service.MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
        
    </application>

</manifest>
```

---

## 🔑 Đăng Ký FCM Token

### Bước 1: Tạo Data Classes

**File: `core/request/FCMTokenRequest.kt`**

```kotlin
package com.example.safeaid.core.request

import com.google.gson.annotations.SerializedName

data class FCMTokenRequest(
    @SerializedName("fcm_token")
    val fcmToken: String
)
```

**File: `core/response/FCMTokenResponse.kt`**

```kotlin
package com.example.safeaid.core.response

import com.google.gson.annotations.SerializedName

data class FCMTokenResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("fcm_token")
    val fcmToken: String? = null
)
```

### Bước 2: Thêm API Endpoints

**File: `core/service/ApiService.kt`**

```kotlin
interface ApiService {
    // ... các endpoints khác ...
    
    // FCM Token Management
    @POST("users/fcm-token")
    suspend fun registerFCMToken(
        @Body request: FCMTokenRequest
    ): Response<FCMTokenResponse>

    @DELETE("users/fcm-token")
    suspend fun deleteFCMToken(): Response<FCMTokenResponse>

    @POST("users/test-notification")
    suspend fun sendTestNotification(): Response<FCMTokenResponse>
}
```

### Bước 3: Lưu Token Vào Local Storage

**File: `pref/AppPreference.kt`**

```kotlin
interface AppPreference {
    // ... các method khác ...
    
    fun getFCMToken(): Flow<String>
    suspend fun saveFCMToken(token: String)
    suspend fun clearFCMToken()
}
```

**File: `pref/AppPreferenceImpl.kt`**

```kotlin
class AppPreferenceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppPreference {
    
    // Key cho FCM token
    companion object {
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
    }
    
    override fun getFCMToken(): Flow<String> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { it[FCM_TOKEN] ?: "" }
    }
    
    override suspend fun saveFCMToken(token: String) {
        dataStore.edit { it[FCM_TOKEN] = token }
    }
    
    override suspend fun clearFCMToken() {
        dataStore.edit { it.remove(FCM_TOKEN) }
    }
}
```

### Bước 4: Tạo FCM Manager

**File: `core/service/FCMManager.kt`**

```kotlin
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
    suspend fun registerToken(token: String): Result<String> {
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
```

### Bước 5: Khởi Tạo FCM Trong MainActivity

**File: `MainActivity.kt`**

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var fcmManager: FCMManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ... setup UI ...
        
        // Khởi tạo FCM
        initializeFCM()
        
        // Request notification permission (Android 13+)
        requestNotificationPermission()
    }

    private fun initializeFCM() {
        lifecycleScope.launch {
            try {
                val result = fcmManager.initializeFCMToken()
                result.onSuccess { token ->
                    Log.d("MainActivity", "✅ FCM đã sẵn sàng")
                    Toast.makeText(this@MainActivity, "Đã đăng ký nhận thông báo", Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Log.e("MainActivity", "❌ Lỗi FCM", error)
                    Toast.makeText(this@MainActivity, "Lỗi đăng ký thông báo", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Exception FCM", e)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }
}
```

---

## 📩 Nhận Và Hiển Thị Thông Báo

### Bước 1: Tạo Notification Channel

**File: `App.kt`**

```kotlin
@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "medicine_reminder_channel",
                "Nhắc Nhở Uống Thuốc",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo nhắc nhở uống thuốc"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
```

### Bước 2: Tạo Firebase Messaging Service

**File: `core/service/MyFirebaseMessagingService.kt`**

```kotlin
package com.example.safeaid.core.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.dermatology.R
import com.example.safeaid.MainActivity
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
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
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
        
        // Đăng ký token mới với backend
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
```

---

## 🔗 Tích Hợp Backend

### API Endpoints Backend Cần Triển Khai

#### 1. Đăng Ký FCM Token

```
POST /api/v1/users/fcm-token
Content-Type: application/json
Authorization: Bearer {access_token}

Request Body:
{
  "fcm_token": "eXfG7y9K8H4vr2bQ..."
}

Response 200:
{
  "message": "FCM token đã được đăng ký thành công",
  "fcm_token": "eXfG7y9K8H4vr2bQ..."
}
```

#### 2. Xóa FCM Token

```
DELETE /api/v1/users/fcm-token
Authorization: Bearer {access_token}

Response 200:
{
  "message": "FCM token đã được xóa"
}
```

#### 3. Gửi Test Notification

```
POST /api/v1/users/test-notification
Authorization: Bearer {access_token}

Response 200:
{
  "message": "Đã gửi thông báo test thành công"
}
```

### Backend Schema Database

```sql
-- Thêm cột fcm_token vào bảng users
ALTER TABLE users ADD COLUMN fcm_token VARCHAR(255);

-- Index để tìm kiếm nhanh
CREATE INDEX idx_users_fcm_token ON users(fcm_token);
```

### Backend Code Mẫu (Python với Firebase Admin SDK)

```python
from firebase_admin import messaging

def send_medicine_reminder(fcm_token: str, reminder_data: dict):
    """
    Gửi thông báo nhắc nhở uống thuốc
    """
    message = messaging.Message(
        notification=messaging.Notification(
            title='💊 Nhắc Nhở Uống Thuốc',
            body=f'Đến giờ uống {reminder_data["medicine_name"]}!'
        ),
        data={
            'type': 'medicine_reminder',
            'reminder_id': str(reminder_data['id']),
            'medicine_name': reminder_data['medicine_name'],
            'dosage': reminder_data['dosage'],
            'unit': reminder_data['unit'],
            'meal_timing': reminder_data['meal_timing'],
            'notes': reminder_data.get('notes', '')
        },
        android=messaging.AndroidConfig(
            priority='high',
            notification=messaging.AndroidNotification(
                channel_id='medicine_reminder_channel',
                sound='default',
                priority='high'
            )
        ),
        token=fcm_token
    )
    
    try:
        response = messaging.send(message)
        print(f'✅ Đã gửi notification: {response}')
        return True
    except messaging.UnregisteredError:
        print(f'❌ Token không hợp lệ, xóa khỏi database')
        # Xóa token khỏi database
        return False
    except Exception as e:
        print(f'❌ Lỗi gửi notification: {e}')
        return False
```

---

## 🧪 Kiểm Tra Và Debug

### Test 1: Kiểm Tra FCM Token

**Mở Logcat trong Android Studio:**

```
Filter: FCMManager
```

**Logs mong đợi:**
```
D/FCMManager: FCM Token: eXfG7y9K8H4vr2bQ...
D/FCMManager: Re-registering token với backend...
D/FCMManager: ✅ Đăng ký thành công: Token đã đăng ký
```

### Test 2: Test Với Firebase Console

1. Vào [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. Vào **Cloud Messaging** trong menu
4. Click **"Send your first message"**
5. Điền:
   - **Notification title:** 🧪 Test
   - **Notification text:** Đây là test notification
6. Click **"Send test message"**
7. Paste FCM token từ logs
8. Click **"Test"**

**Kết quả mong đợi:**
- Notification xuất hiện trên thiết bị
- Có âm thanh và rung

### Test 3: Test Backend API

**Sử dụng PowerShell/Terminal:**

```powershell
# 1. Login để lấy access token
$response = Invoke-RestMethod `
    -Uri "http://192.168.1.36:8000/api/v1/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username":"testuser","password":"password123"}'

$token = $response.access_token

# 2. Gửi test notification
Invoke-RestMethod `
    -Uri "http://192.168.1.36:8000/api/v1/users/test-notification" `
    -Method POST `
    -Headers @{"Authorization"="Bearer $token"}
```

### Test 4: Kiểm Tra Notification Permission

```kotlin
// Thêm vào MainActivity hoặc debug screen
val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
} else {
    true // Android < 13 không cần runtime permission
}

Log.d("DEBUG", "Notification permission: $hasPermission")
Toast.makeText(this, "Permission: $hasPermission", Toast.LENGTH_LONG).show()
```

### Test 5: Test Local Notification

**Code test notification không qua Firebase:**

```kotlin
fun testLocalNotification(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    // Tạo channel (nếu chưa có)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "test_channel",
            "Test",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }
    
    // Hiển thị notification
    val notification = NotificationCompat.Builder(context, "test_channel")
        .setContentTitle("🧪 Test Local")
        .setContentText("Nếu thấy notification này, hệ thống hoạt động tốt!")
        .setSmallIcon(R.drawable.ic_medicine)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()
    
    notificationManager.notify(999, notification)
    
    Log.d("TEST", "✅ Local notification đã gửi!")
}
```

**Gọi hàm này từ button hoặc onCreate:**

```kotlin
binding.btnTestLocal.setOnClickListener {
    testLocalNotification(this)
}
```

---

## 🐛 Xử Lý Lỗi Thường Gặp

### Lỗi 1: "google-services.json is missing"

**Triệu chứng:**
```
Build failed: File google-services.json is missing
```

**Giải pháp:**
1. Download `google-services.json` từ Firebase Console
2. Copy vào thư mục `app/` (cùng cấp với `build.gradle.kts`)
3. Sync lại Gradle

---

### Lỗi 2: Không Nhận Được Notification

**Triệu chứng:**
- Backend báo "sent successfully"
- Thiết bị không thấy notification

**Debug steps:**

#### Bước 1: Kiểm tra Logcat

```
Filter: FCMService
```

**Nếu KHÔNG có log "📨 Nhận Thông Báo FCM":**
- Backend gửi sai token
- Token đã expire
- Payload format sai

**Nếu CÓ log nhưng không hiển thị:**
- Notification permission chưa cấp
- Do Not Disturb mode đang bật
- Channel importance quá thấp

#### Bước 2: Kiểm tra Permission

```kotlin
Settings → Apps → YourApp → Notifications → Phải BẬT
```

#### Bước 3: Kiểm tra Backend Payload

Backend phải gửi đúng format:

```json
{
  "notification": {
    "title": "💊 Nhắc Nhở Uống Thuốc",
    "body": "Đến giờ uống thuốc!"
  },
  "data": {
    "type": "medicine_reminder",
    "reminder_id": "123",
    "medicine_name": "Vitamin C",
    "dosage": "2",
    "unit": "viên",
    "meal_timing": "sau ăn",
    "notes": "Uống với nước ấm"
  },
  "token": "eXfG7y9K8H...",
  "android": {
    "priority": "high"
  }
}
```

**Lưu ý:**
- ✅ Phải có CẢ "notification" VÀ "data"
- ✅ "android.priority" = "high"
- ✅ Token chính xác từ app

---

### Lỗi 3: "Unresolved reference: FirebaseMessaging"

**Triệu chứng:**
```
Compile error: Unresolved reference 'FirebaseMessaging'
```

**Giải pháp:**
1. Kiểm tra `google-services.json` đã copy đúng chỗ
2. Sync Gradle lại
3. Clean project:
   ```
   Build → Clean Project
   Build → Rebuild Project
   ```
4. Invalidate Caches:
   ```
   File → Invalidate Caches / Restart...
   ```

---

### Lỗi 4: "Pages must fill the whole ViewPager2"

**Triệu chứng:**
```
IllegalStateException: Pages must fill the whole ViewPager2 (use match_parent)
```

**Giải pháp:**

Đảm bảo layout của page trong ViewPager2 có:

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <!-- Nội dung -->
</LinearLayout>
```

**KHÔNG DÙNG:**
- `wrap_content` cho width hoặc height
- Constraints không đầy đủ

---

### Lỗi 5: Token Tự Động Bị Xóa

**Triệu chứng:**
- Token đăng ký OK
- Sau vài ngày backend báo token không còn

**Nguyên nhân:**
- Token expire/invalid
- Backend nhận `UnregisteredError` từ Firebase

**Giải pháp:**

Backend phải handle error và xóa token invalid:

```python
try:
    messaging.send(message)
except messaging.UnregisteredError:
    # Token không còn hợp lệ, xóa khỏi database
    delete_fcm_token(user_id)
    print(f'Token của user {user_id} đã bị xóa')
```

App sẽ tự động đăng ký lại khi mở app lần sau.

---

### Lỗi 6: Notification Không Hiển Thị Khi App Ở Background

**Triệu chứng:**
- App mở: Notification hiển thị OK
- App đóng/background: Không thấy notification

**Nguyên nhân:**
Backend chỉ gửi "data" message, không có "notification"

**Giải pháp:**

Backend PHẢI gửi cả "notification" payload:

```python
message = messaging.Message(
    notification=messaging.Notification(  # ← QUAN TRỌNG!
        title='💊 Nhắc Nhở',
        body='Đến giờ uống thuốc!'
    ),
    data={
        # ... data fields
    },
    token=fcm_token
)
```

---

## 📊 Checklist Tổng Hợp

### ✅ Setup Firebase
- [ ] Tạo Firebase project
- [ ] Thêm Android app (package name đúng)
- [ ] Download `google-services.json`
- [ ] Copy file vào `app/`
- [ ] Enable Cloud Messaging API

### ✅ Cấu Hình Android
- [ ] Thêm Firebase dependencies
- [ ] Thêm Google Services plugin
- [ ] Sync Gradle thành công
- [ ] Thêm permission trong AndroidManifest
- [ ] Đăng ký FirebaseMessagingService

### ✅ Code Implementation
- [ ] Tạo FCMTokenRequest/Response
- [ ] Thêm API endpoints
- [ ] Tạo AppPreference methods
- [ ] Tạo FCMManager
- [ ] Initialize FCM trong MainActivity
- [ ] Request notification permission
- [ ] Tạo notification channel
- [ ] Implement MyFirebaseMessagingService

### ✅ Backend Integration
- [ ] Thêm cột fcm_token vào database
- [ ] Implement POST /users/fcm-token
- [ ] Implement DELETE /users/fcm-token
- [ ] Implement POST /users/test-notification
- [ ] Setup Firebase Admin SDK
- [ ] Implement scheduler gửi notification

### ✅ Testing
- [ ] Test local notification
- [ ] Test FCM token registration
- [ ] Test với Firebase Console
- [ ] Test backend API
- [ ] Test end-to-end flow
- [ ] Test khi app background/foreground
- [ ] Test trên nhiều thiết bị

---

## 🎯 Luồng Hoạt Động Hoàn Chỉnh

```
1. USER MỞ APP
   └─▶ MainActivity.onCreate()
       └─▶ initializeFCM()
           └─▶ FirebaseMessaging.getInstance().token
               └─▶ Lấy được token: "eXfG7y9K..."

2. ĐĂNG KÝ TOKEN
   └─▶ FCMManager.registerToken(token)
       └─▶ POST /api/v1/users/fcm-token
           └─▶ Backend lưu vào database
               └─▶ users.fcm_token = "eXfG7y9K..."

3. SCHEDULER CHẠY (BACKEND)
   └─▶ Mỗi phút kiểm tra reminders
       └─▶ Tìm reminder có time = now
           └─▶ Lấy user.fcm_token
               └─▶ Gọi Firebase Admin SDK
                   └─▶ messaging.send(message)

4. FIREBASE GỬI
   └─▶ Push notification đến thiết bị
       └─▶ Android nhận qua FCM

5. APP NHẬN (MyFirebaseMessagingService)
   └─▶ onMessageReceived(message)
       └─▶ Parse data payload
           └─▶ Build notification
               └─▶ NotificationManager.notify()

6. USER THẤY NOTIFICATION
   └─▶ 💊 Nhắc Nhở Uống Thuốc
       └─▶ Đến giờ uống Vitamin C!
           └─▶ - Liều lượng: 2 viên (sau ăn)
               └─▶ 💡 Uống với nước ấm
```

---

## 📱 Demo Screenshots

### 1. Notification Permission Request
```
┌─────────────────────────────────┐
│  Cho phép ứng dụng gửi          │
│  thông báo?                     │
│                                 │
│  ┌────────────┐ ┌────────────┐ │
│  │   Từ chối  │ │  Cho phép  │ │
│  └────────────┘ └────────────┘ │
└─────────────────────────────────┘
```

### 2. Notification Display
```
┌─────────────────────────────────┐
│ 💊 Nhắc Nhở Uống Thuốc    10:00│
│ Đến giờ uống Vitamin C!         │
│ - Liều lượng: 2 viên (sau ăn)  │
│ 💡 Uống với nước ấm             │
└─────────────────────────────────┘
```

### 3. Notification Channel Settings
```
Settings → Apps → YourApp → Notifications

┌─────────────────────────────────┐
│ ☑ Allow notifications           │
│                                 │
│ Categories:                     │
│  💊 Nhắc Nhở Uống Thuốc         │
│     Importance: High            │
│     ☑ Sound                     │
│     ☑ Vibrate                   │
└─────────────────────────────────┘
```

---

## 🔐 Bảo Mật

### Không Commit Các File Sau:

**File `.gitignore`:**

```gitignore
# Firebase
google-services.json
firebase-adminsdk-*.json

# Local config
local.properties
```

### Best Practices:

1. **Token Management:**
   - Luôn validate token trước khi gửi notification
   - Xóa token invalid khỏi database
   - Implement token refresh mechanism

2. **API Security:**
   - Luôn yêu cầu authentication (Bearer token)
   - Rate limit API endpoints
   - Validate input data

3. **Notification Content:**
   - Không gửi thông tin nhạy cảm qua notification
   - Encrypt data nếu cần thiết

---

## 📚 Tài Liệu Tham Khảo

- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [Android Notification Guide](https://developer.android.com/develop/ui/views/notifications)
- [Firebase Admin SDK (Python)](https://firebase.google.com/docs/admin/setup)
- [Firebase Admin SDK (Node.js)](https://firebase.google.com/docs/admin/setup#node.js)

---

## 🆘 Hỗ Trợ

### Khi Gặp Vấn Đề:

1. **Kiểm tra Logcat:**
   ```
   adb logcat | findstr "FCM"
   ```

2. **Xem Firebase Console:**
   - Cloud Messaging → Usage statistics
   - Kiểm tra số notification sent/delivered

3. **Debug Backend:**
   - Kiểm tra logs backend
   - Verify Firebase Admin SDK credentials
   - Test Firebase send API trực tiếp

4. **Test Riêng Từng Phần:**
   - Test local notification → OK? → FCM permission OK
   - Test Firebase Console → OK? → Token OK, Backend issue
   - Test Firebase Console → FAIL? → Token invalid

---

## ✅ Hoàn Thành!

Bạn đã tích hợp thành công hệ thống thông báo FCM! 🎉

**Next Steps:**
1. Test kỹ trên nhiều thiết bị
2. Monitor Firebase Console để xem delivery rates
3. Implement analytics để track notification engagement
4. Thêm notification actions (Mark as taken, Snooze, etc.)

**Chúc bạn thành công!** 🚀

