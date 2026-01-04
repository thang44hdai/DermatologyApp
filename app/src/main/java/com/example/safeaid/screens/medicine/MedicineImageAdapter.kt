package com.example.safeaid.screens.medicine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.safeaid.core.utils.showImageZoom

class MedicineImageAdapter(
    private val images: List<String>,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<MedicineImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.img_medicine)
        private val tvImageNumber: TextView = itemView.findViewById(R.id.tv_image_number)

        fun bind(imageUrl: String, position: Int) {
            // Load image
            Glide.with(itemView.context)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_image_error)
                .error(R.drawable.ic_image_error)
                .into(imageView)

            // Set image number
            tvImageNumber.text = "${position + 1}/${images.size}"

            // Set click listener for zoom
            imageView.setOnClickListener {
                onImageClick(imageUrl)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    override fun getItemCount(): Int = images.size
}