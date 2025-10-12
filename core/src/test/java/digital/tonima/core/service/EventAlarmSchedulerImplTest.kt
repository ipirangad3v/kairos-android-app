package digital.tonima.core.service

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import digital.tonima.core.model.Event
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class EventAlarmSchedulerImplTest {

    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        alarmManager = context.getSystemService(AlarmManager::class.java)
    }

    @Test
    fun `schedule should set exact alarm with correct trigger time`() {
        val event = Event(id = 10L, title = "T", startTime = 123456789L)

        val scheduler = EventAlarmSchedulerImpl(context)
        scheduler.schedule(event)

        val shadowAlarmManager = Shadows.shadowOf(alarmManager)
        val next = shadowAlarmManager.nextScheduledAlarm
        assert(next != null)
        assert(next!!.type == AlarmManager.RTC_WAKEUP)
        assert(next.triggerAtTime == event.startTime)
    }

    @Test
    fun `cancel should cancel the existing alarm`() {
        val event = Event(id = 10L, title = "T", startTime = 123456789L)
        val scheduler = EventAlarmSchedulerImpl(context)

        scheduler.schedule(event)
        scheduler.cancel(event)

        val shadowAlarmManager = Shadows.shadowOf(alarmManager)

        assert(shadowAlarmManager.scheduledAlarms.isEmpty())
    }
}
