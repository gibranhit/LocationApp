package com.gibran.locationapp.domain.models

data class Weather(
    val cityId: String,
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val icon: String,
    val timestamp: Long = System.currentTimeMillis()
)