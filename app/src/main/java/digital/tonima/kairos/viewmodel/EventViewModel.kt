package digital.tonima.kairos.viewmodel

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.kairos.delegates.ProUserProvider
import digital.tonima.kairos.model.Event
import digital.tonima.kairos.repository.CalendarRepository
import digital.tonima.kairos.service.EventAlarmScheduler
import digital.tonima.kairos.util.needsAutostartPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val showAutostartSuggestion: Boolean = false
)

@HiltViewModel
class EventViewModel @Inject constructor(
    proUserProvider: ProUserProvider,
    @ApplicationContext application: Context,
    private val repository: CalendarRepository,
    private val scheduler: EventAlarmScheduler,
) : ViewModel(), ProUserProvider by proUserProvider {
    private val _uiState = MutableStateFlow(EventScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val sharedPreferences = application.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
    private val KEY_GLOBAL_ALARMS_ENABLED = "global_alarms_enabled"
    private val KEY_DISABLED_EVENT_IDS = "disabled_event_ids"
    private val KEY_AUTOSTART_SUGGESTION_DISMISSED = "autostart_suggestion_dismissed"

    init {
        val initialGlobalState = sharedPreferences.getBoolean(KEY_GLOBAL_ALARMS_ENABLED, true)
        _uiState.update { it.copy(isGlobalAlarmEnabled = initialGlobalState) }
        checkIfAutostartSuggestionIsNeeded()
    }

    /**
     * Verifica se o aviso de início automático deve ser exibido.
     */
    private fun checkIfAutostartSuggestionIsNeeded() {
        val isDismissed = sharedPreferences.getBoolean(KEY_AUTOSTART_SUGGESTION_DISMISSED, false)
        if (needsAutostartPermission() && !isDismissed) {
            _uiState.update { it.copy(showAutostartSuggestion = true) }
        }
    }

    /**
     * Salva a preferência do usuário para não mostrar o aviso novamente.
     */
    fun dismissAutostartSuggestion() {
        sharedPreferences.edit {
            putBoolean(KEY_AUTOSTART_SUGGESTION_DISMISSED, true)
        }
        _uiState.update { it.copy(showAutostartSuggestion = false) }
    }

    fun onMonthChanged(yearMonth: YearMonth, forceRefresh: Boolean = false) {
        if (!forceRefresh && yearMonth == _uiState.value.currentMonth) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, currentMonth = yearMonth) }

            val calendarEvents = repository.getEventsForMonth(yearMonth)
            val disabledIds = sharedPreferences.getStringSet(KEY_DISABLED_EVENT_IDS, emptySet()) ?: emptySet()

            val updatedEvents = calendarEvents.map { event ->
                event.copy(isAlarmEnabled = !disabledIds.contains(event.uniqueIntentId.toString()))
            }

            _uiState.update { currentState ->
                currentState.copy(
                    events = updatedEvents,
                    isRefreshing = false
                )
            }

            if (_uiState.value.isGlobalAlarmEnabled) {
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

    fun onAlarmsToggle(isEnabled: Boolean) {
        _uiState.update { it.copy(isGlobalAlarmEnabled = isEnabled) }
        sharedPreferences.edit {
            putBoolean(KEY_GLOBAL_ALARMS_ENABLED, isEnabled)
        }

        if (!isEnabled) {
            cancelAllLoadedAlarms()
        }
    }

    fun onEventAlarmToggle(event: Event, isEnabled: Boolean) {
        val disabledIds =
            sharedPreferences.getStringSet(KEY_DISABLED_EVENT_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        val eventIdStr = event.uniqueIntentId.toString()

        if (isEnabled) {
            disabledIds.remove(eventIdStr)
        } else {
            disabledIds.add(eventIdStr)
        }
        sharedPreferences.edit {
            putStringSet(KEY_DISABLED_EVENT_IDS, disabledIds)
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

        if (_uiState.value.isGlobalAlarmEnabled) {
            if (isEnabled) {
                scheduler.schedule(event)
            } else {
                scheduler.cancel(event)
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
}
