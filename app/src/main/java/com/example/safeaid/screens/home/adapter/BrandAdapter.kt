package com.example.safeaid.screens.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.safeaid.models.Brand
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.core.utils.setOnDebounceClick
import okhttp3.internal.notifyAll

class BrandAdapter(private var items: List<PharmacyResponse>) :
    RecyclerView.Adapter<BrandAdapter.BrandViewHolder>() {
    private var onClick: OnClickBrand? = null
    fun setOnClick(callBack: OnClickBrand) {
        onClick = callBack
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_brand, parent, false)
        return BrandViewHolder(v)
    }

    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun bindData(items: List<PharmacyResponse>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class BrandViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val main: LinearLayout = itemView.findViewById(R.id.main)
        private val img: ImageView = itemView.findViewById(R.id.img_brand)
        private val name: TextView = itemView.findViewById(R.id.tv_brand_name)
        fun bind(b: PharmacyResponse) {
            name.text = b.name
            val url = b.images?.firstOrNull()
            if (url.isNullOrEmpty()) {
                Glide.with(itemView.context).load(android.R.color.darker_gray).into(img)
            } else {
                Glide.with(itemView.context).load(url).centerCrop().into(img)
            }
            main.setOnDebounceClick {
                onClick?.onClick(b)
            }
        }
    }

    interface OnClickBrand {
        fun onClick(item: PharmacyResponse)
    }
}
