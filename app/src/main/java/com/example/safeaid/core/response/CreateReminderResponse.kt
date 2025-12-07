package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName

data class CreateReminderResponse(
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("days_of_week")
    var daysOfWeek: List<Int>? = null,
    @SerializedName("dosage")
    var dosage: String? = null,
    @SerializedName("end_date")
    var endDate: String? = null,
    @SerializedName("frequency")
    var frequency: String? = null,
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("is_active")
    var isActive: Boolean? = null,
    @SerializedName("is_custom_medicine")
    var isCustomMedicine: Boolean? = null,
    @SerializedName("is_notification_enabled")
    var isNotificationEnabled: Boolean? = null,
    @SerializedName("meal_timing")
    var mealTiming: String? = null,
    @SerializedName("medicine_id")
    var medicineId: String? = null,
    @SerializedName("medicine_name")
    var medicineName: String? = null,
    @SerializedName("notes")
    var notes: String? = null,
    @SerializedName("start_date")
    var startDate: String? = null,
    @SerializedName("times")
    var times: List<Time>? = null,
    @SerializedName("unit")
    var unit: String? = null,
    @SerializedName("updated_at")
    var updatedAt: Any? = null,
    @SerializedName("user_id")
    var userId: String? = null
)

data class Time(
    @SerializedName("dosage")
    var dosage: String? = null,
    @SerializedName("period")
    var period: String? = null,
    @SerializedName("time")
    var time: String? = null
)