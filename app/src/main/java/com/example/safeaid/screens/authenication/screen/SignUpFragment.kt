package com.example.safeaid.screens.authenication.screen

import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentSignUpBinding
import com.example.safeaid.core.ui.BaseDialog
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.ViewUtils
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.authenication.viewmodel.LoginState
import com.example.safeaid.screens.authenication.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.getValue

class SignUpFragment() : BaseFragment<FragmentSignUpBinding>() {
    private val viewModel: LoginViewModel by activityViewModels()

    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
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
            val email = viewBinding.tvEmail.text.toString()
            val username = viewBinding.tvUsername.text.toString()
            val pw = viewBinding.tvPw.text.toString()
            val name = viewBinding.tvName.text.toString()
            viewModel.registerAccount(email = email, userName = username, name = name, pw = pw)
        }
    }

    private fun updateUi(state: DataResult<LoginState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is LoginState.Register -> {
                    if (data.isSuccess) {
                        findNavController().navigate(R.id.loginFragment)
                        Toast.makeText(
                            requireContext(),
                            "Đăng kí tài khoản thành công",
                            Toast.LENGTH_SHORT
                        ).show()
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