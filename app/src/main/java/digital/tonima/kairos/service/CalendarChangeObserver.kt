package digital.tonima.kairos.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import logcat.LogPriority
import logcat.logcat

/**
 * Observes the Calendar provider on the phone and triggers an immediate
 * phone→wear sync whenever events are created/updated/deleted.
 *
 * Uses a simple debounce to coalesce rapid change bursts.
 */
object CalendarChangeObserver {
    private const val UNIQUE_WORK_NAME = "phone-event-sync-onchange"
    private const val DEBOUNCE_MS = 3000L

    @Volatile
    private var initialized = false

    private lateinit var appContext: Context
    private lateinit var handlerThread: HandlerThread
    private var observer: ContentObserver? = null

    private val debounceHandler = Handler(Looper.getMainLooper())
    private val debounceRunnable = Runnable {
        tryEnqueueSync()
    }

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            appContext = context.applicationContext

            handlerThread = HandlerThread("CalendarChangeObserver").apply { start() }
            val handler = Handler(handlerThread.looper)

            observer = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    scheduleDebounced()
                }
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    scheduleDebounced()
                }
            }

            try {
                val cr = appContext.contentResolver
                cr.registerContentObserver(CalendarContract.Events.CONTENT_URI, true, observer!!)
                cr.registerContentObserver(CalendarContract.Instances.CONTENT_URI, true, observer!!)
                initialized = true
                logcat { "CalendarChangeObserver: registered content observers." }
            } catch (t: Throwable) {
                logcat(
                    LogPriority.ERROR,
                ) { "CalendarChangeObserver: failed to register observer: ${t.localizedMessage}" }
            }
        }
    }

    private fun scheduleDebounced() {
        // Coalesce bursts of changes
        debounceHandler.removeCallbacks(debounceRunnable)
        debounceHandler.postDelayed(debounceRunnable, DEBOUNCE_MS)
    }

    private fun tryEnqueueSync() {
        val hasPerm = ContextCompat
            .checkSelfPermission(
                appContext, Manifest.permission.READ_CALENDAR,
            ) == PackageManager.PERMISSION_GRANTED
        if (!hasPerm) {
            logcat(LogPriority.WARN) { "CalendarChangeObserver: READ_CALENDAR not granted; skipping immediate sync." }
            return
        }
        try {
            val constraints = Constraints.Builder().build()
            val request = OneTimeWorkRequestBuilder<PhoneEventSyncWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(appContext).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
            logcat { "CalendarChangeObserver: enqueued PhoneEventSyncWorker due to calendar change." }
        } catch (t: Throwable) {
            logcat(LogPriority.ERROR) { "CalendarChangeObserver: failed to enqueue sync: ${t.localizedMessage}" }
        }
    }
}
