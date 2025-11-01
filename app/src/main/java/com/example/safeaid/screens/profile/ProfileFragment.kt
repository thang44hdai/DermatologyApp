package com.example.safeaid.screens.profile

import androidx.navigation.fragment.findNavController
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentProfileBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick

class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
    }

    override fun onInitObserver() {
    }

    override fun onInitListener() {
        viewBinding.btnHistory.setOnDebounceClick {
            findNavController().navigate(R.id.action_mainScreen_to_historyFragment)
        }
    }

}