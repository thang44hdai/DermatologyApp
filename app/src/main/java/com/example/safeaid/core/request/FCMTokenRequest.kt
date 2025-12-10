package com.example.safeaid.core.request

import com.google.gson.annotations.SerializedName

data class FCMTokenRequest(
    @SerializedName("fcm_token")
    val fcmToken: String
)
