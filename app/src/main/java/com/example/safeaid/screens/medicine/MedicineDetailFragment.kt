package com.example.safeaid.screens.medicine

import android.os.Bundle
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

            // Set description
            tvDescription.text = medicine.description ?: "Chưa có mô tả"

            // Set price
            tvPrice.text = medicine.price ?: "0 đ"

            // Load product image
            val imageUrl = medicine.images.firstOrNull()
            if (imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(android.R.color.darker_gray)
                    .into(imgProduct)
            } else {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .centerInside()
                    .into(imgProduct)
            }
        }
    }
}

