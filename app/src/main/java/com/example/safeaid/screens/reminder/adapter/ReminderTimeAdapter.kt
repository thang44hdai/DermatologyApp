package com.example.safeaid.screens.reminder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.databinding.ItemReminderTimeBinding
import com.example.safeaid.screens.reminder.MedicineReminder
import com.example.safeaid.screens.reminder.ReminderTime

class ReminderTimeAdapter(
    private val onMedicineCheckChanged: (MedicineReminder, Boolean) -> Unit
) : ListAdapter<ReminderTime, ReminderTimeAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderTimeBinding.inflate(
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
        private val binding: ItemReminderTimeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val medicineAdapter = MedicineReminderAdapter(onMedicineCheckChanged)

        init {
            binding.rcvMedicines.adapter = medicineAdapter
        }

        fun bind(reminderTime: ReminderTime) {
            binding.tvTime.text = reminderTime.time
            binding.tvTotal.text = "Tổng Cộng ${reminderTime.totalCount}"
            medicineAdapter.submitList(reminderTime.medicines)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ReminderTime>() {
        override fun areItemsTheSame(oldItem: ReminderTime, newItem: ReminderTime): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ReminderTime, newItem: ReminderTime): Boolean {
            return oldItem == newItem
        }
    }
}
