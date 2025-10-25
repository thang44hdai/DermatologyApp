package com.example.safeaid.core.request


import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("password")
    var password: String? = null,
    @SerializedName("username")
    var username: String? = null
)