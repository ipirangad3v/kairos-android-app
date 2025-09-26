package digital.tonima.kairos.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import digital.tonima.core.repository.AudioWarningState
import digital.tonima.core.util.openAutostartSettings
import digital.tonima.kairos.viewmodel.EventScreenUiState

@Composable
fun ControlPanel(
    uiState: EventScreenUiState,
    onToggle: (Boolean) -> Unit,
    onDismissAutostart: () -> Unit
) {
    val context = LocalContext.current
    AlarmsToggleRow(
        modifier = Modifier.padding(vertical = 16.dp),
        alarmsEnabled = uiState.isGlobalAlarmEnabled,
        onToggle = onToggle
    )

    if (uiState.audioWarning != AudioWarningState.NORMAL) {
        RingerModeWarningCard(ringerMode = uiState.audioWarning)
    }

    if (uiState.showAutostartSuggestion) {
        AutostartSuggestionCard(
            onOpenSettings = { openAutostartSettings(context) },
            onDismiss = onDismissAutostart
        )
    }
}
