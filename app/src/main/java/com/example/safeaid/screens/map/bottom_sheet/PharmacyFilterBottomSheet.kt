package com.example.safeaid.screens.map.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dermatology.R
import com.example.dermatology.databinding.BottomSheetPharmacyFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

data class PharmacyFilterCriteria(
    val maxDistance: Double? = null,
    val minRating: Double? = null,
    val is24h: Boolean = false
)

class PharmacyFilterBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetPharmacyFilterBinding? = null
    private val binding get() = _binding!!
    
    private var currentCriteria = PharmacyFilterCriteria()
    private var onApplyFilter: ((PharmacyFilterCriteria) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPharmacyFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int =
        com.google.android.material.R.style.Theme_Design_BottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        restoreCurrentFilters()
    }

    private fun setupListeners() {
        // Distance radio buttons
        binding.radioGroupDistance.setOnCheckedChangeListener { _, checkedId ->
            currentCriteria = currentCriteria.copy(
                maxDistance = when (checkedId) {
                    R.id.radio1km -> 1.0
                    R.id.radio3km -> 3.0
                    R.id.radio5km -> 5.0
                    R.id.radio10km -> 10.0
                    else -> null
                }
            )
        }

        // Rating radio buttons
        binding.radioGroupRating.setOnCheckedChangeListener { _, checkedId ->
            currentCriteria = currentCriteria.copy(
                minRating = when (checkedId) {
                    R.id.radio3star -> 3.0
                    R.id.radio4star -> 4.0
                    R.id.radio45star -> 4.5
                    else -> null
                }
            )
        }

        // 24h switch
        binding.switch24h.setOnCheckedChangeListener { _, isChecked ->
            currentCriteria = currentCriteria.copy(is24h = isChecked)
        }

        // Reset button
        binding.btnReset.setOnClickListener {
            resetFilters()
        }

        // Apply button
        binding.btnApply.setOnClickListener {
            onApplyFilter?.invoke(currentCriteria)
            dismiss()
        }
    }

    private fun restoreCurrentFilters() {
        // Restore distance
        when (currentCriteria.maxDistance) {
            1.0 -> binding.radio1km.isChecked = true
            3.0 -> binding.radio3km.isChecked = true
            5.0 -> binding.radio5km.isChecked = true
            10.0 -> binding.radio10km.isChecked = true
            else -> binding.radioAll.isChecked = true
        }

        // Restore rating
        when (currentCriteria.minRating) {
            3.0 -> binding.radio3star.isChecked = true
            4.0 -> binding.radio4star.isChecked = true
            4.5 -> binding.radio45star.isChecked = true
            else -> binding.radioAllRating.isChecked = true
        }

        // Restore 24h
        binding.switch24h.isChecked = currentCriteria.is24h
    }

    private fun resetFilters() {
        currentCriteria = PharmacyFilterCriteria()
        binding.radioAll.isChecked = true
        binding.radioAllRating.isChecked = true
        binding.switch24h.isChecked = false
    }

    fun setCurrentCriteria(criteria: PharmacyFilterCriteria) {
        currentCriteria = criteria
    }

    fun setOnApplyFilterListener(listener: (PharmacyFilterCriteria) -> Unit) {
        onApplyFilter = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(currentCriteria: PharmacyFilterCriteria = PharmacyFilterCriteria()): PharmacyFilterBottomSheet {
            return PharmacyFilterBottomSheet().apply {
                setCurrentCriteria(currentCriteria)
            }
        }
    }
}
