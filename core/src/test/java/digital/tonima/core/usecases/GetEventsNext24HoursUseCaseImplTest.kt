package digital.tonima.core.usecases

import digital.tonima.core.model.Event
import digital.tonima.core.repository.CalendarRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetEventsNext24HoursUseCaseImplTest {

    @Test
    fun `invoke delegates to repository and returns list`() = runBlocking {
        val repo = mockk<CalendarRepository>()
        val expected = listOf(
            Event(id = 1L, title = "A", startTime = 1000L),
            Event(id = 2L, title = "B", startTime = 2000L)
        )
        coEvery { repo.getEventsNext24Hours() } returns expected

        val useCase = GetEventsNext24HoursUseCaseImpl(repo)

        val result = useCase.invoke()

        assertEquals(expected, result)
        coVerify(exactly = 1) { repo.getEventsNext24Hours() }
    }
}
