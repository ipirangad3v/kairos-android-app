package digital.tonima.kairos.delegates

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface que define o contrato para fornecer o estado "pro" do utilizador.
 * Qualquer ViewModel que precise de saber se o utilizador é premium pode implementar esta interface.
 */
interface ProUserProvider {
    val isProUser: StateFlow<Boolean>
}
