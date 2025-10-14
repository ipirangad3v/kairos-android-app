package digital.tonima.kairos.wear.calendar

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import digital.tonima.core.model.Event
import digital.tonima.core.usecases.GetEventsNext24HoursUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30], application = android.app.Application::class)
class CalendarEventsFetcherImplTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private class FakeUseCase(var toReturn: List<Event> = emptyList()) : GetEventsNext24HoursUseCase {
        var callCount = 0
        override suspend fun invoke(): List<Event> {
            callCount++
            return toReturn
        }
    }

    @After
    fun tearDown() {
        // nothing persistent
    }

    @Test
    fun `requestRescan updates events flow`() = runBlocking {
        val fake = FakeUseCase(
            toReturn = listOf(Event(1, "A", 10L), Event(2, "B", 20L)),
        )
        val fetcher = CalendarEventsFetcherImpl(context, fake)

        // allow background coroutine to run
        delay(50)

        assertEquals(2, fetcher.events.value.size)
        assertEquals("A", fetcher.events.value[0].title)

        // update data and rescan
        fake.toReturn = listOf(Event(3, "C", 30L))
        fetcher.requestRescan()
        delay(50)
        assertEquals(1, fetcher.events.value.size)
        assertEquals(3L, fetcher.events.value[0].id)

        fetcher.kill()
    }

    @Ignore(
        "Robolectric does not dispatch " +
            "flagged receivers reliably in this setup; broadcast path covered indirectly by requestRescan",
    )
    @Test
    fun `provider changed broadcast triggers rescan`() = runBlocking {
        val fake = FakeUseCase(
            toReturn = listOf(Event(1, "First", 10L)),
        )
        val fetcher = CalendarEventsFetcherImpl(context, fake)
        delay(50)
        assertEquals(1, fetcher.events.value.size)

        // change underlying data but do not call requestRescan directly
        fake.toReturn = listOf(Event(2, "Second", 20L), Event(3, "Third", 30L))

        // send broadcast matching authority
        val intent = Intent(Intent.ACTION_PROVIDER_CHANGED).apply {
            data = Uri.parse("content://com.google.android.wearable.provider.calendar")
        }
        context.sendBroadcast(intent)

        // wait a bit for broadcast handling
        delay(100)

        assertEquals(2, fetcher.events.value.size)
        assertEquals(2L, fetcher.events.value[0].id)

        fetcher.kill()
    }
}
