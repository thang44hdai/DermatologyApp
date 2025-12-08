package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ReminderTabResponse(
    @SerializedName("reminders")
    var reminders: List<Reminder>?
)