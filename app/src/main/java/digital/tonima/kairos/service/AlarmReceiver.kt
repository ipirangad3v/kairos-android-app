package digital.tonima.kairos.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import digital.tonima.kairos.R
import digital.tonima.kairos.view.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (AlarmState.startAlarm()) {
            return
        }

        val eventTitle = intent.getStringExtra("EXTRA_EVENT_TITLE") ?: context.getString(R.string.upcoming_event)
        val uniqueId = intent.getIntExtra("EXTRA_UNIQUE_ID", System.currentTimeMillis().toInt())

        val soundServiceIntent = Intent(context, AlarmSoundService::class.java)
        context.startService(soundServiceIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "calendar_alarm_channel"

        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.event_alarm),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_description)
        }
        notificationManager.createNotificationChannel(channel)

        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_EVENT_TITLE", eventTitle)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, uniqueId, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopActionIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            putExtra("EXTRA_UNIQUE_ID", uniqueId)
        }
        val stopActionPendingIntent = PendingIntent.getBroadcast(
            context, uniqueId + 1, stopActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notificationTitle = context.getString(R.string.commitment)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitle)
            .setContentText(eventTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.stop), stopActionPendingIntent)
            .build()

        notificationManager.notify(uniqueId, notification)
    }
}

