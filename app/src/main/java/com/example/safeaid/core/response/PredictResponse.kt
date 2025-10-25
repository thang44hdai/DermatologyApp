package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PredictResponse(
    @SerializedName("all_predictions")
    var allPredictions: List<AllPrediction?>? = null,
    @SerializedName("confidence")
    var confidence: String? = null,
    @SerializedName("label_en")
    var labelEn: String? = null,
    @SerializedName("label_vi")
    var labelVi: String? = null,
    @SerializedName("success")
    var success: Boolean? = null
) : Serializable

data class AllPrediction(
    @SerializedName("confidence")
    var confidence: String? = null,
    @SerializedName("label_en")
    var labelEn: String? = null,
    @SerializedName("label_vi")
    var labelVi: String? = null
)