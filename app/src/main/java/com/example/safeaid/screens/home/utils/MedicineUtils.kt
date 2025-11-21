package com.example.safeaid.screens.home.utils

import com.example.safeaid.core.response.MedicineResponse
import java.text.Normalizer

object MedicineUtils {

    /**
     * Normalize Vietnamese string for search
     */
    fun String.normalize(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return temp.replace("[\\p{InCombiningDiacriticalMarks}]+".toRegex(), "")
            .lowercase()
    }

    /**
     * Filter medicines by search query (only by name)
     */
    fun filterMedicines(
        medicines: List<MedicineResponse>,
        query: String
    ): List<MedicineResponse> {
        if (query.isEmpty()) return medicines

        val normalizedQuery = query.normalize()

        return medicines.filter { medicine ->
            val name = medicine.name?.normalize() ?: ""
            name.contains(normalizedQuery, ignoreCase = true)
        }
    }

    /**
     * Sort medicines by price
     */
    fun sortByPrice(
        medicines: List<MedicineResponse>,
        ascending: Boolean = true
    ): List<MedicineResponse> {
        return if (ascending) {
            medicines.sortedBy { extractPrice(it.price) }
        } else {
            medicines.sortedByDescending { extractPrice(it.price) }
        }
    }

    /**
     * Sort medicines by name
     */
    fun sortByName(
        medicines: List<MedicineResponse>,
        ascending: Boolean = true
    ): List<MedicineResponse> {
        return if (ascending) {
            medicines.sortedBy { it.name?.normalize() ?: "" }
        } else {
            medicines.sortedByDescending { it.name?.normalize() ?: "" }
        }
    }

    /**
     * Extract numeric price from string (e.g., "₫776,000" -> 776000.0)
     */
    private fun extractPrice(priceString: String?): Double {
        if (priceString.isNullOrEmpty()) return Double.MAX_VALUE
        return try {
            priceString.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: Double.MAX_VALUE
        } catch (e: Exception) {
            Double.MAX_VALUE
        }
    }
}

