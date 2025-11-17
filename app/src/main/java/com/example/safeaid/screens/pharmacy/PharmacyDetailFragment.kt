package com.example.safeaid.screens.pharmacy

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentPharmacyDetailBinding
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.screens.home.adapter.ProductAdapter
import com.example.safeaid.screens.main.MainViewModel
import com.example.safeaid.screens.pharmacy.adapter.RateAdapter
import com.example.safeaid.screens.pharmacy.data.RateItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class PharmacyDetailFragment : BaseFragment<FragmentPharmacyDetailBinding>() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: PharmacyViewModel by viewModels()
    private var data: PharmacyResponse? = null
    private val adapter = ProductAdapter(listOf())

    companion object {
        const val ARG = "pharmacy"
        const val IS_DIRECTION = "direction"
    }

    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
        data = arguments?.getSerializable(ARG) as PharmacyResponse?
        viewBinding.rcvMedicines.adapter = adapter
        data?.id?.let { id ->
            viewModel.loadPharmacyDetail(id)
        }
        val isDirection = arguments?.getBoolean(IS_DIRECTION) ?: true
        viewBinding.btnDirection.isVisible = isDirection
        data?.let { bindPharmacyData(it) }
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                state?.doIfSuccess { s ->
                    when (s) {
                        is PharmacyState.PharmacyDetail -> {
                            val medicines = s.data.medicines ?: listOf()
                            adapter.bindData(medicines)
                        }
                    }
                }
                state?.doIfFailure { }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
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

            val rateAdapter = RateAdapter(data)

            viewBinding.rcvRate.apply {
                layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 3)
                this.adapter = rateAdapter
            }

            val url = pharmacy.images?.firstOrNull()
            if (url.isNullOrEmpty()) {
                Glide.with(requireContext()).load(android.R.color.darker_gray).into(viewBinding.imv)
            } else {
                Glide.with(requireContext()).load(url).into(viewBinding.imv)
            }

            val logo = pharmacy.logoUrl
            if (logo.isNullOrEmpty()) {
                Glide.with(requireContext()).load(android.R.color.darker_gray)
                    .into(viewBinding.logo)
            } else {
                Glide.with(requireContext()).load(logo).into(viewBinding.logo)
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