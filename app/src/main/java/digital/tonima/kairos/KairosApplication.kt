package digital.tonima.kairos

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
import digital.tonima.core.service.AlarmSchedulingWorker
import logcat.AndroidLogcatLogger
import logcat.LogPriority.VERBOSE
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class KairosApplication :
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
        setupLogger()
        setupRecurringWork()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL ||
            level == android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE ||
            level == android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE
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

        val initialRequest =
            OneTimeWorkRequestBuilder<AlarmSchedulingWorker>()
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniqueWork(
            "initial-event-scheduler",
            ExistingWorkPolicy.KEEP,
            initialRequest,
        )

        val repeatingRequest =
            PeriodicWorkRequestBuilder<AlarmSchedulingWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            "event-scheduler",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest,
        )
    }
}
