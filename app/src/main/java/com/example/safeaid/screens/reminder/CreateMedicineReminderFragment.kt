package com.example.safeaid.screens.reminder

import androidx.navigation.fragment.findNavController
import com.example.dermatology.databinding.FragmentCreateMedicineReminderBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick

class CreateMedicineReminderFragment : BaseFragment<FragmentCreateMedicineReminderBinding>() {

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
    }

    override fun onInitObserver() {
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().popBackStack()
        }
    }

}