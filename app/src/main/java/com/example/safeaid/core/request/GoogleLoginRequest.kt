package com.example.safeaid.core.request

import com.google.gson.annotations.SerializedName

data class GoogleLoginRequest(
    @SerializedName("id_token")
    var idToken: String? = null
)

