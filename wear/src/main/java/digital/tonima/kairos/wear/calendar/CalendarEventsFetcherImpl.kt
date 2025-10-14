package digital.tonima.kairos.wear.calendar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.paulrybitskyi.hiltbinder.BindType
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.core.model.Event
import digital.tonima.kairos.wear.sync.WearEventCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import logcat.logcat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hilt-bound fetcher that observes calendar provider changes on Wear and exposes the
 * next-24-hours events via a StateFlow. Adapted from a known working sample.
 */
@BindType(to = CalendarEventsFetcher::class, installIn = BindType.Component.SINGLETON)
@Singleton
class CalendarEventsFetcherImpl
    @Inject
    constructor(
        @ApplicationContext private val appContext: Context,
    ) : CalendarEventsFetcher {

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        private val _events = MutableStateFlow<List<Event>>(emptyList())
        override val events: StateFlow<List<Event>> = _events

        @Volatile
        private var scanInProgress = false

        private val eventsUpdatedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                requestRescan()
            }
        }

        private var receiverRegistered = false

        init {
            requestRescan()
            try {
                val filter =
                    IntentFilter(digital.tonima.kairos.wear.sync.WearEventListenerService.ACTION_EVENTS_UPDATED)
                appContext.registerReceiver(eventsUpdatedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
                receiverRegistered = true
            } catch (e: Throwable) {
                logcat { "Failed to register eventsUpdatedReceiver: ${e.message}" }
            }
        }

        override fun requestRescan() {
            if (scanInProgress) {
                logcat { "Rescan already in progress, skipping." }
                return
            }
            scanInProgress = true
            scope.launch {
                try {
                    val list = WearEventCache.load(appContext)
                    _events.value = list
                    logcat { "Loaded ${list.size} cached events from phone." }
                } catch (t: Throwable) {
                    logcat { "Cache load failed: ${t.message}" }
                } finally {
                    scanInProgress = false
                }
            }
        }

        override fun kill() {
            if (receiverRegistered) {
                try { appContext.unregisterReceiver(eventsUpdatedReceiver) } catch (_: Throwable) {}
                receiverRegistered = false
            }
            scope.cancel()
        }
    }
