package com.example.safeaid.core.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReminderCalendarResponse(
    @SerializedName("start_date")
    val startDate: String? = null,
    @SerializedName("end_date")
    val endDate: String? = null,
    @SerializedName("days")
    val days: List<ReminderDay> = listOf()
) : Serializable

data class ReminderDay(
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("has_reminders")
    val hasReminders: Boolean = false,
    @SerializedName("reminder_count")
    val reminderCount: Int = 0,
    @SerializedName("times")
    val times: List<String> = listOf()
) : Serializable
