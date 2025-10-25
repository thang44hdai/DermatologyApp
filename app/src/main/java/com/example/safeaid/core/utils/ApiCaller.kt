package com.example.safeaid.core.utils

import retrofit2.Response
import java.net.SocketTimeoutException


object ApiCaller {
    fun <T> handleResponse(
        result: Response<T>,
        callback: (DataResult<T>) -> Unit
    ) {
        try {
            if (result.isSuccessful) {
                callback(DataResult.Success(result.body()!!))
            } else {
                callback(
                    DataResult.Error(
                        ErrorResponse(
                            "1", result.errorBody()?.charStream()?.readText()
                                ?: result.message(),
                            errorCode = result.code()
                        )
                    )
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()

            callback(
                DataResult.Error(
                    ErrorResponse(
                        "1",
                        result.errorBody()?.string() ?: "",
                        result.code()
                    )
                )
            )
        }
    }

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>,
        callback: (DataResult<T>) -> Unit
    ) {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                callback(DataResult.Success(response.body()!!))
            } else {
                callback(
                    DataResult.Error(
                        ErrorResponse(
                            "API_ERROR",
                            response.errorBody()?.charStream()?.readText() ?: response.message(),
                            errorCode = response.code()
                        )
                    )
                )
            }
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
            callback(
                DataResult.Error(
                    ErrorResponse(
                        "TIMEOUT",
                        "Request timeout. Please try again.",
                        0
                    )
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            callback(
                DataResult.Error(
                    ErrorResponse(
                        "NETWORK_ERROR",
                        e.localizedMessage ?: "Unknown network error",
                        0
                    )
                )
            )
        }
    }
}