package com.example.safeaid.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Product(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("generic_name") val genericName: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("dosage") val dosage: String = "",
    @SerializedName("side_effects") val sideEffects: String = "",
    @SerializedName("suitable_for") val suitableFor: String = "",
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("disease_ids") val diseaseIds: List<Int> = emptyList(),
    @SerializedName("created_at") val createdAt: String = ""
)
