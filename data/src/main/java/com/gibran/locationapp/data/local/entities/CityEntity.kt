package com.gibran.locationapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gibran.locationapp.domain.models.City

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val state: String? = null,
    val subcountry: String? = null,
    val isFavorite: Boolean = false,
    val distance: Double? = null
)

// Extension functions for mapping between entity and domain model
fun CityEntity.toDomain(): City {
    return City(
        id = id,
        name = name,
        country = country,
        latitude = latitude,
        longitude = longitude,
        state = state,
        subcountry = subcountry,
        isFavorite = isFavorite,
        distance = distance
    )
}

fun City.toEntity(): CityEntity {
    return CityEntity(
        id = id,
        name = name,
        country = country,
        latitude = latitude,
        longitude = longitude,
        state = state,
        subcountry = subcountry,
        isFavorite = isFavorite,
        distance = distance
    )
}

fun List<CityEntity>.toDomain(): List<City> {
    return map { it.toDomain() }
}

fun List<City>.toEntity(): List<CityEntity> {
    return map { it.toEntity() }
}
