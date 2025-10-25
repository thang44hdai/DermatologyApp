package com.example.safeaid.screens.camera

import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermatology.databinding.ScanResultFragmentBinding
import com.example.safeaid.core.response.PredictResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.camera.adapter.AllPredictionAdapter
import com.example.safeaid.screens.camera.viewmodel.PredictViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ScanResultFragment() : BaseFragment<ScanResultFragmentBinding>() {
    private val viewModel: PredictViewModel by activityViewModels()
    private var predict: PredictResponse = PredictResponse()
    private val adapter = AllPredictionAdapter()

    companion object {
        const val argKey: String = "data"
    }

    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
        viewBinding.rcvLabel.adapter = adapter
        predict = arguments?.getSerializable(argKey) as PredictResponse

        if (predict.success == true) {
            viewBinding.tvTitle.text = "${predict.labelVi}: ${predict.confidence}"
            adapter.updateData(predict.allPredictions ?: listOf())
        }
    }

    override fun onInitObserver() {
        viewModel.selectedImageUri
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { uri ->
                uri?.let {
                    viewBinding.imv1.setImageURI(it)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.icBack.setOnDebounceClick {
            Log.i("hihihi", "clikc back")
            findNavController().popBackStack()
        }
    }
}