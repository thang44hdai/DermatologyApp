package com.example.safeaid.core.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReminderDayDetailResponse(
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("total_reminders")
    val totalReminders: Int = 0,
    @SerializedName("schedules")
    val schedules: List<ReminderSchedule> = listOf()
) : Serializable

data class ReminderSchedule(
    @SerializedName("reminder_id")
    val reminderId: Int? = null,
    @SerializedName("medicine_name")
    val medicineName: String? = null,
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("dosage")
    val dosage: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("is_taken")
    val isTaken: Boolean = false
) : Serializable
