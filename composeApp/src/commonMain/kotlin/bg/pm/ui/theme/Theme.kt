package bg.pm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Purple accent palette
val Purple80  = Color(0xFFD0AAFF)
val Purple60  = Color(0xFFBB86FC)
val PurpleDark = Color(0xFF6A0DAD)

// Background palette
val BackgroundColor       = Color(0xFF0A0A0A)
val SurfaceColor          = Color(0xFF181818)
val SurfaceVariantColor   = Color(0xFF242424)

private val DarkColorScheme = darkColorScheme(
    primary             = Purple60,
    onPrimary           = Color(0xFF1A0035),
    primaryContainer    = PurpleDark,
    onPrimaryContainer  = Purple80,
    secondary           = Color(0xFFCBB4E0),
    onSecondary         = Color(0xFF341F4A),
    background          = BackgroundColor,
    onBackground        = Color(0xFFF2F2F2),
    surface             = SurfaceColor,
    onSurface           = Color(0xFFF2F2F2),
    surfaceVariant      = SurfaceVariantColor,
    onSurfaceVariant    = Color(0xFFBBBBBB),
    error               = Color(0xFFCF6679),
    outline             = Color(0xFF7B4FA0),
)

@Composable
fun PManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
