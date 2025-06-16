package com.gibran.locationapp.domain.usecases

import com.gibran.locationapp.domain.repositories.CitiesRepository
import com.gibran.locationapp.domain.repositories.LocationRepository
import javax.inject.Inject

class UpdateCitiesDistanceUseCase @Inject constructor(
    private val citiesRepository: CitiesRepository,
    private val locationRepository: LocationRepository
) {
    
    suspend operator fun invoke(): Result<Unit> = runCatching {
        locationRepository.getCurrentLocation()
            .onSuccess { location ->
                citiesRepository.updateDistances(location.latitude, location.longitude)
            }
            .getOrThrow()
        Unit
    }
}
