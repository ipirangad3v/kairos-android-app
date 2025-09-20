package digital.tonima.kairos.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import digital.tonima.kairos.R

@Composable
private fun PermissionRequestScreen(
    @StringRes titleResId: Int,
    @StringRes disclaimerResId: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(titleResId),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(disclaimerResId),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        content()
    }
}

@Composable
fun StandardPermissionsScreen(onSettingsClick: () -> Unit, onRetryClick: () -> Unit) {
    PermissionRequestScreen(
        titleResId = R.string.initial_permissions_required,
        disclaimerResId = R.string.permissions_disclaimer
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSettingsClick) { Text(stringResource(R.string.open_settings)) }
            Button(onClick = onRetryClick) { Text(stringResource(R.string.try_again)) }
        }
    }
}

@Composable
fun ExactAlarmPermissionScreen(onProvidePermissionClick: () -> Unit, onAlreadyAuthorizedClick: () -> Unit) {
    PermissionRequestScreen(
        titleResId = R.string.exact_alarm_permission,
        disclaimerResId = R.string.exact_alarm_permission_disclaimer
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onProvidePermissionClick) { Text(stringResource(R.string.provide_permission)) }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onAlreadyAuthorizedClick) { Text(stringResource(R.string.already_authorized)) }
        }
    }
}

@Composable
fun FullScreenIntentPermissionScreen(onOpenSettingsClick: () -> Unit, onAlreadyAuthorizedClick: () -> Unit) {
    PermissionRequestScreen(
        titleResId = R.string.full_screen_permission,
        disclaimerResId = R.string.full_screen_permission_disclaimer
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onOpenSettingsClick) { Text(stringResource(R.string.open_settings)) }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onAlreadyAuthorizedClick) { Text(stringResource(R.string.already_authorized)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StandardPermissionsScreenPreview() {
    StandardPermissionsScreen(onSettingsClick = {}, onRetryClick = {})
}

@Preview(showBackground = true)
@Composable
fun ExactAlarmPermissionScreenPreview() {
    ExactAlarmPermissionScreen(onProvidePermissionClick = {}, onAlreadyAuthorizedClick = {})
}

@Preview(showBackground = true)
@Composable
fun FullScreenIntentPermissionScreenPreview() {
    FullScreenIntentPermissionScreen(onOpenSettingsClick = {}, onAlreadyAuthorizedClick = {})
}