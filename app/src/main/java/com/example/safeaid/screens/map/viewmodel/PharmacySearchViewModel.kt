package com.example.safeaid.screens.map.viewmodel

import com.example.safeaid.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PharmacySearchViewModel @Inject constructor() :
    BaseViewModel<PharmacySearchState, PharmacySearchEvent>() {
    var sortState: SortState = SortState.DECREASE

    override fun onTriggerEvent(event: PharmacySearchEvent) {
    }
}

sealed class PharmacySearchState
sealed class PharmacySearchEvent

enum class SortState {
    INCREASE, DECREASE
}