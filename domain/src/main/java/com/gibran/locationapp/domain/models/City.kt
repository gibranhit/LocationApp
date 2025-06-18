package com.gibran.locationapp.domain.models

data class City(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    var isFavorite: Boolean = false,
) {
    val displayName: String
        get() = "$name, $country"
    
    val coordinates: String
        get() = String.format("%.4f, %.4f", latitude, longitude)
}
