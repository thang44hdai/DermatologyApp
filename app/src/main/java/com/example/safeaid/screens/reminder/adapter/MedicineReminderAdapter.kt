package com.example.safeaid.screens.reminder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.databinding.ItemMedicineReminderBinding
import com.example.safeaid.screens.reminder.MedicineReminder

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
            binding.tvTime.text = medicine.time
            binding.switchTaken.isChecked = medicine.isTaken

            // Show/hide note
            if (medicine.note.isNotBlank()) {
                binding.tvNote.visibility = android.view.View.VISIBLE
                binding.tvNote.text = "Ghi chú: ${medicine.note}"
                binding.divider.visibility = android.view.View.VISIBLE
            } else {
                binding.tvNote.visibility = android.view.View.GONE
                binding.divider.visibility = android.view.View.GONE
            }

            binding.switchTaken.setOnCheckedChangeListener { _, isChecked ->
                onCheckChanged(medicine, isChecked)
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
