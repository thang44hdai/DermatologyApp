package com.example.safeaid.screens.reminder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.databinding.ItemSelectionChipBinding

class TimePeriodAdapter(
    private val items: List<String>,
    private val onSelectionChanged: (List<String>) -> Unit
) : RecyclerView.Adapter<TimePeriodAdapter.TimePeriodViewHolder>() {

    private val selectedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimePeriodViewHolder {
        val binding = ItemSelectionChipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimePeriodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimePeriodViewHolder, position: Int) {
        holder.bind(items[position], selectedPositions.contains(position))
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedItems(): List<String> {
        return selectedPositions.map { items[it] }
    }

    inner class TimePeriodViewHolder(private val binding: ItemSelectionChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, isSelected: Boolean) {
            binding.tvChip.text = item
            binding.tvChip.isSelected = isSelected

            binding.root.setOnClickListener {
                if (position != RecyclerView.NO_POSITION) {
                    if (selectedPositions.contains(position)) {
                        selectedPositions.remove(position)
                    } else {
                        selectedPositions.add(position)
                    }
                    notifyItemChanged(position)
                    onSelectionChanged(getSelectedItems())
                }
            }
        }
    }
}
