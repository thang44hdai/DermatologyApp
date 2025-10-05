package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName

data class DecisionUserRes(
    @SerializedName("decisionUser")
    val decisionUser: DecisionUser,
    @SerializedName("viewMode")
    val viewMode: String
) {
    data class DecisionUser(
        @SerializedName("shozo_infos")
        val shozoInfos: List<ShozoInfo>
    ) {
        data class ShozoInfo(
            @SerializedName("shozo_name")
            val shozoName: String,
            @SerializedName("userList")
            val userList: List<User>
        ) {
            data class User(
                @SerializedName("user_id")
                val userId: String,
                @SerializedName("user_name")
                val userName: String
            )
        }
    }
}