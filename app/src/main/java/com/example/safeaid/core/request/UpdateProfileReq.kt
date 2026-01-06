package com.example.safeaid.core.request

import com.google.gson.annotations.SerializedName

data class UpdateProfileReq(
    @SerializedName("avatar")
    var avatar: String? = null,
    @SerializedName("date_of_birth")
    var dateOfBirth: String? = null,
    @SerializedName("fullname")
    var fullname: String? = null,
    @SerializedName("gender")
    var gender: String? = null
)
