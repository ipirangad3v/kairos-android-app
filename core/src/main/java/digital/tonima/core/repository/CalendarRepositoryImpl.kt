package digital.tonima.core.repository


import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.paulrybitskyi.hiltbinder.BindType
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.core.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.logcat
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@BindType(installIn = BindType.Component.SINGLETON, to = CalendarRepository::class)
class CalendarRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context
    ) :
    CalendarRepository {
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
        override suspend fun getEventsForMonth(yearMonth: YearMonth): List<Event> = withContext(Dispatchers.IO) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CALENDAR
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                logcat{
                    "Tentativa de aceder ao calendário sem a permissão READ_CALENDAR."
                }
                return@withContext emptyList()
            }
            val events = mutableListOf<Event>()

            val startMillis = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis =
                yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

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
    /**
     * Busca o próximo evento futuro mais próximo.
     * Para complicações, precisamos de dados rápidos e focados.
     */
    override suspend fun getNextUpcomingEvent(): Event? = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            logcat { "Tentativa de aceder ao calendário sem a permissão READ_CALENDAR." }
            return@withContext null
        }

        val now = Instant.now()
        val startMillis = now.toEpochMilli()
        val endMillis = now.plus(30, ChronoUnit.DAYS).toEpochMilli()

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)
        val uri = builder.build()

        val selection = "${CalendarContract.Instances.END} > ?"
        val selectionArgs = arrayOf(now.toEpochMilli().toString())

        val cursor = context.contentResolver.query(
            uri,
            eventProjection,
            selection,
            selectionArgs,
            "${CalendarContract.Instances.BEGIN} ASC"
        )

        var nextEvent: Event? = null
        cursor?.use {
            if (it.moveToFirst()) {
                val eventId = it.getLong(PROJECTION_ID_INDEX)
                val title = it.getString(PROJECTION_TITLE_INDEX)
                val begin = it.getLong(PROJECTION_BEGIN_INDEX)
                nextEvent = Event(id = eventId, title = title, startTime = begin)
            }
        }
        return@withContext nextEvent
    }
    }
