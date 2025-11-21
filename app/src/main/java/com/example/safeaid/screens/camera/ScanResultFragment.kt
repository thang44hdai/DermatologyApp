package com.example.safeaid.screens.camera

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.dermatology.databinding.ScanResultFragmentBinding
import com.example.safeaid.core.response.PredictResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.camera.viewmodel.PredictState
import com.example.safeaid.screens.camera.viewmodel.PredictViewModel
import com.example.safeaid.screens.home.adapter.ProductAdapter
import com.example.safeaid.screens.main.MainViewModel
import com.example.safeaid.screens.map.viewmodel.MapViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ScanResultFragment() : BaseFragment<ScanResultFragmentBinding>() {
    private val viewModel: PredictViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val mapViewModel: MapViewModel by activityViewModels()
    private var predict: PredictResponse = PredictResponse()
    private val adapter = ProductAdapter(listOf(), null)

    companion object {
        const val argKey: String = "data"
    }

    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
        predict = arguments?.getSerializable(argKey) as PredictResponse
        viewBinding.rvProducts.adapter = adapter
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
            adapter.bindData(predict.data?.disease?.medicines ?: listOf())
            Glide.with(requireContext())
                .load(predict.data?.disease?.imageUrl)
                .into(viewBinding.imv1)
        }

        viewModel.detectBoundary(requireContext())
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { updateUi(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().navigate(R.id.mainScreen)
        }

        viewBinding.btnMap.setOnDebounceClick {
            mainViewModel.currentPage = 1
            mapViewModel.isPredicted = true
            findNavController().navigate(R.id.mainScreen)
        }
    }

    private fun updateUi(state: DataResult<PredictState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is PredictState.PredictRes -> {
                }

                is PredictState.DetectBoundaryRes -> {
                    if (!data.isLoading) {
                        viewBinding.progressBar.isVisible = false
                        viewBinding.imv2.isVisible = true
                        Glide.with(requireContext())
                            .load(data.data.imageUrl)
                            .into(viewBinding.imv2)
                    } else {
                        viewBinding.progressBar.isVisible = true
                        viewBinding.imv2.isVisible = false
                    }
                }

                else -> {}
            }
        }
        state?.doIfFailure { }
    }
}