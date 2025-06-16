package com.gibran.locationapp.data.models

import com.squareup.moshi.Json
import com.gibran.locationapp.domain.models.City
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CityDto(
    @Json(name = "_id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "country") val country: String,
    @Json(name = "coord") val coord: CoordDto,
    @Json(name = "state") val state: String? = null,
    @Json(name = "subcountry") val subcountry: String? = null
)

@JsonClass(generateAdapter = true)
data class CoordDto(
    @Json(name = "lon") val longitude: Double,
    @Json(name = "lat") val latitude: Double
)

// Extension function to convert DTO to domain model
fun CityDto.toDomain(): City {
    return City(
        id = id,
        name = name,
        country = country,
        latitude = coord.latitude,
        longitude = coord.longitude,
        state = state,
        subcountry = subcountry
    )
}

// Extension function to convert list of DTOs to domain models
fun List<CityDto>.toDomain(): List<City> {
    return map { it.toDomain() }
}
