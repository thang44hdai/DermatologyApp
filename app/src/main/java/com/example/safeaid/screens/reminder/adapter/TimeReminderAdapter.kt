package com.example.safeaid.screens.reminder.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.databinding.ItemTimeReminderBinding
import com.example.safeaid.screens.reminder.TimePeriodItem
import java.text.SimpleDateFormat
import java.util.*

class TimeReminderAdapter :
    ListAdapter<TimePeriodItem, TimeReminderAdapter.ViewHolder>(DiffCallback()) {
    private var onDeleteItem: (TimePeriodItem) -> Unit = {}

    private val timeFormat = SimpleDateFormat("HH:mm", Locale("vi"))

    fun setOnDeleted(callBack: (TimePeriodItem) -> Unit) {
        this.onDeleteItem = callBack
    }

    inner class ViewHolder(private val binding: ItemTimeReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TimePeriodItem) {
            binding.apply {
                // Display time
                tvTime.setText(if (item.time.isEmpty()) "" else item.time)

                // Display dosage
                edtDosage.setText(item.dosage)

                // Display unit
                tvUnit.text = item.unit

                // Time picker on click
                tvTime.setOnClickListener {
                    showTimePicker(item)
                }

                // Dosage input listener
                setupDosageListener(item)

                // Delete button
                icDelete.setOnClickListener {
                    onDeleteItem(item)
                }
            }
        }

        private fun setupDosageListener(item: TimePeriodItem) {
            // Remove previous TextWatcher if exists
            binding.edtDosage.tag?.let {
                if (it is TextWatcher) {
                    binding.edtDosage.removeTextChangedListener(it)
                }
            }

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val newDosage = s?.toString() ?: "1"
                    if (newDosage.isNotEmpty() && newDosage != item.dosage) {
                        item.dosage = newDosage
                    }
                }
            }
            binding.edtDosage.addTextChangedListener(textWatcher)
            binding.edtDosage.tag = textWatcher
        }

        private fun showTimePicker(item: TimePeriodItem) {
            val calendar = Calendar.getInstance()

            // Parse existing time if available
            if (item.time.isNotEmpty()) {
                try {
                    val date = timeFormat.parse(item.time)
                    date?.let { calendar.time = it }
                } catch (e: Exception) {
                    // Use current time
                }
            }

            val timePickerDialog = android.app.TimePickerDialog(
                binding.root.context,
                com.example.dermatology.R.style.CustomTimePickerDialog,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    item.time = timeFormat.format(calendar.time)
                    binding.tvTime.setText(item.time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // 24-hour format
            )
            
            // Set button colors
            timePickerDialog.setOnShowListener {
                timePickerDialog.getButton(android.app.TimePickerDialog.BUTTON_POSITIVE)?.setTextColor(
                    androidx.core.content.ContextCompat.getColor(binding.root.context, com.example.dermatology.R.color.primary)
                )
                timePickerDialog.getButton(android.app.TimePickerDialog.BUTTON_NEGATIVE)?.setTextColor(
                    androidx.core.content.ContextCompat.getColor(binding.root.context, com.example.dermatology.R.color.gray_neutral_4)
                )
            }
            
            timePickerDialog.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTimeReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<TimePeriodItem>() {
        override fun areItemsTheSame(oldItem: TimePeriodItem, newItem: TimePeriodItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TimePeriodItem, newItem: TimePeriodItem): Boolean {
            return oldItem.time == newItem.time &&
                    oldItem.dosage == newItem.dosage &&
                    oldItem.unit == newItem.unit
        }
    }
}
