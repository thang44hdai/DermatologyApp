package com.example.safeaid.screens.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentHomeBinding
import com.example.safeaid.core.response.MedicineResponse
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.screens.home.adapter.BrandAdapter
import com.example.safeaid.screens.home.adapter.ProductAdapter
import com.example.safeaid.screens.home.utils.MedicineUtils
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

    private var allMedicines = listOf<MedicineResponse>()
    private var currentSearchQuery = ""
    private var sortType = SortType.NONE

    enum class SortType {
        NONE, PRICE_ASC, PRICE_DESC, NAME_ASC, NAME_DESC
    }

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        viewBinding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        viewBinding.rvProducts.adapter = medicinesAdapter
        viewBinding.rvBrands.adapter = brandAdapter

        // Setup SwipeRefreshLayout
        viewBinding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.primary)
        )

        viewModel.loadHomeData()
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> updateUi(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel._medicineResponse
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { data ->
                allMedicines = data
                applyFiltersAndSort()
            }
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
        // Search functionality
        viewBinding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                viewBinding.btnClearSearch.isVisible = s?.isNotEmpty() == true
                applyFiltersAndSort()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Clear search button
        viewBinding.btnClearSearch.setOnClickListener {
            viewBinding.edtSearch.setText("")
            viewBinding.edtSearch.clearFocus()
        }

        // Sort button
        viewBinding.btnSort.setOnClickListener {
            showSortDialog()
        }

        // See all products
        viewBinding.tvSeeAll.setOnClickListener {
            // Could navigate to a full products list screen
        }

        // Pull to refresh
        viewBinding.swipeRefresh.setOnRefreshListener {
            viewModel.loadHomeData()
            viewBinding.swipeRefresh.isRefreshing = false
        }

        // Brand click
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

    private fun applyFiltersAndSort() {
        var filtered = MedicineUtils.filterMedicines(allMedicines, currentSearchQuery)

        // Apply sort
        filtered = when (sortType) {
            SortType.PRICE_ASC -> MedicineUtils.sortByPrice(filtered, ascending = true)
            SortType.PRICE_DESC -> MedicineUtils.sortByPrice(filtered, ascending = false)
            SortType.NAME_ASC -> MedicineUtils.sortByName(filtered, ascending = true)
            SortType.NAME_DESC -> MedicineUtils.sortByName(filtered, ascending = false)
            SortType.NONE -> filtered
        }

        medicinesAdapter.bindData(filtered)
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "Mặc định",
            "Giá: Thấp đến cao",
            "Giá: Cao đến thấp",
            "Tên: A-Z",
            "Tên: Z-A"
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Sắp xếp theo")
            .setItems(sortOptions) { _, which ->
                sortType = when (which) {
                    0 -> SortType.NONE
                    1 -> SortType.PRICE_ASC
                    2 -> SortType.PRICE_DESC
                    3 -> SortType.NAME_ASC
                    4 -> SortType.NAME_DESC
                    else -> SortType.NONE
                }
                applyFiltersAndSort()
            }
            .setNegativeButton("Hủy", null)
            .show()
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
