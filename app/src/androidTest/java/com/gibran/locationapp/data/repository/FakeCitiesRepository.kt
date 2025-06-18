package com.gibran.locationapp.data.repository

import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.repositories.CitiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeCitiesRepository : CitiesRepository {
    
    private val fakeCities = mutableListOf(
        City(
            id = "1",
            name = "New York",
            country = "US",
            latitude = 40.7128,
            longitude = -74.0060,
            isFavorite = true
        ),
        City(
            id = "2",
            name = "London",
            country = "UK",
            latitude = 51.5074,
            longitude = -0.1278,
            isFavorite = false
        ),
        City(
            id = "3",
            name = "Paris",
            country = "FR",
            latitude = 48.8566,
            longitude = 2.3522,
            isFavorite = true
        )
    )

    override suspend fun getCities(): Flow<List<City>> {
        return flowOf(fakeCities.toList())
    }

    override suspend fun searchCities(query: String): Flow<List<City>> {
        return flowOf(
            fakeCities.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.country.contains(query, ignoreCase = true) 
            }
        )
    }


    override suspend fun toggleFavorite(cityId: String): Boolean {
        val cityIndex = fakeCities.indexOfFirst { it.id == cityId }
        return if (cityIndex != -1) {
            val city = fakeCities[cityIndex]
            fakeCities[cityIndex] = city.copy(isFavorite = !city.isFavorite)
            true
        } else {
            false
        }
    }

    override suspend fun getCityById(id: String): City? {
        return fakeCities.find { it.id == id }
    }
}
