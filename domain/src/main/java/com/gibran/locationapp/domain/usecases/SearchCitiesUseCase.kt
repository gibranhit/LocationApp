package com.gibran.locationapp.domain.usecases

import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.repositories.CitiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class SearchConfig(
    val query: String,
    val minQueryLength: Int = 1,
    val sortByRelevance: Boolean = true,
    val includeFavoritesFirst: Boolean = false
)

class SearchCitiesUseCase @Inject constructor(
    private val citiesRepository: CitiesRepository
) {
    
    suspend operator fun invoke(query: String): Flow<List<City>> = 
        citiesRepository.searchCities(query.trim())
    
    suspend operator fun invoke(searchConfig: SearchConfig): Flow<List<City>> {
        val trimmedQuery = searchConfig.query.trim()
        
        if (trimmedQuery.length < searchConfig.minQueryLength) {
            return citiesRepository.searchCities("")
        }
        
        return citiesRepository.searchCities(trimmedQuery).map { cities ->
            when {
                searchConfig.sortByRelevance && searchConfig.includeFavoritesFirst -> 
                    cities.sortedWith(
                        compareBy<City> { !it.isFavorite }
                            .thenBy { getRelevanceScore(it, trimmedQuery) }
                            .thenBy { it.name }
                    )
                searchConfig.sortByRelevance -> 
                    cities.sortedWith(
                        compareBy<City> { getRelevanceScore(it, trimmedQuery) }
                            .thenBy { it.name }
                    )
                searchConfig.includeFavoritesFirst -> 
                    cities.sortedWith(
                        compareBy<City> { !it.isFavorite }
                            .thenBy { it.name }
                    )
                else -> cities.sortedBy { it.name }
            }
        }
    }
    
    private fun getRelevanceScore(city: City, query: String): Int {
        val lowerQuery = query.lowercase()
        val cityName = city.name.lowercase()
        val countryName = city.country.lowercase()
        val stateName = city.state?.lowercase()
        
        return when {
            cityName == lowerQuery || countryName == lowerQuery || stateName == lowerQuery -> 0
            cityName.startsWith(lowerQuery) -> 1
            countryName.startsWith(lowerQuery) -> 2
            stateName?.startsWith(lowerQuery) == true -> 3
            cityName.contains(lowerQuery) -> 4
            countryName.contains(lowerQuery) -> 5
            stateName?.contains(lowerQuery) == true -> 6
            else -> 7
        }
    }
}
