package com.example.safeaid.core.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReminderTabResponse(
    @SerialName("reminders")
    var reminders: List<Reminder>?
)