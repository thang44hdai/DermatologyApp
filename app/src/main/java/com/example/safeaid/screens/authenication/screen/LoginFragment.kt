package com.example.safeaid.screens.authenication.screen

import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentLoginBinding
import com.example.safeaid.core.ui.BaseDialog
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.ViewUtils
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.authenication.viewmodel.LoginState
import com.example.safeaid.screens.authenication.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginFragment() : BaseFragment<FragmentLoginBinding>() {
    private val viewModel: LoginViewModel by activityViewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task)
    }

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

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

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Signed in successfully
            account?.idToken?.let { idToken ->
                viewModel.googleLogin(idToken)
            } ?: run {
                showErrorDialog("Không thể lấy thông tin từ Google. Vui lòng thử lại.")
            }
        } catch (e: ApiException) {
            when (e.statusCode) {
                12501 -> {
                    // User cancelled
                    // Do nothing
                }
                else -> {
                    showErrorDialog("Đăng nhập Google thất bại: ${e.message}")
                }
            }
        }
    }

    private fun showErrorDialog(message: String) {
        val dialog = BaseDialog(requireContext())
        dialog.setView(
            title = "Thông báo",
            message = message,
            onClickPositive = {},
            onClickNegative = null
        )
        dialog.show()
    }

    private fun updateUi(state: DataResult<LoginState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is LoginState.LoginRes -> {
                    if (data.isSuccess) {
                        findNavController().navigate(R.id.mainScreen)
                    } else {
                        val dialog = BaseDialog(requireContext())
                        dialog.setView(
                            title = "Thông báo",
                            message = "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.",
                            onClickPositive = {},
                            onClickNegative = null
                        )
                        dialog.show()
                    }
                }

                else -> {}
            }
        }
        state?.doIfFailure { 
            showErrorDialog("Có lỗi xảy ra. Vui lòng thử lại.")
        }
    }
}
