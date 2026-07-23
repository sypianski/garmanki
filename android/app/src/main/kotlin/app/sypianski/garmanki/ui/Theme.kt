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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
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
 * E-ink "paper" scheme: pure black-on-white, flat, always light regardless of
 * the system dark mode. No gradients or shadows — outlines (kept black) do the
 * separating that elevation does elsewhere. Grade accents survive as the sole
 * colour (see [gradeColor] / [LocalEink]); everything else is greyscale.
 */
private val EinkColors = lightColorScheme(
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFECECEC),
    onSurfaceVariant = Color(0xFF3A3A3A),
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E0E0),
    onPrimaryContainer = Color(0xFF000000),
    secondary = Color(0xFF2B2B2B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E0E0),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFF4A4A4A),
    onTertiary = Color(0xFFFFFFFF),
    outline = Color(0xFF000000),
    outlineVariant = Color(0xFFC4C4C4),
    error = Color(0xFF000000),
    onError = Color(0xFFFFFFFF),
)

/** True inside an e-ink themed subtree; forces [gradeColor] to its light branch. */
val LocalEink = staticCompositionLocalOf { false }

/**
 * Anki grade accents (1=Again … 4=Easy), tuned separately for light and
 * dark so they stay readable on either scheme without shouting. On e-ink the
 * light (darker, saturated) variants are used — they read on white paper.
 */
@Composable
fun gradeColor(ease: Int): Color {
    val dark = !LocalEink.current && isSystemInDarkTheme()
    return when (ease) {
        1 -> if (dark) Color(0xFFEF9A9A) else Color(0xFFB3261E)
        2 -> if (dark) Color(0xFFFFB74D) else Color(0xFFA05A00)
        3 -> if (dark) Color(0xFF81C784) else Color(0xFF2E7D32)
        4 -> if (dark) Color(0xFF64B5F6) else Color(0xFF1565C0)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun GarmankiTheme(
    // Nested calls (e.g. ScreenScaffold) inherit the mode picked at the root.
    eink: Boolean = LocalEink.current,
    content: @Composable () -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val scheme = when {
        eink -> EinkColors
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    // Stock M3 type scale: nothing below 14 sp for body text, no letter-spacing
    // tricks — the terminal look lives on only in MonoFamily log lines.
    CompositionLocalProvider(LocalEink provides eink) {
        MaterialTheme(colorScheme = scheme, typography = Typography(), content = content)
    }
}
