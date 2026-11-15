package com.music.bitchord.ui.components.expressive

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.music.bitchord.R
import com.music.bitchord.data.settings.AppFont
import com.music.bitchord.data.settings.AppSettings
import kotlinx.coroutines.delay

/**
 * Cinematic Hush Music Theme Transition Overlay.
 *
 * Plays when switching to Material 3 Expressive mode:
 * Shows the clean Hush brand logo scaling and blurring into place,
 * switches the theme mid-flight at 700ms, and softly fades out to reveal the M3E UI.
 */
@Composable
fun HushThemeTransitionOverlay(
    onAnimationComplete: () -> Unit,
) {
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }
    val entranceBlur = remember { Animatable(18f) }
    val exitOffset = remember { Animatable(0f) }
    val exitBlur = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1.0f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(1.0f, animationSpec = tween(650, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        entranceBlur.animateTo(0f, animationSpec = tween(750, easing = FastOutSlowInEasing))
        // Mid-point: switch theme to Material 3 Expressive mode
        delay(700)
        AppSettings.setMaterialUiMode(true)
        AppSettings.setExpressiveUiEnabled(true)
        AppSettings.setAppFont(AppFont.PIXEL_ROUND)
        delay(450)
        // Soft blur-fade exit
        exitBlur.animateTo(18f, animationSpec = tween(380, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(1150)
        exitOffset.animateTo(-60f, animationSpec = tween(350, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(1200)
        exitAlpha.animateTo(0f, animationSpec = tween(320, easing = FastOutSlowInEasing))
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08090D))
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF263238).copy(alpha = 0.55f),
                        Color(0xFF0F1117),
                        Color(0xFF08090D),
                    ),
                    radius = 1400f,
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = exitOffset.value
                    this.alpha = (alpha.value * exitAlpha.value).coerceIn(0f, 1f)
                    scaleX = scale.value.coerceAtLeast(0.01f)
                    scaleY = scale.value.coerceAtLeast(0.01f)
                }
                .blur(maxOf(0f, if (entranceBlur.value > 0.1f) entranceBlur.value else exitBlur.value).dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_hush_transparent_logo),
                contentDescription = "Hush Music",
                modifier = Modifier
                    .size(108.dp)
                    .clip(RoundedCornerShape(30.dp)),
            )
        }
    }
}

/**
 * BitChord Classic Theme Transition Overlay.
 *
 * Plays when switching back to BitChord mode:
 * Shows the classic BitChord logo scaling and blurring, switches back to BitChord
 * defaults mid-flight (with liquid glass enabled), and smoothly fades out.
 */
@Composable
fun BitChordThemeTransitionOverlay(
    onAnimationComplete: () -> Unit,
) {
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }
    val entranceBlur = remember { Animatable(18f) }
    val exitOffset = remember { Animatable(0f) }
    val exitBlur = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1.0f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(1.0f, animationSpec = tween(650, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        entranceBlur.animateTo(0f, animationSpec = tween(750, easing = FastOutSlowInEasing))
        // Mid-point: switch theme to BitChord mode
        delay(700)
        AppSettings.setMaterialUiMode(false)
        AppSettings.setExpressiveUiEnabled(false)
        AppSettings.setAppFont(AppFont.DEFAULT)
        AppSettings.setLiquidGlass(true)
        delay(450)
        // Soft blur-fade exit
        exitBlur.animateTo(18f, animationSpec = tween(380, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(1150)
        exitOffset.animateTo(-60f, animationSpec = tween(350, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(1200)
        exitAlpha.animateTo(0f, animationSpec = tween(320, easing = FastOutSlowInEasing))
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B0E))
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF1E293B).copy(alpha = 0.5f),
                        Color(0xFF0F172A),
                        Color(0xFF0A0B0E),
                    ),
                    radius = 1400f,
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = exitOffset.value
                    this.alpha = (alpha.value * exitAlpha.value).coerceIn(0f, 1f)
                    scaleX = scale.value.coerceAtLeast(0.01f)
                    scaleY = scale.value.coerceAtLeast(0.01f)
                }
                .blur(maxOf(0f, if (entranceBlur.value > 0.1f) entranceBlur.value else exitBlur.value).dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "BitChord Classic",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(26.dp)),
            )
        }
    }
}
