package digital.tonima.core.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import com.paulrybitskyi.hiltbinder.BindType
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.core.model.Event
import digital.tonima.core.service.AlarmReceiver.Companion.ACTION_ALARM_TRIGGERED
import digital.tonima.core.service.AlarmReceiver.Companion.EXTRA_EVENT_ID
import digital.tonima.core.service.AlarmReceiver.Companion.EXTRA_EVENT_START_TIME
import digital.tonima.core.service.AlarmReceiver.Companion.EXTRA_EVENT_TITLE
import digital.tonima.core.service.AlarmReceiver.Companion.EXTRA_UNIQUE_ID
import javax.inject.Inject

@BindType(installIn = BindType.Component.SINGLETON, to = EventAlarmScheduler::class)
class EventAlarmSchedulerImpl
@Inject
constructor(
    @ApplicationContext private val context: Context
) : EventAlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(event: Event) {
        Log.d(
            "EventAlarmScheduler",
            "Scheduling alarm for event: ${event.title} at ${event.startTime} with ID: ${event.uniqueIntentId}"
        )
        val canSchedule =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

        if (canSchedule) {
            val intent =
                Intent(context, AlarmReceiver::class.java).apply {
                    action = ACTION_ALARM_TRIGGERED
                    data = "kairos://alarm/${event.uniqueIntentId}".toUri()

                    putExtra(EXTRA_EVENT_TITLE, event.title)
                    putExtra(EXTRA_UNIQUE_ID, event.uniqueIntentId)
                    putExtra(EXTRA_EVENT_ID, event.id)
                    putExtra(EXTRA_EVENT_START_TIME, event.startTime)
                }
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    event.uniqueIntentId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                event.startTime,
                pendingIntent
            )
        }
    }

    override fun cancel(event: Event) {
        Log.d("EventAlarmScheduler", "Cancelling alarm for event ID: ${event.uniqueIntentId}")
        val intent =
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_ALARM_TRIGGERED
                data = "kairos://alarm/${event.uniqueIntentId}".toUri()
            }
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                event.uniqueIntentId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        alarmManager.cancel(pendingIntent)
    }
}
