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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.ui.icons.SfIcons

/**
 * Sleek, compact animated pill button in the top-left corner explicitly displaying "AI".
 * Features a subtle sparkle/accent ring, smooth breathing/shimmer animation,
 * and spring press feedback that opens the AI Playlist Maker.
 */
@Composable
fun AiPlaylistPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ai_breathe_scale",
    )

    val pillShape = RoundedCornerShape(percent = 50)
    val accent = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale * breatheScale
                scaleY = pressScale * breatheScale
            }
            .clip(pillShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f))
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = shimmerAlpha),
                        Color.White.copy(alpha = 0.35f),
                        accent.copy(alpha = shimmerAlpha * 0.5f),
                    )
                ),
                shape = pillShape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = SfIcons.AutoAwesome,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = "AI",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
