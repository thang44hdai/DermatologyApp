package com.example.safeaid.core.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class HistoryResponse(
    @SerializedName("limit")
    var limit: Int? = null,
    @SerializedName("scans")
    var scans: List<Scan> = listOf(),
    @SerializedName("skip")
    var skip: Int? = null,
    @SerializedName("total")
    var total: Int? = null
) : Serializable

data class Scan(
    @SerializedName("diagnosis_history")
    var diagnosisHistory: DiagnosisHistory? = null,
    @SerializedName("disease")
    var disease: Disease? = null,
    @SerializedName("image_url")
    var imageUrl: String? = null,
    @SerializedName("scan_date")
    var scanDate: String? = null,
    @SerializedName("scan_id")
    var scanId: Int? = null,
    @SerializedName("status")
    var status: String? = null,
    @SerializedName("highlighted_image_url")
    var highlightedImageUrl: String? = null,
) : Serializable {
    data class DiagnosisHistory(
        @SerializedName("created_at")
        var createdAt: String? = null,
        @SerializedName("id")
        var id: Int? = null,
        @SerializedName("note")
        var note: String? = null
    ) : Serializable
}