package digital.tonima.kairos.wear.service // Pacote do módulo wear

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import digital.tonima.core.service.AlarmSoundService // Importa do módulo core

class WearAlarmActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val stopSoundIntent = Intent(context, AlarmSoundService::class.java)
        context.stopService(stopSoundIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = intent.getIntExtra("EXTRA_UNIQUE_ID", -1)
        if (notificationId != -1) {
            notificationManager.cancel(notificationId)
        }
    }
}
