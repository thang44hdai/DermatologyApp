package com.example.safeaid.screens.profile

import androidx.lifecycle.ViewModel
import com.example.safeaid.core.base.BaseViewModel
import javax.inject.Inject

class ProfileViewModel @Inject constructor(

) : BaseViewModel<ProfileState, ProfileEvent>() {
    override fun onTriggerEvent(event: ProfileEvent) {
    }
}

sealed class ProfileState {}
sealed class ProfileEvent {}