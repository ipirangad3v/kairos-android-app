package digital.tonima.kairos.wear.calendar

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import digital.tonima.core.model.Event
import digital.tonima.kairos.wear.sync.WearEventCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30], application = android.app.Application::class)
class CalendarEventsFetcherImplTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        // nothing persistent
    }

    @Test
    fun `requestRescan updates events flow`() = runBlocking {
        // seed cache
        WearEventCache.save(context, listOf(Event(1, "A", 10L), Event(2, "B", 20L)))
        val fetcher = CalendarEventsFetcherImpl(context)

        // allow background coroutine to run
        delay(50)

        assertEquals(2, fetcher.events.value.size)
        assertEquals("A", fetcher.events.value[0].title)

        // update cache and rescan
        WearEventCache.save(context, listOf(Event(3, "C", 30L)))
        fetcher.requestRescan()
        delay(50)
        assertEquals(1, fetcher.events.value.size)
        assertEquals(3L, fetcher.events.value[0].id)

        fetcher.kill()
    }

    @Test
    fun `events updated broadcast triggers rescan`() = runBlocking {
        WearEventCache.save(context, listOf(Event(1, "First", 10L)))
        val fetcher = CalendarEventsFetcherImpl(context)
        delay(50)
        assertEquals(1, fetcher.events.value.size)

        // change underlying cache but do not call requestRescan directly
        WearEventCache.save(context, listOf(Event(2, "Second", 20L), Event(3, "Third", 30L)))

        // send our app broadcast
        val intent = Intent(digital.tonima.kairos.wear.sync.WearEventListenerService.ACTION_EVENTS_UPDATED)
        context.sendBroadcast(intent)

        // wait a bit for broadcast handling
        delay(100)

        assertEquals(2, fetcher.events.value.size)
        assertEquals(2L, fetcher.events.value[0].id)

        fetcher.kill()
    }
}
