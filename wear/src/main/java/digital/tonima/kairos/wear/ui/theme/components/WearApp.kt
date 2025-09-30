package digital.tonima.kairos.wear.ui.theme.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.TimeText
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import digital.tonima.core.permissions.PermissionManager
import digital.tonima.core.viewmodel.EventViewModel
import java.time.LocalDate
import digital.tonima.kairos.core.R as coreR

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WearApp(
    viewModel: EventViewModel = hiltViewModel(),
    permissionManager: PermissionManager
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()
    val context = LocalContext.current

    val standardPermissionsToRequest = remember {
        permissionManager.calendarPermissions.toMutableList()
    }

    val standardPermissionState =
        rememberMultiplePermissionsState(permissions = standardPermissionsToRequest)

    LaunchedEffect(standardPermissionState.allPermissionsGranted) {
        if (!standardPermissionState.allPermissionsGranted) {
            standardPermissionState.launchMultiplePermissionRequest()
        }
        viewModel.checkAllPermissions()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAllPermissions()
                viewModel.onDateSelected(LocalDate.now())
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
            if (!uiState.hasCalendarPermission) {
                item {
                    WearOsPermissionsScreenContent(
                        onSettingsClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                )
                            )
                        },
                        onRetryClick = { standardPermissionState.launchMultiplePermissionRequest() }
                    )
                }
            } else {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(coreR.string.app_name),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    ToggleChip(
                        checked = uiState.isGlobalAlarmEnabled,
                        onCheckedChange = { isChecked -> viewModel.onAlarmsToggle(isChecked) },
                        label = { Text(stringResource(coreR.string.activate_event_alarms)) },
                        toggleControl = { Switch(checked = uiState.isGlobalAlarmEnabled) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(coreR.string.events_for_today),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (uiState.events.isEmpty() && !uiState.isRefreshing) {
                    item {
                        Text(
                            text = stringResource(coreR.string.no_events_found_for_this_day),
                        )
                    }
                } else {
                    items(uiState.events.sortedBy { it.startTime }) { event ->
                        EventListItem(event = event)
                    }
                }
            }
        }
    }
}
