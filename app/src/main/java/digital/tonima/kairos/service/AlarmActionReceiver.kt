package digital.tonima.kairos.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import digital.tonima.core.service.AlarmSoundService

class AlarmActionReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        Intent(context, AlarmSoundService::class.java).apply {
            context.stopService(this)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }.apply {
            cancel()
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = intent.getIntExtra("EXTRA_UNIQUE_ID", -1)
        if (notificationId != -1) {
            notificationManager.cancel(notificationId)
        }
    }
}
