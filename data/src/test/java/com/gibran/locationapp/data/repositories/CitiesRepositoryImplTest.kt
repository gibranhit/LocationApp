package com.gibran.locationapp.data.repositories

import app.cash.turbine.test
import com.gibran.locationapp.data.local.dao.CityDao
import com.gibran.locationapp.data.local.entities.CityEntity
import com.gibran.locationapp.domain.models.City
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

@ExperimentalCoroutinesApi
class CitiesRepositoryImplTest {

    private val mockCityDao = mockk<CityDao>()
    private val mockOkHttpClient = mockk<OkHttpClient>(relaxed = true)
    private val mockCitiesFile = mockk<File>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: CitiesRepositoryImpl

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Setup default mock behavior
        every { mockCityDao.getFavoriteCitiesFlow() } returns flowOf(emptyList())
        coEvery { mockCityDao.getFavoriteIds() } returns emptyList()
        
        repository = CitiesRepositoryImpl(
            citiesFile = mockCitiesFile,
            okHttpClient = mockOkHttpClient,
            cityDao = mockCityDao,
            ioDispatcher = testDispatcher,
            defaultDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleFavorite delegates to cityDao successfully`() = runTest {
        // Arrange
        val cityId = "test_city_id"
        coEvery { mockCityDao.toggleFavorite(cityId) } returns true

        // Act
        val result = repository.toggleFavorite(cityId)

        // Assert
        assertTrue("Should return true when toggle succeeds", result)
        coVerify { mockCityDao.toggleFavorite(cityId) }
    }

    @Test
    fun `toggleFavorite returns false when dao returns false`() = runTest {
        // Arrange
        val cityId = "test_city_id"
        coEvery { mockCityDao.toggleFavorite(cityId) } returns false

        // Act
        val result = repository.toggleFavorite(cityId)

        // Assert
        assertFalse("Should return false when dao returns false", result)
        coVerify { mockCityDao.toggleFavorite(cityId) }
    }

    @Test
    fun `toggleFavorite handles dao exceptions`() = runTest {
        // Arrange
        val cityId = "error_city"
        coEvery { mockCityDao.toggleFavorite(cityId) } throws Exception("DAO error")

        // Act & Assert
        try {
            repository.toggleFavorite(cityId)
            fail("Should propagate DAO exception")
        } catch (e: Exception) {
            assertEquals("DAO error", e.message)
        }
        
        coVerify { mockCityDao.toggleFavorite(cityId) }
    }

    @Test
    fun `getCityById returns null for non-existent city when cache is empty`() = runTest {
        // Arrange
        val nonExistentId = "non_existent_city"

        // Act
        val result = repository.getCityById(nonExistentId)

        // Assert
        assertNull("Should return null when cache is empty", result)
    }

    @Test
    fun `getCities handles empty cache gracefully`() = runTest {
        // Act
        val result = repository.getCities()

        // Assert
        result.test {
            val cities = awaitItem()
            // Should return empty list when cache is empty
            assertTrue("Should handle empty cache", cities.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `searchCities handles empty cache gracefully`() = runTest {
        // Act
        val result = repository.searchCities("test")

        // Assert
        result.test {
            val cities = awaitItem()
            assertTrue("Should return empty list when cache is empty", cities.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `favorites flow integration updates city favorite status`() = runTest {
        // Arrange
        val favoriteEntity = CityEntity(
            id = "test_city",
            name = "Test City",
            country = "Test Country",
            latitude = 0.0,
            longitude = 0.0,
            isFavorite = true
        )

        every { mockCityDao.getFavoriteCitiesFlow() } returns flowOf(listOf(favoriteEntity))
        coEvery { mockCityDao.getFavoriteIds() } returns listOf("test_city")

        repository = CitiesRepositoryImpl(
            citiesFile = mockCitiesFile,
            okHttpClient = mockOkHttpClient,
            cityDao = mockCityDao,
            ioDispatcher = testDispatcher,
            defaultDispatcher = testDispatcher
        )

        repository.apply {
            val testCity = City(
                id = "test_city",
                name = "Test City",
                country = "Test Country",
                latitude = 0.0,
                longitude = 0.0,
                isFavorite = false
            )
            val cachedCitiesField = this::class.java.getDeclaredField("cachedCities")
            cachedCitiesField.isAccessible = true
            cachedCitiesField.set(this, listOf(testCity))
        }

        // Act
        val result = repository.getCities()

        // Assert
        result.test {
            val cities = awaitItem()
            assertEquals(1, cities.size)
            val city = cities.first()
            assertEquals("test_city", city.id)
            assertEquals("Test City", city.name)
            assertTrue("City should be marked as favorite", city.isFavorite)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getCities handles DAO flow errors gracefully`() = runTest {
        // Arrange
        coEvery { mockCityDao.getFavoriteCitiesFlow() } throws Exception("DAO flow error")


        // Act
        val result = repository.getCities()

        // Assert
        result.test {
            val cities = awaitItem()
            // Should handle DAO errors gracefully and return empty list
            assertTrue("Should handle DAO errors gracefully", cities.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `searchCities handles DAO flow errors gracefully`() = runTest {
        // Arrange
        every { mockCityDao.getFavoriteCitiesFlow() } throws Exception("DAO flow error")


        // Act
        val result = repository.searchCities("query")

        // Assert
        result.test {
            val cities = awaitItem()
            // Should handle DAO errors gracefully and return empty list
            assertTrue("Should handle DAO errors gracefully", cities.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getCityById handles DAO errors when getting favorite ids`() = runTest {
        // Arrange
        val cityId = "test_city"
        coEvery { mockCityDao.getFavoriteIds() } throws Exception("DAO error")

        // Act
        val result = repository.getCityById(cityId)

        // Assert
        assertNull("Should return null when DAO fails", result)
    }

    @Test
    fun `searchCities with empty query returns all cached cities`() = runTest {
        // Act
        val result = repository.searchCities("")

        // Assert
        result.test {
            val cities = awaitItem()
            // Should handle empty query gracefully
            assertTrue("Should handle empty query", cities.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }
}
