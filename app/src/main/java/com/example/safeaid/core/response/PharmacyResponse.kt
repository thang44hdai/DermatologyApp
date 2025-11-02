package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PharmacyResponse(
    @SerializedName("address")
    var address: String? = null,
    @SerializedName("distance_km")
    var distanceKm: String? = null,
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("latitude")
    var latitude: Double,
    @SerializedName("longitude")
    var longitude: Double,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("open_hours")
    var openHours: String? = null,
    @SerializedName("phone")
    var phone: String? = null,
    @SerializedName("ratings")
    var ratings: String? = null
) : Serializable