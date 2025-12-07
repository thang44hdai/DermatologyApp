package com.example.safeaid.screens.reminder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.databinding.ItemDaySelectorBinding

data class DayItem(
    val id: String,
    val name: String,
    val isSelected: Boolean = false,
    val isAllDays: Boolean = false
)

class DaySelectorAdapter(
    private var onItemClick: (DayItem) -> Unit
) : ListAdapter<DayItem, DaySelectorAdapter.ViewHolder>(DiffCallback()) {

    fun setOnClick(clicked: (DayItem) -> Unit) {
        this.onItemClick = clicked
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDaySelectorBinding.inflate(
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
        private val binding: ItemDaySelectorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DayItem) {
            binding.tvDayName.text = item.name
            binding.ivCheck.isVisible = item.isSelected

            binding.container.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DayItem>() {
        override fun areItemsTheSame(oldItem: DayItem, newItem: DayItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DayItem, newItem: DayItem): Boolean {
            return oldItem == newItem
        }
    }
}
