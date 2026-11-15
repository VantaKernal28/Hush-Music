package com.music.bitchord.ui.components.expressive

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.bitchord.data.model.ROW_ART_PX
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.artworkAt
import com.music.bitchord.data.model.isMatchMissing
import com.music.bitchord.data.model.isMatchPending
import com.music.bitchord.sharedui.resources.Res
import com.music.bitchord.sharedui.resources.now_playing
import com.music.bitchord.sharedui.resources.selected
import com.music.bitchord.ui.components.DownloadedBadge
import com.music.bitchord.ui.components.ExplicitSongTitle
import com.music.bitchord.ui.components.RowMoreButton
import com.music.bitchord.ui.components.SearchPlayingBars
import com.music.bitchord.ui.components.SourceQualityBadge
import com.music.bitchord.ui.components.sourceQualityBadge
import com.music.bitchord.ui.components.trackArtSize
import com.music.bitchord.ui.player.rememberRemoteArtworkUrl
import org.jetbrains.compose.resources.stringResource

/**
 * Material 3 Expressive (M3E) Morphing Track Row Component.
 *
 * Visual Craft & Motion:
 * - Inactive state: Rounded card with 16.dp corners, generous padding, and subtle tonal depth.
 * - Active / Playing state: Morphs using [ExpressiveMotion.CornerMorphSpring] into a full
 *   30.dp stadium pill, with a dynamic tonal fill, vibrant border, circular thumbnail morph,
 *   and a multi-bar bouncy animated equalizer indicator.
 * - Micro-interactions: Tactile squish and press damping via [expressiveSquish] with haptic feedback.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MorphingTrackItem(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: (() -> Unit)? = null,
    onMore: (() -> Unit)? = null,
    trackNumber: Int? = null,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    downloadedTint: Color? = MaterialTheme.colorScheme.primary,
    isCurrent: Boolean = false,
    isPlaying: Boolean = false,
    activeTint: Color = MaterialTheme.colorScheme.primary,
    searchPlayingStyle: Boolean = false,
    selected: Boolean = false,
    showSourceQuality: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 1. Morphing Card Corner Radius (16dp -> 30dp full stadium pill)
    val cardCornerRadius by animateDpAsState(
        targetValue = if (isCurrent) 30.dp else 16.dp,
        animationSpec = ExpressiveMotion.CornerMorphSpring,
        label = "cardCornerRadius",
    )

    // 2. Morphing Thumbnail Corner Radius (12dp squircle -> 26dp full circle)
    val thumbnailCornerRadius by animateDpAsState(
        targetValue = if (isCurrent) 26.dp else 12.dp,
        animationSpec = ExpressiveMotion.CornerMorphSpring,
        label = "thumbCornerRadius",
    )

    // 3. Tonal Fill & Border
    val containerColor by animateColorAsState(
        targetValue = when {
            selected -> activeTint.copy(alpha = 0.22f)
            isCurrent -> activeTint.copy(alpha = 0.16f)
            isPressed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
        },
        animationSpec = tween(durationMillis = 280),
        label = "containerColor",
    )

    val borderColor by animateColorAsState(
        targetValue = if (isCurrent) activeTint.copy(alpha = 0.38f) else Color.Transparent,
        animationSpec = tween(durationMillis = 280),
        label = "borderColor",
    )

    val titleColor by animateColorAsState(
        targetValue = if (isCurrent && !searchPlayingStyle) activeTint else MaterialTheme.colorScheme.onSurface,
        label = "titleColor",
    )

    val matching = song.isMatchPending
    val unavailable = song.isMatchMissing

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .expressiveSquish(isPressed = isPressed, pressScale = 0.97f)
            .clip(RoundedCornerShape(cardCornerRadius))
            .background(containerColor)
            .border(
                width = if (isCurrent) 1.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(cardCornerRadius),
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = onLongPress?.let {
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        it()
                    }
                },
            )
            .alpha(if (unavailable) 0.45f else 1f)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Track Number or Artwork
            if (trackNumber != null) {
                Box(
                    modifier = Modifier.size(50.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (searchPlayingStyle && isCurrent && isPlaying) {
                        SearchPlayingBars()
                    } else if (isCurrent) {
                        if (isPlaying) {
                            AnimatedEqualizerIndicator(tint = activeTint)
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = stringResource(Res.string.now_playing),
                                tint = activeTint,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    } else {
                        Text(
                            text = "$trackNumber",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = subtitleColor,
                            maxLines = 1,
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .trackArtSize(50.dp, song.isVideo)
                        .clip(RoundedCornerShape(thumbnailCornerRadius)),
                ) {
                    AsyncImage(
                        model = rememberRemoteArtworkUrl(song)?.artworkAt(ROW_ART_PX),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    if (searchPlayingStyle && isCurrent && isPlaying) {
                        SearchPlayingBars(Modifier.align(Alignment.Center))
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            // Title and Subtitle Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                ExplicitSongTitle(
                    song = song,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                    ),
                    color = titleColor,
                )
                Spacer(Modifier.height(3.dp))
                val subtitle = listOfNotNull(
                    song.artist.takeIf { it.isNotBlank() },
                    song.downloadFormat,
                ).joinToString(" · ")
                val quality = if (showSourceQuality) song.sourceQualityBadge() else null

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (quality != null) {
                        SourceQualityBadge(
                            quality = quality,
                            color = subtitleColor,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text(
                        text = if (quality != null && subtitle.isNotEmpty()) " · $subtitle" else subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Status Indicators & Equalizer
            if (downloadedTint != null) {
                DownloadedBadge(song.videoId, downloadedTint)
            }

            if (selected) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = stringResource(Res.string.selected),
                    tint = activeTint,
                    modifier = Modifier.size(20.dp),
                )
            }

            if (isCurrent && trackNumber == null && !searchPlayingStyle) {
                Spacer(Modifier.width(8.dp))
                if (isPlaying) {
                    AnimatedEqualizerIndicator(tint = activeTint)
                } else {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(Res.string.now_playing),
                        tint = activeTint,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            if (matching) {
                Spacer(Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = subtitleColor,
                )
            } else {
                song.durationText?.let {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = subtitleColor,
                    )
                }
            }

            if (trailing != null && !matching && !unavailable) {
                Spacer(Modifier.width(4.dp))
                trailing()
            }

            // More Menu Button with tactile tap
            if (onMore != null && !matching && !unavailable) {
                RowMoreButton(
                    onClick = onMore,
                    tint = subtitleColor,
                    modifier = Modifier.expressiveClickable(
                        pressScale = 0.88f,
                        onClick = onMore,
                    ),
                )
            }
        }
    }
}

/**
 * High-performance 3-bar animated equalizer for Material 3 Expressive.
 * Uses an infinite transition with staggered phases to avoid recompositions.
 */
@Composable
fun AnimatedEqualizerIndicator(
    tint: Color,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 18.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eqTransition")

    val bar1Phase by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 480, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eqBar1",
    )

    val bar2Phase by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 390, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eqBar2",
    )

    val bar3Phase by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eqBar3",
    )

    Canvas(modifier = modifier.size(sizeDp)) {
        val totalWidth = size.width
        val totalHeight = size.height
        val barCount = 3
        val barWidth = 3.dp.toPx()
        val spacing = (totalWidth - (barCount * barWidth)) / (barCount - 1).coerceAtLeast(1)
        val corner = CornerRadius(barWidth / 2f, barWidth / 2f)

        val heights = listOf(bar1Phase, bar2Phase, bar3Phase)
        heights.forEachIndexed { i, fraction ->
            val h = (totalHeight * fraction).coerceIn(barWidth, totalHeight)
            val left = i * (barWidth + spacing)
            val top = totalHeight - h
            drawRoundRect(
                color = tint,
                topLeft = Offset(left, top),
                size = Size(barWidth, h),
                cornerRadius = corner,
            )
        }
    }
}
