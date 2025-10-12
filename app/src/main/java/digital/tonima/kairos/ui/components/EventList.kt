package digital.tonima.kairos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import digital.tonima.core.model.Event
import digital.tonima.core.viewmodel.EventScreenUiState
import digital.tonima.kairos.core.R
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EventList(
    modifier: Modifier = Modifier,
    uiState: EventScreenUiState,
    eventsByDate: Map<LocalDate, List<Event>>,
    onRefresh: () -> Unit,
    onEventToggle: (event: Event, isEnabled: Boolean, disableAllOccurrences: Boolean) -> Unit,
    onEventClick: (Event) -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = uiState.isRefreshing, onRefresh = onRefresh)
    val eventsInDay = remember(uiState.selectedDate, eventsByDate) {
        eventsByDate[uiState.selectedDate] ?: emptyList()
    }

    val pendingToggle = remember { androidx.compose.runtime.mutableStateOf<Pair<Event, Boolean>?>(null) }

    Box(modifier.pullRefresh(pullRefreshState)) {
        if (eventsInDay.isEmpty() && !uiState.isRefreshing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.no_events_found_for_this_day)) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(eventsInDay, key = { it.uniqueIntentId }) { event ->
                    EventCard(
                        event = event,
                        isGloballyEnabled = uiState.isGlobalAlarmEnabled,
                        onToggle = { isEnabled ->
                            if (event.isRecurring) {
                                pendingToggle.value = event to isEnabled
                            } else {
                                onEventToggle(event, isEnabled, false)
                            }
                        },
                        onEventClick = { onEventClick(event) },
                    )
                }
            }
        }
        PullRefreshIndicator(
            refreshing = uiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        pendingToggle.value?.let { (pendingEvent, pendingEnabled) ->
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { pendingToggle.value = null },
                title = { Text(stringResource(R.string.update_alarm_title)) },
                text = { Text(stringResource(R.string.update_alarm_message)) },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        onEventToggle(pendingEvent, pendingEnabled, true)
                        pendingToggle.value = null
                    }) { Text(stringResource(R.string.recurring_option)) }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        onEventToggle(pendingEvent, pendingEnabled, false)
                        pendingToggle.value = null
                    }) { Text(stringResource(R.string.only_this_option)) }
                },
            )
        }
    }
}
