package digital.tonima.kairos.repository

import digital.tonima.kairos.model.Event
import java.time.YearMonth

interface CalendarRepository {
    suspend fun getEventsForMonth(yearMonth: YearMonth): List<Event>
}
