package digital.tonima.kairos.wear.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import digital.tonima.core.receiver.AlarmReceiver.Companion.ACTION_ALARM_TRIGGERED
import digital.tonima.core.receiver.AlarmReceiver.Companion.EXTRA_EVENT_TITLE
import digital.tonima.core.service.AlarmSoundAndVibrateService
import digital.tonima.kairos.core.R

class WearAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_ALARM_TRIGGERED) {
            return
        }
        val eventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: context.getString(R.string.upcoming_event)
        AlarmSoundAndVibrateService.startAlarm(context, eventTitle)
    }
}
