package com.gibran.locationapp.domain.usecases

import app.cash.turbine.test
import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.repositories.CitiesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class GetCitiesUseCaseTest {

    private val mockRepository = mockk<CitiesRepository>()
    private val useCase = GetCitiesUseCase(mockRepository)

    private val testCities = listOf(
        City("1", "New York", "United States", 40.7128, -74.0060, null, null, false),
        City("2", "London", "United Kingdom", 51.5074, -0.1278, null, null, true),
        City("3", "Paris", "France", 48.8566, 2.3522, null, null, false)
    )

    @Test
    fun `invoke returns cities flow from repository`() = runTest {
        coEvery { mockRepository.getCities() } returns flowOf(testCities)

        val result = useCase()

        result.test {
            val cities = awaitItem()
            assertEquals(3, cities.size)
            assertEquals("New York", cities[0].name)
            assertEquals("London", cities[1].name)
            assertEquals("Paris", cities[2].name)
            cancelAndConsumeRemainingEvents()
        }

        coVerify { mockRepository.getCities() }
    }

    @Test
    fun `invoke returns empty flow when repository returns empty`() = runTest {
        coEvery { mockRepository.getCities() } returns flowOf(emptyList())

        val result = useCase()

        result.test {
            val cities = awaitItem()
            assertTrue(cities.isEmpty())
            cancelAndConsumeRemainingEvents()
        }

        coVerify { mockRepository.getCities() }
    }

    @Test
    fun `invoke propagates repository exception`() = runTest {
        val exception = Exception("Database error")
        coEvery { mockRepository.getCities() } throws exception

        try {
            useCase()
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Database error", e.message)
        }

        coVerify { mockRepository.getCities() }
    }
}