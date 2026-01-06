package com.example.safeaid.screens.medicine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermatology.R

class MedicineImagePagerAdapter(
    private val images: List<String>,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<MedicineImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.img_medicine_pager)

        fun bind(imageUrl: String) {
            // Load image
            Glide.with(itemView.context)
                .load(imageUrl)
                .fitCenter()
                .placeholder(R.drawable.ic_image_error)
                .error(R.drawable.ic_image_error)
                .into(imageView)

            // Set click listener for zoom
            imageView.setOnClickListener {
                onImageClick(imageUrl)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine_image_pager, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size
}