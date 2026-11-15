package com.music.bitchord.ui.components.expressive

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable Material 3 Expressive tactile click modifier.
 *
 * Tracks press states to:
 * 1. Scale down interactive elements on press (`0.93f`) with snappy spring damping.
 * 2. Recoil with a bouncy overshoot (`1.03f -> 1.0f`) on release.
 * 3. Dynamically morph corner radius on press (e.g., `26.dp -> 14.dp`).
 * 4. Trigger subtle tactile haptic feedback.
 * 5. Uses `graphicsLayer` for GPU-accelerated 60/120fps animation without recomposition overhead.
 */
fun Modifier.expressiveClickable(
    enabled: Boolean = true,
    pressScale: Float = 0.93f,
    overshootScale: Float = 1.03f,
    unpressedCorner: Dp? = null,
    pressedCorner: Dp? = null,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val actualInteraction = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by actualInteraction.collectIsPressedAsState()

    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            scaleAnim.animateTo(
                targetValue = pressScale,
                animationSpec = ExpressiveMotion.TactilePressSpring,
            )
        } else {
            if (scaleAnim.value < 0.99f) {
                // Perform bouncy overshoot on release
                scaleAnim.animateTo(
                    targetValue = overshootScale,
                    animationSpec = ExpressiveMotion.BouncyOvershootSpring,
                )
                scaleAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = ExpressiveMotion.SpatialSpring,
                )
            } else {
                scaleAnim.snapTo(1f)
            }
        }
    }

    var modifier = this
        .graphicsLayer {
            scaleX = scaleAnim.value
            scaleY = scaleAnim.value
        }

    if (unpressedCorner != null && pressedCorner != null) {
        val cornerRadius by animateDpAsState(
            targetValue = if (isPressed) pressedCorner else unpressedCorner,
            animationSpec = ExpressiveMotion.CornerMorphSpring,
            label = "expressiveCornerMorph",
        )
        modifier = modifier.clip(RoundedCornerShape(cornerRadius))
    }

    modifier.clickable(
        interactionSource = actualInteraction,
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )
}

/**
 * Lightweight tactile squish modifier for icons, FABs, and playback transport buttons.
 */
fun Modifier.expressiveSquish(
    isPressed: Boolean,
    pressScale: Float = 0.91f,
): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressScale else 1f,
        animationSpec = if (isPressed) ExpressiveMotion.TactilePressSpring else ExpressiveMotion.BouncyOvershootSpring,
        label = "expressiveSquish",
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

fun Modifier.expressiveSquish(
    interactionSource: MutableInteractionSource,
    pressScale: Float = 0.91f,
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    expressiveSquish(isPressed = isPressed, pressScale = pressScale)
}

/**
 * Material 3 Expressive tactile squish modifier with its own internal interaction source.
 */
fun Modifier.m3eSquish(pressScale: Float = 0.92f): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    expressiveSquish(isPressed = isPressed, pressScale = pressScale)
}

/**
 * Material 3 Expressive generous container modifier with bold tactile corners and subtle elevation tint.
 */
fun Modifier.expressiveTonalCard(
    cornerRadius: Dp = 24.dp,
    containerColor: Color = Color.White.copy(alpha = 0.07f),
    borderColor: Color = Color.White.copy(alpha = 0.12f),
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(containerColor)
    .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
