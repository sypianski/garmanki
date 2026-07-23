package app.sypianski.garmanki.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.sypianski.garmanki.R

/** Space Mono survives only for technical read-outs (push/sync log lines). */
val MonoFamily = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold),
)

// Neutral slate fallback for devices without dynamic color (pre-Android 12).
private val LightColors = lightColorScheme(
    primary = Color(0xFF41586D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC6DBF0),
    onPrimaryContainer = Color(0xFF12212F),
    secondary = Color(0xFF555F6B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9E3F0),
    onSecondaryContainer = Color(0xFF121C26),
    tertiary = Color(0xFF64596E),
    onTertiary = Color(0xFFFFFFFF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9C7E4),
    onPrimary = Color(0xFF0F2A3E),
    primaryContainer = Color(0xFF294155),
    onPrimaryContainer = Color(0xFFC6DBF0),
    secondary = Color(0xFFBDC7D4),
    onSecondary = Color(0xFF27313C),
    secondaryContainer = Color(0xFF3D4753),
    onSecondaryContainer = Color(0xFFD9E3F0),
    tertiary = Color(0xFFCFC0D9),
    onTertiary = Color(0xFF352B3F),
)

/**
 * Anki grade accents (1=Again … 4=Easy), tuned separately for light and
 * dark so they stay readable on either scheme without shouting.
 */
@Composable
fun gradeColor(ease: Int): Color {
    val dark = isSystemInDarkTheme()
    return when (ease) {
        1 -> if (dark) Color(0xFFEF9A9A) else Color(0xFFB3261E)
        2 -> if (dark) Color(0xFFFFB74D) else Color(0xFFA05A00)
        3 -> if (dark) Color(0xFF81C784) else Color(0xFF2E7D32)
        4 -> if (dark) Color(0xFF64B5F6) else Color(0xFF1565C0)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun GarmankiTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    // Stock M3 type scale: nothing below 14 sp for body text, no letter-spacing
    // tricks — the terminal look lives on only in MonoFamily log lines.
    MaterialTheme(colorScheme = scheme, typography = Typography(), content = content)
}
