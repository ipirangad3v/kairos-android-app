package digital.tonima.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import digital.tonima.core.repository.AppPreferencesRepositoryImpl
import digital.tonima.kairos.core.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import logcat.logcat

class AlarmSoundAndVibrateService : Service() {

    companion object {
        val VIBRATION_PATTERN = longArrayOf(0, 500, 500)
        const val VIBRATION_REPEAT_INDEX = 0
        const val ACTION_START_ALARM = "digital.tonima.core.service.START_ALARM_SOUND"
        const val ACTION_STOP_ALARM = "digital.tonima.core.service.STOP_ALARM_SOUND"
        private const val NOTIFICATION_CHANNEL_ID = "calendar_alarm_channel"
        private const val NOTIFICATION_ID = 0xA11A7

        fun startAlarm(context: Context) {
            val intent = Intent(context, AlarmSoundAndVibrateService::class.java).apply {
                action = ACTION_START_ALARM
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopAlarm(context: Context) {
            val intent = Intent(context, AlarmSoundAndVibrateService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        logcat { "AlarmSoundAndVibrateService: onStartCommand - action: ${intent?.action}" }

        when (intent?.action) {
            ACTION_START_ALARM -> {
                stopAndReleaseResources()

                ensureForeground()

                val vibrateOnly = try {
                    runBlocking { AppPreferencesRepositoryImpl(applicationContext).getVibrateOnly().first() }
                } catch (e: Exception) {
                    logcat { "AlarmSoundAndVibrateService: erro ao ler preferência vibrate-only: ${e.localizedMessage}" }
                    false
                }

                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(VIBRATOR_SERVICE) as Vibrator
                }
                vibrator?.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, VIBRATION_REPEAT_INDEX))

                if (!vibrateOnly) {
                    val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
                    ringtone?.apply {
                        isLooping = true
                        play()
                        logcat { "AlarmSoundAndVibrateService: Ringtone iniciado." }
                    } ?: run {
                        logcat(logcat.LogPriority.ERROR) { "AlarmSoundAndVibrateService: Falha ao obter Ringtone." }
                    }
                } else {
                    logcat { "AlarmSoundAndVibrateService: Modo somente vibrar ativado. Som não será reproduzido." }
                }
            }
            ACTION_STOP_ALARM -> {
                logcat { "AlarmSoundAndVibrateService: Recebida solicitação para parar alarme." }
                stopAndReleaseResources()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                logcat { "AlarmSoundAndVibrateService: Ação desconhecida ou nula." }
            }
        }

        return START_NOT_STICKY
    }

    private fun ensureForeground() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.event_alarm),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notification_description)
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)

        val stopIntent = Intent(this, AlarmSoundAndVibrateService::class.java).apply { action = ACTION_STOP_ALARM }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_k_monochrome)
            .setContentTitle(getString(R.string.event_alarm))
            .setContentText(getString(R.string.upcoming_event))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, getString(R.string.stop), stopPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopAndReleaseResources() {
        if (ringtone?.isPlaying == true) {
            ringtone?.stop()
            logcat { "AlarmSoundAndVibrateService: Ringtone parado." }
        }
        ringtone = null

        vibrator?.cancel()
        vibrator = null
        logcat { "AlarmSoundAndVibrateService: Vibração cancelada." }
    }

    override fun onDestroy() {
        logcat { "AlarmSoundAndVibrateService: onDestroy chamado. Garantindo que os recursos sejam liberados." }
        stopAndReleaseResources()
        super.onDestroy()
    }
}
