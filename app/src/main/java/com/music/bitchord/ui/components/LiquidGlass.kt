/*
 * The glass rendering itself is Kyant0/backdrop (Apache-2.0), vendored at
 * [com.music.bitchord.ui.components.backdrop] — see that package for the
 * upstream attribution. This file is the integration glue, adapted from
 * EchoMusicApp/Echo-Music's GlassEffectConfig/Modifier.liquidGlass
 * (GPL-3.0), cut down from Echo's full per-component/vibrancy-slider config
 * to the single on/off switch BitChord exposes in Settings. It is used by the
 * floating nav bar and, while enabled, the app-wide floating top controls.
 */
package com.music.bitchord.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.components.backdrop.Backdrop
import com.music.bitchord.ui.components.backdrop.backdrops.LayerBackdrop
import com.music.bitchord.ui.components.backdrop.drawBackdrop
import com.music.bitchord.ui.components.backdrop.effects.blur
import com.music.bitchord.ui.components.backdrop.effects.colorControls
import com.music.bitchord.ui.components.backdrop.effects.lens
import com.music.bitchord.ui.components.backdrop.highlight.Highlight
import com.music.bitchord.ui.components.backdrop.highlight.HighlightElement
import com.music.bitchord.ui.components.backdrop.internal.ShapeProvider
import com.music.bitchord.ui.components.backdrop.shadow.Shadow
import androidx.compose.ui.unit.dp

/** Whether the liquid glass nav bar is turned on — see [AppSettings.liquidGlass]. */
val LocalLiquidGlassEnabled = staticCompositionLocalOf { false }

/** The backdrop content (app UI) that a liquid glass surface samples from. */
val LocalAppBackdrop = staticCompositionLocalOf<Backdrop> { error("No AppBackdrop provided") }

/**
 * Where a [liquidGlass] surface composed under it also records the glass it
 * draws — the sampled, blurred and tinted backdrop, without its rim or shadow —
 * so a lens above it can refract that surface rather than the page behind it.
 * The tab bar's travelling selection pill is the one reader; see
 * [com.music.bitchord.ui.components.floatingtabbar.GlassSelectionPill].
 */
internal val LocalGlassExport = compositionLocalOf<LayerBackdrop?> { null }

/**
 * The backdrop blur pipeline requires [android.graphics.RenderEffect] on a
 * [android.graphics.RenderNode], available from Android 12 (API 31).
 */
fun isGlassSupported(sdkInt: Int = Build.VERSION.SDK_INT): Boolean = sdkInt >= Build.VERSION_CODES.S

/** Apple-matched defaults (Echo's GlassEffectConfig()), fixed rather than user sliders. */
private const val VIBRANCY = 1f
private const val BLUR_RADIUS_DP = 8f
private const val LENS_HEIGHT = 0.5f
private const val LENS_AMOUNT = 0.5f
private const val LENS_MAX_DP = 48f
private const val SURFACE_OPACITY = 0.4f

/**
 * Resolution fraction the glass surface records and processes its backdrop at.
 *
 * A third, which is what Echo's `glassResolutionScale` comes to at this blur
 * radius. It was briefly raised to a half while the sharp-text bug was being
 * chased — wrongly, as it turned out: the cause was a transparent backdrop, not
 * the resample (see MainActivity's `paintBackdrop`). A third is nine times fewer
 * pixels than full resolution through the colour matrix, the blur and the lens
 * shader, on as many as six surfaces at once in the middle of a fold, and the
 * blur is what hides the upscale.
 */
private const val GLASS_RESOLUTION_SCALE = 0.33f

/**
 * The blur and resolution under [AppSettings.glassPerformance]. With no lens to
 * bend what is behind it, the frost alone has to break up a page's text, so it
 * is twice the radius — and that wider blur hides a smaller recording, which
 * takes the surface from about 11% of its pixels to about 6%.
 */
private const val FROST_BLUR_RADIUS_DP = 16f
private const val FROST_RESOLUTION_SCALE = 0.25f

/**
 * The hairline along a bar's edge, and what stands in for the glass rim
 * wherever the glass itself is not drawn.
 *
 * A surface filled with the theme's own `surface` colour has no edge of its own
 * against a dark page — it is the same near-black the page is. The glass gets
 * its edge from [Highlight], and this is that edge for everything that does not.
 */
internal val GLASS_EDGE_WIDTH = 0.5.dp
internal val GLASS_EDGE_COLOR = Color.White.copy(alpha = 0.10f)

/**
 * Icon and label colour for content sitting on a glass surface.
 *
 * Glass shows whatever is behind it rather than the theme's surface colour, so
 * the usual onSurface greys have nothing dependable to sit against. Pure black
 * or white off the theme's luminance is the only tint that holds against
 * arbitrary artwork, and it is what Echo's own glass nav bar uses.
 */
@Composable
fun glassContentColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) Color.Black else Color.White

/**
 * Selected-tab indicator colour for a glass surface: white in both themes.
 *
 * In light theme that is the inverse of [glassContentColor], a bright lift
 * behind a dark glyph. In dark theme it is a faint white lift over the bar's
 * dark glass rather than a black scrim pressed into it — the selected tab is
 * raised out of the bar, as on iOS, not sunk.
 */
@Composable
fun glassIndicatorColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color.White.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = DARK_INDICATOR_OPACITY)
    }

/** The selected tab's pill in dark theme, a step whiter than the bar. */
private const val DARK_INDICATOR_OPACITY = 0.14f

/**
 * A lightweight visual match for liquid glass over a stable background.
 *
 * This keeps the same translucent tint, directional highlight and hairline as
 * [liquidGlass], but intentionally performs no backdrop capture, blur, lens
 * refraction or shadow rendering. When Liquid Glass is disabled or unsupported,
 * [fallbackColor] preserves the control's existing filled appearance.
 */
@Composable
fun Modifier.lightweightLiquidGlass(
    shape: CornerBasedShape,
    fallbackColor: Color,
): Modifier {
    val dark = isSystemInDarkTheme()
    val useGlass = LocalLiquidGlassEnabled.current && isGlassSupported()
    if (!useGlass) {
        return clip(shape)
            .background(color = fallbackColor, shape = shape)
            .border(GLASS_EDGE_WIDTH, GLASS_EDGE_COLOR, shape)
    }

    val fill = Brush.verticalGradient(
        colors = if (dark) {
            listOf(Color(0xFF242426).copy(alpha = 0.44f), Color(0xFF1C1C1E).copy(alpha = 0.52f))
        } else {
            listOf(Color.White.copy(alpha = 0.92f), Color.White.copy(alpha = 0.66f))
        },
    )
    val sheen = Brush.linearGradient(
        0f to Color.White.copy(alpha = if (dark) 0.11f else 0.55f),
        0.42f to Color.Transparent,
        1f to Color.Transparent,
    )
    val rim = Brush.verticalGradient(
        colors = if (dark) {
            listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.04f))
        } else {
            listOf(Color.White.copy(alpha = 0.95f), Color.Black.copy(alpha = 0.05f))
        },
    )

    return clip(shape)
        .background(fill)
        .background(sheen)
        .border(width = 1.dp, brush = rim, shape = shape)
}

/**
 * Just the rim of liquid glass — the directional [Highlight] that catches the
 * light along a glass edge — for a control that keeps its own fill.
 */
@Composable
fun Modifier.liquidGlassRim(shape: CornerBasedShape): Modifier {
    if (!LocalLiquidGlassEnabled.current || !isGlassSupported()) return this
    val shapeProvider = remember(shape) { ShapeProvider { shape } }
    return then(
        HighlightElement(
            shapeProvider = shapeProvider,
            highlight = { Highlight.Default },
        ),
    )
}

/**
 * Exact port of ozyern/exhale's [Modifier.liquidGlassSurface] for general composables and cards.
 * Combines dual vertical gradient fill, diagonal specular sheen, and hairline specular rim.
 */
@Composable
fun Modifier.liquidGlassSurface(
    shape: Shape,
    tint: Color = Color.Unspecified,
    base: Color = Color.Unspecified,
): Modifier {
    val dark = isSystemInDarkTheme()

    val fill = Brush.verticalGradient(
        colors = if (dark) {
            listOf(Color(0xFF242426).copy(alpha = 0.44f), Color(0xFF1C1C1E).copy(alpha = 0.52f))
        } else {
            listOf(Color.White.copy(alpha = 0.92f), Color.White.copy(alpha = 0.66f))
        },
    )

    val sheen = Brush.linearGradient(
        0f to Color.White.copy(alpha = if (dark) 0.11f else 0.55f),
        0.42f to Color.Transparent,
        1f to Color.Transparent,
    )

    val rim = Brush.verticalGradient(
        colors = if (dark) {
            listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.04f))
        } else {
            listOf(Color.White.copy(alpha = 0.95f), Color.Black.copy(alpha = 0.05f))
        },
    )

    return this
        .clip(shape)
        .then(if (base.isSpecified) Modifier.background(base) else Modifier)
        .background(fill)
        .then(
            if (tint.isSpecified) {
                Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            tint.copy(alpha = if (dark) 0.14f else 0.16f),
                            tint.copy(alpha = if (dark) 0.02f else 0.03f),
                        ),
                    )
                )
            } else {
                Modifier
            }
        )
        .background(sheen)
        .border(width = 1.dp, brush = rim, shape = shape)
}

/**
 * Renders this composable as a liquid glass surface sampling [LocalAppBackdrop]
 * with ozyern/exhale's full multi-layered glass pipeline:
 * 1. Dual gradient fill for physical depth & specular luminosity
 * 2. Diagonal sheen gradient capturing corner reflection
 * 3. 1.dp top-lit specular rim highlight
 * 4. Vibrancy saturation boost and progressive blur diffusion
 * 5. Hardware lens refraction on Android 13+ (Tiramisu)
 * 6. Authentic hardware fallback on unsupported devices / reduceDynamicBlur
 */
@Composable
fun Modifier.liquidGlass(
    shape: CornerBasedShape,
    /** Drawn over the glass in place of the theme's default tint, when set. */
    surfaceTint: Color? = null,
): Modifier {
    val dark = isSystemInDarkTheme()
    val reduceDynamicBlur by AppSettings.reduceDynamicBlur.collectAsStateWithLifecycle()
    val progressiveBlur by AppSettings.progressiveBlur.collectAsStateWithLifecycle()
    val frostedGlass by AppSettings.frostedGlass.collectAsStateWithLifecycle()
    val glassDepthEffect by AppSettings.glassDepthEffect.collectAsStateWithLifecycle()
    val glassTransparency by AppSettings.glassTransparency.collectAsStateWithLifecycle()
    val glassRefraction by AppSettings.glassRefraction.collectAsStateWithLifecycle()

    val alphaDark1 = ((1.0f - glassTransparency * 0.45f) * (if (frostedGlass) 0.60f else 0.44f)).coerceIn(0.15f, 0.85f)
    val alphaDark2 = ((1.0f - glassTransparency * 0.45f) * (if (frostedGlass) 0.68f else 0.52f)).coerceIn(0.20f, 0.90f)
    val alphaLight1 = ((1.0f - glassTransparency * 0.35f) * 0.92f).coerceIn(0.40f, 0.98f)
    val alphaLight2 = ((1.0f - glassTransparency * 0.35f) * 0.66f).coerceIn(0.30f, 0.85f)

    val fill = Brush.verticalGradient(
        colors = if (dark) {
            listOf(Color(0xFF242426).copy(alpha = alphaDark1), Color(0xFF1C1C1E).copy(alpha = alphaDark2))
        } else {
            listOf(Color.White.copy(alpha = alphaLight1), Color.White.copy(alpha = alphaLight2))
        },
    )

    val sheen = Brush.linearGradient(
        0f to Color.White.copy(alpha = (if (dark) 0.11f else 0.55f) * (0.6f + 0.4f * glassDepthEffect)),
        0.42f to Color.Transparent,
        1f to Color.Transparent,
    )

    val rim = Brush.verticalGradient(
        colors = if (dark) {
            listOf(
                Color.White.copy(alpha = (0.12f + 0.16f * glassDepthEffect).coerceIn(0.05f, 0.40f)),
                Color.White.copy(alpha = (0.02f + 0.04f * glassDepthEffect).coerceIn(0.01f, 0.15f)),
            )
        } else {
            listOf(
                Color.White.copy(alpha = (0.75f + 0.24f * glassDepthEffect).coerceIn(0.4f, 1f)),
                Color.Black.copy(alpha = (0.02f + 0.06f * glassDepthEffect).coerceIn(0.01f, 0.15f)),
            )
        },
    )

    // Fallback if hardware RenderEffect is not supported or reduceDynamicBlur is requested:
    // Render the complete multi-layered glass surface so it looks like genuine glass without shader pass
    if (!isGlassSupported() || reduceDynamicBlur) {
        return this
            .clip(shape)
            .background(fill)
            .then(
                if (frostedGlass) {
                    Modifier.background(if (dark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.35f))
                } else if (surfaceTint != null) {
                    Modifier.background(surfaceTint)
                } else Modifier
            )
            .background(sheen)
            .border(width = 1.dp, brush = rim, shape = shape)
    }

    val backdrop = LocalAppBackdrop.current
    val exportedBackdrop = LocalGlassExport.current
    val density = LocalDensity.current
    val resolutionScale = GLASS_RESOLUTION_SCALE
    val blurRadiusDp = when {
        frostedGlass -> 32f
        progressiveBlur -> 20f
        else -> BLUR_RADIUS_DP
    }
    val blurPx = with(density) { blurRadiusDp.dp.toPx() } * resolutionScale
    val lensHeightPx = with(density) { (LENS_HEIGHT * LENS_MAX_DP * glassRefraction).dp.toPx() } * resolutionScale
    val lensAmountPx = with(density) { (LENS_AMOUNT * LENS_MAX_DP * glassRefraction).dp.toPx() } * resolutionScale
    val chromaticAberration = !frostedGlass && (glassRefraction > 0.15f)

    return this
        .drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                colorControls(saturation = if (frostedGlass) 1.25f else 1.6f)
                blur(blurPx)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    lens(
                        refractionHeight = lensHeightPx,
                        refractionAmount = lensAmountPx,
                        depthEffect = glassDepthEffect > 0.05f,
                        chromaticAberration = chromaticAberration,
                    )
                }
            },
            highlight = { Highlight.Default },
            shadow = { Shadow.Default.copy(alpha = glassDepthEffect.coerceIn(0f, 1f)) },
            onDrawSurface = {
                drawRect(fill)
                if (frostedGlass) {
                    drawRect(if (dark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.35f))
                } else if (surfaceTint != null) {
                    drawRect(surfaceTint)
                }
                drawRect(sheen)
            },
            exportedBackdrop = exportedBackdrop,
            backdropScale = resolutionScale,
        )
        .border(width = 1.dp, brush = rim, shape = shape)
}
