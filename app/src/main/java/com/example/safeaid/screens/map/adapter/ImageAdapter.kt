package com.example.safeaid.screens.map.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.dermatology.R
import com.example.dermatology.databinding.ItemImageBinding
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.core.utils.showImageZoom

class ImageAdapter(
    private val imageList: List<String>
) : RecyclerView.Adapter<ImageAdapter.ImageVH>() {

    inner class ImageVH(private val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String) {
            Glide.with(binding.root.context)
                .load(url)
                .placeholder(R.drawable.ic_image_error)
                .error(R.drawable.ic_image_error)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imv)

            binding.root.setOnDebounceClick {
                binding.root.context.showImageZoom(url)
            }
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
