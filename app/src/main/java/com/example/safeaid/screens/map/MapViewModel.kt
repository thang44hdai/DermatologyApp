package com.example.safeaid.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.request.RegisterRequest
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.core.utils.ApiCaller
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.screens.authenication.viewmodel.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<MapState, MapEvent>() {
    fun searchPharmacyNear(
        latitude: String,
        longitude: String,
        radiusKm: String = "100",
        limit: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = {
                    apiService.getPharmaciesNearBy(
                        latitude = latitude,
                        longitude = longitude,
                        radiusKm = radiusKm,
                        limit = null
                    )
                },
                callback = { result ->
                    result.doIfSuccess {
                        updateState(DataResult.Success(MapState.PharmaciesNearBy(data = it)))
                    }
                    result.doIfFailure {
                    }
                }
            )
        }
    }

    override fun onTriggerEvent(event: MapEvent) {

    }
}

sealed class MapState {
    class PharmaciesNearBy(val data: List<PharmacyResponse>) : MapState()
}

sealed class MapEvent {}