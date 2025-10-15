package digital.tonima.kairos.wear.ui

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import digital.tonima.core.model.Event
import digital.tonima.kairos.wear.sync.SyncActions
import digital.tonima.kairos.wear.sync.WearEventCache
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30], application = android.app.Application::class)
class WearCalendarViewModelTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `loads cached events on init`() {
        val initial = listOf(Event(1, "A", 10L), Event(2, "B", 20L))
        WearEventCache.save(context, initial)

        val vm = WearCalendarViewModel(context)

        assertEquals(initial, vm.next24hEvents.value)
    }

    @Test
    fun `updates when ACTION_EVENTS_UPDATED is broadcast`() {
        WearEventCache.save(context, listOf(Event(1, "First", 10L)))
        val vm = WearCalendarViewModel(context)
        assertEquals(1, vm.next24hEvents.value.size)

        // change cache and notify
        WearEventCache.save(context, listOf(Event(2, "Second", 20L), Event(3, "Third", 30L)))
        context.sendBroadcast(Intent(SyncActions.ACTION_EVENTS_UPDATED))
        // Ensure the broadcast is processed on the main looper before assertions
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertEquals(2, vm.next24hEvents.value.size)
        assertEquals(2L, vm.next24hEvents.value[0].id)
    }

    @Test
    fun `requestRescan reloads from cache`() {
        WearEventCache.save(context, listOf(Event(5, "Old", 50L)))
        val vm = WearCalendarViewModel(context)
        assertEquals(1, vm.next24hEvents.value.size)

        WearEventCache.save(context, listOf(Event(6, "New", 60L)))
        vm.requestRescan()

        assertEquals(1, vm.next24hEvents.value.size)
        assertEquals(6L, vm.next24hEvents.value[0].id)
    }
}
