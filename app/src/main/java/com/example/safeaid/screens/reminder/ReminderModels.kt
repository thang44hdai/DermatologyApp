package com.example.safeaid.screens.reminder

data class CalendarDay(
    val date: String,
    val dayName: String,
    val dayNumber: String,
    val hasReminders: Boolean,
    val isSelected: Boolean = false
)

data class ReminderTime(
    val time: String,
    val totalCount: Int,
    val medicines: List<MedicineReminder>
)

data class MedicineReminder(
    val reminderId: Int,
    val name: String,
    val dosage: String,
    val status: String,
    val time: String = "",
    val note: String = "",
    val isTaken: Boolean = false
)
