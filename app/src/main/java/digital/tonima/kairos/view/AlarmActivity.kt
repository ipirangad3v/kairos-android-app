package digital.tonima.kairos.view

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.tonima.kairos.R
import digital.tonima.kairos.service.AlarmSoundService
import digital.tonima.kairos.service.AlarmState
import digital.tonima.kairos.ui.theme.KairosTheme

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val eventTitle = intent.getStringExtra("EXTRA_EVENT_TITLE") ?: getString(R.string.upcoming_event)

        val soundServiceIntent = Intent(this, AlarmSoundService::class.java)
        startService(soundServiceIntent)

        setContent {
            KairosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = getString(R.string.event_alarm),
                            fontSize = 24.sp,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = eventTitle,
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                        Button(
                            onClick = {
                                // Libera a trava do alarme.
                                AlarmState.stopAlarm()
                                // Para o serviço de som ao clicar no botão.
                                stopService(soundServiceIntent)
                                finish()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            Text(text = getString(R.string.stop), fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AlarmState.stopAlarm()
        stopService(Intent(this, AlarmSoundService::class.java))
    }
}

