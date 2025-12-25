package com.example.safeaid.screens.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.safeaid.core.response.Brand
import com.example.safeaid.core.utils.setOnDebounceClick

class BrandNewAdapter(
    private var items: List<Brand> = listOf(),
    private val onItemClick: ((Brand) -> Unit)? = null
) : RecyclerView.Adapter<BrandNewAdapter.BrandViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_brand_new, parent, false)
        return BrandViewHolder(view)
    }

    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Brand>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class BrandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.card_brand)
        private val imgLogo: ImageView = itemView.findViewById(R.id.img_brand_logo)

        fun bind(brand: Brand) {
            // Load brand logo
            if (!brand.logoPath.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(brand.logoPath)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .fitCenter()
                    .into(imgLogo)
            } else {
                imgLogo.setImageResource(R.drawable.ic_default_avatar)
            }

            // Set click listener
            cardView.setOnDebounceClick {
                onItemClick?.invoke(brand)
            }
        }
    }
}
