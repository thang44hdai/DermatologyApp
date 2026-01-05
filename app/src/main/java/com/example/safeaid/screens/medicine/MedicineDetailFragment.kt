package com.example.safeaid.screens.medicine

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentMedicineDetailBinding
import com.example.safeaid.core.response.MedicineResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.formatPrice
import com.example.safeaid.core.utils.showImageZoom
import com.example.safeaid.screens.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MedicineDetailFragment : BaseFragment<FragmentMedicineDetailBinding>() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private var medicine: MedicineResponse? = null
    private lateinit var imageAdapter: MedicineImageAdapter
    private var isDescriptionExpanded = false
    private var fullDescription = ""

    companion object {
        const val ARG_MEDICINE = "medicine"
    }

    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
        medicine = arguments?.getSerializable(ARG_MEDICINE) as? MedicineResponse
        medicine?.let { bindMedicineData(it) }
    }

    override fun onInitObserver() {
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewBinding.btnFindPharmacy.setOnClickListener {
            // Navigate to map screen to find nearest pharmacy
            mainViewModel.currentPage = 1
            findNavController().navigate(R.id.mainScreen)
        }

        viewBinding.btnSeeMoreDescription.setOnClickListener {
            toggleDescriptionExpansion()
        }
    }

    private fun bindMedicineData(medicine: MedicineResponse) {
        with(viewBinding) {
            // Set product title
            tvProductTitle.text = medicine.name ?: "Sản phẩm"

            // Set brand
            val brandName = medicine.brand?.name ?: "Chưa rõ"
            tvBrand.text = "Thương hiệu: $brandName"

            // Set price with Vietnamese format
            tvPrice.text = medicine.price?.formatPrice()

            // Set rating (mock data - replace with actual if available)
            tvRating.text = "3.6k"
            tvSold.text = "Đã bán 1.8k"

            // Set category (type)
            tvCategory.text = medicine.type ?: "Tuýp"

            // Set description with expand/collapse functionality
            fullDescription = medicine.description ?: "Không có mô tả sản phẩm"
            setupDescriptionWithExpandCollapse()

            // Set usage (description)
            tvUsage.text = medicine.description
                ?: "Gel dưỡng da cao ẩm, giúp giảm mụn, giảm thâm mụn, hỗ trợ thu nhỏ lỗ chân lông và làm da."

            // Set specification (dosage)
            tvSpecification.text = medicine.dosage ?: "40ml"

            // Set note (side_effects)
            tvNote.text = medicine.sideEffects
                ?: "Một thông tin quan trọng cần chú ý tham khảo: Đọc kỹ hướng dẫn sử dụng trước khi dùng"

            // Setup image gallery
            setupImageGallery(medicine.images)
        }
    }

    private fun setupImageGallery(images: List<String>) {
        val imageList = if (images.isNotEmpty()) {
            images
        } else {
            // If no images, show placeholder
            listOf("")
        }

        imageAdapter = MedicineImageAdapter(imageList) { imageUrl ->
            requireContext().showImageZoom(imageUrl)
        }

        viewBinding.rvProductImages.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
    }

    private fun setupDescriptionWithExpandCollapse() {
        with(viewBinding) {
            tvDescription.text = fullDescription
            
            // Check if text needs to be truncated
            tvDescription.post {
                val lineCount = tvDescription.lineCount
                if (lineCount > 5) {
                    // Text is longer than 5 lines, show "Xem thêm" button
                    btnSeeMoreDescription.visibility = android.view.View.VISIBLE
                    tvDescription.maxLines = 5
                    isDescriptionExpanded = false
                } else {
                    // Text fits in 5 lines or less, hide "Xem thêm" button
                    btnSeeMoreDescription.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun toggleDescriptionExpansion() {
        with(viewBinding) {
            if (isDescriptionExpanded) {
                // Collapse: show only 5 lines
                tvDescription.maxLines = 5
                btnSeeMoreDescription.text = "Xem thêm"
                isDescriptionExpanded = false
            } else {
                // Expand: show all lines
                tvDescription.maxLines = Int.MAX_VALUE
                btnSeeMoreDescription.text = "Thu gọn"
                isDescriptionExpanded = true
            }
        }
    }
}

