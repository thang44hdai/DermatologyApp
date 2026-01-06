package com.example.safeaid.screens.home

import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.response.CategoryResponse
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

    private val categories = MutableStateFlow<List<CategoryResponse>>(listOf())
    val _categories = categories.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()
    
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData = _hasMoreData.asStateFlow()
    
    private var currentSkip = 0
    private val pageSize = 20

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
                        // Use mock user data if API fails
                        user.value = UserResponse(
                            fullname = "Người dùng",
                            email = "user@example.com",
                            avatarUrl = null
                        )
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
                        // Use mock data if API fails
                        medicineResponse.value = com.example.safeaid.core.data.MockDataSource.getMockMedicines()
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
                        // Use mock data if API fails
                        val mockPharmacies = com.example.safeaid.core.data.MockDataSource.getMockPharmacies()
                        updateState(DataResult.Success(HomeState.PharmaciesList(
                            ListPharmacyResponse(pharmacies = mockPharmacies)
                        )))
                    }
                }
            )
        }

        // Load categories
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = { apiService.getCategories() },
                callback = { result ->
                    result.doIfSuccess { data ->
                        categories.value = data
                    }
                    result.doIfFailure {
                        categories.value = listOf()
                    }
                }
            )
        }
    }

    fun loadMedicines(reset: Boolean = false) {
        if (reset) {
            currentSkip = 0
            _hasMoreData.value = true
        }

        if (!_hasMoreData.value) return
        
        _isLoadingMore.value = true
        
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = { 
                    apiService.getMedicines(
                        skip = currentSkip.toString(),
                        limit = pageSize.toString()
                    )
                },
                callback = { result ->
                    _isLoadingMore.value = false
                    result.doIfSuccess { data ->
                        val newMedicines = data.medicines
                        
                        if (reset) {
                            medicineResponse.value = newMedicines
                        } else {
                            medicineResponse.value = medicineResponse.value + newMedicines
                        }
                        
                        // Update pagination state
                        currentSkip += newMedicines.size
                        _hasMoreData.value = newMedicines.size == pageSize
                        
                        updateState(DataResult.Success(HomeState.MedicinesLoaded(
                            medicines = medicineResponse.value,
                            isLoadMore = !reset
                        )))
                    }
                    result.doIfFailure { error ->
                    }
                }
            )
        }
    }

    fun loadMoreMedicines() {
        if (!_isLoadingMore.value && _hasMoreData.value) {
            loadMedicines(reset = false)
        }
    }

    override fun onTriggerEvent(event: HomeEvent) {

    }
}

sealed class HomeState {
    class PharmaciesList(val data: ListPharmacyResponse) : HomeState()
    class MedicinesLoaded(val medicines: List<MedicineResponse>, val isLoadMore: Boolean) : HomeState()
}

sealed class HomeEvent {}
