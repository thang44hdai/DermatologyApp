package com.example.safeaid.core.request

import com.google.gson.annotations.SerializedName

data class CreateReminderRequest(
    @SerializedName("medicine_id")
    val medicineId: String? = null,
    
    @SerializedName("medicine_name")
    val medicineName: String,
    
    @SerializedName("dosage")
    val dosage: String? = null,
    
    @SerializedName("unit")
    val unit: String? = null,
    
    @SerializedName("meal_timing")
    val mealTiming: String? = null,
    
    @SerializedName("frequency")
    val frequency: String,
    
    @SerializedName("times")
    val times: List<TimeSchedule>,
    
    @SerializedName("days_of_week")
    val daysOfWeek: List<Int>? = null,
    
    @SerializedName("start_date")
    val startDate: String,
    
    @SerializedName("end_date")
    val endDate: String? = null,
    
    @SerializedName("is_notification_enabled")
    val isNotificationEnabled: Boolean = true,
    
    @SerializedName("notes")
    val notes: String? = null
)

data class TimeSchedule(
    @SerializedName("time")
    val time: String,
    
    @SerializedName("period")
    val period: String,
    
    @SerializedName("dosage")
    val dosage: String
)
