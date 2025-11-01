package com.example.safeaid.screens.history

import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermatology.databinding.ScanResultFragmentBinding
import com.example.safeaid.core.response.Scan
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailHistoryFragment : BaseFragment<ScanResultFragmentBinding>() {
    private lateinit var data: Scan

    companion object {
        const val ARG: String = "scan"
    }

    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
        viewBinding.btnBack.isVisible = false
        viewBinding.btnMap.isVisible = false
        viewBinding.icBack.isVisible = true
        data = arguments?.getSerializable(ARG) as Scan
        Glide.with(requireContext())
            .load(data.imageUrl)
            .into(viewBinding.imv1)
        viewBinding.tvTitle.text = data.disease?.diseaseName
        viewBinding.tvSymptomsDescription.text = data.disease?.description
        viewBinding.tv1.text = "Ảnh quét " + data.diagnosisHistory?.createdAt
    }

    override fun onInitObserver() {
    }

    override fun onInitListener() {
        viewBinding.icBack.setOnDebounceClick {
            findNavController().popBackStack()
        }
    }
}