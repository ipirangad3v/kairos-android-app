package digital.tonima.kairos.viewmodel

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import digital.tonima.kairos.model.Event
import digital.tonima.kairos.repository.CalendarRepository
import digital.tonima.kairos.service.EventAlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventViewModel(private val application: Application) : AndroidViewModel(application) {

    private val repository = CalendarRepository(application)
    private val scheduler = EventAlarmScheduler(application)

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    private val sharedPreferences = application.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
    private val KEY_GLOBAL_ALARMS_ENABLED = "global_alarms_enabled"
    private val KEY_DISABLED_EVENT_IDS = "disabled_event_ids"

    private val _alarmsEnabled = MutableStateFlow(false)
    val alarmsEnabled = _alarmsEnabled.asStateFlow()

    init {
        _alarmsEnabled.value = sharedPreferences.getBoolean(KEY_GLOBAL_ALARMS_ENABLED, true)
    }

    fun loadEvents() {
        viewModelScope.launch {
            val calendarEvents = repository.getEvents()
            val disabledIds = sharedPreferences.getStringSet(KEY_DISABLED_EVENT_IDS, emptySet()) ?: emptySet()

            _events.value = calendarEvents.map { event ->
                // Um ID único para cada instância de evento (importante para eventos recorrentes)
                val uniqueId = "${event.id}_${event.startTime}"
                // O alarme do evento está ativo se seu ID não estiver na lista de desativados.
                event.copy(isAlarmEnabled = !disabledIds.contains(uniqueId))
            }

            // Se o botão global estiver ativo, agenda os alarmes para todos os eventos marcados como ativos.
            if (_alarmsEnabled.value) {
                scheduleAllEnabledAlarms()
            }
        }
    }

    /**
     * Chamado quando o botão global de ativar/desativar alarmes é tocado.
     */
    fun onAlarmsToggle(isEnabled: Boolean) {
        _alarmsEnabled.value = isEnabled
        // Salva o estado do botão global
        sharedPreferences.edit {
            putBoolean(KEY_GLOBAL_ALARMS_ENABLED, isEnabled)
        }

        if (isEnabled) {
            scheduleAllEnabledAlarms()
        } else {
            cancelAllAlarms()
        }
    }

    /**
     * Chamado quando o botão de um evento individual é tocado.
     */
    fun onEventAlarmToggle(event: Event, isEnabled: Boolean) {
        // 1. Atualiza o estado salvo para este evento específico.
        val disabledIds = sharedPreferences.getStringSet(KEY_DISABLED_EVENT_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        val uniqueId = "${event.id}_${event.startTime}"

        if (isEnabled) {
            disabledIds.remove(uniqueId)
        } else {
            disabledIds.add(uniqueId)
        }
        sharedPreferences.edit {
            putStringSet(KEY_DISABLED_EVENT_IDS, disabledIds)
        }

        // 2. Atualiza a lista na UI para refletir a mudança visualmente.
        _events.value = _events.value.map {
            if (it.id == event.id && it.startTime == event.startTime) {
                it.copy(isAlarmEnabled = isEnabled)
            } else {
                it
            }
        }

        // 3. Agenda ou cancela o alarme para este evento, mas somente se o botão global estiver ativo.
        if (_alarmsEnabled.value) {
            if (isEnabled) {
                scheduler.schedule(event)
            } else {
                scheduler.cancel(event)
            }
        }
    }


    private fun scheduleAllEnabledAlarms() {
        viewModelScope.launch {
            _events.value.filter { it.isAlarmEnabled }.forEach { event ->
                scheduler.schedule(event)
            }
        }
    }

    private fun cancelAllAlarms() {
        viewModelScope.launch {
            _events.value.forEach { event ->
                scheduler.cancel(event)
            }
        }
    }
}

