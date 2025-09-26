package digital.tonima.kairos.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.core.R
import digital.tonima.core.model.Event
import digital.tonima.kairos.wear.presentation.theme.KairosTheme
import digital.tonima.kairos.wear.viewmodel.EventViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KairosTheme {
                WearApp()
            }
        }
    }
}

@Composable
fun WearApp(
    viewModel: EventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    LaunchedEffect(Unit) {
        viewModel.onDateSelected(LocalDate.now())
    }

    Scaffold(
        timeText = { TimeText(modifier = Modifier.scrollAway(listState)) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, start = 8.dp, end = 8.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.displayMedium)
            }
            item {
                Spacer(Modifier.height(8.dp))
                ToggleChip(
                    checked = uiState.isGlobalAlarmEnabled,
                    onCheckedChange = { isChecked -> viewModel.onAlarmsToggle(isChecked) },
                    label = { Text(stringResource(R.string.activate_event_alarms)) },
                    toggleControl = { Switch(checked = uiState.isGlobalAlarmEnabled) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
            item {
                Spacer(Modifier.height(16.dp))
                Text(text = stringResource(R.string.activate_event_alarms), style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
            }
            if (uiState.events.isEmpty() && !uiState.isRefreshing) {
                item {
                    Text(text = stringResource(R.string.no_events_found_for_this_day))
                }
            } else {
                items(uiState.events.sortedBy { it.startTime }) { event ->
                    EventListItem(event = event)
                }
            }
        }
    }
}

@Composable
fun EventListItem(event: Event) {
    val formatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }
    val localTime = Instant.ofEpochMilli(event.startTime)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    val formattedTime = remember(localTime) { formatter.format(localTime) }

    Card(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(text = event.title, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(hiltViewModel())
}
