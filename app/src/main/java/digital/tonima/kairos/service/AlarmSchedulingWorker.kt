package digital.tonima.kairos.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import digital.tonima.kairos.repository.CalendarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Um trabalhador em background que periodicamente verifica os próximos eventos
 * e agenda os alarmes necessários.
 */
class AlarmSchedulingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "AlarmSchedulingWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Worker iniciado para verificação periódica.")
        try {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_CALENDAR
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Permissão READ_CALENDAR não concedida. O Worker não pode continuar.")
                return@withContext Result.success()
            }

            val repository = CalendarRepository(applicationContext)
            val scheduler = EventAlarmScheduler(applicationContext)
            val sharedPreferences = applicationContext.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)

            val isGlobalAlarmEnabled = sharedPreferences.getBoolean("global_alarms_enabled", true)
            if (!isGlobalAlarmEnabled) {
                Log.i(TAG, "Alarmes globais estão desativados. Worker encerrando sem agendar.")
                return@withContext Result.success()
            }

            val currentMonthEvents = repository.getEventsForMonth(YearMonth.now())
            val nextMonthEvents = repository.getEventsForMonth(YearMonth.now().plusMonths(1))
            val allUpcomingEvents = currentMonthEvents + nextMonthEvents
            Log.d(TAG, "Encontrados ${allUpcomingEvents.size} eventos no total para os próximos 2 meses.")

            val disabledIds = sharedPreferences.getStringSet("disabled_event_ids", emptySet()) ?: emptySet()

            val now = System.currentTimeMillis()
            val scheduleWindowEnd = now + TimeUnit.MINUTES.toMillis(75)
            val sdf = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault())

            Log.d(TAG, "Verificando eventos na janela de tempo: ${sdf.format(Date(now))} até ${sdf.format(Date(scheduleWindowEnd))}")

            val eventsToSchedule = allUpcomingEvents
                .filter { event ->
                    event.startTime in (now + 1)..scheduleWindowEnd
                }
                .filter { !disabledIds.contains(it.uniqueIntentId.toString()) }

            if (eventsToSchedule.isEmpty()) {
                Log.i(TAG, "Nenhum evento encontrado para agendar nesta janela de tempo.")
            } else {
                Log.i(TAG, "Encontrados ${eventsToSchedule.size} evento(s) para agendar:")
                eventsToSchedule.forEach { event ->
                    Log.d(TAG, "  -> Agendando '${event.title}' para ${sdf.format(Date(event.startTime))}")
                    scheduler.schedule(event)
                }
            }

            Log.d(TAG, "Worker concluído com sucesso.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker falhou com uma exceção.", e)
            e.printStackTrace()
            Result.failure()
        }
    }
}

