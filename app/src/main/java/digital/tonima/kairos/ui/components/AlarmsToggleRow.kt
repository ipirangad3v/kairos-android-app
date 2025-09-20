package digital.tonima.kairos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import digital.tonima.kairos.R

@Composable
fun AlarmsToggleRow(
    modifier: Modifier = Modifier,
    alarmsEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(R.string.activate_event_alarms), style = MaterialTheme.typography.titleMedium)
        Switch(checked = alarmsEnabled, onCheckedChange = onToggle)
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmsToggleRowPreview() {
    AlarmsToggleRow(
        alarmsEnabled = true,
        onToggle = {}
    )
}
