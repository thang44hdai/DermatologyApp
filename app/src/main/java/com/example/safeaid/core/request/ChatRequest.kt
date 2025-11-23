package com.example.safeaid.core.request


import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("message")
    var message: String? = null,
    @SerializedName("session_id")
    var sessionId: String? = null
)