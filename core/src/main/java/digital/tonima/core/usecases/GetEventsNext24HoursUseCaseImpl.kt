package digital.tonima.core.usecases

import com.paulrybitskyi.hiltbinder.BindType
import digital.tonima.core.model.Event
import digital.tonima.core.repository.CalendarRepository
import javax.inject.Inject

@BindType(installIn = BindType.Component.SINGLETON, to = GetEventsNext24HoursUseCase::class)
class GetEventsNext24HoursUseCaseImpl @Inject constructor(
    private val calendarRepository: CalendarRepository
): GetEventsNext24HoursUseCase {
    override suspend fun invoke(): List<Event> = calendarRepository.getEventsNext24Hours()
}
