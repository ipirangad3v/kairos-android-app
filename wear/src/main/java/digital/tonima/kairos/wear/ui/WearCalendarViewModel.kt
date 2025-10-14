package digital.tonima.kairos.wear.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.core.model.Event
import digital.tonima.kairos.wear.calendar.CalendarEventsFetcher
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class WearCalendarViewModel
    @Inject
    constructor(
        private val fetcher: CalendarEventsFetcher,
    ) : ViewModel() {

        val next24hEvents: StateFlow<List<Event>> = fetcher.events

        fun requestRescan() = fetcher.requestRescan()
    }
