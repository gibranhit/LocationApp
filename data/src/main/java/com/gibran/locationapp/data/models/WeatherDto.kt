package com.gibran.locationapp.data.models

import com.gibran.locationapp.domain.models.Weather
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherDto(
    @Json(name = "main") val main: MainDto,
    @Json(name = "weather") val weather: List<WeatherDescriptionDto>,
    @Json(name = "wind") val wind: WindDto,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class MainDto(
    @Json(name = "temp") val temperature: Double,
    @Json(name = "humidity") val humidity: Int
)

@JsonClass(generateAdapter = true)
data class WeatherDescriptionDto(
    @Json(name = "main") val main: String,
    @Json(name = "description") val description: String,
    @Json(name = "icon") val icon: String
)

@JsonClass(generateAdapter = true)
data class WindDto(
    @Json(name = "speed") val speed: Double
)

// Extension function to convert DTO to domain model
fun WeatherDto.toDomain(cityId: String): Weather {
    return Weather(
        cityId = cityId,
        temperature = main.temperature,
        description = weather.firstOrNull()?.description ?: "",
        humidity = main.humidity,
        windSpeed = wind.speed,
        icon = weather.firstOrNull()?.icon ?: "",
        timestamp = System.currentTimeMillis()
    )
}