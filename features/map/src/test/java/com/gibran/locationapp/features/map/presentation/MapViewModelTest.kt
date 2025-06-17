package com.gibran.locationapp.features.map.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.usecases.GetCityByIdUseCase
import com.gibran.locationapp.domain.usecases.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class MapViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val mockToggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>()
    private val mockGetCityByIdUseCase = mockk<GetCityByIdUseCase>()
    private lateinit var viewModel: MapViewModel

    private val testCityId = "test_city_id"
    private val testCity = City(
        id = testCityId,
        name = "New York",
        country = "US",
        latitude = 40.7128,
        longitude = -74.0060,
        state = "New York",
        isFavorite = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MapViewModel(mockToggleFavoriteUseCase, mockGetCityByIdUseCase)
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
        assertFalse(initialState.isFavorite)
        assertFalse(initialState.isLoading)
        assertNull(initialState.error)
        assertFalse(initialState.isInitialized)
    }

    @Test
    fun `initializeCity should set initial state correctly`() = runTest {
        // Arrange
        coEvery { mockGetCityByIdUseCase(testCityId) } returns testCity.copy(isFavorite = true)

        // Act
        viewModel.initializeCity(testCityId, true)
        advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertTrue(finalState.isFavorite)
        assertFalse(finalState.isLoading)
        assertNull(finalState.error)
        assertTrue(finalState.isInitialized)
        coVerify { mockGetCityByIdUseCase(testCityId) }
    }

    @Test
    fun `toggleFavorite should work when properly initialized`() = runTest {
        // Arrange
        coEvery { mockGetCityByIdUseCase(testCityId) } returns testCity.copy(isFavorite = false)
        coEvery { mockToggleFavoriteUseCase(testCityId) } returns true

        // Act
        viewModel.initializeCity(testCityId, false)
        advanceUntilIdle()
        viewModel.toggleFavorite()
        advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertTrue(finalState.isFavorite)
        assertFalse(finalState.isLoading)
        assertNull(finalState.error)
        coVerify { mockToggleFavoriteUseCase(testCityId) }
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        // Arrange
        coEvery { mockGetCityByIdUseCase(testCityId) } returns null
        viewModel.initializeCity(testCityId, false)
        advanceUntilIdle()

        // Verify error exists
        val errorState = viewModel.uiState.value
        assertNotNull(errorState.error)

        // Act
        viewModel.clearError()

        // Assert
        val clearedState = viewModel.uiState.value
        assertNull(clearedState.error)
    }

    @Test
    fun `initializeCity should handle city not found`() = runTest {
        // Arrange
        coEvery { mockGetCityByIdUseCase(testCityId) } returns null

        // Act
        viewModel.initializeCity(testCityId, false)
        advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertEquals("City not found", finalState.error)
        assertTrue(finalState.isInitialized)
        coVerify { mockGetCityByIdUseCase(testCityId) }
    }

    @Test
    fun `initializeCity should handle exception correctly`() = runTest {
        // Arrange
        val errorMessage = "Database error"
        coEvery { mockGetCityByIdUseCase(testCityId) } throws Exception(errorMessage)

        // Act
        viewModel.initializeCity(testCityId, false)
        advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertEquals("Failed to load city data: $errorMessage", finalState.error)
        assertTrue(finalState.isInitialized)
        coVerify { mockGetCityByIdUseCase(testCityId) }
    }

    @Test
    fun `toggleFavorite should handle error correctly`() = runTest {
        // Arrange
        val errorMessage = "Network error"
        coEvery { mockGetCityByIdUseCase(testCityId) } returns testCity.copy(isFavorite = false)
        coEvery { mockToggleFavoriteUseCase(testCityId) } throws Exception(errorMessage)

        // Act
        viewModel.initializeCity(testCityId, false)
        advanceUntilIdle()
        viewModel.toggleFavorite()
        advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isFavorite) // Should remain unchanged
        assertFalse(finalState.isLoading)
        assertEquals("Failed to update favorite: $errorMessage", finalState.error)
        coVerify { mockToggleFavoriteUseCase(testCityId) }
    }

    @Test
    fun `toggleFavorite should not work when cityId is empty`() = runTest {
        // Arrange
        // viewModel not initialized with cityId

        // Act
        viewModel.toggleFavorite()
        advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isFavorite)
        assertFalse(finalState.isLoading)
        assertNull(finalState.error)
        coVerify(exactly = 0) { mockToggleFavoriteUseCase(any()) }
    }

    @Test
    fun `retryOperation should call refreshFavoriteStatus when cityId is not empty`() = runTest {
        // Arrange
        coEvery { mockGetCityByIdUseCase(testCityId) } returns testCity.copy(isFavorite = true)
        viewModel.initializeCity(testCityId, false)
        advanceUntilIdle()

        // Change mock to return different value for retry
        coEvery { mockGetCityByIdUseCase(testCityId) } returns testCity.copy(isFavorite = false)

        // Act
        viewModel.retryOperation()
        advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isFavorite) // Should be updated from retry
        assertFalse(finalState.isLoading)
        coVerify(atLeast = 2) { mockGetCityByIdUseCase(testCityId) } // Once for init, once for retry
    }

    @Test
    fun `retryOperation should not work when cityId is empty`() = runTest {
        // Arrange
        // viewModel not initialized with cityId

        // Act
        viewModel.retryOperation()
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 0) { mockGetCityByIdUseCase(any()) }
    }

    @Test
    fun `loading state should be set correctly during initializeCity`() = runTest {
        // Arrange
        coEvery { mockGetCityByIdUseCase(testCityId) } returns testCity.copy(isFavorite = false)

        // Act
        viewModel.initializeCity(testCityId, false)

        // Assert - Check loading state during initialization
        val loadingState = viewModel.uiState.value
        assertFalse(loadingState.isLoading)
        assertTrue(loadingState.isInitialized)

        advanceUntilIdle()

        // Assert - Check final state after initialization completes
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertFalse(finalState.isFavorite)
        assertNull(finalState.error)
    }

    @Test
    fun `error state should clear previous error when retrying successfully`() = runTest {
        // Arrange - First call fails
        coEvery { mockGetCityByIdUseCase(testCityId) } throws Exception("Network error")
        viewModel.initializeCity(testCityId, false)
        advanceUntilIdle()

        // Assert - Verify error state
        val errorState = viewModel.uiState.value
        assertEquals("Failed to load city data: Network error", errorState.error)

        // Arrange - Second call succeeds
        coEvery { mockGetCityByIdUseCase(testCityId) } returns testCity.copy(isFavorite = true)

        // Act - Retry operation
        viewModel.retryOperation()
        advanceUntilIdle()

        // Assert - Verify success state and error cleared
        val successState = viewModel.uiState.value
        assertFalse(successState.isLoading)
        assertTrue(successState.isFavorite)
        assertNull(successState.error) // Error should be cleared
    }
}
