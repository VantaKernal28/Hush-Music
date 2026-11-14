package com.music.bitchord.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.R
import com.music.bitchord.data.settings.AppFont
import com.music.bitchord.data.settings.AppSettings

// Hush Music signature warm sunset-orange, peach, and deep-lavender aesthetic
val HushSunsetOrange = Color(0xFFFF6E40)
val HushPeach = Color(0xFFFFAB91)
val HushDeepLavender = Color(0xFFB388FF)
val HushViolet = Color(0xFF7E57C2)

// Deep charcoal / AMOLED surfaces
val HushAmoledDark = Color(0xFF0A0A0C)
val HushCharcoalSurface = Color(0xFF121214)
val HushSurfaceVariant = Color(0xFF1E1E24)
val HushCardSurface = Color(0xFF26262E)
val HushOutline = Color(0xFF32323C)

// Multi-stop gradient for cards, hero accents, and brand badges
val HushSunsetGradient = listOf(
    Color(0xFFFF5722),
    Color(0xFFFF7A45),
    Color(0xFFFFAB91),
    Color(0xFFB388FF),
    Color(0xFF673AB7),
)

// Primary accent token
val AccentRed = HushSunsetOrange

/** Hush Brand Font Family */
val CherryBombOne = FontFamily(
    Font(R.font.cherry_bomb_one_regular, FontWeight.Normal),
)

/** Accent Color Swatch definition for app-wide dynamic theme customization */
data class HushAccentSwatch(
    val id: String,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val isDynamic: Boolean = false,
)

/**
 * 14-Color Circular Accent Swatch Palette covering every color spectrum
 * plus Material You dynamic wallpaper Monet.
 */
val HushAccents = listOf(
    HushAccentSwatch("sunset", "Sunset Orange", Color(0xFFFF6E40), Color(0xFFFFAB91)),
    HushAccentSwatch("crimson", "Crimson Red", Color(0xFFE53935), Color(0xFFEF9A9A)),
    HushAccentSwatch("rose", "Rose Pink", Color(0xFFE91E63), Color(0xFFF48FB1)),
    HushAccentSwatch("purple", "Royal Purple", Color(0xFF9C27B0), Color(0xFFCE93D8)),
    HushAccentSwatch("indigo", "Deep Indigo", Color(0xFF3F51B5), Color(0xFF9FA8DA)),
    HushAccentSwatch("blue", "Electric Blue", Color(0xFF2196F3), Color(0xFF90CAF9)),
    HushAccentSwatch("cyan", "Cyan / Aqua", Color(0xFF00BCD4), Color(0xFF80DEEA)),
    HushAccentSwatch("teal", "Mint / Teal", Color(0xFF009688), Color(0xFF80CBC4)),
    HushAccentSwatch("emerald", "Emerald Green", Color(0xFF4CAF50), Color(0xFFA5D6A7)),
    HushAccentSwatch("lime", "Lime", Color(0xFF8BC34A), Color(0xFFC5E1A5)),
    HushAccentSwatch("amber", "Solar Amber", Color(0xFFFFB300), Color(0xFFFFE082)),
    HushAccentSwatch("brown", "Warm Brown", Color(0xFF8D6E63), Color(0xFFBCAAA4)),
    HushAccentSwatch("monochrome", "Monochrome", Color(0xFF90A4AE), Color(0xFFCFD8DC)),
    HushAccentSwatch("dynamic", "Dynamic Monet", Color(0xFFFF6E40), Color(0xFFFFAB91), isDynamic = true),
)

@Composable
fun resolveAccentColor(accentId: String, dark: Boolean): Color {
    val context = LocalContext.current
    if (accentId == "dynamic" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val scheme = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        return scheme.primary
    }
    return HushAccents.find { it.id == accentId }?.primary ?: HushSunsetOrange
}

/** Standard M3 corner radii */
val StandardShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

/** Material 3 Expressive corner radii: expanded organic containers (24-28dp) */
val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

val LocalExpressiveMode = compositionLocalOf { false }
val LocalHushAccent = compositionLocalOf { HushSunsetOrange }

private val DarkColors = darkColorScheme(
    primary = HushSunsetOrange,
    onPrimary = Color.Black,
    secondary = HushPeach,
    onSecondary = Color.Black,
    tertiary = HushDeepLavender,
    onTertiary = Color.Black,
    background = HushAmoledDark,
    onBackground = Color.White,
    surface = HushCharcoalSurface,
    onSurface = Color.White,
    surfaceVariant = HushSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0B0BC),
    outline = HushOutline,
)

private val LightColors = lightColorScheme(
    primary = HushSunsetOrange,
    onPrimary = Color.White,
    secondary = Color(0xFFE64A19),
    onSecondary = Color.White,
    tertiary = HushDeepLavender,
    onTertiary = Color.White,
    background = Color(0xFFFAF9F8),
    onBackground = Color(0xFF121214),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF121214),
    surfaceVariant = Color(0xFFF2F0F4),
    onSurfaceVariant = Color(0xFF6A6A74),
    outline = Color(0xFFE0DEE4),
)

val SFProDisplay = FontFamily(
    Font(R.font.sf_pro_display_regular, FontWeight.W400),
    Font(R.font.sf_pro_display_regular, FontWeight.W500),
    Font(R.font.sf_pro_display_medium, FontWeight.W600),
    Font(R.font.sf_pro_display_semibold, FontWeight.W700),
    Font(R.font.sf_pro_display_bold, FontWeight.W800),
)

private val BitChordTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.W800, fontSize = 34.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.W800, fontSize = 30.sp, letterSpacing = (-0.4).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.W700, fontSize = 22.sp, letterSpacing = (-0.25).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.W700, fontSize = 20.sp, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.W600, fontSize = 16.sp, letterSpacing = 0.05.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.W400, fontSize = 16.sp, letterSpacing = 0.2.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.W400, fontSize = 14.sp, letterSpacing = 0.3.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.W600, fontSize = 12.sp, letterSpacing = 0.25.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.W600, fontSize = 11.sp, letterSpacing = 0.3.sp),
).withFamily(SFProDisplay)

val SFProDisplayLegacy = FontFamily(
    Font(R.font.sf_pro_display_regular, FontWeight.W400),
    Font(R.font.sf_pro_display_medium, FontWeight.W500),
    Font(R.font.sf_pro_display_semibold, FontWeight.W600),
    Font(R.font.sf_pro_display_bold, FontWeight.W700),
    Font(R.font.sf_pro_display_heavy, FontWeight.W800),
)

private val LegacyTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.W800, fontSize = 34.sp, letterSpacing = (-0.8).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.W800, fontSize = 30.sp, letterSpacing = (-0.7).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.W700, fontSize = 22.sp, letterSpacing = (-0.4).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.W700, fontSize = 20.sp, letterSpacing = (-0.3).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.W600, fontSize = 16.sp, letterSpacing = (-0.2).sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.W400, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.W400, fontSize = 14.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.W600, fontSize = 12.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.W600, fontSize = 11.sp),
).withFamily(SFProDisplayLegacy)

private fun Typography.withFamily(family: FontFamily) = Typography(
    displayLarge = displayLarge.copy(fontFamily = family),
    displayMedium = displayMedium.copy(fontFamily = family),
    displaySmall = displaySmall.copy(fontFamily = family),
    headlineLarge = headlineLarge.copy(fontFamily = family),
    headlineMedium = headlineMedium.copy(fontFamily = family),
    headlineSmall = headlineSmall.copy(fontFamily = family),
    titleLarge = titleLarge.copy(fontFamily = family),
    titleMedium = titleMedium.copy(fontFamily = family),
    titleSmall = titleSmall.copy(fontFamily = family),
    bodyLarge = bodyLarge.copy(fontFamily = family),
    bodyMedium = bodyMedium.copy(fontFamily = family),
    bodySmall = bodySmall.copy(fontFamily = family),
    labelLarge = labelLarge.copy(fontFamily = family),
    labelMedium = labelMedium.copy(fontFamily = family),
    labelSmall = labelSmall.copy(fontFamily = family),
)

@Composable
fun BitChordTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    font: AppFont = AppFont.DEFAULT,
    content: @Composable () -> Unit,
) {
    val currentAccentId by AppSettings.themeAccent.collectAsStateWithLifecycle()
    val isExpressive by AppSettings.expressiveMode.collectAsStateWithLifecycle()
    val resolvedAccent = resolveAccentColor(currentAccentId, darkTheme)

    val baseScheme = if (darkTheme) DarkColors else LightColors
    val colorScheme = baseScheme.copy(
        primary = resolvedAccent,
        primaryContainer = resolvedAccent.copy(alpha = if (darkTheme) 0.22f else 0.16f),
        onPrimaryContainer = if (darkTheme) Color.White else Color.Black,
        secondary = resolvedAccent.copy(alpha = 0.85f),
        secondaryContainer = resolvedAccent.copy(alpha = if (darkTheme) 0.16f else 0.10f),
    )

    val shapes = if (isExpressive) ExpressiveShapes else StandardShapes

    CompositionLocalProvider(
        LocalExpressiveMode provides isExpressive,
        LocalHushAccent provides resolvedAccent,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = when (font) {
                AppFont.DEFAULT -> BitChordTypography
                AppFont.LEGACY -> LegacyTypography
            },
            shapes = shapes,
            content = content,
        )
    }
}

/** Hush Music's primary theme entrypoint. */
@Composable
fun HushMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    font: AppFont = AppFont.DEFAULT,
    content: @Composable () -> Unit,
) = BitChordTheme(darkTheme, font, content)

@Composable
fun SystemBarIcons(dark: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    val window = findWindow(view) ?: return
    SideEffect {
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = dark
            isAppearanceLightNavigationBars = dark
        }
    }
}
