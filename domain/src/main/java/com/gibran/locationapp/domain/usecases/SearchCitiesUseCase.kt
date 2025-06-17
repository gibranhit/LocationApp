package com.gibran.locationapp.domain.usecases

import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.repositories.CitiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class SearchCitiesUseCase @Inject constructor(
    private val citiesRepository: CitiesRepository
) {

    suspend operator fun invoke(query: String): Flow<List<City>> {
        val trimmedQuery = query.trim()

        return if (trimmedQuery.isEmpty()) {
            citiesRepository.getCities().map { cities ->
                cities.sortedBy { it.name }
            }
        } else {
            citiesRepository.searchCities(trimmedQuery).map { cities ->
                applyPrefixMatching(cities, trimmedQuery)
            }
        }
    }

    /**
     * Applies strict prefix matching according to requirements:
     * - "A" matches "Alabama, US", "Albuquerque, US", etc. but NOT "Sydney, AU"
     * - "s" matches "Sydney, AU" (case insensitive)
     * - "Al" matches "Alabama, US" and "Albuquerque, US"
     * - "Alb" matches only "Albuquerque, US"
     */
    private fun applyPrefixMatching(cities: List<City>, query: String): List<City> {
        return cities.filter { city ->
            val targetStrings = buildTargetStrings(city)

            targetStrings.any { target ->
                target.startsWith(query, ignoreCase = true)
            }
        }.sortedBy { it.name }
    }

    /**
     * Builds all possible target strings for a city according to requirements.
     * Examples: "Alabama, US", "Alabama", etc.
     */
    private fun buildTargetStrings(city: City): List<String> {
        return listOfNotNull(
            "${city.name}, ${city.country}",
            city.displayName,
            city.name,
            city.state?.let { "${city.name}, $it" },
            city.state?.let { "${city.name}, $it, ${city.country}" }
        ).distinct()
    }

}
