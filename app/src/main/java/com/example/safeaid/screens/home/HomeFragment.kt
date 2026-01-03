package com.example.safeaid.screens.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentHomeBinding
import com.example.safeaid.core.response.Brand
import com.example.safeaid.core.response.CategoryResponse
import com.example.safeaid.core.response.MedicineResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.screens.home.adapter.BrandNewAdapter
import com.example.safeaid.screens.home.adapter.CategoryAdapter
import com.example.safeaid.screens.home.adapter.ProductAdapter
import com.example.safeaid.screens.home.utils.MedicineUtils
import com.example.safeaid.screens.home.viewmodel.BrandViewModel
import com.example.safeaid.screens.medicine.MedicineDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val brandNewAdapter = BrandNewAdapter(listOf()) { brand ->
        // Navigate to brand detail
        val bundle = Bundle()
        bundle.putString("brand_id", brand.id.toString())
        findNavController().navigate(
            R.id.action_mainScreen_to_brandDetailFragment,
            bundle
        )
    }
    private val medicinesAdapter = ProductAdapter(listOf()) { medicine ->
        val bundle = Bundle()
        bundle.putSerializable(MedicineDetailFragment.ARG_MEDICINE, medicine)
        findNavController().navigate(
            R.id.action_mainScreen_to_medicineDetailFragment,
            bundle
        )
    }
    private val categoryAdapter = CategoryAdapter(listOf()) { category ->
        // Navigate to category detail
        val bundle = Bundle()
        bundle.putString("category_id", category.id.toString())
        findNavController().navigate(
            R.id.action_mainScreen_to_categoryDetailFragment,
            bundle
        )
    }
    private val viewModel: HomeViewModel by activityViewModels()
    private val brandViewModel: BrandViewModel by viewModels()

    private var allMedicines = listOf<MedicineResponse>()
    private var allBrands = listOf<Brand>()
    private var allCategories = listOf<CategoryResponse>()
    private var currentSearchQuery = ""
    private var searchMode = SearchMode.ALL
    private var isProductsExpanded = false
    private var isCategoriesExpanded = false
    private val INITIAL_PRODUCT_COUNT = 6
    private val INITIAL_CATEGORY_COUNT = 6

    enum class SearchMode {
        ALL, BRANDS, PRODUCTS
    }

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        viewBinding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        viewBinding.rvProducts.adapter = medicinesAdapter
        viewBinding.rvBrands.adapter = brandNewAdapter
        viewBinding.rvCategories.adapter = categoryAdapter

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
        brandViewModel.loadBrands()
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

        viewModel._categories
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { data ->
                allCategories = data
                updateCategoriesDisplay()
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        // Observe brands from BrandViewModel
        brandViewModel.brands
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { data ->
                allBrands = data.brands
                applyFiltersAndSort()
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
            brandViewModel.loadBrands()
            viewBinding.swipeRefresh.isRefreshing = false
        }

        // Brand click - removed old adapter click listener

        // Show more/less button for products
        viewBinding.btnShowMore.setOnClickListener {
            isProductsExpanded = !isProductsExpanded
            applyFiltersAndSort()
            updateShowMoreButton()
        }

        // Show more/less button for categories
        viewBinding.btnShowMoreCategories.setOnClickListener {
            isCategoriesExpanded = !isCategoriesExpanded
            updateCategoriesDisplay()
            updateShowMoreCategoriesButton()
        }
    }

    private fun updateCategoriesDisplay() {
        var displayCategories = allCategories
        
        // Show button only if there are more than INITIAL_CATEGORY_COUNT items
        if (displayCategories.size > INITIAL_CATEGORY_COUNT) {
            viewBinding.btnShowMoreCategories.isVisible = true
            
            // Limit to INITIAL_CATEGORY_COUNT if not expanded
            if (!isCategoriesExpanded) {
                displayCategories = displayCategories.take(INITIAL_CATEGORY_COUNT)
            }
        } else {
            viewBinding.btnShowMoreCategories.isVisible = false
        }
        
        categoryAdapter.updateData(displayCategories)
        updateShowMoreCategoriesButton()
    }

    private fun updateShowMoreCategoriesButton() {
        if (viewBinding.btnShowMoreCategories.isVisible) {
            if (isCategoriesExpanded) {
                viewBinding.btnShowMoreCategories.text = "Thu nhỏ lại"
                viewBinding.btnShowMoreCategories.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, R.drawable.ic_expand_less, 0
                )
            } else {
                viewBinding.btnShowMoreCategories.text = "Hiển thị thêm"
                viewBinding.btnShowMoreCategories.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, R.drawable.ic_expand_more, 0
                )
            }
        }
    }

    private fun applyFiltersAndSort() {
        // Filter and update brands
        when (searchMode) {
            SearchMode.ALL, SearchMode.BRANDS -> {
                val filteredBrands = if (currentSearchQuery.isEmpty()) {
                    allBrands
                } else {
                    allBrands.filter { brand ->
                        brand.name?.contains(currentSearchQuery, ignoreCase = true) == true ||
                        brand.description?.contains(currentSearchQuery, ignoreCase = true) == true
                    }
                }
                brandNewAdapter.updateData(filteredBrands)
            }
            SearchMode.PRODUCTS -> {
                // Hide brands section
            }
        }

        // Filter products
        when (searchMode) {
            SearchMode.ALL, SearchMode.PRODUCTS -> {
                var filteredProducts = MedicineUtils.filterMedicines(allMedicines, currentSearchQuery)
                
                // Show button only if there are more than INITIAL_PRODUCT_COUNT items
                if (filteredProducts.size > INITIAL_PRODUCT_COUNT) {
                    viewBinding.btnShowMore.isVisible = true
                    
                    // Limit to INITIAL_PRODUCT_COUNT if not expanded
                    if (!isProductsExpanded) {
                        filteredProducts = filteredProducts.take(INITIAL_PRODUCT_COUNT)
                    }
                } else {
                    viewBinding.btnShowMore.isVisible = false
                }
                
                medicinesAdapter.bindData(filteredProducts)
                updateShowMoreButton()
            }
            SearchMode.BRANDS -> {
                // Hide products section
                viewBinding.btnShowMore.isVisible = false
            }
        }
    }

    private fun updateShowMoreButton() {
        if (viewBinding.btnShowMore.isVisible) {
            if (isProductsExpanded) {
                viewBinding.btnShowMore.text = "Thu nhỏ lại"
                viewBinding.btnShowMore.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, R.drawable.ic_expand_less, 0
                )
            } else {
                viewBinding.btnShowMore.text = "Hiển thị thêm"
                viewBinding.btnShowMore.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, R.drawable.ic_expand_more, 0
                )
            }
        }
    }

    private fun updateUi(state: DataResult<HomeState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is HomeState.PharmaciesList -> {
                    // No longer using pharmacies as brands
                }
            }
        }
        state?.doIfFailure { }
    }
}
