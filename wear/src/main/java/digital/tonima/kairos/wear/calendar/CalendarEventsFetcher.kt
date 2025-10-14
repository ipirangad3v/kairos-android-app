package digital.tonima.kairos.wear.calendar

import digital.tonima.core.model.Event
import kotlinx.coroutines.flow.StateFlow

interface CalendarEventsFetcher {
    val events: StateFlow<List<Event>>
    fun requestRescan()
    fun kill()
}
