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
class SearchCitiesUseCaseTest {

    private val mockRepository = mockk<CitiesRepository>()
    private val useCase = SearchCitiesUseCase(mockRepository)
    
    private val testCities = listOf(
        City("1", "Alabama", "US", 32.377716, -86.300568, null, null, false),
        City("2", "Albuquerque", "US", 35.0841, -106.6511, null, null, false),
        City("3", "Anaheim", "US", 33.8358, -117.9142, null, null, false),
        City("4", "Arizona", "US", 34.1682, -111.9309, null, null, false),
        City("5", "Sydney", "AU", -33.8688, 151.2093, null, null, false)
    )

    @Test
    fun `prefix A returns all cities except Sydney`() = runTest {
        coEvery { mockRepository.searchCities("A") } returns flowOf(testCities)
        
        val result = useCase("A")
        
        result.test {
            val cities = awaitItem()
            val cityNames = cities.map { it.name }
            assertTrue(cityNames.contains("Alabama"))
            assertTrue(cityNames.contains("Albuquerque"))
            assertTrue(cityNames.contains("Anaheim"))
            assertTrue(cityNames.contains("Arizona"))
            assertFalse(cityNames.contains("Sydney"))
            cancelAndConsumeRemainingEvents()
        }
        
        coVerify { mockRepository.searchCities("A") }
    }

    @Test
    fun `prefix s returns only Sydney`() = runTest {
        coEvery { mockRepository.searchCities("s") } returns flowOf(testCities)
        
        val result = useCase("s")
        
        result.test {
            val cities = awaitItem()
            assertEquals(1, cities.size)
            assertEquals("Sydney", cities[0].name)
            assertEquals("AU", cities[0].country)
            cancelAndConsumeRemainingEvents()
        }
        
        coVerify { mockRepository.searchCities("s") }
    }

    @Test
    fun `prefix Al returns Alabama and Albuquerque`() = runTest {
        coEvery { mockRepository.searchCities("Al") } returns flowOf(testCities)
        
        val result = useCase("Al")
        
        result.test {
            val cities = awaitItem()
            assertEquals(2, cities.size)
            val cityNames = cities.map { it.name }.sorted()
            assertEquals(listOf("Alabama", "Albuquerque"), cityNames)
            cancelAndConsumeRemainingEvents()
        }
        
        coVerify { mockRepository.searchCities("Al") }
    }

    @Test
    fun `prefix Alb returns only Albuquerque`() = runTest {
        coEvery { mockRepository.searchCities("Alb") } returns flowOf(testCities)
        
        val result = useCase("Alb")
        
        result.test {
            val cities = awaitItem()
            assertEquals(1, cities.size)
            assertEquals("Albuquerque", cities[0].name)
            assertEquals("US", cities[0].country)
            cancelAndConsumeRemainingEvents()
        }
        
        coVerify { mockRepository.searchCities("Alb") }
    }

    @Test
    fun `empty query returns all cities sorted`() = runTest {
        coEvery { mockRepository.getCities() } returns flowOf(testCities)
        
        val result = useCase("")
        
        result.test {
            val cities = awaitItem()
            assertEquals(5, cities.size)
            val cityNames = cities.map { it.name }
            assertEquals(cityNames.sorted(), cityNames)
            cancelAndConsumeRemainingEvents()
        }
        
        coVerify { mockRepository.getCities() }
    }

    @Test
    fun `case insensitive search works`() = runTest {
        coEvery { mockRepository.searchCities("al") } returns flowOf(testCities)
        
        val result = useCase("al")
        
        result.test {
            val cities = awaitItem()
            assertEquals(2, cities.size)
            val cityNames = cities.map { it.name }.sorted()
            assertEquals(listOf("Alabama", "Albuquerque"), cityNames)
            cancelAndConsumeRemainingEvents()
        }
        
        coVerify { mockRepository.searchCities("al") }
    }

    @Test
    fun `results are sorted alphabetically`() = runTest {
        coEvery { mockRepository.searchCities("A") } returns flowOf(testCities.reversed())
        
        val result = useCase("A")
        
        result.test {
            val cities = awaitItem()
            val cityNames = cities.map { it.name }
            assertEquals(cityNames.sorted(), cityNames)
            cancelAndConsumeRemainingEvents()
        }
        
        coVerify { mockRepository.searchCities("A") }
    }

    @Test
    fun `handles repository exception`() = runTest {
        coEvery { mockRepository.searchCities("error") } throws Exception("Search failed")
        
        try {
            useCase("error")
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Search failed", e.message)
        }
        
        coVerify { mockRepository.searchCities("error") }
    }
}
