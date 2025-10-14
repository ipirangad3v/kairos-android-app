package digital.tonima.kairos.wear.ui

import digital.tonima.core.model.Event
import digital.tonima.kairos.wear.calendar.CalendarEventsFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeFetcher(initial: List<Event>) : CalendarEventsFetcher {
    private val _events = MutableStateFlow(initial)
    override val events: StateFlow<List<Event>> = _events

    var rescanRequested = false

    override fun requestRescan() {
        rescanRequested = true
    }

    override fun kill() { /* no-op */ }
}

class WearCalendarViewModelTest {

    @Test
    fun `exposes fetcher events`() {
        val initial = listOf(Event(1, "A", 10L), Event(2, "B", 20L))
        val vm = WearCalendarViewModel(FakeFetcher(initial))

        assertEquals(initial, vm.next24hEvents.value)
    }

    @Test
    fun `delegates requestRescan to fetcher`() {
        val fetcher = FakeFetcher(emptyList())
        val vm = WearCalendarViewModel(fetcher)

        vm.requestRescan()

        assertTrue(fetcher.rescanRequested)
    }
}
