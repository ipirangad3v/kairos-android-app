package digital.tonima.kairos

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import digital.tonima.kairos.service.AlarmSchedulingWorker
import java.util.concurrent.TimeUnit

class KairosApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        setupRecurringWork()
    }

    private fun setupRecurringWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<AlarmSchedulingWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "event-scheduler",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }

}