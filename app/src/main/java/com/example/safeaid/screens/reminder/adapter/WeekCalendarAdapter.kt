package com.example.safeaid.screens.reminder.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.R
import com.example.dermatology.databinding.ItemCalendarDayBinding
import com.example.dermatology.databinding.ItemWeekNavigationBinding
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.reminder.CalendarDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class WeekCalendarItem {
    data class NavigationButton(val isPrevious: Boolean) : WeekCalendarItem()
    data class DayItem(val day: CalendarDay) : WeekCalendarItem()
}

class WeekCalendarAdapter(
    private val onDayClick: (CalendarDay) -> Unit,
    private val onPreviousWeekClick: () -> Unit,
    private val onNextWeekClick: () -> Unit
) : ListAdapter<WeekCalendarItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_NAVIGATION = 0
        private const val VIEW_TYPE_DAY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is WeekCalendarItem.NavigationButton -> VIEW_TYPE_NAVIGATION
            is WeekCalendarItem.DayItem -> VIEW_TYPE_DAY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_NAVIGATION -> {
                val binding = ItemWeekNavigationBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                NavigationViewHolder(binding)
            }
            else -> {
                val binding = ItemCalendarDayBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DayViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is WeekCalendarItem.NavigationButton -> {
                (holder as NavigationViewHolder).bind(item.isPrevious)
            }
            is WeekCalendarItem.DayItem -> {
                (holder as DayViewHolder).bind(item.day)
            }
        }
    }

    inner class NavigationViewHolder(
        private val binding: ItemWeekNavigationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(isPrevious: Boolean) {
            if (isPrevious) {
                binding.btnNavigation.setImageResource(R.drawable.ic_chevron_left)
                binding.btnNavigation.setOnDebounceClick {
                    onPreviousWeekClick()
                }
            } else {
                binding.btnNavigation.setImageResource(R.drawable.ic_chevron_right)
                binding.btnNavigation.setOnDebounceClick {
                    onNextWeekClick()
                }
            }
        }
    }

    inner class DayViewHolder(
        private val binding: ItemCalendarDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(day: CalendarDay) {
            binding.tvDayName.text = day.dayName
            binding.tvDayNumber.text = convertDate(day.date)
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

    private class DiffCallback : DiffUtil.ItemCallback<WeekCalendarItem>() {
        override fun areItemsTheSame(oldItem: WeekCalendarItem, newItem: WeekCalendarItem): Boolean {
            return when {
                oldItem is WeekCalendarItem.NavigationButton && newItem is WeekCalendarItem.NavigationButton ->
                    oldItem.isPrevious == newItem.isPrevious
                oldItem is WeekCalendarItem.DayItem && newItem is WeekCalendarItem.DayItem ->
                    oldItem.day.date == newItem.day.date
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: WeekCalendarItem, newItem: WeekCalendarItem): Boolean {
            return oldItem == newItem
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertDate(dateString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM")
        val date = LocalDate.parse(dateString, inputFormatter)
        return date.format(outputFormatter)
    }
}
