package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PharmacyResponse(
    @SerializedName("address")
    var address: String? = null,
    @SerializedName("distance_km")
    var distanceKm: Int? = null,
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("latitude")
    var latitude: Int? = null,
    @SerializedName("longitude")
    var longitude: Int? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("open_hours")
    var openHours: String? = null,
    @SerializedName("phone")
    var phone: String? = null,
    @SerializedName("ratings")
    var ratings: Int? = null
) : Serializable