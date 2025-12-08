package com.example.safeaid.screens.reminder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.databinding.ItemReminderBinding
import com.example.safeaid.core.response.Reminder

class ReminderItemAdapter(
    private val onItemClick: (Reminder) -> Unit
) : ListAdapter<Reminder, ReminderItemAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: Reminder) {
            binding.apply {
                tvMedicineName.text = reminder.medicineName
                tvDosage.text = "${reminder.times?.firstOrNull()?.dosage ?: "1"} ${reminder.unit ?: "Viên"}"
                
                // Set click listener
                root.setOnClickListener {
                    onItemClick(reminder)
                }
                
                // Arrow icon
                icArrow.setOnClickListener {
                    onItemClick(reminder)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(
            oldItem: Reminder,
            newItem: Reminder
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Reminder,
            newItem: Reminder
        ): Boolean {
            return oldItem == newItem
        }
    }
}
