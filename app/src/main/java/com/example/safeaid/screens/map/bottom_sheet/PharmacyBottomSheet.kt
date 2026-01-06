package com.example.safeaid.screens.map.bottom_sheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dermatology.R
import com.example.dermatology.databinding.LayoutBottomSheetPharmacyBinding
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.screens.map.adapter.ImageAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PharmacyBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetPharmacyBinding? = null
    private val binding get() = _binding!!

    private var pharmacy: PharmacyResponse? = null

    private var onClick: OnClickPharmacyBottomSheet? = null
    fun setOnClick(clicked: OnClickPharmacyBottomSheet) {
        this.onClick = clicked
    }

    interface OnClickPharmacyBottomSheet {
        fun onClickViewDetail(data: PharmacyResponse)
        fun onClickDirection(data: PharmacyResponse)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pharmacy = arguments?.getSerializable(ARG_PHARMACY) as? PharmacyResponse
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutBottomSheetPharmacyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int =
        com.google.android.material.R.style.Theme_Design_BottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior()
        setupUi()
        setupListener()
    }

    private fun setupBottomSheetBehavior() {
        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                behavior.peekHeight =
                    resources.displayMetrics.heightPixels / 2 // hoặc một giá trị cụ thể
                behavior.isFitToContents = true
                behavior.isDraggable = true
                behavior.skipCollapsed = false
            }
        }
    }

    private fun setupUi() {
        pharmacy?.let { item ->
            binding.tvTitle.text = item.name ?: "Tên hiệu thuốc không rõ"
            binding.tvAddress.text = item.address ?: "Không có địa chỉ"
            binding.tvSdt.text = "Số điện thoại: ${item.phone ?: "Không có"}"
            binding.tvTime.text =
                if (item.isOpen247 == true) "7:00 - 23:00" else "${item.openTime?.substring(0,6) ?: "Chưa cập nhật"} - ${item.closeTime?.substring(0,6) ?: "Chưa cập nhật"}"
            binding.tvNumberImage.text = "Hình ảnh (${(item.images ?: listOf()).size})"

            binding.rcv.apply {
                adapter = ImageAdapter(
                    item.images ?: listOf()
                )
            }
        }
    }

    private fun setupListener() {
        binding.btnViewDetail.setOnClickListener {
            pharmacy?.let { it1 -> onClick?.onClickViewDetail(it1) }
            dismiss()
        }

        binding.btnDirection.setOnClickListener {
            pharmacy?.let { it1 -> onClick?.onClickDirection(it1) }
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PHARMACY = "ARG_PHARMACY"

        fun newInstance(pharmacy: PharmacyResponse): PharmacyBottomSheet {
            return PharmacyBottomSheet().apply {
                arguments = bundleOf(ARG_PHARMACY to pharmacy)
            }
        }
    }
}
