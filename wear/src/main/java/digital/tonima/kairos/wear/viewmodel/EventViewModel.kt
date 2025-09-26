package digital.tonima.kairos.wear.viewmodel

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.core.delegates.ProUserProvider
import digital.tonima.core.model.Event
import digital.tonima.core.repository.AudioWarningState
import digital.tonima.core.repository.RingerModeRepository
import digital.tonima.core.service.EventAlarmScheduler
import digital.tonima.core.util.needsAutostartPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class EventScreenUiState(
    val events: List<Event> = emptyList(),
    val isGlobalAlarmEnabled: Boolean = true,
    val isRefreshing: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val showAutostartSuggestion: Boolean = false,
    val showUpgradeConfirmation: Boolean = false,
    val audioWarning: AudioWarningState = AudioWarningState.NORMAL
)

@HiltViewModel
class EventViewModel
    @Inject
    constructor(
        proUserProvider: ProUserProvider,
        @ApplicationContext application: Context,
        private val ringerModeRepository: RingerModeRepository,
        private val scheduler: EventAlarmScheduler
    ) : ViewModel(), ProUserProvider by proUserProvider {
        private val _uiState = MutableStateFlow(EventScreenUiState())
        val uiState = _uiState.asStateFlow()

        private val sharedPreferences = application.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
        private val KEY_GLOBAL_ALARMS_ENABLED = "global_alarms_enabled"
    private val KEY_AUTOSTART_SUGGESTION_DISMISSED = "autostart_suggestion_dismissed"

        init {
            val initialGlobalState = sharedPreferences.getBoolean(KEY_GLOBAL_ALARMS_ENABLED, true)
            _uiState.update { it.copy(isGlobalAlarmEnabled = initialGlobalState) }
            checkIfAutostartSuggestionIsNeeded()

            ringerModeRepository.startObserving()
            ringerModeRepository.ringerMode
                .onEach { warning -> _uiState.update { it.copy(audioWarning = warning) } }
                .launchIn(viewModelScope)
        }

        override fun onCleared() {
            super.onCleared()
            ringerModeRepository.stopObserving()
        }

        private fun checkIfAutostartSuggestionIsNeeded() {
            val isDismissed = sharedPreferences.getBoolean(KEY_AUTOSTART_SUGGESTION_DISMISSED, false)
            if (needsAutostartPermission() && !isDismissed) {
                _uiState.update { it.copy(showAutostartSuggestion = true) }
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

    private fun cancelAllLoadedAlarms() {
            viewModelScope.launch {
                _uiState.value.events.forEach { event ->
                    scheduler.cancel(event)
                }
            }
        }

}
