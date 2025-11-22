package com.example.safeaid.screens.home

import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.response.ListPharmacyResponse
import com.example.safeaid.core.response.MedicineResponse
import com.example.safeaid.core.response.UserResponse
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
class HomeViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<HomeState, HomeEvent>() {
    private val medicineResponse = MutableStateFlow<List<MedicineResponse>>(listOf())
    val _medicineResponse = medicineResponse.asStateFlow()

    private val user = MutableStateFlow<UserResponse>(UserResponse())
    val _user = user.asStateFlow()


    fun loadHomeData() {
        // Load user info
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = { apiService.getUserInfo() },
                callback = { result ->
                    result.doIfSuccess { data ->
                        user.value = data
                    }
                    result.doIfFailure {
                    }
                }
            )
        }

        // Load medicines
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = { apiService.getMedicines() },
                callback = { result ->
                    result.doIfSuccess { data ->
                        medicineResponse.value = data.medicines
                    }
                    result.doIfFailure {
                    }
                }
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = { apiService.getPharmacies() },
                callback = { result ->
                    result.doIfSuccess { data ->
                        updateState(DataResult.Success(HomeState.PharmaciesList(data)))
                    }
                    result.doIfFailure {
                    }
                }
            )
        }
    }

    override fun onTriggerEvent(event: HomeEvent) {

    }
}

sealed class HomeState {
    class PharmaciesList(val data: ListPharmacyResponse) : HomeState()
}

sealed class HomeEvent {}
