package com.example.safeaid.screens.category

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentCategoryDetailBinding
import com.example.safeaid.core.response.CategoryDetailResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.home.adapter.ProductAdapter
import com.example.safeaid.screens.home.adapter.ProductAdapter2
import com.example.safeaid.screens.medicine.MedicineDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class CategoryDetailFragment : BaseFragment<FragmentCategoryDetailBinding>() {
    private val viewModel: CategoryDetailViewModel by viewModels()
    private var categoryId: String? = null

    private val productsAdapter = ProductAdapter(listOf()) { medicine ->
        val bundle = Bundle()
        bundle.putSerializable(MedicineDetailFragment.ARG_MEDICINE, medicine)
        findNavController().navigate(
            R.id.action_categoryDetailFragment_to_medicineDetailFragment,
            bundle
        )
    }

    companion object {
        const val ARG_CATEGORY_ID = "category_id"
    }

    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
        categoryId = arguments?.getString(ARG_CATEGORY_ID)
        
        // Setup RecyclerView
        viewBinding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        viewBinding.rvProducts.adapter = productsAdapter
        
        categoryId?.let {
            viewModel.loadCategoryDetail(it)
        }
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> updateUi(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.isLoading
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { isLoading ->
                if (isLoading) {
                    showLoading()
                } else {
                    hideLoading()
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().popBackStack()
        }
    }

    private fun updateUi(state: DataResult<CategoryDetailState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is CategoryDetailState.CategoryDetailLoaded -> {
                    bindCategoryData(data.data)
                }
            }
        }
        state?.doIfFailure { error ->
            android.widget.Toast.makeText(
                requireContext(),
                "Lỗi: ${error.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun bindCategoryData(categoryDetail: CategoryDetailResponse) {
        val category = categoryDetail.category
        val medicines = categoryDetail.medicines

        with(viewBinding) {
            // Set category info
            tvCategoryName.text = category?.name ?: "Danh mục"

            // Set product count
            val productCount = medicines.size
            tvProductCount.text = "$productCount sản phẩm"

            // Load category icon if available
            if (!category?.imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(category?.imageUrl)
                    .placeholder(R.drawable.ic_image_error)
                    .error(R.drawable.ic_image_error)
                    .centerInside()
                    .into(imgCategoryIcon)
            } else {
                imgCategoryIcon.setImageResource(R.drawable.ic_image_error)
            }

            // Update products
            if (medicines.isNotEmpty()) {
                rvProducts.visibility = android.view.View.VISIBLE
                layoutEmptyState.visibility = android.view.View.GONE
                productsAdapter.bindData(medicines)
            } else {
                rvProducts.visibility = android.view.View.GONE
                layoutEmptyState.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun showLoading() {
        with(viewBinding) {
            layoutLoading.visibility = android.view.View.VISIBLE
            rvProducts.visibility = android.view.View.GONE
            layoutEmptyState.visibility = android.view.View.GONE
        }
    }

    private fun hideLoading() {
        viewBinding.layoutLoading.visibility = android.view.View.GONE
    }
}