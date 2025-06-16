package com.gibran.locationapp.domain.repositories

import com.gibran.locationapp.domain.models.City
import kotlinx.coroutines.flow.Flow

interface CitiesRepository {
    suspend fun getCities(): Flow<List<City>>
    suspend fun searchCities(query: String): Flow<List<City>>
    suspend fun getFavorites(): Flow<List<City>>
    suspend fun toggleFavorite(cityId: String): Boolean
    suspend fun updateDistances(userLatitude: Double, userLongitude: Double)
    suspend fun getCityById(id: String): City?
}