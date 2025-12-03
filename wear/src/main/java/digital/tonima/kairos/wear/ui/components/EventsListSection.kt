package digital.tonima.kairos.wear.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.Text
import digital.tonima.core.model.Event
import digital.tonima.kairos.core.R as coreR

@Composable
fun EventsListSection(
    events: List<Event>,
    isRefreshing: Boolean,
    isGlobalAlarmEnabled: Boolean,
    onEventToggle: (event: Event, isEnabled: Boolean, applyToSeries: Boolean) -> Unit,
) {
    if (events.isEmpty() && !isRefreshing) {
        Text(text = stringResource(coreR.string.no_events_found_for_this_day))
        return
    }

    val sorted = events.sortedBy { it.startTime }
    for (event in sorted) {
        val pendingToggle = remember(event.id, event.startTime) { mutableStateOf<Pair<Event, Boolean>?>(null) }
        EventCard(
            event = event,
            isGloballyEnabled = isGlobalAlarmEnabled,
            onToggle = { isEnabled ->
                if (event.isRecurring) {
                    pendingToggle.value = event to isEnabled
                } else {
                    onEventToggle(event, isEnabled, false)
                }
            },
        )

        pendingToggle.value?.let { (pendingEvent, pendingEnabled) ->
            AlertDialog(
                onDismissRequest = { pendingToggle.value = null },
                title = {
                    Text(
                        text = stringResource(coreR.string.update_alarm_title),
                        style = androidx.wear.compose.material3.MaterialTheme.typography.titleSmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                },
                text = { Text(stringResource(coreR.string.update_alarm_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        onEventToggle(pendingEvent, pendingEnabled, true)
                        pendingToggle.value = null
                    }) { Text(stringResource(coreR.string.recurring_option)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        onEventToggle(pendingEvent, pendingEnabled, false)
                        pendingToggle.value = null
                    }) { Text(stringResource(coreR.string.only_this_option)) }
                },
                containerColor = androidx.wear.compose.material3.MaterialTheme.colorScheme.background,
            )
        }
    }
}
