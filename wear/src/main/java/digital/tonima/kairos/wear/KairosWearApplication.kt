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
import logcat.LogPriority.VERBOSE
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
        // Ensure WorkManager is initialized with HiltWorkerFactory config since the default
        // androidx.startup initializer is disabled in the manifest.
        try {
            androidx.work.WorkManager.initialize(this, workManagerConfiguration)
        } catch (_: IllegalStateException) {
            // Already initialized; ignore
        }
        setupLogger()
        setupRecurringWork()
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

        // Cancel any legacy phone-style schedulers that might have been enqueued in older builds
        // so Wear only schedules alarms from cached phone events.
        try {
            workManager.cancelUniqueWork("event-scheduler")
            workManager.cancelUniqueWork("initial-event-scheduler")
            // Best-effort: in case any jobs were tagged with the worker class name
            workManager.cancelAllWorkByTag("digital.tonima.core.service.AlarmSchedulingWorker")
        } catch (_: Throwable) { }

        val initialRequest =
            OneTimeWorkRequestBuilder<CachedEventSchedulingWorker>()
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniqueWork(
            "wear-initial-event-scheduler",
            ExistingWorkPolicy.KEEP,
            initialRequest,
        )

        val repeatingRequest =
            PeriodicWorkRequestBuilder<CachedEventSchedulingWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            "wear-event-scheduler",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest,
        )
    }
}
