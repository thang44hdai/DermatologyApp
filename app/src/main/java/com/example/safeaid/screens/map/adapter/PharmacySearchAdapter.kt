package com.example.safeaid.screens.map.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.dermatology.databinding.ItemPharmacySearchBinding
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.screens.map.utils.PharmacyUtils

class PharmacySearchAdapter(
    private val onItemClick: (PharmacyResponse) -> Unit
) : ListAdapter<PharmacyResponse, PharmacySearchAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPharmacySearchBinding.inflate(
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
        private val binding: ItemPharmacySearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pharmacy: PharmacyResponse) {
            binding.apply {
                tvPharmacyName.text = pharmacy.name
                tvAddress.text = pharmacy.address
                tvDistance.text = PharmacyUtils.formatDistance(pharmacy.distanceKm)
                tvOpenStatus.text = PharmacyUtils.formatOpenStatus(pharmacy)
                tvOpenStatus.setTextColor(root.context.getColor(R.color.primary))

                // Load logo
                Glide.with(root.context)
                    .load(pharmacy.logoUrl)
                    .placeholder(R.drawable.ic_location_map)
                    .error(R.drawable.ic_location_map)
                    .into(ivPharmacyLogo)

                root.setOnClickListener {
                    onItemClick(pharmacy)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PharmacyResponse>() {
        override fun areItemsTheSame(
            oldItem: PharmacyResponse,
            newItem: PharmacyResponse
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PharmacyResponse,
            newItem: PharmacyResponse
        ): Boolean {
            return oldItem == newItem
        }
    }
}
