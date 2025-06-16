package com.gibran.locationapp.domain.models

data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null
)