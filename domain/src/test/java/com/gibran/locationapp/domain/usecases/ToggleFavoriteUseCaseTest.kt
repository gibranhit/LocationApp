package com.gibran.locationapp.domain.usecases

import com.gibran.locationapp.domain.repositories.CitiesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class ToggleFavoriteUseCaseTest {

    private val mockRepository = mockk<CitiesRepository>()
    private val useCase = ToggleFavoriteUseCase(mockRepository)

    @Test
    fun `invoke returns true when toggle succeeds`() = runTest {
        coEvery { mockRepository.toggleFavorite("1") } returns true

        val result = useCase("1")

        assertTrue(result)
        coVerify { mockRepository.toggleFavorite("1") }
    }

    @Test
    fun `invoke returns false when toggle fails`() = runTest {
        coEvery { mockRepository.toggleFavorite("1") } returns false

        val result = useCase("1")

        assertFalse(result)
        coVerify { mockRepository.toggleFavorite("1") }
    }

    @Test
    fun `invoke propagates repository exception`() = runTest {
        val exception = Exception("Network error")
        coEvery { mockRepository.toggleFavorite("1") } throws exception

        try {
            useCase("1")
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Network error", e.message)
        }

        coVerify { mockRepository.toggleFavorite("1") }
    }

    @Test
    fun `invoke handles empty city id`() = runTest {
        coEvery { mockRepository.toggleFavorite("") } returns false

        val result = useCase("")

        assertFalse(result)
        coVerify { mockRepository.toggleFavorite("") }
    }

    @Test
    fun `invoke works with different city ids`() = runTest {
        coEvery { mockRepository.toggleFavorite("city1") } returns true
        coEvery { mockRepository.toggleFavorite("city2") } returns false

        val result1 = useCase("city1")
        val result2 = useCase("city2")

        assertTrue(result1)
        assertFalse(result2)
        coVerify { mockRepository.toggleFavorite("city1") }
        coVerify { mockRepository.toggleFavorite("city2") }
    }
}