package com.example.safeaid.models

import com.google.gson.annotations.SerializedName

data class Brand(
    @SerializedName("name") val name: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("open_hours") val openHours: String = "",
    @SerializedName("ratings") val ratings: Int = 0,
    @SerializedName("latitude") val latitude: Double = 0.0,
    @SerializedName("longitude") val longitude: Double = 0.0,
    @SerializedName("id") val id: Int = 0
)
