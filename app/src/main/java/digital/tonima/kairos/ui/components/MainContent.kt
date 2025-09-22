package digital.tonima.kairos.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import digital.tonima.kairos.model.Event
import digital.tonima.kairos.util.openAutostartSettings
import digital.tonima.kairos.viewmodel.EventScreenUiState
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current

    val eventsByDate = remember(uiState.events) {
        uiState.events.groupBy {
            Instant.ofEpochMilli(it.startTime).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    // Layout para o modo paisagem
    if (isLandscape) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                AlarmsToggleRow(
                    modifier = Modifier.padding(vertical = 16.dp),
                    alarmsEnabled = uiState.isGlobalAlarmEnabled,
                    onToggle = onToggle
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
            }
            EventList(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 16.dp, bottom = 88.dp),
                uiState = uiState,
                eventsByDate = eventsByDate,
                onRefresh = onRefresh,
                onEventToggle = onEventToggle,
                onEventClick = onEventClick
            )
        }
    } else {
        // Layout para o modo retrato
        Column(Modifier.padding(horizontal = 16.dp)) {
            AlarmsToggleRow(
                modifier = Modifier.padding(vertical = 16.dp),
                alarmsEnabled = uiState.isGlobalAlarmEnabled,
                onToggle = onToggle
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
            EventList(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                eventsByDate = eventsByDate,
                onRefresh = onRefresh,
                onEventToggle = onEventToggle,
                onEventClick = onEventClick
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
