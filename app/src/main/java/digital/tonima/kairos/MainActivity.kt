package digital.tonima.kairos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.kairos.billing.BillingManager
import digital.tonima.kairos.ui.theme.KairosTheme
import digital.tonima.kairos.ui.view.EventScreen
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KairosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EventScreen {
                        billingManager.launchPurchaseFlow(this)
                    }
                }
            }
        }
    }
}
