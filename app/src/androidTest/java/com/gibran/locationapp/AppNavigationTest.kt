package com.gibran.locationapp

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gibran.locationapp.data.di.RepositoryModule
import com.gibran.locationapp.navigation.AppNavigation
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(RepositoryModule::class)
class AppNavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun appNavigation_startsAtCitiesDestination() {
        // Arrange
        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            MaterialTheme {
                AppNavigation(navController)
            }
        }

        // Assert - Cities Screen should be displayed
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show only favorites").assertIsDisplayed()
        
        // Cities should be displayed from fake repository
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()
        composeTestRule.onNodeWithText("London, UK").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paris, FR").assertIsDisplayed()
    }

    @Test
    fun appNavigation_clickCityNavigatesToMap() {
        // Arrange
        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            MaterialTheme {
                AppNavigation(navController)
            }
        }

        // Assert - Cities Screen should be displayed
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()

        // Click on a city to navigate to map
        composeTestRule.onNodeWithText("New York, US").performClick()

        // Assert - Map Screen should be displayed
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun appNavigation_clickWeatherInfoNavigatesToWeather() {
        // Arrange
        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            MaterialTheme {
                AppNavigation(navController)
            }
        }

        // Assert - Cities Screen should be displayed
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()

        // Click on weather info button for a city
        composeTestRule.onAllNodesWithContentDescription("View weather information")[1].performClick()

        // Assert - Weather Screen should be displayed with fake data
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("22°C").assertCountEquals(2) // From fake weather data
        composeTestRule.onNodeWithText("Partly Cloudy").assertIsDisplayed()
    }

    @Test
    fun appNavigation_backNavigationFromMapToCities() {
        // Arrange
        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            MaterialTheme {
                AppNavigation(navController)
            }
        }

        // Click on a city to navigate to map
        composeTestRule.onNodeWithText("New York, US").performClick()

        // Navigate back to cities
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // Assert - Back to Cities Screen
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()
    }

    @Test
    fun appNavigation_backNavigationFromWeatherToCities() {
        // Arrange
        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            MaterialTheme {
                AppNavigation(navController)
            }
        }

        // Click on weather info button for a city
        composeTestRule.onAllNodesWithContentDescription("View weather information")[0].performClick()

        // Navigate back to cities
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // Assert - Back to Cities Screen
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()
        composeTestRule.onNodeWithText("New York, US").assertIsDisplayed()
    }

    @Test
    fun appNavigation_mapDestinationWithParameters() {
        // Arrange
        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            MaterialTheme {
                AppNavigation(navController)
            }
        }

        // Click on a city to navigate to map
        composeTestRule.onNodeWithText("London, UK").performClick()

        // Assert - Map Screen should be displayed with London parameters
        composeTestRule.onNodeWithText("London, UK").assertIsDisplayed()
    }

    @Test
    fun appNavigation_multipleNavigationSteps() {
        // Arrange
        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            MaterialTheme {
                AppNavigation(navController)
            }
        }

        // Start at Cities Screen
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paris, FR").assertIsDisplayed()

        // Navigate to Map
        composeTestRule.onNodeWithText("Paris, FR").performClick()
        composeTestRule.onNodeWithText("Paris, FR").assertIsDisplayed() // Map screen title

        // Navigate back to Cities
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()

        // Navigate to Weather
        composeTestRule.onAllNodesWithContentDescription("View weather information")[2].performClick() // Paris weather
        composeTestRule.onNodeWithText("Paris, FR Weather").assertIsDisplayed()

        // Navigate back to Cities
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()
    }
}
