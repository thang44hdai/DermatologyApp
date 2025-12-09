package com.example.safeaid.core.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInResponse(
    @SerialName("success")
    var success: Boolean?,
    @SerialName("message")
    var message: String?,
    @SerialName("new_streak")
    var newStreak: Int?
)
