package com.example.safeaid.core.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PredictResponse(
    @SerializedName("data")
    var data: Data? = null,
    @SerializedName("success")
    var success: Boolean? = null
) : Serializable {
    data class Data(
        @SerializedName("confidence")
        var confidence: String? = null,
        @SerializedName("diagnosis_history_id")
        var diagnosisHistoryId: Int? = null,
        @SerializedName("disease")
        var disease: Disease? = null,
        @SerializedName("image_url")
        var imageUrl: String? = null,
        @SerializedName("label_en")
        var labelEn: String? = null,
        @SerializedName("label_vi")
        var labelVi: String? = null,
        @SerializedName("scan_id")
        var scanId: Int? = null,
        @SerializedName("user_id")
        var userId: Int? = null,
        @SerializedName("highlighted_image_url")
        var highlightedImageUrl: String? = null,
    ) : Serializable
}

data class Disease(
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("disease_name")
    var diseaseName: String? = null,
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("image_url")
    var imageUrl: String? = null,
    @SerializedName("symptoms")
    var symptoms: String? = null,
    @SerializedName("treatment")
    var treatment: String? = null,
    @SerializedName("medicines")
    var medicines: List<MedicineResponse> = listOf()
) : Serializable