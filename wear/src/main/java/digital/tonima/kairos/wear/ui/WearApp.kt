package digital.tonima.kairos.wear.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
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
import digital.tonima.core.model.Event
import digital.tonima.core.permissions.PermissionManager
import digital.tonima.core.viewmodel.EventViewModel
import digital.tonima.kairos.wear.ui.components.EventCard
import digital.tonima.kairos.wear.ui.components.WearOsPermissionsScreenContent
import logcat.LogPriority
import logcat.logcat
import digital.tonima.kairos.core.R as coreR

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WearApp(
    viewModel: EventViewModel = hiltViewModel(),
    permissionManager: PermissionManager,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val wearCalendarViewModel: WearCalendarViewModel = hiltViewModel()
    val next24hEvents by wearCalendarViewModel.next24hEvents.collectAsStateWithLifecycle()
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
                wearCalendarViewModel.requestRescan()
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
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, start = 8.dp, end = 8.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState,
        ) {
            val needsStandardPermissions = !standardPermissionState.allPermissionsGranted
            val needsExactAlarmPermission = !uiState.hasExactAlarmPermission
            if (needsStandardPermissions || needsExactAlarmPermission) {
                item {
                    WearOsPermissionsScreenContent(
                        onSettingsClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null),
                                ),
                            )
                        },
                        onRetryClick = { standardPermissionState.launchMultiplePermissionRequest() },
                    )
                }
                if (needsExactAlarmPermission) {
                    item {
                        Chip(
                            onClick = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    val i = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = "package:${context.packageName}".toUri()
                                    }
                                    context.startActivity(i)
                                }
                            },
                            label = { Text(stringResource(coreR.string.allow_exact_alarms)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        )
                    }
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
                            .padding(horizontal = 16.dp),
                    )
                }
                item {
                    Spacer(Modifier.height(4.dp))
                    ToggleChip(
                        checked = uiState.vibrateOnly,
                        onCheckedChange = { enabled -> viewModel.onVibrateOnlyChanged(enabled) },
                        label = { Text(stringResource(coreR.string.vibrate_only)) },
                        toggleControl = { Switch(checked = uiState.vibrateOnly) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(coreR.string.events_for_today),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                val displayEvents = next24hEvents
                if (displayEvents.isEmpty() && !uiState.isRefreshing) {
                    item {
                        Text(
                            text = stringResource(coreR.string.no_events_found_for_this_day),
                        )
                    }
                } else {
                    items(displayEvents.sortedBy { it.startTime }) { event ->
                        val pendingToggle = remember { mutableStateOf<Pair<Event, Boolean>?>(null) }
                        EventCard(
                            event = event,
                            isGloballyEnabled = uiState.isGlobalAlarmEnabled,
                            onToggle = { isEnabled ->
                                if (event.isRecurring) {
                                    pendingToggle.value = event to isEnabled
                                } else {
                                    viewModel.onEventAlarmToggle(event, isEnabled, false)
                                }
                            },
                        )
                        pendingToggle.value?.let { (pendingEvent, pendingEnabled) ->
                            AlertDialog(
                                onDismissRequest = { pendingToggle.value = null },
                                title = {
                                    androidx.compose.material3.Text(
                                        stringResource(coreR.string.update_alarm_title),
                                    )
                                },
                                text = {
                                    androidx.compose.material3.Text(
                                        stringResource(coreR.string.update_alarm_message),
                                    )
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        viewModel.onEventAlarmToggle(pendingEvent, pendingEnabled, true)
                                        pendingToggle.value = null
                                    }) {
                                        androidx.compose.material3.Text(
                                            stringResource(coreR.string.recurring_option),
                                        )
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        viewModel.onEventAlarmToggle(pendingEvent, pendingEnabled, false)
                                        pendingToggle.value = null
                                    }) {
                                        androidx.compose.material3.Text(
                                            stringResource(coreR.string.only_this_option),
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    val versionName = try {
                        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        pInfo.versionName ?: ""
                    } catch (e: Exception) {
                        logcat(LogPriority.ERROR) {
                            "Erro ao obter a versão da app: ${e.localizedMessage}"
                        }
                        ""
                    }
                    Text(
                        text = "v$versionName",
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
