package com.music.bitchord

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.DialogWindowProvider
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.music.bitchord.data.LocalGroups
import com.music.bitchord.data.YtMusicRepository
import com.music.bitchord.data.listentogether.ListenTogether
import com.music.bitchord.data.model.BrowseType
import com.music.bitchord.data.model.PlaybackSourceType
import com.music.bitchord.data.model.SearchFilter
import com.music.bitchord.data.model.SearchResult
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.scrobbling.primaryArtist
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.playback.QueueCoordinator
import com.music.bitchord.playback.setQueueDragActive
import com.music.bitchord.playback.toggleAutoplay
import com.music.bitchord.playback.toggleShuffle
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.components.DacVolumeKeys
import com.music.bitchord.ui.components.IosMenuPopover
import com.music.bitchord.ui.components.backdrop.backdrops.layerBackdrop
import com.music.bitchord.ui.components.backdrop.backdrops.snapshotBackdropSource
import com.music.bitchord.ui.navigation.AppRoute
import com.music.bitchord.ui.player.LocalPlayerDock
import com.music.bitchord.ui.player.NowPlayingScreen
import com.music.bitchord.ui.replay.ReplayStoryPage
import com.music.bitchord.ui.utils.guardSheetFromContentTouches
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Hosts the full-screen modal player sheet and its backdrop-blur popover menu. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NowPlayingSheetHost(
    playerSong: Song?,
    windowWidth: Dp,
    windowHeight: Dp,
    overlays: MainOverlayState,
    chrome: MainChromeState,
    nav: MainNavState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    playerDock: PlayerDockState,
    playback: MainPlaybackActions,
    downloads: MainDownloadActions,
    viewModel: MainViewModel,
) {
    if (!overlays.showNowPlaying || playerSong == null) return

    val sheetDensity = LocalDensity.current
    val dock = playerDock.playerDock
    val motion = playerDock.playerSheetMotion
    val confirmSheetValue: (SheetValue) -> Boolean = { value ->
        if (value == SheetValue.Hidden && !playerDock.reducePlayerMotion) {
            motion.close()
            false
        } else {
            true
        }
    }
    val nowPlayingSheetState = rememberSaveable(
        saver = SheetState.Saver(
            skipPartiallyExpanded = true,
            confirmValueChange = confirmSheetValue,
            density = sheetDensity,
            skipHiddenState = false,
        ),
    ) {
        SheetState(
            skipPartiallyExpanded = true,
            density = sheetDensity,
            initialValue = if (motion.holding) SheetValue.Expanded else SheetValue.Hidden,
            confirmValueChange = confirmSheetValue,
        )
    }
    DisposableEffect(nowPlayingSheetState) {
        motion.sheet = nowPlayingSheetState
        dock.sheetOffset = motion::position
        onDispose {
            dock.sheetOffset = null
            motion.onSheetGone()
        }
    }
    ModalBottomSheet(
        onDismissRequest = { overlays.showNowPlaying = false },
        sheetState = nowPlayingSheetState,
        scrimColor = if (playerDock.reducePlayerMotion) BottomSheetDefaults.ScrimColor else Color.Transparent,
        shape = PlayerSheetShape,
        containerColor = Color.Transparent,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        sheetMaxWidth = Dp.Unspecified,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = playerDock.reducePlayerMotion),
    ) {
        BackHandler(enabled = !playerDock.reducePlayerMotion) { motion.close() }
        val sheetWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        DacVolumeKeys()
        DisposableEffect(sheetWindow, playerDock.reducePlayerMotion) {
            if (!playerDock.reducePlayerMotion) motion.attachWindow(sheetWindow)
            onDispose { }
        }
        Box(
            Modifier
                .offset { IntOffset(0, motion.contentOffsetPx()) }
                .drawBehind {
                    val open = dock.openFraction()
                    if (!playerDock.reducePlayerMotion && open > 0f && !(open >= 1f && dock.attached)) {
                        val top = motion.position().takeUnless { it.isNaN() } ?: 0f
                        drawRect(
                            color = Color.Black.copy(alpha = PLAYER_SCRIM_ALPHA * open),
                            topLeft = Offset(0f, -top.toInt().toFloat()),
                            size = Size(size.width, size.height),
                        )
                    }
                }
                .guardSheetFromContentTouches(nowPlayingSheetState)
                .onSizeChanged {
                    dock.sheetTravel = it.height.toFloat()
                    motion.laidOut = true
                },
        ) {
            val playerMenuUp = overlays.songActions != null &&
                overlays.songMenuOrigin == null &&
                overlays.menuFromPlayer
            LaunchedEffect(playerMenuUp, chrome.reduceDynamicBlur) {
                if (!playerMenuUp || chrome.reduceDynamicBlur) {
                    delay(SNAPSHOT_RELEASE_DELAY_MS)
                    chrome.playerBackdrop.release()
                }
            }
            Box(
                when {
                    chrome.reduceDynamicBlur -> Modifier
                    chrome.playerLiveCanvas -> Modifier.layerBackdrop(chrome.livePlayerBackdrop)
                    else -> Modifier.snapshotBackdropSource(chrome.playerBackdrop)
                },
            ) {
                CompositionLocalProvider(LocalPlayerDock provides playerDock.activeDock) {
                    MainNowPlayingScreen(
                        song = playerSong,
                        windowWidth = windowWidth,
                        windowHeight = windowHeight,
                        overlays = overlays,
                        chrome = chrome,
                        feeds = feeds,
                        scroll = scroll,
                        dismissPlayer = playerDock.onDismiss,
                        playback = playback,
                        viewModel = viewModel,
                    )
                }
            }
            IosMenuPopover(
                item = overlays.songActions?.takeIf { overlays.songMenuOrigin == null && overlays.menuFromPlayer },
                anchor = overlays.songMenuAnchor,
                onDismiss = { overlays.songActions = null },
                backdrop = if (chrome.playerLiveCanvas) chrome.livePlayerBackdrop else chrome.playerBackdrop,
            ) { song ->
                SongMenuBody(
                    heldSong = song,
                    overlays = overlays,
                    nav = nav,
                    feeds = feeds,
                    dismissPlayer = playerDock.onDismiss,
                    playback = playback,
                    downloads = downloads,
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
internal fun MainNowPlayingScreen(
    song: Song,
    windowWidth: Dp,
    windowHeight: Dp,
    overlays: MainOverlayState,
    chrome: MainChromeState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    dismissPlayer: () -> Unit,
    playback: MainPlaybackActions,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val controller = playback.controller
    val player = playback.player
    val displayedSong = resolveDisplayedSong(song, playback.versions)
    val playedBy = resolvePlayedBy(feeds.partyState, displayedSong)

    NowPlayingScreen(
        song = displayedSong,
        playedBy = playedBy,
        accountName = feeds.account?.name,
        onDismissPlayer = dismissPlayer,
        windowWidth = windowWidth,
        windowHeight = windowHeight,
        menuOpen = { overlays.songActions != null && overlays.menuFromPlayer && overlays.songMenuOrigin == null },
        onLiveCanvasChanged = { chrome.playerLiveCanvas = it },
        onOpenMixer = { overlays.showSongMixer = true },
        isPlaying = player.isPlaying,
        isLoading = playback.playPauseBusy,
        position = player.position,
        durationMs = player.durationMs,
        audioVersionSwitching = playback.versions.switchingAudioVersion,
        qualityUpgraded = player.isQualityUpgraded,
        onPlayPause = { playback.togglePlayPause() },
        onNext = { controller?.seekToNextMediaItem() },
        onPrevious = { controller?.seekToPrevious() },
        onBlockedControl = playback.showHostOnlyNotice,
        onSeekFraction = { fraction -> seekByFraction(controller, fraction) },
        onSeek = { target -> seekToTimestamp(controller, target) },
        queue = player.queue,
        queueIndex = player.queueIndex,
        hasPrevious = player.hasPrevious,
        hasNext = player.hasNext,
        repeatMode = player.repeatMode,
        shuffleEnabled = feeds.shuffleEnabled,
        autoplayEnabled = feeds.autoplayEnabled,
        signedIn = feeds.signedIn,
        likeStatus = playback.likeStatusOf(song),
        onToggleLike = { playback.toggleLikeOf(song) },
        onToggleShuffle = { controller?.toggleShuffle() },
        onCycleRepeat = { cycleControllerRepeat(controller) },
        onToggleAutoplay = { controller?.toggleAutoplay() },
        onJumpTo = { index ->
            controller?.let { c ->
                c.seekTo(index, 0L)
                c.play()
            }
        },
        onRemoveFromQueue = { controller?.removeMediaItem(it) },
        onMoveInQueue = { from, to -> controller?.moveMediaItem(from, to) },
        onQueueDragActiveChange = { active -> controller?.setQueueDragActive(active) },
        onOpenMenu = {
            overlays.openSongMenuFromPlayer(
                song = song,
                captureSnapshot = !AppSettings.reduceDynamicBlur.value && !chrome.playerLiveCanvas,
                playerBackdrop = chrome.playerBackdrop,
            )
        },
        onOpenAlbum = { id ->
            dismissPlayer()
            viewModel.openDetail(
                id,
                song.albumName ?: song.title,
                song.artist,
                song.thumbnailUrl,
                BrowseType.ALBUM,
            )
        },
        onOpenArtist = { id, name ->
            openArtistFromPlayer(id, name, context, scope, dismissPlayer, viewModel)
        },
        onOpenPlaybackSource = {
            openPlaybackSourceFromPlayer(
                displayedSong = displayedSong,
                queueLabel = scroll.queueLabel,
                context = context,
                overlays = overlays,
                dismissPlayer = dismissPlayer,
                viewModel = viewModel,
            )
        },
        lyrics = feeds.lyrics,
        lyricsSource = feeds.lyricsSource,
        lyricsProviderStates = feeds.lyricsProviderStates,
        onSelectLyricsProvider = viewModel::selectLyricsProvider,
        lyricsUnavailable = feeds.lyricsChecked && feeds.lyrics.isNullOrEmpty(),
        lyricsOffsetOpen = overlays.showLyricsOffset,
        onDismissLyricsOffset = { overlays.showLyricsOffset = false },
        onListenTogether = {
            dismissPlayer()
            viewModel.navigator.pushAll(AppRoute.Settings, AppRoute.ListenTogether)
        },
        onClearQueue = {
            controller?.let { c -> QueueCoordinator.clearUserQueue(c) }
        },
    )
}

private fun resolveDisplayedSong(song: Song, versions: VersionSwitchState): Song {
    val effectiveSong = versions.optimisticVersionSong?.takeIf {
        it.videoId == versions.convertedAudioId ||
            it.videoId == versions.convertedVideoId ||
            it.videoId == versions.keepVideoId ||
            it.videoId == YtMusicRepository.cachedAudioVersion(song.videoId)?.videoId ||
            it.videoId == YtMusicRepository.cachedVideoVersion(song.videoId)?.videoId
    } ?: song
    return versions.activeRadioSeed
        ?.takeIf { (videoId, _) -> effectiveSong.radioName == null && videoId == effectiveSong.videoId }
        ?.let { (videoId, name) ->
            effectiveSong.copy(
                radioName = name,
                playbackSource = name,
                playbackSourceType = PlaybackSourceType.SHARED_LINK,
                playbackSourceId = videoId,
            )
        }
        ?: effectiveSong
}

private fun resolvePlayedBy(
    partyState: ListenTogether.State,
    displayedSong: Song,
): String? = partyState
    .takeIf { it.inParty && it.playback.track?.videoId == displayedSong.videoId }
    ?.playback
    ?.let { playback ->
        playback.startedByName?.takeIf(String::isNotBlank)
            ?: partyState.members.firstOrNull { it.memberId == playback.startedBy }
                ?.displayName?.takeIf(String::isNotBlank)
    }

private fun seekByFraction(controller: MediaController?, fraction: Float) {
    controller?.let { player ->
        val duration = player.duration
        if (duration > 0) {
            player.seekTo(
                (fraction * duration).toLong()
                    .coerceIn(0L, (duration - SEEK_END_GUARD_MS).coerceAtLeast(0L)),
            )
        }
    }
}

private fun seekToTimestamp(controller: MediaController?, target: Long) {
    controller?.let { player ->
        val duration = player.duration
        player.seekTo(
            if (duration > 0) {
                target.coerceIn(0L, (duration - SEEK_END_GUARD_MS).coerceAtLeast(0L))
            } else {
                target.coerceAtLeast(0L)
            },
        )
    }
}

private fun cycleControllerRepeat(controller: MediaController?) {
    controller?.let {
        val next = when (it.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        AppSettings.setRepeatMode(next)
        it.repeatMode = next
    }
}

private fun openArtistFromPlayer(
    id: String?,
    name: String,
    context: Context,
    scope: CoroutineScope,
    dismissPlayer: () -> Unit,
    viewModel: MainViewModel,
) {
    dismissPlayer()
    val channelId = id?.removePrefix("MPLA")?.takeIf {
        it.startsWith("UC") || it.startsWith(LocalGroups.PREFIX)
    }
    if (channelId != null) {
        viewModel.openDetail(
            channelId,
            name,
            context.getString(R.string.artist),
            null,
            BrowseType.ARTIST,
        )
    } else {
        scope.launch {
            val primary = name.primaryArtist().ifBlank { name.trim() }
            if (primary.isBlank()) return@launch
            val found = YtMusicRepository.findArtistPageId(primary).getOrNull()
                ?: YtMusicRepository.search(primary, SearchFilter.ARTISTS).getOrNull()
                    ?.filterIsInstance<SearchResult.Browse>()
                    ?.firstOrNull { it.item.type == BrowseType.ARTIST }
                    ?.item?.browseId
            if (found != null) {
                viewModel.openDetail(
                    found,
                    primary,
                    context.getString(R.string.artist),
                    null,
                    BrowseType.ARTIST,
                )
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.couldnt_find, name),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }
}

private fun openPlaybackSourceFromPlayer(
    displayedSong: Song,
    queueLabel: String,
    context: Context,
    overlays: MainOverlayState,
    dismissPlayer: () -> Unit,
    viewModel: MainViewModel,
) {
    val sourceType = displayedSong.playbackSourceType ?: PlaybackSourceType.QUEUE
    val sourceTitle = displayedSong.playbackSource
        ?: displayedSong.albumName
        ?: queueLabel
    val sourceId = displayedSong.playbackSourceId

    dismissPlayer()
    viewModel.navigator.clear()

    when (sourceType) {
        PlaybackSourceType.BROWSE -> {
            val id = sourceId ?: return
            viewModel.closeMoodGenre()
            viewModel.openDetail(id, sourceTitle)
        }
        PlaybackSourceType.HOME -> {
            viewModel.closeMoodGenre()
            overlays.selectedTab = TAB_HOME
        }
        PlaybackSourceType.SEARCH -> {
            viewModel.closeMoodGenre()
            overlays.selectedTab = TAB_SEARCH
        }
        PlaybackSourceType.HISTORY -> {
            viewModel.navigator.push(AppRoute.History)
            viewModel.loadHistory()
        }
        PlaybackSourceType.REPLAY -> {
            overlays.replayLandingPage = ReplayStoryPage.INTRO
            viewModel.navigator.push(AppRoute.Replay)
        }
        PlaybackSourceType.EXPLORE -> overlays.selectedTab = TAB_EXPLORE
        PlaybackSourceType.SHARED_LINK -> {
            val id = sourceId ?: return
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://music.youtube.com/watch?v=$id"),
                ),
            )
        }
        PlaybackSourceType.QUEUE -> Unit
    }
}
