package com.music.bitchord.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.music.bitchord.ui.player.PlayerBackHandler
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import kotlinx.coroutines.launch

/**
 * Centered floating modal card with a blurred frosted backdrop matching Hush's
 * Apple-inspired M3E card language:
 * - Centered horizontally and vertically with generous breathing room (never anchored to the bottom)
 * - Blurred + dimmed scrim over underlying app content
 * - Liquid spring entrance & circular `X` close affordance at top-start
 */
@Composable
fun HushCenteredCard(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    reduceBlur: Boolean = false,
    title: String? = null,
    subtitle: String? = null,
    maxWidth: Dp = 368.dp,
    maxHeight: Dp = 548.dp,
    cornerShape: Shape = ContinuousCornerShape(30.dp),
    dismissOnBackdropClick: Boolean = true,
    showCloseButton: Boolean = true,
    headerEnd: (@Composable RowScope.() -> Unit)? = null,
    headerCenter: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    PlayerBackHandler(enabled = true, onBack = onDismiss)

    val darkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val scale = remember { Animatable(0.88f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.76f,
                    stiffness = 420f,
                ),
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 190),
            )
        }
    }

    val scrimColor = if (darkTheme) {
        Color.Black.copy(alpha = 0.58f)
    } else {
        Color(0xFF0E1117).copy(alpha = 0.38f)
    }

    // Single-pass Haze blur on the card itself (with fast scrim behind) avoids
    // double fullscreen GPU blur passes that drop frames during spring openings.
    val backdropBlurModifier = Modifier.background(scrimColor)

    val cardBaseColor = if (darkTheme) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }

    val cardSurfaceModifier = if (hazeState != null && !reduceBlur) {
        val frostAlpha = if (darkTheme) 0.88f else 0.92f
        Modifier.optimizedHazeEffect(
            state = hazeState,
            style = HazeStyle(
                backgroundColor = cardBaseColor,
                tints = listOf(HazeTint(cardBaseColor.copy(alpha = frostAlpha))),
                blurRadius = 22.dp,
                noiseFactor = 0f,
                fallbackTint = HazeTint(cardBaseColor.copy(alpha = 0.96f)),
            ),
        )
    } else {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    cardBaseColor.copy(alpha = 0.98f),
                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.98f),
                ),
            ),
        )
    }

    val rimColor = if (darkTheme) {
        Color.White.copy(alpha = 0.14f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    val closeChipFill = if (darkTheme) {
        Color.White.copy(alpha = 0.11f)
    } else {
        Color.Black.copy(alpha = 0.07f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(50f)
            .graphicsLayer { this.alpha = alpha.value }
            .then(backdropBlurModifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                if (dismissOnBackdropClick) onDismiss()
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                }
                .shadow(
                    elevation = 28.dp,
                    shape = cornerShape,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.35f),
                )
                .clip(cornerShape)
                .then(cardSurfaceModifier)
                .border(width = 1.dp, color = rimColor, shape = cornerShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            if (showCloseButton || title != null || headerCenter != null || headerEnd != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (showCloseButton) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(closeChipFill)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else {
                        Spacer(Modifier.size(34.dp))
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            headerCenter != null -> headerCenter()
                            title != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                                if (!subtitle.isNullOrBlank()) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }

                    if (headerEnd != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            content = headerEnd,
                        )
                    } else {
                        Spacer(Modifier.size(34.dp))
                    }
                }
            }

            content()
        }
    }
}

/**
 * Convenience animated wrapper when visibility is controlled as a Boolean by the caller.
 */
@Composable
fun HushCenteredCardHost(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    reduceBlur: Boolean = false,
    title: String? = null,
    subtitle: String? = null,
    maxWidth: Dp = 368.dp,
    maxHeight: Dp = 548.dp,
    cornerShape: Shape = ContinuousCornerShape(30.dp),
    dismissOnBackdropClick: Boolean = true,
    showCloseButton: Boolean = true,
    headerEnd: (@Composable RowScope.() -> Unit)? = null,
    headerCenter: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(130)),
    ) {
        HushCenteredCard(
            onDismiss = onDismiss,
            hazeState = hazeState,
            reduceBlur = reduceBlur,
            title = title,
            subtitle = subtitle,
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            cornerShape = cornerShape,
            dismissOnBackdropClick = dismissOnBackdropClick,
            showCloseButton = showCloseButton,
            headerEnd = headerEnd,
            headerCenter = headerCenter,
            content = content,
        )
    }
}
