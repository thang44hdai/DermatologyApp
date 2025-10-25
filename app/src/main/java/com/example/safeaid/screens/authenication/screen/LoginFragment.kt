package com.example.safeaid.screens.authenication.screen

import android.util.Log
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
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.authenication.viewmodel.LoginState
import com.example.safeaid.screens.authenication.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginFragment() : BaseFragment<FragmentLoginBinding>() {
    private val viewModel: LoginViewModel by activityViewModels()

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
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
            val userName = viewBinding.tvEmail.text.toString()
            val pw = viewBinding.tvPw.text.toString()
            viewModel.login(userName, pw)
        }
        viewBinding.tvSignUp.setOnDebounceClick {
            findNavController().navigate(R.id.signUpFragment)
        }
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
                            message = "Error",
                            onClickPositive = {},
                            onClickNegative = null
                        )
                        dialog.show()
                    }
                }

                else -> {}
            }
        }
        state?.doIfFailure { }
    }
}