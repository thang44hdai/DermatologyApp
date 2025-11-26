package com.example.safeaid.screens.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.R
import com.example.dermatology.databinding.ItemCalendarDayBinding
import com.example.safeaid.core.utils.setOnDebounceClick

class CalendarDayAdapter(
    private val onDayClick: (CalendarDay) -> Unit
) : ListAdapter<CalendarDay, CalendarDayAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
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
        private val binding: ItemCalendarDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(day: CalendarDay) {
            binding.tvDayName.text = day.dayName
            binding.tvDayNumber.text = day.dayNumber
            binding.indicator.isVisible = day.hasReminders

            if (day.isSelected) {
                binding.cardDay.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.primary)
                )
                binding.tvDayName.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
                binding.tvDayNumber.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
                binding.indicator.setBackgroundResource(R.drawable.bg_reminder_indicator_selected)
            } else {
                binding.cardDay.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
                binding.tvDayName.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.black)
                )
                binding.tvDayNumber.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.black)
                )
                binding.indicator.setBackgroundResource(R.drawable.bg_reminder_indicator)
            }

            binding.root.setOnDebounceClick {
                onDayClick(day)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CalendarDay>() {
        override fun areItemsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem == newItem
        }
    }
}
