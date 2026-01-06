package com.example.safeaid.screens.profile

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.response.RegisterResponse
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.core.utils.ApiCaller
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<ProfileState, ProfileEvent>() {

    fun updateProfile(
        fullname: String?,
        gender: String?,
        dateOfBirth: String?,
        avatarFile: java.io.File?
    ) {
        updateState(DataResult.Loading)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("ProfileViewModel", "Updating profile - fullname: $fullname, gender: $gender, dateOfBirth: $dateOfBirth")
                Log.d("ProfileViewModel", "Avatar file: ${avatarFile?.name}, size: ${avatarFile?.length()}, extension: ${avatarFile?.extension}")
                
                // Prepare form data
                val fullnameBody = fullname?.let { 
                    okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), it)
                }
                val genderBody = gender?.let { 
                    okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), it)
                }
                val dateOfBirthBody = dateOfBirth?.let { 
                    okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), it)
                }
                
                // Prepare avatar file with correct MIME type
                val avatarPart = avatarFile?.let { file ->
                    // Determine MIME type based on file extension
                    val mimeType = when (file.extension.lowercase()) {
                        "jpg", "jpeg" -> "image/jpeg"
                        "png" -> "image/png"
                        "webp" -> "image/webp"
                        else -> "image/jpeg" // Default to jpeg
                    }
                    
                    val requestFile = RequestBody.create(
                        mimeType.toMediaTypeOrNull(),
                        file
                    )
                    MultipartBody.Part.createFormData("avatar", file.name, requestFile)
                }
                
                ApiCaller.safeApiCall(
                    apiCall = { 
                        apiService.updateProfile(
                            fullname = fullnameBody,
                            gender = genderBody,
                            dateOfBirth = dateOfBirthBody,
                            avatar = avatarPart
                        )
                    },
                    callback = { result ->
                        result.doIfSuccess {
                            updateState(DataResult.Success(ProfileState.UpdateSuccess(it)))
                            Log.d("ProfileViewModel", "Profile updated successfully: $it")
                        }
                        result.doIfFailure { error ->
                            Log.e("ProfileViewModel", "Profile update failed: ${error.message}")
                            updateState(DataResult.Error(error))
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error preparing profile data: ${e.message}")
            }
        }
    }

    override fun onTriggerEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.UpdateProfile -> updateProfile(
                event.fullname,
                event.gender,
                event.dateOfBirth,
                event.avatarFile
            )
        }
    }
}

sealed class ProfileState {
    class UpdateSuccess(val data: RegisterResponse) : ProfileState()
}

sealed class ProfileEvent {
    class UpdateProfile(
        val fullname: String?,
        val gender: String?,
        val dateOfBirth: String?,
        val avatarFile: java.io.File?
    ) : ProfileEvent()
}