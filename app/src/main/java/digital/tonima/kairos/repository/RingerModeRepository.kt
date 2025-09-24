package digital.tonima.kairos.repository

import kotlinx.coroutines.flow.StateFlow

interface RingerModeRepository {
    val ringerMode: StateFlow<AudioWarningState>

    fun startObserving()

    fun stopObserving()
}
