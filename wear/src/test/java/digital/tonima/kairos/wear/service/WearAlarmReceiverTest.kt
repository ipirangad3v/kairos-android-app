package digital.tonima.kairos.wear.service

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import digital.tonima.core.receiver.AlarmReceiver.Companion.ACTION_ALARM_TRIGGERED
import digital.tonima.core.receiver.AlarmReceiver.Companion.EXTRA_EVENT_TITLE
import digital.tonima.core.receiver.AlarmReceiver.Companion.EXTRA_UNIQUE_ID
import digital.tonima.kairos.core.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class WearAlarmReceiverTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication() as Application
    }

    @Test
    fun `onReceive posts a high-priority alarm notification with expected content`() {
        val eventTitle = "Team Sync"
        val uniqueId = 12345

        val intent = Intent(ACTION_ALARM_TRIGGERED).apply {
            putExtra(EXTRA_EVENT_TITLE, eventTitle)
            putExtra(EXTRA_UNIQUE_ID, uniqueId)
            `package` = context.packageName
        }

        val receiver = WearAlarmReceiver()
        receiver.onReceive(context, intent)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNm = shadowOf(nm)
        val posted = shadowNm.allNotifications

        assertEquals(1, posted.size)
        val notification = posted.first()

        assertEquals(context.getString(R.string.commitment), notification.extras.getString("android.title"))
        assertEquals(eventTitle, notification.extras.getString("android.text"))
        assertNotNull("Notification should have a contentIntent to open the app", notification.contentIntent)
    }
}
