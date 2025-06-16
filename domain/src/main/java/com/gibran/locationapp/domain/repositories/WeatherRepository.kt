package com.gibran.locationapp.domain.repositories

import com.gibran.locationapp.domain.models.Weather

interface WeatherRepository {
    suspend fun getWeather(latitude: Double, longitude: Double): Result<Weather>
    suspend fun getCachedWeather(cityId: String): Weather?
}