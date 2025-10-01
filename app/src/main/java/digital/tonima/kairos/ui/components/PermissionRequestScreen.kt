package digital.tonima.kairos.ui.components

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import digital.tonima.kairos.core.R

@Composable
fun StandardPermissionsScreen(onSettingsClick: () -> Unit, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.initial_permissions_required),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.permissions_disclaimer),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSettingsClick) { Text(stringResource(R.string.open_settings)) }
            Button(onClick = onRetryClick) { Text(stringResource(R.string.try_again)) }
        }
    }
}

@Composable
fun ExactAlarmPermissionScreen(onAlreadyAuthorizedClick: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.exact_alarm_permission),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.exact_alarm_permission_disclaimer),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM),
                )
            }
        }) { Text(stringResource(R.string.provide_permission)) }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onAlreadyAuthorizedClick) { Text(stringResource(R.string.already_authorized)) }
    }
}

@Composable
fun FullScreenIntentPermissionScreen(onAlreadyAuthorizedClick: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.full_screen_permission),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.full_screen_permission_disclaimer),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                context.startActivity(
                    Intent(
                        ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                        "package:${context.packageName}".toUri(),
                    ),
                )
            }
        }) { Text(stringResource(R.string.open_settings)) }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onAlreadyAuthorizedClick) { Text(stringResource(R.string.already_authorized)) }
    }
}
