package com.gibran.locationapp.data.repositories

import com.gibran.locationapp.data.models.WeatherDto
import com.gibran.locationapp.data.models.MainDto
import com.gibran.locationapp.data.models.WeatherDescriptionDto
import com.gibran.locationapp.data.models.WindDto
import com.gibran.locationapp.data.remote.WeatherApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import retrofit2.Response

@ExperimentalCoroutinesApi
class WeatherRepositoryImplTest {

    private val mockApiService = mockk<WeatherApiService>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private val testApiKey = "test_api_key"

    private lateinit var repository:WeatherRepositoryImpl

    private val testLatitude = 40.7128
    private val testLongitude = -74.0060

    private val mockWeatherDto = WeatherDto(
        main = MainDto(
            temperature = 22.5,
            humidity = 65
        ),
        weather = listOf(
            WeatherDescriptionDto(
                main = "Clouds",
                description = "partly cloudy",
                icon = "02d"
            )
        ),
        wind = WindDto(speed = 3.2),
        name = "New York"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = WeatherRepositoryImpl(
            weatherApiService = mockApiService,
            ioDispatcher = testDispatcher,
            apiKey = testApiKey
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getWeather returns success when API call succeeds`() = runTest {
        // Arrange
        val successResponse = Response.success(mockWeatherDto)
        coEvery { 
            mockApiService.getCurrentWeather(testLatitude, testLongitude, testApiKey) 
        } returns successResponse

        // Act
        val result = repository.getWeather(testLatitude, testLongitude)

        // Assert
        assertTrue(result.isSuccess)
        val weather = result.getOrNull()
        assertNotNull(weather)
        assertEquals("${testLatitude}_${testLongitude}", weather?.cityId)
        assertEquals(22.5, weather?.temperature!!, 0.01)
        assertEquals("partly cloudy", weather.description)
        assertEquals(65, weather.humidity)
        assertEquals(3.2, weather.windSpeed, 0.01)
        assertEquals("02d", weather.icon)
        
        coVerify { mockApiService.getCurrentWeather(testLatitude, testLongitude, testApiKey) }
    }

    @Test
    fun `getWeather returns failure when API returns error response`() = runTest {
        // Arrange
        val errorResponse = Response.error<WeatherDto>(404, 
            okhttp3.ResponseBody.create(null, "Not Found"))
        coEvery { 
            mockApiService.getCurrentWeather(testLatitude, testLongitude, testApiKey) 
        } returns errorResponse

        // Act
        val result = repository.getWeather(testLatitude, testLongitude)

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception?.message?.contains("API Error: 404") == true)
        
        coVerify { mockApiService.getCurrentWeather(testLatitude, testLongitude, testApiKey) }
    }

    @Test
    fun `getWeather returns failure when API returns null body`() = runTest {
        // Arrange
        val emptyResponse = Response.success<WeatherDto>(null)
        coEvery { 
            mockApiService.getCurrentWeather(testLatitude, testLongitude, testApiKey) 
        } returns emptyResponse

        // Act
        val result = repository.getWeather(testLatitude, testLongitude)

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertEquals("Empty response body", exception?.message)
        
        coVerify { mockApiService.getCurrentWeather(testLatitude, testLongitude, testApiKey) }
    }

    @Test
    fun `getWeather returns failure when API throws exception`() = runTest {
        // Arrange
        val networkException = Exception("Network error")
        coEvery { 
            mockApiService.getCurrentWeather(testLatitude, testLongitude, testApiKey) 
        } throws networkException

        // Act
        val result = repository.getWeather(testLatitude, testLongitude)

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertEquals(networkException, exception)
        assertEquals("Network error", exception?.message)
        
        coVerify { mockApiService.getCurrentWeather(testLatitude, testLongitude, testApiKey) }
    }

    @Test
    fun `getWeather uses correct API parameters`() = runTest {
        // Arrange
        val successResponse = Response.success(mockWeatherDto)
        val customLat = 51.5074
        val customLon = -0.1278
        coEvery { 
            mockApiService.getCurrentWeather(customLat, customLon, testApiKey) 
        } returns successResponse

        // Act
        val result = repository.getWeather(customLat, customLon)

        // Assert
        assertTrue(result.isSuccess)
        val weather = result.getOrNull()
        assertEquals("${customLat}_${customLon}", weather?.cityId)
        
        coVerify { mockApiService.getCurrentWeather(customLat, customLon, testApiKey) }
    }

    @Test
    fun `getWeather maps weather data correctly`() = runTest {
        // Arrange
        val detailedWeatherDto = WeatherDto(
            main = MainDto(
                temperature = 15.7,
                humidity = 80
            ),
            weather = listOf(
                WeatherDescriptionDto(
                    main = "Rain",
                    description = "light rain",
                    icon = "10n"
                )
            ),
            wind = WindDto(speed = 5.5),
            name = "London"
        )
        val successResponse = Response.success(detailedWeatherDto)
        coEvery { 
            mockApiService.getCurrentWeather(testLatitude, testLongitude, testApiKey) 
        } returns successResponse

        // Act
        val result = repository.getWeather(testLatitude, testLongitude)

        // Assert
        assertTrue(result.isSuccess)
        val weather = result.getOrNull()
        assertNotNull(weather)
        assertEquals(15.7, weather?.temperature!!, 0.01)
        assertEquals("light rain", weather.description)
        assertEquals(80, weather.humidity)
        assertEquals(5.5, weather.windSpeed, 0.01)
        assertEquals("10n", weather.icon)
        assertTrue(weather.timestamp > 0)
    }
}
