package com.example.safeaid.screens.authenication.screen

import android.util.Log
import android.view.View
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

class LoginFragment() : BaseFragment<FragmentLoginBinding>() {
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
        viewBinding.root.setOnDebounceClick {
            ViewUtils.hideKeyboardFrom(requireContext(), viewBinding.root)
        }
        viewBinding.btnLogin.setOnDebounceClick {
            val userName = viewBinding.tvEmail.text.toString()
            val pw = viewBinding.tvPw.text.toString()
            viewModel.login(userName, pw)
        }
        viewBinding.tvSignUp.setOnDebounceClick {
            findNavController().navigate(R.id.signUpFragment)
        }

        viewBinding.btnGoogleLogin.setOnDebounceClick {
            signInWithGoogle()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.updateState(null)
    }

    private fun signInWithGoogle() {
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
                showErrorDialog("Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.")
            } catch (e: Exception) {
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

    private fun updateUi(state: DataResult<LoginState>?) {
        state?.doIfSuccess { data ->
            viewBinding.progressBar.visibility = View.GONE
            when (data) {
                is LoginState.LoginRes -> {
                    if (data.isSuccess) {
                        findNavController().navigate(R.id.mainScreen)
                    }
                }

                else -> {}
            }
        }
        state?.doIfFailure { error ->
            viewBinding.progressBar.visibility = View.GONE
            requireContext().showErrorDialog(
                message = "${error.errorCode}: ${error.message}",
            )
        }
        state?.onLoading {
            viewBinding.progressBar.visibility = View.VISIBLE
        }
    }
}
