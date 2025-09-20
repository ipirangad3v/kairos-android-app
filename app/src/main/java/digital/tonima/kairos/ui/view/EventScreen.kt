package digital.tonima.kairos.ui.view

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import digital.tonima.kairos.R
import digital.tonima.kairos.model.Event
import digital.tonima.kairos.ui.components.ExactAlarmPermissionScreen
import digital.tonima.kairos.ui.components.FullScreenIntentPermissionScreen
import digital.tonima.kairos.ui.components.MainContent
import digital.tonima.kairos.ui.components.StandardPermissionsScreen
import digital.tonima.kairos.viewmodel.EventViewModel
import java.time.YearMonth

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(viewModel: EventViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val standardPermissionsToRequest = remember {
        mutableListOf(Manifest.permission.READ_CALENDAR).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    val standardPermissionState =
        rememberMultiplePermissionsState(permissions = standardPermissionsToRequest)
    LaunchedEffect(Unit) {
        if (!standardPermissionState.allPermissionsGranted) standardPermissionState.launchMultiplePermissionRequest()
    }

    val hasExactAlarmPermission = remember { mutableStateOf(true) }
    val checkExactAlarmPermission = {
        hasExactAlarmPermission.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else true
    }
    LaunchedEffect(Unit) { checkExactAlarmPermission() }

    val hasFullScreenIntentPermission = remember { mutableStateOf(true) }
    val checkFullScreenIntentPermission = {
        hasFullScreenIntentPermission.value =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.canUseFullScreenIntent()
            } else true
    }
    LaunchedEffect(Unit) { checkFullScreenIntentPermission() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, uiState.currentMonth) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onMonthChanged(uiState.currentMonth, forceRefresh = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            if (standardPermissionState.allPermissionsGranted && hasExactAlarmPermission.value && hasFullScreenIntentPermission.value) {
                FloatingActionButton(
                    onClick = {
                        val intent =
                            context.packageManager.getLaunchIntentForPackage("com.google.android.calendar")
                        if (intent != null) context.startActivity(intent)
                        else Toast.makeText(
                            context,
                            context.getString(R.string.google_calendar_not_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(
                        Icons.Filled.DateRange,
                        contentDescription = stringResource(R.string.open_calendar)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !standardPermissionState.allPermissionsGranted -> StandardPermissionsScreen(
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

                !hasExactAlarmPermission.value -> ExactAlarmPermissionScreen(
                    onProvidePermissionClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        )
                    },
                    onAlreadyAuthorizedClick = checkExactAlarmPermission
                )

                !hasFullScreenIntentPermission.value -> FullScreenIntentPermissionScreen(
                    onOpenSettingsClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) context.startActivity(
                            Intent(
                                ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                                "package:${context.packageName}".toUri()
                            )
                        )
                    },
                    onAlreadyAuthorizedClick = checkFullScreenIntentPermission
                )

                else -> {
                    val onEventClick = { event: Event ->
                        val uri = ContentUris.withAppendedId(
                            CalendarContract.Events.CONTENT_URI,
                            event.id
                        )
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            putExtra(
                                "beginTime",
                                event.startTime
                            )
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                context,
                                context.getString(R.string.cannot_open_event),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    LaunchedEffect(Unit) { viewModel.onMonthChanged(YearMonth.now(), true) }
                    MainContent(
                        uiState = uiState,
                        onRefresh = { viewModel.onMonthChanged(uiState.currentMonth, true) },
                        onToggle = viewModel::onAlarmsToggle,
                        onEventToggle = viewModel::onEventAlarmToggle,
                        onMonthChanged = viewModel::onMonthChanged,
                        onDateSelected = viewModel::onDateSelected,
                        onEventClick = onEventClick
                    )
                }
            }
        }
    }
}