package com.gibran.locationapp.features.details.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gibran.locationapp.core.ui.theme.Dimens
import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.models.Weather
import com.gibran.locationapp.features.details.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailsScreen(
    city: City,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeatherDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(city) {
        viewModel.loadWeather(city.latitude, city.longitude)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.weather_title, city.displayName)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.spacingLarge)
        ) {
            // City Info Card
            CityInfoCard(city = city)
            
            Spacer(modifier = Modifier.height(Dimens.spacingLarge))
            
            // Weather Content
            if (uiState.isLoading){
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.weather?.let {
                WeatherContent(weather = it)
            }

            uiState.error?.let{
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.spacingLarge)
                    ) {
                        Text(
                            text = stringResource(R.string.error_loading_weather),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CityInfoCard(
    city: City,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingLarge)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.location),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.spacingXl)
                )
                Spacer(modifier = Modifier.width(Dimens.spacingMedium))
                Column {
                    Text(
                        text = city.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = city.coordinates,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherContent(
    weather: Weather,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge)
    ) {
        // Main Weather Card
        WeatherMainCard(weather = weather)
        
        // Weather Details
        WeatherDetailsCard(weather = weather)
    }
}

@Composable
private fun WeatherMainCard(
    weather: Weather,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingXl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Temperature
            Text(
                text = stringResource(R.string.temperature_celsius, weather.temperature.toInt()),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(Dimens.spacingSmall))
            
            // Weather Description
            Text(
                text = weather.description.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Dimens.spacingMedium))
            
            // Weather Icon Placeholder
            Icon(
                Icons.Default.WbSunny,
                contentDescription = weather.description,
                modifier = Modifier.size(Dimens.spacingXxxxl),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun WeatherDetailsCard(
    weather: Weather,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingLarge)
        ) {
            Text(
                text = stringResource(R.string.weather_details),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Dimens.spacingMedium)
            )
            
            // Weather details grid
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
            ) {
                WeatherDetailItem(
                    icon = Icons.Default.Thermostat,
                    label = stringResource(R.string.temperature),
                    value = stringResource(R.string.temperature_celsius, weather.temperature.toInt())
                )
                
                WeatherDetailItem(
                    icon = Icons.Default.Water,
                    label = stringResource(R.string.humidity),
                    value = stringResource(R.string.humidity_percentage, weather.humidity)
                )
                
                WeatherDetailItem(
                    icon = Icons.Default.Air,
                    label = stringResource(R.string.wind_speed),
                    value = stringResource(R.string.wind_speed_ms, weather.windSpeed)
                )
                
                WeatherDetailItem(
                    icon = Icons.Default.Schedule,
                    label = stringResource(R.string.last_updated),
                    value = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(weather.timestamp))
                )
            }
        }
    }
}

@Composable
private fun WeatherDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.spacingXl)
        )
        Spacer(modifier = Modifier.width(Dimens.spacingMedium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private val sampleWeather = Weather(
    cityId = "1",
    temperature = 22.5,
    description = "partly cloudy",
    humidity = 65,
    windSpeed = 3.2,
    icon = "02d",
    timestamp = System.currentTimeMillis()
)

@Preview(showBackground = true)
@Composable
fun WeatherDetailsScreenPreview() {
    MaterialTheme {
        WeatherContent(weather = sampleWeather)
    }
}
