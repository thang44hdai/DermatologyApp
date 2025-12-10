package com.example.safeaid.core.request

import com.google.gson.annotations.SerializedName

data class UpdateReminderStatusRequest(
    @SerializedName("reminder_id")
    val reminderId: String,
    
    @SerializedName("scheduled_time")
    val scheduledTime: String,
    
    @SerializedName("target_date")
    val targetDate: String
)
