package digital.tonima.kairos.wear.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

private val DarkColorScheme = ColorScheme(
    primary = Purple80,
    onPrimary = Color.White,
    primaryContainer = Purple80,
    onPrimaryContainer = Color.Black,
    secondary = PurpleGrey80,
    onSecondary = Color.White,
    secondaryContainer = PurpleGrey80,
    onSecondaryContainer = Color.Black,
    tertiary = Pink80,
    onTertiary = Color.White,
    tertiaryContainer = Pink80,
    onTertiaryContainer = Color.Black,
    error = Color(0xFFF2B8B5),
    onError = Color.Black,
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF2B8B5),
    background = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

private val LightColorScheme = ColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Purple40,
    onPrimaryContainer = Color.White,
    secondary = PurpleGrey40,
    onSecondary = Color.White,
    secondaryContainer = PurpleGrey40,
    onSecondaryContainer = Color.White,
    tertiary = Pink40,
    onTertiary = Color.White,
    tertiaryContainer = Pink40,
    onTertiaryContainer = Color.White,
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410E0B),
    background = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

@Composable
fun KairosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
