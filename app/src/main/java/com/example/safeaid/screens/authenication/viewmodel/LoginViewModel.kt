package com.example.safeaid.screens.authenication.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.request.LoginRequest
import com.example.safeaid.core.request.RefreshTokenRequest
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.core.utils.ApiCaller
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.onLoading
import com.example.safeaid.pref.AppPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,
    private val appPreference: AppPreference
) : BaseViewModel<LoginState, LoginEvent>() {
    fun verifyToken() {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = { apiService.verifyToken() },
                callback = { result ->
                    result.doIfSuccess {
                        updateState(DataResult.Success(LoginState.LoginRes(isSuccess = true)))
                    }
                    result.doIfFailure {
                        refreshToken()
                    }
                }
            )
        }
    }

    fun login(userName: String, pw: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = LoginRequest(username = userName, password = pw)
            ApiCaller.safeApiCall(
                apiCall = { apiService.login(request) },
                callback = { result ->
                    result.onLoading {
                    }
                    result.doIfSuccess {
                        val token = it.accessToken
                        val refresherToken = it.refreshToken
                        launch(Dispatchers.IO) { token?.let { it1 -> appPreference.saveToken(it1) } }
                        launch(Dispatchers.IO) {
                            refresherToken?.let { it1 ->
                                appPreference.saveToken(
                                    it1
                                )
                            }
                        }
                        updateState(DataResult.Success(LoginState.LoginRes(isSuccess = true)))
                    }
                    result.doIfFailure {
                        updateState(DataResult.Success(LoginState.LoginRes(isSuccess = false)))
                    }
                }
            )
        }
    }

    private fun refreshToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val refreshToken = appPreference.getRefreshToken().first()
            val request = RefreshTokenRequest(refreshToken = refreshToken)
            ApiCaller.safeApiCall(
                apiCall = { apiService.refreshToken(request) },
                callback = { result ->
                    result.onLoading {
                    }
                    result.doIfSuccess {
                        val token = it.accessToken
                        val refresherToken = it.refreshToken
                        launch(Dispatchers.IO) { token?.let { it1 -> appPreference.saveToken(it1) } }
                        launch(Dispatchers.IO) {
                            refresherToken?.let { it1 ->
                                appPreference.saveToken(
                                    it1
                                )
                            }
                        }
                        updateState(DataResult.Success(LoginState.LoginRes(isSuccess = true)))
                    }
                    result.doIfFailure {
                        updateState(DataResult.Success(LoginState.LoginRes(isSuccess = false)))
                    }
                }
            )
        }
    }

    override fun onTriggerEvent(event: LoginEvent) {
    }
}

sealed class LoginState {
    class LoginRes(val isSuccess: Boolean) : LoginState()
}

sealed class LoginEvent {}