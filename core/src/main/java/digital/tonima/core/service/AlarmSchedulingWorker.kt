package digital.tonima.core.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import digital.tonima.core.permissions.PermissionManager
import digital.tonima.core.repository.CalendarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.logcat
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Um trabalhador em background que periodicamente verifica os próximos eventos
 * e agenda os alarmes necessários.
 */
@HiltWorker
class AlarmSchedulingWorker
@AssistedInject
constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CalendarRepository,
    private val scheduler: EventAlarmScheduler,
    private val permissionManager: PermissionManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            logcat {
                "Worker iniciado para verificação periódica."
            }
            try {
                if (!permissionManager.hasCalendarPermission()) {
                    logcat(LogPriority.WARN) {
                        "Permissão READ_CALENDAR não concedida. O Worker não pode continuar."
                    }
                    return@withContext Result.success()
                }
                val sharedPreferences = applicationContext.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)

                val isGlobalAlarmEnabled = sharedPreferences.getBoolean("global_alarms_enabled", true)
                if (!isGlobalAlarmEnabled) {
                    logcat(LogPriority.INFO) {
                        "Alarmes globais estão desativados. Worker encerrando sem agendar."
                    }
                    return@withContext Result.success()
                }

                val currentMonthEvents = repository.getEventsForMonth(YearMonth.now())
                val nextMonthEvents = repository.getEventsForMonth(YearMonth.now().plusMonths(1))
                val allUpcomingEvents = currentMonthEvents + nextMonthEvents

                logcat {
                    "Encontrados ${allUpcomingEvents.size} eventos no total para os próximos 2 meses."
                }

                val disabledIds = sharedPreferences.getStringSet("disabled_event_ids", emptySet()) ?: emptySet()

                val now = System.currentTimeMillis()
                val scheduleWindowEnd = now + TimeUnit.MINUTES.toMillis(75)
                val sdf = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault())

                logcat {
                    "Verificando eventos na janela de tempo: ${
                        sdf.format(
                            Date(now)
                        )
                    } até ${sdf.format(Date(scheduleWindowEnd))}"
                }

                val eventsToSchedule =
                    allUpcomingEvents
                        .filter { event ->
                            event.startTime in (now + 1)..scheduleWindowEnd
                        }.filter { !disabledIds.contains(it.uniqueIntentId.toString()) }

                if (eventsToSchedule.isEmpty()) {
                    logcat(LogPriority.INFO) {
                        "Nenhum evento encontrado para agendar nesta janela de tempo."
                    }
                } else {
                    logcat(LogPriority.INFO) {
                        "Encontrados ${eventsToSchedule.size} evento(s) para agendar:"
                    }
                    eventsToSchedule.forEach { event ->
                        logcat {
                            "Agendando alarme para o evento '${event.title}' em ${sdf.format(Date(event.startTime))}"
                        }
                        scheduler.schedule(event)
                    }
                }
                logcat {
                    "Worker concluído com sucesso."
                }
                Result.success()
            } catch (e: Exception) {
                logcat(LogPriority.ERROR) {
                    "Worker falhou com uma exceção: ${e.localizedMessage}"
                }
                Result.failure()
            }
        }
}
