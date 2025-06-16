package com.gibran.locationapp.features.cities.presentation

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gibran.locationapp.domain.models.City

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitiesScreen(
    modifier: Modifier = Modifier,
    viewModel: CitiesViewModel = hiltViewModel(),
    onCityClicked: (City) -> Unit = {},
    onCityInfoClicked: (City) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val cities by viewModel.cities.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        SearchAndFilterBar(
            uiState = uiState,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onToggleFavoritesFilter = viewModel::toggleFavoritesFilter,
            modifier = Modifier.padding(16.dp)
        )

        MessageSection(
            uiState = uiState,
            onClearError = viewModel::clearError,
            onClearSuccess = viewModel::clearSuccessMessage
        )

        CitiesContent(
            uiState = uiState,
            cities = cities,
            onCityClicked = onCityClicked,
            onCityInfoClicked = onCityInfoClicked,
            onToggleFavorite = viewModel::toggleFavorite,
            onRefresh = viewModel::refreshCities
        )
    }
}

@Composable
private fun MessageSection(
    uiState: CitiesUiState,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit
) {
    uiState.error?.let { error ->
        MessageCard(
            message = error,
            isError = true,
            onDismiss = onClearError
        )
    }

    uiState.successMessage?.let { message ->
        MessageCard(
            message = message,
            isError = false,
            onDismiss = onClearSuccess
        )
    }
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = if (isError) 
                    MaterialTheme.colorScheme.onErrorContainer 
                else 
                    MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = if (isError) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun CitiesContent(
    uiState: CitiesUiState,
    cities: List<City>,
    onCityClicked: (City) -> Unit,
    onCityInfoClicked: (City) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRefresh: () -> Unit
) {
    when {
        uiState.isLoading -> LoadingState()
        cities.isEmpty() -> EmptyState(uiState, onRefresh)
        else -> CitiesList(cities, onCityClicked, onCityInfoClicked, onToggleFavorite)
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading cities...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
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
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when {
                    uiState.searchQuery.isNotEmpty() -> "No cities found for \"${uiState.searchQuery}\""
                    uiState.showOnlyFavorites -> "No favorite cities yet"
                    else -> "No cities available"
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (uiState.searchQuery.isEmpty() && !uiState.showOnlyFavorites) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onRefresh) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun CitiesList(
    cities: List<City>,
    onCityClicked: (City) -> Unit,
    onCityInfoClicked: (City) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "${cities.size} cities found",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(
            items = cities,
            key = { city -> city.id }
        ) { city ->
            CityItem(
                city = city,
                onCityClicked = { onCityClicked(city) },
                onToggleFavorite = { onToggleFavorite(city.id) },
                onInfoClicked = { onCityInfoClicked(city) }
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
            label = { Text("Search cities") },
            placeholder = { Text("Start typing city name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Show only favorites",
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
    onToggleFavorite: () -> Unit,
    onInfoClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCityClicked() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = city.coordinates,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                city.distance?.let { distance ->
                    Text(
                        text = String.format("%.1f km away", distance),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onInfoClicked) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "City information",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (city.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (city.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (city.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
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
            onCityInfoClicked = {},
            onToggleFavorite = {},
            onRefresh = {}
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
            onCityInfoClicked = {},
            onToggleFavorite = {},
            onRefresh = {}
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
            onCityInfoClicked = {},
            onToggleFavorite = {},
            onRefresh = {}
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
            onCityInfoClicked = {},
            onToggleFavorite = {},
            onRefresh = {}
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
            onCityInfoClicked = {},
            onToggleFavorite = {},
            onRefresh = {}
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
            onCityInfoClicked = {},
            onToggleFavorite = {},
            onRefresh = {}
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
                onToggleFavorite = {},
                onInfoClicked = {}
            )
            CityItem(
                city = sampleCities[1], // London (not favorite)
                onCityClicked = {},
                onToggleFavorite = {},
                onInfoClicked = {}
            )
        }
    }
}
