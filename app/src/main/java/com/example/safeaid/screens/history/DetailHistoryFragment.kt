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
                button.visibility = android.view.View.VISIBLE
                textView.maxLines = 5
            } else {
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
            button.text = "Xem thêm"
            animateTextCollapse(textView) {
                textView.maxLines = 5
                updateState(false)
            }
        } else {
            button.text = "Thu gọn"
            animateTextExpand(textView) {
                textView.maxLines = Int.MAX_VALUE
                updateState(true)
            }
        }
    }

    private fun animateTextExpand(textView: TextView, onComplete: () -> Unit) {
        // Get current height
        val initialHeight = textView.height
        
        textView.maxLines = Int.MAX_VALUE
        textView.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(textView.width, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = textView.measuredHeight
        
        textView.maxLines = 5
        textView.layoutParams.height = initialHeight
        
        val animator = android.animation.ValueAnimator.ofInt(initialHeight, targetHeight)
        animator.duration = 300
        animator.interpolator = android.view.animation.DecelerateInterpolator()
        
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            textView.layoutParams.height = animatedValue
            textView.requestLayout()
        }
        
        animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                textView.layoutParams.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                onComplete()
            }
        })
        
        animator.start()
    }

    private fun animateTextCollapse(textView: TextView, onComplete: () -> Unit) {
        val initialHeight = textView.height
        
        textView.maxLines = 5
        textView.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(textView.width, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = textView.measuredHeight
        
        val animator = android.animation.ValueAnimator.ofInt(initialHeight, targetHeight)
        animator.duration = 300
        animator.interpolator = android.view.animation.DecelerateInterpolator()
        
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            textView.layoutParams.height = animatedValue
            textView.requestLayout()
        }
        
        animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                textView.layoutParams.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                onComplete()
            }
        })
        
        animator.start()
    }
}