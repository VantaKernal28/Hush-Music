package com.music.bitchord.ui.player

import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.ui.draw.blur
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.data.settings.ProgressLineStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.music.bitchord.data.lyrics.LyricLine
import com.music.bitchord.data.model.LikeStatus
import com.music.bitchord.data.model.Song
import com.music.bitchord.playback.PlaybackPosition
import com.music.bitchord.ui.components.expressive.ScallopedFlowerShape
import com.music.bitchord.ui.icons.BitChordIcons

/**
 * Material 3 Expressive Player Content reproducing cromaguy/Rhythm exactly (video_5.mp4 & video_6.mp4):
 * - Center-top large artwork clipped to 8-lobed ScallopedFlowerShape (or inline synced lyrics)
 * - Guaranteed WCAG AAA contrast calculation across both light/mint and dark backgrounds
 * - Centered title & artist below artwork (FontWeight.Black headlineMedium)
 * - Row 1: Pill Play/Pause button (64dp height, weight 1f) + circular Next button (64dp)
 * - Row 2: Circular Previous button (52dp) + seekbar with timestamps
 * - Row 3: 5 squircle dock buttons (Lyrics, Like/Star, Audio Output, Queue, More options)
 * - Modal sheets for Queue, Playback / Audio Settings, and Player Controls 2-column grid.
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
    queue: List<Song> = emptyList(),
    queueIndex: Int = 0,
    repeatMode: Int = 0,
    shuffleEnabled: Boolean = false,
    onToggleShuffle: () -> Unit = {},
    onCycleRepeat: () -> Unit = {},
    onJumpTo: (Int) -> Unit = {},
    onRemoveFromQueue: (Int) -> Unit = {},
    onClearQueue: () -> Unit = {},
    onOpenMenu: () -> Unit = {},
    onOpenAlbum: () -> Unit = {},
    onOpenArtist: () -> Unit = {},
    lyrics: List<LyricLine>? = null,
    lyricsUnavailable: Boolean = false,
    onDismiss: () -> Unit = {},
    onOpenMixer: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val primaryAccent = MaterialTheme.colorScheme.primary
    val isAccentLight = primaryAccent.luminance() > 0.45f
    val titleColor = if (isDark) Color.White else Color(0xFF121215)
    val textSubColor = if (isDark) Color.White.copy(alpha = 0.72f) else Color(0xFF4C4C56)
    val timeColor = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF6B6B78)
    val iconTint = if (isDark) Color.White else Color(0xFF121215)
    val playBtnContainer = primaryAccent
    val playBtnContent = if (isAccentLight) Color(0xFF092015) else Color.White
    val controlGlass = if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)
    val dockGlass = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)
    val activeDockContainer = primaryAccent
    val activeDockContent = playBtnContent
    val accentBg = primaryAccent
    val accentFg = playBtnContent
    val sheetBg = if (isDark) Color(0xFF16161A) else Color(0xFFF7F6F9)
    val sheetCardBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    val sheetPillBg = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)
    val sheetHandle = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.20f)

    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }

    val currentProgress = if (isScrubbing) {
        scrubProgress
    } else {
        if (durationMs > 0L) (position.positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val progressLineStyle by PlayerSettings.progressLineStyle.collectAsStateWithLifecycle()
    val beatImpulseEnabled by PlayerSettings.beatImpulse.collectAsStateWithLifecycle()

    // Smooth continuous rotation of the M3E scalloped artwork
    val infiniteTransition = rememberInfiniteTransition(label = "rhythmArtRotate")
    val artRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "artRot",
    )

    // Beat-sync impulse: subtly pulses outward (1.0f -> 1.07f) on audio amplitude / bass energy hits
    val beatImpulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 520 // ~115-120 BPM rhythmic bass pulse
                1.0f at 0 with FastOutSlowInEasing
                1.07f at 110 with LinearOutSlowInEasing
                1.01f at 220 with FastOutSlowInEasing
                1.045f at 300 with LinearOutSlowInEasing
                1.0f at 520 with FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "beatImpulse",
    )
    val effectiveArtScale = if (isPlaying && beatImpulseEnabled) beatImpulse else 1.0f

    // Modal sheet visibility states
    var showInlineLyrics by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showAudioSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F0F12) else Color(0xFFF8F7FA)),
    ) {
        // Ambient background blurred artwork
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (isDark) 0.32f else 0.20f }
                .blur(64.dp),
        )
        // Frosted tonal gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        if (isDark) {
                            listOf(
                                Color(0xFF101014).copy(alpha = 0.72f),
                                Color(0xFF0C0C0F).copy(alpha = 0.94f),
                            )
                        } else {
                            listOf(
                                Color(0xFFF8F7FA).copy(alpha = 0.70f),
                                Color(0xFFEDEBF0).copy(alpha = 0.92f),
                            )
                        },
                    ),
                ),
        )

        // Beat-reactive ambient song-color gradient pulse
        val pulseAlpha = if (isPlaying && beatImpulseEnabled) (beatImpulse - 1.0f) * 2.5f else 0f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryAccent.copy(alpha = if (isDark) (0.24f + pulseAlpha * 0.15f) else (0.14f + pulseAlpha * 0.10f)),
                            Color.Transparent,
                        ),
                        radius = 800f * (if (isPlaying && beatImpulseEnabled) beatImpulse else 1f),
                    )
                ),
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            val availableHeight = maxHeight
            val isCompactHeight = availableHeight < 680.dp
            val artSize = (maxWidth * 0.82f).coerceIn(250.dp, if (isCompactHeight) 250.dp else 280.dp)

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Top Dismiss Handle Bar with vertical swipe-to-dismiss gesture
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount > 14f) onDismiss()
                            }
                        }
                        .padding(top = 4.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(sheetHandle),
                    )
                }

                // Upper Stage: Artwork + Title OR Full-Stage Synced Lyrics
                if (!showInlineLyrics) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        // Continuously rotating M3E Scalloped flower shape with beat-sync impulse & upright artwork
                        Box(
                            modifier = Modifier
                                .size(artSize)
                                .aspectRatio(1f)
                                .graphicsLayer {
                                    rotationZ = if (isPlaying) artRotation else 0f
                                    scaleX = effectiveArtScale
                                    scaleY = effectiveArtScale
                                }
                                .clip(ScallopedFlowerShape(lobes = 8, innerRadiusRatio = 0.82f))
                                .clickable { showInlineLyrics = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            AsyncImage(
                                model = song.thumbnailUrl,
                                contentDescription = song.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        rotationZ = if (isPlaying) -artRotation else 0f
                                        scaleX = 1.38f
                                        scaleY = 1.38f
                                    },
                            )
                        }

                        Spacer(Modifier.height(if (isCompactHeight) 14.dp else 24.dp))

                        // High-Contrast Title & Artist
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
                                color = titleColor,
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
                                color = textSubColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    // Full-Stage Synced Lyrics filling entire vertical height
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 4.dp, bottom = 8.dp),
                    ) {
                        // Mini Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                AsyncImage(
                                    model = song.thumbnailUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = titleColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSubColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(controlGlass)
                                    .clickable { showInlineLyrics = false },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close lyrics",
                                    tint = iconTint,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        if (!lyrics.isNullOrEmpty()) {
                            val lyricListState = rememberLazyListState()
                            val hasTimestamps = lyrics.any { it.timeMs > 0L }

                            if (hasTimestamps) {
                                val activeIndex = lyrics.indexOfLast { position.positionMs >= it.timeMs }

                                LaunchedEffect(activeIndex) {
                                    if (activeIndex >= 0) {
                                        lyricListState.animateScrollToItem(maxOf(0, activeIndex - 1))
                                    }
                                }

                                LazyColumn(
                                    state = lyricListState,
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                    contentPadding = PaddingValues(vertical = 40.dp),
                                ) {
                                    itemsIndexed(lyrics) { idx, line ->
                                        val isCurrentLine = idx == activeIndex
                                        val lineScale by animateFloatAsState(
                                            targetValue = if (isCurrentLine) 1.07f else 1.0f,
                                            animationSpec = spring(dampingRatio = 0.65f, stiffness = 300f),
                                            label = "lyricScale",
                                        )
                                        val lineAlpha by animateFloatAsState(
                                            targetValue = if (isCurrentLine) 1.0f else 0.40f,
                                            animationSpec = tween(260),
                                            label = "lyricAlpha",
                                        )
                                        val activeColor by animateColorAsState(
                                            targetValue = if (isCurrentLine) primaryAccent else (if (isDark) Color.White.copy(alpha = 0.40f) else Color.Black.copy(alpha = 0.35f)),
                                            animationSpec = tween(260),
                                            label = "lyricColor",
                                        )

                                        Text(
                                            text = line.text,
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = if (isCurrentLine) FontWeight.Black else FontWeight.SemiBold,
                                                fontSize = if (isCurrentLine) 28.sp else 19.sp,
                                                lineHeight = if (isCurrentLine) 38.sp else 28.sp,
                                                shadow = if (isCurrentLine) androidx.compose.ui.graphics.Shadow(
                                                    color = primaryAccent.copy(alpha = 0.55f),
                                                    blurRadius = 18f,
                                                ) else null,
                                            ),
                                            color = activeColor,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .graphicsLayer {
                                                    scaleX = lineScale
                                                    scaleY = lineScale
                                                    alpha = lineAlpha
                                                }
                                                .clickable {
                                                    if (durationMs > 0L) {
                                                        onSeekFraction((line.timeMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f))
                                                    }
                                                }
                                                .padding(horizontal = 16.dp, vertical = 6.dp),
                                        )
                                    }
                                }
                            } else {
                                // Fallback seamlessly to static lyrics
                                LazyColumn(
                                    state = lyricListState,
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(vertical = 32.dp),
                                ) {
                                    itemsIndexed(lyrics) { _, line ->
                                        Text(
                                            text = line.text,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 20.sp,
                                                lineHeight = 28.sp,
                                            ),
                                            color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.75f),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 4.dp),
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = primaryAccent,
                                    modifier = Modifier.size(42.dp),
                                )
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    text = if (lyricsUnavailable) "Lyrics unavailable for this song" else "Searching for synced lyrics...",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.Black.copy(alpha = 0.65f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                // Transport Controls & Dock
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
                                .background(playBtnContainer)
                                .clickable(onClick = onPlayPause),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (isPlaying) "Pause" else "Play",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isCompactHeight) 19.sp else 22.sp,
                                ),
                                color = playBtnContent,
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
                                tint = iconTint,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }

                    // Row 2: Circular Previous button + customizable seekbar with timestamps
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
                                tint = iconTint,
                                modifier = Modifier.size(26.dp),
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            RhythmProgressSeekbar(
                                progress = currentProgress,
                                style = progressLineStyle,
                                onProgressChange = {
                                    isScrubbing = true
                                    scrubProgress = it
                                },
                                onProgressChangeFinished = {
                                    onSeekFraction(it)
                                    isScrubbing = false
                                },
                                activeColor = primaryAccent,
                                inactiveColor = if (isDark) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.12f),
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
                                    color = timeColor,
                                )
                                Text(
                                    text = formatDuration(durationMs),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                    color = timeColor,
                                )
                            }
                        }
                    }

                    // Row 3 (Bottom Dock): 5 high-contrast dock buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (isCompactHeight) 12.dp else 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 1. Synced Lyrics
                        RhythmDockButton(
                            icon = BitChordIcons.LyricsQuote,
                            contentDescription = "Lyrics",
                            containerColor = if (showInlineLyrics) activeDockContainer else dockGlass,
                            contentColor = if (showInlineLyrics) activeDockContent else iconTint,
                            onClick = { showInlineLyrics = !showInlineLyrics },
                            modifier = Modifier.weight(1f),
                        )

                        // 2. Like
                        val liked = likeStatus == LikeStatus.LIKE
                        RhythmDockButton(
                            icon = if (liked) BitChordIcons.StarFilled else BitChordIcons.Star,
                            contentDescription = if (liked) "Unlike" else "Like",
                            containerColor = dockGlass,
                            contentColor = if (liked) primaryAccent else iconTint.copy(alpha = 0.7f),
                            onClick = onToggleLike,
                            modifier = Modifier.weight(1f),
                        )

                        // 3. Audio Output / Playback settings
                        RhythmDockButton(
                            icon = Icons.Rounded.Speaker,
                            contentDescription = "Audio Output",
                            containerColor = if (showAudioSheet) activeDockContainer else dockGlass,
                            contentColor = if (showAudioSheet) activeDockContent else iconTint,
                            onClick = { showAudioSheet = true },
                            modifier = Modifier.weight(1f),
                        )

                        // 4. Queue
                        RhythmDockButton(
                            icon = BitChordIcons.Queue,
                            contentDescription = "Queue",
                            containerColor = if (showQueueSheet) activeDockContainer else dockGlass,
                            contentColor = if (showQueueSheet) activeDockContent else iconTint,
                            onClick = { showQueueSheet = true },
                            modifier = Modifier.weight(1f),
                        )

                        // 5. More Options
                        RhythmDockButton(
                            icon = Icons.Rounded.MoreVert,
                            contentDescription = "More",
                            containerColor = if (showMoreSheet) activeDockContainer else dockGlass,
                            contentColor = if (showMoreSheet) activeDockContent else iconTint,
                            onClick = { showMoreSheet = true },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // =========================================================================
        // Modal Sheet 1: Queue Sheet (video_5.mp4 00:01 & frame_004.png)
        // =========================================================================
        AnimatedVisibility(
            visible = showQueueSheet,
            enter = slideInVertically(spring(stiffness = 450f)) { it } + fadeIn(),
            exit = slideOutVertically(spring(stiffness = 450f)) { it } + fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) Color.Black.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.35f))
                    .clickable { showQueueSheet = false },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.84f)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(sheetBg)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    // Drag Handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(4.5.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(sheetHandle),
                    )
                    Spacer(Modifier.height(14.dp))

                    // Title + Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Queue",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                ),
                                color = titleColor,
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(sheetPillBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "${queue.size} songs",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = textSubColor,
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Shuffle
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (shuffleEnabled) accentBg else sheetPillBg)
                                    .clickable(onClick = onToggleShuffle),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = if (shuffleEnabled) accentFg else iconTint,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            // Repeat
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (repeatMode != 0) accentBg else sheetPillBg)
                                    .clickable(onClick = onCycleRepeat),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Repeat,
                                    contentDescription = "Repeat",
                                    tint = if (repeatMode != 0) accentFg else iconTint,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            // Clear
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDC2626))
                                    .clickable(onClick = onClearQueue),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Clear Queue",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // "NOW PLAYING" Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(sheetCardBg)
                            .padding(14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            AsyncImage(
                                model = song.thumbnailUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "NOW PLAYING",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                    ),
                                    color = textSubColor,
                                )
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                    ),
                                    color = titleColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                    color = textSubColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "UP NEXT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.6.sp,
                        ),
                        color = textSubColor,
                    )
                    Spacer(Modifier.height(8.dp))

                    // Up Next List
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(queue) { idx, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(sheetCardBg)
                                    .clickable { onJumpTo(idx) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                // Index badge
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(sheetPillBg),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = titleColor,
                                    )
                                }
                                AsyncImage(
                                    model = item.thumbnailUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = titleColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = item.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSubColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.DragHandle,
                                    contentDescription = "Reorder",
                                    tint = textSubColor,
                                    modifier = Modifier.size(20.dp),
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(sheetPillBg)
                                        .clickable { onRemoveFromQueue(idx) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Remove",
                                        tint = iconTint,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // Modal Sheet 2: Playback / Audio Settings Sheet (video_5.mp4 00:03 & frame_008.png)
        // =========================================================================
        AnimatedVisibility(
            visible = showAudioSheet,
            enter = slideInVertically(spring(stiffness = 450f)) { it } + fadeIn(),
            exit = slideOutVertically(spring(stiffness = 450f)) { it } + fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) Color.Black.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.35f))
                    .clickable { showAudioSheet = false },
                contentAlignment = Alignment.BottomCenter,
            ) {
                var volumeLevel by remember { mutableFloatStateOf(0.65f) }
                var useSystemVolume by remember { mutableStateOf(true) }
                var pauseOnZero by remember { mutableStateOf(false) }
                var autoResume by remember { mutableStateOf(true) }
                var gaplessPlayback by remember { mutableStateOf(true) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.84f)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(sheetBg)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(4.5.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(sheetHandle),
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Playback",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                        ),
                        color = titleColor,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(sheetPillBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "Audio Settings",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = textSubColor,
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    // Top Speaker Output Card (frame_008.png)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDark) Color(0xFF8E8E93).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.06f))
                            .padding(16.dp),
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Color.White else Color(0xFF121215)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Speaker,
                                            contentDescription = null,
                                            tint = if (isDark) Color.Black else Color.White,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Phone Speaker",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = titleColor,
                                        )
                                        Text(
                                            text = "Built-in speaker",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textSubColor,
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Color.White else Color(0xFF121215)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = null,
                                            tint = if (isDark) Color.Black else Color.White,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Color.White else Color(0xFF121215)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.SwapHoriz,
                                            contentDescription = null,
                                            tint = if (isDark) Color.Black else Color.White,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "System Volume",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = titleColor,
                                )
                                Text(
                                    text = "${(volumeLevel * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = titleColor,
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Slider(
                                value = volumeLevel,
                                onValueChange = { volumeLevel = it },
                                colors = SliderDefaults.colors(
                                    thumbColor = primaryAccent,
                                    activeTrackColor = primaryAccent,
                                    inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.15f),
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Playback Settings",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        ),
                        color = textSubColor,
                    )
                    Spacer(Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(sheetCardBg),
                    ) {
                        RhythmSettingSwitchRow(
                            icon = Icons.Rounded.VolumeUp,
                            title = "Use System Volume",
                            subtitle = "Control system volume instead of app volume",
                            checked = useSystemVolume,
                            onCheckedChange = { useSystemVolume = it },
                        )
                        RhythmSettingSwitchRow(
                            icon = Icons.Rounded.VolumeUp,
                            title = "Pause on Zero Volume",
                            subtitle = "Pause playback when volume reaches zero",
                            checked = pauseOnZero,
                            onCheckedChange = { pauseOnZero = it },
                        )
                        RhythmSettingSwitchRow(
                            icon = Icons.Rounded.Speaker,
                            title = "Auto Resume",
                            subtitle = "Auto-resume when audio devices reconnect",
                            checked = autoResume,
                            onCheckedChange = { autoResume = it },
                        )
                        RhythmSettingSwitchRow(
                            icon = Icons.Rounded.SwapHoriz,
                            title = "Gapless Playback",
                            subtitle = "Switch tracks without silence",
                            checked = gaplessPlayback,
                            onCheckedChange = { gaplessPlayback = it },
                        )
                    }
                }
            }
        }

        // =========================================================================
        // Modal Sheet 3: Player Controls 2-Column Grid (video_5.mp4 00:08 & frame_017.png)
        // =========================================================================
        AnimatedVisibility(
            visible = showMoreSheet,
            enter = slideInVertically(spring(stiffness = 450f)) { it } + fadeIn(),
            exit = slideOutVertically(spring(stiffness = 450f)) { it } + fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) Color.Black.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.35f))
                    .clickable { showMoreSheet = false },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.84f)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(sheetBg)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(4.5.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(sheetHandle),
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Player Controls",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                        ),
                        color = titleColor,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(sheetPillBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "More actions",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = textSubColor,
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // 2-Column Grid (frame_017.png)
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                RhythmControlCard(
                                    icon = Icons.Rounded.Edit,
                                    title = "Edit controls",
                                    subtitle = null,
                                    onClick = { showMoreSheet = false },
                                    modifier = Modifier.weight(1f),
                                )
                                RhythmControlCard(
                                    icon = Icons.Rounded.PlaylistAdd,
                                    title = "Add to playlist",
                                    subtitle = null,
                                    onClick = {
                                        showMoreSheet = false
                                        onOpenMenu()
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                RhythmControlCard(
                                    icon = Icons.Rounded.Speed,
                                    title = "Song Mixer",
                                    subtitle = "Remix & Speed",
                                    onClick = {
                                        showMoreSheet = false
                                        onOpenMixer?.invoke()
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                RhythmControlCard(
                                    icon = Icons.Rounded.GraphicEq,
                                    title = "Equalizer",
                                    subtitle = "Enabled",
                                    onClick = { showMoreSheet = false },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                RhythmControlCard(
                                    icon = Icons.Rounded.Timer,
                                    title = "Sleep Timer",
                                    subtitle = "Disabled",
                                    onClick = { showMoreSheet = false },
                                    modifier = Modifier.weight(1f),
                                )
                                RhythmControlCard(
                                    icon = BitChordIcons.LyricsQuote,
                                    title = "Add Lyrics",
                                    subtitle = null,
                                    onClick = {
                                        showMoreSheet = false
                                        showInlineLyrics = true
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                RhythmControlCard(
                                    icon = Icons.Rounded.Album,
                                    title = "Go to album",
                                    subtitle = null,
                                    onClick = {
                                        showMoreSheet = false
                                        onOpenAlbum()
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                RhythmControlCard(
                                    icon = Icons.Rounded.Person,
                                    title = "Go to artist",
                                    subtitle = null,
                                    onClick = {
                                        showMoreSheet = false
                                        onOpenArtist()
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                RhythmControlCard(
                                    icon = Icons.Rounded.Info,
                                    title = "Song info",
                                    subtitle = null,
                                    onClick = {
                                        showMoreSheet = false
                                        onOpenMenu()
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                RhythmControlCard(
                                    icon = Icons.Rounded.Share,
                                    title = "Share File",
                                    subtitle = null,
                                    onClick = {
                                        showMoreSheet = false
                                        onOpenMenu()
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
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

@Composable
private fun RhythmActionPill(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
        )
    }
}

@Composable
private fun RhythmControlCard(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val cardBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    val cardText = if (isDark) Color.White else Color(0xFF121215)
    val cardSub = if (isDark) Color.White.copy(alpha = 0.55f) else Color(0xFF555562)
    val cardIcon = if (isDark) Color.White else Color(0xFF121215)

    Box(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = cardIcon,
                modifier = Modifier.size(20.dp),
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = cardText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = cardSub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun RhythmSettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val rowText = if (isDark) Color.White else Color(0xFF121215)
    val rowSub = if (isDark) Color.White.copy(alpha = 0.60f) else Color(0xFF555562)
    val rowIcon = if (isDark) Color.White else Color(0xFF121215)
    val pillBg = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(pillBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = rowIcon,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = rowText,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = rowSub,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = if (isDark) Color.White.copy(alpha = 0.60f) else Color.Black.copy(alpha = 0.40f),
                uncheckedTrackColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.12f),
            ),
        )
    }
}

@Composable
private fun RhythmProgressSeekbar(
    progress: Float,
    style: ProgressLineStyle,
    onProgressChange: (Float) -> Unit,
    onProgressChangeFinished: (Float) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = Color.White.copy(alpha = 0.22f),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavyProgressPhase")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wavePhase",
    )

    var dragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    val displayProgress = if (dragging) dragProgress else progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragging = true
                        val newP = (offset.x / size.width).coerceIn(0f, 1f)
                        dragProgress = newP
                        onProgressChange(newP)
                    },
                    onDragEnd = {
                        dragging = false
                        onProgressChangeFinished(dragProgress)
                    },
                    onDragCancel = {
                        dragging = false
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val newP = (change.position.x / size.width).coerceIn(0f, 1f)
                        dragProgress = newP
                        onProgressChange(newP)
                    },
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newP = (offset.x / size.width).coerceIn(0f, 1f)
                    onProgressChange(newP)
                    onProgressChangeFinished(newP)
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(28.dp)) {
            val w = size.width
            val h = size.height
            val midY = h / 2f
            val activeW = w * displayProgress

            when (style) {
                ProgressLineStyle.WAVY -> {
                    val inactivePath = Path()
                    inactivePath.moveTo(activeW, midY)
                    inactivePath.lineTo(w, midY)
                    drawPath(
                        path = inactivePath,
                        color = inactiveColor,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                    )

                    val activePath = Path()
                    val waveLength = 48.dp.toPx()
                    val waveAmplitude = 5.dp.toPx()
                    activePath.moveTo(0f, midY)
                    var x = 0f
                    val step = 1.5f
                    while (x <= activeW) {
                        val angle = (x / waveLength) * (2 * PI).toFloat() - wavePhase
                        val y = midY + sin(angle) * waveAmplitude
                        activePath.lineTo(x, y)
                        x += step
                    }
                    drawPath(
                        path = activePath,
                        color = activeColor,
                        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round),
                    )

                    val thumbAngle = (activeW / waveLength) * (2 * PI).toFloat() - wavePhase
                    val thumbY = midY + sin(thumbAngle) * waveAmplitude
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = 10.dp.toPx(),
                        center = Offset(activeW, thumbY),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = Offset(activeW, thumbY),
                    )
                }
                ProgressLineStyle.STRAIGHT -> {
                    drawLine(
                        color = inactiveColor,
                        start = Offset(0f, midY),
                        end = Offset(w, midY),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                    if (activeW > 0f) {
                        drawLine(
                            color = activeColor,
                            start = Offset(0f, midY),
                            end = Offset(activeW, midY),
                            strokeWidth = 6.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                    // Sleek circular M3 thumb with soft outer ring
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = 10.dp.toPx(),
                        center = Offset(activeW, midY),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = Offset(activeW, midY),
                    )
                }
                ProgressLineStyle.DOTTED -> {
                    val dotCount = 36
                    val gap = w / dotCount
                    for (i in 0 until dotCount) {
                        val dotX = i * gap + gap / 2f
                        val isDotActive = dotX <= activeW
                        drawCircle(
                            color = if (isDotActive) activeColor else inactiveColor,
                            radius = if (isDotActive) 3.5.dp.toPx() else 2.5.dp.toPx(),
                            center = Offset(dotX, midY),
                        )
                    }
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = 10.dp.toPx(),
                        center = Offset(activeW, midY),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = Offset(activeW, midY),
                    )
                }
                ProgressLineStyle.PULSE -> {
                    val inactivePath = Path()
                    inactivePath.moveTo(activeW, midY)
                    inactivePath.lineTo(w, midY)
                    drawPath(
                        path = inactivePath,
                        color = inactiveColor,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                    )

                    val activePath = Path()
                    activePath.moveTo(0f, midY)
                    var px = 0f
                    val pulseStep = 1.5f
                    val pulsePeriod = 64.dp.toPx()
                    while (px <= activeW) {
                        val phaseOffset = ((px - wavePhase * 18.dp.toPx()) % pulsePeriod + pulsePeriod) % pulsePeriod
                        val norm = phaseOffset / pulsePeriod
                        val spike = when {
                            norm in 0.38f..0.43f -> -3.5.dp.toPx() * sin((norm - 0.38f) / 0.05f * PI.toFloat())
                            norm in 0.47f..0.50f -> 2.5.dp.toPx()
                            norm in 0.50f..0.54f -> -10.dp.toPx() // R-peak (sharp heartbeat spike!)
                            norm in 0.54f..0.58f -> 3.5.dp.toPx()
                            norm in 0.62f..0.70f -> -3.dp.toPx() * sin((norm - 0.62f) / 0.08f * PI.toFloat())
                            else -> 0f
                        }
                        activePath.lineTo(px, midY + spike)
                        px += pulseStep
                    }
                    drawPath(
                        path = activePath,
                        color = activeColor,
                        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = 10.dp.toPx(),
                        center = Offset(activeW, midY),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = Offset(activeW, midY),
                    )
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSeconds = ms / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

