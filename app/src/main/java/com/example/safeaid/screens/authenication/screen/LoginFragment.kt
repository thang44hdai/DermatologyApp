package com.example.safeaid.screens.authenication.screen

import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentLoginBinding
import com.example.safeaid.core.ui.BaseDialog
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.ui.showErrorDialog
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.ViewUtils
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.authenication.viewmodel.LoginState
import com.example.safeaid.screens.authenication.viewmodel.LoginViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import androidx.credentials.CredentialManager
import com.example.safeaid.core.ui.showInfoDialog
import com.example.safeaid.core.utils.onLoading
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import kotlinx.coroutines.launch

class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    private val viewModel: LoginViewModel by activityViewModels()
    private lateinit var credentialManager: CredentialManager

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        credentialManager = CredentialManager.create(requireContext())
        viewModel.verifyToken()
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> updateUi(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.btnLogin.setOnDebounceClick {
            if (validateInput()) {
                showLoading()
                val userName = viewBinding.tvEmail.text.toString().trim()
                val pw = viewBinding.tvPw.text.toString().trim()
                viewModel.login(userName, pw)
            }
        }
        
        viewBinding.tvSignUp.setOnDebounceClick {
            findNavController().navigate(R.id.signUpFragment)
        }

        viewBinding.btnGoogleLogin.setOnDebounceClick {
            showLoading()
            signInWithGoogle()
        }

        viewBinding.tvForgetPw.setOnDebounceClick {
            requireContext().showInfoDialog(
                title = "Quên mật khẩu",
                message = "Tính năng đặt lại mật khẩu sẽ được cập nhật trong phiên bản tiếp theo."
            )
        }

        // Password toggle functionality
        viewBinding.ivPasswordToggle.setOnDebounceClick {
            togglePasswordVisibility(viewBinding.tvPw, viewBinding.ivPasswordToggle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.updateState(null)
    }

    private fun signInWithGoogle() {
        // Update loading message for Google login
        viewBinding.tvLoadingMessage.text = "Đăng nhập Google..."
        viewBinding.tvLoadingSubtitle.text = "Đang xác thực tài khoản"
        
        val googleIdOption =
            GetSignInWithGoogleOption.Builder(getString(R.string.default_web_client_id))
                .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = requireContext()
                )
                handleSignInResult(result.credential)
            } catch (e: GetCredentialException) {
                hideLoading()
                showErrorDialog("Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.")
            } catch (e: Exception) {
                hideLoading()
                Log.e("LoginFragment", "Unexpected error during Google Sign-In", e)
                showErrorDialog("Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.")
            }
        }
    }

    private fun handleSignInResult(credential: Credential) {
        when {
            credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    Log.i("LoginFragment", "idToken$idToken")
                    if (idToken != null) {
                        Log.d("LoginFragment", "Google Sign-In successful")
                        viewModel.googleLogin(idToken)
                    } else {
                        showErrorDialog("Không thể lấy ID token từ Google. Vui lòng thử lại.")
                    }
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e("LoginFragment", "Failed to parse Google ID token", e)
                    showErrorDialog("Lỗi xử lý thông tin đăng nhập. Vui lòng thử lại.")
                }
            }

            else -> {
                Log.w("LoginFragment", "Unexpected credential type")
                showErrorDialog("Loại xác thực không được hỗ trợ.")
            }
        }
    }

    private fun showErrorDialog(message: String) {
        requireContext().showErrorDialog(
            title = "Lỗi đăng nhập",
            message = message
        )
    }

    private fun validateInput(): Boolean {
        val email = viewBinding.tvEmail.text.toString().trim()
        val password = viewBinding.tvPw.text.toString().trim()
        var isValid = true

        // Clear previous errors
        clearAllErrors()

        // Email/Username validation
        if (email.isEmpty()) {
            showFieldError(viewBinding.tvEmailError, "Vui lòng nhập email hoặc tên đăng nhập")
            isValid = false
        }

        // Password validation
        if (password.isEmpty()) {
            showFieldError(viewBinding.tvPasswordError, "Vui lòng nhập mật khẩu")
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
        viewBinding.tvPasswordError.visibility = View.GONE
    }

    private fun togglePasswordVisibility(editText: EditText, toggleIcon: ImageView) {
        if (editText.inputType == (android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // Show password
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleIcon.setImageResource(R.drawable.ic_eye_on)
        } else {
            // Hide password
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleIcon.setImageResource(R.drawable.ic_eye_off)
        }
        // Move cursor to end
        editText.setSelection(editText.text.length)
    }

    private fun showLoading() {
        viewBinding.loadingOverlay.visibility = View.VISIBLE
        viewBinding.scrollView.alpha = 0.5f
        viewBinding.btnLogin.isEnabled = false
        viewBinding.btnGoogleLogin.isEnabled = false
    }

    private fun hideLoading() {
        viewBinding.loadingOverlay.visibility = View.GONE
        viewBinding.scrollView.alpha = 1.0f
        viewBinding.btnLogin.isEnabled = true
        viewBinding.btnGoogleLogin.isEnabled = true
    }

    private fun updateUi(state: DataResult<LoginState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is LoginState.LoginRes -> {
                    if (data.isSuccess) {
                        // Show success message briefly before navigation
                        viewBinding.tvLoadingMessage.text = "Đăng nhập thành công!"
                        viewBinding.tvLoadingSubtitle.text = "Chuyển hướng..."
                        
                        // Delay navigation for better UX
                        viewBinding.root.postDelayed({
                            findNavController().navigate(R.id.mainScreen)
                            hideLoading()
                        }, 1000)
                    }
                }

                else -> {}
            }
        }
        state?.doIfFailure { error ->
            hideLoading()
            requireContext().showErrorDialog(
                title = "Đăng nhập thất bại",
                message = error.message ?: "Có lỗi xảy ra. Vui lòng thử lại."
            )
        }
        state?.onLoading {
            showLoading()
        }
    }
}
