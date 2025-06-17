package com.gibran.locationapp.features.details.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gibran.locationapp.domain.models.Weather
import com.gibran.locationapp.domain.repositories.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherDetailsViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun loadWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            // Clear error and set loading state
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            weatherRepository.getWeather(latitude, longitude)
                .onSuccess { weather ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, 
                        weather = weather,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Unknown error occurred while loading weather",
                        isLoading = false
                    )
                }
        }
    }
}

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weather: Weather? = null,
    val error: String? = null,
)
