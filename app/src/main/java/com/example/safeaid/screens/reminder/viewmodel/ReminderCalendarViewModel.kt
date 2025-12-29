package com.example.safeaid.screens.reminder.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.request.UpdateReminderStatusRequest
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.core.utils.ApiCaller
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.ErrorResponse
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

    private val _currentWeekOffset = MutableStateFlow(0)
    val currentWeekOffset: StateFlow<Int> = _currentWeekOffset

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun loadWeekCalendar(
        weekOffset: Int = 0,
        autoSelectPosition: AutoSelectPosition = AutoSelectPosition.TODAY
    ) {
        _currentWeekOffset.value = weekOffset

        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = {
                    apiService.getReminderCalendar(weekOffset)
                },
                callback = { result ->
                    result.doIfSuccess { response ->
                        val dayNameFormat = SimpleDateFormat("EEE", Locale("vi", "VN"))
                        val dayNumberFormat = SimpleDateFormat("dd", Locale.getDefault())

                        val days = response.days.map { day ->
                            val date = dateFormat.parse(day.date)
                            val dayName = if (date != null) {
                                dayNameFormat.format(date).replaceFirstChar { it.uppercase() }
                            } else ""
                            val dayNumber = if (date != null) {
                                dayNumberFormat.format(date)
                            } else ""

                            CalendarDay(
                                date = day.date ?: "",
                                dayName = dayName,
                                dayNumber = dayNumber,
                                hasReminders = day.reminderCount > 0,
                                isSelected = false
                            )
                        }

                        // Determine which day to select based on autoSelectPosition
                        val selectedDay = when (autoSelectPosition) {
                            AutoSelectPosition.TODAY -> {
                                val todayDate = Utils.getCurrentDate()
                                days.find { it.date == todayDate } ?: days.firstOrNull()
                            }

                            AutoSelectPosition.FIRST -> days.firstOrNull()
                            AutoSelectPosition.LAST -> days.lastOrNull()
                        }

                        val updatedDays = days.map { day ->
                            day.copy(isSelected = day.date == selectedDay?.date)
                        }

                        _calendarDays.value = updatedDays

                        selectedDay?.let {
                            _selectedDate.value = it.date
                            loadRemindersForDay(it.date)
                        }
                    }
                    result.doIfFailure {
                        _calendarDays.value = emptyList()
                        updateState(
                            DataResult.Error(
                                ErrorResponse(
                                    message = "Lỗi truy cập dữ liệu",
                                    errorCode = 0,
                                    errorType = ""
                                )
                            )
                        )
                    }
                }
            )
        }
    }

    fun selectDay(day: CalendarDay) {
        _selectedDate.value = day.date
        val updatedDays = _calendarDays.value.map {
            it.copy(isSelected = it.date == day.date)
        }
        _calendarDays.value = updatedDays
        loadRemindersForDay(day.date)
    }

    fun loadPreviousWeek() {
        loadWeekCalendar(_currentWeekOffset.value - 1, AutoSelectPosition.LAST)
    }

    fun loadNextWeek() {
        loadWeekCalendar(_currentWeekOffset.value + 1, AutoSelectPosition.FIRST)
    }

    fun reloadCurrentWeek() {
        // Reload current week and keep the selected date
        val currentSelectedDate = _selectedDate.value
        val currentOffset = _currentWeekOffset.value

        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = {
                    apiService.getReminderCalendar(currentOffset)
                },
                callback = { result ->
                    result.doIfSuccess { response ->
                        val dayNameFormat = SimpleDateFormat("EEE", Locale("vi", "VN"))
                        val dayNumberFormat = SimpleDateFormat("dd", Locale.getDefault())

                        val days = response.days.map { day ->
                            val date = dateFormat.parse(day.date)
                            val dayName = if (date != null) {
                                dayNameFormat.format(date).replaceFirstChar { it.uppercase() }
                            } else ""
                            val dayNumber = if (date != null) {
                                dayNumberFormat.format(date)
                            } else ""

                            CalendarDay(
                                date = day.date ?: "",
                                dayName = dayName,
                                dayNumber = dayNumber,
                                hasReminders = day.reminderCount > 0,
                                isSelected = day.date == currentSelectedDate
                            )
                        }

                        _calendarDays.value = days

                        // Reload reminders for the selected date
                        if (currentSelectedDate.isNotEmpty()) {
                            loadRemindersForDay(currentSelectedDate)
                        }
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
                                    val firstStatus =
                                        if (schedule.mealTiming == "before_meal") "Trước khi ăn" else "Sau khi ăn"
                                    val status =
                                        "$firstStatus • ${schedule.dosage} ${schedule.unit}"
                                    MedicineReminder(
                                        reminderId = schedule.reminderId,
                                        name = schedule.medicineName ?: "Unknown",
                                        dosage = schedule.dosage ?: "",
                                        status = status,
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
                        updateState(
                            DataResult.Error(
                                ErrorResponse(
                                    message = "Lỗi truy cập dữ liệu",
                                    errorCode = 0,
                                    errorType = ""
                                )
                            )
                        )
                    }
                }
            )
        }
    }

    fun onMedicineCheckChanged(medicine: MedicineReminder, isChecked: Boolean) {
        // Update medicine check state locally
//        Log.i("hihihi", "$isChecked\n$medicine")
//        Log.i("hihihi", "${_reminderTimes.value}")
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = {
                    apiService.updateReminderStatus(
                        reminderId = medicine.reminderId ?: "",
                        scheduledTime = medicine.time,
                        targetDate = _selectedDate.value
                    )
                },
                callback = { result ->
                    result.doIfSuccess {
                        _reminderTimes.value = _reminderTimes.value.map { reminderTime ->
                            reminderTime.copy(
                                medicines = reminderTime.medicines.map { med ->
                                    if (med.reminderId == medicine.reminderId && med.time == medicine.time) {
                                        val x = med.copy(isTaken = isChecked)
                                        med.copy(isTaken = isChecked)
                                    } else {
                                        med
                                    }
                                }
                            )
                        }
                    }
                    result.doIfFailure {
                        updateState(DataResult.Error(it.copy(errorCode = Random().nextInt())))
                    }
                }
            )
        }
    }

    override fun onTriggerEvent(event: ReminderEvent) {}
}

enum class AutoSelectPosition {
    TODAY,
    FIRST,
    LAST
}

sealed class ReminderState {}
sealed class ReminderEvent {}
