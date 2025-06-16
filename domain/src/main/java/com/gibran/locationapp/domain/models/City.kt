package com.gibran.locationapp.domain.models

data class City(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val state: String? = null,
    val subcountry: String? = null,
    var isFavorite: Boolean = false,
    var distance: Double? = null
) {
    val displayName: String
        get() = if (state != null) "$name, $state" else "$name, $country"
    
    val coordinates: String
        get() = String.format("%.4f, %.4f", latitude, longitude)
}
