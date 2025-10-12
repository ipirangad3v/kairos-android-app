package digital.tonima.kairos.ui.view

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import digital.tonima.core.model.Event
import digital.tonima.core.viewmodel.EventViewModel
import digital.tonima.kairos.BuildConfig.ADMOB_BANNER_AD_UNIT_HOME
import digital.tonima.kairos.R.drawable
import digital.tonima.kairos.core.R
import digital.tonima.kairos.core.R.drawable.date_range
import digital.tonima.kairos.ui.components.AdBannerView
import digital.tonima.kairos.ui.components.DrawerContent
import digital.tonima.kairos.ui.components.ExactAlarmPermissionScreen
import digital.tonima.kairos.ui.components.FullScreenIntentPermissionScreen
import digital.tonima.kairos.ui.components.MainContent
import digital.tonima.kairos.ui.components.StandardPermissionsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    viewModel: EventViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onPurchaseRequest: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isProUser by viewModel.isProUser.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val standardPermissionsToRequest = remember {
        mutableListOf(Manifest.permission.READ_CALENDAR).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    val standardPermissionState =
        rememberMultiplePermissionsState(permissions = standardPermissionsToRequest)

    val openAppSettings = {
        context.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null),
            ),
        )
    }

    val openExactAlarmSettings = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.startActivity(Intent("android.settings.REQUEST_SCHEDULE_EXACT_ALARM"))
        } else {
            Toast.makeText(context, R.string.not_applicable_on_this_android_version, Toast.LENGTH_SHORT).show()
        }
    }
    val openFullScreenIntentSettings = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            context.startActivity(
                Intent("android.settings.MANAGE_APP_ALL_ALARMS").apply {
                    data = Uri.fromParts("package", context.packageName, null)
                },
            )
        } else {
            Toast.makeText(context, R.string.not_applicable_on_this_android_version, Toast.LENGTH_SHORT).show()
        }
    }

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
                viewModel.onMonthChanged(uiState.currentMonth, forceRefresh = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                isProUser = isProUser,
                onUpgradeToPro = viewModel::onUpgradeToProRequest,
                onCloseDrawer = { scope.launch { drawerState.close() } },
            )
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(painterResource(drawable.menu), contentDescription = stringResource(R.string.menu))
                        }
                    },
                )
            },
            floatingActionButton = {
                if (
                    uiState.hasCalendarPermission &&
                    uiState.hasExactAlarmPermission &&
                    uiState.hasFullScreenIntentPermission
                ) {
                    FloatingActionButton(
                        onClick = {
                            val intent =
                                context.packageManager.getLaunchIntentForPackage("com.google.android.calendar")
                            if (intent != null) {
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.google_calendar_not_found),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ) {
                        Icon(
                            painterResource(date_range),
                            contentDescription = stringResource(R.string.open_calendar),
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                when {
                    !uiState.hasCalendarPermission ||
                        !uiState.hasPostNotificationsPermission -> StandardPermissionsScreen(
                        onSettingsClick = openAppSettings,
                        onRetryClick = { standardPermissionState.launchMultiplePermissionRequest() },
                    )

                    !uiState.hasExactAlarmPermission -> ExactAlarmPermissionScreen(
                        onAlreadyAuthorizedClick = openExactAlarmSettings,
                    )

                    !uiState.hasFullScreenIntentPermission -> FullScreenIntentPermissionScreen(
                        onAlreadyAuthorizedClick = openFullScreenIntentSettings,
                    )

                    else -> {
                        val onEventClick = { event: Event ->
                            val uri = ContentUris.withAppendedId(
                                CalendarContract.Events.CONTENT_URI,
                                event.id,
                            )
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                putExtra(
                                    "beginTime",
                                    event.startTime,
                                )
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.cannot_open_event),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }

                        MainContent(
                            uiState = uiState,
                            onRefresh = { viewModel.onMonthChanged(uiState.currentMonth, true) },
                            onToggle = viewModel::onAlarmsToggle,
                            onEventToggle = viewModel::onEventAlarmToggle,
                            onMonthChanged = viewModel::onMonthChanged,
                            onDateSelected = viewModel::onDateSelected,
                            onEventClick = onEventClick,
                            onDismissAutostart = viewModel::dismissAutostartSuggestion,
                            onReturnToToday = viewModel::returnToToday,
                            onVibrateToggle = viewModel::onVibrateOnlyChanged,
                        )
                    }
                }
                AdBannerView(
                    adId = ADMOB_BANNER_AD_UNIT_HOME,
                    isProUser = isProUser,
                )
            }
        }
    }
    if (uiState.showUpgradeConfirmation) {
        LaunchedEffect(uiState.showUpgradeConfirmation) {
            onPurchaseRequest()
            viewModel.onPurchaseFlowHandled()
        }
    }
}
