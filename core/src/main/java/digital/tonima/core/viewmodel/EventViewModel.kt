package digital.tonima.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.core.delegates.ProUserProvider
import digital.tonima.core.model.Event
import digital.tonima.core.permissions.PermissionManager
import digital.tonima.core.repository.AppPreferencesRepository
import digital.tonima.core.repository.AudioWarningState
import digital.tonima.core.repository.RingerModeRepository
import digital.tonima.core.service.EventAlarmScheduler
import digital.tonima.core.usecases.GetEventsForMonthUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.LogPriority
import logcat.logcat
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class EventScreenUiState(
    val events: List<Event> = emptyList(),
    val isGlobalAlarmEnabled: Boolean = true,
    val isRefreshing: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val showAutostartSuggestion: Boolean = false,
    val showUpgradeConfirmation: Boolean = false,
    val hasCalendarPermission: Boolean = false,
    val hasPostNotificationsPermission: Boolean = false,
    val hasExactAlarmPermission: Boolean = false,
    val hasFullScreenIntentPermission: Boolean = false,
    val audioWarning: AudioWarningState = AudioWarningState.NORMAL,
    val vibrateOnly: Boolean = false
)

@HiltViewModel
class EventViewModel
@Inject
constructor(
    proUserProvider: ProUserProvider,
    private val getEventsForMonthUseCase: GetEventsForMonthUseCase,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val ringerModeRepository: RingerModeRepository,
    private val scheduler: EventAlarmScheduler,
    private val permissionManager: PermissionManager
) : ViewModel(), ProUserProvider by proUserProvider {

    private val _uiState = MutableStateFlow(EventScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        appPreferencesRepository.isGlobalAlarmEnabled()
            .onEach { isEnabled ->
                _uiState.update { it.copy(isGlobalAlarmEnabled = isEnabled) }
                if (!isEnabled) {
                    cancelAllLoadedAlarms()
                } else {
                    if (_uiState.value.hasCalendarPermission) {
                        onMonthChanged(_uiState.value.currentMonth, forceRefresh = true)
                    }
                }
            }
            .launchIn(viewModelScope)

        appPreferencesRepository.getAutostartSuggestionDismissed()
            .onEach { dismissed ->
                _uiState.update { it.copy(showAutostartSuggestion = !dismissed) }
            }
            .launchIn(viewModelScope)

        appPreferencesRepository.getVibrateOnly()
            .onEach { vibrate -> _uiState.update { it.copy(vibrateOnly = vibrate) } }
            .launchIn(viewModelScope)

        checkAllPermissions()

        ringerModeRepository.startObserving()
        ringerModeRepository.ringerMode
            .onEach { warning -> _uiState.update { it.copy(audioWarning = warning) } }
            .launchIn(viewModelScope)
    }

    public override fun onCleared() {
        super.onCleared()
        ringerModeRepository.stopObserving()
    }

    fun checkAllPermissions() {
        _uiState.update { currentState ->
            currentState.copy(
                hasCalendarPermission = permissionManager.hasCalendarPermission(),
                hasPostNotificationsPermission = permissionManager.hasPostNotificationsPermission(),
                hasExactAlarmPermission = permissionManager.hasExactAlarmPermission(),
                hasFullScreenIntentPermission = permissionManager.hasFullScreenIntentPermission()
            )
        }
        if (_uiState.value.hasCalendarPermission) {
            onMonthChanged(_uiState.value.currentMonth, forceRefresh = true)
        }
    }

    fun dismissAutostartSuggestion() {
        viewModelScope.launch {
            appPreferencesRepository.setAutostartSuggestionDismissed(true)
        }
    }


    fun onMonthChanged(yearMonth: YearMonth, forceRefresh: Boolean = false) {
        if (!_uiState.value.hasCalendarPermission) {
            logcat(LogPriority.WARN) { "Permissão de calendário não concedida. Não é possível mudar o mês ou carregar eventos." }
            _uiState.update { it.copy(events = emptyList()) }
            return
        }
        if (!forceRefresh && yearMonth == _uiState.value.currentMonth) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, currentMonth = yearMonth) }

            val calendarEvents = getEventsForMonthUseCase.invoke(yearMonth)
            val disabledInstanceIds = appPreferencesRepository.getDisabledEventIds().firstOrNull() ?: emptySet()
            val disabledSeriesIds = appPreferencesRepository.getDisabledSeriesIds().firstOrNull() ?: emptySet()

            val updatedEvents = calendarEvents.map { event ->
                val isInstanceDisabled = disabledInstanceIds.contains(event.uniqueIntentId.toString())
                val isSeriesDisabled = disabledSeriesIds.contains(event.id.toString())
                event.copy(isAlarmEnabled = !(isInstanceDisabled || isSeriesDisabled))
            }

            _uiState.update { currentState ->
                currentState.copy(
                    events = updatedEvents,
                    isRefreshing = false
                )
            }

            if (appPreferencesRepository.isGlobalAlarmEnabled().firstOrNull() == true) {
                scheduleImmediateEvents(updatedEvents)
            }
        }
    }

    private fun scheduleImmediateEvents(events: List<Event>) {
        val now = System.currentTimeMillis()
        val scheduleWindowEnd = now + TimeUnit.MINUTES.toMillis(75)

        events
            .filter { it.isAlarmEnabled }
            .filter { event ->
                event.startTime in (now + 1)..scheduleWindowEnd
            }
            .forEach { event ->
                scheduler.schedule(event)
            }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun returnToToday() {
        _uiState.update {
            it.copy(
                selectedDate = LocalDate.now(),
                currentMonth = YearMonth.now()
            )
        }
    }

    fun onAlarmsToggle(isEnabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.setGlobalAlarmEnabled(isEnabled)
        }
    }

    fun onVibrateOnlyChanged(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.setVibrateOnly(enabled)
        }
    }

    fun onEventAlarmToggle(event: Event, isEnabled: Boolean, disableAllOccurrences: Boolean = false) {
        viewModelScope.launch {
            val currentDisabledInstanceIds =
                appPreferencesRepository.getDisabledEventIds().firstOrNull()?.toMutableSet() ?: mutableSetOf()
            val currentDisabledSeriesIds =
                appPreferencesRepository.getDisabledSeriesIds().firstOrNull()?.toMutableSet() ?: mutableSetOf()
            val instanceIdStr = event.uniqueIntentId.toString()
            val seriesIdStr = event.id.toString()

            if (isEnabled) {
                if (disableAllOccurrences) {
                    currentDisabledSeriesIds.remove(seriesIdStr)
                    appPreferencesRepository.setDisabledSeriesIds(currentDisabledSeriesIds)
                } else {
                    currentDisabledInstanceIds.remove(instanceIdStr)
                    appPreferencesRepository.setDisabledEventIds(currentDisabledInstanceIds)
                }
            } else {
                if (disableAllOccurrences) {
                    currentDisabledSeriesIds.add(seriesIdStr)
                    appPreferencesRepository.setDisabledSeriesIds(currentDisabledSeriesIds)
                } else {
                    currentDisabledInstanceIds.add(instanceIdStr)
                    appPreferencesRepository.setDisabledEventIds(currentDisabledInstanceIds)
                }
            }

            _uiState.update { currentState ->
                val updatedEvents = currentState.events.map {
                    if (it.uniqueIntentId == event.uniqueIntentId) {
                        it.copy(isAlarmEnabled = isEnabled)
                    } else {
                        it
                    }
                }
                currentState.copy(events = updatedEvents)
            }

            if (appPreferencesRepository.isGlobalAlarmEnabled().firstOrNull() == true) {
                if (isEnabled) {
                    scheduler.schedule(event)
                } else {
                    scheduler.cancel(event)
                }
            }
        }
    }

    private fun cancelAllLoadedAlarms() {
        viewModelScope.launch {
            _uiState.value.events.forEach { event ->
                scheduler.cancel(event)
            }
        }
    }

    fun onUpgradeToProRequest() {
        _uiState.update { it.copy(showUpgradeConfirmation = true) }
    }

    fun onPurchaseFlowHandled() {
        _uiState.update { it.copy(showUpgradeConfirmation = false) }
    }
}
