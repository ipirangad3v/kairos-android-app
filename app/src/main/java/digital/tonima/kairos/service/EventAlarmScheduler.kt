package digital.tonima.kairos.service

import digital.tonima.kairos.model.Event

interface EventAlarmScheduler {
    fun schedule(event: Event)

    fun cancel(event: Event)
}
