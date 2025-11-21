package com.example.safeaid.screens.home.utils

import com.example.safeaid.core.response.PharmacyResponse
import java.text.Normalizer

object BrandUtils {

    /**
     * Normalize Vietnamese string for search
     */
    fun String.normalize(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return temp.replace("[\\p{InCombiningDiacriticalMarks}]+".toRegex(), "")
            .lowercase()
    }

    /**
     * Filter brands by search query (only by name)
     */
    fun filterBrands(
        brands: List<PharmacyResponse>,
        query: String
    ): List<PharmacyResponse> {
        if (query.isEmpty()) return brands

        val normalizedQuery = query.normalize()

        return brands.filter { brand ->
            val name = brand.name?.normalize() ?: ""
            name.contains(normalizedQuery, ignoreCase = true)
        }
    }
}

