package com.example.safeaid.screens.pharmacy

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentPharmacyDetailBinding
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.screens.main.MainViewModel
import com.example.safeaid.screens.pharmacy.adapter.RateAdapter
import com.example.safeaid.screens.pharmacy.data.RateItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PharmacyDetailFragment : BaseFragment<FragmentPharmacyDetailBinding>() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private var data: PharmacyResponse? = null

    companion object {
        const val ARG = "pharmacy"
        const val IS_DIRECTION = "direction"
    }

    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
        data = arguments?.getSerializable(ARG) as PharmacyResponse?
        val isDirection = arguments?.getBoolean(IS_DIRECTION) ?: true
        viewBinding.btnDirection.isVisible = isDirection
        data?.let { bindPharmacyData(it) }
    }

    override fun onInitObserver() {
    }

    override fun onInitListener() {
    }

    private fun bindPharmacyData(pharmacy: PharmacyResponse) {
        with(viewBinding) {
            tvName.text = pharmacy.name ?: "Tên hiệu thuốc không xác định"
            tvAddress.text = pharmacy.address ?: "Địa chỉ chưa cập nhật"
            val distance = pharmacy.distanceKm ?: "Chưa xác định"
            val rating = pharmacy.ratings ?: "Chưa có đánh giá"

            val data = listOf(
                RateItem(R.drawable.ic_location_pharmacy, "Km", "$distance"),
                RateItem(R.drawable.ic_star_pharmacy, "Đánh giá", "$rating"),
                RateItem(R.drawable.ic_milk, "Sản phẩm", "$100+")
            )

            val adapter = RateAdapter(data)

            viewBinding.rcvRate.apply {
                layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 3)
                this.adapter = adapter
            }

            btnDirection.setOnClickListener {
                mainViewModel.currentPage = 1
                findNavController().navigate(R.id.mainScreen)
            }

            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

}