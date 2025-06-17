package com.gibran.locationapp.features.map.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gibran.locationapp.domain.usecases.GetCityByIdUseCase
import com.gibran.locationapp.domain.usecases.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInitialized: Boolean = false
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getCityByIdUseCase: GetCityByIdUseCase
) : ViewModel() {

    private var cityId: String = ""
    
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun initializeCity(id: String, initialIsFavorite: Boolean) {
        cityId = id
        _uiState.value = _uiState.value.copy(
            isFavorite = initialIsFavorite,
            isInitialized = true
        )
        refreshFavoriteStatus()
    }

    private fun refreshFavoriteStatus() {
        if (cityId.isNotEmpty()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                try {
                    val city = getCityByIdUseCase(cityId)
                    city?.let {
                        _uiState.value = _uiState.value.copy(
                            isFavorite = it.isFavorite,
                            isLoading = false,
                            error = null
                        )
                    } ?: run {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "City not found"
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load city data: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        if (cityId.isNotEmpty() && !_uiState.value.isLoading) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                try {
                    toggleFavoriteUseCase(cityId)
                    _uiState.value = _uiState.value.copy(
                        isFavorite = !_uiState.value.isFavorite,
                        isLoading = false
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to update favorite: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun retryOperation() {
        if (cityId.isNotEmpty()) {
            refreshFavoriteStatus()
        }
    }
}
