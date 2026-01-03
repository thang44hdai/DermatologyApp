package com.example.safeaid.screens.history

import android.os.Build
import android.widget.ImageView
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
import com.example.safeaid.core.utils.toCustomDateFormat
import com.example.safeaid.screens.home.adapter.ProductAdapter2
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailHistoryFragment : BaseFragment<ScanResultFragmentBinding>() {
    private lateinit var data: Scan
    private val adapter = ProductAdapter2(listOf(), null)

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
        viewBinding.layoutImv.isVisible = false
        data = arguments?.getSerializable(ARG) as Scan
        loadImageWithAnimation(data?.imageUrl, viewBinding.imv1)
        loadImageWithAnimation(data?.highlightedImageUrl, viewBinding.imv2)
        viewBinding.tvTitle.text = "${data?.disease?.diseaseName}"
        viewBinding.tvDescription.text = "${data?.disease?.description}"
        viewBinding.tvSymptomsDescription.text = "${data?.disease?.symptoms}"
        viewBinding.tvTreatmentDescription.text = "${data?.disease?.treatment}"
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
    }

    private fun loadImageWithAnimation(url: String?, imageView: ImageView) {
        Glide.with(requireContext())
            .load(url)
            .placeholder(R.drawable.ic_image_error)
            .error(R.drawable.ic_image_error)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }
}