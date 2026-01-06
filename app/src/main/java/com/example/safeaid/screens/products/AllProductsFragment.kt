package com.example.safeaid.screens.products

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentAllProductsBinding
import com.example.safeaid.core.response.MedicineResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.home.HomeState
import com.example.safeaid.screens.home.HomeViewModel
import com.example.safeaid.screens.home.adapter.ProductAdapter
import com.example.safeaid.screens.home.utils.MedicineUtils
import com.example.safeaid.screens.medicine.MedicineDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class AllProductsFragment : BaseFragment<FragmentAllProductsBinding>() {
    private val viewModel: HomeViewModel by activityViewModels()
    private val productsAdapter = ProductAdapter(listOf()) { medicine ->
        val bundle = Bundle()
        bundle.putSerializable(MedicineDetailFragment.ARG_MEDICINE, medicine)
        findNavController().navigate(
            R.id.action_allProductsFragment_to_medicineDetailFragment,
            bundle
        )
    }
    
    private var allMedicines = listOf<MedicineResponse>()
    private var currentSearchQuery = ""
    private var sortType = SortType.NONE
    private var isLoadingMore = false
    
    enum class SortType {
        NONE, PRICE_ASC, PRICE_DESC, NAME_ASC, NAME_DESC
    }
    
    override fun isHostFragment(): Boolean = false
    
    override fun onInit() {
        setupToolbar()
        setupRecyclerView()
        setupLoadMore()
        
        // Load initial data if not already loaded
        if (allMedicines.isEmpty()) {
            viewModel.loadMedicines(reset = true)
        }
    }
    
    private fun setupToolbar() {
        viewBinding.icBack.setOnDebounceClick {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        viewBinding.rvAllProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productsAdapter
        }
    }
    
    private fun setupLoadMore() {
        viewBinding.rvAllProducts.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                
                if (!isLoadingMore &&
                    (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 4 &&
                    firstVisibleItemPosition >= 0 &&
                    currentSearchQuery.isEmpty()) {
                    
                    viewModel.loadMoreMedicines()
                }
            }
        })
    }
    
    override fun onInitObserver() {
        // Observe medicines data
        viewModel._medicineResponse
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { data ->
                allMedicines = data
                updateProductCount()
                applyFiltersAndSort()
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
            
        viewModel.isLoadingMore
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { loading ->
                isLoadingMore = loading
                viewBinding.progressBar.isVisible = loading
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
        
        // Sort button
        viewBinding.filterBar.setOnClickListener {
            showSortDialog()
        }
        
        // Clear search button
        viewBinding.btnClearSearch.setOnClickListener {
            viewBinding.edtSearch.text?.clear()
        }
        
        // Pull to refresh (if you have SwipeRefreshLayout)
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadMedicines(reset = true)
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }
    
    private fun applyFiltersAndSort() {
        var filteredProducts = MedicineUtils.filterMedicines(allMedicines, currentSearchQuery)
        
        // Apply sort
        filteredProducts = when (sortType) {
            SortType.PRICE_ASC -> MedicineUtils.sortByPrice(filteredProducts, ascending = true)
            SortType.PRICE_DESC -> MedicineUtils.sortByPrice(filteredProducts, ascending = false)
            SortType.NAME_ASC -> MedicineUtils.sortByName(filteredProducts, ascending = true)
            SortType.NAME_DESC -> MedicineUtils.sortByName(filteredProducts, ascending = false)
            SortType.NONE -> filteredProducts
        }
        
        productsAdapter.bindData(filteredProducts)
        updateProductCount(filteredProducts.size)
        
        // Show/hide clear button
        viewBinding.btnClearSearch.isVisible = currentSearchQuery.isNotEmpty()
        
        // Show empty state
        viewBinding.tvEmptyState.isVisible = filteredProducts.isEmpty()
        viewBinding.rvAllProducts.isVisible = filteredProducts.isNotEmpty()
    }
    
    private fun updateProductCount(count: Int? = null) {
        val displayCount = count ?: allMedicines.size
        viewBinding.tvProductCount.text = "$displayCount sản phẩm"
    }
    
    private fun showSortDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_sort_options, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Get views
        val radioDefault = dialogView.findViewById<android.widget.RadioButton>(R.id.radio_default)
        val radioPriceAsc = dialogView.findViewById<android.widget.RadioButton>(R.id.radio_price_asc)
        val radioPriceDesc = dialogView.findViewById<android.widget.RadioButton>(R.id.radio_price_desc)
        val radioNameAsc = dialogView.findViewById<android.widget.RadioButton>(R.id.radio_name_asc)
        val radioNameDesc = dialogView.findViewById<android.widget.RadioButton>(R.id.radio_name_desc)
        
        val optionDefault = dialogView.findViewById<android.widget.LinearLayout>(R.id.option_default)
        val optionPriceAsc = dialogView.findViewById<android.widget.LinearLayout>(R.id.option_price_asc)
        val optionPriceDesc = dialogView.findViewById<android.widget.LinearLayout>(R.id.option_price_desc)
        val optionNameAsc = dialogView.findViewById<android.widget.LinearLayout>(R.id.option_name_asc)
        val optionNameDesc = dialogView.findViewById<android.widget.LinearLayout>(R.id.option_name_desc)
        
        val btnCancel = dialogView.findViewById<android.widget.TextView>(R.id.btn_cancel)
        val btnApply = dialogView.findViewById<android.widget.TextView>(R.id.btn_apply)
        
        // Set current selection
        when (sortType) {
            SortType.NONE -> radioDefault.isChecked = true
            SortType.PRICE_ASC -> radioPriceAsc.isChecked = true
            SortType.PRICE_DESC -> radioPriceDesc.isChecked = true
            SortType.NAME_ASC -> radioNameAsc.isChecked = true
            SortType.NAME_DESC -> radioNameDesc.isChecked = true
        }
        
        var selectedSortType = sortType
        
        // Option click listeners
        optionDefault.setOnClickListener {
            radioDefault.isChecked = true
            radioPriceAsc.isChecked = false
            radioPriceDesc.isChecked = false
            radioNameAsc.isChecked = false
            radioNameDesc.isChecked = false
            selectedSortType = SortType.NONE
        }
        
        optionPriceAsc.setOnClickListener {
            radioDefault.isChecked = false
            radioPriceAsc.isChecked = true
            radioPriceDesc.isChecked = false
            radioNameAsc.isChecked = false
            radioNameDesc.isChecked = false
            selectedSortType = SortType.PRICE_ASC
        }
        
        optionPriceDesc.setOnClickListener {
            radioDefault.isChecked = false
            radioPriceAsc.isChecked = false
            radioPriceDesc.isChecked = true
            radioNameAsc.isChecked = false
            radioNameDesc.isChecked = false
            selectedSortType = SortType.PRICE_DESC
        }
        
        optionNameAsc.setOnClickListener {
            radioDefault.isChecked = false
            radioPriceAsc.isChecked = false
            radioPriceDesc.isChecked = false
            radioNameAsc.isChecked = true
            radioNameDesc.isChecked = false
            selectedSortType = SortType.NAME_ASC
        }
        
        optionNameDesc.setOnClickListener {
            radioDefault.isChecked = false
            radioPriceAsc.isChecked = false
            radioPriceDesc.isChecked = false
            radioNameAsc.isChecked = false
            radioNameDesc.isChecked = true
            selectedSortType = SortType.NAME_DESC
        }
        
        // Button listeners
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnApply.setOnClickListener {
            sortType = selectedSortType
            applyFiltersAndSort()
            dialog.dismiss()
        }
        
        dialog.show()
    }
}
