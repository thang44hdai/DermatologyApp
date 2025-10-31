package com.example.safeaid.core.request

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("avatar_url")
    var avatarUrl: String? = null,
    @SerializedName("date_of_birth")
    var dateOfBirth: String? = null,
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("fullname")
    var fullname: String? = null,
    @SerializedName("gender")
    var gender: String? = null,
    @SerializedName("password")
    var password: String? = null,
    @SerializedName("username")
    var username: String? = null
)