package com.example.safeaid.screens.reminder.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.response.Reminder
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.core.utils.ApiCaller
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderListViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<ReminderListState, ReminderListEvent>() {

    private val _allReminders = MutableStateFlow<List<Reminder>>(listOf())
    private val allReminders = _allReminders.asStateFlow()

    fun loadReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = { apiService.getReminderTabs() },
                callback = { result ->
                    result.doIfSuccess { data ->
                        Log.i("hihihi", "$data")
                        _allReminders.value = data.reminders ?: listOf()
                        filterReminders("active")
                    }
                    result.doIfFailure { error ->
                    }
                }
            )
        }
    }

    fun filterReminders(tab: String) {
        val filtered = when (tab) {
            "active" -> _allReminders.value.filter { it.isActive == true }
            "old" -> _allReminders.value.filter { it.isActive == false }
            else -> _allReminders.value
        }
        updateState(DataResult.Success(ReminderListState.RemindersList(filtered)))
    }

    override fun onTriggerEvent(event: ReminderListEvent) {
    }
}

sealed class ReminderListState {
    data class RemindersList(val reminders: List<Reminder>) : ReminderListState()
}

sealed class ReminderListEvent
