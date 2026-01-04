package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DetailConversationResponse(
    @SerializedName("messages")
    var messages: List<Message> = listOf(),
    @SerializedName("session_id")
    var sessionId: String? = null,
    @SerializedName("total")
    var total: Int? = null
) : Serializable

data class Message(
    @SerializedName("content")
    var content: String? = null,
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("role")
    var role: String? = null,
    @SerializedName("sources")
    var sources: List<Source>? = listOf()
) : Serializable
