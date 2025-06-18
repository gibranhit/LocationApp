package com.gibran.locationapp.features.cities

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.assertCountEquals
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import android.content.res.Configuration
import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.features.cities.presentation.CitiesContent
import com.gibran.locationapp.features.cities.presentation.CitiesScreenContent
import com.gibran.locationapp.features.cities.presentation.CitiesUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before
import org.junit.After

@RunWith(AndroidJUnit4::class)
class CitiesScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        // Ensure we start in portrait mode
        device.setOrientationNatural()
        device.waitForWindowUpdate(null, 1000)
    }

    @After
    fun tearDown() {
        // Reset to natural orientation after each test
        device.setOrientationNatural()
        device.waitForWindowUpdate(null, 1000)
    }

    private val sampleCities = listOf(
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

    // Helper function to create a mock configuration
    private fun createMockConfiguration(orientation: Int): Configuration {
        return Configuration().apply {
            this.orientation = orientation
        }
    }

    // ========== REAL DEVICE ORIENTATION TESTS ==========

    @Test
    fun testRealDevicePortraitMode() {
        // Ensure portrait mode
        device.setOrientationNatural()
        device.waitForWindowUpdate(null, 2000)

        composeTestRule.setContent {
            MaterialTheme {
                CitiesScreenContent(
                    uiState = CitiesUiState(isLoading = false),
                    cities = sampleCities,
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onSearchQueryChanged = {},
                    onToggleFavoritesFilter = {},
                    onToggleFavorite = {},
                    onRefresh = {},
                    onClearError = {}
                )
            }
        }

        // Give time for composition to settle
        composeTestRule.waitForIdle()

        // In portrait mode, search field should be displayed
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show only favorites").assertIsDisplayed()

        // Cities should be displayed
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()

        // Map placeholder should NOT be displayed in portrait
        composeTestRule.onNodeWithText("Select a City").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("Choose a city from the list to view it on the map").assertIsNotDisplayed()
    }

    @Test
    fun testRealDeviceLandscapeMode() {
        // Change to landscape mode
        device.setOrientationLeft()
        device.waitForWindowUpdate(null, 3000)

        composeTestRule.setContent {
            MaterialTheme {
                CitiesScreenContent(
                    uiState = CitiesUiState(isLoading = false),
                    cities = sampleCities,
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onSearchQueryChanged = {},
                    onToggleFavoritesFilter = {},
                    onToggleFavorite = {},
                    onRefresh = {},
                    onClearError = {}
                )
            }
        }

        // Give time for composition to settle
        composeTestRule.waitForIdle()

        // In landscape mode, search field should still be displayed
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show only favorites").assertIsDisplayed()

        // Cities should be displayed
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()

        // Map placeholder SHOULD be displayed in landscape when no city is selected
        composeTestRule.onNodeWithText("Select a City").assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose a city from the list to view it on the map").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Select city").assertIsDisplayed()
    }


    @Test
    fun testRealDeviceLandscapeWithLoadingState() {
        // Change to landscape mode
        device.setOrientationLeft()
        device.waitForWindowUpdate(null, 3000)

        composeTestRule.setContent {
            MaterialTheme {
                CitiesScreenContent(
                    uiState = CitiesUiState(isLoading = true),
                    cities = emptyList(),
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onSearchQueryChanged = {},
                    onToggleFavoritesFilter = {},
                    onToggleFavorite = {},
                    onRefresh = {},
                    onClearError = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // In landscape with loading, should show both loading state AND map placeholder
        composeTestRule.onNodeWithText("Loading cities…").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select a City").assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose a city from the list to view it on the map").assertIsDisplayed()
    }

    // ========== MOCK CONFIGURATION TESTS (Faster) ==========

    @Test
    fun displaysCitiesListCorrectly() {
        composeTestRule.setContent {
            MaterialTheme {
                CitiesContent(
                    uiState = CitiesUiState(isLoading = false),
                    cities = sampleCities,
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onToggleFavorite = {},
                    onRefresh = {}
                )
            }
        }

        // Check cities count
        composeTestRule.onNodeWithText("3 cities found").assertIsDisplayed()

        // Check city names are displayed
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()
        composeTestRule.onNodeWithText("London, UK").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paris, FR").assertIsDisplayed()

        // Check coordinates are displayed
        composeTestRule.onNodeWithText("40.7128, -74.0060").assertIsDisplayed()
        composeTestRule.onNodeWithText("51.5074, -0.1278").assertIsDisplayed()
        composeTestRule.onNodeWithText("48.8566, 2.3522").assertIsDisplayed()
    }

    @Test
    fun displaysPortraitModeCorrectly() {
        val portraitConfig = createMockConfiguration(Configuration.ORIENTATION_PORTRAIT)
        
        composeTestRule.setContent {
            CompositionLocalProvider(LocalConfiguration provides portraitConfig) {
                MaterialTheme {
                    CitiesScreenContent(
                        uiState = CitiesUiState(isLoading = false),
                        cities = sampleCities,
                        onCityClicked = {},
                        onWeatherInfoClicked = {},
                        onSearchQueryChanged = {},
                        onToggleFavoritesFilter = {},
                        onToggleFavorite = {},
                        onRefresh = {},
                        onClearError = {}
                    )
                }
            }
        }

        // In portrait mode, search field should be displayed
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show only favorites").assertIsDisplayed()
        
        // Cities should be displayed
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()
        
        // Map placeholder should NOT be displayed in portrait
        composeTestRule.onNodeWithText("Select a City").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("Choose a city from the list to view it on the map").assertIsNotDisplayed()
    }

    @Test
    fun displaysLandscapeModeCorrectly() {
        val landscapeConfig = createMockConfiguration(Configuration.ORIENTATION_LANDSCAPE)
        
        composeTestRule.setContent {
            CompositionLocalProvider(LocalConfiguration provides landscapeConfig) {
                MaterialTheme {
                    CitiesScreenContent(
                        uiState = CitiesUiState(isLoading = false),
                        cities = sampleCities,
                        onCityClicked = {},
                        onWeatherInfoClicked = {},
                        onSearchQueryChanged = {},
                        onToggleFavoritesFilter = {},
                        onToggleFavorite = {},
                        onRefresh = {},
                        onClearError = {}
                    )
                }
            }
        }

        // In landscape mode, search field should still be displayed
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show only favorites").assertIsDisplayed()
        
        // Cities should be displayed
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()
        
        // Map placeholder SHOULD be displayed in landscape when no city is selected
        composeTestRule.onNodeWithText("Select a City").assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose a city from the list to view it on the map").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Select city").assertIsDisplayed()
    }

    @Test
    fun displaysMapPlaceholderInLandscapeOnly() {
        // Test Portrait - No map placeholder
        val portraitConfig = createMockConfiguration(Configuration.ORIENTATION_PORTRAIT)
        
        composeTestRule.setContent {
            CompositionLocalProvider(LocalConfiguration provides portraitConfig) {
                MaterialTheme {
                    CitiesScreenContent(
                        uiState = CitiesUiState(isLoading = false),
                        cities = sampleCities,
                        onCityClicked = {},
                        onWeatherInfoClicked = {},
                        onSearchQueryChanged = {},
                        onToggleFavoritesFilter = {},
                        onToggleFavorite = {},
                        onRefresh = {},
                        onClearError = {}
                    )
                }
            }
        }

        // Verify map elements are NOT displayed in portrait
        composeTestRule.onNodeWithText("Select a City").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("Choose a city from the list to view it on the map").assertIsNotDisplayed()
    }

    @Test
    fun displaysLandscapeWithEmptyState() {
        val landscapeConfig = createMockConfiguration(Configuration.ORIENTATION_LANDSCAPE)
        
        composeTestRule.setContent {
            CompositionLocalProvider(LocalConfiguration provides landscapeConfig) {
                MaterialTheme {
                    CitiesScreenContent(
                        uiState = CitiesUiState(
                            isLoading = false,
                            searchQuery = "",
                            showOnlyFavorites = false
                        ),
                        cities = emptyList(),
                        onCityClicked = {},
                        onWeatherInfoClicked = {},
                        onSearchQueryChanged = {},
                        onToggleFavoritesFilter = {},
                        onToggleFavorite = {},
                        onRefresh = {},
                        onClearError = {}
                    )
                }
            }
        }

        // In landscape with empty cities, should show both empty state AND map placeholder
        composeTestRule.onNodeWithText("No cities available").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select a City").assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose a city from the list to view it on the map").assertIsDisplayed()
    }

    @Test
    fun displaysLandscapeWithLoadingState() {
        val landscapeConfig = createMockConfiguration(Configuration.ORIENTATION_LANDSCAPE)
        
        composeTestRule.setContent {
            CompositionLocalProvider(LocalConfiguration provides landscapeConfig) {
                MaterialTheme {
                    CitiesScreenContent(
                        uiState = CitiesUiState(isLoading = true),
                        cities = emptyList(),
                        onCityClicked = {},
                        onWeatherInfoClicked = {},
                        onSearchQueryChanged = {},
                        onToggleFavoritesFilter = {},
                        onToggleFavorite = {},
                        onRefresh = {},
                        onClearError = {}
                    )
                }
            }
        }

        // In landscape with loading, should show both loading state AND map placeholder
        composeTestRule.onNodeWithText("Loading cities…").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select a City").assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose a city from the list to view it on the map").assertIsDisplayed()
    }

    @Test
    fun displaysSearchAndFilterElements() {
        composeTestRule.setContent {
            MaterialTheme {
                CitiesScreenContent(
                    uiState = CitiesUiState(
                        isLoading = false,
                        searchQuery = "New York",
                        showOnlyFavorites = true
                    ),
                    cities = sampleCities,
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onSearchQueryChanged = {},
                    onToggleFavoritesFilter = {},
                    onToggleFavorite = {},
                    onRefresh = {},
                    onClearError = {}
                )
            }
        }

        // Check search field elements
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
        
        // Check favorites filter
        composeTestRule.onNodeWithText("Show only favorites").assertIsDisplayed()
        
        // Check clear search button when there's a search query
        composeTestRule.onNodeWithContentDescription("Clear search").assertIsDisplayed()
    }

    @Test
    fun displaysFavoriteIconsCorrectly() {
        composeTestRule.setContent {
            MaterialTheme {
                CitiesContent(
                    uiState = CitiesUiState(isLoading = false),
                    cities = sampleCities,
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onToggleFavorite = {},
                    onRefresh = {}
                )
            }
        }

        // Check favorite and non-favorite content descriptions
        composeTestRule.onAllNodesWithContentDescription("Remove from favorites").assertCountEquals(2) // New York and Paris are favorites
        composeTestRule.onAllNodesWithContentDescription("Add to favorites").assertCountEquals(1) // London is not favorite
    }

    @Test
    fun displaysWeatherInfoButtons() {
        composeTestRule.setContent {
            MaterialTheme {
                CitiesContent(
                    uiState = CitiesUiState(isLoading = false),
                    cities = sampleCities,
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onToggleFavorite = {},
                    onRefresh = {}
                )
            }
        }

        // Check weather info buttons are displayed for each city
        composeTestRule.onAllNodesWithContentDescription("View weather information").assertCountEquals(3)
    }

    @Test
    fun displaysLoadingState() {
        composeTestRule.setContent {
            MaterialTheme {
                CitiesContent(
                    uiState = CitiesUiState(isLoading = true),
                    cities = emptyList(),
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onToggleFavorite = {},
                    onRefresh = {}
                )
            }
        }

        // Check loading message is displayed
        composeTestRule.onNodeWithText("Loading cities…").assertIsDisplayed()
    }

    @Test
    fun displaysEmptyStateWithNoSearch() {
        composeTestRule.setContent {
            MaterialTheme {
                CitiesContent(
                    uiState = CitiesUiState(
                        isLoading = false,
                        searchQuery = "",
                        showOnlyFavorites = false
                    ),
                    cities = emptyList(),
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onToggleFavorite = {},
                    onRefresh = {}
                )
            }
        }

        // Check empty state message and retry button
        composeTestRule.onNodeWithText("No cities available").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun displaysEmptyStateWithSearch() {
        composeTestRule.setContent {
            MaterialTheme {
                CitiesContent(
                    uiState = CitiesUiState(
                        isLoading = false,
                        searchQuery = "Tokyo",
                        showOnlyFavorites = false
                    ),
                    cities = emptyList(),
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onToggleFavorite = {},
                    onRefresh = {}
                )
            }
        }

        // Check empty search state message
        composeTestRule.onNodeWithText("No cities found for \"Tokyo\"").assertIsDisplayed()
        // Retry button should not be displayed for search results
        composeTestRule.onNodeWithText("Retry").assertIsNotDisplayed()
    }

    @Test
    fun displaysEmptyStateWithFavoritesFilter() {
        composeTestRule.setContent {
            MaterialTheme {
                CitiesContent(
                    uiState = CitiesUiState(
                        isLoading = false,
                        searchQuery = "",
                        showOnlyFavorites = true
                    ),
                    cities = emptyList(),
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onToggleFavorite = {},
                    onRefresh = {}
                )
            }
        }

        // Check empty favorites state message
        composeTestRule.onNodeWithText("No favorite cities yet").assertIsDisplayed()
        // Retry button should not be displayed for favorites filter
        composeTestRule.onNodeWithText("Retry").assertIsNotDisplayed()
    }

    @Test
    fun displaysErrorMessage() {
        val errorMessage = "Failed to load cities"

        composeTestRule.setContent {
            MaterialTheme {
                CitiesScreenContent(
                    uiState = CitiesUiState(
                        isLoading = false,
                        error = errorMessage
                    ),
                    cities = sampleCities,
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onSearchQueryChanged = {},
                    onToggleFavoritesFilter = {},
                    onToggleFavorite = {},
                    onRefresh = {},
                    onClearError = {}
                )
            }
        }

        // Check error message is displayed
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        
        // Check cities are still displayed with error
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()
    }

    @Test
    fun displaysLocationIcon() {
        composeTestRule.setContent {
            MaterialTheme {
                CitiesContent(
                    uiState = CitiesUiState(
                        isLoading = false,
                        searchQuery = "",
                        showOnlyFavorites = false
                    ),
                    cities = emptyList(),
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onToggleFavorite = {},
                    onRefresh = {}
                )
            }
        }

        // Check location icon is displayed in empty state
        composeTestRule.onNodeWithContentDescription("Location").assertIsDisplayed()
    }

    @Test
    fun displaysSelectedCityHighlight() {
        composeTestRule.setContent {
            MaterialTheme {
                CitiesContent(
                    uiState = CitiesUiState(isLoading = false),
                    cities = sampleCities,
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onToggleFavorite = {},
                    onRefresh = {},
                    selectedCity = sampleCities[0] // New York selected
                )
            }
        }

        // Check cities are displayed (selected city highlighting is visual)
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()
        composeTestRule.onNodeWithText("London, UK").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paris, FR").assertIsDisplayed()
    }

    @Test
    fun displaysFilteredCitiesList() {
        val filteredCities = listOf(sampleCities[0]) // Only New York

        composeTestRule.setContent {
            MaterialTheme {
                CitiesContent(
                    uiState = CitiesUiState(
                        isLoading = false,
                        searchQuery = "New York",
                        showOnlyFavorites = false
                    ),
                    cities = filteredCities,
                    onCityClicked = {},
                    onWeatherInfoClicked = {},
                    onToggleFavorite = {},
                    onRefresh = {}
                )
            }
        }

        // Check filtered results count
        composeTestRule.onNodeWithText("1 cities found").assertIsDisplayed()
        
        // Check only New York is displayed
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()
        composeTestRule.onNodeWithText("London, UK").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("Paris, FR").assertIsNotDisplayed()
    }
}
