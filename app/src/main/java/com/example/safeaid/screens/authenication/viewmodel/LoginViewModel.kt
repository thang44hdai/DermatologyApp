package com.example.safeaid.screens.authenication.viewmodel

import com.example.safeaid.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : BaseViewModel<LoginState, LoginEvent>() {


    override fun onTriggerEvent(event: LoginEvent) {
    }
}

sealed class LoginState {}
sealed class LoginEvent {}