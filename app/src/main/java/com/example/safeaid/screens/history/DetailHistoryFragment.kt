package com.example.safeaid.screens.history

import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.dermatology.R
import com.example.dermatology.databinding.ScanResultFragmentBinding
import com.example.safeaid.core.response.Scan
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.core.utils.showImageZoom
import com.example.safeaid.core.utils.toCustomDateFormat
import com.example.safeaid.screens.home.adapter.ProductAdapter2
import com.example.safeaid.screens.medicine.MedicineDetailFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailHistoryFragment : BaseFragment<ScanResultFragmentBinding>() {
    private lateinit var data: Scan
    private val adapter = ProductAdapter2(listOf()) { medicine ->
        val bundle = Bundle()
        bundle.putSerializable(MedicineDetailFragment.ARG_MEDICINE, medicine)
        findNavController().navigate(
            R.id.action_detailHistoryFragment_to_medicineDetailFragment,
            bundle
        )
    }

    // Variables for expand/collapse functionality
    private var isDescriptionExpanded = false
    private var isSymptomsExpanded = false
    private var isTreatmentExpanded = false
    private var fullDescription = ""
    private var fullSymptoms = ""
    private var fullTreatment = ""

    companion object {
        const val ARG: String = "scan"
    }

    override fun isHostFragment(): Boolean {
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onInit() {
        viewBinding.icBack.isVisible = false
        viewBinding.btnChat.isVisible = false
        viewBinding.btnBack.isVisible = true
        data = arguments?.getSerializable(ARG) as Scan
        loadImageWithAnimation(data?.imageUrl, viewBinding.imv1)
        loadImageWithAnimation(data?.highlightedImageUrl, viewBinding.imv2)
        viewBinding.tvTitle.text = "${data?.disease?.diseaseName}"
        
        // Setup expandable descriptions
        fullDescription = data?.disease?.description ?: ""
        fullSymptoms = data?.disease?.symptoms ?: ""
        fullTreatment = data?.disease?.treatment ?: ""
        
        setupExpandableText(viewBinding.tvDescription, viewBinding.btnSeeMoreDescription, fullDescription)
        setupExpandableText(viewBinding.tvSymptomsDescription, viewBinding.btnSeeMoreSymptoms, fullSymptoms)
        setupExpandableText(viewBinding.tvTreatmentDescription, viewBinding.btnSeeMoreTreatment, fullTreatment)
        
        viewBinding.tv1.text = "Ảnh quét lúc " + data.scanDate?.toCustomDateFormat()
        viewBinding.rvProducts.adapter = adapter
        adapter.bindData(data?.disease?.medicines ?: listOf())
    }

    override fun onInitObserver() {
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().popBackStack()
        }

        viewBinding.imv1.setOnDebounceClick {
            requireContext().showImageZoom(data.imageUrl)
        }

        viewBinding.imv2.setOnDebounceClick {
            requireContext().showImageZoom(data.highlightedImageUrl)
        }

        // Expand/collapse click listeners
        viewBinding.btnSeeMoreDescription.setOnDebounceClick {
            toggleTextExpansion(
                viewBinding.tvDescription,
                viewBinding.btnSeeMoreDescription,
                fullDescription,
                isDescriptionExpanded
            ) { isDescriptionExpanded = it }
        }

        viewBinding.btnSeeMoreSymptoms.setOnDebounceClick {
            toggleTextExpansion(
                viewBinding.tvSymptomsDescription,
                viewBinding.btnSeeMoreSymptoms,
                fullSymptoms,
                isSymptomsExpanded
            ) { isSymptomsExpanded = it }
        }

        viewBinding.btnSeeMoreTreatment.setOnDebounceClick {
            toggleTextExpansion(
                viewBinding.tvTreatmentDescription,
                viewBinding.btnSeeMoreTreatment,
                fullTreatment,
                isTreatmentExpanded
            ) { isTreatmentExpanded = it }
        }
    }

    private fun loadImageWithAnimation(url: String?, imageView: ImageView) {
        Glide.with(requireContext())
            .load(url)
            .placeholder(R.drawable.ic_image_error)
            .error(R.drawable.ic_image_error)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }

    private fun setupExpandableText(textView: TextView, button: TextView, fullText: String) {
        textView.text = fullText
        
        textView.post {
            val lineCount = textView.lineCount
            if (lineCount > 5) {
                // Text is longer than 5 lines, show "Xem thêm" button
                button.visibility = android.view.View.VISIBLE
                textView.maxLines = 5
            } else {
                // Text fits in 5 lines or less, hide "Xem thêm" button
                button.visibility = android.view.View.GONE
            }
        }
    }

    private fun toggleTextExpansion(
        textView: TextView,
        button: TextView,
        fullText: String,
        isExpanded: Boolean,
        updateState: (Boolean) -> Unit
    ) {
        if (isExpanded) {
            // Collapse: show only 5 lines
            textView.maxLines = 5
            button.text = "Xem thêm"
            updateState(false)
        } else {
            // Expand: show all lines
            textView.maxLines = Int.MAX_VALUE
            button.text = "Thu gọn"
            updateState(true)
        }
    }
}