package com.gibran.locationapp.domain.usecases

import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.repositories.CitiesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCitiesUseCase @Inject constructor(
    private val citiesRepository: CitiesRepository
) {
    suspend operator fun invoke(): Flow<List<City>> {
        return citiesRepository.getCities()
    }
}
