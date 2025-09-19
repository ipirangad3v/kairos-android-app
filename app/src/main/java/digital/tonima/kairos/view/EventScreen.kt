package digital.tonima.kairos.view

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import digital.tonima.kairos.model.Event
import digital.tonima.kairos.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(viewModel: EventViewModel) {
    val events by viewModel.events.collectAsState()
    val alarmsEnabled by viewModel.alarmsEnabled.collectAsState()
    val context = LocalContext.current

    val standardPermissionsToRequest = remember {
        mutableListOf(Manifest.permission.READ_CALENDAR).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    val standardPermissionState = rememberMultiplePermissionsState(permissions = standardPermissionsToRequest)

    // Pede as permissões padrão uma única vez quando a tela é exibida.
    LaunchedEffect(Unit) {
        if (!standardPermissionState.allPermissionsGranted) {
            standardPermissionState.launchMultiplePermissionRequest()
        }
    }

    val hasExactAlarmPermission = remember { mutableStateOf(true) }

    val checkExactAlarmPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            hasExactAlarmPermission.value = alarmManager.canScheduleExactAlarms()
        } else {
            hasExactAlarmPermission.value = true
        }
    }
    LaunchedEffect(Unit) {
        checkExactAlarmPermission()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarmes da Agenda") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                // Caso 1: Permissões padrão e de alarme concedidas. Mostrar a UI principal.
                standardPermissionState.allPermissionsGranted && hasExactAlarmPermission.value -> {
                    // O LaunchedEffect aqui agora depende se as permissões foram concedidas,
                    // garantindo que os eventos carreguem no momento certo.
                    LaunchedEffect(Unit) {
                        viewModel.loadEvents()
                    }
                    MainContent(
                        events = events,
                        alarmsEnabled = alarmsEnabled,
                        onToggle = { viewModel.onAlarmsToggle(it) },
                        onEventToggle = { event, isEnabled ->
                            viewModel.onEventAlarmToggle(event, isEnabled)
                        }
                    )
                }
                // Caso 2: Permissões padrão concedidas, mas a de alarme NÃO. Mostrar tela para permissão de alarme.
                standardPermissionState.allPermissionsGranted && !hasExactAlarmPermission.value -> {
                    ExactAlarmPermissionScreen(
                        // Passa a função de verificação para o botão "Já autorizei".
                        onAlreadyAuthorizedClick = checkExactAlarmPermission
                    )
                }
                // Caso 3: Permissões padrão NÃO foram concedidas. Mostrar tela para permissões padrão.
                else -> {
                    StandardPermissionsScreen(
                        onSettingsClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", context.packageName, null)
                            intent.data = uri
                            context.startActivity(intent)
                        },
                        onRetryClick = { standardPermissionState.launchMultiplePermissionRequest() }
                    )
                }
            }
        }
    }
}

@Composable
fun MainContent(
    events: List<Event>,
    alarmsEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onEventToggle: (event: Event, isEnabled: Boolean) -> Unit
) {
    Column {
        AlarmsToggleRow(
            alarmsEnabled = alarmsEnabled,
            onToggle = onToggle
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum evento futuro encontrado ou a agenda está vazia.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(events) { event ->
                    EventCard(
                        event = event,
                        isGloballyEnabled = alarmsEnabled,
                        onToggle = { isEnabled -> onEventToggle(event, isEnabled) }
                    )
                }
            }
        }
    }
}

@Composable
fun StandardPermissionsScreen(onSettingsClick: () -> Unit, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Permissões Iniciais Necessárias",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Para funcionar, o aplicativo precisa de acesso à sua agenda e permissão para enviar notificações.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSettingsClick) {
                Text("Abrir Configurações")
            }
            Button(onClick = onRetryClick) {
                Text("Tentar Novamente")
            }
        }
    }
}

@Composable
fun ExactAlarmPermissionScreen(onAlreadyAuthorizedClick: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Permissão Final Necessária",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Para garantir que os alarmes disparem na hora exata, o Android requer uma permissão especial. Por favor, ative-a na próxima tela.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
            }
        }) {
            Text("Conceder Permissão de Alarme")
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Botão para o usuário forçar a verificação após conceder a permissão.
        TextButton(onClick = onAlreadyAuthorizedClick) {
            Text("Já autorizei, continuar")
        }
    }
}


@Composable
fun AlarmsToggleRow(alarmsEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Ativar alarmes para eventos", style = MaterialTheme.typography.titleMedium)
        Switch(checked = alarmsEnabled, onCheckedChange = onToggle)
    }
}

@Composable
fun EventCard(event: Event, isGloballyEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = formatMillisToDateTime(event.startTime), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = event.isAlarmEnabled,
                onCheckedChange = onToggle,
                enabled = isGloballyEnabled
            )
        }
    }
}

fun formatMillisToDateTime(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}

