package com.example.safeaid.screens.reminder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.databinding.ItemSelectionChipBinding

data class SelectionItem(
    val id: String,
    val label: String,
    val isSelected: Boolean = false
)

class SelectionChipAdapter(
    private val onItemClick: (SelectionItem) -> Unit
) : ListAdapter<SelectionItem, SelectionChipAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectionChipBinding.inflate(
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
        private val binding: ItemSelectionChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SelectionItem) {
            binding.tvChip.text = item.label
            binding.tvChip.isSelected = item.isSelected

            binding.tvChip.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SelectionItem>() {
        override fun areItemsTheSame(oldItem: SelectionItem, newItem: SelectionItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SelectionItem, newItem: SelectionItem): Boolean {
            return oldItem == newItem
        }
    }
}
