package digital.tonima.kairos.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import digital.tonima.kairos.model.Event

class EventAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(event: Event) {
        val canSchedule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canSchedule) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("EXTRA_EVENT_TITLE", event.title)
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
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.uniqueIntentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

