package com.gibran.locationapp.data.repositories

import com.gibran.locationapp.data.di.IoDispatcher
import com.gibran.locationapp.data.local.entities.WeatherEntity
import com.gibran.locationapp.data.models.toDomain
import com.gibran.locationapp.data.remote.WeatherApiService
import com.gibran.locationapp.domain.models.Weather
import com.gibran.locationapp.domain.repositories.WeatherRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @Named("weather_api_key") private val apiKey: String
) : WeatherRepository {

    override suspend fun getWeather(latitude: Double, longitude: Double): Result<Weather> =
        withContext(ioDispatcher) {
            try {
                val response = weatherApiService.getCurrentWeather(
                    latitude = latitude,
                    longitude = longitude,
                    apiKey = apiKey,
                )

                if (response.isSuccessful) {
                    response.body()?.let { weatherDto ->
                        val cityId = "${latitude}_${longitude}"
                        val weather = weatherDto.toDomain(cityId)
                        Result.success(weather)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
