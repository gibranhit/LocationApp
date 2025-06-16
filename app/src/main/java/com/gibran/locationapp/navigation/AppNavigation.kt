package com.gibran.locationapp.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.gibran.locationapp.features.cities.presentation.CitiesScreen
import kotlinx.serialization.Serializable

// Define navigation destinations using serializable classes
@Serializable
object CitiesDestination

@Serializable
data class MapDestination(
    val latitude: Double,
    val longitude: Double,
    val cityName: String
)

@Serializable
data class CityInfoDestination(
    val cityId: String,
    val cityName: String
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
                            cityName = city.name
                        )
                    )
                },
                onCityInfoClicked = { city ->
                    navController.navigate(
                        CityInfoDestination(
                            cityId = city.id,
                            cityName = city.name
                        )
                    )
                }
            )
        }
        
        // Map screen (placeholder for now)
        composable<MapDestination> { backStackEntry ->
            val mapDestination = backStackEntry.toRoute<MapDestination>()
            MapScreen(
                latitude = mapDestination.latitude,
                longitude = mapDestination.longitude,
                cityName = mapDestination.cityName,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // City info screen (placeholder for now)
        composable<CityInfoDestination> { backStackEntry ->
            val cityInfoDestination = backStackEntry.toRoute<CityInfoDestination>()
            CityInfoScreen(
                cityId = cityInfoDestination.cityId,
                cityName = cityInfoDestination.cityName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

// Placeholder screens - these would be implemented in their respective feature modules
@Composable
private fun MapScreen(
    latitude: Double,
    longitude: Double,
    cityName: String,
    onBackClick: () -> Unit
) {
    // TODO: Implement map screen in features:map module
    Text("Map Screen for $cityName at $latitude, $longitude")
}

@Composable
private fun CityInfoScreen(
    cityId: String,
    cityName: String,
    onBackClick: () -> Unit
) {
    // TODO: Implement city info screen in features:details module  
    Text("City Info Screen for $cityName")
}
