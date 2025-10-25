package com.example.safeaid.screens.camera

import androidx.navigation.fragment.findNavController
import com.example.dermatology.databinding.ScanResultFragmentBinding
import com.example.safeaid.core.response.PredictResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick

class ScanResultFragment() : BaseFragment<ScanResultFragmentBinding>() {
    private var predict: PredictResponse = PredictResponse()

    companion object {
        const val argKey: String = "data"
    }

    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
        predict = arguments?.getSerializable(argKey) as PredictResponse

        if (predict.success == true) {
            viewBinding.tvTitle.text = "${predict.labelVi}: ${predict.confidence}"
        }
    }

    override fun onInitObserver() {
    }

    override fun onInitListener() {
        viewBinding.icBack.setOnDebounceClick {
            findNavController().popBackStack()
        }
    }
}