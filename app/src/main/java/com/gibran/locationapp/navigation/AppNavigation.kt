package com.gibran.locationapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.gibran.locationapp.features.cities.presentation.CitiesScreen
import com.gibran.locationapp.features.details.presentation.WeatherDetailsScreen
import com.gibran.locationapp.features.map.presentation.MapScreen
import com.gibran.locationapp.domain.models.City
import kotlinx.serialization.Serializable

// Define navigation destinations using serializable classes
@Serializable
object CitiesDestination

@Serializable
data class MapDestination(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val countryCode: String = "",
    val cityId: String = "",
    val isFavorite: Boolean = false
)

@Serializable
data class WeatherDestination(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val countryCode: String = "",
    val cityId: String = "",
    val state: String? = null,
    val isFavorite: Boolean = false
)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = CitiesDestination
    ) {
        // Cities screen
        composable<CitiesDestination> {
            CitiesScreen(
                onCityClicked = { city ->
                    navController.navigate(
                        MapDestination(
                            latitude = city.latitude,
                            longitude = city.longitude,
                            cityName = city.name,
                            countryCode = city.country,
                            cityId = city.id,
                            isFavorite = city.isFavorite
                        )
                    )
                },
                onWeatherInfoClicked = { city ->
                    navController.navigate(
                        WeatherDestination(
                            latitude = city.latitude,
                            longitude = city.longitude,
                            cityName = city.name,
                            countryCode = city.country,
                            cityId = city.id,
                            state = city.state,
                            isFavorite = city.isFavorite
                        )
                    )
                }
            )
        }
        
        // Map screen using the actual implementation
        composable<MapDestination> { backStackEntry ->
            val mapDestination = backStackEntry.toRoute<MapDestination>()
            MapScreen(
                latitude = mapDestination.latitude,
                longitude = mapDestination.longitude,
                cityName = mapDestination.cityName,
                countryCode = mapDestination.countryCode,
                cityId = mapDestination.cityId,
                initialIsFavorite = mapDestination.isFavorite,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Weather details screen
        composable<WeatherDestination> { backStackEntry ->
            val weatherDestination = backStackEntry.toRoute<WeatherDestination>()
            val city = City(
                id = weatherDestination.cityId,
                name = weatherDestination.cityName,
                country = weatherDestination.countryCode,
                latitude = weatherDestination.latitude,
                longitude = weatherDestination.longitude,
                state = weatherDestination.state,
                isFavorite = weatherDestination.isFavorite
            )
            WeatherDetailsScreen(
                city = city,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
