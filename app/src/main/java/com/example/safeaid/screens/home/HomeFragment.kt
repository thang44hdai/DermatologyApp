package com.example.safeaid.screens.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.safeaid.screens.home.utils.BrandUtils
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
    private var allBrands = listOf<PharmacyResponse>()
    private var currentSearchQuery = ""
    private var searchMode = SearchMode.ALL

    enum class SearchMode {
        ALL, BRANDS, PRODUCTS
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

        // Setup search mode tag
        setupSearchModeTag()
        updateUIForSearchMode()

        // Setup banner
        setupBanner()

        viewModel.loadHomeData()
    }

    private fun setupBanner() {
        // Set initial health tip
        updateHealthTip()

        // Quick scan button click
        viewBinding.btnQuickScan.setOnClickListener {
            findNavController().navigate(R.id.action_mainScreen_to_cameraFragment)
        }

        // Banner click to also navigate to camera
        viewBinding.banner.setOnClickListener {
            findNavController().navigate(R.id.action_mainScreen_to_cameraFragment)
        }
    }

    private fun updateHealthTip() {
        val healthTips = listOf(
            "Mẹo: Rửa mặt 2 lần/ngày giúp da sạch và khỏe mạnh",
            "Mẹo: Sử dụng kem chống nắng hàng ngày để bảo vệ da",
            "Mẹo: Uống đủ nước giúp da luôn căng mịn và sáng khỏe",
            "Mẹo: Ngủ đủ giấc 7-8 tiếng mỗi đêm tốt cho sức khỏe da",
            "Mẹo: Tránh chạm tay lên mặt để giảm nguy cơ mụn",
            "Mẹo: Tẩy trang kỹ trước khi đi ngủ giúp da thông thoáng"
        )
        // Rotate tips based on day of week for variety
        val dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
        val tipIndex = (dayOfWeek - 1) % healthTips.size
        viewBinding.tvHealthTip.text = healthTips[tipIndex]
    }

    private fun setupSearchModeTag() {
        viewBinding.tagSearchMode.setOnClickListener {
            cycleSearchMode()
        }
    }

    private fun cycleSearchMode() {
        searchMode = when (searchMode) {
            SearchMode.ALL -> SearchMode.BRANDS
            SearchMode.BRANDS -> SearchMode.PRODUCTS
            SearchMode.PRODUCTS -> SearchMode.ALL
        }
        updateUIForSearchMode()
        applyFiltersAndSort()
    }

    private fun updateUIForSearchMode() {
        when (searchMode) {
            SearchMode.ALL -> {
                viewBinding.tagSearchMode.text = "Tất cả"
                viewBinding.edtSearch.hint = "Tìm kiếm tên thương hiệu, sản phẩm"
                viewBinding.brandsHeader.isVisible = true
                viewBinding.rvBrands.isVisible = true
                viewBinding.layoutProductsLabel.isVisible = true
                viewBinding.rvProducts.isVisible = true
            }
            SearchMode.BRANDS -> {
                viewBinding.tagSearchMode.text = "Thương hiệu"
                viewBinding.edtSearch.hint = "Tìm kiếm thương hiệu theo tên"
                viewBinding.brandsHeader.isVisible = true
                viewBinding.rvBrands.isVisible = true
                viewBinding.layoutProductsLabel.isVisible = false
                viewBinding.rvProducts.isVisible = false
            }
            SearchMode.PRODUCTS -> {
                viewBinding.tagSearchMode.text = "Sản phẩm"
                viewBinding.edtSearch.hint = "Tìm kiếm sản phẩm theo tên"
                viewBinding.brandsHeader.isVisible = false
                viewBinding.rvBrands.isVisible = false
                viewBinding.layoutProductsLabel.isVisible = true
                viewBinding.rvProducts.isVisible = true
            }
        }
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
                applyFiltersAndSort()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // See all products
        viewBinding.layoutProductsLabel.setOnClickListener {
            findNavController().navigate(R.id.action_mainScreen_to_allProductsFragment)
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
        // Filter and update brands
        when (searchMode) {
            SearchMode.ALL, SearchMode.BRANDS -> {
                val filteredBrands = BrandUtils.filterBrands(allBrands, currentSearchQuery)
                brandAdapter.bindData(filteredBrands)
            }
            SearchMode.PRODUCTS -> {
                // Hide brands section
            }
        }

        // Filter products
        when (searchMode) {
            SearchMode.ALL, SearchMode.PRODUCTS -> {
                var filteredProducts = MedicineUtils.filterMedicines(allMedicines, currentSearchQuery)
                medicinesAdapter.bindData(filteredProducts)
            }
            SearchMode.BRANDS -> {
                // Hide products section
            }
        }
    }

    private fun updateUi(state: DataResult<HomeState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is HomeState.PharmaciesList -> {
                    allBrands = data.data.pharmacies
                    applyFiltersAndSort()
                }
            }
        }
        state?.doIfFailure { }
    }
}
