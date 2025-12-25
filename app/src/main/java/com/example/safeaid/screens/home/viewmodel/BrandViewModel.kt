package com.example.safeaid.screens.home.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.data.MockDataSource
import com.example.safeaid.core.response.BrandDetailResponse
import com.example.safeaid.core.response.BrandsResponse
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
class BrandViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<BrandState, BrandEvent>() {

    private val _brands = MutableStateFlow<BrandsResponse>(BrandsResponse())
    val brands = _brands.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadBrands() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            ApiCaller.safeApiCall(
                apiCall = { apiService.getBrand() },
                callback = { result ->
                    _isLoading.value = false
                    result.doIfSuccess { data ->
                        _brands.value = data
                        updateState(DataResult.Success(BrandState.BrandsList(data)))
                    }
                    result.doIfFailure { error ->
                        updateState(DataResult.Error(error))
                    }
                }
            )
        }
    }

    fun loadBrandDetail(brandId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            ApiCaller.safeApiCall(
                apiCall = { apiService.getDetailsBrand(brandId) },
                callback = { result ->
                    _isLoading.value = false
                    result.doIfSuccess { data ->
                        updateState(DataResult.Success(BrandState.BrandDetail(data)))
                    }
                    result.doIfFailure { error ->
                        updateState(DataResult.Error(error))
                    }
                }
            )
        }
    }

    override fun onTriggerEvent(event: BrandEvent) {
        // Handle events if needed
    }
}

sealed class BrandState {
    data class BrandsList(val data: BrandsResponse) : BrandState()
    data class BrandDetail(val data: BrandDetailResponse) : BrandState()
}

sealed class BrandEvent
