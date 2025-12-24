package com.example.safeaid.screens.history

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.safeaid.core.response.Scan
import com.example.safeaid.core.utils.toCustomDateFormat

class HistoryAdapter(
    private var scans: List<Scan> = emptyList(),
    private val onItemClick: ((Scan) -> Unit)? = null
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imv: ImageView = itemView.findViewById(R.id.imv)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvDes: TextView = itemView.findViewById(R.id.tvDes)
        private val ivStatusIcon: ImageView = itemView.findViewById(R.id.ivStatusIcon)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: Scan) {
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.bg_take)
                .error(R.drawable.bg_take)
                .into(imv)

            tvName.text = item.disease?.diseaseName ?: "Không xác định"
            tvDes.text = item.disease?.description ?: "Không có mô tả"
            
            // Handle status with icon and color
            when (item.status?.lowercase()) {
                "completed" -> {
                    ivStatusIcon.setImageResource(R.drawable.ic_check_success)
                    tvStatus.text = "Thành công"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#17AF7C"))
                }
                else -> {
                    ivStatusIcon.setImageResource(R.drawable.ic_close_error)
                    tvStatus.text = "Thất bại"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#E40005"))
                }
            }
            
            tvDate.text = "Ngày: ${item.scanDate?.toCustomDateFormat() ?: "Không có"}"

            itemView.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_scan, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(scans[position])
    }

    override fun getItemCount(): Int = scans.size

    fun updateData(newData: List<Scan>) {
        scans = newData
        notifyDataSetChanged()
    }
}
