package com.example.safeaid.core.response

import com.google.gson.annotations.SerializedName

data class FCMTokenResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("fcm_token")
    val fcmToken: String? = null
)
