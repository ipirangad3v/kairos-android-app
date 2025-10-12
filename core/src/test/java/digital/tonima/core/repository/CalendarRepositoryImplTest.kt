package digital.tonima.core.repository

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var repository: CalendarRepository

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        repository = CalendarRepositoryImpl(context)
        mockkStatic(ContextCompat::class)
    }

    @Test
    fun `getEventsForMonth returns empty list when permission denied`() = runTest {
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_DENIED

        val result = repository.getEventsForMonth(YearMonth.of(2024, 10))

        assertEquals(emptyList<digital.tonima.core.model.Event>(), result)
    }

    @Test
    fun `getNextUpcomingEvent returns null when permission denied`() = runTest {
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_DENIED

        val result = repository.getNextUpcomingEvent()

        assertNull(result)
    }
}
