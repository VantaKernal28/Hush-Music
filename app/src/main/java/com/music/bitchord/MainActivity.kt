package com.music.bitchord

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.session.MediaController
import com.music.bitchord.data.LocalGroups
import com.music.bitchord.data.OfflinePlaylists
import com.music.bitchord.data.listentogether.JamInviteLink
import com.music.bitchord.data.model.LikeStatus
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.LibrarySort
import com.music.bitchord.data.settings.LibraryViewType
import com.music.bitchord.data.settings.LocalMusicSort
import com.music.bitchord.data.settings.SongSort
import com.music.bitchord.data.settings.ThemeMode
import com.music.bitchord.download.Downloads
import com.music.bitchord.playback.AudioCache
import com.music.bitchord.playback.MusicLink
import com.music.bitchord.playback.PlayerDeepLink
import com.music.bitchord.playback.QualityUpgrade
import com.music.bitchord.playback.rememberMediaController
import com.music.bitchord.playback.rememberPlayerState
import com.music.bitchord.ui.LocalMaterialUiMode
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.components.LocalAppBackdrop
import com.music.bitchord.ui.components.LocalLiquidGlassEnabled
import com.music.bitchord.ui.components.backdrop.backdrops.LayerBackdrop
import com.music.bitchord.ui.components.backdrop.backdrops.rememberLayerBackdrop
import com.music.bitchord.ui.components.handleDacVolumeKey
import com.music.bitchord.ui.components.optimizedHazeEffect
import com.music.bitchord.ui.icons.BitChordIcons
import com.music.bitchord.ui.navigation.AppRoute
import com.music.bitchord.ui.performance.resolvePerformanceRefreshRate
import com.music.bitchord.ui.screens.localizedLabel
import com.music.bitchord.ui.theme.BitChordTheme
import com.music.bitchord.ui.theme.SystemBarIcons
import com.music.bitchord.ui.utils.rememberIosOverscrollFactory
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import java.util.Locale

/** A full first screen of a native YouTube Music radio before AutoPlay tops it up. */
/** How much of an artist page's header scrolls away before the top fade begins. */
internal const val TOP_FADE_START = 0.55f

/** How much of the header's height the fade takes to reach full strength. */
internal const val TOP_FADE_RAMP = 0.3f

internal const val INITIAL_RADIO_TRACKS = 24

internal fun shouldSkipAfterDislike(
    previousStatus: LikeStatus,
    targetVideoId: String,
    currentVideoId: String?,
): Boolean = previousStatus != LikeStatus.DISLIKE && targetVideoId == currentVideoId


class MainActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(com.music.bitchord.ui.components.AppLocaleHelper.wrapBaseContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Before the composition, so a cold launch from a widget's artwork has
        // the request already standing by the time BitChordApp first reads it.
        PlayerDeepLink.consume(intent)
        JamInviteLink.consume(intent)
        // Likewise for a link tapped or shared from another app — see [MusicLink].
        MusicLink.consume(intent)
        setContent {
            val theme by AppSettings.themeMode.collectAsStateWithLifecycle()
            val appFont by AppSettings.appFont.collectAsStateWithLifecycle()
            val highPerformance by AppSettings.highPerformanceMode.collectAsStateWithLifecycle()
            val liquidGlassEnabled by AppSettings.liquidGlass.collectAsStateWithLifecycle()
            val iosOverscrollFactory = rememberIosOverscrollFactory()
            val performanceRefreshRate by AppSettings.performanceRefreshRate.collectAsStateWithLifecycle()
            val composeView = LocalView.current
            LaunchedEffect(highPerformance, performanceRefreshRate, composeView) {
                applyPerformanceMode(highPerformance, performanceRefreshRate, composeView)
            }
            val darkTheme = when (theme) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            BitChordTheme(darkTheme = darkTheme, font = appFont) {
                // The glass surfaces sample this layer, and a layer records only
                // what is drawn into it — which, for BitChord, is a page that
                // paints no background of its own. Everywhere a page is not
                // showing artwork the recording is transparent, so the glass had
                // nothing to blur there and you saw straight through it to the
                // sharp page underneath: album art came through the bar blurred
                // and text came through it untouched. The window's background is
                // the floor the pages have always been drawn against, so it is
                // laid down here too and the recording is opaque like the screen.
                val windowBackground = MaterialTheme.colorScheme.background
                val paintBackdrop: ContentDrawScope.() -> Unit = remember(windowBackground) {
                    {
                        drawRect(windowBackground)
                        drawContent()
                    }
                }
                val appBackdrop = rememberLayerBackdrop(onDraw = paintBackdrop)
                val materialUiMode by AppSettings.materialUiMode.collectAsStateWithLifecycle()
                CompositionLocalProvider(
                    LocalOverscrollFactory provides iosOverscrollFactory,
                    LocalLiquidGlassEnabled provides (liquidGlassEnabled && !materialUiMode),
                    LocalAppBackdrop provides appBackdrop,
                    LocalMaterialUiMode provides materialUiMode,
                ) {
                // The window's width, measured rather than asked for.
                //
                // `Configuration.screenWidthDp` is the wrong question here: in a
                // freeform or desktop window it can report the display rather
                // than the window it is actually in, and it lands a beat late
                // when that window is dragged. The layout downstream splits in
                // two on the strength of this number and sizes both halves from
                // it, so a stale one is a player pane sized for a window that no
                // longer exists and a page squeezed to a sliver to pay for it.
                // A measured constraint cannot be stale — it is the very width
                // the split is about to be laid out in.
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    BitChordApp(
                        darkTheme = darkTheme,
                        windowWidth = maxWidth,
                        windowHeight = maxHeight,
                        appBackdrop = appBackdrop,
                    )
                }
                }
            }
        }
    }

    /**
     * The volume keys move the DAC's own volume while direct USB output owns
     * it: Android's music stream no longer reaches that DAC, so the system
     * volume would change nothing anyone hears. Handled here rather than left
     * to the media session so the app's own HUD answers instead of Android's
     * remote-volume panel; in the background the session takes them. A sheet
     * in a window of its own asks the same through [DacVolumeKeys].
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        handleDacVolumeKey(event) || super.dispatchKeyEvent(event)

    /**
     * Requests a window refresh rate without forcing a display mode or
     * resolution. Android may still lower it for temperature, battery state or
     * hardware limits, which is why Settings describes this as a preference.
     */
    private fun applyPerformanceMode(enabled: Boolean, refreshRate: Int, composeView: View) {
        val supportedRefreshRate = composeView.display.resolvePerformanceRefreshRate(refreshRate)
        window.attributes = window.attributes.apply {
            preferredRefreshRate = if (enabled) supportedRefreshRate.toFloat() else 0f
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.setFrameRatePowerSavingsBalanced(!enabled)
            composeView.requestedFrameRate = if (enabled) {
                supportedRefreshRate.toFloat()
            } else {
                View.REQUESTED_FRAME_RATE_CATEGORY_DEFAULT
            }
        }
    }

    /**
     * The other half of the relay. This activity is `singleTask`, so once it is
     * running a second tap on the widget does not rebuild anything — it arrives
     * here, and [onCreate] never runs again.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Replaces what getIntent() returns, so the extra this consumes is the
        // one that just arrived and not the one the task was started with.
        setIntent(intent)
        PlayerDeepLink.consume(intent)
        JamInviteLink.consume(intent)
        MusicLink.consume(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BitChordApp(
    darkTheme: Boolean,
    /** The width of the window this is laid out in — see the call site. */
    windowWidth: Dp,
    /**
     * The window's height, measured the same way and for the same reason as
     * [windowWidth] — and needed alongside it for exactly one thing: telling
     * a portrait window apart from a landscape one. Width alone can't; a
     * big tablet's portrait width comfortably clears a phone's landscape
     * width, so the two-column player (see [landscapePlayerAvailable])
     * would fire in portrait too if it only ever asked about width.
     */
    windowHeight: Dp,
    appBackdrop: LayerBackdrop,
    viewModel: MainViewModel = viewModel(),
) {
    val chrome = rememberMainChromeState(appBackdrop)
    val overlays = rememberMainOverlayState()
    val nav = rememberMainNavState(viewModel)
    val appForeground = com.music.bitchord.ui.rememberIsForeground()
    val artworkMotion = appForeground && !overlays.showNowPlaying
    val playerDock = rememberPlayerDockState(overlays)
    val feeds = rememberMainFeedState(
        viewModel = viewModel,
        overlays = overlays,
        nav = nav,
        appForeground = appForeground,
    )

    SystemBarIcons(
        dark = !darkTheme && !overlays.showNowPlaying && nav.route != AppRoute.Replay &&
            overlays.replayStory == null && !feeds.onboardingVisible && !overlays.showDiwali,
    )

    val controller = rememberMediaController()
    val player = rememberPlayerState(controller)
    val scroll = rememberMainScrollState(
        viewModel = viewModel,
        nav = nav,
        overlays = overlays,
        selectedMoodGenre = feeds.selectedMoodGenre,
        hasPlayerSong = player.song != null,
        materialUiMode = chrome.materialUiMode,
    )
    val playback = rememberMainPlaybackActions(
        controller = controller,
        player = player,
        viewModel = viewModel,
        overlays = overlays,
        feeds = feeds,
        scroll = scroll,
        playerDock = playerDock,
    )
    val downloads = rememberMainDownloadActions(
        viewModel = viewModel,
        overlays = overlays,
        feeds = feeds,
        nav = nav,
        player = player,
    )

    MainNavigationAndPromptEffects(
        overlays = overlays,
        nav = nav,
        feeds = feeds,
        playerDock = playerDock,
        appForeground = appForeground,
        viewModel = viewModel,
    )
    MainStorageAndFeedEffects(
        overlays = overlays,
        nav = nav,
        feeds = feeds,
        scroll = scroll,
        viewModel = viewModel,
    )

    val playerSong = rememberResolvedPlayerSong(player.song, overlays)

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Nothing of the page under a portrait player that is fully up:
            // that player is its own window, opaque from edge to edge, and
            // this one drawing on behind it — the page, the glass bar and the
            // blurs that sample it, a now-playing row's bars — is GPU time
            // taken from the player on every frame for nothing anyone can see.
            // The same test the player's own dim skips itself on. Composition
            // and layout carry on, so the page is current the moment the
            // player starts to come down and this draws again.
            .drawWithContent {
                if (!playerDock.coveredByPlayer) drawContent()
            },
    ) {
        MainBackHandlers(nav = nav, feeds = feeds, overlays = overlays, viewModel = viewModel)
        MainContentShell(
            windowWidth = windowWidth,
            artworkMotion = artworkMotion,
            chrome = chrome,
            overlays = overlays,
            nav = nav,
            feeds = feeds,
            scroll = scroll,
            playerDock = playerDock,
            playback = playback,
            downloads = downloads,
            viewModel = viewModel,
        )
        PageBackdropEffect(overlays = overlays, chrome = chrome, playerSong = playerSong)
        NowPlayingSheetHost(
            playerSong = playerSong,
            windowWidth = windowWidth,
            windowHeight = windowHeight,
            overlays = overlays,
            chrome = chrome,
            nav = nav,
            feeds = feeds,
            scroll = scroll,
            playerDock = playerDock,
            playback = playback,
            downloads = downloads,
            viewModel = viewModel,
        )
        ReplayOverlaysHost(overlays = overlays, feeds = feeds)
        TrackContextMenuHost(
            playerSong = playerSong,
            overlays = overlays,
            chrome = chrome,
            nav = nav,
            feeds = feeds,
            playerDock = playerDock,
            playback = playback,
            downloads = downloads,
            viewModel = viewModel,
        )
        PlaylistAndMixModalsHost(
            overlays = overlays,
            chrome = chrome,
            nav = nav,
            feeds = feeds,
            playback = playback,
            viewModel = viewModel,
        )
        BrowseContextMenuHost(
            overlays = overlays,
            chrome = chrome,
            nav = nav,
            feeds = feeds,
            playback = playback,
            downloads = downloads,
            viewModel = viewModel,
        )
        OnboardingAndSignInHost(
            overlays = overlays,
            nav = nav,
            feeds = feeds,
            viewModel = viewModel,
        )
        SettingsAndAccountDialogsHost(
            overlays = overlays,
            chrome = chrome,
            nav = nav,
            feeds = feeds,
            viewModel = viewModel,
        )
        IntegrationAndAlertDialogsHost(
            playerSong = playerSong,
            overlays = overlays,
            chrome = chrome,
        )
        DisplayAndTutorialOverlaysHost(
            playerSong = playerSong,
            overlays = overlays,
            chrome = chrome,
            feeds = feeds,
            playerDock = playerDock,
            playback = playback,
            viewModel = viewModel,
        )
    }
}

/**
 * The addon exclusions already explained this session, as `configId:reason` —
 * see [SourceRegistry.addonsLeftOutOfDownloads]. Process-wide rather than
 * remembered by the composition, so turning the phone doesn't say it again.
 */
internal val explainedDownloadExclusions = java.util.Collections.synchronizedSet(mutableSetOf<String>())

/** UIKit's sheet curve: leaves at once, settles long — a sheet let go of, not pushed. */
internal val OnboardingDismissEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

internal fun tween(durationMillis: Int) =
    androidx.compose.animation.core.tween<Float>(durationMillis)

@Composable
internal fun LibrarySort.localizedLabel(): String = when (this) {
    LibrarySort.DEFAULT -> stringResource(R.string.sort_default)
    LibrarySort.TITLE_ASC -> stringResource(R.string.sort_title_ascending)
    LibrarySort.TITLE_DESC -> stringResource(R.string.sort_title_descending)
}

/**
 * The track-list sort menu, styled after the account switcher: a full-screen
 * scrim to catch the dismissal tap, and the options on a frosted panel that
 * blurs the page behind it. Composed here in the main hierarchy rather than
 * as a popup window — which is exactly what lets the haze see the content it
 * is blurring.
 */
@Composable
internal fun FrostedSortMenu(
    hazeState: HazeState,
    selected: SongSort,
    onSelect: (SongSort) -> Unit,
    onFlipDateDirection: () -> Unit,
    onDismiss: () -> Unit,
) {
    FrostedMenuPanel(hazeState = hazeState, onDismiss = onDismiss) {
        // Date added is one row, Spotify-style: the arrow on it shows
        // the direction — up for newest first, down for oldest — and
        // tapping flips it, the rotation animating the flip. Up is
        // also where a fresh activation lands, newest first being the
        // point of the feature.
        val dateActive = selected == SongSort.DATE_ADDED_ASC ||
            selected == SongSort.DATE_ADDED_DESC
        val arrowRotation by animateFloatAsState(
            targetValue = if (selected == SongSort.DATE_ADDED_ASC) 180f else 0f,
            animationSpec = tween(durationMillis = 200),
            label = "dateAddedArrow",
        )
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button) { onFlipDateDirection() }
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.sort_date_added_toggle),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            if (dateActive) {
                Icon(
                    Icons.Rounded.ArrowUpward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.rotate(arrowRotation),
                )
            }
        }
        SongSort.entries
            .filter { it != SongSort.DATE_ADDED_ASC && it != SongSort.DATE_ADDED_DESC }
            .forEach { option ->
                FrostedMenuRow(
                    label = option.localizedLabel(),
                    checked = option == selected,
                    onClick = { onSelect(option) },
                )
            }
    }
}

/**
 * A device folder's menu, off the same button: list or grid for its tabs, then
 * its sort orders — the Files app's arrangement, view first and order under it.
 */
@Composable
internal fun FrostedFolderMenu(
    hazeState: HazeState,
    sort: LocalMusicSort,
    viewType: LibraryViewType,
    onSort: (LocalMusicSort) -> Unit,
    onToggleViewType: () -> Unit,
    onDismiss: () -> Unit,
) {
    FrostedMenuPanel(hazeState = hazeState, onDismiss = onDismiss) {
        val grid = viewType == LibraryViewType.GRID
        FrostedMenuRow(
            label = stringResource(if (grid) R.string.switch_to_list_view else R.string.switch_to_grid_view),
            checked = false,
            onClick = onToggleViewType,
            icon = if (grid) BitChordIcons.ListView else BitChordIcons.GridView,
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
        LocalMusicSort.entries.forEach { option ->
            FrostedMenuRow(
                label = option.localizedLabel(),
                checked = option == sort,
                onClick = { onSort(option) },
            )
        }
    }
}

/** The scrim and frosted panel both top-bar menus are drawn on. */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun FrostedMenuPanel(
    hazeState: HazeState,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val reduceDynamicBlur by AppSettings.reduceDynamicBlur.collectAsStateWithLifecycle()
    val shape = MaterialTheme.shapes.extraLarge
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = .48f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.TopEnd,
    ) {
        Surface(
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = shape,
            modifier = Modifier
                .padding(top = 56.dp, end = 20.dp)
                .width(IntrinsicSize.Max)
                .clip(shape)
                .then(
                    if (reduceDynamicBlur) {
                        Modifier.background(MaterialTheme.colorScheme.surface)
                    } else {
                        Modifier.optimizedHazeEffect(
                            state = hazeState,
                            style = HazeMaterials.thin(MaterialTheme.colorScheme.surface),
                        )
                    },
                )
                .clickable(onClick = {}),
        ) {
            Column(Modifier.padding(vertical = 8.dp), content = content)
        }
    }
}

/** One option on a frosted menu: its label, and a tick or [icon] at the end. */
@Composable
private fun FrostedMenuRow(
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (checked) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        } else if (icon != null) {
            Spacer(Modifier.width(16.dp))
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SongSort.localizedLabel(): String = when (this) {
    SongSort.DEFAULT -> stringResource(R.string.sort_default)
    SongSort.TITLE_ASC -> stringResource(R.string.sort_title_ascending)
    SongSort.TITLE_DESC -> stringResource(R.string.sort_title_descending)
    SongSort.DATE_ADDED_ASC -> stringResource(R.string.sort_date_added_oldest)
    SongSort.DATE_ADDED_DESC -> stringResource(R.string.sort_date_added)
}

/**
 * Whether this page id is one of the device folders — `local:downloads`,
 * `local:all` and the rest, the pages with Songs / Artists / Albums tabs.
 *
 * Asked rather than `startsWith("local:")` because that prefix covers unlike
 * pages: a folder, one downloaded playlist, and an album or artist grouped out
 * of a folder — the last two plain track listings that want the same chrome
 * every other release page gets. See [Downloads.PLAYLIST_PREFIX] and
 * [LocalGroups.PREFIX] for why they share a namespace at all.
 */
internal fun String?.isDeviceFolder(): Boolean =
    this != null && startsWith("local:") &&
        !startsWith(Downloads.PLAYLIST_PREFIX) && !startsWith(LocalGroups.PREFIX) &&
        !startsWith(OfflinePlaylists.PREFIX)

/**
 * Whether [videoId] is the track playing, and is known to be playing YouTube's
 * own copy — which decides whether the player menu leads with "Revert to
 * original" or with "Upgrade quality".
 *
 * See [QualityUpgrade.isKnownToBePlayingYouTubesOwn] for why "known" is doing
 * real work here: an unresolved track playing off the disk cache is judged by
 * the origin its cache entry was tagged with, and keeps the revert on offer
 * only when the entry has none.
 */
internal fun playingYouTubesOwn(videoId: String, controller: MediaController?): Boolean {
    val item = controller?.currentMediaItem?.takeIf { it.mediaId == videoId } ?: return false
    val uri = item.localConfiguration?.uri
    return QualityUpgrade.isKnownToBePlayingYouTubesOwn(
        videoId,
        uri?.let(QualityUpgrade::cacheTag),
        uri?.let(AudioCache::originOf),
    )
}

/** `M:SS`/`H:MM:SS`, the same shape [String?.durationMillis] parses back. */
internal fun formatDurationText(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(Locale.ROOT, minutes, seconds)
}

/**
 * How far short of the end a seek is allowed to land.
 *
 * Seeking to the final millisecond is indistinguishable from the track running
 * out, so it starts the next song — which is not what anyone dragging to the end
 * of the bar, or tapping the last line of a lyric, is asking for. A second back
 * from the end plays the outro instead.
 */
internal const val SEEK_END_GUARD_MS = 1_000L

/**
 * How far a detail page scrolls before its title moves up into the bar.
 *
 * Roughly the height of the sleeve and the credit stacked above the Play pair,
 * so the two titles hand over as the header one leaves rather than sitting on
 * screen together. The bar cross-fades over 220ms, which absorbs the difference
 * between that estimate and a particular page's real header.
 */
internal val DETAIL_TITLE_DROP = 320.dp

/**
 * How fast, a second, a mini-player pull has to be moving when the finger
 * lifts to decide open or closed on its own, wherever it got to — see
 * [MiniPlayerPull]. A flick is a whole gesture; only a slow pull is judged by
 * the distance it covered.
 */
internal val PLAYER_PULL_FLING_VELOCITY = 400.dp

/** M3's own scrim strength, which the player's dim stands in for. */
internal const val PLAYER_SCRIM_ALPHA = 0.32f

/**
 * [content] in a recompose scope of its own, so state read inside it
 * recomposes it alone rather than everything it is written in.
 */
@Composable
internal fun Isolated(content: @Composable () -> Unit) = content()

/** Longer than the ⋯ menu's exit, so its panel keeps its frost on the way out. */
internal const val SNAPSHOT_RELEASE_DELAY_MS = 400L

/**
 * The player sheet's shape: square, as [RectangleShape] was, but reaching a
 * whole sheet's height above the sheet's top edge.
 *
 * The sheet's Surface clips its content to this shape, and on the way down to
 * the mini player the artwork travels ahead of the sheet carrying it — up
 * above the sheet's top edge, where a plain rectangle cut it off mid-air,
 * leaving only its lower part to arrive in the bar. Nothing else is drawn
 * outside the sheet (its background is transparent and it casts no shadow),
 * so at rest this is the rectangle it replaces.
 */
internal object PlayerSheetShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Rectangle(Rect(0f, -size.height, size.width, size.height))
}

internal const val TAB_HOME = 0
internal const val TAB_EXPLORE = 1
internal const val TAB_LIBRARY = 2
internal const val TAB_SEARCH = 3
