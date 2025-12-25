package com.example.safeaid.screens.brand.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.safeaid.core.response.MedicineResponse
import com.example.safeaid.core.utils.setOnDebounceClick

class BrandMedicineAdapter(
    private var medicines: List<MedicineResponse> = listOf(),
    private val onItemClick: ((MedicineResponse) -> Unit)? = null
) : RecyclerView.Adapter<BrandMedicineAdapter.MedicineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine_brand, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        holder.bind(medicines[position])
    }

    override fun getItemCount(): Int = medicines.size

    fun updateData(newMedicines: List<MedicineResponse>) {
        medicines = newMedicines
        notifyDataSetChanged()
    }

    inner class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgMedicine: ImageView = itemView.findViewById(R.id.img_medicine)
        private val tvMedicineName: TextView = itemView.findViewById(R.id.tv_medicine_name)
        private val tvMedicineType: TextView = itemView.findViewById(R.id.tv_medicine_type)
        private val tvMedicinePrice: TextView = itemView.findViewById(R.id.tv_medicine_price)
        private val tvDosage: TextView = itemView.findViewById(R.id.tv_dosage)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)

        fun bind(medicine: MedicineResponse) {
            // Set medicine name
            tvMedicineName.text = medicine.name ?: "Tên thuốc"

            // Set medicine type
            tvMedicineType.text = medicine.type ?: "Loại thuốc"

            // Set medicine price
            tvMedicinePrice.text = "Giá: ${medicine.price ?: "Liên hệ"}"

            // Set dosage
            tvDosage.text = "Liều dùng: ${medicine.dosage ?: "Theo chỉ định bác sĩ"}"

            // Set description
            tvDescription.text = medicine.description ?: "Không có mô tả"

            // Load medicine image (use first image - images[0])
            val imageUrl = medicine.images?.getOrNull(0)
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .centerCrop()
                    .into(imgMedicine)
            } else {
                imgMedicine.setImageResource(R.drawable.ic_default_avatar)
            }

            // Set click listener
            itemView.setOnDebounceClick {
                onItemClick?.invoke(medicine)
            }
        }
    }
}