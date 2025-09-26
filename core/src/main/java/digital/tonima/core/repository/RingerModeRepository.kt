package digital.tonima.core.repository

import kotlinx.coroutines.flow.StateFlow

interface RingerModeRepository {
    val ringerMode: StateFlow<AudioWarningState>

    fun startObserving()

    fun stopObserving()
}
