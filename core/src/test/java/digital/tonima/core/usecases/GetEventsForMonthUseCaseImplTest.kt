package digital.tonima.core.usecases

import digital.tonima.core.model.Event
import digital.tonima.core.repository.CalendarRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class GetEventsForMonthUseCaseImplTest {

    private lateinit var mockEventsRepository: CalendarRepository
    private lateinit var getEventsForMonthUseCase: GetEventsForMonthUseCase

    @Before
    fun setup() {
        mockEventsRepository = mockk()
        getEventsForMonthUseCase = GetEventsForMonthUseCaseImpl(mockEventsRepository)
    }

    @Test
    fun `invoke calls repository and returns list of events`() = runTest {
        val yearMonth = YearMonth.of(2023, 10)

        fun toEpochMillis(date: LocalDate, time: LocalTime): Long {
            return ZonedDateTime.of(date, time, ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        val expectedEvents = listOf(
            Event(
                id = 1L,
                title = "Test Event 1",
                startTime = toEpochMillis(LocalDate.of(2023, 10, 26), LocalTime.of(10, 0)),
                isAlarmEnabled = true
            ),
            Event(
                id = 2L,
                title = "Test Event 2",
                startTime = toEpochMillis(LocalDate.of(2023, 10, 27), LocalTime.of(14, 0)),
                isAlarmEnabled = false
            )
        )
        coEvery { mockEventsRepository.getEventsForMonth(yearMonth) } returns expectedEvents

        val result = getEventsForMonthUseCase.invoke(yearMonth)

        assertEquals(expectedEvents, result)
        coVerify(exactly = 1) { mockEventsRepository.getEventsForMonth(yearMonth) }
    }
}

