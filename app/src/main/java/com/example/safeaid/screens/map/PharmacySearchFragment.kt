package com.example.safeaid.screens.map

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentPharmacySearchBinding
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.screens.map.adapter.PharmacySearchAdapter
import com.example.safeaid.screens.map.utils.PharmacyUtils
import com.example.safeaid.screens.map.viewmodel.MapState
import com.example.safeaid.screens.map.bottom_sheet.PharmacyFilterBottomSheet
import com.example.safeaid.screens.map.bottom_sheet.PharmacyFilterCriteria
import com.example.safeaid.screens.map.viewmodel.MapViewModel
import com.example.safeaid.screens.map.viewmodel.PharmacySearchViewModel
import com.example.safeaid.screens.map.viewmodel.SortState
import com.example.safeaid.screens.pharmacy.PharmacyDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PharmacySearchFragment : BaseFragment<FragmentPharmacySearchBinding>() {
    private val viewModel: MapViewModel by activityViewModels()
    private val filterViewModel: PharmacySearchViewModel by viewModels()
    private lateinit var adapter: PharmacySearchAdapter
    private var allPharmacies = listOf<PharmacyResponse>()
    private var currentFilterCriteria = PharmacyFilterCriteria()
    private var currentSearchQuery = ""

    override fun isHostFragment(): Boolean = false

    override fun onInit() {
        setupRecyclerView()
        viewBinding.edtSearch.requestFocus()
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                updateUi(state)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewBinding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                applyAllFilters()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        lifecycleScope.launch(Dispatchers.Main) {
            viewBinding.edtSearch.setText(viewModel.filterState.first())
        }

        viewBinding.btnSort.setOnClickListener {
            adapter.submitList(listOf())
            sort()
        }

        viewBinding.btnFilter.setOnClickListener {
            showFilterBottomSheet()
        }
    }

    private fun setupRecyclerView() {
        adapter = PharmacySearchAdapter { pharmacy ->
            val bundle = Bundle().apply {
                putSerializable(PharmacyDetailFragment.ARG, pharmacy)
                putBoolean(PharmacyDetailFragment.IS_DIRECTION, true)
            }
            findNavController().navigate(
                R.id.action_pharmacySearchFragment_to_pharmacyDetailFragment,
                bundle
            )
        }
        viewBinding.rvPharmacy.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.rvPharmacy.adapter = adapter
    }

    private fun updateUi(state: DataResult<MapState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is MapState.PharmaciesNearBy -> {
                    allPharmacies = data.data
                    updateResultCount(data.data.size)
                    adapter.submitList(data.data)
                }
            }
        }
    }

    private fun applyAllFilters() {
        var filtered = PharmacyUtils.filterPharmacies(allPharmacies, currentSearchQuery)
        
        filtered = PharmacyUtils.filterPharmaciesByCriteria(
            pharmacies = filtered,
            is24h = currentFilterCriteria.is24h,
            maxDistanceKm = currentFilterCriteria.maxDistance,
            minRating = currentFilterCriteria.minRating
        )
        
        adapter.submitList(filtered)
        updateResultCount(filtered.size)
        viewModel.filter(currentSearchQuery)
    }

    private fun showFilterBottomSheet() {
        val filterSheet = PharmacyFilterBottomSheet.newInstance(currentFilterCriteria)
        filterSheet.setOnApplyFilterListener { criteria ->
            currentFilterCriteria = criteria
            applyAllFilters()
        }
        filterSheet.show(childFragmentManager, "PharmacyFilter")
    }


    private fun addFilterChip(text: String, onClose: () -> Unit) {
        val chip = com.google.android.material.chip.Chip(requireContext()).apply {
            this.text = text
            isCloseIconVisible = true
            setOnCloseIconClickListener { onClose() }
        }
        viewBinding.chipGroupActiveFilters.addView(chip)
    }

    private fun sort() {
        val query: SortState =
            if (filterViewModel.sortState == SortState.DECREASE) SortState.INCREASE else SortState.DECREASE
        val filtered = PharmacyUtils.sortByDistance(allPharmacies, query)
        adapter.submitList(filtered)
        filterViewModel.sortState = query
        if (query == SortState.DECREASE) {
            viewBinding.tvSort.text = "Xa nhất"
        } else {
            viewBinding.tvSort.text = "Gần nhất"
        }
    }

    private fun updateResultCount(count: Int) {
        viewBinding.tvResultCount.text = "Kết quả ($count)"
    }
}
