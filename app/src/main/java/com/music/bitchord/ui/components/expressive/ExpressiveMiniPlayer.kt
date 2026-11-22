package com.music.bitchord.ui.components.expressive

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.data.settings.AppSettings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.bitchord.R
import com.music.bitchord.data.model.ROW_ART_PX
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.artworkAt
import com.music.bitchord.playback.MusicOnlyPlayback
import com.music.bitchord.ui.components.BAR_GUTTER
import com.music.bitchord.ui.components.ExplicitSongTitle
import com.music.bitchord.ui.components.MiniPlayerNextGlyph
import com.music.bitchord.ui.components.MiniPlayerPull
import com.music.bitchord.ui.components.VideoThumbnails
import com.music.bitchord.ui.components.miniPlayerGestures
import com.music.bitchord.ui.components.playerDockArt
import com.music.bitchord.ui.components.rememberRemoteArtworkUrl
import com.music.bitchord.ui.components.thumbnailBorder
import com.music.bitchord.ui.haptics.Haptic
import com.music.bitchord.ui.haptics.rememberHaptics
import com.music.bitchord.ui.player.AndroidMusicVideo
import com.music.bitchord.ui.player.PlayerDock
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.hazeEffect

/**
 * Material 3 Expressive (M3E) Floating Mini Player.
 *
 * Visual & Motion Craft:
 * - Floating tactile stadium pill (50% corner radius) with generous internal padding.
 * - Prominent squircle primary Play/Pause action button (46.dp × 46.dp) with tactile spring squish.
 * - Breathing circular thumbnail that subtly scales to 1.0f when playing and settles to 0.94f on pause.
 * - Bold display title and artist hierarchy with high contrast M3E styling.
 * - Fluid swipe-to-skip and vertical pull-to-expand gesture integrations.
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun ExpressiveMiniPlayer(
    song: Song,
    isPlaying: Boolean,
    isLoading: Boolean,
    hazeState: HazeState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onExpand: () -> Unit,
    controlsLocked: Boolean = false,
    onBlockedControl: () -> Unit = {},
    dock: PlayerDock? = null,
    pull: MiniPlayerPull? = null,
    progressProvider: (() -> Float)? = null,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val haptics = rememberHaptics()
    val materialUi by AppSettings.materialUiMode.collectAsStateWithLifecycle()
    val shape = remember(materialUi) {
        if (materialUi) RoundedCornerShape(24.dp) else RoundedCornerShape(percent = 50)
    }

    val miniPlayerProgressLine by AppSettings.miniPlayerProgressLine.collectAsStateWithLifecycle()
    val rotatingScallopPlay by AppSettings.rotatingScallopPlay.collectAsStateWithLifecycle()

    // Rotating scalloped Play/Pause button for Material UI Mode
    val infiniteTransition = rememberInfiniteTransition(label = "m3ePlaySpin")
    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "playRotation",
    )
    val playRotation = if (materialUi && isPlaying && rotatingScallopPlay) animatedRotation else 0f
    val playButtonShape = remember(materialUi) {
        if (materialUi) ScallopedFlowerShape(lobes = 9, innerRadiusRatio = 0.85f) else SquircleShape(16.dp)
    }

    // Breathing artwork scale during playback
    val artworkScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.94f,
        animationSpec = ExpressiveMotion.SpatialSpring,
        label = "miniArtScale",
    )

    Box(
        modifier = modifier
            .padding(horizontal = BAR_GUTTER)
            .miniPlayerGestures(
                pull = pull,
                onNext = {
                    haptics.play(Haptic.SkipNext)
                    onNext()
                },
                onPrevious = {
                    haptics.play(Haptic.SkipPrevious)
                    onPrevious()
                },
                locked = controlsLocked,
                onBlocked = onBlockedControl,
            )
            .shadow(elevation = 12.dp, shape = shape, ambientColor = Color.Black.copy(alpha = 0.25f))
            .clip(shape)
            .then(
                if (materialUi) {
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            shape = shape,
                        )
                } else {
                    Modifier
                        .hazeEffect(
                            state = hazeState,
                            style = HazeMaterials.regular(MaterialTheme.colorScheme.surfaceContainerHigh),
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            shape = shape,
                        )
                },
            )
            .clickable(onClick = onExpand),
    ) {
        // Thin top progress line for Rhythm Material UI Mode
        if (materialUi && miniPlayerProgressLine) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
            ) {
                val progress = progressProvider?.invoke()?.coerceIn(0f, 1f) ?: 0f
                if (progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Artwork / Video Thumbnail
            val showsPicture = song.isVideo && MusicOnlyPlayback.showsPicture(song.videoId)
            val artWidth by animateDpAsState(
                targetValue = if (showsPicture && !VideoThumbnails.square) 72.dp else 46.dp,
                label = "miniArtWidth",
            )
            val artCornerRadius = if (showsPicture) 14.dp else 23.dp

            Box(
                modifier = Modifier
                    .size(width = artWidth, height = 46.dp)
                    .graphicsLayer {
                        scaleX = artworkScale
                        scaleY = artworkScale
                    }
                    .playerDockArt(dock, artCornerRadius)
                    .clip(RoundedCornerShape(artCornerRadius))
                    .thumbnailBorder(RoundedCornerShape(artCornerRadius))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                AsyncImage(
                    model = rememberRemoteArtworkUrl(song)?.artworkAt(ROW_ART_PX),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                if (showsPicture) {
                    AndroidMusicVideo(
                        videoId = song.videoId,
                        fit = false,
                        onRenderedChanged = {},
                        modifier = Modifier.fillMaxSize(),
                        priority = 0,
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Metadata: Bold Title and Artist
            Column(
                modifier = Modifier.weight(1f),
            ) {
                ExplicitSongTitle(
                    song = song,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    badgeColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    fadeOverflow = true,
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.width(8.dp))

            // Tactile Squircle Play / Pause Button
            if (isLoading) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(24.dp),
                    )
                }
            } else {
                val playInteraction = remember { MutableInteractionSource() }
                val isPlayPressed by playInteraction.collectIsPressedAsState()
                val playButtonScale by animateFloatAsState(
                    targetValue = if (isPlayPressed) 0.88f else 1.0f,
                    animationSpec = ExpressiveMotion.TactilePressSpring,
                    label = "playBtnScale",
                )

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .graphicsLayer {
                            scaleX = playButtonScale
                            scaleY = playButtonScale
                            rotationZ = playRotation
                        }
                        .clip(playButtonShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(
                            interactionSource = playInteraction,
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPlayPause()
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer { rotationZ = -playRotation },
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            // Tactile Next Track Button
            val nextInteraction = remember { MutableInteractionSource() }
            val isNextPressed by nextInteraction.collectIsPressedAsState()
            val nextScale by animateFloatAsState(
                targetValue = if (isNextPressed) 0.86f else 1.0f,
                animationSpec = ExpressiveMotion.TactilePressSpring,
                label = "nextBtnScale",
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = nextScale
                        scaleY = nextScale
                    }
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = nextInteraction,
                        indication = null,
                        enabled = !controlsLocked,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNext()
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                MiniPlayerNextGlyph(
                    tint = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = if (controlsLocked) 0.3f else 0.85f),
                )
            }
        }
    }
}
