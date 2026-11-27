package com.music.bitchord.ui.player

import androidx.compose.animation.core.Animatable
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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.displayCutoutPadding
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RepeatOne
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
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.graphics.lerp
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
import com.music.bitchord.ui.components.expressive.M3EShapes
import com.music.bitchord.ui.components.expressive.ScallopedFlowerShape
import com.music.bitchord.ui.icons.BitChordIcons
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.BluetoothAudio
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.runtime.mutableIntStateOf
import com.music.bitchord.playback.EqLayout
import com.music.bitchord.playback.EqualizerPreset
import com.music.bitchord.ui.components.HushCenteredCard
import com.music.bitchord.ui.components.HushCenteredCardHost
import com.music.bitchord.ui.haptics.rememberHaptics
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay

/**
 * Material 3 Expressive Player Content:
 * - Rotating Material 3 Expressive artwork shape (260dp–280dp) with edge-to-edge upright crop
 * - Song poster-derived vibrant accent color for active lyrics & highlights with WCAG contrast
 * - Liquid Glass synced LyricsPanel choreography (spring scroll, blur falloff, word glow)
 * - Straight default seekbar + audio specs badge + equal-height transport controls
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
    onShareLyrics: ((LyricsShareRequest) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val fallbackAccent = MaterialTheme.colorScheme.primary

    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }

    val currentProgress = if (isScrubbing) {
        scrubProgress
    } else {
        if (durationMs > 0L) (position.positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val progressLineStyle by PlayerSettings.progressLineStyle.collectAsStateWithLifecycle()
    val beatImpulseEnabled by PlayerSettings.beatImpulse.collectAsStateWithLifecycle()
    val expressiveUiEnabled by PlayerSettings.expressiveUiEnabled.collectAsStateWithLifecycle()
    val playerArtShapeName by PlayerSettings.expressivePlayerArtShape.collectAsStateWithLifecycle()
    val controlsShapeName by PlayerSettings.expressiveControlsShape.collectAsStateWithLifecycle()
    val liveSongGradientEnabled by PlayerSettings.liveSongColorGradient.collectAsStateWithLifecycle()

    // Palette extraction for rich multi-stop ambient gradient & poster-matched lyric highlight
    val artPalette = rememberArtworkColors(song.thumbnailUrl)
    val paletteDominant = artPalette.colors.getOrNull(0) ?: fallbackAccent
    val paletteVibrant = artPalette.colors.getOrNull(1) ?: paletteDominant
    val paletteAmbient = artPalette.colors.getOrNull(2) ?: paletteDominant

    val posterAccent = remember(paletteVibrant, paletteDominant, fallbackAccent, liveSongGradientEnabled, isDark) {
        val raw = if (liveSongGradientEnabled) {
            if (paletteVibrant.luminance() > 0.12f) paletteVibrant else paletteDominant
        } else {
            fallbackAccent
        }
        val lum = raw.luminance()
        if (isDark) {
            when {
                lum < 0.42f -> lerp(raw, Color.White, ((0.52f - lum) / 0.52f).coerceIn(0.25f, 0.72f))
                else -> raw
            }
        } else {
            when {
                lum > 0.36f -> lerp(raw, Color(0xFF141418), ((lum - 0.25f) / 0.75f).coerceIn(0.30f, 0.70f))
                else -> raw
            }
        }
    }

    val primaryAccent = posterAccent
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
    val sheetBg = if (isDark) Color(0xFF141418) else Color(0xFFF4F3F7)
    val sheetCardBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    val sheetPillBg = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)
    val sheetHandle = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.20f)

    val albumShape = remember(playerArtShapeName, expressiveUiEnabled) {
        if (expressiveUiEnabled) M3EShapes.fromName(playerArtShapeName) else RoundedCornerShape(28.dp)
    }
    val controlsShape = remember(controlsShapeName, expressiveUiEnabled) {
        if (expressiveUiEnabled) M3EShapes.fromName(controlsShapeName) else CircleShape
    }
    val isRotationalShape = expressiveUiEnabled && M3EShapes.isRotationalName(playerArtShapeName)

    // Frame-paced smooth rotation that holds angle cleanly on pause
    var artRotationDeg by remember(song.videoId) { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying, isRotationalShape) {
        if (!isPlaying || !isRotationalShape) return@LaunchedEffect
        var lastNanos = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dtSec = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.1f)
            lastNanos = now
            artRotationDeg = (artRotationDeg + dtSec * 14f) % 360f
        }
    }

    // True Beat-Spike Energy Detection: smoother spring curve (1.0f -> 1.045f)
    val beatTimestamp by AudioBeatDetector.beatTimestamp.collectAsStateWithLifecycle()
    val beatEnergy by AudioBeatDetector.beatEnergy.collectAsStateWithLifecycle()
    val beatAnim = remember { Animatable(1.0f) }

    LaunchedEffect(beatTimestamp) {
        if (beatTimestamp > 0L && isPlaying && beatImpulseEnabled) {
            val targetScale = 1.0f + 0.045f * beatEnergy.coerceIn(0.5f, 1.0f)
            beatAnim.snapTo(targetScale)
            beatAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = 220f,
                ),
            )
        }
    }

    val effectiveArtScale = if (isPlaying && beatImpulseEnabled) beatAnim.value else 1.0f

    // Modal sheet visibility states
    val playerHaze = remember { HazeState() }
    var showInlineLyrics by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showAudioSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var showEditControlsCard by remember { mutableStateOf(false) }
    var showEqualizerCard by remember { mutableStateOf(false) }
    var showSleepTimerCard by remember { mutableStateOf(false) }
    var showSongInfoCard by remember { mutableStateOf(false) }

    val lyricHaptics = rememberHaptics()
    val canShareLyrics = onShareLyrics != null && lyricsShareAvailable && !lyrics.isNullOrEmpty()
    val lyricPicker = rememberLyricsPicker(
        song = song,
        lines = lyrics,
        subLines = null,
        artworkUrl = song.thumbnailUrl,
        haptics = lyricHaptics,
        onCard = { req -> onShareLyrics?.invoke(req) },
    )
    val openQuickLyricsShare: () -> Unit = {
        val raw = lyrics.orEmpty()
        if (raw.isNotEmpty() && onShareLyrics != null) {
            val allShareLines = raw.map { LyricsShareLine(text = it.text, subText = null) }
            val posMs = position.positionMs
            val activeIdx = raw.indexOfLast { it.timeMs <= posMs }.coerceAtLeast(0)
            val endIdx = (activeIdx + 3).coerceAtMost(raw.lastIndex)
            val rangeIndices = (activeIdx..endIdx).filter { raw[it].text.isNotBlank() }
            val picked = rangeIndices.map { allShareLines[it] }.ifEmpty {
                allShareLines.filter { it.text.isNotBlank() }.take(4)
            }
            if (picked.isNotEmpty()) {
                onShareLyrics.invoke(
                    LyricsShareRequest(
                        song = song,
                        artworkUrl = song.thumbnailUrl,
                        lines = picked,
                        allLines = allShareLines,
                        selectedIndices = rangeIndices,
                    ),
                )
            }
        }
    }

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = playerHaze),
        ) {
        // Beat-reactive ambient song-color poster gradient that swells with real beat spikes
        val pulseAlpha = if (isPlaying && beatImpulseEnabled) (beatAnim.value - 1.0f) * 3.5f else 0f
        val gradientRadius = 850f * effectiveArtScale
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            paletteVibrant.copy(alpha = if (isDark) (0.28f + pulseAlpha * 0.20f) else (0.16f + pulseAlpha * 0.12f)),
                            paletteDominant.copy(alpha = if (isDark) (0.16f + pulseAlpha * 0.12f) else (0.08f + pulseAlpha * 0.08f)),
                            Color.Transparent,
                        ),
                        radius = gradientRadius,
                    )
                ),
        )
        // Subtle ambient lower gradient wash
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            paletteAmbient.copy(alpha = if (isDark) 0.08f else 0.04f),
                        ),
                    )
                ),
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .displayCutoutPadding(),
        ) {
            val availableHeight = maxHeight
            val isLandscape = maxWidth > maxHeight
            val isCompactHeight = availableHeight < 680.dp
            val artSize = (maxWidth * 0.82f).coerceIn(250.dp, if (isCompactHeight) 250.dp else 280.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isLandscape) 16.dp else 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (isLandscape) Arrangement.Top else Arrangement.SpaceBetween,
            ) {
                // Top Header: Subtle drag handle + balanced action row (Collapse on left, "Now Playing" centered, Output & Lyrics on right)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Subtle Top Drag Handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.5.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(sheetHandle)
                                .clickable(onClick = onDismiss),
                        )
                    }

                    // Top Bar Row with perfectly centered "Now Playing" title
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    ) {
                        // Left: Circular Collapse button (44.dp, CircleShape)
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(controlGlass)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Collapse",
                                tint = iconTint,
                                modifier = Modifier.size(26.dp),
                            )
                        }

                        // Center: Centered bold "Now Playing" title (18.sp, tonal ink)
                        Text(
                            text = "Now Playing",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                            ),
                            color = titleColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center),
                        )

                        // Right: Circular Audio Output and Lyrics action buttons (44.dp, CircleShape)
                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (showAudioSheet) primaryAccent else controlGlass)
                                    .clickable { showAudioSheet = true },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Speaker,
                                    contentDescription = "Audio Output",
                                    tint = if (showAudioSheet) activeDockContent else iconTint,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (showInlineLyrics) primaryAccent else controlGlass)
                                    .clickable { showInlineLyrics = !showInlineLyrics },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = BitChordIcons.LyricsQuote,
                                    contentDescription = "Lyrics",
                                    tint = if (showInlineLyrics) activeDockContent else iconTint,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }

                if (isLandscape) {
                    // Landscape 2-Pane Horizontal Split
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Left Pane: 46% Width (Centerpiece Rotating M3E Artwork Shape or Synced Lyrics)
                        Box(
                            modifier = Modifier
                                .weight(0.46f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center,
                        ) {
                            AnimatedContent(
                                targetState = showInlineLyrics,
                                transitionSpec = {
                                    (fadeIn(tween(260)) + scaleIn(initialScale = 0.94f, animationSpec = tween(260))) togetherWith
                                        (fadeOut(tween(180)) + scaleOut(targetScale = 0.94f, animationSpec = tween(180)))
                                },
                                label = "landscapeCoverLyrics",
                            ) { showingLyrics ->
                                if (!showingLyrics) {
                                    val landscapeArtSize = (availableHeight * 0.70f).coerceIn(160.dp, 280.dp)
                                    Box(
                                        modifier = Modifier
                                            .size(landscapeArtSize)
                                            .graphicsLayer {
                                                scaleX = effectiveArtScale
                                                scaleY = effectiveArtScale
                                                if (isRotationalShape) rotationZ = artRotationDeg
                                            }
                                            .clip(albumShape)
                                            .background(if (isDark) Color(0xFF242428) else Color(0xFFE2E0E6))
                                            .border(1.5.dp, Color.White.copy(alpha = 0.12f), albumShape)
                                            .clickable { showInlineLyrics = true },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        AsyncImage(
                                            model = song.thumbnailUrl,
                                            contentDescription = song.title,
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.Center,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer {
                                                    if (isRotationalShape) {
                                                        rotationZ = -artRotationDeg
                                                        scaleX = 1.42f
                                                        scaleY = 1.42f
                                                    }
                                                },
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .lyricsPinchToCover(enabled = showInlineLyrics) {
                                                showInlineLyrics = false
                                            },
                                    ) {
                                        LyricsPanel(
                                            lines = lyrics.orEmpty(),
                                            trackKey = song.videoId,
                                            playhead = rememberLyricPlayhead(position),
                                            looking = !lyricsUnavailable && lyrics == null,
                                            isPlaying = isPlaying,
                                            onSeekToLine = { ms ->
                                                if (durationMs > 0L) {
                                                    onSeekFraction((ms.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f))
                                                }
                                            },
                                            controlsOpen = true,
                                            onRevealControls = {},
                                            onHideControls = {},
                                            canPick = canShareLyrics,
                                            picking = lyricPicker.picking,
                                            picked = lyricPicker.picks,
                                            onPickLine = lyricPicker.pick,
                                            onTogglePick = lyricPicker.toggle,
                                            centerFocus = true,
                                            activeColor = primaryAccent,
                                            inactiveColor = if (isDark) Color.White else Color(0xFF1C1C22),
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        if (canShareLyrics && !lyricPicker.picking) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(controlGlass)
                                                    .clickable { openQuickLyricsShare() },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.IosShare,
                                                    contentDescription = "Share Lyrics Card",
                                                    tint = iconTint,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            }
                                        }
                                        if (lyricPicker.picking) {
                                            LyricsPickBar(
                                                shareEnabled = lyricPicker.picks.isNotEmpty() && !lyricPicker.overBudget,
                                                onCancel = lyricPicker.cancel,
                                                onShare = lyricPicker.share,
                                                modifier = Modifier.align(Alignment.BottomCenter),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Right Pane: 54% Width (Title, Clickable Artist, Seekbar, Transport, Dock)
                        Column(
                            modifier = Modifier
                                .weight(0.54f)
                                .fillMaxHeight()
                                .padding(start = 16.dp, end = 4.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // Title & Clickable Artist
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        letterSpacing = (-0.5).sp,
                                    ),
                                    color = titleColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                    ),
                                    color = textSubColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onOpenArtist() }
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                )
                            }

                            // Progress Seekbar & Timestamps
                            Column(modifier = Modifier.fillMaxWidth()) {
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
                                    activeColor = if (isDark) Color.White else primaryAccent,
                                    inactiveColor = if (isDark) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.12f),
                                )
                                Spacer(Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val currentMs = if (isScrubbing) (scrubProgress * durationMs).toLong() else position.positionMs
                                    Text(
                                        text = formatDuration(currentMs),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                        ),
                                        color = timeColor,
                                    )
                                    Text(
                                        text = formatDuration(durationMs),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                        ),
                                        color = timeColor,
                                    )
                                }
                            }

                            // Transport Controls Row (Previous, Play/Pause Pill, Next)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFFE8E7EE) else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable(onClick = onPrevious),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipPrevious,
                                        contentDescription = "Previous",
                                        tint = if (isDark) Color(0xFF141418) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF28282E) else Color(0xFF1E1E24))
                                        .clickable(onClick = onPlayPause),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                            contentDescription = if (isPlaying) "Pause" else "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp),
                                        )
                                        Text(
                                            text = if (isPlaying) "Pause" else "Play",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                            ),
                                            color = Color.White,
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFFE8E7EE) else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable(onClick = onNext),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipNext,
                                        contentDescription = "Next",
                                        tint = if (isDark) Color(0xFF141418) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }

                            // 5-Button Utility Dock
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val audioActive = showAudioSheet
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (audioActive) primaryAccent else dockGlass)
                                        .clickable { showAudioSheet = true },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Speaker,
                                        contentDescription = "Audio Output",
                                        tint = if (audioActive) activeDockContent else iconTint,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                val liked = likeStatus == LikeStatus.LIKE
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(dockGlass)
                                        .clickable(onClick = onToggleLike),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = if (liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = if (liked) "Unlike" else "Like",
                                        tint = if (liked) primaryAccent else iconTint,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                val lyricsActive = showInlineLyrics
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (lyricsActive) primaryAccent else dockGlass)
                                        .clickable { showInlineLyrics = !showInlineLyrics },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = BitChordIcons.LyricsQuote,
                                        contentDescription = "Lyrics",
                                        tint = if (lyricsActive) activeDockContent else iconTint,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                val queueActive = showQueueSheet
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (queueActive) primaryAccent else dockGlass)
                                        .clickable { showQueueSheet = true },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = BitChordIcons.Queue,
                                        contentDescription = "Queue",
                                        tint = if (queueActive) activeDockContent else iconTint,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                val moreActive = showMoreSheet
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (moreActive) primaryAccent else dockGlass)
                                        .clickable { showMoreSheet = true },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreVert,
                                        contentDescription = "More",
                                        tint = if (moreActive) activeDockContent else iconTint,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                } else {
                AnimatedContent(
                    targetState = showInlineLyrics,
                    transitionSpec = {
                        (fadeIn(spring(stiffness = 380f, dampingRatio = 0.82f)) + scaleIn(initialScale = 0.94f, animationSpec = spring(stiffness = 380f, dampingRatio = 0.82f))) togetherWith
                        (fadeOut(spring(stiffness = 450f)) + scaleOut(targetScale = 1.04f, animationSpec = spring(stiffness = 450f)))
                    },
                    label = "m3e_player_lyrics_morph",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) { lyricsActive ->
                    if (!lyricsActive) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        // Rotating Material 3 Expressive geometric shape artwork (260dp–280dp) with upright cropped poster
                        val displayArtSize = (this@BoxWithConstraints.maxWidth * 0.78f).coerceIn(250.dp, if (isCompactHeight) 260.dp else 280.dp)
                        Box(
                            modifier = Modifier
                                .size(displayArtSize)
                                .graphicsLayer {
                                    scaleX = effectiveArtScale
                                    scaleY = effectiveArtScale
                                    if (isRotationalShape) rotationZ = artRotationDeg
                                }
                                .clip(albumShape)
                                .background(if (isDark) Color(0xFF242428) else Color(0xFFE2E0E6))
                                .border(1.5.dp, Color.White.copy(alpha = 0.12f), albumShape)
                                .clickable { showInlineLyrics = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            AsyncImage(
                                model = song.thumbnailUrl,
                                contentDescription = song.title,
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        if (isRotationalShape) {
                                            rotationZ = -artRotationDeg
                                            scaleX = 1.42f
                                            scaleY = 1.42f
                                        }
                                    },
                            )
                        }

                        Spacer(Modifier.height(if (isCompactHeight) 14.dp else 22.dp))

                        // Centered Bold Title & Artist Subtitle (matching video_3.mp4 & vid4_f08.jpg)
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isCompactHeight) 22.sp else 24.sp,
                                letterSpacing = (-0.5).sp,
                            ),
                            color = titleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = if (isCompactHeight) 15.sp else 16.sp,
                            ),
                            color = textSubColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onOpenArtist() }
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                } else {
                    // Full-Stage Liquid Glass Synced LyricsPanel tinted dynamically to song poster gradient
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
                                        modifier = Modifier.clickable { onOpenArtist() },
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (canShareLyrics) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(controlGlass)
                                            .clickable { openQuickLyricsShare() },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.IosShare,
                                            contentDescription = "Share Lyrics Card",
                                            tint = iconTint,
                                            modifier = Modifier.size(17.dp),
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
                        }

                        Spacer(Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .lyricsPinchToCover(enabled = showInlineLyrics) {
                                    showInlineLyrics = false
                                },
                        ) {
                            LyricsPanel(
                            lines = lyrics.orEmpty(),
                            trackKey = song.videoId,
                            playhead = rememberLyricPlayhead(position),
                            looking = !lyricsUnavailable && lyrics == null,
                            isPlaying = isPlaying,
                            onSeekToLine = { ms ->
                                if (durationMs > 0L) {
                                    onSeekFraction((ms.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f))
                                }
                            },
                            controlsOpen = true,
                            onRevealControls = {},
                            onHideControls = {},
                            canPick = canShareLyrics,
                            picking = lyricPicker.picking,
                            picked = lyricPicker.picks,
                            onPickLine = lyricPicker.pick,
                            onTogglePick = lyricPicker.toggle,
                            centerFocus = true,
                            activeColor = primaryAccent,
                            inactiveColor = if (isDark) Color.White else Color(0xFF1C1C22),
                            modifier = Modifier.fillMaxSize(),
                        )
                            if (lyricPicker.picking) {
                                LyricsPickBar(
                                    shareEnabled = lyricPicker.picks.isNotEmpty() && !lyricPicker.overBudget,
                                    onCancel = lyricPicker.cancel,
                                    onShare = lyricPicker.share,
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                )
                            }
                        }
                    }
                }
                }

                // Exact Transport & Seekbar Layout (video_3.mp4 00:00 & vid4_f08.jpg)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 12.dp else 16.dp),
                ) {
                    // Row 1: Wide dark/tonal pill Play/Pause button on left + circular Next button on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Wide Play / Pause pill button (height = 64.dp, weight(1f), CircleShape with bold label)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isCompactHeight) 56.dp else 64.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF28282E) else Color(0xFF1E1E24))
                                .clickable(onClick = onPlayPause),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp),
                                )
                                Text(
                                    text = if (isPlaying) "Pause" else "Play",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                    ),
                                    color = Color.White,
                                )
                            }
                        }

                        // Circular Next button (size = 64.dp, CircleShape)
                        Box(
                            modifier = Modifier
                                .size(if (isCompactHeight) 56.dp else 64.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFFE8E7EE) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(onClick = onNext),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Next",
                                tint = if (isDark) Color(0xFF141418) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(30.dp),
                            )
                        }
                    }

                    // Row 2: Circular Previous button on left + clean M3E progress slider with timestamps on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Circular Previous button (size = 52.dp, CircleShape)
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFFE8E7EE) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(onClick = onPrevious),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous",
                                tint = if (isDark) Color(0xFF141418) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(26.dp),
                            )
                        }

                        // Progress slider with aligned start/end timestamps
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
                                activeColor = if (isDark) Color.White else primaryAccent,
                                inactiveColor = if (isDark) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.12f),
                            )

                            Spacer(Modifier.height(4.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val currentMs = if (isScrubbing) (scrubProgress * durationMs).toLong() else position.positionMs
                                Text(
                                    text = formatDuration(currentMs),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                    color = timeColor,
                                )
                                Text(
                                    text = formatDuration(durationMs),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                    color = timeColor,
                                )
                            }
                        }
                    }

                    // Row 3: Exact 5-Button Bottom Utility Dock (Audio Output, Favorite, Lyrics, Queue, More)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 1. Audio Output
                        val audioActive = showAudioSheet
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (audioActive) primaryAccent else dockGlass)
                                .clickable { showAudioSheet = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Speaker,
                                contentDescription = "Audio Output",
                                tint = if (audioActive) activeDockContent else iconTint,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        // 2. Favorite (Heart)
                        val liked = likeStatus == LikeStatus.LIKE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(dockGlass)
                                .clickable(onClick = onToggleLike),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = if (liked) "Unlike" else "Like",
                                tint = if (liked) primaryAccent else iconTint,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        // 3. Lyrics
                        val lyricsActive = showInlineLyrics
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (lyricsActive) primaryAccent else dockGlass)
                                .clickable { showInlineLyrics = !showInlineLyrics },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = BitChordIcons.LyricsQuote,
                                contentDescription = "Lyrics",
                                tint = if (lyricsActive) activeDockContent else iconTint,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        // 4. Queue
                        val queueActive = showQueueSheet
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (queueActive) primaryAccent else dockGlass)
                                .clickable { showQueueSheet = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = BitChordIcons.Queue,
                                contentDescription = "Queue",
                                tint = if (queueActive) activeDockContent else iconTint,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        // 5. More (3-dot)
                        val moreActive = showMoreSheet
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (moreActive) primaryAccent else dockGlass)
                                .clickable { showMoreSheet = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "More",
                                tint = if (moreActive) activeDockContent else iconTint,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
                }
            }
        }
        }
        // =========================================================================
        // Modal Sheet 1: Queue Sheet (Centered Frosted Card)
        // =========================================================================
        HushCenteredCardHost(
            visible = showQueueSheet,
            onDismiss = { showQueueSheet = false },
            hazeState = playerHaze,
            title = "Queue",
            subtitle = "${queue.size} songs",
            maxWidth = 420.dp,
            maxHeight = 620.dp,
            headerEnd = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (shuffleEnabled) accentBg else sheetPillBg)
                            .clickable(onClick = onToggleShuffle),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (shuffleEnabled) accentFg else iconTint,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (repeatMode != 0) accentBg else sheetPillBg)
                            .clickable(onClick = onCycleRepeat),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Repeat,
                            contentDescription = "Repeat",
                            tint = if (repeatMode != 0) accentFg else iconTint,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626))
                            .clickable(onClick = onClearQueue),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Clear Queue",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            },
        ) {
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

            Spacer(Modifier.height(14.dp))
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
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
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

        // =========================================================================
        // Modal Sheet 2: Playback / Audio Settings Card (Centered Frosted Card)
        // =========================================================================
        HushCenteredCardHost(
            visible = showAudioSheet,
            onDismiss = { showAudioSheet = false },
            hazeState = playerHaze,
            title = "Audio Settings",
            subtitle = "Output & Live Volume",
            maxWidth = 420.dp,
            maxHeight = 620.dp,
        ) {
            val systemVolume = PlayerPlatform.host.volume
            val systemVolumeLevel by systemVolume.level.collectAsStateWithLifecycle()
            val useSystemVolume by PlayerSettings.useSystemVolume.collectAsStateWithLifecycle()
            val pauseOnZero by PlayerSettings.pauseOnZeroVolume.collectAsStateWithLifecycle()
            val autoResume by PlayerSettings.autoResumeOnVolume.collectAsStateWithLifecycle()
            val appPlayerVolume by PlayerSettings.appPlayerVolume.collectAsStateWithLifecycle()
            val smartFade by PlayerSettings.smartFadeEnabled.collectAsStateWithLifecycle()
            val audioOutputs = rememberAudioOutputs()
            val activeOutput = audioOutputs.firstOrNull { it.isActive } ?: audioOutputs.firstOrNull()
            val activeOutputName = if (activeOutput != null) {
                outputLabel(activeOutput, accountName = null)
            } else {
                "Phone Speaker"
            }
            val activeOutputSub = when (activeOutput?.kind) {
                AudioOutputKind.BLUETOOTH -> "Bluetooth Audio"
                AudioOutputKind.WIRED -> "Wired Headphones"
                AudioOutputKind.USB -> "USB Audio DAC"
                AudioOutputKind.HDMI -> "HDMI Output"
                else -> "Built-in speaker"
            }
            val activeOutputIcon = when (activeOutput?.kind) {
                AudioOutputKind.BLUETOOTH -> Icons.Rounded.BluetoothAudio
                AudioOutputKind.WIRED -> Icons.Rounded.Headphones
                AudioOutputKind.USB -> Icons.Rounded.Usb
                else -> Icons.Rounded.Speaker
            }

            var showDevicePicker by remember { mutableStateOf(false) }
            val openOutputPicker = rememberOutputPicker { showDevicePicker = !showDevicePicker }
            var sliderLevel by remember { mutableFloatStateOf(if (useSystemVolume) systemVolumeLevel else appPlayerVolume) }
            var isDraggingVolume by remember { mutableStateOf(false) }

            LaunchedEffect(systemVolumeLevel, appPlayerVolume, useSystemVolume, showAudioSheet) {
                systemVolume.refresh()
                if (!isDraggingVolume) {
                    sliderLevel = if (useSystemVolume) systemVolumeLevel else appPlayerVolume
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Top Speaker Output & Live Volume Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) Color(0xFF8E8E93).copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.06f))
                        .padding(16.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
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
                                        imageVector = activeOutputIcon,
                                        contentDescription = null,
                                        tint = if (isDark) Color.Black else Color.White,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Column {
                                    Text(
                                        text = activeOutputName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = titleColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = activeOutputSub,
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
                                        .background(if (isDark) Color.White else Color(0xFF121215))
                                        .clickable { systemVolume.refresh() },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = "Refresh Volume",
                                        tint = if (isDark) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color.White else Color(0xFF121215))
                                        .clickable { openOutputPicker() },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SwapHoriz,
                                        contentDescription = "Switch Audio Output",
                                        tint = if (isDark) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }

                        if (showDevicePicker && audioOutputs.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                audioOutputs.forEach { device ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (device.isActive) primaryAccent.copy(alpha = 0.20f) else sheetCardBg)
                                            .clickable {
                                                PlayerPlatform.host.selectAudioOutput(device.id)
                                                showDevicePicker = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = outputLabel(device, accountName = null),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (device.isActive) FontWeight.Bold else FontWeight.Medium,
                                            ),
                                            color = titleColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )
                                        if (device.isActive) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = primaryAccent,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (useSystemVolume) "System Volume" else "Player Volume",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = titleColor,
                            )
                            Text(
                                text = "${(sliderLevel * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = titleColor,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = if (sliderLevel <= 0.01f) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                                contentDescription = null,
                                tint = textSubColor,
                                modifier = Modifier.size(20.dp),
                            )
                            Slider(
                                value = sliderLevel.coerceIn(0f, 1f),
                                onValueChange = { next ->
                                    isDraggingVolume = true
                                    sliderLevel = next
                                    if (useSystemVolume) {
                                        systemVolume.set(next)
                                    } else {
                                        PlayerSettings.setAppPlayerVolume(next)
                                    }
                                },
                                onValueChangeFinished = { isDraggingVolume = false },
                                colors = SliderDefaults.colors(
                                    thumbColor = primaryAccent,
                                    activeTrackColor = primaryAccent,
                                    inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.15f),
                                ),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Text(
                    text = "PLAYBACK BEHAVIOR",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                    color = textSubColor,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(sheetCardBg),
                ) {
                    RhythmSettingSwitchRow(
                        icon = Icons.Rounded.VolumeUp,
                        title = "Use System Volume",
                        subtitle = "Control hardware volume instead of in-app gain",
                        checked = useSystemVolume,
                        onCheckedChange = { PlayerSettings.setUseSystemVolume(it) },
                    )
                    RhythmSettingSwitchRow(
                        icon = Icons.Rounded.VolumeOff,
                        title = "Pause on Zero Volume",
                        subtitle = "Automatically pause playback when volume hits 0%",
                        checked = pauseOnZero,
                        onCheckedChange = { PlayerSettings.setPauseOnZeroVolume(it) },
                    )
                    RhythmSettingSwitchRow(
                        icon = Icons.Rounded.Speaker,
                        title = "Auto Resume",
                        subtitle = "Resume playback when volume is turned back up",
                        checked = autoResume,
                        onCheckedChange = { PlayerSettings.setAutoResumeOnVolume(it) },
                    )
                    RhythmSettingSwitchRow(
                        icon = Icons.Rounded.SwapHoriz,
                        title = "Gapless & Smart Fade",
                        subtitle = "Smooth transitions between tracks",
                        checked = smartFade,
                        onCheckedChange = { PlayerSettings.setSmartFadeEnabled(it) },
                    )
                }
            }
        }

        // =========================================================================
        // Modal Sheet 3: Player Controls Card (Centered Frosted Card)
        // =========================================================================
        val eqEnabledState by PlayerSettings.equalizerEnabled.collectAsStateWithLifecycle()
        val sleepActiveMins by PlayerPlatform.host.sleepTimerActiveMinutes.collectAsStateWithLifecycle()
        val sleepAfterTrack by PlayerPlatform.host.sleepTimerAfterTrack.collectAsStateWithLifecycle()

        HushCenteredCardHost(
            visible = showMoreSheet,
            onDismiss = { showMoreSheet = false },
            hazeState = playerHaze,
            title = "Player Controls",
            subtitle = "${song.title} · ${song.artist}",
            maxWidth = 420.dp,
            maxHeight = 620.dp,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
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
                            subtitle = "Shapes & seekbar",
                            onClick = {
                                showMoreSheet = false
                                showEditControlsCard = true
                            },
                            modifier = Modifier.weight(1f),
                        )
                        RhythmControlCard(
                            icon = Icons.Rounded.PlaylistAdd,
                            title = "Add to playlist",
                            subtitle = "Save to library",
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
                            title = "Remix Studio",
                            subtitle = "Speed, 2-Song & Save",
                            onClick = {
                                showMoreSheet = false
                                onOpenMixer?.invoke()
                            },
                            modifier = Modifier.weight(1f),
                        )
                        RhythmControlCard(
                            icon = Icons.Rounded.GraphicEq,
                            title = "Equalizer",
                            subtitle = if (eqEnabledState) "Active" else "Off",
                            onClick = {
                                showMoreSheet = false
                                showEqualizerCard = true
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
                            icon = Icons.Rounded.Timer,
                            title = "Sleep Timer",
                            subtitle = when {
                                sleepAfterTrack -> "End of track"
                                sleepActiveMins != null -> "${sleepActiveMins}m set"
                                else -> "Disabled"
                            },
                            onClick = {
                                showMoreSheet = false
                                showSleepTimerCard = true
                            },
                            modifier = Modifier.weight(1f),
                        )
                        RhythmControlCard(
                            icon = BitChordIcons.LyricsQuote,
                            title = if (showInlineLyrics) "Show Artwork" else "Show Lyrics",
                            subtitle = "Tap cover or pinch",
                            onClick = {
                                showMoreSheet = false
                                showInlineLyrics = !showInlineLyrics
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
                            subtitle = "Details & codecs",
                            onClick = {
                                showMoreSheet = false
                                showSongInfoCard = true
                            },
                            modifier = Modifier.weight(1f),
                        )
                        RhythmControlCard(
                            icon = Icons.Rounded.Download,
                            title = "Save Locally",
                            subtitle = "To Music/Hush",
                            onClick = {
                                showMoreSheet = false
                                PlayerPlatform.host.saveTrackLocally(song)
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
                            icon = Icons.Rounded.Share,
                            title = "Share & More",
                            subtitle = "Share link or options",
                            onClick = {
                                showMoreSheet = false
                                onOpenMenu()
                            },
                            modifier = Modifier.weight(1f),
                        )
                        if (canShareLyrics) {
                            RhythmControlCard(
                                icon = Icons.Rounded.IosShare,
                                title = "Lyrics Card",
                                subtitle = "Share styled lyrics",
                                onClick = {
                                    showMoreSheet = false
                                    openQuickLyricsShare()
                                },
                                modifier = Modifier.weight(1f),
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // =========================================================================
        // Centered Sub-Cards: Edit Controls, Equalizer, Sleep Timer, Song Info
        // =========================================================================
        RhythmEditControlsCard(
            visible = showEditControlsCard,
            hazeState = playerHaze,
            onDismiss = { showEditControlsCard = false },
        )
        RhythmEqualizerCard(
            visible = showEqualizerCard,
            hazeState = playerHaze,
            onDismiss = { showEqualizerCard = false },
        )
        RhythmSleepTimerCard(
            visible = showSleepTimerCard,
            hazeState = playerHaze,
            onDismiss = { showSleepTimerCard = false },
        )
        if (showSongInfoCard) {
            PlayerPlatform.host.SongInfoCard(
                song = song,
                onDismiss = { showSongInfoCard = false },
            )
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
                    val strokeW = 4.5.dp.toPx()
                    val radius = strokeW / 2f
                    val waveLength = 48.dp.toPx()
                    val waveAmplitude = 5.dp.toPx()

                    val inactivePath = Path()
                    inactivePath.moveTo(activeW.coerceAtLeast(radius), midY)
                    inactivePath.lineTo((w - radius).coerceAtLeast(radius), midY)
                    drawPath(
                        path = inactivePath,
                        color = inactiveColor,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    )

                    val activePath = Path()
                    activePath.moveTo(radius, midY)
                    var x = radius
                    val step = 3f
                    while (x <= activeW) {
                        val angle = (x / waveLength) * (2 * PI).toFloat() - wavePhase
                        val y = midY + sin(angle) * waveAmplitude
                        activePath.lineTo(x, y)
                        x += step
                    }
                    drawPath(
                        path = activePath,
                        color = activeColor,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    )

                    val thumbAngle = (activeW / waveLength) * (2 * PI).toFloat() - wavePhase
                    val thumbY = midY + sin(thumbAngle) * waveAmplitude
                    val thumbX = activeW.coerceIn(radius, (w - radius).coerceAtLeast(radius))
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = 10.dp.toPx(),
                        center = Offset(thumbX, thumbY),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = Offset(thumbX, thumbY),
                    )
                }
                ProgressLineStyle.STRAIGHT -> {
                    val trackHeight = 6.dp.toPx()
                    val radius = trackHeight / 2f
                    drawLine(
                        color = inactiveColor,
                        start = Offset(radius, midY),
                        end = Offset((w - radius).coerceAtLeast(radius), midY),
                        strokeWidth = trackHeight,
                        cap = StrokeCap.Round,
                    )
                    if (activeW > 0f) {
                        val activeEnd = activeW.coerceIn(radius, (w - radius).coerceAtLeast(radius))
                        drawLine(
                            color = activeColor,
                            start = Offset(radius, midY),
                            end = Offset(activeEnd, midY),
                            strokeWidth = trackHeight,
                            cap = StrokeCap.Round,
                        )
                    }
                    val thumbX = activeW.coerceIn(radius, (w - radius).coerceAtLeast(radius))
                    // Sleek circular M3 thumb with soft outer ring
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = 10.dp.toPx(),
                        center = Offset(thumbX, midY),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = Offset(thumbX, midY),
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
                    val thumbX = activeW.coerceIn(0f, w)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = 10.dp.toPx(),
                        center = Offset(thumbX, midY),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = Offset(thumbX, midY),
                    )
                }
                ProgressLineStyle.PULSE -> {
                    val strokeW = 4.5.dp.toPx()
                    val radius = strokeW / 2f
                    val inactivePath = Path()
                    inactivePath.moveTo(activeW.coerceAtLeast(radius), midY)
                    inactivePath.lineTo((w - radius).coerceAtLeast(radius), midY)
                    drawPath(
                        path = inactivePath,
                        color = inactiveColor,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    )

                    val activePath = Path()
                    activePath.moveTo(radius, midY)
                    var px = radius
                    val pulseStep = 2.5f
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
                        style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    )
                    val thumbX = activeW.coerceIn(radius, (w - radius).coerceAtLeast(radius))
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = 10.dp.toPx(),
                        center = Offset(thumbX, midY),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = Offset(thumbX, midY),
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

private fun Modifier.lyricsPinchToCover(
    enabled: Boolean,
    onReturnToCover: () -> Unit,
): Modifier = if (!enabled) {
    this
} else {
    this.pointerInput(Unit) {
        awaitPointerEventScope {
            var zoom = 1f
            while (true) {
                val event = awaitPointerEvent()
                val pressed = event.changes.count { it.pressed }
                if (pressed >= 2) {
                    val stepZoom = event.calculateZoom()
                    if (stepZoom > 0f && stepZoom.isFinite()) {
                        zoom *= stepZoom
                        event.changes.forEach { if (it.pressed) it.consume() }
                        if (zoom < 0.88f) {
                            zoom = 1f
                            onReturnToCover()
                        }
                    }
                } else {
                    zoom = 1f
                }
            }
        }
    }
}

@Composable
private fun RhythmEditControlsCard(
    visible: Boolean,
    hazeState: HazeState,
    onDismiss: () -> Unit,
) {
    val progressStyle by PlayerSettings.progressLineStyle.collectAsStateWithLifecycle()
    val beatImpulse by PlayerSettings.beatImpulse.collectAsStateWithLifecycle()
    val artShapeName by PlayerSettings.expressivePlayerArtShape.collectAsStateWithLifecycle()
    val controlsShapeName by PlayerSettings.expressiveControlsShape.collectAsStateWithLifecycle()
    val liveGradient by PlayerSettings.liveSongColorGradient.collectAsStateWithLifecycle()
    val hideVolumeBar by PlayerSettings.hideVolumeBar.collectAsStateWithLifecycle()
    val hideSongStatus by PlayerSettings.hideSongStatus.collectAsStateWithLifecycle()

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val titleColor = if (isDark) Color.White else Color(0xFF121215)
    val subColor = if (isDark) Color.White.copy(alpha = 0.60f) else Color(0xFF555562)
    val accent = MaterialTheme.colorScheme.primary
    val chipBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    val artShapeOptions = remember {
        listOf("8-Leaf Clover", "Flower", "Sunny", "4-Sided Cookie", "Circle", "Squircle", "Puffy", "Gem", "Heart", "Pill", "Hexagon", "Starburst")
    }
    val controlShapeOptions = remember {
        listOf("Flower", "Squircle", "Pill", "Circle", "8-Leaf Clover", "4-Sided Cookie")
    }

    HushCenteredCardHost(
        visible = visible,
        onDismiss = onDismiss,
        hazeState = hazeState,
        title = "Edit Controls",
        subtitle = "Customize M3E Player Layout & Motion",
        maxWidth = 420.dp,
        maxHeight = 620.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "SEEKBAR STYLE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                color = subColor,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProgressLineStyle.entries.forEach { style ->
                    val selected = progressStyle == style
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) accent else chipBg)
                            .clickable { PlayerSettings.setProgressLineStyle(style) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = style.label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selected) Color.White else titleColor,
                            maxLines = 1,
                        )
                    }
                }
            }

            Text(
                text = "ARTWORK SHAPE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                color = subColor,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                artShapeOptions.forEach { shapeName ->
                    val selected = artShapeName == shapeName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) accent else chipBg)
                            .border(
                                width = 1.dp,
                                color = if (selected) accent else Color.Transparent,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable { PlayerSettings.setExpressivePlayerArtShape(shapeName) }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                    ) {
                        Text(
                            text = shapeName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selected) Color.White else titleColor,
                        )
                    }
                }
            }

            Text(
                text = "TRANSPORT CONTROLS SHAPE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                color = subColor,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                controlShapeOptions.forEach { shapeName ->
                    val selected = controlsShapeName == shapeName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) accent else chipBg)
                            .clickable { PlayerSettings.setExpressiveControlsShape(shapeName) }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                    ) {
                        Text(
                            text = shapeName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selected) Color.White else titleColor,
                        )
                    }
                }
            }

            RhythmSettingSwitchRow(
                icon = Icons.Rounded.GraphicEq,
                title = "Beat Impulse",
                subtitle = "Pulse artwork with live bass & beat peaks",
                checked = beatImpulse,
                onCheckedChange = { PlayerSettings.setBeatImpulse(it) },
            )
            RhythmSettingSwitchRow(
                icon = Icons.Rounded.Tune,
                title = "Live Song Color Gradient",
                subtitle = "Adapt glow & lyric highlight to album art",
                checked = liveGradient,
                onCheckedChange = { PlayerSettings.setLiveSongColorGradient(it) },
            )
            RhythmSettingSwitchRow(
                icon = Icons.Rounded.VolumeUp,
                title = "Show Quick Volume Strip",
                subtitle = "Display inline output & volume indicator",
                checked = !hideVolumeBar,
                onCheckedChange = { PlayerSettings.setHideVolumeBar(!it) },
            )
            RhythmSettingSwitchRow(
                icon = Icons.Rounded.MusicNote,
                title = "Minimal Header Mode",
                subtitle = "Hide quality & playback status badges",
                checked = hideSongStatus,
                onCheckedChange = { PlayerSettings.setHideSongStatus(it) },
            )
        }
    }
}

@Composable
private fun RhythmEqualizerCard(
    visible: Boolean,
    hazeState: HazeState,
    onDismiss: () -> Unit,
) {
    val eqEnabled by PlayerSettings.equalizerEnabled.collectAsStateWithLifecycle()
    val bands by PlayerSettings.equalizerBands.collectAsStateWithLifecycle()
    val presetName by PlayerSettings.equalizerPresetName.collectAsStateWithLifecycle()
    val balance by PlayerSettings.equalizerBalance.collectAsStateWithLifecycle()

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val titleColor = if (isDark) Color.White else Color(0xFF121215)
    val subColor = if (isDark) Color.White.copy(alpha = 0.60f) else Color(0xFF555562)
    val accent = MaterialTheme.colorScheme.primary
    val chipBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    val cardBg = if (isDark) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.04f)
    val presets = remember { EqualizerPreset.entries.filter { it != EqualizerPreset.CUSTOM } }
    val bandLabels = remember {
        EqLayout.MANUAL_BANDS_HZ.map { hz ->
            if (hz >= 1000f) {
                val khz = hz / 1000f
                if (khz % 1f == 0f) "${khz.toInt()} kHz" else "$khz kHz"
            } else {
                "${hz.toInt()} Hz"
            }
        }
    }
    val maxRange = EqLayout.MANUAL_RANGE_DB

    HushCenteredCardHost(
        visible = visible,
        onDismiss = onDismiss,
        hazeState = hazeState,
        title = "Equalizer",
        subtitle = if (eqEnabled) {
            presetName.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
        } else {
            "Bypassed · Tap to enable"
        },
        maxWidth = 420.dp,
        maxHeight = 640.dp,
        headerEnd = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(chipBg)
                    .clickable {
                        PlayerSettings.setEqualizerEnabled(true)
                        PlayerSettings.applyEqualizerPreset(EqualizerPreset.FLAT.name)
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "Reset",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = titleColor,
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RhythmSettingSwitchRow(
                icon = Icons.Rounded.GraphicEq,
                title = "Enable Equalizer",
                subtitle = if (eqEnabled) "Active on live PCM pipeline" else "Pass-through studio flat audio",
                checked = eqEnabled,
                onCheckedChange = { PlayerSettings.setEqualizerEnabled(it) },
            )

            Text(
                text = "PRESETS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                color = subColor,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                presets.forEach { preset ->
                    val selected = eqEnabled && presetName == preset.name
                    val label = preset.name.replace('_', ' ').lowercase()
                        .split(' ')
                        .joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) accent else chipBg)
                            .clickable {
                                PlayerSettings.setEqualizerEnabled(true)
                                PlayerSettings.applyEqualizerPreset(preset.name)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selected) Color.White else titleColor,
                        )
                    }
                }
            }

            Text(
                text = "FREQUENCY BANDS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                color = subColor,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                bandLabels.forEachIndexed { idx, hzLabel ->
                    val currentDb = bands.getOrElse(idx) { 0f }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = hzLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = titleColor,
                            modifier = Modifier.width(52.dp),
                        )
                        Slider(
                            value = currentDb,
                            onValueChange = { nextDb ->
                                val mutable = List(bandLabels.size) { i ->
                                    if (i == idx) nextDb else bands.getOrElse(i) { 0f }
                                }
                                if (!eqEnabled) PlayerSettings.setEqualizerEnabled(true)
                                PlayerSettings.setEqualizerBands(mutable)
                            },
                            valueRange = -maxRange..maxRange,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = accent,
                                activeTrackColor = accent,
                                inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.12f),
                            ),
                        )
                        val rounded = (currentDb * 10).toInt() / 10f
                        Text(
                            text = if (rounded > 0f) "+$rounded" else "$rounded",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = subColor,
                            modifier = Modifier.width(42.dp),
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Channel Balance",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = titleColor,
                    )
                    Text(
                        text = when {
                            balance < -0.05f -> "L ${((-balance) * 100).toInt()}%"
                            balance > 0.05f -> "R ${(balance * 100).toInt()}%"
                            else -> "Center"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = subColor,
                    )
                }
                Slider(
                    value = balance,
                    onValueChange = { next ->
                        if (!eqEnabled) PlayerSettings.setEqualizerEnabled(true)
                        PlayerSettings.setEqualizerBalance(if (kotlin.math.abs(next) < 0.04f) 0f else next)
                    },
                    valueRange = -1f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = accent,
                        activeTrackColor = accent,
                        inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.12f),
                    ),
                )
            }
        }
    }
}

@Composable
private fun RhythmSleepTimerCard(
    visible: Boolean,
    hazeState: HazeState,
    onDismiss: () -> Unit,
) {
    val activeMinutes by PlayerPlatform.host.sleepTimerActiveMinutes.collectAsStateWithLifecycle()
    val afterTrack by PlayerPlatform.host.sleepTimerAfterTrack.collectAsStateWithLifecycle()
    val deadlineMs by PlayerPlatform.host.sleepTimerDeadlineMs.collectAsStateWithLifecycle()
    var remainingMs by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(visible, deadlineMs, afterTrack, activeMinutes) {
        if (!visible) return@LaunchedEffect
        while (true) {
            remainingMs = PlayerPlatform.host.sleepTimerRemainingMs()
            delay(1000L)
        }
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val titleColor = if (isDark) Color.White else Color(0xFF121215)
    val subColor = if (isDark) Color.White.copy(alpha = 0.62f) else Color(0xFF555562)
    val accent = MaterialTheme.colorScheme.primary
    val chipBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    val statusText = when {
        afterTrack -> "Stops at end of current track"
        remainingMs != null && remainingMs!! > 0L -> {
            val totalSec = remainingMs!! / 1000L
            val m = totalSec / 60L
            val s = totalSec % 60L
            "Pauses in $m:${s.toString().padStart(2, '0')}"
        }
        activeMinutes != null -> "${activeMinutes}m active"
        else -> "Timer is off"
    }

    val minuteOptions = remember { listOf(5, 10, 15, 20, 30, 45, 60) }

    HushCenteredCardHost(
        visible = visible,
        onDismiss = onDismiss,
        hazeState = hazeState,
        title = "Sleep Timer",
        subtitle = statusText,
        maxWidth = 390.dp,
        maxHeight = 480.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            minuteOptions.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowItems.forEach { mins ->
                        val selected = !afterTrack && activeMinutes == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) accent else chipBg)
                                .clickable { PlayerPlatform.host.startSleepTimer(mins) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "$mins min",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (selected) Color.White else titleColor,
                            )
                        }
                    }
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (afterTrack) accent else chipBg)
                    .clickable { PlayerPlatform.host.startSleepTimerAfterTrack() }
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Stop After Current Track",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (afterTrack) Color.White else titleColor,
                )
            }

            if (activeMinutes != null || afterTrack) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFDC2626).copy(alpha = 0.16f))
                        .clickable { PlayerPlatform.host.cancelSleepTimer() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Turn Off Timer",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFEF4444),
                    )
                }
            }
        }
    }
}

