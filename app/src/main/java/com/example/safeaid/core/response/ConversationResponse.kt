package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ConversationResponse(
    @SerializedName("sessions")
    var sessions: List<Session> = listOf(),
    @SerializedName("total")
    var total: String? = null
) : Serializable

data class Session(
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("last_message")
    var lastMessage: String? = null,
    @SerializedName("message_count")
    var messageCount: String? = null,
    @SerializedName("title")
    var title: String? = null,
    @SerializedName("updated_at")
    var updatedAt: String? = null
) : Serializable