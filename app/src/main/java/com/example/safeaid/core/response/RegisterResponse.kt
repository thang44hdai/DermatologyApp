package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("avatar_url")
    var avatarUrl: String? = null,
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("date_of_birth")
    var dateOfBirth: String? = null,
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("fullname")
    var fullname: String? = null,
    @SerializedName("gender")
    var gender: String? = null,
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("role")
    var role: String? = null,
    @SerializedName("updated_at")
    var updatedAt: String? = null,
    @SerializedName("username")
    var username: String? = null
)