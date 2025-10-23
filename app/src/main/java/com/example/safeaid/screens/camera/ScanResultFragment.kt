package com.example.safeaid.screens.camera

import androidx.navigation.fragment.findNavController
import com.example.dermatology.databinding.ScanResultFragmentBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick

class ScanResultFragment() : BaseFragment<ScanResultFragmentBinding>() {
    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
    }

    override fun onInitObserver() {
    }

    override fun onInitListener() {
        viewBinding.icBack.setOnDebounceClick {
            findNavController().popBackStack()
        }
    }
}