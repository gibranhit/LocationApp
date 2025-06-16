package com.gibran.locationapp.domain.usecases

import com.gibran.locationapp.domain.models.Weather
import com.gibran.locationapp.domain.repositories.WeatherRepository
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double): Result<Weather?> =
        weatherRepository.getWeather(latitude, longitude)
}
