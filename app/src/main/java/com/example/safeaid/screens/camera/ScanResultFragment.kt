package com.example.safeaid.screens.camera

import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.dermatology.databinding.ScanResultFragmentBinding
import com.example.safeaid.core.response.PredictResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.camera.viewmodel.PredictViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ScanResultFragment() : BaseFragment<ScanResultFragmentBinding>() {
    private val viewModel: PredictViewModel by viewModels()
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
            val confidencePercent = try {
                val value = predict.data?.confidence?.substring(
                    1,
                    predict.data!!.confidence?.length?.minus(1) ?: 0
                )
                    ?.toDoubleOrNull()
                if (value != null) String.format("%.2f%%", value * 100)
                else ""
            } catch (e: Exception) {
                ""
            }
            viewBinding.tvTitle.text =
                "${predict.data?.labelVi} (${predict.data?.labelEn}): ${confidencePercent}"
            viewBinding.tvDescription.text = predict.data?.disease?.description
            viewBinding.tvSymptomsDescription.text = predict.data?.disease?.symptoms
            viewBinding.tvTreatmentDescription.text = predict.data?.disease?.treatment
            Glide.with(requireContext())
                .load(predict.data?.disease?.imageUrl)
                .into(viewBinding.imv1)
        }
    }

    override fun onInitObserver() {
//        viewModel.selectedImageUri
//            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
//            .onEach { uri ->
//                uri?.let {
//                    viewBinding.imv1.setImageURI(it)
//                }
//            }
//            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().navigate(R.id.mainScreen)
        }
    }
}