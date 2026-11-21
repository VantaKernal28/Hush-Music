package com.music.bitchord.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.music.bitchord.data.model.Song
import com.music.bitchord.playback.LikeStatus
import com.music.bitchord.playback.PlaybackPosition
import com.music.bitchord.ui.components.expressive.ScallopedFlowerShape
import com.music.bitchord.ui.icons.BitChordIcons

/**
 * Material 3 Expressive Player Content reproducing cromaguy/Rhythm exactly (video_2.mp4 & video_3.mp4):
 * - Center-top large artwork clipped to 8-lobed ScallopedFlowerShape
 * - Centered title & artist below artwork (FontWeight.Black headlineMedium)
 * - Row 1: Pill Play/Pause button (64dp height, weight 1f) + circular Next button (64dp)
 * - Row 2: Circular Previous button (52dp) + seekbar with timestamps
 * - Row 3: 5 squircle buttons (Lyrics, Like/Star, Audio Output, Queue, More options)
 * - Dynamically tinted by the user's selected accent color.
 */
@Composable
fun RhythmPlayerContent(
    song: Song,
    isPlaying: Boolean,
    isLoading: Boolean,
    position: PlaybackPosition,
    durationMs: Long,
    likeStatus: LikeStatus,
    onToggleLike: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekFraction: (Float) -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenAudioOutput: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryAccent = MaterialTheme.colorScheme.primary
    val isAccentLight = primaryAccent.luminance() > 0.45f
    val accentBg = primaryAccent
    val accentFg = if (isAccentLight) Color(0xFF0D281E) else Color.White
    val buttonDarkContainer = if (isAccentLight) Color(0xFF092015) else Color.White
    val buttonDarkContent = if (isAccentLight) accentBg else Color(0xFF092015)
    val controlGlass = accentFg.copy(alpha = 0.22f)
    val dockGlass = accentFg.copy(alpha = 0.16f)

    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }

    val currentProgress = if (isScrubbing) {
        scrubProgress
    } else {
        if (durationMs > 0L) (position.positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        val availableHeight = maxHeight
        val isCompactHeight = availableHeight < 680.dp
        val artSize = (maxWidth * 0.76f).coerceIn(200.dp, if (isCompactHeight) 240.dp else 310.dp)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Dismiss Handle Bar
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(38.dp)
                    .height(4.5.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(accentFg.copy(alpha = 0.35f)),
            )

            // Center-Top Artwork: Clipped to 8-lobed ScallopedFlowerShape
            Box(
                modifier = Modifier
                    .size(artSize)
                    .aspectRatio(1f)
                    .clip(ScallopedFlowerShape(lobes = 8, innerRadiusRatio = 0.82f))
                    .background(accentFg.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Track Title & Artist
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = if (isCompactHeight) 22.sp else 26.sp,
                    ),
                    color = accentFg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = if (isCompactHeight) 15.sp else 17.sp,
                    ),
                    color = accentFg.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }

            // Transport Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 10.dp else 16.dp),
            ) {
                // Row 1: Wide Pill Play/Pause button + circular Next button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isCompactHeight) 56.dp else 64.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(buttonDarkContainer)
                            .clickable(onClick = onPlayPause),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isPlaying) "Pause" else "Play",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isCompactHeight) 19.sp else 22.sp,
                            ),
                            color = buttonDarkContent,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(if (isCompactHeight) 56.dp else 64.dp)
                            .clip(CircleShape)
                            .background(controlGlass)
                            .clickable(onClick = onNext),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next",
                            tint = accentFg,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                // Row 2: Circular Previous button + styled seekbar with timestamps
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompactHeight) 46.dp else 52.dp)
                            .clip(CircleShape)
                            .background(controlGlass)
                            .clickable(onClick = onPrevious),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous",
                            tint = accentFg,
                            modifier = Modifier.size(26.dp),
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Slider(
                            value = currentProgress,
                            onValueChange = {
                                isScrubbing = true
                                scrubProgress = it
                            },
                            onValueChangeFinished = {
                                onSeekFraction(scrubProgress)
                                isScrubbing = false
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = accentFg,
                                activeTrackColor = accentFg,
                                inactiveTrackColor = accentFg.copy(alpha = 0.22f),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val currentMs = if (isScrubbing) (scrubProgress * durationMs).toLong() else position.positionMs
                            Text(
                                text = formatDuration(currentMs),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = accentFg.copy(alpha = 0.72f),
                            )
                            Text(
                                text = formatDuration(durationMs),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = accentFg.copy(alpha = 0.72f),
                            )
                        }
                    }
                }

                // Row 3 (Bottom Dock): 5 rounded-square buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isCompactHeight) 12.dp else 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 1. Synced Lyrics
                    RhythmDockButton(
                        icon = BitChordIcons.LyricsQuote,
                        contentDescription = "Lyrics",
                        containerColor = dockGlass,
                        contentColor = accentFg,
                        onClick = onOpenLyrics,
                        modifier = Modifier.weight(1f),
                    )

                    // 2. Like
                    val liked = likeStatus == LikeStatus.LIKE
                    RhythmDockButton(
                        icon = if (liked) BitChordIcons.StarFilled else BitChordIcons.Star,
                        contentDescription = if (liked) "Unlike" else "Like",
                        containerColor = dockGlass,
                        contentColor = if (liked) accentFg else accentFg.copy(alpha = 0.7f),
                        onClick = onToggleLike,
                        modifier = Modifier.weight(1f),
                    )

                    // 3. Audio Output
                    RhythmDockButton(
                        icon = Icons.Rounded.Speaker,
                        contentDescription = "Audio Output",
                        containerColor = dockGlass,
                        contentColor = accentFg,
                        onClick = onOpenAudioOutput,
                        modifier = Modifier.weight(1f),
                    )

                    // 4. Queue
                    RhythmDockButton(
                        icon = BitChordIcons.Queue,
                        contentDescription = "Queue",
                        containerColor = dockGlass,
                        contentColor = accentFg,
                        onClick = onOpenQueue,
                        modifier = Modifier.weight(1f),
                    )

                    // 5. More Options
                    RhythmDockButton(
                        icon = Icons.Rounded.MoreVert,
                        contentDescription = "More",
                        containerColor = dockGlass,
                        contentColor = accentFg,
                        onClick = onOpenMenu,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RhythmDockButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(22.dp),
        )
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSeconds = ms / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
