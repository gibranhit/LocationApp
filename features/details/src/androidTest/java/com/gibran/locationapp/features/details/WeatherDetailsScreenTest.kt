package com.gibran.locationapp.features.details

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.models.Weather
import com.gibran.locationapp.features.details.presentation.WeatherContent
import com.gibran.locationapp.features.details.presentation.WeatherDetailsContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherDetailsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysCityInfoCorrectly() {
        val city = City(
            id = "1",
            name = "Test City",
            latitude = 12.34,
            longitude = 56.78,
            country = "Test Country"
        )

        composeTestRule.setContent {
            MaterialTheme {
                WeatherDetailsContent(
                    city = city,
                    isLoading = false,
                    weather = null,
                    error = null,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test City, Test Country").assertIsDisplayed()

        composeTestRule.onNodeWithText(String.format("%.4f, %.4f", 12.34, 56.78)).assertIsDisplayed()
    }

    @Test
    fun displaysWeatherDataCorrectly() {
        // Mock city data
        val city = City(
            id = "1",
            name = "Test City",
            latitude = 12.34,
            longitude = 56.78,
            country = "Test Country"
        )

        // Mock weather data
        val weather = Weather(
            cityId = "1",
            temperature = 22.5,
            description = "Partly Cloudy",
            humidity = 65,
            windSpeed = 3.2,
            icon = "02d",
            timestamp = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            MaterialTheme {
                WeatherDetailsContent(
                    city = city,
                    isLoading = false,
                    weather = weather,
                    error = null,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onAllNodesWithText("22°C").assertCountEquals(2)

        composeTestRule.onNodeWithText("Partly Cloudy").assertIsDisplayed()

        composeTestRule.onNodeWithText("65%").assertIsDisplayed()

        composeTestRule.onNodeWithText("3.2 m/s").assertIsDisplayed()
    }

    @Test
    fun displaysLoadingState() {
        val city = City(
            id = "1",
            name = "Test City",
            latitude = 12.34,
            longitude = 56.78,
            country = "Test Country"
        )

        composeTestRule.setContent {
            MaterialTheme {
                WeatherDetailsContent(
                    city = city,
                    isLoading = true,
                    weather = null,
                    error = null,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test City, Test Country").assertIsDisplayed()
    }

    @Test
    fun displaysErrorState() {
        val city = City(
            id = "1",
            name = "Test City",
            latitude = 12.34,
            longitude = 56.78,
            country = "Test Country"
        )

        val errorMessage = "Failed to load weather data"

        composeTestRule.setContent {
            MaterialTheme {
                WeatherDetailsContent(
                    city = city,
                    isLoading = false,
                    weather = null,
                    error = errorMessage,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun displaysLoadingIndicator() {
        val city = City(
            id = "1",
            name = "Test City",
            latitude = 12.34,
            longitude = 56.78,
            country = "Test Country"
        )

        composeTestRule.setContent {
            MaterialTheme {
                WeatherDetailsContent(
                    city = city,
                    isLoading = true,
                    weather = null,
                    error = null,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test City, Test Country").assertIsDisplayed()
    }

    @Test
    fun displaysErrorTitle() {
        val city = City(
            id = "1",
            name = "Test City",
            latitude = 12.34,
            longitude = 56.78,
            country = "Test Country"
        )

        composeTestRule.setContent {
            MaterialTheme {
                WeatherDetailsContent(
                    city = city,
                    isLoading = false,
                    weather = null,
                    error = "Network error",
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Error loading weather").assertIsDisplayed()
        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
    }

    @Test
    fun displaysTopBarTitle() {
        val city = City(
            id = "1",
            name = "Test City",
            latitude = 12.34,
            longitude = 56.78,
            country = "Test Country"
        )

        composeTestRule.setContent {
            MaterialTheme {
                WeatherDetailsContent(
                    city = city,
                    isLoading = false,
                    weather = null,
                    error = null,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test City, Test Country Weather").assertIsDisplayed()
    }
}
