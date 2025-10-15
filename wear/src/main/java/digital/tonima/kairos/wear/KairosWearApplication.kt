package digital.tonima.kairos.wear

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import digital.tonima.kairos.wear.sync.CachedEventSchedulingWorker
import logcat.AndroidLogcatLogger
import logcat.LogPriority
import logcat.LogPriority.VERBOSE
import logcat.logcat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class KairosWearApplication :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(workerFactory)
                .build()

    override fun onCreate() {
        super.onCreate()
        try {
            WorkManager.initialize(this, workManagerConfiguration)
        } catch (e: IllegalStateException) {
            logcat(
                LogPriority.ERROR,
            ) { "WorkManager already initialized." }
        }
        setupLogger()
        setupRecurringWork()

        // Schedule an immediate one-time run to ensure alarms are set at startup
        val initialRequest = OneTimeWorkRequestBuilder<CachedEventSchedulingWorker>().build()
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                WorkNames.UNIQUE_SCHEDULE_NOW,
                ExistingWorkPolicy.REPLACE,
                initialRequest,
            )
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_RUNNING_CRITICAL ||
            level == TRIM_MEMORY_COMPLETE ||
            level == TRIM_MEMORY_MODERATE
        ) {
            setupRecurringWork()
        }
    }

    private fun setupLogger() {
        AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = VERBOSE)
    }

    private fun setupRecurringWork() {
        val constraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

        val workManager = WorkManager.getInstance(applicationContext)

        try {
            workManager.cancelUniqueWork("event-scheduler")
            workManager.cancelUniqueWork("initial-event-scheduler")
            workManager.cancelAllWorkByTag("digital.tonima.core.service.AlarmSchedulingWorker")
        } catch (_: Throwable) { }

        val repeatingRequest =
            PeriodicWorkRequestBuilder<CachedEventSchedulingWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            WorkNames.UNIQUE_PERIODIC_SCHEDULER,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest,
        )
    }
}
