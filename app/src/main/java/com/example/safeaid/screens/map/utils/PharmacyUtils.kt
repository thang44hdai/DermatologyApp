package com.example.safeaid.screens.map.utils

import com.example.safeaid.core.response.PharmacyResponse

object PharmacyUtils {
    
    /**
     * Format pharmacy open status for display
     */
    fun formatOpenStatus(pharmacy: PharmacyResponse): String {
        return when {
            pharmacy.openTime?.contains("24") == true -> "Mở cửa 24/7"
            pharmacy.openTime != null && pharmacy.closeTime != null -> 
                "Mở cửa: ${pharmacy.openTime} - ${pharmacy.closeTime}"
            pharmacy.openTime != null -> "Mở cửa vào ${pharmacy.openTime}"
            else -> "Liên hệ để biết giờ mở cửa"
        }
    }

    /**
     * Format distance for display
     */
    fun formatDistance(distanceKm: String?): String {
        return "${distanceKm ?: "N/A"}km"
    }

    /**
     * Filter pharmacies by query (name or address)
     */
    fun filterPharmacies(
        pharmacies: List<PharmacyResponse>,
        query: String
    ): List<PharmacyResponse> {
        if (query.isEmpty()) return pharmacies
        
        return pharmacies.filter { pharmacy ->
            pharmacy.name?.contains(query, ignoreCase = true) == true ||
                    pharmacy.address?.contains(query, ignoreCase = true) == true
        }
    }

    /**
     * Filter pharmacies by criteria
     */
    fun filterPharmaciesByCriteria(
        pharmacies: List<PharmacyResponse>,
        is24h: Boolean = false,
        maxDistanceKm: Double? = null,
        minRating: Double? = null
    ): List<PharmacyResponse> {
        var filtered = pharmacies

        if (is24h) {
            filtered = filtered.filter { it.openTime?.contains("24") == true }
        }

        maxDistanceKm?.let { maxDist ->
            filtered = filtered.filter {
                it.distanceKm?.toDoubleOrNull()?.let { dist -> dist <= maxDist } ?: false
            }
        }

        minRating?.let { minRate ->
            filtered = filtered.filter {
                it.ratings?.toDoubleOrNull()?.let { rating -> rating >= minRate } ?: false
            }
        }

        return filtered
    }
}
