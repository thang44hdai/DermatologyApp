package com.example.safeaid.screens.reminder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.databinding.ItemSelectionChipBinding

class SelectionChipAdapter(
    private val items: List<String>,
    private val onItemSelected: (String, Int) -> Unit
) : RecyclerView.Adapter<SelectionChipAdapter.ChipViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val binding = ItemSelectionChipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        holder.bind(position, items[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = items.size

    private fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    inner class ChipViewHolder(private val binding: ItemSelectionChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(selectedPosition: Int, item: String, isSelected: Boolean) {
            binding.tvChip.text = item
            binding.tvChip.isSelected = isSelected

            binding.root.setOnClickListener {
                setSelectedPosition(selectedPosition)
                onItemSelected(item, selectedPosition)
            }
        }
    }
}
