package com.example.safeaid.core.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ListMedicineResponse(
    @SerializedName("limit")
    var limit: String? = null,
    @SerializedName("medicines")
    var medicines: List<MedicineResponse> = listOf(),
    @SerializedName("skip")
    var skip: String? = null,
    @SerializedName("total")
    var total: String? = null
) : Serializable