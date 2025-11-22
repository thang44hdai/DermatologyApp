package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MedicineResponse(
    @SerializedName("brand")
    var brand: BrandResponse? = null,
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("disease_ids")
    var diseaseIds: List<String> = listOf(),
    @SerializedName("dosage")
    var dosage: String? = null,
    @SerializedName("generic_name")
    var genericName: String? = null,
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("images")
    var images: List<String> = listOf(),
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("price")
    var price: String? = null,
    @SerializedName("side_effects")
    var sideEffects: String? = null,
    @SerializedName("suitable_for")
    var suitableFor: String? = null,
    @SerializedName("type")
    var type: String? = null
) : Serializable

data class BrandResponse(
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("logo_path")
    var logoPath: String? = null,
    @SerializedName("name")
    var name: String? = null
) : Serializable