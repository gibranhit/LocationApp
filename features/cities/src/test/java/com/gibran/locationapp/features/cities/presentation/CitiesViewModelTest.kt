package com.gibran.locationapp.features.cities.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.usecases.GetCitiesUseCase
import com.gibran.locationapp.domain.usecases.SearchCitiesUseCase
import com.gibran.locationapp.domain.usecases.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class CitiesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val mockGetCitiesUseCase = mockk<GetCitiesUseCase>(relaxed = true)
    private val mockSearchCitiesUseCase = mockk<SearchCitiesUseCase>(relaxed = true)
    private val mockToggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>(relaxed = true)
    private lateinit var viewModel: CitiesViewModel

    private val testCities = listOf(
        City(
            id = "1",
            name = "New York",
            country = "United States",
            latitude = 40.7128,
            longitude = -74.0060,
            state = null,
            subcountry = null,
            isFavorite = false
        ),
        City(
            id = "2",
            name = "London",
            country = "United Kingdom",
            latitude = 51.5074,
            longitude = -0.1278,
            state = null,
            subcountry = null,
            isFavorite = true
        ),
        City(
            id = "3",
            name = "Paris",
            country = "France",
            latitude = 48.8566,
            longitude = 2.3522,
            state = null,
            subcountry = null,
            isFavorite = false
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Default mock behavior
        coEvery { mockGetCitiesUseCase() } returns flowOf(testCities)
        coEvery { mockSearchCitiesUseCase(any()) } returns flowOf(testCities)
        coEvery { mockToggleFavoriteUseCase(any()) } returns true

        viewModel = CitiesViewModel(
            mockGetCitiesUseCase,
            mockSearchCitiesUseCase,
            mockToggleFavoriteUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() {
        // Arrange
        // (viewModel already initialized in setup)

        // Act
        val initialState = viewModel.uiState.value

        // Assert
        assertFalse(initialState.isLoading)
        assertEquals("", initialState.searchQuery)
        assertFalse(initialState.showOnlyFavorites)
        assertNull(initialState.error)
    }

    @Test
    fun `cities should be loaded on initialization`() = runTest {
        // Act
        advanceUntilIdle()

        // Assert
        val cities = viewModel.cities.value
        assertEquals(3, cities.size)
        assertEquals("London", cities[0].name) // Should be sorted by name
        assertEquals("New York", cities[1].name)
        assertEquals("Paris", cities[2].name)
        coVerify { mockGetCitiesUseCase() }
    }

    @Test
    fun `search should update cities flow using Turbine`() = runTest {
        // Arrange
        val searchResults = listOf(testCities[1]) // Only London
        coEvery { mockSearchCitiesUseCase("London") } returns flowOf(searchResults)

        // Act & Assert using Turbine
        viewModel.cities.test {
            // Skip initial emission
            awaitItem()

            // Trigger search
            viewModel.onSearchQueryChanged("London")

            // Verify search results
            val searchedCities = awaitItem()
            assertEquals(1, searchedCities.size)
            assertEquals("London", searchedCities[0].name)

            cancelAndConsumeRemainingEvents()
        }

        // Verify search was called
        coVerify { mockSearchCitiesUseCase("London") }
    }

    @Test
    fun `onSearchQueryChanged should update search query`() = runTest {
        // Arrange
        val searchQuery = "London"

        // Act
        viewModel.onSearchQueryChanged(searchQuery)
        advanceUntilIdle()

        // Assert
        assertEquals(searchQuery, viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `toggleFavoritesFilter should filter favorite cities`() = runTest {
        // Arrange
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showOnlyFavorites)

        // Act
        viewModel.toggleFavoritesFilter()
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState.value.showOnlyFavorites)
        // Should only show favorite cities (London)
        val favoriteCities = viewModel.cities.value
        assertEquals(1, favoriteCities.size)
        assertEquals("London", favoriteCities[0].name)
        assertTrue(favoriteCities[0].isFavorite)
    }

    @Test
    fun `toggleFavorite should call ToggleFavoriteUseCase`() = runTest {
        // Arrange
        val cityId = "1"

        // Act
        viewModel.toggleFavorite(cityId)
        advanceUntilIdle()

        // Assert
        coVerify { mockToggleFavoriteUseCase(cityId) }
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `toggleFavorite should handle error correctly`() = runTest {
        // Arrange
        val cityId = "1"
        val errorMessage = "Network error"
        coEvery { mockToggleFavoriteUseCase(cityId) } throws Exception(errorMessage)

        // Act
        viewModel.toggleFavorite(cityId)
        advanceUntilIdle()

        // Assert
        assertEquals("Failed to update favorite: $errorMessage", viewModel.uiState.value.error)
        coVerify { mockToggleFavoriteUseCase(cityId) }
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        // Arrange - Set error state
        coEvery { mockToggleFavoriteUseCase(any()) } throws Exception("Test error")
        viewModel.toggleFavorite("1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // Act
        viewModel.clearError()

        // Assert
        assertNull(viewModel.uiState.value.error)
    }
}
