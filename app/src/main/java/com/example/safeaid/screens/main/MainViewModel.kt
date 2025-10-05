package com.example.safeaid.screens.main

import android.os.Parcelable
import com.example.safeaid.core.base.BaseViewModel

class MainViewModel : BaseViewModel<MainEvent, MainState>() {

    var currentPage = 0
    var currentItem = 0

    var savedScrollPosition: Parcelable? = null

    override fun onTriggerEvent(event: MainState) {
    }
}

sealed class MainState {
}

sealed class MainEvent