package digital.tonima.kairos.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import digital.tonima.kairos.model.Event

class EventAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(event: Event) {
        Log.d(
            "EventAlarmScheduler",
            "Scheduling alarm for event: ${event.title} at ${event.startTime} with ID: ${event.uniqueIntentId}"
        )
        val canSchedule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canSchedule) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.example.calendaralarm.ALARM_TRIGGER.${event.uniqueIntentId}"
                data = "kairos://alarm/${event.uniqueIntentId}".toUri()
                putExtra("EXTRA_EVENT_TITLE", event.title)
                putExtra("EXTRA_UNIQUE_ID", event.uniqueIntentId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
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

    fun cancel(event: Event) {
        Log.d("EventAlarmScheduler", "Cancelling alarm for event ID: $event")
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.calendaralarm.ALARM_TRIGGER.${event.uniqueIntentId}"
            data = "kairos://alarm/${event.uniqueIntentId}".toUri()
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.uniqueIntentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
