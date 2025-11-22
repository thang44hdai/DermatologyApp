package com.example.safeaid.screens.medicine

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentMedicineDetailBinding
import com.example.safeaid.core.response.MedicineResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.screens.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MedicineDetailFragment : BaseFragment<FragmentMedicineDetailBinding>() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private var medicine: MedicineResponse? = null

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
            findNavController().popBackStack()
        }

        viewBinding.btnFindPharmacy.setOnClickListener {
            // Navigate to map screen to find nearest pharmacy
            mainViewModel.currentPage = 1
            findNavController().navigate(R.id.mainScreen)
        }
    }

    private fun bindMedicineData(medicine: MedicineResponse) {
        with(viewBinding) {
            // Set product title
            tvProductTitle.text = medicine.name ?: "Sản phẩm"

            // Set brand
            val brandName = medicine.brand?.name ?: "Chưa rõ"
            tvBrand.text = "Thương hiệu: $brandName"

            // Set price
            val priceText = medicine.price ?: "0"
            tvPrice.text = "$priceText đ/Hộp"

            // Set rating (mock data - replace with actual if available)
            tvRating.text = "3.6k"
            tvSold.text = "Đã bán 1.8k"

            // Set category (type)
            tvCategory.text = medicine.type ?: "Tuýp"

            // Set description (suitable_for)
            tvDescription.text = medicine.suitableFor ?: "Sản phẩm hỗ trợ giảm mụn"

            // Set usage (description)
            tvUsage.text = medicine.description ?: "Gel dưỡng da cao ẩm, giúp giảm mụn, giảm thâm mụn, hỗ trợ thu nhỏ lỗ chân lông và làm da."

            // Set specification (dosage)
            tvSpecification.text = medicine.dosage ?: "40ml"

            // Set note (side_effects)
            tvNote.text = medicine.sideEffects ?: "Một thông tin quan trọng cần chú ý tham khảo: Đọc kỹ hướng dẫn sử dụng trước khi dùng"

            // Load product image
            val imageUrl = medicine.images.firstOrNull()
            if (imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(R.drawable.ic_default_avatar)
                    .into(imgProduct)
            } else {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .centerInside()
                    .error(R.drawable.ic_default_avatar)
                    .into(imgProduct)
            }
        }
    }
}

