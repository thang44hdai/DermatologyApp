package com.example.safeaid.screens.authenication.screen

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dermatology.databinding.FragmentSignUpBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.authenication.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

class SignUpFragment() : BaseFragment<FragmentSignUpBinding>() {
    private val viewModel: LoginViewModel by activityViewModels()

    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
    }

    override fun onInitObserver() {
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().popBackStack()
        }
    }

}