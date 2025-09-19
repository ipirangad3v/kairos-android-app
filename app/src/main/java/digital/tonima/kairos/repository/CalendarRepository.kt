package digital.tonima.kairos.repository

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import digital.tonima.kairos.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class CalendarRepository(private val context: Context) {

    @SuppressLint("MissingPermission")
    suspend fun getEvents(): List<Event> = withContext(Dispatchers.IO) {
        val eventList = mutableListOf<Event>()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN
        )

        val beginTime = Calendar.getInstance()
        val endTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) } // Busca eventos dos próximos 30 dias

        val uriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(uriBuilder, beginTime.timeInMillis)
        ContentUris.appendId(uriBuilder, endTime.timeInMillis)

        val cursor = context.contentResolver.query(
            uriBuilder.build(),
            projection,
            null,
            null,
            CalendarContract.Instances.BEGIN + " ASC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val titleColumn = it.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val beginColumn = it.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn)
                val begin = it.getLong(beginColumn)

                if (!title.isNullOrEmpty()) {
                    eventList.add(Event(id = id, title = title, startTime = begin))
                }
            }
        }
        return@withContext eventList
    }
}

