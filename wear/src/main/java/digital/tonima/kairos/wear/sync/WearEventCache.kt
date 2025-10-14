package digital.tonima.kairos.wear.sync

import android.content.Context
import digital.tonima.core.model.Event
import org.json.JSONArray
import org.json.JSONObject

object WearEventCache {
    private const val PREF = "PhoneEventsCache"
    private const val KEY_JSON = "json"

    fun save(context: Context, events: List<Event>) {
        val arr = JSONArray()
        events.forEach { e ->
            val o = JSONObject()
            o.put("id", e.id)
            o.put("title", e.title)
            o.put("start", e.startTime)
            o.put("recurring", e.isRecurring)
            arr.put(o)
        }
        // Use commit() so that subsequent workers reading immediately after this call
        // can see the persisted data without racing SharedPreferences apply().
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_JSON, arr.toString())
            .commit()
    }

    fun load(context: Context): List<Event> {
        val json = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_JSON, null)
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val arr = JSONArray(json)
            val list = ArrayList<Event>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    Event(
                        id = o.getLong("id"),
                        title = o.getString("title"),
                        startTime = o.getLong("start"),
                        isRecurring = o.optBoolean("recurring", false),
                    ),
                )
            }
            list
        } catch (_: Throwable) {
            emptyList()
        }
    }
}
