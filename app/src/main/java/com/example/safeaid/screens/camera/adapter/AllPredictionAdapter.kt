package com.example.safeaid.screens.camera.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.R
import com.example.safeaid.core.response.AllPrediction


class AllPredictionAdapter(
    private var items: List<AllPrediction> = emptyList()
) : RecyclerView.Adapter<AllPredictionAdapter.PredictionViewHolder>() {

    inner class PredictionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reference_label, parent, false)
        return PredictionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PredictionViewHolder, position: Int) {
        val item = items[position]
        val confidencePercent = try {
            val value = item.confidence?.substring(1, item.confidence?.length?.minus(1) ?: 0)
                ?.toDoubleOrNull()
            if (value != null) String.format("%.2f%%", value * 100)
            else ""
        } catch (e: Exception) {
            ""
        }
        holder.tvName.text = "${item.labelVi ?: "Không rõ"} ($confidencePercent)"
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<AllPrediction>) {
        items = newItems
        notifyDataSetChanged()
    }
}
