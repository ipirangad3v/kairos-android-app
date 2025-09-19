package digital.tonima.kairos.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import digital.tonima.kairos.repository.CalendarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.YearMonth
import java.util.concurrent.TimeUnit

/**
 * Um trabalhador em background que periodicamente verifica os próximos eventos
 * e agenda os alarmes necessários.
 */
class AlarmSchedulingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val repository = CalendarRepository(applicationContext)
            val scheduler = EventAlarmScheduler(applicationContext)
            val sharedPreferences = applicationContext.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)

            val isGlobalAlarmEnabled = sharedPreferences.getBoolean("global_alarms_enabled", true)
            if (!isGlobalAlarmEnabled) {
                return@withContext Result.success()
            }

            val currentMonthEvents = repository.getEventsForMonth(YearMonth.now())
            val nextMonthEvents = repository.getEventsForMonth(YearMonth.now().plusMonths(1))
            val allUpcomingEvents = currentMonthEvents + nextMonthEvents

            val disabledIds = sharedPreferences.getStringSet("disabled_event_ids", emptySet()) ?: emptySet()

            val now = System.currentTimeMillis()
            val scheduleWindowEnd = now + TimeUnit.MINUTES.toMillis(75)

            allUpcomingEvents
                .filter { event ->
                    event.startTime in (now + 1)..scheduleWindowEnd
                }
                .filter { !disabledIds.contains(it.uniqueIntentId.toString()) }
                .forEach { event ->
                    scheduler.schedule(event)
                }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

