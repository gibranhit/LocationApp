package com.gibran.locationapp.domain.usecases

import com.gibran.locationapp.domain.models.Location
import com.gibran.locationapp.domain.repositories.LocationRepository
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): Result<Location> = 
        locationRepository.getCurrentLocation()
}
