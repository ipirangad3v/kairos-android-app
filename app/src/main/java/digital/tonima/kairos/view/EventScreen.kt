package digital.tonima.kairos.view

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
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import digital.tonima.kairos.R
import digital.tonima.kairos.model.Event
import digital.tonima.kairos.viewmodel.EventScreenUiState
import digital.tonima.kairos.viewmodel.EventViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.text.get

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
                    onAlreadyAuthorizedClick = checkExactAlarmPermission
                )

                !hasFullScreenIntentPermission.value -> FullScreenIntentPermissionScreen(
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainContent(
    uiState: EventScreenUiState,
    onRefresh: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onEventToggle: (event: Event, isEnabled: Boolean) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onEventClick: (Event) -> Unit
) {
    val pullRefreshState =
        rememberPullRefreshState(refreshing = uiState.isRefreshing, onRefresh = onRefresh)
    val eventsByDate = remember(uiState.events) {
        uiState.events.groupBy {
            Instant.ofEpochMilli(it.startTime).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }
    val eventsInDay = remember(uiState.selectedDate, eventsByDate) {
        eventsByDate[uiState.selectedDate] ?: emptyList()
    }

    Column(Modifier.padding(horizontal = 16.dp)) {
        AlarmsToggleRow(
            modifier = Modifier.padding(vertical = 16.dp),
            alarmsEnabled = uiState.isGlobalAlarmEnabled,
            onToggle = onToggle
        )
        CalendarView(
            currentMonth = uiState.currentMonth,
            selectedDate = uiState.selectedDate,
            eventsByDate = eventsByDate,
            onMonthChanged = onMonthChanged,
            onDateSelected = onDateSelected
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(Modifier.pullRefresh(pullRefreshState)) {
            if (eventsInDay.isEmpty() && !uiState.isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text(stringResource(R.string.no_events_found_for_this_day)) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(eventsInDay, key = { it.uniqueIntentId }) { event ->
                        EventCard(
                            event = event,
                            isGloballyEnabled = uiState.isGlobalAlarmEnabled,
                            onToggle = { isEnabled -> onEventToggle(event, isEnabled) },
                            onEventClick = { onEventClick(event) })
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun CalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<Event>>,
    onMonthChanged: (YearMonth) -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )
    LaunchedEffect(state.firstVisibleMonth.yearMonth) { onMonthChanged(state.firstVisibleMonth.yearMonth) }
    Column {
        MonthHeader(month = state.firstVisibleMonth.yearMonth)
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                Day(
                    day = day,
                    isSelected = selectedDate == day.date,
                    hasEvents = eventsByDate.containsKey(day.date)
                ) { onDateSelected(it.date) }
            },
            monthHeader = {
                DaysOfWeekHeader(
                    daysOfWeek = it.weekDays.first().map { it.date.dayOfWeek })
            }
        )
    }
}

@Composable
private fun MonthHeader(month: YearMonth) {
    // Usa o Locale padrão do sistema para formatação.
    val locale = Locale.getDefault()
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
    Text(
        text = month.format(formatter).replaceFirstChar { it.titlecase(locale) },
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun DaysOfWeekHeader(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Usa o Locale padrão do sistema para os nomes dos dias.
        val locale = Locale.getDefault()
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: (CalendarDay) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(enabled = day.position == DayPosition.MonthDate, onClick = { onClick(day) }),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    day.position != DayPosition.MonthDate -> MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.3f
                    )

                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (hasEvents && day.position == DayPosition.MonthDate) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun StandardPermissionsScreen(onSettingsClick: () -> Unit, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.initial_permissions_required),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.permissions_disclaimer),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSettingsClick) { Text(stringResource(R.string.open_settings)) }
            Button(onClick = onRetryClick) { Text(stringResource(R.string.try_again)) }
        }
    }
}

@Composable
fun ExactAlarmPermissionScreen(onAlreadyAuthorizedClick: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.exact_alarm_permission),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.exact_alarm_permission_disclaimer),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) context.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            )
        }) { Text(stringResource(R.string.provide_permission)) }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onAlreadyAuthorizedClick) { Text(stringResource(R.string.already_authorized)) }
    }
}

@Composable
fun FullScreenIntentPermissionScreen(onAlreadyAuthorizedClick: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.full_screen_permission),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.full_screen_permission_disclaimer),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                    Uri.parse("package:${context.packageName}")
                )
            )
        }) { Text(stringResource(R.string.open_settings)) }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onAlreadyAuthorizedClick) { Text(stringResource(R.string.already_authorized)) }
    }
}

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

