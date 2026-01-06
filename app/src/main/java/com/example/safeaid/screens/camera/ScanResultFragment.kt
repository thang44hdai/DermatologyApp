package com.example.safeaid.screens.camera

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.dermatology.R
import com.example.dermatology.databinding.ScanResultFragmentBinding
import com.example.safeaid.core.response.PredictResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.core.utils.showImageZoom
import com.example.safeaid.screens.home.adapter.ProductAdapter2
import com.example.safeaid.screens.medicine.MedicineDetailFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScanResultFragment() : BaseFragment<ScanResultFragmentBinding>() {
    private var predict: PredictResponse = PredictResponse()
    private val adapter = ProductAdapter2(listOf()) { medicine ->
        val bundle = Bundle()
        bundle.putSerializable(MedicineDetailFragment.ARG_MEDICINE, medicine)
        findNavController().navigate(
            R.id.action_scanResultFragment_to_medicineDetailFragment,
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
                "${predict.data?.labelVi} (${predict.data?.labelEn})"
            
            // Setup expandable descriptions
            fullDescription = predict.data?.disease?.description ?: ""
            fullSymptoms = predict.data?.disease?.symptoms ?: ""
            fullTreatment = predict.data?.disease?.treatment ?: ""
            
            setupExpandableText(viewBinding.tvDescription, viewBinding.btnSeeMoreDescription, fullDescription)
            setupExpandableText(viewBinding.tvSymptomsDescription, viewBinding.btnSeeMoreSymptoms, fullSymptoms)
            setupExpandableText(viewBinding.tvTreatmentDescription, viewBinding.btnSeeMoreTreatment, fullTreatment)
            
            adapter.bindData(predict.data?.disease?.medicines ?: listOf())

            // Load images with loading animation
            loadImageWithAnimation(predict.data?.imageUrl, viewBinding.imv1)
            loadImageWithAnimation(predict.data?.highlightedImageUrl, viewBinding.imv2)
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
            // Collapse: show only 5 lines with animation
            button.text = "Xem thêm"
            animateTextCollapse(textView) {
                textView.maxLines = 5
                updateState(false)
            }
        } else {
            // Expand: show all lines with animation
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
        
        // Temporarily set maxLines to unlimited to measure full height
        textView.maxLines = Int.MAX_VALUE
        textView.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(textView.width, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = textView.measuredHeight
        
        // Reset to initial state
        textView.maxLines = 5
        textView.layoutParams.height = initialHeight
        
        // Create animator
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
        // Get current height
        val initialHeight = textView.height
        
        // Measure height with 5 lines
        textView.maxLines = 5
        textView.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(textView.width, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = textView.measuredHeight
        
        // Create animator
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

    override fun onInitObserver() {}

    override fun onInitListener() {
        viewBinding.icBack.setOnDebounceClick {
            findNavController().navigate(R.id.mainScreen)
        }

        viewBinding.btnChat.setOnDebounceClick {
            // Navigate to chat screen
            findNavController().navigate(R.id.chatBotFragment)
        }

        // Click to zoom images
        viewBinding.imv1.setOnDebounceClick {
            requireContext().showImageZoom(predict.data?.imageUrl)
        }

        viewBinding.imv2.setOnDebounceClick {
            requireContext().showImageZoom(predict.data?.highlightedImageUrl)
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
}