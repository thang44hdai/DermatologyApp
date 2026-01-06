package com.example.safeaid.screens.profile

import android.app.DatePickerDialog
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentEditProfileBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.onLoading
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditProfileFragment : BaseFragment<FragmentEditProfileBinding>() {
    private val profileViewModel: ProfileViewModel by viewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    
    private var selectedGender = ""
    private var selectedDateOfBirth = ""
    private var selectedAvatarFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // Image picker launcher - simple approach like CameraFragment
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            handleSelectedImage(it)
        }
    }
    
    override fun isHostFragment(): Boolean = false
    
    override fun onInit() {
        setupUI()
        loadCurrentUserData()
    }
    
    private fun setupUI() {
        // Setup gender radio buttons
        setupGenderSelection()
        
        // Setup date picker
        setupDatePicker()
        
        // Setup text watchers for validation
        setupTextWatchers()
    }
    
    private fun loadCurrentUserData() {
        homeViewModel._user.value.let { user ->
            // Load fullname
            viewBinding.edtFullName.setText(user.fullname ?: "")
            
            // Load gender and set radio button
            user.gender?.let { gender ->
                selectedGender = gender
                when (gender.lowercase()) {
                    "male", "nam" -> {
                        viewBinding.radioMale.isChecked = true
                        selectedGender = "male"
                    }
                    "female", "nữ" -> {
                        viewBinding.radioFemale.isChecked = true
                        selectedGender = "female"
                    }
                    "other", "khác" -> {
                        viewBinding.radioOther.isChecked = true
                        selectedGender = "other"
                    }
                }
            }
            
            // Load date of birth and display it
            user.dateOfBirth?.let { dateString ->
                try {
                    // Try parsing the date string (could be in different formats)
                    val date = try {
                        dateFormat.parse(dateString)
                    } catch (e: Exception) {
                        // Try alternative format if the first one fails
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateString)
                    }
                    
                    if (date != null) {
                        selectedDateOfBirth = dateFormat.format(date)
                        viewBinding.tvDateOfBirth.text = displayDateFormat.format(date)
                        viewBinding.tvDateOfBirth.setTextColor(
                            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.black)
                        )
                    }
                } catch (e: Exception) {
                    // If all parsing fails, just use the original string
                    selectedDateOfBirth = dateString
                    viewBinding.tvDateOfBirth.text = dateString
                    viewBinding.tvDateOfBirth.setTextColor(
                        androidx.core.content.ContextCompat.getColor(requireContext(), R.color.black)
                    )
                }
            }

            // Load avatar if available
            user.avatarUrl?.let { url ->
                Glide.with(requireContext())
                    .load(url)
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(viewBinding.imgAvatar)
            }
        }
        
        // Validate form after loading data
        validateForm()
    }
    
    private fun setupGenderSelection() {
        // Set custom colors for radio buttons
        val colorStateList = android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary),
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray_neutral_4)
            )
        )
        
        viewBinding.radioMale.buttonTintList = colorStateList
        viewBinding.radioFemale.buttonTintList = colorStateList
        viewBinding.radioOther.buttonTintList = colorStateList
        
        viewBinding.radioGroupGender.setOnCheckedChangeListener { _, checkedId ->
            selectedGender = when (checkedId) {
                R.id.radio_male -> "male"
                R.id.radio_female -> "female"
                R.id.radio_other -> "other"
                else -> ""
            }
        }
    }
    
    private fun setupDatePicker() {
        viewBinding.tvDateOfBirth.setOnClickListener {
            showDatePicker()
        }
    }
    
    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        
        viewBinding.edtFullName.addTextChangedListener(textWatcher)
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        
        // If date already selected, use it
        if (selectedDateOfBirth.isNotEmpty()) {
            try {
                calendar.time = dateFormat.parse(selectedDateOfBirth) ?: Date()
            } catch (e: Exception) {
                // Use current date if parsing fails
            }
        }
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.CustomDatePickerDialog,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDateOfBirth = dateFormat.format(calendar.time)
                viewBinding.tvDateOfBirth.text = displayDateFormat.format(calendar.time)
                viewBinding.tvDateOfBirth.setTextColor(
                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.black)
                )
                validateForm()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set max date to today (can't select future dates)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        
        // Set button colors
        datePickerDialog.setOnShowListener {
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary)
            )
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray_neutral_4)
            )
        }
        
        datePickerDialog.show()
    }
    
    private fun showImagePicker() {
        // Only allow specific image types that server supports
        pickImageLauncher.launch("image/*")
    }
    
    private fun handleSelectedImage(uri: Uri) {
        try {
            // Get the actual MIME type from ContentResolver
            val mimeType = requireContext().contentResolver.getType(uri)
            
            // Validate supported image types
            val supportedTypes = listOf("image/jpeg", "image/jpg", "image/png", "image/webp")
            if (mimeType !in supportedTypes) {
                Toast.makeText(
                    requireContext(),
                    "Định dạng ảnh không được hỗ trợ. Vui lòng chọn ảnh JPEG, PNG hoặc WebP",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            
            val fileExtension = when (mimeType) {
                "image/jpeg" -> ".jpg"
                "image/jpg" -> ".jpg"
                "image/png" -> ".png"
                "image/webp" -> ".webp"
                else -> ".jpg" // Default to jpg if unknown
            }
            
            // Display selected image
            Glide.with(requireContext())
                .load(uri)
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
                .into(viewBinding.imgAvatar)
            
            // Convert URI to File with proper extension
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().cacheDir, "avatar_${System.currentTimeMillis()}$fileExtension")
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            selectedAvatarFile = file
            
            Toast.makeText(
                requireContext(),
                "Đã chọn ảnh đại diện",
                Toast.LENGTH_SHORT
            ).show()
            
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Lỗi khi chọn ảnh: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun validateForm(): Boolean {
        val fullName = viewBinding.edtFullName.text.toString().trim()

        val isValid = fullName.isNotEmpty()
        
        viewBinding.btnSave.isEnabled = isValid
        viewBinding.btnSave.alpha = if (isValid) 1.0f else 0.5f
        
        return isValid
    }
    
    override fun onInitObserver() {
        profileViewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                state?.onLoading {
                    viewBinding.btnSave.isEnabled = false
                    viewBinding.progressBar.visibility = android.view.View.VISIBLE
                }
                
                state?.doIfSuccess { data ->
                    viewBinding.btnSave.isEnabled = true
                    viewBinding.progressBar.visibility = android.view.View.GONE
                    
                    when (data) {
                        is ProfileState.UpdateSuccess -> {
                            Toast.makeText(
                                requireContext(),
                                "Cập nhật thông tin thành công",
                                Toast.LENGTH_SHORT
                            ).show()
                            
                            // Reload user data in HomeViewModel
                            homeViewModel.loadHomeData()
                            
                            findNavController().navigateUp()
                        }
                    }
                }
                
                state?.doIfFailure { error ->
                    viewBinding.btnSave.isEnabled = true
                    viewBinding.progressBar.visibility = android.view.View.GONE
                    
                    Toast.makeText(
                        requireContext(),
                        error.message ?: "Có lỗi xảy ra",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
    
    override fun onInitListener() {
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().navigateUp()
        }
        
        viewBinding.btnSave.setOnDebounceClick {
            saveProfile()
        }
        
        viewBinding.avatarSection.setOnDebounceClick {
            showImagePicker()
        }
    }

    private fun saveProfile() {
        if (!validateForm()) return

        profileViewModel.onTriggerEvent(
            ProfileEvent.UpdateProfile(
                fullname = viewBinding.edtFullName.text.toString().trim(),
                gender = selectedGender.ifEmpty { null },
                dateOfBirth = selectedDateOfBirth.ifEmpty { null },
                avatarFile = selectedAvatarFile
            )
        )
    }
}