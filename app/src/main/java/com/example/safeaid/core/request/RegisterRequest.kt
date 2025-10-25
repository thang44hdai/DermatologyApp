package com.example.safeaid.core.request


import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("full_name")
    var fullName: String? = null,
    @SerializedName("password")
    var password: String? = null,
    @SerializedName("username")
    var username: String? = null
)