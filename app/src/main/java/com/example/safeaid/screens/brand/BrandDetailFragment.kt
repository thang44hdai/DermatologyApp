package com.example.safeaid.screens.brand

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentBrandDetailBinding
import com.example.safeaid.core.response.Brand
import com.example.safeaid.core.response.BrandDetailResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.brand.adapter.BrandMedicineAdapter
import com.example.safeaid.screens.home.viewmodel.BrandState
import com.example.safeaid.screens.home.viewmodel.BrandViewModel
import com.example.safeaid.screens.medicine.MedicineDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class BrandDetailFragment : BaseFragment<FragmentBrandDetailBinding>() {
    private val viewModel: BrandViewModel by viewModels()
    private var brandId: String? = null
    private lateinit var medicineAdapter: BrandMedicineAdapter

    companion object {
        const val ARG_BRAND_ID = "brand_id"
    }

    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
        brandId = arguments?.getString(ARG_BRAND_ID)
        
        // Setup RecyclerView
        medicineAdapter = BrandMedicineAdapter { medicine ->
        }
        
        viewBinding.rvMedicines.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = medicineAdapter
        }
        
        brandId?.let {
            viewModel.loadBrandDetail(it)
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
                // Handle loading state if needed
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().popBackStack()
        }
    }

    private fun updateUi(state: DataResult<BrandState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is BrandState.BrandDetail -> {
                    bindBrandData(data.data)
                }
                else -> {}
            }
        }
        state?.doIfFailure { error ->
            android.widget.Toast.makeText(
                requireContext(),
                "${error.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun bindBrandData(data: BrandDetailResponse) {
        val brand = data.brand ?: Brand()
        with(viewBinding) {
            // Set brand name
            tvBrandName.text = brand.name ?: "Thương hiệu"

            // Set brand description
            if (!brand.description.isNullOrEmpty()) {
                tvDescription.text = brand.description
            } else {
                tvDescription.text = "Chưa có mô tả"
            }

            // Set created date
            if (!brand.createdAt.isNullOrEmpty()) {
                tvCreatedDate.text = formatDate(brand.createdAt ?: "")
            } else {
                tvCreatedDate.text = "Ngày tạo: Chưa rõ"
            }

            // Load brand logo
            if (!brand.logoPath.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(brand.logoPath)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .fitCenter()
                    .into(imgBrandLogo)
            } else {
                imgBrandLogo.setImageResource(R.drawable.ic_default_avatar)
            }

            // Update medicines list
            medicineAdapter.updateData(data.medicines)
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Parse ISO 8601 format: 2025-11-08T08:37:11
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            
            val date = inputFormat.parse(dateString)
            if (date != null) {
                outputFormat.format(date)
            } else {
                "Chưa rõ"
            }
        } catch (e: Exception) {
            // If parsing fails, try to extract just the date part
            try {
                val datePart = dateString.split("T")[0]
                val parts = datePart.split("-")
                if (parts.size == 3) {
                    "${parts[2]}/${parts[1]}/${parts[0]}"
                } else {
                    "Chưa rõ"
                }
            } catch (e: Exception) {
                "Chưa rõ"
            }
        }
    }
}
