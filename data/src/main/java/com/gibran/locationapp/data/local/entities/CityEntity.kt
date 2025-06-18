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
    val isFavorite: Boolean = false,
)
