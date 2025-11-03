package com.example.safeaid.screens.map.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.databinding.ItemImageBinding

class ImageAdapter(
    private val imageList: List<Int>
) : RecyclerView.Adapter<ImageAdapter.ImageVH>() {

    inner class ImageVH(private val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resId: Int) {
            binding.imv.setImageResource(resId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVH {
        val binding =
            ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageVH(binding)
    }

    override fun getItemCount() = imageList.size

    override fun onBindViewHolder(holder: ImageVH, position: Int) {
        holder.bind(imageList[position])
    }
}
