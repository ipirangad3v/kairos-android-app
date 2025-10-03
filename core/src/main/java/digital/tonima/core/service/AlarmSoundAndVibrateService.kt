package digital.tonima.core.service

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
import logcat.logcat

class AlarmSoundAndVibrateService : Service() {

    companion object {
        val VIBRATION_PATTERN = longArrayOf(0, 500, 500)
        const val VIBRATION_REPEAT_INDEX = 0
        const val ACTION_START_ALARM = "digital.tonima.core.service.START_ALARM_SOUND"
        const val ACTION_STOP_ALARM = "digital.tonima.core.service.STOP_ALARM_SOUND"

        fun startAlarm(context: Context) {
            val intent = Intent(context, AlarmSoundAndVibrateService::class.java).apply {
                action = ACTION_START_ALARM
            }
            context.startService(intent)
        }

        fun stopAlarm(context: Context) {
            val intent = Intent(context, AlarmSoundAndVibrateService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            context.startService(intent) // Envia uma intent para o serviço parar
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

                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(VIBRATOR_SERVICE) as Vibrator
                }
                vibrator?.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, VIBRATION_REPEAT_INDEX))

                val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
                ringtone?.apply {
                    isLooping = true
                    play()
                    logcat { "AlarmSoundAndVibrateService: Ringtone iniciado." }
                } ?: run {
                    logcat(logcat.LogPriority.ERROR) { "AlarmSoundAndVibrateService: Falha ao obter Ringtone." }
                }
            }
            ACTION_STOP_ALARM -> {
                logcat { "AlarmSoundAndVibrateService: Recebida solicitação para parar alarme." }
                stopAndReleaseResources()
                stopSelf()
            }
            else -> {
                logcat { "AlarmSoundAndVibrateService: Ação desconhecida ou nula." }
            }
        }

        return START_NOT_STICKY
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
        stopAndReleaseResources() // Garanta que os recursos sejam liberados ao destruir o serviço
        super.onDestroy()
    }
}
