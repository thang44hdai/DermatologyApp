package com.example.safeaid.screens.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.dermatology.databinding.ItemMedicineSourceBinding
import com.example.safeaid.core.response.Source

class MedicineSourceAdapter(
    private val onItemClick: (Source) -> Unit
) : ListAdapter<Source, MedicineSourceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicineSourceBinding.inflate(
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
        private val binding: ItemMedicineSourceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(source: Source) {
            binding.tvMedicineName.text = source.name ?: "Không rõ tên"
            
            val price = source.price ?: "0"
            binding.tvMedicinePrice.text = formatPrice(price)

            // Load image with Glide
            Glide.with(binding.root.context)
                .load(source.imageUrl)
                .placeholder(R.drawable.ic_ai)
                .error(R.drawable.ic_ai)
                .centerCrop()
                .into(binding.ivMedicine)

            binding.root.setOnClickListener {
                onItemClick(source)
            }
        }

        private fun formatPrice(price: String): String {
            return try {
                val priceValue = price.toDoubleOrNull() ?: 0.0
                String.format("%,.0fđ", priceValue)
            } catch (e: Exception) {
                "${price}đ"
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Source>() {
        override fun areItemsTheSame(oldItem: Source, newItem: Source): Boolean {
            return oldItem.medicineId == newItem.medicineId
        }

        override fun areContentsTheSame(oldItem: Source, newItem: Source): Boolean {
            return oldItem == newItem
        }
    }
}
