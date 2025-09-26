package digital.tonima.core.service

import digital.tonima.core.model.Event

interface EventAlarmScheduler {
    fun schedule(event: Event)

    fun cancel(event: Event)
}
