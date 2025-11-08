package com.example.safeaid.screens.pharmacy

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.response.PharmacyDetailResponse
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.core.utils.ApiCaller
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PharmacyViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<PharmacyState, PharmacyEvent>() {

    fun loadPharmacyDetail(pharmacyId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = { apiService.getPharmacyMedicines(pharmacyId) },
                callback = { result ->
                    result.doIfSuccess { data ->
                        updateState(DataResult.Success(PharmacyState.PharmacyDetail(data)))
                    }
                    result.doIfFailure {
                    }
                }
            )
        }
    }

    override fun onTriggerEvent(event: PharmacyEvent) {

    }
}

sealed class PharmacyState {
    class PharmacyDetail(val data: PharmacyDetailResponse) : PharmacyState()
}

sealed class PharmacyEvent {}