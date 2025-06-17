package com.gibran.locationapp.domain.usecases

import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.repositories.CitiesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class GetCityByIdUseCaseTest {

    private val mockRepository = mockk<CitiesRepository>()
    private val useCase = GetCityByIdUseCase(mockRepository)

    private val testCity = City("1", "New York", "United States", 40.7128, -74.0060, null, null, false)

    @Test
    fun `invoke returns city from repository`() = runTest {
        coEvery { mockRepository.getCityById("1") } returns testCity

        val result = useCase("1")

        assertEquals(testCity, result)
        assertEquals("New York", result?.name)
        assertEquals("United States", result?.country)
        coVerify { mockRepository.getCityById("1") }
    }

    @Test
    fun `invoke returns null when city not found`() = runTest {
        coEvery { mockRepository.getCityById("999") } returns null

        val result = useCase("999")

        assertNull(result)
        coVerify { mockRepository.getCityById("999") }
    }

    @Test
    fun `invoke propagates repository exception`() = runTest {
        val exception = Exception("Database error")
        coEvery { mockRepository.getCityById("1") } throws exception

        try {
            useCase("1")
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Database error", e.message)
        }

        coVerify { mockRepository.getCityById("1") }
    }

    @Test
    fun `invoke handles empty city id`() = runTest {
        coEvery { mockRepository.getCityById("") } returns null

        val result = useCase("")

        assertNull(result)
        coVerify { mockRepository.getCityById("") }
    }
}