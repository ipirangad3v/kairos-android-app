package digital.tonima.kairos.wear.sync

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import digital.tonima.core.model.Event
import logcat.logcat

class WearEventListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        val events = mutableListOf<Event>()
        dataEvents.use { buffer ->
            buffer.forEach { event ->
                if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == PATH_EVENTS_24H) {
                    try {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        val list = dataMap.getDataMapArrayList(KEY_EVENTS)
                        list?.forEach { dm ->
                            val id = dm.getLong(KEY_ID)
                            val title = dm.getString(KEY_TITLE) ?: "(sem título)"
                            val start = dm.getLong(KEY_START)
                            val rec = dm.getBoolean(KEY_RECUR)
                            events.add(Event(id = id, title = title, startTime = start, isRecurring = rec))
                        }
                    } catch (t: Throwable) {
                        logcat { "Wear listener parse error: ${t.localizedMessage}" }
                    }
                }
            }
        }
        if (events.isNotEmpty()) {
            logcat { "Wear received ${events.size} events from phone." }
            WearEventCache.save(this, events)
            // Notify app that events cache updated so UI can refresh
            sendBroadcast(android.content.Intent(ACTION_EVENTS_UPDATED))
            triggerScheduling(this)
        } else {
            logcat { "Wear received data change but no events found." }
        }
    }

    private fun triggerScheduling(context: Context) {
        val work = OneTimeWorkRequestBuilder<CachedEventSchedulingWorker>().build()
        WorkManager.getInstance(context).enqueue(work)
    }

    companion object {
        const val PATH_EVENTS_24H = "/kairos/events24h"
        const val KEY_EVENTS = "events"
        const val KEY_ID = "id"
        const val KEY_TITLE = "title"
        const val KEY_START = "start"
        const val KEY_RECUR = "recurring"
        const val ACTION_EVENTS_UPDATED = "digital.tonima.kairos.wear.ACTION_EVENTS_UPDATED"
    }
}
