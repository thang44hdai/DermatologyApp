package com.example.safeaid.core.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CategoryResponse(
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("image_url")
    var imageUrl: String? = null,
    @SerializedName("is_active")
    var isActive: Boolean? = null,
    @SerializedName("medicine_count")
    var medicineCount: String? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("updated_at")
    var updatedAt: String? = null
) : Serializable