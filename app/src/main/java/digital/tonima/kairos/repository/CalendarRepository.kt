package digital.tonima.kairos.repository

import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import digital.tonima.kairos.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.YearMonth
import java.time.ZoneId

class CalendarRepository(private val context: Context) {

    private val eventProjection: Array<String> = arrayOf(
        CalendarContract.Instances.EVENT_ID,
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.BEGIN
    )

    private val PROJECTION_ID_INDEX = 0
    private val PROJECTION_TITLE_INDEX = 1
    private val PROJECTION_BEGIN_INDEX = 2

    /**
     * Busca todos os eventos para um mês específico.
     * Esta função é otimizada para buscar apenas os dados necessários para o mês visível.
     */
    suspend fun getEventsForMonth(yearMonth: YearMonth): List<Event> = withContext(Dispatchers.IO) {
        val events = mutableListOf<Event>()

        val startMillis = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)
        val uri = builder.build()

        val cursor = context.contentResolver.query(
            uri,
            eventProjection,
            null,
            null,
            "${CalendarContract.Instances.BEGIN} ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val eventId = it.getLong(PROJECTION_ID_INDEX)
                val title = it.getString(PROJECTION_TITLE_INDEX)
                val begin = it.getLong(PROJECTION_BEGIN_INDEX)

                events.add(Event(id = eventId, title = title, startTime = begin))
            }
        }

        return@withContext events
    }
}
