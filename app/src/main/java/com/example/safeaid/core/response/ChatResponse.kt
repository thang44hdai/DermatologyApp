package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ChatResponse(
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("message")
    var message: String? = null,
    @SerializedName("session_id")
    var sessionId: String? = null,
    @SerializedName("sources")
    var sources: List<Source?>? = null
) : Serializable {
    data class Source(
        @SerializedName("image_url")
        var imageUrl: String? = null,
        @SerializedName("medicine_id")
        var medicineId: Int? = null,
        @SerializedName("name")
        var name: String? = null,
        @SerializedName("price")
        var price: String? = null
    ) : Serializable
}