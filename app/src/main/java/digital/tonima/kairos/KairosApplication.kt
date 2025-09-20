package digital.tonima.kairos

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import digital.tonima.kairos.service.AlarmSchedulingWorker
import java.util.concurrent.TimeUnit

class KairosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupRecurringWork()
    }

    private fun setupRecurringWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workManager = WorkManager.getInstance(applicationContext)

        val initialRequest = OneTimeWorkRequestBuilder<AlarmSchedulingWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "initial-event-scheduler",
            ExistingWorkPolicy.KEEP,
            initialRequest
        )

        val repeatingRequest =
            PeriodicWorkRequestBuilder<AlarmSchedulingWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            "event-scheduler",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }

}
