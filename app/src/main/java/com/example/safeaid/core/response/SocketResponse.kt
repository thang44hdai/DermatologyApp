package com.example.safeaid.core.response

import com.google.gson.annotations.SerializedName

sealed class SocketResponse {
    data class Status(
        @SerializedName("type") val type: String,
        @SerializedName("status") val status: String
    ) : SocketResponse()

    data class Start(
        @SerializedName("type") val type: String,
        @SerializedName("session_id") val sessionId: String
    ) : SocketResponse()

    data class Chunk(
        @SerializedName("type") val type: String,
        @SerializedName("content") val content: String
    ) : SocketResponse()

    data class End(
        @SerializedName("type") val type: String,
        @SerializedName("sources") val sources: List<Source>,
        @SerializedName("created_at") val createdAt: String
    ) : SocketResponse()

    data class Error(
        @SerializedName("type") val type: String,
        @SerializedName("error") val error: String,
        @SerializedName("detail") val detail: String?
    ) : SocketResponse()
}
