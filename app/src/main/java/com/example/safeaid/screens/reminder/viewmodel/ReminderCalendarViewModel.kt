package com.example.safeaid.screens.reminder.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.request.UpdateReminderStatusRequest
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.core.utils.ApiCaller
import com.example.safeaid.core.utils.Utils
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.screens.reminder.CalendarDay
import com.example.safeaid.screens.reminder.MedicineReminder
import com.example.safeaid.screens.reminder.ReminderTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReminderCalendarViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<ReminderState, ReminderEvent>() {

    private val _calendarDays = MutableStateFlow<List<CalendarDay>>(emptyList())
    val calendarDays: StateFlow<List<CalendarDay>> = _calendarDays

    private val _reminderTimes = MutableStateFlow<List<ReminderTime>>(emptyList())
    val reminderTimes: StateFlow<List<ReminderTime>> = _reminderTimes

    private val _selectedDate = MutableStateFlow<String>("")
    val selectedDate: StateFlow<String> = _selectedDate

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> = _isEmpty

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun loadWeekCalendar() {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        val dayNameFormat = SimpleDateFormat("EEE", Locale("vi", "VN"))
        val dayNumberFormat = SimpleDateFormat("dd", Locale.getDefault())

        val days = mutableListOf<CalendarDay>()
        for (i in -15..15) {
            val date = Calendar.getInstance().apply {
                time = today
                add(Calendar.DAY_OF_YEAR, i)
            }.time

            val dateStr = dateFormat.format(date)
            val dayName = dayNameFormat.format(date).replaceFirstChar { it.uppercase() }
            val dayNumber = dayNumberFormat.format(date)

            days.add(
                CalendarDay(
                    date = dateStr,
                    dayName = dayName,
                    dayNumber = dayNumber,
                    hasReminders = false,
                    isSelected = i == 0
                )
            )
        }

        _calendarDays.value = days
        _selectedDate.value = days[0].date

        // Load reminders for the week
        loadRemindersForMonth()

        // Load detail for today
        loadRemindersForDay(days[15].date)
    }

    fun selectDay(day: CalendarDay) {
        _selectedDate.value = day.date
        val updatedDays = _calendarDays.value.map {
            it.copy(isSelected = it.date == day.date)
        }
        _calendarDays.value = updatedDays
        loadRemindersForDay(day.date)
    }

    private fun loadRemindersForMonth() {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = {
                    apiService.getReminderCalendar()
                },
                callback = { result ->
                    result.doIfSuccess { response ->
                        Log.i("hihihi", "$response")
                        // Update calendar days with reminder indicators
                        val daysWithReminders = response.days.associate {
                            it.date to it.reminderCount
                        }

                        _calendarDays.value = _calendarDays.value.map { day ->
                            day.copy(hasReminders = daysWithReminders[day.date] ?: 0 > 0)
                        }
                    }
                    result.doIfFailure {
                        // Keep calendar without indicators
                    }
                }
            )
        }
    }

    private fun loadRemindersForDay(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = {
                    apiService.getReminderDayDetail(date)
                },
                callback = { result ->
                    result.doIfSuccess { response ->
                        if (response.schedules.isNotEmpty()) {
                            // Group schedules by time
                            val groupedByTime = response.schedules.groupBy {
                                Utils.getPeriodFromTime(it.time ?: "")
                            }

                            val reminderTimes = groupedByTime.map { (time, schedules) ->
                                val medicines = schedules.map { schedule ->
                                    MedicineReminder(
                                        reminderId = schedule.reminderId,
                                        name = schedule.medicineName ?: "Unknown",
                                        dosage = schedule.dosage ?: "",
                                        status = schedule.status ?: "",
                                        time = schedule.time ?: "",
                                        note = schedule.note ?: "",
                                        isTaken = schedule.isTaken
                                    )
                                }

                                ReminderTime(
                                    time = Utils.getPeriodFromTimeVn(time),
                                    totalCount = medicines.size,
                                    medicines = medicines
                                )
                            }

                            _reminderTimes.value = reminderTimes
                            _isEmpty.value = false
                        } else {
                            _reminderTimes.value = emptyList()
                            _isEmpty.value = true
                        }
                    }
                    result.doIfFailure {
                        _reminderTimes.value = emptyList()
                        _isEmpty.value = true
                    }
                }
            )
        }
    }

    fun onMedicineCheckChanged(medicine: MedicineReminder, isChecked: Boolean) {
        // Update medicine check state locally
//        Log.i("hihihi", "$medicine")
//        Log.i("hihihi", "${_reminderTimes.value}")
        _reminderTimes.value = _reminderTimes.value.map { reminderTime ->
            reminderTime.copy(
                medicines = reminderTime.medicines.map { med ->
                    if (med.reminderId == medicine.reminderId && med.time == medicine.time) {
                        med.copy(isTaken = isChecked)
                    } else {
                        med.copy()
                    }
                }
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val request = UpdateReminderStatusRequest(
                reminderId = medicine.reminderId ?: "",
                scheduledTime = medicine.time,
                targetDate = _selectedDate.value
            )
            apiService.updateReminderStatus(medicine.reminderId ?: "", request)
        }
    }

    override fun onTriggerEvent(event: ReminderEvent) {}
}

sealed class ReminderState {}
sealed class ReminderEvent {}
