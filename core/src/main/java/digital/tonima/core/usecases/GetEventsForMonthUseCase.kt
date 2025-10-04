package digital.tonima.core.usecases

import digital.tonima.core.model.Event
import java.time.YearMonth

interface GetEventsForMonthUseCase {
    suspend fun invoke(yearMonth: YearMonth): List<Event>
}
