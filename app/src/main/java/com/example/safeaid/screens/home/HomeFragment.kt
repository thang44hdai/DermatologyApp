package com.example.safeaid.screens.home

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentHomeBinding
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.screens.home.adapter.BrandAdapter
import com.example.safeaid.screens.home.adapter.ProductAdapter
import com.example.safeaid.screens.medicine.MedicineDetailFragment
import com.example.safeaid.screens.pharmacy.PharmacyDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val brandAdapter = BrandAdapter(listOf())
    private val medicinesAdapter = ProductAdapter(listOf()) { medicine ->
        val bundle = Bundle()
        bundle.putSerializable(MedicineDetailFragment.ARG_MEDICINE, medicine)
        findNavController().navigate(
            R.id.action_mainScreen_to_medicineDetailFragment,
            bundle
        )
    }
    private val viewModel: HomeViewModel by activityViewModels()

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        viewBinding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        viewBinding.rvProducts.adapter = medicinesAdapter
        viewBinding.rvBrands.adapter = brandAdapter

        viewModel.loadHomeData()
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> updateUi(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel._medicineResponse
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { data -> medicinesAdapter.bindData(data) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel._user
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { data ->
                viewBinding.tvName.text = data.fullname
                if (data.avatarUrl == null) {
                    viewBinding.avatar.setImageResource(R.drawable.ic_default_avatar)
                } else {
                    com.bumptech.glide.Glide.with(requireContext())
                        .load(data.avatarUrl)
                        .circleCrop()
                        .into(viewBinding.avatar)
                }

            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.tvSeeAll.setOnClickListener {
        }

        brandAdapter.setOnClick(object : BrandAdapter.OnClickBrand {
            override fun onClick(item: PharmacyResponse) {
                val bundle = Bundle()
                bundle.putSerializable(PharmacyDetailFragment.ARG, item)
                bundle.putBoolean(PharmacyDetailFragment.IS_DIRECTION, true)
                findNavController().navigate(
                    R.id.action_mainScreen_to_pharmacyDetailFragment,
                    bundle
                )
            }

        })
    }

    private fun updateUi(state: DataResult<HomeState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is HomeState.PharmaciesList -> {
                    brandAdapter.bindData(data.data.pharmacies)
                }
            }
        }
        state?.doIfFailure { }
    }

}