package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DetectBoundaryResponse(
    @SerializedName("image_url")
    var imageUrl: String? = null,
    @SerializedName("label_en")
    var labelEn: String? = null,
    @SerializedName("predicted_index")
    var predictedIndex: String? = null,
    @SerializedName("success")
    var success: Boolean? = null
) : Serializable