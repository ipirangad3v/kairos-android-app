package digital.tonima.core.usecases

import digital.tonima.core.model.Event

interface GetEventsNext24HoursUseCase {
    suspend operator fun invoke(): List<Event>
}
