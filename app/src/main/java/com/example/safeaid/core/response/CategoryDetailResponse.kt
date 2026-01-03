package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CategoryDetailResponse(
    @SerializedName("category")
    var category: CategoryResponse? = null,
    @SerializedName("limit")
    var limit: Int? = null,
    @SerializedName("medicines")
    var medicines: List<MedicineResponse> = listOf(),
    @SerializedName("skip")
    var skip: String? = null,
    @SerializedName("total")
    var total: String? = null
) : Serializable