package com.example.safeaid.core.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reminder(
    @SerialName("created_at")
    var createdAt: String?,
    @SerialName("days_of_week")
    var daysOfWeek: List<Int>?,
    @SerialName("dosage")
    var dosage: String?,
    @SerialName("end_date")
    var endDate: String?,
    @SerialName("frequency")
    var frequency: String?,
    @SerialName("id")
    var id: String?,
    @SerialName("is_active")
    var isActive: Boolean?,
    @SerialName("is_custom_medicine")
    var isCustomMedicine: Boolean?,
    @SerialName("is_notification_enabled")
    var isNotificationEnabled: Boolean?,
    @SerialName("meal_timing")
    var mealTiming: String?,
    @SerialName("medicine_id")
    var medicineId: String?,
    @SerialName("medicine_name")
    var medicineName: String?,
    @SerialName("notes")
    var notes: String?,
    @SerialName("start_date")
    var startDate: String?,
    @SerialName("times")
    var times: List<Time>?,
    @SerialName("unit")
    var unit: String?,
    @SerialName("updated_at")
    var updatedAt: String?,
    @SerialName("user_id")
    var userId: String?
)