package com.example.safeaid.core.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ListPharmacyResponse(
    @SerializedName("pharmacies")
    var pharmacies: List<PharmacyResponse> = listOf(),
    @SerializedName("total")
    var total: String? = null,
    @SerializedName("skip")
    var skip: String? = null,
    @SerializedName("limit")
    var limit: String? = null
) : Serializable