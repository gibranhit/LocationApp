package com.gibran.locationapp.features.cities.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.usecases.GetCitiesUseCase
import com.gibran.locationapp.domain.usecases.SearchCitiesUseCase
import com.gibran.locationapp.domain.usecases.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CitiesUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val showOnlyFavorites: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CitiesViewModel @Inject constructor(
    private val getCitiesUseCase: GetCitiesUseCase,
    private val searchCitiesUseCase: SearchCitiesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CitiesUiState())
    val uiState: StateFlow<CitiesUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val cities: StateFlow<List<City>> = _uiState
        .map { state -> state.searchQuery to state.showOnlyFavorites }
        .distinctUntilChanged()
        .flatMapLatest { (searchQuery, showOnlyFavorites) ->
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val citiesFlow = if (searchQuery.isNotBlank()) {
                searchCitiesUseCase(query = searchQuery)
            } else {
                getCitiesUseCase()
            }
            
            citiesFlow.map { allCities ->
                val filteredCities = if (showOnlyFavorites) {
                    allCities.filter { it.isFavorite }
                } else {
                    allCities
                }
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                filteredCities.sortedWith(compareBy<City> { it.name }.thenBy { it.country })
            }.catch { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load cities: ${e.message}"
                )
                emit(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleFavoritesFilter() {
        _uiState.value = _uiState.value.copy(
            showOnlyFavorites = !_uiState.value.showOnlyFavorites
        )
    }

    fun toggleFavorite(cityId: String) {
        viewModelScope.launch {
            runCatching {
                toggleFavoriteUseCase(cityId)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update favorite: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refreshCities() {
        val currentQuery = _uiState.value.searchQuery
        _uiState.value = _uiState.value.copy(searchQuery = "")
        _uiState.value = _uiState.value.copy(searchQuery = currentQuery)
    }
}
