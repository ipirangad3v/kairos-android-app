package digital.tonima.kairos.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import digital.tonima.core.service.AlarmSoundAndVibrateService
import digital.tonima.core.viewmodel.EventViewModel
import digital.tonima.kairos.BuildConfig.ADMOB_BANNER_AD_UNIT_ALARM_ACTIVITY
import digital.tonima.kairos.core.R
import digital.tonima.kairos.ui.components.AdBannerView
import digital.tonima.kairos.ui.theme.KairosTheme

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val eventTitle =
            intent.getStringExtra("EXTRA_EVENT_TITLE") ?: getString(R.string.upcoming_event)

        setContent {
            val viewModel: EventViewModel = hiltViewModel()
            val isProUser by viewModel.isProUser.collectAsStateWithLifecycle()
            KairosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = getString(R.string.event_alarm),
                            fontSize = 24.sp,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = eventTitle,
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineLarge,
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                        Button(
                            onClick = {
                                stopService(
                                    Intent(
                                        this@AlarmActivity,
                                        AlarmSoundAndVibrateService::class.java,
                                    ),
                                )
                                finish()
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                        ) {
                            Text(text = getString(R.string.stop), fontSize = 20.sp)
                        }
                        AdBannerView(
                            adId = ADMOB_BANNER_AD_UNIT_ALARM_ACTIVITY,
                            isProUser = isProUser,
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, AlarmSoundAndVibrateService::class.java))
    }
}
