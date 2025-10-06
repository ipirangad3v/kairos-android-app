package digital.tonima.kairos.wear.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4) // Poderia ser usado para variações escuras
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

object ColorTokens {
    // Cores Primárias (ajustadas para tema escuro)
    val Primary = Purple80 // Geralmente uma cor mais clara para tema escuro
    val PrimaryDim = Color(0xFFB39DDB) // Um pouco mais escura que Primary
    val PrimaryContainer = Color(0xFF4F378B) // Um tom mais escuro para containers primários
    val OnPrimary = Color.Black // Texto sobre Primary (claro)
    val OnPrimaryContainer = Color(0xFFEADDFF) // Texto sobre PrimaryContainer (escuro)

    // Cores Secundárias (ajustadas para tema escuro)
    val Secondary = PurpleGrey80
    val SecondaryDim = Color(0xFFB0A7BF)
    val SecondaryContainer = Color(0xFF4A4458)
    val OnSecondary = Color.Black
    val OnSecondaryContainer = Color(0xFFE8DEF8)

    // Cores Terciárias (ajustadas para tema escuro)
    val Tertiary = Pink80
    val TertiaryDim = Color(0xFFD4A2B0)
    val TertiaryContainer = Color(0xFF633B48)
    val OnTertiary = Color.Black
    val OnTertiaryContainer = Color(0xFFFFD8E4)

    // Cores de Superfície (ajustadas para tema escuro)
    val Background = Color.Black // Fundo principal, preto para AMOLED
    val OnBackground = Color.White // Texto/elementos sobre o fundo
    val SurfaceContainerLow = Color(0xFF1C1B1F) // Superfície de menor elevação
    val SurfaceContainer = Color(0xFF211F26) // Superfície de elevação média
    val SurfaceContainerHigh = Color(0xFF2C2A33) // Superfície de maior elevação
    val OnSurface = Color.White // Texto/elementos sobre superfícies
    val OnSurfaceVariant = Color(0xFFCAC4D0) // Variação do texto/elementos sobre superfícies

    // Cores de Contorno
    val Outline = Color(0xFF938F99)
    val OutlineVariant = Color(0xFF49454F)

    // Cores de Erro (ajustadas para tema escuro)
    val Error = Color(0xFFF2B8B5) // Cor de erro (clara para tema escuro)
    val ErrorDim = Color(0xFFE0B4B1)
    val ErrorContainer = Color(0xFF8C1D18) // Container de erro (escuro)
    val OnError = Color.Black // Texto sobre Error (claro)
    val OnErrorContainer = Color(0xFFF2B8B5) // Texto sobre ErrorContainer (escuro)
}
