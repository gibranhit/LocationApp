package com.gibran.locationapp.domain.repositories

import com.gibran.locationapp.domain.models.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<Location>
    fun observeLocationUpdates(): Flow<Location>
    suspend fun hasLocationPermission(): Boolean
}