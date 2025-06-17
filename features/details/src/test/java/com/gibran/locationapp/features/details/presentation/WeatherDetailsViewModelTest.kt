package com.gibran.locationapp.features.details.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.gibran.locationapp.domain.models.Weather
import com.gibran.locationapp.domain.repositories.WeatherRepository
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
class WeatherDetailsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val mockWeatherRepository = mockk<WeatherRepository>()
    private lateinit var viewModel: WeatherDetailsViewModel

    private val testLatitude = 40.7128
    private val testLongitude = -74.0060

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WeatherDetailsViewModel(mockWeatherRepository)
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
        assertNull(initialState.weather)
        assertNull(initialState.error)
    }

    @Test
    fun `loadWeather should load weather successfully`() = runTest {
        // Arrange
        val mockWeather = Weather(
            cityId = "${testLatitude}_${testLongitude}",
            temperature = 22.5,
            description = "partly cloudy",
            humidity = 65,
            windSpeed = 3.2,
            icon = "02d",
            timestamp = System.currentTimeMillis()
        )
        coEvery { mockWeatherRepository.getWeather(testLatitude, testLongitude) } returns Result.success(mockWeather)

        // Act
        viewModel.loadWeather(testLatitude, testLongitude)
        advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertEquals(mockWeather, finalState.weather)
        assertNull(finalState.error)
        coVerify { mockWeatherRepository.getWeather(testLatitude, testLongitude) }
    }

    @Test
    fun `loadWeather should handle error correctly`() = runTest {
        // Arrange
        val errorMessage = "Network error"
        coEvery { mockWeatherRepository.getWeather(testLatitude, testLongitude) } returns Result.failure(Exception(errorMessage))

        // Act
        viewModel.loadWeather(testLatitude, testLongitude)
        advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.weather)
        assertEquals(errorMessage, finalState.error)
        coVerify { mockWeatherRepository.getWeather(testLatitude, testLongitude) }
    }

    @Test
    fun `loadWeather should handle unknown error`() = runTest {
        // Arrange
        coEvery { mockWeatherRepository.getWeather(testLatitude, testLongitude) } returns Result.failure(Exception())

        // Act
        viewModel.loadWeather(testLatitude, testLongitude)
        advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.weather)
        assertEquals("Unknown error occurred while loading weather", finalState.error)
        coVerify { mockWeatherRepository.getWeather(testLatitude, testLongitude) }
    }

    @Test
    fun `repository should be called with correct parameters`() = runTest {
        // Arrange
        val mockWeather = Weather(
            cityId = "${testLatitude}_${testLongitude}",
            temperature = 30.0,
            description = "hot",
            humidity = 30,
            windSpeed = 1.0,
            icon = "01d",
            timestamp = System.currentTimeMillis()
        )
        coEvery { mockWeatherRepository.getWeather(testLatitude, testLongitude) } returns Result.success(mockWeather)

        // Act
        viewModel.loadWeather(testLatitude, testLongitude)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { mockWeatherRepository.getWeather(testLatitude, testLongitude) }
    }

    @Test
    fun `error state should be cleared when loading new weather successfully`() = runTest {
        // Arrange - First call fails
        coEvery { mockWeatherRepository.getWeather(testLatitude, testLongitude) } returns Result.failure(Exception("API error"))

        // Act - First call (should fail)
        viewModel.loadWeather(testLatitude, testLongitude)
        advanceUntilIdle()

        // Assert - Verify error state
        val errorState = viewModel.uiState.value
        assertEquals("API error", errorState.error)
        assertNull(errorState.weather)

        // Arrange - Second call succeeds
        val mockWeather = Weather(
            cityId = "${testLatitude}_${testLongitude}",
            temperature = 18.0,
            description = "clear sky",
            humidity = 55,
            windSpeed = 2.8,
            icon = "01d",
            timestamp = System.currentTimeMillis()
        )
        coEvery { mockWeatherRepository.getWeather(testLatitude, testLongitude) } returns Result.success(mockWeather)

        // Act - Second call (should succeed)
        viewModel.loadWeather(testLatitude, testLongitude)
        advanceUntilIdle()

        // Assert - Verify success state and error cleared
        val successState = viewModel.uiState.value
        assertFalse(successState.isLoading)
        assertEquals(mockWeather, successState.weather)
        assertNull(successState.error) // Error should be cleared
    }
}
