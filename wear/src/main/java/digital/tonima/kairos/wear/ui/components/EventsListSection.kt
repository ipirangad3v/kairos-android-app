package digital.tonima.kairos.wear.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.MaterialTheme
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

        if (event.isRecurring) {
            Dialog(
                visible = pendingToggle.value != null,
                onDismissRequest = { pendingToggle.value = null },
            ) {
                pendingToggle.value?.let { (pendingEvent, pendingEnabled) ->
                    if (pendingEvent.id == event.id && pendingEvent.startTime == event.startTime) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(coreR.string.update_alarm_title),
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = stringResource(coreR.string.update_alarm_message),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    onEventToggle(pendingEvent, pendingEnabled, true)
                                    pendingToggle.value = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(stringResource(coreR.string.recurring_option)) }
                            Button(
                                onClick = {
                                    onEventToggle(pendingEvent, pendingEnabled, false)
                                    pendingToggle.value = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(stringResource(coreR.string.only_this_option)) }
                        }
                    }
                }
            }
        }
    }
}
