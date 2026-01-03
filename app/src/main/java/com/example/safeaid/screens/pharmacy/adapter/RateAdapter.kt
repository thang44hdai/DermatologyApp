package com.example.safeaid.screens.pharmacy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.R
import com.example.safeaid.screens.pharmacy.data.RateItem

class RateAdapter(private var items: List<RateItem>) :
    RecyclerView.Adapter<RateAdapter.RateViewHolder>() {

    fun updateData(newItems: List<RateItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class RateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.ic)
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvValue: TextView = itemView.findViewById(R.id.tv_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rate, parent, false)
        return RateViewHolder(view)
    }

    override fun onBindViewHolder(holder: RateViewHolder, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconRes)
        holder.tvName.text = item.name
        holder.tvValue.text = item.value
    }

    override fun getItemCount(): Int = items.size
}