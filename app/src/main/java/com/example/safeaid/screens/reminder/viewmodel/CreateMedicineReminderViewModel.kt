package com.example.safeaid.screens.reminder.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.request.CreateReminderRequest
import com.example.safeaid.core.response.CreateReminderResponse
import com.example.safeaid.core.response.MedicineResponse
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.core.utils.ApiCaller
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateMedicineReminderViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<CreateReminderState, CreateReminderEvent>() {

    fun loadMedicines() {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = {
                    apiService.getMedicines()
                },
                callback = { result ->
                    result.doIfSuccess { response ->
                        updateState(DataResult.Success(CreateReminderState.MedicinesList(response.medicines)))
                    }
                    result.doIfFailure {}
                }
            )
        }
    }

    fun createReminder(request: CreateReminderRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = {
                    apiService.createReminder(request)
                },
                callback = { result ->
                    result.doIfSuccess { response ->
                        updateState(DataResult.Success(CreateReminderState.CreateSuccess(response)))
                    }
                    result.doIfFailure { error ->
                    }
                }
            )
        }
    }

    override fun onTriggerEvent(event: CreateReminderEvent) {}
}

sealed class CreateReminderState {
    data class MedicinesList(val medicines: List<MedicineResponse>) : CreateReminderState()
    data class CreateSuccess(val response: CreateReminderResponse) : CreateReminderState()
}

sealed class CreateReminderEvent {}
