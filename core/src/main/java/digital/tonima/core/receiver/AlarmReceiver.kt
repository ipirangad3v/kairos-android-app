package digital.tonima.core.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import digital.tonima.core.service.AlarmSoundAndVibrateService
import digital.tonima.kairos.core.R

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ALARM_TRIGGERED = "digital.tonima.core.ALARM_TRIGGERED"
        const val EXTRA_EVENT_TITLE = "EXTRA_EVENT_TITLE"
        const val EXTRA_UNIQUE_ID = "EXTRA_UNIQUE_ID"
        const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
        const val EXTRA_EVENT_START_TIME = "EXTRA_EVENT_START_TIME"
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val eventTitle =
            intent.getStringExtra(EXTRA_EVENT_TITLE) ?: context.getString(R.string.upcoming_event)
        val uniqueId = intent.getIntExtra(EXTRA_UNIQUE_ID, System.currentTimeMillis().toInt())
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        val startTime = intent.getLongExtra(EXTRA_EVENT_START_TIME, -1L)

        AlarmSoundAndVibrateService.Companion.startAlarm(context)

        val uiIntent = Intent(ACTION_ALARM_TRIGGERED).apply {
            putExtra(EXTRA_EVENT_TITLE, eventTitle)
            putExtra(EXTRA_UNIQUE_ID, uniqueId)
            putExtra(EXTRA_EVENT_ID, eventId)
            putExtra(EXTRA_EVENT_START_TIME, startTime)

            setPackage(context.packageName)
        }
        context.sendBroadcast(uiIntent)
    }
}
