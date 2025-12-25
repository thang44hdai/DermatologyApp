package com.example.safeaid.core.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BrandDetailResponse(
    @SerializedName("brand")
    var brand: Brand? = null,
    @SerializedName("medicines")
    var medicines: List<MedicineResponse> = listOf()
) : Serializable