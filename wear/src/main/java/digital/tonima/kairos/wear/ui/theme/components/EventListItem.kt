package digital.tonima.kairos.wear.ui.theme.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.MaterialTheme
import digital.tonima.core.model.Event
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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
            .padding(vertical = 4.dp, horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Text(text = event.title)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
