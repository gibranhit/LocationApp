package com.gibran.locationapp.features.map.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gibran.locationapp.core.ui.theme.Dimens
import com.gibran.locationapp.core.ui.theme.Strings as CoreStrings
import com.gibran.locationapp.core.ui.components.ErrorCardWithRetry
import com.gibran.locationapp.core.ui.components.LocationPermissionHandler
import com.gibran.locationapp.core.ui.components.LocationPermissionRequestUI
import com.gibran.locationapp.features.map.R
import com.gibran.locationapp.features.map.R.string
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    latitude: Double,
    longitude: Double,
    cityName: String,
    countryCode: String = "",
    cityId: String = "",
    initialIsFavorite: Boolean = false,
    onBackClick: () -> Unit = {},
) {
    val viewModel: MapViewModel = hiltViewModel()
    
    // Initialize the favorite state if cityId is provided
    LaunchedEffect(cityId, initialIsFavorite) {
        if (cityId.isNotEmpty()) {
            viewModel.initializeCity(cityId, initialIsFavorite)
        }
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Clear error when it's displayed
    uiState.error?.let {
        LaunchedEffect(it) {
            // Auto-clear error after 3 seconds
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }
    
    MapScreenContent(
        latitude = latitude,
        longitude = longitude,
        cityName = cityName,
        countryCode = countryCode,
        uiState = uiState,
        onBackClick = onBackClick,
        onToggleFavorite = { viewModel.toggleFavorite() },
        onClearError = { viewModel.clearError() },
        onRetry = { viewModel.retryOperation() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreenContent(
    latitude: Double,
    longitude: Double,
    cityName: String,
    countryCode: String = "",
    uiState: MapUiState = MapUiState(),
    onBackClick: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onClearError: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    LocationPermissionHandler(
        onPermissionResult = { /* Handle result if needed */ }
    ) { permissionGranted, requestPermission ->
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.map_top_app_bar_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    actions = {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimens.spacingXl),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(
                                onClick = onToggleFavorite,
                                enabled = !uiState.isLoading
                            ) {
                                Icon(
                                    if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (uiState.isFavorite) stringResource(string.remove_from_favorites) else stringResource(string.add_to_favorites),
                                    tint = if (uiState.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(Dimens.spacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge)
            ) {
                // Error message if there's an error
                uiState.error?.let { errorMessage ->
                    ErrorCardWithRetry(
                        modifier = Modifier.fillMaxWidth(),
                        message = errorMessage,
                        onRetry = onRetry,
                        onDismiss = onClearError
                    )
                }
                
                // City information card
                CityInfoCard(
                    cityName = cityName,
                    countryCode = countryCode,
                    latitude = latitude,
                    longitude = longitude,
                    isFavorite = uiState.isFavorite,
                    isLoading = uiState.isLoading,
                    onToggleFavorite = onToggleFavorite,
                )
                
                // Real Google Map or Permission Request
                if (permissionGranted) {
                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.mapHeight)
                            .clip(RoundedCornerShape(Dimens.spacingMedium)),
                        cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), 12f)
                        },
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            zoomGesturesEnabled = true,
                            scrollGesturesEnabled = true,
                            tiltGesturesEnabled = true,
                            rotationGesturesEnabled = true,
                            compassEnabled = true,
                            mapToolbarEnabled = true,
                            myLocationButtonEnabled = true
                        ),
                        properties = MapProperties(
                            isMyLocationEnabled = true
                        )
                    ) {
                        // Marker for the city
                        Marker(
                            state = rememberMarkerState(position = LatLng(latitude, longitude)),
                            title = if (countryCode.isNotEmpty()) "$cityName, $countryCode" else cityName,
                            snippet = CoreStrings.coordinateFormat4.format(latitude, longitude)
                        )
                    }
                } else {
                    // Permission not granted
                    LocationPermissionRequestUI(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.mapHeight)
                            .clip(RoundedCornerShape(Dimens.spacingMedium)),
                        onRequestPermission = requestPermission
                    )
                }
            }
        }
    }
}

@Composable
private fun CityInfoCard(
    cityName: String,
    countryCode: String,
    latitude: Double,
    longitude: Double,
    isFavorite: Boolean,
    isLoading: Boolean,
    onToggleFavorite: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
        ) {
            // Title row with city name and favorite button
            Text(
                text = if (countryCode.isNotEmpty()) "$cityName, $countryCode" else cityName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            HorizontalDivider()
            
            // Coordinates section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.map_city_card_latitude_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CoreStrings.coordinateFormat6.format(latitude),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = stringResource(id = R.string.map_city_card_longitude_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CoreStrings.coordinateFormat6.format(longitude),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Map Screen - Favorite City")
@Composable
fun MapScreenPreview() {
    MaterialTheme {
        MapScreenContent(
            latitude = 40.7128,
            longitude = -74.0060,
            cityName = "New York",
            countryCode = "US",
            uiState = MapUiState(isFavorite = true)
        )
    }
}

@Preview(showBackground = true, name = "Map Screen - Non-Favorite City")
@Composable
fun MapScreenNotFavoritePreview() {
    MaterialTheme {
        MapScreenContent(
            latitude = 51.5074,
            longitude = -0.1278,
            cityName = "London",
            countryCode = "UK",
            uiState = MapUiState(isFavorite = false)
        )
    }
}

@Preview(showBackground = true, name = "Map Screen - With Error")
@Composable
fun MapScreenWithErrorPreview() {
    MaterialTheme {
        MapScreenContent(
            latitude = 48.8566,
            longitude = 2.3522,
            cityName = "Paris",
            countryCode = "FR",
            uiState = MapUiState(
                isFavorite = true,
                error = "Failed to update favorite: Network error occurred"
            )
        )
    }
}

@Preview(showBackground = true, name = "Map Screen - City Only")
@Composable
fun MapScreenCityOnlyPreview() {
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

@Preview(showBackground = true, name = "City Info Card")
@Composable
fun CityInfoCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(Dimens.spacingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge)
        ) {
            CityInfoCard(
                cityName = "Barcelona",
                countryCode = "ES",
                latitude = 41.3851,
                longitude = 2.1734,
                isFavorite = true,
                isLoading = false,
                onToggleFavorite = {}
            )
            CityInfoCard(
                cityName = "Sydney",
                countryCode = "AU",
                latitude = -33.8688,
                longitude = 151.2093,
                isFavorite = false,
                isLoading = false,
                onToggleFavorite = {}
            )
        }
    }
}
