package com.music.bitchord.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import com.music.bitchord.wear.R

/**
 * SF Pro Display, matching Hush Music on the phone.
 */
val SFPro = FontFamily(
    Font(R.font.sf_pro_display_regular, FontWeight.W400),
    Font(R.font.sf_pro_display_medium, FontWeight.W500),
    Font(R.font.sf_pro_display_semibold, FontWeight.W600),
    Font(R.font.sf_pro_display_bold, FontWeight.W700),
)

/** Hush Music phone-identical palette tokens */
val HushSunsetOrange = Color(0xFFFF6E40)
val HushOrange = HushSunsetOrange
val HushCoral = HushSunsetOrange
val HushPeach = Color(0xFFFFAB91)
val HushDeepLavender = Color(0xFFB388FF)
val HushViolet = Color(0xFF7E57C2)
val HushRose = Color(0xFFFA2D48)
val AccentRed = HushRose

/** Multi-stop gradient matching Hush phone badges & fallback ambient mesh */
val HushSunsetGradient = listOf(
    Color(0xFFFF5722),
    Color(0xFFFF7A45),
    Color(0xFFB388FF),
    Color(0xFF673AB7),
)

/** Hush Classic Liquid Glass & AMOLED Dark Surfaces */
val HushAmoledDark = Color(0xFF0A0A0C)
val HushCharcoalSurface = Color(0xFF121214)
val HushSurfaceVariant = Color(0xFF1E1E24)
val HushCardSurface = Color(0xFF26262E)
val HushOutline = Color(0xFF32323C)

/** Hush Material 3 Expressive Dark Tonal Surfaces */
val HushM3Surface = Color(0xFF111411)
val HushM3Container = Color(0xFF1D221D)
val HushM3ContainerHigh = Color(0xFF282E27)
val HushM3OnSurface = Color(0xFFE1E3DF)
val HushM3OnSurfaceVariant = Color(0xFFC3C8BD)

/** Translucent Frosted Liquid Glass Tokens */
val GlassSurface = Color(0x24FFFFFF)
val GlassBorder = Color(0x38FFFFFF)
val GlassBorderSubtle = Color(0x18FFFFFF)

val Secondary = Color(0xFFB0B0BC)
val Cell = HushCharcoalSurface
val CellActive = HushCardSurface
val CellBorder = GlassBorderSubtle

@Immutable
data class HushWearStyle(
    val accentPrimary: Color = HushSunsetOrange,
    val accentSecondary: Color = HushPeach,
    val materialUiMode: Boolean = false,
)

val LocalHushWearStyle = compositionLocalOf { HushWearStyle() }

object WearType {
    val title = TextStyle(fontFamily = SFPro, fontWeight = FontWeight.W600, fontSize = 15.sp, letterSpacing = (-0.2).sp)
    val subtitle = TextStyle(fontFamily = SFPro, fontWeight = FontWeight.W400, fontSize = 13.sp, letterSpacing = (-0.1).sp)
    val header = TextStyle(fontFamily = SFPro, fontWeight = FontWeight.W700, fontSize = 17.sp, letterSpacing = (-0.3).sp)
    val row = TextStyle(fontFamily = SFPro, fontWeight = FontWeight.W500, fontSize = 14.sp, letterSpacing = (-0.15).sp)
    val rowSub = TextStyle(fontFamily = SFPro, fontWeight = FontWeight.W400, fontSize = 12.sp)
    val button = TextStyle(fontFamily = SFPro, fontWeight = FontWeight.W600, fontSize = 14.sp)
    val caption = TextStyle(fontFamily = SFPro, fontWeight = FontWeight.W500, fontSize = 11.sp, letterSpacing = 0.2.sp)
}

/**
 * Hush Music theme for Wear OS, mirroring the phone app's active accent swatch
 * and Classic Liquid Glass vs Material 3 Expressive surface scheme.
 */
@Composable
fun HushWearTheme(
    style: HushWearStyle = HushWearStyle(),
    content: @Composable () -> Unit,
) {
    val primary = style.accentPrimary
    val secondary = style.accentSecondary
    val scheme = if (style.materialUiMode) {
        ColorScheme(
            primary = primary,
            onPrimary = Color.Black,
            primaryContainer = primary.copy(alpha = 0.28f),
            onPrimaryContainer = Color.White,
            secondary = secondary,
            onSecondary = Color.Black,
            background = Color.Black,
            onBackground = HushM3OnSurface,
            onSurface = HushM3OnSurface,
            onSurfaceVariant = HushM3OnSurfaceVariant,
            surfaceContainer = HushM3Container,
            surfaceContainerHigh = HushM3ContainerHigh,
            outline = HushOutline,
            outlineVariant = GlassBorderSubtle,
        )
    } else {
        ColorScheme(
            primary = primary,
            onPrimary = Color.Black,
            primaryContainer = primary.copy(alpha = 0.22f),
            onPrimaryContainer = Color.White,
            secondary = secondary,
            onSecondary = Color.Black,
            background = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White,
            onSurfaceVariant = Secondary,
            surfaceContainer = HushSurfaceVariant,
            surfaceContainerHigh = HushCardSurface,
            outline = GlassBorder,
            outlineVariant = GlassBorderSubtle,
        )
    }
    CompositionLocalProvider(LocalHushWearStyle provides style) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography(defaultFontFamily = SFPro),
            content = content,
        )
    }
}

@Composable
fun BitChordWearTheme(
    style: HushWearStyle = HushWearStyle(),
    content: @Composable () -> Unit,
) = HushWearTheme(style, content)
