package com.gibran.locationapp.domain.usecases

import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.repositories.CitiesRepository
import javax.inject.Inject

class GetCityByIdUseCase @Inject constructor(
    private val citiesRepository: CitiesRepository
) {
    suspend operator fun invoke(cityId: String): City? {
        return citiesRepository.getCityById(cityId)
    }
}
