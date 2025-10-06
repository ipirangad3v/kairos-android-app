package digital.tonima.kairos.wear.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.core.model.Event
import digital.tonima.core.usecases.GetNextEventUseCase
import digital.tonima.kairos.wear.MainActivity
import kotlinx.coroutines.runBlocking
import logcat.LogPriority
import logcat.logcat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject
import digital.tonima.kairos.core.R as coreR

@AndroidEntryPoint
class KairosComplicationService : ComplicationDataSourceService() {

    @Inject
    lateinit var getNextEventUseCase: GetNextEventUseCase
    private fun getTapAction(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )
    }

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener,
    ) {
        logcat { "onComplicationRequest para tipo: ${request.complicationType}" }

        val context = this
        val tapAction = getTapAction(context)

        val nextEvent: Event? = runBlocking { getNextEventUseCase.invoke() }

        val complicationData = when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> createShortTextComplicationData(nextEvent, tapAction)
            ComplicationType.LONG_TEXT -> createLongTextComplicationData(nextEvent, tapAction)
            ComplicationType.RANGED_VALUE -> createRangedValueComplicationData(
                nextEvent,
                tapAction,
            )

            else -> {
                logcat(LogPriority.WARN) { "Tipo de complicação não suportado: ${request.complicationType}" }
                null
            }
        }
        listener.onComplicationData(complicationData)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder("10:30").build(),
                contentDescription = PlainComplicationText.Builder("Evento").build(),
            )
                .setMonochromaticImage(
                    MonochromaticImage.Builder(
                        image = Icon.createWithResource(
                            this@KairosComplicationService,
                            coreR.drawable.date_range,
                        ),
                    ).build(),
                )
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder("Reunião - 10:30").build(),
                contentDescription = PlainComplicationText.Builder("Próximo").build(),
            )
                .setMonochromaticImage(
                    MonochromaticImage.Builder(
                        image = Icon.createWithResource(
                            this,
                            coreR.drawable.date_range,
                        ),
                    ).build(),
                )
                .build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 50f,
                min = 0f,
                max = 100f,
                contentDescription = PlainComplicationText.Builder("Tempo para evento").build(),
            )
                .setText(PlainComplicationText.Builder("50m").build())
                .setTitle(PlainComplicationText.Builder("Prox. Evento").build())
                .build()

            else -> null
        }
    }

    private fun createShortTextComplicationData(
        nextEvent: Event?,
        tapAction: PendingIntent,
    ): ComplicationData {
        val text = if (nextEvent != null) {
            val localTime = Instant.ofEpochMilli(nextEvent.startTime)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
            val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            PlainComplicationText.Builder(formatter.format(localTime)).build()
        } else {
            PlainComplicationText.Builder(getString(coreR.string.no_events)).build()
        }

        val icon = MonochromaticImage.Builder(
            image = Icon.createWithResource(
                this,
                coreR.drawable.date_range,
            ),
        ).build()

        return ShortTextComplicationData.Builder(
            text = text,
            contentDescription = PlainComplicationText.Builder("Próximo").build(),
        )
            .setTapAction(tapAction)
            .setMonochromaticImage(icon)
            .build()
    }

    private fun createLongTextComplicationData(
        nextEvent: Event?,
        tapAction: PendingIntent,
    ): ComplicationData {
        val mainDisplayBody: PlainComplicationText
        val detailTitle: PlainComplicationText
        val contentDesc: PlainComplicationText

        if (nextEvent != null) {
            val localTime = Instant.ofEpochMilli(nextEvent.startTime)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
            val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

            mainDisplayBody = PlainComplicationText
                .Builder("${nextEvent.title} - ${formatter.format(localTime)}").build()
            detailTitle = PlainComplicationText
                .Builder(getString(coreR.string.next_event_title_placeholder)).build()
            contentDesc = PlainComplicationText
                .Builder(getString(coreR.string.complication_long_text_description_event)).build()
        } else {
            mainDisplayBody = PlainComplicationText
                .Builder(getString(coreR.string.no_upcoming_events)).build()
            detailTitle = PlainComplicationText
                .Builder(getString(coreR.string.no_events_title_placeholder)).build()
            contentDesc = PlainComplicationText
                .Builder(getString(coreR.string.complication_long_text_description_no_event)).build()
        }

        val icon = MonochromaticImage.Builder(
            image = Icon.createWithResource(
                this,
                coreR.drawable.date_range,
            ),
        ).build()

        return LongTextComplicationData.Builder(
            text = mainDisplayBody,
            contentDescription = contentDesc,
        )
            .setTitle(detailTitle)
            .setTapAction(tapAction)
            .setMonochromaticImage(icon)
            .build()
    }
    private fun createRangedValueComplicationData(
        nextEvent: Event?,
        tapAction: PendingIntent,
    ): ComplicationData {
        return if (nextEvent != null) {
            val now = Instant.now()
            val eventTime = Instant.ofEpochMilli(nextEvent.startTime)
            val duration = Duration.between(now, eventTime)
            val minutesUntil = duration.toMinutes()

            val maxRange = 1440f
            val minRange = 0f
            val value = minutesUntil.toFloat().coerceIn(minRange, maxRange)

            RangedValueComplicationData.Builder(
                value = value,
                min = minRange,
                max = maxRange,
                contentDescription = PlainComplicationText.Builder("Tempo para o próximo evento").build(),
            )
                .setText(PlainComplicationText.Builder("${minutesUntil}m").build())
                .setTitle(PlainComplicationText.Builder(nextEvent.title).build())
                .setTapAction(tapAction)
                .build()
        } else {
            val maxRange = 1440f
            val minRange = 0f
            val value = 0f

            RangedValueComplicationData.Builder(
                value = value,
                min = minRange,
                max = maxRange,
                contentDescription = PlainComplicationText.Builder(
                    getString(coreR.string.no_upcoming_events_ranged_description),
                ).build(),
            )
                .setText(PlainComplicationText.Builder(getString(coreR.string.no_upcoming_events_short)).build())
                .setTitle(PlainComplicationText.Builder(getString(coreR.string.no_upcoming_events_title)).build())
                .setTapAction(tapAction)
                .build()
        }
    }

    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        logcat { "Complication $complicationInstanceId desativada." }
        super.onComplicationDeactivated(complicationInstanceId)
    }
}
