package com.gibran.locationapp.data.repository

import com.gibran.locationapp.domain.models.Weather
import com.gibran.locationapp.domain.repositories.WeatherRepository

class FakeWeatherRepository : WeatherRepository {
    
    override suspend fun getWeather(latitude: Double, longitude: Double): Result<Weather> {
        // Return fake weather data based on coordinates
        val fakeWeather = when {
            latitude > 50.0 -> Weather(
                cityId = "london",
                temperature = 18.0,
                description = "Cloudy",
                humidity = 75,
                windSpeed = 2.5,
                icon = "04d"
            )
            latitude > 40.0 -> Weather(
                cityId = "newyork",
                temperature = 22.5,
                description = "Partly Cloudy",
                humidity = 65,
                windSpeed = 3.2,
                icon = "02d"
            )
            else -> Weather(
                cityId = "paris",
                temperature = 25.5,
                description = "Sunny",
                humidity = 55,
                windSpeed = 1.8,
                icon = "01d"
            )
        }
        
        return Result.success(fakeWeather)
    }
}
