package digital.tonima.kairos.wear.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import digital.tonima.core.receiver.AlarmReceiver.Companion.ACTION_ALARM_TRIGGERED
import digital.tonima.core.receiver.AlarmReceiver.Companion.EXTRA_EVENT_TITLE
import digital.tonima.core.receiver.AlarmReceiver.Companion.EXTRA_UNIQUE_ID
import digital.tonima.kairos.core.R
import digital.tonima.kairos.wear.MainActivity

class WearAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_ALARM_TRIGGERED) {
            return
        }

        val eventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: context.getString(R.string.upcoming_event)
        val uniqueId = intent.getIntExtra(EXTRA_UNIQUE_ID, System.currentTimeMillis().toInt())

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "wear_alarm_channel"

        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.event_alarm),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_description)
        }
        notificationManager.createNotificationChannel(channel)

        val stopActionIntent = Intent(context, WearAlarmActionReceiver::class.java).apply {
            putExtra(EXTRA_UNIQUE_ID, uniqueId)
        }
        val stopActionPendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueId + 1,
            stopActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notificationTitle = context.getString(R.string.commitment)

        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            uniqueId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(digital.tonima.kairos.core.R.drawable.ic_k_monochrome)
            .setContentTitle(notificationTitle)
            .setContentText(eventTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.stop), stopActionPendingIntent)

        notificationManager.notify(uniqueId, notificationBuilder.build())
    }
}
