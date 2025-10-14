package digital.tonima.kairos.wear.calendar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.CalendarContract
import com.paulrybitskyi.hiltbinder.BindType
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.core.model.Event
import digital.tonima.core.usecases.GetEventsNext24HoursUseCase
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
        private val getEventsNext24Hours: GetEventsNext24HoursUseCase,
    ) : CalendarEventsFetcher {

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        private val _events = MutableStateFlow<List<Event>>(emptyList())
        override val events: StateFlow<List<Event>> = _events

        @Volatile
        private var scanInProgress = false

        private var isReceiverRegistered = false

        private val authority: String = "com.google.android.wearable.provider.calendar"
        private val backupAuthority: String = CalendarContract.AUTHORITY

        private val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleProviderChanged(intent, authority)
            }
        }

        private val broadcastReceiverBackup = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleProviderChanged(intent, backupAuthority)
            }
        }

        init {
            registerReceivers()
            // initial scan
            requestRescan()
        }

        private fun handleProviderChanged(intent: Intent, auth: String) {
            if (Intent.ACTION_PROVIDER_CHANGED == intent.action) {
                logcat { "Calendar provider changed for authority($auth); rescan requested." }
                requestRescan()
            } else {
                logcat { "Ignoring broadcast action(${intent.action}) for authority($auth)" }
            }
        }

        private fun registerReceivers() {
            if (isReceiverRegistered) return
            val filter = IntentFilter(Intent.ACTION_PROVIDER_CHANGED).apply {
                addDataScheme("content")
                addDataAuthority(authority, null)
            }
            appContext.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)

            val filterBackup = IntentFilter(Intent.ACTION_PROVIDER_CHANGED).apply {
                addDataScheme("content")
                addDataAuthority(backupAuthority, null)
            }
            appContext.registerReceiver(broadcastReceiverBackup, filterBackup, Context.RECEIVER_NOT_EXPORTED)

            isReceiverRegistered = true
        }

        override fun requestRescan() {
            if (scanInProgress) {
                logcat { "Rescan already in progress, skipping." }
                return
            }
            scanInProgress = true
            scope.launch {
                try {
                    val list = getEventsNext24Hours()
                    _events.value = list
                    logcat { "Rescan complete. events=${list.size}" }
                } catch (t: Throwable) {
                    logcat { "Rescan failed: ${t.message}" }
                } finally {
                    scanInProgress = false
                }
            }
        }

        override fun kill() {
            if (isReceiverRegistered) {
                try { appContext.unregisterReceiver(broadcastReceiver) } catch (_: Throwable) {}
                try { appContext.unregisterReceiver(broadcastReceiverBackup) } catch (_: Throwable) {}
            }
            isReceiverRegistered = false
            scope.cancel()
        }
    }
