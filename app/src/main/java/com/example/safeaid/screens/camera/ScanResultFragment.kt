package com.example.safeaid.screens.camera

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.dermatology.R
import com.example.dermatology.databinding.ScanResultFragmentBinding
import com.example.safeaid.core.response.PredictResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.home.adapter.ProductAdapter
import com.example.safeaid.screens.main.MainViewModel
import com.example.safeaid.screens.map.viewmodel.MapViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScanResultFragment() : BaseFragment<ScanResultFragmentBinding>() {
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
                "${predict.data?.labelVi} (${predict.data?.labelEn})"
            viewBinding.tvDescription.text = predict.data?.disease?.description
            viewBinding.tvSymptomsDescription.text = predict.data?.disease?.symptoms
            viewBinding.tvTreatmentDescription.text = predict.data?.disease?.treatment
            adapter.bindData(predict.data?.disease?.medicines ?: listOf())
            
            // Load images with loading animation
            loadImageWithAnimation(predict.data?.imageUrl, viewBinding.imv1)
            loadImageWithAnimation(predict.data?.highlightedImageUrl, viewBinding.imv2)
        }
    }

    private fun loadImageWithAnimation(url: String?, imageView: ImageView) {
        Glide.with(requireContext())
            .load(url)
            .placeholder(R.drawable.ic_loading_placeholder)
            .error(R.drawable.ic_image_error)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }

    private fun showImageZoom(imageUrl: String?) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image_zoom)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )

        val imageView = dialog.findViewById<ImageView>(R.id.img_zoom)
        val btnClose = dialog.findViewById<ImageView>(R.id.btn_close)

        Glide.with(requireContext())
            .load(imageUrl)
            .into(imageView)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
            showImageZoom(predict.data?.imageUrl)
        }

        viewBinding.imv2.setOnDebounceClick {
            showImageZoom(predict.data?.highlightedImageUrl)
        }
    }
}