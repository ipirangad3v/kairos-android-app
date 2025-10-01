package digital.tonima.core.repository

import digital.tonima.core.model.Event
import java.time.YearMonth

interface CalendarRepository {
    suspend fun getEventsForMonth(yearMonth: YearMonth): List<Event>
    suspend fun getNextUpcomingEvent(): Event?
}
