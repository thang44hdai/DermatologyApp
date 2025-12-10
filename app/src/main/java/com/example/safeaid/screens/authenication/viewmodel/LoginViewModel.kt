package com.example.safeaid.screens.authenication.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.request.GoogleLoginRequest
import com.example.safeaid.core.request.LoginRequest
import com.example.safeaid.core.request.RefreshTokenRequest
import com.example.safeaid.core.request.RegisterRequest
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
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,
    private val appPreference: AppPreference
) : BaseViewModel<LoginState, LoginEvent>() {
    fun verifyToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = appPreference.getToken().first()
            if (!token.isEmpty()) {
                updateState(DataResult.Loading)
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
    }

    fun login(userName: String, pw: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateState(DataResult.Loading)
            val request = LoginRequest(username = userName, password = pw)
            ApiCaller.safeApiCall(
                apiCall = { apiService.login(request) },
                callback = { result ->
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
                    result.doIfFailure { error ->
                        updateState(DataResult.Error(error))
                    }
                }
            )
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateState(DataResult.Loading)
            val request = GoogleLoginRequest(idToken = idToken)
            ApiCaller.safeApiCall(
                apiCall = { apiService.googleLogin(request) },
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
                    result.doIfFailure { error ->
                        updateState(DataResult.Error(error))
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
                    result.doIfFailure { error ->
                        updateState(DataResult.Error(error))
                    }
                }
            )
        }
    }

    fun registerAccount(email: String, userName: String, name: String, pw: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val request =
                RegisterRequest(email = email, username = userName, fullname = name, password = pw)
            ApiCaller.safeApiCall(
                apiCall = { apiService.register(request) },
                callback = { result ->
                    result.doIfSuccess {
                        updateState(DataResult.Success(LoginState.Register(isSuccess = true)))
                    }
                    result.doIfFailure {
                        updateState(DataResult.Success(LoginState.Register(isSuccess = false)))
                    }
                }
            )
        }
    }

    fun clearToken() {
        viewModelScope.launch(Dispatchers.IO) {
            appPreference.saveToken("")
            appPreference.saveRefreshToken("")
        }
    }

    override fun onTriggerEvent(event: LoginEvent) {
    }
}

sealed class LoginState {
    class LoginRes(val isSuccess: Boolean) : LoginState()
    class Register(val isSuccess: Boolean) : LoginState()
}

sealed class LoginEvent {}