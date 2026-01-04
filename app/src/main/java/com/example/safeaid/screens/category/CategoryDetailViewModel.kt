package com.example.safeaid.screens.category

import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.response.CategoryDetailResponse
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
class CategoryDetailViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<CategoryDetailState, CategoryDetailEvent>() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    var categoryId: String? = null

    fun loadCategoryDetail(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            ApiCaller.safeApiCall(
                apiCall = { apiService.getDetailsCategory(categoryId) },
                callback = { result ->
                    _isLoading.value = false
                    result.doIfSuccess { data ->
                        updateState(DataResult.Success(CategoryDetailState.CategoryDetailLoaded(data)))
                    }
                    result.doIfFailure { error ->
                        updateState(DataResult.Error(error))
                    }
                }
            )
        }
    }

    override fun onTriggerEvent(event: CategoryDetailEvent) {
        // Handle events if needed
    }
}

sealed class CategoryDetailState {
    data class CategoryDetailLoaded(val data: CategoryDetailResponse) : CategoryDetailState()
}

sealed class CategoryDetailEvent