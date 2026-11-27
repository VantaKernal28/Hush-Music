package com.music.bitchord.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.icons.SfIcons

/**
 * Sleek, circular Material 3 button in the top-left corner with pulsing AI icon.
 * Features Liquid Glass frosted blur & specular rim in Classic mode,
 * clean M3E tonal elevation in Material UI mode, smooth breathing animation,
 * and spring press feedback that opens the AI Playlist Maker.
 */
@Composable
fun AiPlaylistPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val materialUi by AppSettings.materialUiMode.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val circleShape = remember { CircleShape }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "ai_press_scale",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ai_pill_fx")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ai_shimmer_alpha",
    )
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ai_breathe_scale",
    )

    val accent = MaterialTheme.colorScheme.primary

    val glassModifier = if (!materialUi) {
        Modifier.liquidGlass(
            shape = circleShape,
            surfaceTint = if (isDark) accent.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.72f),
        )
    } else {
        Modifier
            .clip(circleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = shimmerAlpha),
                        Color.White.copy(alpha = 0.35f),
                        accent.copy(alpha = shimmerAlpha * 0.5f),
                    )
                ),
                shape = circleShape,
            )
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = pressScale * breatheScale
                scaleY = pressScale * breatheScale
                alpha = if (isPressed) 1f else 0.74f
            }
            .then(glassModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = SfIcons.AutoAwesome,
            contentDescription = "Playlist Maker",
            tint = if (!materialUi && !isDark) Color(0xFF1A1A1E) else accent,
            modifier = Modifier.size(18.dp),
        )
    }
}
