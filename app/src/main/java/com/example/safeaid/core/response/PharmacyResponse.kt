package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PharmacyResponse(
    @SerializedName("address")
    var address: String? = null,
    @SerializedName("distance_km")
    var distanceKm: String? = null,
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("latitude")
    var latitude: Double,
    @SerializedName("longitude")
    var longitude: Double,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("open_time")
    var openTime: String? = null,
    @SerializedName("close_time")
    var closeTime: String? = null,
    @SerializedName("phone")
    var phone: String? = null,
    @SerializedName("ratings")
    var ratings: String? = null,
    @SerializedName("images")
    var images: List<String>? = listOf(),
    @SerializedName("logo_url")
    var logoUrl: String? = null,
    @SerializedName("is_open_247")
    var isOpen247: Boolean? = null,
) : Serializable