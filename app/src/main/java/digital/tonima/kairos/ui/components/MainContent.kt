package digital.tonima.kairos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import digital.tonima.kairos.BuildConfig
import digital.tonima.kairos.R
import digital.tonima.kairos.model.Event
import digital.tonima.kairos.util.openAutostartSettings
import digital.tonima.kairos.viewmodel.EventScreenUiState
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainContent(
    uiState: EventScreenUiState,
    onRefresh: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onEventToggle: (event: Event, isEnabled: Boolean) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onEventClick: (Event) -> Unit,
    onDismissAutostart: () -> Unit
) {
    val pullRefreshState =
        rememberPullRefreshState(refreshing = uiState.isRefreshing, onRefresh = onRefresh)
    val eventsByDate = remember(uiState.events) {
        uiState.events.groupBy {
            Instant.ofEpochMilli(it.startTime).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }
    val eventsInDay = remember(uiState.selectedDate, eventsByDate) {
        eventsByDate[uiState.selectedDate] ?: emptyList()
    }
    val context = LocalContext.current

    Column(Modifier.padding(horizontal = 16.dp)) {
        AlarmsToggleRow(
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            alarmsEnabled = uiState.isGlobalAlarmEnabled,
            onToggle = onToggle
        )
        AdBannerView(
            adId = BuildConfig.ADMOB_BANNER_AD_UNIT_HOME,
            isProUser = false
        )

        if (uiState.showAutostartSuggestion) {
            AutostartSuggestionCard(
                onOpenSettings = { openAutostartSettings(context) },
                onDismiss = onDismissAutostart
            )
        }

        CalendarView(
            modifier = Modifier.padding(top = 8.dp),
            currentMonth = uiState.currentMonth,
            selectedDate = uiState.selectedDate,
            eventsByDate = eventsByDate,
            onMonthChanged = onMonthChanged,
            onDateSelected = onDateSelected
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(Modifier.pullRefresh(pullRefreshState)) {
            if (eventsInDay.isEmpty() && !uiState.isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text(stringResource(R.string.no_events_found_for_this_day)) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(eventsInDay, key = { it.uniqueIntentId }) { event ->
                        EventCard(
                            event = event,
                            isGloballyEnabled = uiState.isGlobalAlarmEnabled,
                            onToggle = { isEnabled -> onEventToggle(event, isEnabled) },
                            onEventClick = { onEventClick(event) })
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    val sampleEvents = listOf(
        Event(
            id = 1L,
            title = "Meeting with Team",
            startTime = System.currentTimeMillis() + 3600000, // 1 hour from now
            isAlarmEnabled = true
        ),
        Event(
            id = 2L,
            title = "Doctor Appointment",
            startTime = System.currentTimeMillis() + 7200000 // 2 hours from now
        ),
        Event(
            id = 3L,
            title = "Lunch with Sarah",
            startTime = System.currentTimeMillis() + 10800000 // 3 hours from now
        )
    )
    val uiState = EventScreenUiState(
        isGlobalAlarmEnabled = true,
        isRefreshing = false,
        currentMonth = YearMonth.now(),
        selectedDate = LocalDate.now(),
        events = sampleEvents
    )
    MainContent(
        uiState = uiState,
        onRefresh = {},
        onToggle = {},
        onEventToggle = { _, _ -> },
        onMonthChanged = {},
        onDateSelected = {},
        onEventClick = {},
        onDismissAutostart = {}
    )
}
