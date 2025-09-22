package digital.tonima.kairos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import digital.tonima.kairos.R
import digital.tonima.kairos.R.string.app_name

@Composable
fun DrawerContent(
    isProUser: Boolean,
    onUpgradeToPro: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)) {
            Text(stringResource(app_name), modifier = Modifier.padding(16.dp))
        }
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        if (!isProUser) {
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Star, contentDescription = null) },
                label = { Text(stringResource(R.string.remove_ads)) },
                selected = false,
                onClick = {
                    onUpgradeToPro()
                    onCloseDrawer()
                }
            )
        }
    }
}
