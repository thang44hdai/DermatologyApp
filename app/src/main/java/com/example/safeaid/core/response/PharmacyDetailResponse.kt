package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName

data class PharmacyDetailResponse(
    @SerializedName("medicines")
    var medicines: List<MedicineResponse>? = null,
    @SerializedName("pharmacy_id")
    var pharmacyId: Int? = null,
    @SerializedName("pharmacy_name")
    var pharmacyName: String? = null,
    @SerializedName("total")
    var total: String? = null
)