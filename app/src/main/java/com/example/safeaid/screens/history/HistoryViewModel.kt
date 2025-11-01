package com.example.safeaid.screens.history

import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.response.HistoryResponse
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
class HistoryViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<HistoryState, HistoryEvent>() {
    fun getHistoryList() {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = {
                    apiService.getHistoryList()
                },
                callback = { result ->
                    result.doIfSuccess {
                        updateState(DataResult.Success(HistoryState.HistoryList(data = it)))
                    }
                    result.doIfFailure {
                    }
                }
            )
        }
    }

    override fun onTriggerEvent(event: HistoryEvent) {

    }
}

sealed class HistoryState {
    class HistoryList(val data: HistoryResponse) : HistoryState()
}

sealed class HistoryEvent {}