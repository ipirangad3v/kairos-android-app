package digital.tonima.core.usecases

import com.paulrybitskyi.hiltbinder.BindType
import digital.tonima.core.model.Event
import digital.tonima.core.repository.CalendarRepository
import java.time.YearMonth
import javax.inject.Inject

@BindType(installIn = BindType.Component.SINGLETON, to = GetEventsForMonthUseCase::class)
class GetEventsForMonthUseCaseImpl @Inject constructor(
    private val eventsRepository: CalendarRepository
) : GetEventsForMonthUseCase {
    override suspend fun invoke(yearMonth: YearMonth): List<Event> =
        eventsRepository.getEventsForMonth(yearMonth)
}
