package digital.tonima.kairos.wear.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.core.model.Event
import digital.tonima.kairos.wear.WorkNames
import digital.tonima.kairos.wear.sync.CachedEventSchedulingWorker
import digital.tonima.kairos.wear.sync.SyncActions
import digital.tonima.kairos.wear.sync.WearEventCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import logcat.LogPriority
import logcat.logcat
import javax.inject.Inject

@HiltViewModel
class WearCalendarViewModel
    @Inject
    constructor(
        @ApplicationContext private val appContext: Context,
    ) : ViewModel() {

        private val _next24hEvents = MutableStateFlow<List<Event>>(emptyList())
        val next24hEvents: StateFlow<List<Event>> = _next24hEvents.asStateFlow()

        private val eventsUpdatedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == SyncActions.ACTION_EVENTS_UPDATED) {
                    reloadFromCache()
                }
            }
        }

        init {
            reloadFromCache()
            // Listen for updates coming from WearEventListenerService
            if (Build.VERSION.SDK_INT >= 33) {
                appContext.registerReceiver(
                    eventsUpdatedReceiver,
                    IntentFilter(SyncActions.ACTION_EVENTS_UPDATED),
                    Context.RECEIVER_NOT_EXPORTED,
                )
            } else {
                @Suppress("DEPRECATION")
                appContext.registerReceiver(
                    eventsUpdatedReceiver,
                    IntentFilter(SyncActions.ACTION_EVENTS_UPDATED),
                )
            }
        }

        fun requestRescan() {
            // Just reload from cache and trigger scheduling based on cached events
            reloadFromCache()
            try {
                WorkManager.getInstance(appContext)
                    .enqueueUniqueWork(
                        WorkNames.UNIQUE_SCHEDULE_NOW,
                        ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequestBuilder<CachedEventSchedulingWorker>().build(),
                    )
            } catch (_: Throwable) {
                // In unit tests or unusual contexts WorkManager may not be initialized; ignore.
            }
        }

        private fun reloadFromCache() {
            val events = WearEventCache.load(appContext).sortedBy { it.startTime }
            _next24hEvents.value = events
        }

        override fun onCleared() {
            super.onCleared()
            try {
                appContext.unregisterReceiver(eventsUpdatedReceiver)
            } catch (e: Exception) {
                logcat(LogPriority.ERROR) {
                    "WearCalendarViewModel: Receiver not registered: ${e.localizedMessage}"
                }
            }
        }
    }
