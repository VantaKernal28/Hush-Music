package com.music.bitchord.wear.ui

import androidx.compose.runtime.Composable
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

/** Hush Signature Brand Palette */
val HushOrange = Color(0xFFFF5722)
val HushCoral = Color(0xFFFF6E40)
val HushViolet = Color(0xFF7C4DFF)
val HushRose = Color(0xFFFA2D48)
val AccentRed = HushRose

/** Translucent Frosted Glass Materials */
val GlassSurface = Color(0x24FFFFFF)
val GlassBorder = Color(0x2EFFFFFF)
val GlassBorderSubtle = Color(0x1AFFFFFF)

/** Dark Surface Container Hierarchy */
val Secondary = Color(0xFF9E9EA6)
val Cell = Color(0xFF1B1D25)
val CellActive = Color(0xFF262835)
val CellBorder = Color(0x1FFFFFFF)

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
 * Hush Music theme for Wear OS:
 * Vibrant Hush Coral/Orange accents, frosted liquid glass surfaces,
 * and deep AMOLED blacks for extreme power efficiency.
 */
@Composable
fun HushWearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme(
            primary = HushCoral,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF381E19),
            onPrimaryContainer = Color(0xFFFFDBCE),
            secondary = HushViolet,
            onSecondary = Color.White,
            background = Color.Black,
            onBackground = Color(0xFFF2F2F7),
            surface = Color(0xFF13141B),
            onSurface = Color(0xFFF2F2F7),
            onSurfaceVariant = Secondary,
            surfaceContainer = Cell,
            surfaceContainerHigh = CellActive,
            outline = GlassBorder,
            outlineVariant = GlassBorderSubtle,
        ),
        typography = Typography(defaultFontFamily = SFPro),
        content = content,
    )
}

@Composable
fun BitChordWearTheme(content: @Composable () -> Unit) = HushWearTheme(content)
