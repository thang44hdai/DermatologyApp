package com.example.safeaid.screens.camera.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.response.DetectBoundaryResponse
import com.example.safeaid.core.response.PredictResponse
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.core.utils.ApiCaller
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.ErrorResponse
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.screens.history.HistoryState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PredictViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<PredictState, PredictEvent>() {

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
//    val selectedImageUri = _selectedImageUri.asStateFlow()

    private var selectedImageUri: Uri? = null
    private var selectedImageFile: File? = null

    private fun setSelectedImage(uri: Uri) {
        _selectedImageUri.value = uri
    }

    fun predict(imageUri: Uri? = null, imageFile: File? = null, context: Context) {
        if (imageUri == null && imageFile == null) {
            return
        }

        updateState(DataResult.Loading)

        if (imageFile != null) {
            selectedImageFile = imageFile
            selectedImageUri = null
        } else if (imageUri != null) {
            selectedImageUri = imageUri
            selectedImageFile = null
            setSelectedImage(imageUri)
        }

        viewModelScope.launch {
            try {
                val filePart = when {
                    imageFile != null -> {
                        val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
                    }

                    imageUri != null -> {
                        val inputStream = context.contentResolver.openInputStream(imageUri)
                        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
                        tempFile.outputStream().use { output ->
                            inputStream?.copyTo(output)
                        }
                        val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                    }

                    else -> null
                }

                filePart?.let { part ->
                    ApiCaller.safeApiCall(
                        apiCall = { apiService.predictImage(part) },
                        callback = { result ->
                            result.doIfSuccess {
                                updateState(DataResult.Success(PredictState.PredictRes(it)))
                            }
                            result.doIfFailure {
                                updateState(
                                    DataResult.Error(
                                        ErrorResponse(
                                            message = "Lỗi truy cập dữ liệu",
                                            errorCode = 0,
                                            errorType = ""
                                        )
                                    )
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                updateState(
                    DataResult.Error(
                        ErrorResponse(
                            message = "Lỗi truy cập dữ liệu local",
                            errorCode = 0,
                            errorType = ""
                        )
                    )
                )
            }
        }
    }

    override fun onTriggerEvent(event: PredictEvent) {
    }
}

sealed class PredictState {
    class PredictRes(val data: PredictResponse) : PredictState()
    class DetectBoundaryRes(val isLoading: Boolean = true, val data: DetectBoundaryResponse) :
        PredictState()
}

sealed class PredictEvent