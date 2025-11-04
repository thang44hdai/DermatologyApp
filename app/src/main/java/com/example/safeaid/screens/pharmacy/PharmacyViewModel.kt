package com.example.safeaid.screens.pharmacy

import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.screens.map.MapEvent
import javax.inject.Inject

@HiltViewModel
class PharmacyViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<PharmacyState, PharmacyEvent>() {

    override fun onTriggerEvent(event: PharmacyEvent) {

    }
}

sealed class PharmacyState {}
sealed class PharmacyEvent {}