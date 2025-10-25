package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("full_name")
    var fullName: String? = null,
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("is_active")
    var isActive: Int? = null,
    @SerializedName("role")
    var role: String? = null,
    @SerializedName("username")
    var username: String? = null
)