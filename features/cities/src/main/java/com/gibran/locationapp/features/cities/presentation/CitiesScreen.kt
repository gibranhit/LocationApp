package com.gibran.locationapp.features.cities.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.features.cities.R
import com.gibran.locationapp.core.ui.theme.Dimens
import com.gibran.locationapp.core.ui.components.ErrorCard
import com.gibran.locationapp.core.ui.components.LoadingIndicator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitiesScreen(
    modifier: Modifier = Modifier,
    viewModel: CitiesViewModel = hiltViewModel(),
    onCityClicked: (City) -> Unit = {},
    onWeatherInfoClicked: (City) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val cities by viewModel.cities.collectAsState()
    val configuration = LocalConfiguration.current
    
    // State for selected city in landscape mode
    var selectedCity by remember { mutableStateOf<City?>(null) }
    
    if (configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
        Row(modifier = modifier.fillMaxSize()) {
            // Left side - Cities list
            Column(modifier = Modifier.weight(1f)) {
                SearchAndFilterBar(
                    uiState = uiState,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onToggleFavoritesFilter = viewModel::toggleFavoritesFilter,
                    modifier = Modifier.padding(Dimens.spacingLarge)
                )

                MessageSection(
                    uiState = uiState,
                    onClearError = viewModel::clearError,
                )

                CitiesContent(
                    uiState = uiState,
                    cities = cities,
                    onCityClicked = { city ->
                        selectedCity = city
                    },
                    onWeatherInfoClicked = onWeatherInfoClicked,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onRefresh = viewModel::refreshCities,
                    selectedCity = selectedCity
                )
            }
            
            // Right side - Map
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(Dimens.spacingLarge)
            ) {
                if (selectedCity != null) {
                    MapScreen(
                        selectedCity = selectedCity,
                        onCityDeselected = { selectedCity = null }
                    )
                } else {
                    // Placeholder when no city is selected
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.spacingXs)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Dimens.spacingXxl),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = stringResource(R.string.select_city_icon),
                                modifier = Modifier.size(Dimens.spacingXxxxl),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                            Text(
                                text = stringResource(R.string.select_city),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacingSmall))
                            Text(
                                text = stringResource(R.string.select_city_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            SearchAndFilterBar(
                uiState = uiState,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onToggleFavoritesFilter = viewModel::toggleFavoritesFilter,
                modifier = Modifier.padding(Dimens.spacingLarge)
            )

            MessageSection(
                uiState = uiState,
                onClearError = viewModel::clearError,
            )

            CitiesContent(
                uiState = uiState,
                cities = cities,
                onCityClicked = onCityClicked,
                onWeatherInfoClicked = onWeatherInfoClicked,
                onToggleFavorite = viewModel::toggleFavorite,
                onRefresh = viewModel::refreshCities
            )
        }
    }
}

@Composable
private fun MessageSection(
    uiState: CitiesUiState,
    onClearError: () -> Unit,
) {
    uiState.error?.let { error ->
        ErrorCard(
            message = error,
            modifier = Modifier.padding(
                horizontal = Dimens.spacingLarge, 
                vertical = Dimens.spacingSmall
            ),
            onDismiss = onClearError
        )
    }
}

@Composable
private fun CitiesContent(
    uiState: CitiesUiState,
    cities: List<City>,
    onCityClicked: (City) -> Unit,
    onWeatherInfoClicked: (City) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRefresh: () -> Unit,
    selectedCity: City? = null
) {
    when {
        uiState.isLoading -> LoadingState()
        cities.isEmpty() -> EmptyState(uiState, onRefresh)
        else -> CitiesList(cities, onCityClicked, onWeatherInfoClicked, onToggleFavorite, selectedCity)
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(
            text = stringResource(R.string.loading_cities),
            modifier = Modifier.padding(Dimens.spacingXxl)
        )
    }
}

@Composable
private fun EmptyState(
    uiState: CitiesUiState,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(Dimens.spacingXxl)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = stringResource(R.string.location_icon),
                modifier = Modifier.size(Dimens.spacingXxxxl),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Dimens.spacingLarge))
            Text(
                text = when {
                    uiState.searchQuery.isNotEmpty() -> stringResource(R.string.no_cities_found, uiState.searchQuery)
                    uiState.showOnlyFavorites -> stringResource(R.string.no_favorite_cities)
                    else -> stringResource(R.string.no_cities_available)
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (uiState.searchQuery.isEmpty() && !uiState.showOnlyFavorites) {
                Spacer(modifier = Modifier.height(Dimens.spacingSmall))
                Button(onClick = onRefresh) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

@Composable
private fun CitiesList(
    cities: List<City>,
    onCityClicked: (City) -> Unit,
    onWeatherInfoClicked: (City) -> Unit,
    onToggleFavorite: (String) -> Unit,
    selectedCity: City?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.spacingLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
    ) {
        item {
            Text(
                text = stringResource(R.string.cities_found, cities.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Dimens.spacingSmall)
            )
        }
        
        items(
            items = cities,
            key = { city -> city.id }
        ) { city ->
            CityItem(
                city = city,
                onCityClicked = { onCityClicked(city) },
                onWeatherInfoClicked = { onWeatherInfoClicked(city) },
                onToggleFavorite = { onToggleFavorite(city.id) },
                isSelected = selectedCity?.id == city.id
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndFilterBar(
    uiState: CitiesUiState,
    onSearchQueryChanged: (String) -> Unit,
    onToggleFavoritesFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            label = { Text(stringResource(R.string.search_cities)) },
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_icon)) },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Dimens.spacingSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.show_only_favorites),
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = uiState.showOnlyFavorites,
                onCheckedChange = { onToggleFavoritesFilter() }
            )
        }
    }
}

@Composable
private fun CityItem(
    city: City,
    onCityClicked: () -> Unit,
    onWeatherInfoClicked: () -> Unit,
    onToggleFavorite: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCityClicked() }
            .then(
                if (isSelected) Modifier.background(MaterialTheme.colorScheme.primaryContainer) else Modifier
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.spacingXs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(Dimens.spacingXs))
                
                Text(
                    text = city.coordinates,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                city.distance?.let { distance ->
                    Text(
                        text = stringResource(R.string.distance_away, distance),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onWeatherInfoClicked) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = stringResource(R.string.weather_info),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (city.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (city.isFavorite) stringResource(R.string.remove_from_favorites) else stringResource(R.string.add_to_favorites),
                        tint = if (city.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MapScreen(
    selectedCity: City?,
    onCityDeselected: () -> Unit
) {
    if (selectedCity == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.no_cities_on_map))
        }
    } else {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                LatLng(selectedCity.latitude, selectedCity.longitude),
                10f // Better zoom for individual city view
            )
        }

        val markerState = rememberMarkerState(position = LatLng(selectedCity.latitude, selectedCity.longitude))
        
        // Update camera position when selected city changes
        LaunchedEffect(selectedCity) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(selectedCity.latitude, selectedCity.longitude),
                    10f
                )
            )
            markerState.position = LatLng(selectedCity.latitude, selectedCity.longitude)
        }
        
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                zoomGesturesEnabled = true,
                scrollGesturesEnabled = true,
                compassEnabled = true
            )
        ) {
            Marker(
                state = markerState,
                title = selectedCity.displayName,
                snippet = selectedCity.coordinates,
            )
        }
        
        // Show selected city info overlay
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingLarge),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.spacingSmall)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.spacingLarge),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.location_icon),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.spacingXl)
                )
                Spacer(modifier = Modifier.width(Dimens.spacingMedium))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedCity.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedCity.coordinates,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onCityDeselected
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Sample data for previews
private val sampleCities = listOf(
    City(
        id = "1",
        name = "New York",
        country = "US",
        latitude = 40.7128,
        longitude = -74.0060,
        state = "New York",
        isFavorite = true,
        distance = 125.5
    ),
    City(
        id = "2",
        name = "London",
        country = "UK", 
        latitude = 51.5074,
        longitude = -0.1278,
        isFavorite = false,
        distance = 587.2
    ),
    City(
        id = "3",
        name = "Paris",
        country = "FR",
        latitude = 48.8566,
        longitude = 2.3522,
        isFavorite = true,
        distance = 789.1
    ),
    City(
        id = "4",
        name = "Tokyo",
        country = "JP",
        latitude = 35.6762,
        longitude = 139.6503,
        isFavorite = false,
        distance = 1250.7
    )
)

// Preview functions
@Preview(showBackground = true)
@Composable
fun CitiesScreenPreview() {
    MaterialTheme {
        CitiesContent(
            uiState = CitiesUiState(
                searchQuery = "",
                showOnlyFavorites = false,
                isLoading = false
            ),
            cities = sampleCities,
            onCityClicked = {},
            onWeatherInfoClicked = {},
            onToggleFavorite = {},
            onRefresh = {},
            selectedCity = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CitiesScreenLoadingPreview() {
    MaterialTheme {
        CitiesContent(
            uiState = CitiesUiState(
                isLoading = true
            ),
            cities = emptyList(),
            onCityClicked = {},
            onWeatherInfoClicked = {},
            onToggleFavorite = {},
            onRefresh = {},
            selectedCity = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CitiesScreenEmptyPreview() {
    MaterialTheme {
        CitiesContent(
            uiState = CitiesUiState(
                searchQuery = "",
                showOnlyFavorites = false,
                isLoading = false
            ),
            cities = emptyList(),
            onCityClicked = {},
            onWeatherInfoClicked = {},
            onToggleFavorite = {},
            onRefresh = {},
            selectedCity = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CitiesScreenSearchPreview() {
    MaterialTheme {
        CitiesContent(
            uiState = CitiesUiState(
                searchQuery = "New York",
                showOnlyFavorites = false,
                isLoading = false
            ),
            cities = listOf(sampleCities[0]), // Just New York
            onCityClicked = {},
            onWeatherInfoClicked = {},
            onToggleFavorite = {},
            onRefresh = {},
            selectedCity = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CitiesScreenFavoritesPreview() {
    MaterialTheme {
        CitiesContent(
            uiState = CitiesUiState(
                searchQuery = "",
                showOnlyFavorites = true,
                isLoading = false
            ),
            cities = emptyList(),
            onCityClicked = {},
            onWeatherInfoClicked = {},
            onToggleFavorite = {},
            onRefresh = {},
            selectedCity = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CitiesScreenErrorPreview() {
    MaterialTheme {
        CitiesContent(
            uiState = CitiesUiState(
                searchQuery = "",
                showOnlyFavorites = false,
                isLoading = false,
                error = "Failed to load cities"
            ),
            cities = sampleCities,
            onCityClicked = {},
            onWeatherInfoClicked = {},
            onToggleFavorite = {},
            onRefresh = {},
            selectedCity = null
        )
    }
}

@Preview(showBackground = true, name = "Single City Item")
@Composable
private fun CityItemPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CityItem(
                city = sampleCities[0], // New York (favorite)
                onCityClicked = {},
                onWeatherInfoClicked = {},
                onToggleFavorite = {},
                isSelected = false
            )
            CityItem(
                city = sampleCities[1], // London (not favorite)
                onCityClicked = {},
                onWeatherInfoClicked = {},
                onToggleFavorite = {},
                isSelected = true // Show selected state
            )
        }
    }
}
