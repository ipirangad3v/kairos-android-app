package digital.tonima.core.usecases

import digital.tonima.core.model.Event

interface GetNextEventUseCase {
    suspend operator fun invoke(): Event?
}
