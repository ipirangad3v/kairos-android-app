package digital.tonima.kairos.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface BillingManager {
    val isProUser: StateFlow<Boolean>
    fun connect()
    fun launchPurchaseFlow(activity: Activity)
}
