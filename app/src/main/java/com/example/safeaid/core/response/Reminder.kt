package com.example.safeaid.core.response


import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Reminder(
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("days_of_week")
    var daysOfWeek: List<Int>?,
    @SerializedName("dosage")
    var dosage: String?,
    @SerializedName("end_date")
    var endDate: String?,
    @SerializedName("frequency")
    var frequency: String?,
    @SerializedName("id")
    var id: String?,
    @SerializedName("is_active")
    var isActive: Boolean?,
    @SerializedName("is_custom_medicine")
    var isCustomMedicine: Boolean?,
    @SerializedName("is_notification_enabled")
    var isNotificationEnabled: Boolean?,
    @SerializedName("meal_timing")
    var mealTiming: String?,
    @SerializedName("medicine_id")
    var medicineId: String?,
    @SerializedName("medicine_name")
    var medicineName: String?,
    @SerializedName("notes")
    var notes: String?,
    @SerializedName("start_date")
    var startDate: String?,
    @SerializedName("times")
    var times: List<Time>?,
    @SerializedName("unit")
    var unit: String?,
    @SerializedName("updated_at")
    var updatedAt: String?,
    @SerializedName("user_id")
    var userId: String?
)