package digital.tonima.core.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import digital.tonima.kairos.core.R

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ALARM_TRIGGERED = "digital.tonima.core.ALARM_TRIGGERED"
        const val EXTRA_EVENT_TITLE = "EXTRA_EVENT_TITLE"
        const val EXTRA_UNIQUE_ID = "EXTRA_UNIQUE_ID"
        const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
        const val EXTRA_EVENT_START_TIME = "EXTRA_EVENT_START_TIME"

        val VIBRATION_PATTERN = longArrayOf(0, 500, 500)
        const val VIBRATION_REPEAT_INDEX = 0
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val eventTitle =
            intent.getStringExtra(EXTRA_EVENT_TITLE) ?: context.getString(R.string.upcoming_event)
        val uniqueId = intent.getIntExtra(EXTRA_UNIQUE_ID, System.currentTimeMillis().toInt())
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        val startTime = intent.getLongExtra(EXTRA_EVENT_START_TIME, -1L)

        val soundServiceIntent = Intent(context, AlarmSoundService::class.java)
        context.startService(soundServiceIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }.apply {
            vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, VIBRATION_REPEAT_INDEX))
        }


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
