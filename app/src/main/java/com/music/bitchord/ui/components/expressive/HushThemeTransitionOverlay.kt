package com.music.bitchord.ui.components.expressive

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.R
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.theme.CherryBombOne
import kotlinx.coroutines.delay

/**
 * Cinematic Hush Music Theme Transition Overlay.
 *
 * Plays when the user switches to Material You Theme:
 * 1. Fullscreen ambient backdrop with radial glow.
 * 2. Logo entrance with calm scale-up (0.88f -> 1.0f), blur resolution (18dp -> 0dp), and opacity fade-in.
 * 3. Brand typography entrance in crisp white.
 * 4. Mid-flight theme switch at 700ms to Material You.
 * 5. Smooth blur & horizontal/scale exit (18dp blur, fade to 0), revealing the transformed UI.
 */
@Composable
fun HushThemeTransitionOverlay(
    onAnimationComplete: () -> Unit,
) {
    val scale = remember { Animatable(0.88f) }
    val alpha = remember { Animatable(0f) }
    val entranceBlur = remember { Animatable(16f) }
    val exitOffset = remember { Animatable(0f) }
    val exitBlur = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }
    var subtitleText by remember { mutableStateOf("Transforming to Material You...") }

    LaunchedEffect(Unit) {
        // Logo Entrance: luxurious scale-up 0.88f -> 1.0f
        scale.animateTo(1.0f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(1.0f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        entranceBlur.animateTo(0f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        // Mid-point: switch theme to Material You while logo is holding center
        delay(700)
        AppSettings.setMaterialUiMode(true)
        AppSettings.setExpressiveUiEnabled(true)
        subtitleText = "Material You Active"
        delay(500)
        // Soft blur-fade exit
        exitBlur.animateTo(18f, animationSpec = tween(400, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(1250)
        exitOffset.animateTo(-60f, animationSpec = tween(380, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(1300)
        exitAlpha.animateTo(0f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08090D))
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF263238).copy(alpha = 0.5f),
                        Color(0xFF0F1117),
                        Color(0xFF08090D),
                    ),
                    radius = 1400f,
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer {
                    translationX = exitOffset.value
                    this.alpha = (alpha.value * exitAlpha.value).coerceIn(0f, 1f)
                    scaleX = scale.value.coerceAtLeast(0.01f)
                    scaleY = scale.value.coerceAtLeast(0.01f)
                }
                .blur(maxOf(0f, if (entranceBlur.value > 0.1f) entranceBlur.value else exitBlur.value).dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_hush_transparent_logo),
                contentDescription = "Hush Music",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(26.dp)),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Hush Music",
                fontFamily = CherryBombOne,
                fontSize = 38.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.2.sp,
            )
        }
    }
}
