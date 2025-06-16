package com.gibran.locationapp.domain.usecases

import com.gibran.locationapp.domain.repositories.CitiesRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val citiesRepository: CitiesRepository
) {
    suspend operator fun invoke(cityId: String): Boolean {
        return citiesRepository.toggleFavorite(cityId)
    }
}