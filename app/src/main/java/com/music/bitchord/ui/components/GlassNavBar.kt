/*
 * The bar's structure and its inline/expanded behaviour are
 * EchoMusicApp/Echo-Music's AppFloatingNavBar + FloatingMiniPlayer (GPL-3.0),
 * over the FloatingTabBar vendored in [com.music.bitchord.ui.components.floatingtabbar].
 * BitChord's own tabs, song model, transport and haptics are wired through it in
 * place of Echo's Screens routing and PlayerConnection.
 */
@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.music.bitchord.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.bitchord.R
import com.music.bitchord.data.model.ROW_ART_PX
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.artworkAt
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.playback.MusicOnlyPlayback
import com.music.bitchord.ui.components.floatingtabbar.FloatingTabBarScrollConnection
import com.music.bitchord.ui.haptics.Haptic
import com.music.bitchord.ui.haptics.rememberHaptics
import com.music.bitchord.ui.player.AndroidMusicVideo
import com.music.bitchord.ui.player.PlayerDock
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Real ozyern/exhale Liquid Glass Navigation Bar & Floating Mini-Player.
 *
 * Visual & Motion Craft:
 * - Floating Mini-Player positioned cleanly 8.dp above the tab bar (zero overlap).
 * - All 4 tabs (Home, Explore, Library, Search) rendered with fluid spring-driven sliding glass pill.
 * - Liquid stretch (STRETCH) & counter-squash (SQUASH) as the pill glides across tabs.
 * - Drag-gesture support across the bar.
 * - Solid/tonal fallback with all 4 tabs when Liquid Glass is toggled OFF (NEVER collapses to a 2-icon stub).
 */
@Composable
fun GlassNavBar(
    tabs: List<BottomTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    scrollConnection: FloatingTabBarScrollConnection? = null,
    song: Song?,
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onExpand: () -> Unit,
    /** @see com.music.bitchord.data.listentogether.ListenTogether.State.controlsLocked */
    controlsLocked: Boolean = false,
    onBlockedControl: () -> Unit = {},
    /** Where the player's artwork lands when it closes into this bar. */
    dock: PlayerDock? = null,
    /** Dragging the bar's player up into the full player. */
    pull: MiniPlayerPull? = null,
    modifier: Modifier = Modifier,
    onTabLongClick: ((Int) -> Unit)? = null,
) {
    val pillShape = remember { RoundedCornerShape(percent = 50) }
    val contentColor = glassContentColor()
    val useGlass = LocalLiquidGlassEnabled.current && isGlassSupported()
    val reduceDynamicBlur by AppSettings.reduceDynamicBlur.collectAsStateWithLifecycle()
    val container = MaterialTheme.colorScheme.surfaceContainer

    Column(
        modifier = modifier
            .windowInsetsPadding(floatingBarInsets)
            .padding(horizontal = NAV_BAR_GUTTER)
            .padding(bottom = NAV_BAR_LIFT)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Floating MiniPlayer (cleanly above tab bar with 8.dp spacer, zero overlap)
        song?.let { currentSong ->
            val miniPlayerShape = pillShape
            val miniPlayerModifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(miniPlayerShape)
                .then(
                    if (useGlass && !reduceDynamicBlur) {
                        Modifier.liquidGlass(shape = miniPlayerShape)
                    } else {
                        Modifier
                            .background(container)
                            .border(1.dp, Color.White.copy(alpha = 0.12f), miniPlayerShape)
                    }
                )

            GlassNowPlaying(
                song = currentSong,
                isInline = false,
                isPlaying = isPlaying,
                isLoading = isLoading,
                contentColor = contentColor,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onExpand = onExpand,
                controlsLocked = controlsLocked,
                onBlockedControl = onBlockedControl,
                dock = dock,
                pull = pull,
                modifier = miniPlayerModifier,
            )

            Spacer(Modifier.height(8.dp))
        }

        // 2. Liquid Glass Tab Bar (ALL 4 Tabs: Home, Explore, Library, Search)
        // With fluid spring-driven sliding/morphing pill animation across tabs
        val reduceAnimation by AppSettings.reduceAnimation.collectAsStateWithLifecycle()
        val glassSpec: AnimationSpec<Float> = if (reduceAnimation) snap() else GlassSpring

        var dragOffset by remember { mutableFloatStateOf(0f) }
        val haptics = rememberHaptics()
        val density = LocalDensity.current
        val currentSelectedIndex by rememberUpdatedState(selectedIndex)

        var rowSize by remember { mutableStateOf(IntSize.Zero) }
        val gapPx = with(density) { 6.dp.toPx() }
        val n = tabs.size

        val tabWidthPx = if (rowSize.width > 0 && n > 0) {
            (rowSize.width - gapPx * (n - 1)) / n
        } else 0f
        val tabStepPx = if (rowSize.width > 0 && n > 0) {
            (rowSize.width + gapPx) / n
        } else 0f

        val currentTabStepPx by rememberUpdatedState(tabStepPx)
        val currentLastIndex by rememberUpdatedState(tabs.lastIndex)
        val currentOnTabSelected by rememberUpdatedState(onTabSelected)

        val pillTargetPx = if (tabStepPx > 0f) {
            selectedIndex * tabStepPx + dragOffset
        } else 0f

        val animatedPillOffset by animateFloatAsState(
            targetValue = pillTargetPx,
            animationSpec = glassSpec,
            label = "classicTabPillOffset",
        )

        val lag = if (tabStepPx > 0f) {
            (abs(pillTargetPx - animatedPillOffset) / tabStepPx).coerceIn(0f, 1f)
        } else 0f

        var lastHapticTab by remember { mutableIntStateOf(selectedIndex) }

        LaunchedEffect(selectedIndex) {
            dragOffset = 0f
            lastHapticTab = selectedIndex
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(pillShape)
                .then(
                    if (useGlass && !reduceDynamicBlur) {
                        Modifier.liquidGlass(shape = pillShape)
                    } else {
                        Modifier
                            .background(container)
                            .border(1.dp, Color.White.copy(alpha = 0.12f), pillShape)
                    }
                )
                .padding(horizontal = PILL_INSET, vertical = PILL_INSET),
        ) {
            // Fluid Sliding Glass / Solid Selection Pill
            if (tabWidthPx > 0f) {
                Box(
                    modifier = Modifier
                        .width(with(density) { tabWidthPx.toDp() })
                        .height(with(density) { rowSize.height.toDp() })
                        .graphicsLayer {
                            translationX = animatedPillOffset
                            scaleX = 1f + lag * STRETCH
                            scaleY = 1f - lag * STRETCH * SQUASH
                        }
                        .clip(pillShape)
                        .then(
                            if (useGlass) {
                                Modifier
                                    .background(Color.White.copy(alpha = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) 0.35f else 0.16f))
                                    .border(1.dp, Color.White.copy(alpha = 0.22f), pillShape)
                            } else {
                                Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                            }
                        ),
                )
            }

            // Tabs Row (ALL 4 Tabs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { rowSize = it }
                    .pointerInput(Unit) {
                        var totalDrag = 0f
                        var dragStartIndex = currentSelectedIndex
                        detectHorizontalDragGestures(
                            onDragStart = {
                                totalDrag = 0f
                                dragStartIndex = currentSelectedIndex
                            },
                            onDragCancel = { dragOffset = 0f },
                            onDragEnd = {
                                val step = currentTabStepPx
                                if (step > 0f) {
                                    val absoluteIndexFloat = (dragStartIndex + totalDrag / step)
                                        .coerceIn(0f, currentLastIndex.toFloat())
                                    val newIndex = absoluteIndexFloat.roundToInt()
                                    if (newIndex != currentSelectedIndex) {
                                        currentOnTabSelected(newIndex)
                                    }
                                }
                                dragOffset = 0f
                            },
                            onHorizontalDrag = { _, delta ->
                                val step = currentTabStepPx
                                if (step <= 0f) return@detectHorizontalDragGestures
                                totalDrag += delta
                                val absoluteIndexFloat = (dragStartIndex + totalDrag / step)
                                    .coerceIn(0f, currentLastIndex.toFloat())
                                dragOffset = absoluteIndexFloat * step - currentSelectedIndex * step

                                val approxTab = absoluteIndexFloat.roundToInt()
                                if (approxTab != lastHapticTab) {
                                    haptics.play(Haptic.Tick)
                                    lastHapticTab = approxTab
                                }
                            },
                        )
                    },
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val adaptiveTint = if (useGlass) contentColor else null
                tabs.forEachIndexed { index, tab ->
                    BottomBarItem(
                        tab = tab,
                        selected = index == selectedIndex,
                        glassSpec = glassSpec,
                        selectedTint = adaptiveTint,
                        unselectedTint = adaptiveTint?.copy(alpha = 0.65f),
                        onClick = {
                            if (index != selectedIndex) haptics.play(Haptic.Select)
                            onTabSelected(index)
                        },
                        onLongClick = { onTabLongClick?.invoke(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * The now playing controls docked into [GlassNavBar] as its accessory.
 *
 * Two densities of the same row rather than two components, so the shared
 * element carrying it between the bar's states has one thing to interpolate:
 * [isInline] drops the skip button and draws the play button smaller. The
 * artwork and the type are the same at both, as they are in Apple's accessory,
 * which only changes width between its two placements.
 *
 * The content is [MiniPlayer]'s — same artwork, same transport, same haptics —
 * and not Echo's, which reaches into a player connection this app doesn't have.
 * The press response is Echo's, and belongs to the glass rather than the row:
 * a surface you can push on is the whole point of the material.
 */
@Composable
private fun GlassNowPlaying(
    song: Song,
    isInline: Boolean,
    isPlaying: Boolean,
    isLoading: Boolean,
    contentColor: Color,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onExpand: () -> Unit,
    controlsLocked: Boolean,
    onBlockedControl: () -> Unit,
    dock: PlayerDock?,
    pull: MiniPlayerPull?,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberHaptics()
    val pressSource = remember { MutableInteractionSource() }
    val isPressed by pressSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 1.04f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "accessoryPressScale",
    )

    val titleStyle = MaterialTheme.typography.titleMedium.copy(
        fontSize = ACCESSORY_TITLE_SIZE,
        fontWeight = ACCESSORY_TITLE_WEIGHT,
        letterSpacing = ACCESSORY_TITLE_TRACKING,
    )
    val artistStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = ACCESSORY_ARTIST_SIZE,
        letterSpacing = ACCESSORY_ARTIST_TRACKING,
    )
    val glyphSlot = if (isInline) 32.dp else 40.dp
    val glyphSize = if (isInline) 24.dp else EXPANDED_GLYPH_SIZE

    Box(
        // Inline, the accessory is stretched to the row's height by the tab bar
        // (`fillMaxHeight` on a row measured at IntrinsicSize.Max), and this row
        // is shorter than that — the tab pill beside it, with a 28dp glyph in
        // 10dp of padding, is the tallest thing in the row and sets the height.
        // A Box defaults to TopStart, so the artwork and the transport sat a
        // couple of dp above the pill's centre line. Expanded the Box wraps its
        // content, so centring is a no-op there.
        contentAlignment = Alignment.Center,
        // The gestures and the tap belong to the whole glass surface, not the
        // row centred in it. Inline, that row is shorter than the pill it sits
        // in, and a pull started in the strip above or below it went nowhere —
        // on a bar only 48dp tall to begin with.
        modifier = Modifier
            // Outside the press scale, which it lifts along with everything else.
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
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .then(modifier)
            .clickable(
                interactionSource = pressSource,
                indication = null,
                onClick = onExpand,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    // The artwork sits in the end of a capsule, which curves
                    // away from it; at 8dp its corner met the curve.
                    start = 16.dp,
                    end = if (isInline) 8.dp else 12.dp,
                    // Expanded, the 40dp play button and these come to the 48dp
                    // Apple's accessory is.
                    top = 4.dp,
                    bottom = 4.dp,
                ),
        ) {
            AnimatedContent(
                targetState = song,
                transitionSpec = {
                    (fadeIn(tween(190)) + slideInVertically { it / 2 }) togetherWith
                        (fadeOut(tween(130)) + slideOutVertically { -it / 2 }) using
                        SizeTransform(clip = false) { _, _ -> snap() }
                },
                label = "glassNowPlayingTrack",
                modifier = Modifier.weight(1f),
            ) { currentSong ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val showsPicture = currentSong.isVideo && MusicOnlyPlayback.showsPicture(currentSong.videoId)
                    val artWidth by animateDpAsState(
                        targetValue = if (showsPicture && !VideoThumbnails.square) ACCESSORY_ART_SIZE * (16f / 9f) else ACCESSORY_ART_SIZE,
                        label = "glassMiniArtWidth",
                    )
                    Box(
                        modifier = Modifier
                            .size(width = artWidth, height = ACCESSORY_ART_SIZE)
                            .playerDockArt(dock, ACCESSORY_ART_CORNER)
                            .clip(RoundedCornerShape(ACCESSORY_ART_CORNER))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        AsyncImage(
                            model = rememberRemoteArtworkUrl(currentSong)?.artworkAt(ROW_ART_PX),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        if (showsPicture) {
                            AndroidMusicVideo(
                                videoId = currentSong.videoId,
                                fit = false,
                                onRenderedChanged = {},
                                modifier = Modifier.fillMaxSize(),
                                priority = 0,
                            )
                        }
                    }
                    Spacer(Modifier.width(if (isInline) 8.dp else 9.dp))
                    Column(Modifier.weight(1f)) {
                        val secondary = contentColor.copy(alpha = 0.7f)
                        ExplicitSongTitle(
                            song = currentSong,
                            style = titleStyle,
                            color = contentColor,
                            badgeColor = secondary,
                            fadeOverflow = true,
                        )
                        FadingLine(
                            text = currentSong.artist,
                            style = artistStyle,
                            color = secondary,
                        )
                    }
                }
            }
            if (isLoading) {
                Box(Modifier.size(glyphSlot), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = contentColor,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(if (isInline) 18.dp else 20.dp),
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        haptics.play(if (isPlaying) Haptic.Pause else Haptic.Resume)
                        onPlayPause()
                    },
                    modifier = Modifier.size(glyphSlot),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
                        tint = contentColor,
                        modifier = Modifier.size(glyphSize),
                    )
                }
            }
            // Dropped inline: the collapsed row is sharing its width with the
            // tab pill and the Search circle, and the title is what has to
            // survive that, not a second transport button.
            if (!isInline) {
                // Glyph centres 44dp apart, as Apple's are.
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        haptics.play(Haptic.SkipNext)
                        onNext()
                    },
                    enabled = !controlsLocked,
                    modifier = Modifier.size(glyphSlot),
                ) {
                    MiniPlayerNextGlyph(
                        tint = contentColor.copy(alpha = if (controlsLocked) 0.3f else 1f),
                    )
                }
            }
        }
    }
}

/** The collapsed row's margin at either side of the screen, and its items' spacing. */
private val INLINE_GUTTER = 28.dp
private val INLINE_SPACING = 14.dp

/**
 * Half the difference between the expanded tab row, 62dp, and the collapsed
 * one, 48 — a 28dp glyph in 10dp of padding — so the one folds onto the
 * other's centre line.
 */
private val INLINE_LIFT = 7.dp

/**
 * The accessory's type, measured off Apple's, collapsed and expanded alike:
 * 13pt over 12pt, set looser than the scale's 16 and 14 as SF Pro Text spaces
 * these sizes — the title less so than measured, tuned by eye on device: in
 * Semibold +0.4 read loose and the cut's own spacing tight.
 */
private val ACCESSORY_TITLE_SIZE = 13.sp

/**
 * W700, which in this app's family draws the Semibold cut (see SFProDisplay):
 * Apple's accessory sets its title in Semibold, and the scale's W600 — the
 * Medium cut — read thin at 13sp.
 */
private val ACCESSORY_TITLE_WEIGHT = FontWeight.W700
private val ACCESSORY_TITLE_TRACKING = 0.15.sp
private val ACCESSORY_ARTIST_SIZE = 12.sp
private val ACCESSORY_ARTIST_TRACKING = 0.35.sp

/** The accessory's artwork, about Apple's 30pt at both densities, and its corner. */
private val ACCESSORY_ART_SIZE = 30.4.dp
private val ACCESSORY_ART_CORNER = 5.7.dp

/** The expanded play glyph: its triangle about Apple's 15pt tall. */
private val EXPANDED_GLYPH_SIZE = 29.dp
