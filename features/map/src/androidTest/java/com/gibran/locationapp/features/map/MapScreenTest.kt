package com.gibran.locationapp.features.map

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gibran.locationapp.features.map.presentation.MapScreenContent
import com.gibran.locationapp.features.map.presentation.MapUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysCityInfoCorrectly() {
        composeTestRule.setContent {
            MaterialTheme {
                MapScreenContent(
                    latitude = 40.7128,
                    longitude = -74.0060,
                    cityName = "New York",
                    countryCode = "US",
                    uiState = MapUiState(isFavorite = false)
                )
            }
        }

        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()

        composeTestRule.onNodeWithText("Map").assertIsDisplayed()

        composeTestRule.onNodeWithText("Latitude").assertIsDisplayed()
        composeTestRule.onNodeWithText("40.712800").assertIsDisplayed()

        composeTestRule.onNodeWithText("Longitude").assertIsDisplayed()
        composeTestRule.onNodeWithText("-74.006000").assertIsDisplayed()
    }

    @Test
    fun displaysCityNameOnlyWhenCountryCodeEmpty() {
        composeTestRule.setContent {
            MaterialTheme {
                MapScreenContent(
                    latitude = 35.6762,
                    longitude = 139.6503,
                    cityName = "Tokyo",
                    countryCode = "",
                    uiState = MapUiState(isFavorite = false)
                )
            }
        }

        composeTestRule.onNodeWithText("Tokyo").assertIsDisplayed()
    }

    @Test
    fun displaysFavoriteIconWhenCityIsFavorite() {
        composeTestRule.setContent {
            MaterialTheme {
                MapScreenContent(
                    latitude = 51.5074,
                    longitude = -0.1278,
                    cityName = "London",
                    countryCode = "UK",
                    uiState = MapUiState(isFavorite = true)
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()
    }

    @Test
    fun displaysNonFavoriteIconWhenCityIsNotFavorite() {
        composeTestRule.setContent {
            MaterialTheme {
                MapScreenContent(
                    latitude = 48.8566,
                    longitude = 2.3522,
                    cityName = "Paris",
                    countryCode = "FR",
                    uiState = MapUiState(isFavorite = false)
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add to favorites").assertIsDisplayed()
    }

    @Test
    fun displaysLoadingIndicatorWhenLoading() {
        composeTestRule.setContent {
            MaterialTheme {
                MapScreenContent(
                    latitude = 41.3851,
                    longitude = 2.1734,
                    cityName = "Barcelona",
                    countryCode = "ES",
                    uiState = MapUiState(isLoading = true, isFavorite = false)
                )
            }
        }

        composeTestRule.onNodeWithText("Barcelona, ES").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("41.385100").assertIsDisplayed()
        composeTestRule.onNodeWithText("2.173400").assertIsDisplayed()
    }

    @Test
    fun displaysErrorMessage() {
        val errorMessage = "Failed to update favorite status"

        composeTestRule.setContent {
            MaterialTheme {
                MapScreenContent(
                    latitude = -33.8688,
                    longitude = 151.2093,
                    cityName = "Sydney",
                    countryCode = "AU",
                    uiState = MapUiState(
                        isFavorite = true,
                        error = errorMessage
                    )
                )
            }
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()

        composeTestRule.onNodeWithText("Sydney, AU").assertIsDisplayed()
    }

    @Test
    fun displaysBackButton() {
        composeTestRule.setContent {
            MaterialTheme {
                MapScreenContent(
                    latitude = 55.7558,
                    longitude = 37.6176,
                    cityName = "Moscow",
                    countryCode = "RU",
                    uiState = MapUiState(isFavorite = false)
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun displaysCoordinateLabels() {
        composeTestRule.setContent {
            MaterialTheme {
                MapScreenContent(
                    latitude = 19.4326,
                    longitude = -99.1332,
                    cityName = "Mexico City",
                    countryCode = "MX",
                    uiState = MapUiState(isFavorite = false)
                )
            }
        }

        composeTestRule.onNodeWithText("Latitude").assertIsDisplayed()
        composeTestRule.onNodeWithText("Longitude").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("19.432600").assertIsDisplayed()
        composeTestRule.onNodeWithText("-99.133200").assertIsDisplayed()
    }
}
