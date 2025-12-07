package com.example.safeaid.screens.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermatology.databinding.ItemCategoryBinding
import com.example.safeaid.core.response.CategoryResponse

class CategoryAdapter(
    private var categories: List<CategoryResponse>,
    private val onItemClick: (CategoryResponse) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryResponse) {
            binding.apply {
                tvName.text = category.name
                tvCount.text = "${category.medicineCount ?: 0} sản phẩm"

                Glide.with(itemView.context).load(category.imageUrl).centerCrop().into(ivIcon)

                root.setOnClickListener {
                    onItemClick(category)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size

    fun updateData(newCategories: List<CategoryResponse>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}
