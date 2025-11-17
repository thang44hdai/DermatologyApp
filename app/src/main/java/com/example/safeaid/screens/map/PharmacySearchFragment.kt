package com.example.safeaid.screens.map

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
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
import com.example.safeaid.screens.pharmacy.PharmacyDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class PharmacySearchFragment : BaseFragment<FragmentPharmacySearchBinding>() {
    private val viewModel: MapViewModel by activityViewModels()
    private lateinit var adapter: PharmacySearchAdapter
    private var allPharmacies = listOf<PharmacyResponse>()
    private var selectedFilters = mutableSetOf<String>()

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
                filterPharmacies(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Sort button
        viewBinding.btnSort.setOnClickListener {
            // Toggle sort order
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

    private fun filterPharmacies(query: String) {
        val filtered = PharmacyUtils.filterPharmacies(allPharmacies, query)
        adapter.submitList(filtered)
        updateResultCount(filtered.size)
    }

    private fun toggleFilter(filter: String) {
        if (selectedFilters.contains(filter)) {
            selectedFilters.remove(filter)
        } else {
            selectedFilters.add(filter)
        }
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = PharmacyUtils.filterPharmaciesByCriteria(
            pharmacies = allPharmacies,
            is24h = selectedFilters.contains("24/7"),
            maxDistanceKm = if (selectedFilters.contains("5 km")) 5.0 else null,
            minRating = if (selectedFilters.contains("> 4 sao")) 4.0 else null
        )

        adapter.submitList(filtered)
        updateResultCount(filtered.size)
    }

    private fun updateResultCount(count: Int) {
        viewBinding.tvResultCount.text = "Kết quả ($count)"
    }
}
