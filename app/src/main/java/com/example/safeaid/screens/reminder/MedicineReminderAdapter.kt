package com.example.safeaid.screens.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.R
import com.example.dermatology.databinding.ItemMedicineReminderBinding
import com.example.safeaid.core.utils.setOnDebounceClick

class MedicineReminderAdapter(
    private val onCheckChanged: (MedicineReminder, Boolean) -> Unit
) : ListAdapter<MedicineReminder, MedicineReminderAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicineReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemMedicineReminderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: MedicineReminder) {
            binding.tvMedicineName.text = medicine.name
            binding.tvMedicineInfo.text = "${medicine.status} • ${medicine.dosage}"
            if (medicine.isTaken) {
                binding.checkbox.setBackgroundResource(R.drawable.ic_check_box_ticked)
            } else {
                binding.checkbox.setBackgroundResource(R.drawable.ic_check_box_no_select)
            }

            binding.checkbox.setOnDebounceClick {
                onCheckChanged(medicine, !medicine.isTaken)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<MedicineReminder>() {
        override fun areItemsTheSame(
            oldItem: MedicineReminder,
            newItem: MedicineReminder
        ): Boolean {
            return oldItem.reminderId == newItem.reminderId
        }

        override fun areContentsTheSame(
            oldItem: MedicineReminder,
            newItem: MedicineReminder
        ): Boolean {
            return oldItem == newItem
        }
    }
}
