package digital.tonima.core.usecases

import com.paulrybitskyi.hiltbinder.BindType
import digital.tonima.core.model.Event
import digital.tonima.core.repository.CalendarRepository
import javax.inject.Inject

@BindType(installIn = BindType.Component.SINGLETON, to = GetNextEventUseCase::class)
class GetNextEventUseCaseImpl @Inject constructor(
    private val eventsRepository: CalendarRepository
): GetNextEventUseCase {
    override suspend fun invoke(): Event? = eventsRepository.getNextUpcomingEvent()

}
