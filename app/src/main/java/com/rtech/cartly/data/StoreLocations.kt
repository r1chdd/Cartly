package com.rtech.cartly.data

import com.google.firebase.firestore.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Branch(val name: String, val latitude: Double, val longitude: Double)

object StoreLocations {

    private const val EARTH_RADIUS_KM = 6371.0

    val branches: Map<String, List<Branch>> = mapOf(
        "Checkers" to listOf(
            Branch("Checkers Greenacres", -33.9565, 25.5784),
            Branch("Checkers Walmer Park", -33.9701, 25.5917),
            Branch("Checkers Sunridge Park", -33.9385, 25.6240),
            Branch("Checkers Sandton City", -26.1076, 28.0565),
            Branch("Checkers Canal Walk", -33.8569, 18.5153)
        ),
        "Pick n Pay" to listOf(
            Branch("Pick n Pay Greenacres", -33.9565, 25.5784),
            Branch("Pick n Pay Walmer Park", -33.9701, 25.5917),
            Branch("Pick n Pay Sunridge Park", -33.9385, 25.6240),
            Branch("Pick n Pay Sandton City", -26.1076, 28.0565),
            Branch("Pick n Pay Canal Walk", -33.8569, 18.5153)
        ),
        "Shoprite" to listOf(
            Branch("Shoprite Walmer", -33.9688, 25.5698),
            Branch("Shoprite Korsten", -33.9197, 25.6266),
            Branch("Shoprite Sandton", -26.1076, 28.0565),
            Branch("Shoprite Sea Point", -33.9177, 18.3889)
        ),
        "Spar" to listOf(
            Branch("Spar Walmer", -33.9680, 25.5740),
            Branch("Spar Newton Park", -33.9290, 25.5910),
            Branch("Spar Sunridge Park", -33.9385, 25.6240),
            Branch("Spar Sandton", -26.1076, 28.0565)
        )
    )

    fun distanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        return EARTH_RADIUS_KM * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun nearestBranchDistance(store: String, location: GeoPoint?): Double? {
        if (location == null) return null
        val storeBranches = branches[store] ?: return null
        return storeBranches.minOfOrNull {
            distanceKm(location.latitude, location.longitude, it.latitude, it.longitude)
        }
    }

    fun distanceLabel(store: String, location: GeoPoint?): String? {
        val km = nearestBranchDistance(store, location) ?: return null
        return String.format("%.1f km", km)
    }
}