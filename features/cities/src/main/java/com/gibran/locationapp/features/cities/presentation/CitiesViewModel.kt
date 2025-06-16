package com.gibran.locationapp.features.cities.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.usecases.GetCitiesUseCase
import com.gibran.locationapp.domain.usecases.SearchCitiesUseCase
import com.gibran.locationapp.domain.usecases.SearchConfig
import com.gibran.locationapp.domain.usecases.ToggleFavoriteUseCase
import com.gibran.locationapp.domain.usecases.UpdateCitiesDistanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CitiesUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val showOnlyFavorites: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class CitiesViewModel @Inject constructor(
    private val getCitiesUseCase: GetCitiesUseCase,
    private val searchCitiesUseCase: SearchCitiesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val updateCitiesDistanceUseCase: UpdateCitiesDistanceUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _showOnlyFavorites = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _successMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CitiesUiState> = combine(
        _searchQuery, _showOnlyFavorites, _isLoading, _error, _successMessage
    ) { searchQuery, showOnlyFavorites, isLoading, error, successMessage ->
        CitiesUiState(
            searchQuery = searchQuery,
            showOnlyFavorites = showOnlyFavorites,
            isLoading = isLoading,
            error = error,
            successMessage = successMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CitiesUiState()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val cities: StateFlow<List<City>> = combine(
        _searchQuery, _showOnlyFavorites
    ) { searchQuery, showOnlyFavorites ->
        searchQuery to showOnlyFavorites
    }.flatMapLatest { (searchQuery, showOnlyFavorites) ->
        _isLoading.value = true
        _error.value = null
        
        val citiesFlow = if (searchQuery.isNotBlank()) {
            searchCitiesUseCase(SearchConfig(query = searchQuery, sortByRelevance = true))
        } else {
            getCitiesUseCase()
        }
        
        citiesFlow.map { allCities ->
            _isLoading.value = allCities.isEmpty()
            
            val filteredCities = if (showOnlyFavorites) {
                allCities.filter { it.isFavorite }
            } else {
                allCities
            }
            
            filteredCities.sortedWith(compareBy<City> { it.name }.thenBy { it.country })
        }.catch { e ->
            _error.value = "Failed to load cities: ${e.message}"
            _isLoading.value = false
            emit(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    init {
        updateDistances()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesFilter() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun toggleFavorite(cityId: String) {
        viewModelScope.launch {
            runCatching {
                toggleFavoriteUseCase(cityId)
            }.onFailure { e ->
                _error.value = "Failed to update favorite: ${e.message}"
            }
        }
    }

    fun addToFavorites(city: City) {
        if (!city.isFavorite) {
            toggleFavorite(city.id)
            showSuccessMessage("${city.name} added to favorites")
        }
    }

    fun removeFromFavorites(city: City) {
        if (city.isFavorite) {
            toggleFavorite(city.id)
            showSuccessMessage("${city.name} removed from favorites")
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun refreshCities() {
        val currentQuery = _searchQuery.value
        _searchQuery.value = ""
        _searchQuery.value = currentQuery
        updateDistances()
    }

    private fun updateDistances() {
        viewModelScope.launch {
            updateCitiesDistanceUseCase()
                .onFailure { /* Silently handle - non-critical functionality */ }
        }
    }

    private fun showSuccessMessage(message: String) {
        _successMessage.value = message
        _error.value = null
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _successMessage.value = null
        }
    }
}
