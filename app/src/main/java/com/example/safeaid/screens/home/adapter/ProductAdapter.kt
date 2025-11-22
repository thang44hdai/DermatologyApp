package com.example.safeaid.screens.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.safeaid.core.response.MedicineResponse

class ProductAdapter(
    private var items: List<MedicineResponse>,
    private val onItemClick: ((MedicineResponse) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun bindData(items: List<MedicineResponse>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: ImageView = itemView.findViewById(R.id.img_product)
        private val name: TextView = itemView.findViewById(R.id.tv_product_name)
        private val price: TextView = itemView.findViewById(R.id.tv_price)

        fun bind(p: MedicineResponse) {
            name.text = p.name
            price.text = p.price
            val url = p.images.firstOrNull()
            if (url.isNullOrEmpty()) {
                Glide.with(itemView.context).load(android.R.color.darker_gray).into(img)
            } else {
                Glide.with(itemView.context).load(url).centerCrop().into(img)
            }

            itemView.setOnClickListener {
                onItemClick?.invoke(p)
            }
        }
    }
}
