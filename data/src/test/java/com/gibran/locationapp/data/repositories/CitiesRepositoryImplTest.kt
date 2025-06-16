package com.gibran.locationapp.data.repositories

import com.gibran.locationapp.data.models.CityDto
import com.gibran.locationapp.data.models.CoordDto
import com.gibran.locationapp.data.remote.CitiesApiService
import com.gibran.locationapp.domain.models.City
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*

class CitiesRepositoryImplTest {

    private val mockApiService = mock(CitiesApiService::class.java)
    private val repository = CitiesRepositoryImpl(mockApiService)

    @Test
    fun `searchCities should return cities matching prefix case insensitive`() = runTest {
        // Given
        val mockCities = listOf(
            CityDto("1", "New York", "United States", CoordDto(-74.0, 40.7), "New York"),
            CityDto("2", "Newark", "United States", CoordDto(-74.2, 40.7), "New Jersey"), 
            CityDto("3", "London", "United Kingdom", CoordDto(-0.1, 51.5)),
            CityDto("4", "Newcastle", "United Kingdom", CoordDto(-1.6, 54.9)),
            CityDto("5", "Barcelona", "Spain", CoordDto(2.15, 41.38))
        )
        
        `when`(mockApiService.getCities()).thenReturn(mockCities)
        
        // Load cities first
        repository.getCities().first()
        
        // When searching for "new" (case insensitive prefix)
        val result = repository.searchCities("new").first()
        
        // Then should return cities starting with "new"
        assertEquals(3, result.size)
        
        val cityNames = result.map { it.name }.sorted()
        assertTrue(cityNames.contains("New York"))
        assertTrue(cityNames.contains("Newark"))
        assertTrue(cityNames.contains("Newcastle"))
        
        // Should not contain London or Barcelona
        assertFalse(cityNames.contains("London"))
        assertFalse(cityNames.contains("Barcelona"))
    }

    @Test
    fun `searchCities should return cities matching country prefix`() = runTest {
        // Given
        val mockCities = listOf(
            CityDto("1", "London", "United Kingdom", CoordDto(-0.1, 51.5)),
            CityDto("2", "Manchester", "United Kingdom", CoordDto(-2.2, 53.5)),
            CityDto("3", "New York", "United States", CoordDto(-74.0, 40.7)),
            CityDto("4", "Barcelona", "Spain", CoordDto(2.15, 41.38))
        )
        
        `when`(mockApiService.getCities()).thenReturn(mockCities)
        
        // Load cities first
        repository.getCities().first()
        
        // When searching for "united" (country prefix)
        val result = repository.searchCities("united").first()
        
        // Then should return cities from countries starting with "united"
        assertEquals(3, result.size)
        
        val cityNames = result.map { it.name }.sorted()
        assertTrue(cityNames.contains("London"))
        assertTrue(cityNames.contains("Manchester"))
        assertTrue(cityNames.contains("New York"))
        
        // Should not contain Barcelona
        assertFalse(cityNames.contains("Barcelona"))
    }

    @Test
    fun `searchCities should return empty list for non-matching prefix`() = runTest {
        // Given
        val mockCities = listOf(
            CityDto("1", "London", "United Kingdom", CoordDto(-0.1, 51.5)),
            CityDto("2", "Paris", "France", CoordDto(2.3, 48.9))
        )
        
        `when`(mockApiService.getCities()).thenReturn(mockCities)
        
        // Load cities first
        repository.getCities().first()
        
        // When searching for non-matching prefix
        val result = repository.searchCities("xyz").first()
        
        // Then should return empty list
        assertTrue(result.isEmpty())
    }

    @Test
    fun `searchCities should return all cities for empty query`() = runTest {
        // Given
        val mockCities = listOf(
            CityDto("1", "London", "United Kingdom", CoordDto(-0.1, 51.5)),
            CityDto("2", "Paris", "France", CoordDto(2.3, 48.9))
        )
        
        `when`(mockApiService.getCities()).thenReturn(mockCities)
        
        // Load cities first
        val allCities = repository.getCities().first()
        
        // When searching with empty query
        val result = repository.searchCities("").first()
        
        // Then should return all cities
        assertEquals(allCities.size, result.size)
    }
}