package digital.tonima.kairos.service

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Um objeto singleton para gerenciar o estado global do alarme.
 * Isso atua como uma trava para impedir que vários alarmes toquem simultaneamente.
 */
object AlarmState {
    private val isAlarmActive = AtomicBoolean(false)

    /**
     * Tenta iniciar um alarme.
     * @return `true` se um alarme já estava ativo (e o novo deve ser ignorado),
     * `false` se o alarme pôde ser iniciado com sucesso.
     */
    fun startAlarm(): Boolean {
        return isAlarmActive.getAndSet(true)
    }

    /**
     * Para o alarme e libera a trava.
     */
    fun stopAlarm() {
        isAlarmActive.set(false)
    }
}
