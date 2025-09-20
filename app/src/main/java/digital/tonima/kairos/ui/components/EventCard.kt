package digital.tonima.kairos.ui.components

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.tonima.kairos.R
import digital.tonima.kairos.model.Event
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCard(
    event: Event,
    isGloballyEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onEventClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onEventClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatMillisToTime(event.startTime),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = event.isAlarmEnabled,
                onCheckedChange = onToggle,
                enabled = isGloballyEnabled
            )
        }
    }
}

@Composable
fun formatMillisToTime(millis: Long): String {
    val context = LocalContext.current
    val timeFormat = DateFormat.getTimeFormat(context)
    val formattedTime = timeFormat.format(Date(millis))
    return stringResource(R.string.at_time, formattedTime)
}


@Preview
@Composable
fun EventCardPreview() {
    val sampleEvent = Event(
        id = 1L,
        title = "Team Meeting",
        startTime = System.currentTimeMillis() + 3600000, // 1 hour from now
        isAlarmEnabled = true
    )
    EventCard(
        event = sampleEvent,
        isGloballyEnabled = true,
        onToggle = {},
        onEventClick = {}
    )
}
