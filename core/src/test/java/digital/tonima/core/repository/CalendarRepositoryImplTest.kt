package digital.tonima.core.repository

import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class CalendarRepositoryImplTest {

    @Test
    fun `getEventsNext24Hours returns empty when calendar permission denied`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        // Robolectric by default denies dangerous permissions; ensure our assumption
        val permissionState = context.checkSelfPermission(android.Manifest.permission.READ_CALENDAR)
        assertTrue(
            "Precondition failed: permission should be denied in test (was $permissionState)",
            permissionState != PackageManager.PERMISSION_GRANTED
        )

        val repo = CalendarRepositoryImpl(context)

        val events = repo.getEventsNext24Hours()

        assertTrue(events.isEmpty())
    }
}
