package com.example.safeaid.screens.authenication.screen

import android.app.DatePickerDialog
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentSignUpBinding
import com.example.safeaid.core.ui.BaseDialog
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.ui.showErrorDialog
import com.example.safeaid.core.ui.showInfoDialog
import com.example.safeaid.core.ui.showSuccessDialog
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.KeyboardUtils
import com.example.safeaid.core.utils.ViewUtils
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.authenication.viewmodel.LoginState
import com.example.safeaid.screens.authenication.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.*

class SignUpFragment : BaseFragment<FragmentSignUpBinding>() {
    private val viewModel: LoginViewModel by activityViewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("vi"))

    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
        setupGenderDropdown()
        setupDatePicker()

        // Setup hide keyboard on touch outside
        KeyboardUtils.setupHideKeyboardOnTouchOutside(
            this,
            viewBinding.root
        )
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> updateUi(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.root.setOnDebounceClick {
            ViewUtils.hideKeyboardFrom(requireContext(), viewBinding.root)
        }
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().popBackStack()
        }

        viewBinding.btnSignUp.setOnDebounceClick {
            if (validateInput()) {
                showLoading()
                val email = viewBinding.tvEmail.text.toString().trim()
                val username = viewBinding.tvUsername.text.toString().trim()
                val pw = viewBinding.tvPw.text.toString().trim()
                val name = viewBinding.tvName.text.toString().trim()
                viewModel.registerAccount(email = email, userName = username, name = name, pw = pw)
            }
        }

        viewBinding.tvLogin.setOnDebounceClick {
            findNavController().popBackStack()
        }

        viewBinding.tvDate.setOnDebounceClick {
            showDatePicker()
        }

        // Password toggle functionality
        viewBinding.ivPasswordToggle.setOnDebounceClick {
            togglePasswordVisibility(viewBinding.tvPw, viewBinding.ivPasswordToggle)
        }

        viewBinding.ivConfirmPasswordToggle.setOnDebounceClick {
            togglePasswordVisibility(viewBinding.tvConfirmPw, viewBinding.ivConfirmPasswordToggle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.updateState(null)
    }

    private fun setupGenderDropdown() {
        viewBinding.tvGender.setOnDebounceClick {
            showGenderDialog()
        }

        // Make the entire gender container clickable
        viewBinding.tvGender.parent.parent.let { parent ->
            if (parent is android.view.ViewGroup) {
                parent.setOnDebounceClick {
                    showGenderDialog()
                }
            }
        }
    }

    private fun showGenderDialog() {
        val genders = arrayOf("Nam", "Nữ", "Khác")

        val builder = AlertDialog.Builder(requireContext())
        builder
            .setTitle("Chọn giới tính")
            .setItems(genders) { dialog, which ->
                viewBinding.tvGender.text = genders[which]
                viewBinding.tvGender.setTextColor(resources.getColor(R.color.black, null))
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.red_600)
        )
    }

    private fun setupDatePicker() {
        viewBinding.tvDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.CustomDatePickerDialog,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                viewBinding.tvDate.setText(dateFormat.format(selectedDate.time))
            },
            year, month, day
        )

        // Set max date to today (user must be born before today)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        // Set min date to 100 years ago
        val minCalendar = Calendar.getInstance()
        minCalendar.add(Calendar.YEAR, -100)
        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis

        datePickerDialog.show()
    }

    private fun validateInput(): Boolean {
        val email = viewBinding.tvEmail.text.toString().trim()
        val name = viewBinding.tvName.text.toString().trim()
        val username = viewBinding.tvUsername.text.toString().trim()
        val password = viewBinding.tvPw.text.toString().trim()
        val confirmPassword = viewBinding.tvConfirmPw.text.toString().trim()
        val gender = viewBinding.tvGender.text.toString().trim()
        val date = viewBinding.tvDate.text.toString().trim()

        var isValid = true

        // Clear all previous errors
        clearAllErrors()

        // Email validation
        if (email.isEmpty()) {
            showFieldError(viewBinding.tvEmailError, "Vui lòng nhập email")
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showFieldError(viewBinding.tvEmailError, "Email không hợp lệ")
            isValid = false
        }

        // Name validation
        if (name.isEmpty()) {
            showFieldError(viewBinding.tvNameError, "Vui lòng nhập họ và tên")
            isValid = false
        } else if (name.length < 2) {
            showFieldError(viewBinding.tvNameError, "Tên phải có ít nhất 2 ký tự")
            isValid = false
        }

        // Username validation
        if (username.isEmpty()) {
            showFieldError(viewBinding.tvUsernameError, "Vui lòng nhập tên đăng nhập")
            isValid = false
        } else if (username.length < 3) {
            showFieldError(viewBinding.tvUsernameError, "Tên đăng nhập phải có ít nhất 3 ký tự")
            isValid = false
        }

        // Password validation
        if (password.isEmpty()) {
            showFieldError(viewBinding.tvPasswordError, "Vui lòng nhập mật khẩu")
            isValid = false
        } else if (password.length < 6) {
            showFieldError(viewBinding.tvPasswordError, "Mật khẩu phải có ít nhất 6 ký tự")
            isValid = false
        }

        // Confirm password validation
        if (confirmPassword.isEmpty()) {
            showFieldError(viewBinding.tvConfirmPasswordError, "Vui lòng xác nhận mật khẩu")
            isValid = false
        } else if (password != confirmPassword) {
            showFieldError(viewBinding.tvConfirmPasswordError, "Mật khẩu xác nhận không khớp")
            isValid = false
        }

        // Terms validation
        if (!viewBinding.cbTerms.isChecked) {
            requireContext().showErrorDialog(
                title = "Điều khoản sử dụng",
                message = "Vui lòng đồng ý với điều khoản sử dụng để tiếp tục"
            )
            isValid = false
        }

        return isValid
    }

    private fun showFieldError(errorTextView: TextView, message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
    }

    private fun clearAllErrors() {
        viewBinding.tvEmailError.visibility = View.GONE
        viewBinding.tvNameError.visibility = View.GONE
        viewBinding.tvUsernameError.visibility = View.GONE
        viewBinding.tvPasswordError.visibility = View.GONE
        viewBinding.tvConfirmPasswordError.visibility = View.GONE
    }

    private fun togglePasswordVisibility(editText: EditText, toggleIcon: ImageView) {
        if (editText.inputType == (android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // Show password
            editText.inputType =
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleIcon.setImageResource(com.example.dermatology.R.drawable.ic_eye_on)
        } else {
            // Hide password
            editText.inputType =
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleIcon.setImageResource(com.example.dermatology.R.drawable.ic_eye_off)
        }
        // Move cursor to end
        editText.setSelection(editText.text.length)
    }

    private fun showLoading() {
        viewBinding.loadingOverlay.visibility = View.VISIBLE
        viewBinding.scrollView.alpha = 0.5f
        viewBinding.btnSignUp.isEnabled = false
    }

    private fun hideLoading() {
        viewBinding.loadingOverlay.visibility = View.GONE
        viewBinding.scrollView.alpha = 1.0f
        viewBinding.btnSignUp.isEnabled = true
    }

    private fun updateUi(state: DataResult<LoginState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is LoginState.Register -> {
                    hideLoading()
                    if (data.isSuccess) {
                        requireContext().showSuccessDialog(
                            title = "Đăng ký thành công",
                            message = "Tài khoản của bạn đã được tạo thành công. Vui lòng đăng nhập để tiếp tục."
                        ) {
                            findNavController().popBackStack()
                        }
                    } else {
                        requireContext().showErrorDialog(
                            title = "Đăng ký thất bại",
                            message = "Không thể tạo tài khoản. Vui lòng thử lại."
                        )
                    }
                }

                else -> {}
            }
        }
        state?.doIfFailure { error ->
            hideLoading()
            requireContext().showErrorDialog(
                title = "Lỗi đăng ký",
                message = error.message ?: "Có lỗi xảy ra. Vui lòng thử lại."
            )
        }
    }
}