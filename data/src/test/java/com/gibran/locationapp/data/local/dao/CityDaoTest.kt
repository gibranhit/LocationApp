package com.gibran.locationapp.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.gibran.locationapp.data.local.AppDatabase
import com.gibran.locationapp.data.local.entities.CityEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CityDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var cityDao: CityDao

    private val testCity1 = CityEntity(
        id = "1",
        name = "New York",
        country = "United States",
        latitude = 40.7128,
        longitude = -74.0060,
        state = "NY",
        subcountry = null,
        isFavorite = false
    )

    private val testCity2 = CityEntity(
        id = "2",
        name = "London",
        country = "United Kingdom",
        latitude = 51.5074,
        longitude = -0.1278,
        state = null,
        subcountry = null,
        isFavorite = true
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        cityDao = database.cityDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertCity_shouldInsertCitySuccessfully() = runTest {
        // Arrange
        // (testCity1 already defined)

        // Act
        cityDao.insertCity(testCity1)

        // Assert
        val favoriteIds = cityDao.getFavoriteIds()
        assertFalse("City should not be favorite initially", favoriteIds.contains("1"))
    }

    @Test
    fun insertCity_shouldReplaceExistingCity() = runTest {
        // Arrange
        cityDao.insertCity(testCity1)
        val updatedCity = testCity1.copy(name = "Updated New York", isFavorite = true)

        // Act
        cityDao.insertCity(updatedCity)

        // Assert
        val favoriteIds = cityDao.getFavoriteIds()
        assertTrue("Updated city should be favorite", favoriteIds.contains("1"))
    }

    @Test
    fun deleteCityById_shouldRemoveCityFromDatabase() = runTest {
        // Arrange
        cityDao.insertCity(testCity2) // favorite city
        val initialFavorites = cityDao.getFavoriteIds()
        assertTrue("City should be in favorites initially", initialFavorites.contains("2"))

        // Act
        cityDao.deleteCityById("2")

        // Assert
        val finalFavorites = cityDao.getFavoriteIds()
        assertFalse("City should be removed from favorites", finalFavorites.contains("2"))
    }

    @Test
    fun getFavoriteIds_shouldReturnOnlyFavoriteCityIds() = runTest {
        // Arrange
        cityDao.insertCity(testCity1) // not favorite
        cityDao.insertCity(testCity2) // favorite

        // Act
        val favoriteIds = cityDao.getFavoriteIds()

        // Assert
        assertEquals("Should have 1 favorite", 1, favoriteIds.size)
        assertTrue("Should contain favorite city", favoriteIds.contains("2"))
        assertFalse("Should not contain non-favorite city", favoriteIds.contains("1"))
    }

    @Test
    fun getFavoriteCitiesFlow_shouldEmitFavoriteCitiesOnly() = runTest {
        // Arrange
        cityDao.insertCity(testCity1) // not favorite
        cityDao.insertCity(testCity2) // favorite

        // Act & Assert
        cityDao.getFavoriteCitiesFlow().test {
            val favoriteCities = awaitItem()
            assertEquals("Should have 1 favorite city", 1, favoriteCities.size)
            assertEquals("Should be the correct favorite city", "2", favoriteCities[0].id)
            assertTrue("City should be marked as favorite", favoriteCities[0].isFavorite)
        }
    }

    @Test
    fun getFavoriteCitiesFlow_shouldEmitEmptyListWhenNoFavorites() = runTest {
        // Arrange
        cityDao.insertCity(testCity1) // not favorite

        // Act & Assert
        cityDao.getFavoriteCitiesFlow().test {
            val favoriteCities = awaitItem()
            assertTrue("Should have no favorite cities", favoriteCities.isEmpty())
        }
    }

    @Test
    fun toggleFavorite_shouldAddCityToFavoritesWhenNotFavorite() = runTest {
        // Arrange
        val cityId = "new_city_123"
        val initialFavorites = cityDao.getFavoriteIds()
        assertFalse("City should not be favorite initially", initialFavorites.contains(cityId))

        // Act
        val result = cityDao.toggleFavorite(cityId)

        // Assert
        assertTrue("Should return true when adding to favorites", result)
        val finalFavorites = cityDao.getFavoriteIds()
        assertTrue("City should be in favorites", finalFavorites.contains(cityId))
    }

    @Test
    fun toggleFavorite_shouldRemoveCityFromFavoritesWhenFavorite() = runTest {
        // Arrange
        cityDao.insertCity(testCity2) // favorite city
        val initialFavorites = cityDao.getFavoriteIds()
        assertTrue("City should be favorite initially", initialFavorites.contains("2"))

        // Act
        val result = cityDao.toggleFavorite("2")

        // Assert
        assertFalse("Should return false when removing from favorites", result)
        val finalFavorites = cityDao.getFavoriteIds()
        assertFalse("City should not be in favorites", finalFavorites.contains("2"))
    }

    @Test
    fun toggleFavorite_shouldCreateMinimalEntityWhenAddingToFavorites() = runTest {
        // Arrange
        val cityId = "minimal_city"

        // Act
        val result = cityDao.toggleFavorite(cityId)

        // Assert
        assertTrue("Should return true", result)
        
        cityDao.getFavoriteCitiesFlow().test {
            val favoriteCities = awaitItem()
            assertEquals("Should have 1 favorite", 1, favoriteCities.size)
            val favoriteCity = favoriteCities[0]
            assertEquals("Should have correct ID", cityId, favoriteCity.id)
            assertEquals("Should have empty name", "", favoriteCity.name)
            assertEquals("Should have empty country", "", favoriteCity.country)
            assertEquals("Should have zero coordinates", 0.0, favoriteCity.latitude, 0.001)
            assertEquals("Should have zero coordinates", 0.0, favoriteCity.longitude, 0.001)
            assertTrue("Should be marked as favorite", favoriteCity.isFavorite)
        }
    }

    @Test
    fun toggleFavorite_shouldHandleMultipleToggles() = runTest {
        // Arrange
        val cityId = "toggle_test"

        // Act & Assert - First toggle (add to favorites)
        var result = cityDao.toggleFavorite(cityId)
        assertTrue("First toggle should add to favorites", result)
        var favorites = cityDao.getFavoriteIds()
        assertTrue("Should be in favorites", favorites.contains(cityId))

        // Act & Assert - Second toggle (remove from favorites)
        result = cityDao.toggleFavorite(cityId)
        assertFalse("Second toggle should remove from favorites", result)
        favorites = cityDao.getFavoriteIds()
        assertFalse("Should not be in favorites", favorites.contains(cityId))

        // Act & Assert - Third toggle (add back to favorites)
        result = cityDao.toggleFavorite(cityId)
        assertTrue("Third toggle should add back to favorites", result)
        favorites = cityDao.getFavoriteIds()
        assertTrue("Should be in favorites again", favorites.contains(cityId))
    }

}
