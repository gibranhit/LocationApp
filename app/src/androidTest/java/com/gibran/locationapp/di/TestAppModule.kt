package com.gibran.locationapp.di

import com.gibran.locationapp.data.repository.FakeCitiesRepository
import com.gibran.locationapp.data.repository.FakeWeatherRepository
import com.gibran.locationapp.domain.repositories.CitiesRepository
import com.gibran.locationapp.domain.repositories.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Singleton
    fun provideCitiesRepository(): CitiesRepository = FakeCitiesRepository()

    @Provides
    @Singleton
    fun provideWeatherRepository(): WeatherRepository = FakeWeatherRepository()
}
