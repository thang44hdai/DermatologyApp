package com.example.safeaid.core.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaveRunningSessionResponse(
    @SerialName("success")
    var success: Boolean?,
    @SerialName("message")
    var message: String?
)
