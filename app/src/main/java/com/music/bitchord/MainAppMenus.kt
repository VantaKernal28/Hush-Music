package com.music.bitchord

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.data.LocalGroups
import com.music.bitchord.data.NerdStats
import com.music.bitchord.data.OfflinePlaylists
import com.music.bitchord.data.TrackLog
import com.music.bitchord.data.YtMusicRepository
import com.music.bitchord.data.lyrics.LyricLine
import com.music.bitchord.data.model.BrowseType
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.UiState
import com.music.bitchord.data.model.UserPlaylist
import com.music.bitchord.data.model.artworkAt
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.download.DownloadStore
import com.music.bitchord.download.DownloadTarget
import com.music.bitchord.download.Downloads
import com.music.bitchord.download.LocalFileSaver
import com.music.bitchord.playback.AudioCache
import com.music.bitchord.playback.QueueShuffle
import com.music.bitchord.playback.hasYouTubeOriginal
import com.music.bitchord.playback.revertToOriginal
import com.music.bitchord.playback.swapToVersion
import com.music.bitchord.playback.upgradeQuality
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.components.BrowseActionsMenu
import com.music.bitchord.ui.components.BrowseTarget
import com.music.bitchord.ui.components.HeldContextMenu
import com.music.bitchord.ui.components.IosMenuPopover
import com.music.bitchord.ui.components.SongActionsMenu
import com.music.bitchord.ui.components.isGlassSupported
import com.music.bitchord.ui.player.LyricsShareLine
import com.music.bitchord.ui.player.LyricsShareRequest
import com.music.bitchord.ui.player.MixViewRequest
import com.music.bitchord.ui.screens.CACHE_FOLDER_BROWSE_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Captures and releases the page backdrop still while a popover menu floats above a page.
 * Isolated in its own recompose scope so menu openings only recompose this helper.
 */
@Composable
internal fun PageBackdropEffect(
    overlays: MainOverlayState,
    chrome: MainChromeState,
    playerSong: Song?,
) {
    Isolated {
        val playerRaised = overlays.showNowPlaying && playerSong != null
        val pageMenuUp =
            (overlays.songActions != null &&
                overlays.songMenuOrigin == null &&
                !(overlays.menuFromPlayer && playerRaised)) ||
                (overlays.browseActions != null &&
                    overlays.browseMenuOrigin == null &&
                    !overlays.browseRenameInSheet)
        LaunchedEffect(pageMenuUp, chrome.reduceDynamicBlur) {
            if (pageMenuUp && !chrome.reduceDynamicBlur && isGlassSupported()) {
                chrome.pageBackdrop.capture()
            } else {
                delay(SNAPSHOT_RELEASE_DELAY_MS)
                chrome.pageBackdrop.release()
            }
        }
    }
}

/** Hosts both the held-card menu and popover menu for tracks over the main chrome. */
@Composable
internal fun TrackContextMenuHost(
    playerSong: Song?,
    overlays: MainOverlayState,
    chrome: MainChromeState,
    nav: MainNavState,
    feeds: MainFeedState,
    playerDock: PlayerDockState,
    playback: MainPlaybackActions,
    downloads: MainDownloadActions,
    viewModel: MainViewModel,
) {
    Isolated {
        val playerRaised = overlays.showNowPlaying && playerSong != null
        HeldContextMenu(
            item = overlays.songActions,
            held = overlays.songMenuOrigin,
            hazeState = chrome.hazeState,
            onDismiss = { overlays.songActions = null },
            onPreviewClick = { song ->
                song.albumId?.let { id ->
                    overlays.songActions = null
                    viewModel.openDetail(
                        id,
                        song.albumName ?: song.title,
                        song.artist,
                        song.thumbnailUrl,
                        BrowseType.ALBUM,
                    )
                }
            },
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
        LaunchedEffect(playerRaised) {
            if (!playerRaised && overlays.menuFromPlayer) overlays.songActions = null
        }
        IosMenuPopover(
            item = overlays.songActions?.takeIf {
                overlays.songMenuOrigin == null && !(overlays.menuFromPlayer && playerRaised)
            },
            anchor = overlays.songMenuAnchor,
            onDismiss = { overlays.songActions = null },
            backdrop = chrome.pageBackdrop,
            hazeState = chrome.hazeState,
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
        LaunchedEffect(overlays.songActions == null) {
            if (overlays.songActions == null) {
                overlays.songMenuOrigin = null
                overlays.songMenuAnchor = null
                overlays.menuFromHistory = false
            }
        }
    }
}

/** Shared body for track long-press cards, overflow popovers, and the player's more menu. */
@Composable
internal fun SongMenuBody(
    heldSong: Song,
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    dismissPlayer: () -> Unit,
    playback: MainPlaybackActions,
    downloads: MainDownloadActions,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val controller = playback.controller
    val player = playback.player
    val fromPlayer = overlays.menuFromPlayer
    val needsLinks = !fromPlayer && heldSong.localUri == null &&
        heldSong.videoId.isNotBlank() &&
        (heldSong.albumId == null || heldSong.artistId == null)
    var menuLinks by remember(heldSong.videoId) { mutableStateOf<Song?>(null) }
    var menuLinksLoading by remember(heldSong.videoId) { mutableStateOf(needsLinks) }
    LaunchedEffect(heldSong.videoId, needsLinks) {
        if (!needsLinks) return@LaunchedEffect
        menuLinksLoading = true
        menuLinks = YtMusicRepository.trackLinks(heldSong.videoId).getOrNull()
        menuLinksLoading = false
    }
    val song = menuLinks?.takeIf { needsLinks && it.videoId == heldSong.videoId }?.let { extra ->
        heldSong.copy(
            artistId = heldSong.artistId ?: extra.artistId,
            albumId = heldSong.albumId ?: extra.albumId,
            albumName = heldSong.albumName ?: extra.albumName,
        )
    } ?: heldSong
    val share: () -> Unit = {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://music.youtube.com/watch?v=${song.videoId}")
        }
        context.startActivity(Intent.createChooser(sendIntent, song.title))
        overlays.songActions = null
    }
    val openPage: (String, String, String, BrowseType) -> Unit = { id, title, sub, type ->
        overlays.songActions = null
        dismissPlayer()
        val art = song.thumbnailUrl.takeUnless { type == BrowseType.ARTIST }
        viewModel.openDetail(id, title, sub, art, type)
    }

    val versions = playback.versions
    val versionEffectiveSong = versions.optimisticVersionSong?.takeIf {
        it.videoId == versions.convertedAudioId ||
            it.videoId == versions.convertedVideoId ||
            it.videoId == versions.keepVideoId ||
            it.videoId == YtMusicRepository.cachedAudioVersion(song.videoId)?.videoId ||
            it.videoId == YtMusicRepository.cachedVideoVersion(song.videoId)?.videoId
    } ?: song
    val menuIsAudioVersion = if (versions.optimisticVersionSong != null) {
        !versions.optimisticVersionSong!!.isVideo
    } else {
        !song.isVideo && versions.convertedVideoId != song.videoId
    }
    val onToggleVersion = buildVersionToggleHandler(
        song = song,
        versionEffectiveSong = versionEffectiveSong,
        controllerCurrentId = controller?.currentMediaItem?.mediaId,
        context = context,
        scope = scope,
        overlays = overlays,
        feeds = feeds,
        playback = playback,
    )

    LaunchedEffect(song.videoId) { viewModel.loadSongMenu(song.videoId) }
    val editable = viewModel.editablePlaylist(nav.detail?.browseId)
        ?.takeIf { !fromPlayer && song.setVideoId != null }
    val automixOn by AppSettings.smartFadeEnabled.collectAsStateWithLifecycle()
    val cacheVersion by AudioCache.contentsChanged.collectAsStateWithLifecycle()
    val inCache by produceState(false, song.videoId, cacheVersion) {
        value = song.localUri == null &&
            withContext(Dispatchers.IO) { AudioCache.holds(song.videoId) }
    }

    SongActionsMenu(
        song = song,
        signedIn = feeds.signedIn,
        likeStatus = playback.likeStatusOf(song),
        onPlayNext = { playback.playNext(song); overlays.songActions = null },
        onAddToQueue = { playback.addToQueue(song); overlays.songActions = null },
        onStartRadio = { playback.startRadio(song); overlays.songActions = null },
        onDownload = {
            if (!fromPlayer && nav.detail?.browseId == CACHE_FOLDER_BROWSE_ID) {
                downloads.downloadFromCache(listOf(song))
            } else {
                downloads.downloadSong(song)
            }
        },
        onSaveLocally = if (song.localUri == null) {
            {
                overlays.songActions = null
                overlays.showQueueNotice("Saving \"${song.title}\" locally...")
                scope.launch {
                    LocalFileSaver.saveTrackToPublicMusic(context, song)
                        .onSuccess { fileName ->
                            overlays.showQueueNotice("Saved to Music/${DownloadStore.FOLDER}/$fileName")
                        }
                        .onFailure { err ->
                            Toast.makeText(
                                context,
                                err.message ?: "Could not save file locally",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                }
            }
        } else {
            null
        },
        onUploadToWebDav =
            if (com.music.bitchord.data.webdav.WebDavConfig.isConfigured(feeds.webdavUrl) &&
                com.music.bitchord.data.webdav.WebDavUploads.isUploadable(song)
            ) {
                {
                    overlays.songActions = null
                    downloads.uploadToWebDav(listOf(song))
                }
            } else {
                null
            },
        onToggleLike = { playback.toggleLikeOf(song) },
        onToggleDislike = {
            val previousStatus = viewModel.toggleDislike(song.videoId)
            if (
                previousStatus != null &&
                shouldSkipAfterDislike(
                    previousStatus = previousStatus,
                    targetVideoId = song.videoId,
                    currentVideoId = player.song?.videoId,
                )
            ) {
                controller?.seekToNextMediaItem()
            }
        },
        onAddToPlaylist = {
            overlays.songActions = null
            if (song.localUri != null && (song.videoId.startsWith("content://") || song.videoId.startsWith("file://"))) {
                overlays.offlinePlaylistTarget = song
            } else {
                viewModel.loadPlaylists()
                overlays.playlistTarget = song
            }
        },
        onRemoveFromPlaylist = editable?.let {
            {
                overlays.songActions = null
                viewModel.removeFromPlaylist(it.browseId, song)
            }
        } ?: nav.detail?.browseId
            ?.takeIf { !fromPlayer && song.localUri != null && OfflinePlaylists.isOfflinePlaylist(it) }
            ?.let { id ->
                {
                    overlays.songActions = null
                    OfflinePlaylists.remove(id, song)
                }
            },
        onRemoveFromCache = if (inCache) {
            {
                overlays.songActions = null
                downloads.removeFromCache(listOf(song))
            }
        } else {
            null
        },
        onRemoveFromHistory = song.feedbackToken?.takeIf { overlays.menuFromHistory }?.let { token ->
            {
                overlays.songActions = null
                viewModel.removeFromHistory(token, onFailure = {
                    Toast.makeText(context, R.string.remove_from_history_failed, Toast.LENGTH_SHORT).show()
                })
            }
        },
        onOpenAlbum = { id ->
            openPage(id, song.albumName ?: song.title, song.artist, BrowseType.ALBUM)
        },
        onOpenArtist = { id ->
            openPage(id, song.artist, context.getString(R.string.artist), BrowseType.ARTIST)
        },
        resolvingLinks = if (fromPlayer) overlays.linksLoading else needsLinks && menuLinksLoading,
        showSleepTimer = fromPlayer,
        onRollbackToOriginal = if (fromPlayer &&
            song.hasYouTubeOriginal() &&
            song.localUri == null &&
            song.videoId !in feeds.pinnedToOriginal &&
            !playingYouTubesOwn(song.videoId, controller) &&
            controller?.currentMediaItem?.mediaId == song.videoId
        ) {
            {
                controller?.revertToOriginal()
                overlays.songActions = null
            }
        } else {
            null
        },
        onUpgradeQuality = if (fromPlayer &&
            (song.videoId in feeds.pinnedToOriginal || playingYouTubesOwn(song.videoId, controller)) &&
            song.localUri == null &&
            controller?.currentMediaItem?.mediaId == song.videoId
        ) {
            {
                controller?.upgradeQuality()
                overlays.songActions = null
            }
        } else {
            null
        },
        upgradeQualityInProgress = fromPlayer && song.videoId in feeds.qualityUpgradesInFlight,
        onToggleAudioVersion = onToggleVersion,
        isAudioVersion = menuIsAudioVersion,
        audioVersionMissing = song.videoId in versions.missingVersions,
        onShare = share.takeIf { song.videoId.isNotBlank() },
        onShareLyricsCard = {
            openLyricsShareRequest(
                song = song,
                playingSongId = player.song?.videoId,
                positionMs = player.position.positionMs,
                lyrics = feeds.lyrics,
                overlays = overlays,
            )
        },
        onCopyLog = if (fromPlayer) {
            {
                overlays.songActions = null
                scope.launch {
                    val text = TrackLog.forTrack(song, NerdStats.current.value)
                    clipboard.setText(AnnotatedString(text))
                    val count = text.lineSequence().count()
                    Toast.makeText(
                        context,
                        context.resources.getQuantityString(R.plurals.log_copied_line_count, count, count),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        } else {
            null
        },
        onLyricsOffset = if (fromPlayer) {
            {
                overlays.songActions = null
                overlays.showLyricsOffset = true
            }
        } else {
            null
        },
        onAutomixOverview = if (fromPlayer && automixOn) {
            {
                overlays.songActions = null
                MixViewRequest.open = true
            }
        } else {
            null
        },
    )
}

private fun buildVersionToggleHandler(
    song: Song,
    versionEffectiveSong: Song,
    controllerCurrentId: String?,
    context: Context,
    scope: CoroutineScope,
    overlays: MainOverlayState,
    feeds: MainFeedState,
    playback: MainPlaybackActions,
): (() -> Unit)? {
    if (controllerCurrentId != song.videoId) return null
    val versions = playback.versions
    return toggle@{
        overlays.songActions = null
        if (feeds.partyState.inParty) {
            overlays.showQueueNotice(context.getString(R.string.version_switch_in_party))
            return@toggle
        }
        if (versions.switchingAudioVersion) return@toggle
        val c = playback.controller
        val original = versions.convertedFromVideo
        val originalAudio = versions.convertedFromAudio
        when {
            c == null -> Unit
            original != null && (
                versions.convertedAudioId == song.videoId ||
                    versions.convertedAudioId == versionEffectiveSong.videoId ||
                    versions.convertedAudioId == versions.optimisticVersionSong?.videoId
                ) -> {
                versions.keepVideoId = original.videoId
                versions.convertedFromVideo = null
                versions.convertedAudioId = null
                if (!versions.versionAlignmentPending(song.videoId, original.videoId)) {
                    versions.optimisticVersionSong = original
                }
                c.swapToVersion(original)
            }
            originalAudio != null && (
                versions.convertedVideoId == song.videoId ||
                    versions.convertedVideoId == versionEffectiveSong.videoId ||
                    versions.convertedVideoId == versions.optimisticVersionSong?.videoId
                ) -> {
                versions.convertedFromAudio = null
                versions.convertedVideoId = null
                if (!versions.versionAlignmentPending(song.videoId, originalAudio.videoId)) {
                    versions.optimisticVersionSong = originalAudio
                }
                c.swapToVersion(originalAudio)
            }
            versionEffectiveSong.isVideo || song.isVideo -> {
                val cached = YtMusicRepository.cachedAudioVersion(song.videoId)
                    ?: YtMusicRepository.cachedAudioVersion(versionEffectiveSong.videoId)
                if (cached != null && cached.videoId != song.videoId &&
                    !versions.versionAlignmentPending(song.videoId, cached.videoId)
                ) {
                    versions.optimisticVersionSong = cached.copy(
                        isVideoOrigin = true,
                        queueTier = song.queueTier,
                        queueEntryId = song.queueEntryId,
                        radioName = song.radioName,
                        playbackSource = song.playbackSource,
                        playbackSourceType = song.playbackSourceType,
                        playbackSourceId = song.playbackSourceId,
                    )
                }
                scope.launch { playback.switchToMusicOnly(song, pauseWhileResolving = false) }
            }
            else -> {
                val cached = YtMusicRepository.cachedVideoVersion(song.videoId)
                    ?: YtMusicRepository.cachedVideoVersion(versionEffectiveSong.videoId)
                if (cached != null && cached.videoId != song.videoId &&
                    !versions.versionAlignmentPending(song.videoId, cached.videoId)
                ) {
                    versions.optimisticVersionSong = cached.copy(
                        queueTier = song.queueTier,
                        queueEntryId = song.queueEntryId,
                        radioName = song.radioName,
                        playbackSource = song.playbackSource,
                        playbackSourceType = song.playbackSourceType,
                        playbackSourceId = song.playbackSourceId,
                    )
                }
                scope.launch { playback.switchToVideo(song, pauseWhileResolving = false) }
            }
        }
    }
}

private fun openLyricsShareRequest(
    song: Song,
    playingSongId: String?,
    positionMs: Long,
    lyrics: List<LyricLine>?,
    overlays: MainOverlayState,
) {
    val currentLines = if (song.videoId == playingSongId) lyrics else null
    val preloaded = currentLines.orEmpty()
        .filter { it.text.isNotBlank() }
        .map { LyricsShareLine(text = it.text, subText = null) }
    val preSelected = if (preloaded.isNotEmpty()) {
        val activeIdx = currentLines.orEmpty().indexOfLast { it.timeMs <= positionMs }.coerceAtLeast(0)
        val endIdx = (activeIdx + 3).coerceAtMost(preloaded.lastIndex)
        (activeIdx..endIdx).toList()
    } else {
        emptyList()
    }
    overlays.songActions = null
    overlays.lyricsCardTarget = LyricsShareRequest(
        song = song,
        artworkUrl = song.artworkAt(544) ?: song.thumbnailUrl,
        lines = preSelected.mapNotNull { preloaded.getOrNull(it) },
        allLines = preloaded,
        selectedIndices = preSelected,
    )
}

/** Hosts rename sheet, held-card menu, and popover menu for albums and playlists. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BrowseContextMenuHost(
    overlays: MainOverlayState,
    chrome: MainChromeState,
    nav: MainNavState,
    feeds: MainFeedState,
    playback: MainPlaybackActions,
    downloads: MainDownloadActions,
    viewModel: MainViewModel,
) {
    Isolated {
        overlays.browseActions?.takeIf { overlays.browseRenameInSheet }?.let { target ->
            ModalBottomSheet(
                onDismissRequest = { overlays.browseActions = null },
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                BrowseMenuBody(
                    target = target,
                    overlays = overlays,
                    nav = nav,
                    feeds = feeds,
                    playback = playback,
                    downloads = downloads,
                    viewModel = viewModel,
                )
            }
        }
        HeldContextMenu(
            item = overlays.browseActions,
            held = overlays.browseMenuOrigin,
            hazeState = chrome.hazeState,
            onDismiss = { overlays.browseActions = null },
            onPreviewClick = { target ->
                target.browseId?.let { id ->
                    overlays.browseActions = null
                    viewModel.openDetail(
                        browseId = id,
                        title = target.title,
                        subtitle = target.subtitle,
                        thumbnailUrl = target.thumbnailUrl,
                        type = target.type,
                    )
                }
            },
        ) { target ->
            BrowseMenuBody(
                target = target,
                overlays = overlays,
                nav = nav,
                feeds = feeds,
                playback = playback,
                downloads = downloads,
                viewModel = viewModel,
            )
        }
        IosMenuPopover(
            item = overlays.browseActions?.takeIf {
                overlays.browseMenuOrigin == null && !overlays.browseRenameInSheet
            },
            anchor = overlays.browseMenuAnchor,
            onDismiss = { overlays.browseActions = null },
            backdrop = chrome.pageBackdrop,
            hazeState = chrome.hazeState,
        ) { target ->
            BrowseMenuBody(
                target = target,
                overlays = overlays,
                nav = nav,
                feeds = feeds,
                playback = playback,
                downloads = downloads,
                viewModel = viewModel,
            )
        }
        LaunchedEffect(overlays.browseActions == null) {
            if (overlays.browseActions == null) {
                overlays.browseMenuOrigin = null
                overlays.browseMenuAnchor = null
                overlays.browseRenameInSheet = false
            }
        }
    }
}

/** Shared body for album and playlist context menus and release overflow popovers. */
@Composable
internal fun BrowseMenuBody(
    target: BrowseTarget,
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    playback: MainPlaybackActions,
    downloads: MainDownloadActions,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val act: ((List<Song>) -> Unit) -> () -> Unit = { action ->
        {
            overlays.browseActions = null
            playback.withBrowseSongs(target, action)
        }
    }
    LaunchedEffect(target.browseId) {
        viewModel.resolvePlaylistOwnership(target.browseId)
    }
    val ownedPlaylists by viewModel.playlistOwned.collectAsStateWithLifecycle()
    val playlist = target.browseId
        ?.takeIf { feeds.signedIn && ownedPlaylists[it] == true }
        ?.let { id -> feeds.playlists.firstOrNull { it.browseId == id } }
    val remote = target.browseId?.startsWith("local:") == false
    val downloadable = remote ||
        target.browseId?.startsWith("local:playlist:sp_local_") == true
    val pinnedPlaylists by AppSettings.pinnedPlaylists.collectAsStateWithLifecycle()
    val pinnableId = target.browseId?.takeIf {
        target.type == BrowseType.PLAYLIST &&
            !it.startsWith(OfflinePlaylists.PREFIX) && !it.startsWith(LocalGroups.PREFIX)
    }
    val offlineEditable = OfflinePlaylists.get(target.browseId)?.takeUnless { it.isLiked }

    BrowseActionsMenu(
        target = target.copy(
            playlist = playlist ?: offlineEditable?.let {
                UserPlaylist(playlistId = it.id, title = it.title, subtitle = "", thumbnailUrl = null)
            },
        ),
        onRenameInSheet = {
            overlays.browseRenameInSheet = true
            overlays.browseMenuOrigin = null
        },
        startRenaming = overlays.browseRenameInSheet,
        onPlayNext = act(playback.playSongsNext),
        onAddToQueue = act(playback.addSongsToQueue),
        onPlay = act { songs -> playback.play(songs, 0) }.takeIf { target.fromCard },
        onShuffle = act { songs ->
            QueueShuffle.enableForNextQueue()
            playback.play(songs, songs.indices.random())
        }.takeIf { target.fromCard },
        onOpen = target.browseId
            ?.takeIf { target.fromCard }
            ?.let { id ->
                {
                    overlays.browseActions = null
                    viewModel.openDetail(
                        browseId = id,
                        title = target.title,
                        subtitle = target.subtitle,
                        thumbnailUrl = target.thumbnailUrl,
                        type = target.type,
                    )
                }
            },
        onDownloadAll = act { songs ->
            downloads.startDownload(
                songs,
                target.browseId
                    ?.takeIf { target.type != BrowseType.ARTIST }
                    ?.let { id ->
                        DownloadTarget(
                            id = id,
                            title = target.title,
                            subtitle = target.subtitle,
                            thumbnailUrl = target.thumbnailUrl,
                            playlist = target.type == BrowseType.PLAYLIST,
                        )
                    },
            )
        }.takeIf { downloadable },
        onSaveLocally = act { songs ->
            val saveable = songs.filter { it.localUri == null }
            if (saveable.isEmpty()) {
                overlays.showQueueNotice("Already saved on device")
            } else {
                overlays.showQueueNotice("Saving ${saveable.size} tracks locally...")
                scope.launch {
                    val (saved, total) = LocalFileSaver.savePlaylistToPublicMusic(context, target.title, saveable)
                    overlays.showQueueNotice("Saved $saved/$total tracks to Music/${DownloadStore.FOLDER}")
                }
            }
        }.takeIf { downloadable },
        onShare = target.browseId
            ?.takeIf { remote && (target.type == BrowseType.ALBUM || target.type == BrowseType.PLAYLIST) }
            ?.let { id ->
                {
                    val url = if (target.type == BrowseType.PLAYLIST) {
                        "https://music.youtube.com/playlist?list=${id.removePrefix("VL")}"
                    } else {
                        "https://music.youtube.com/browse/$id"
                    }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, url)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, target.title))
                    overlays.browseActions = null
                }
            },
        isPinned = pinnableId != null && pinnableId in pinnedPlaylists,
        onTogglePin = pinnableId?.let { id ->
            {
                val nowPinned = AppSettings.togglePinnedPlaylist(id)
                if (!nowPinned && id !in pinnedPlaylists) {
                    Toast.makeText(
                        context,
                        context.getString(
                            R.string.pinned_playlist_limit,
                            AppSettings.MAX_PINNED_PLAYLISTS,
                        ),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                overlays.browseActions = null
            }
        },
        onRename = playlist?.let { p ->
            { name: String ->
                overlays.browseActions = null
                viewModel.renamePlaylist(p, name)
            }
        } ?: offlineEditable?.let { p ->
            { name: String ->
                overlays.browseActions = null
                OfflinePlaylists.rename(p.browseId, name)
                viewModel.retitleDetail(p.browseId, name.trim())
            }
        },
        onReorder = playlist?.let { p ->
            {
                overlays.browseActions = null
                if (nav.detail?.browseId != p.browseId) {
                    viewModel.openDetail(
                        browseId = p.browseId,
                        title = p.title,
                        subtitle = p.subtitle,
                        thumbnailUrl = p.thumbnailUrl,
                        type = BrowseType.PLAYLIST,
                    )
                }
                nav.detailSearching = false
                overlays.reorderTarget = p
                overlays.reorderEntries = UiState.Loading
                overlays.reorderSaving = false
                viewModel.loadPlaylistEntries(p) { result ->
                    if (overlays.reorderTarget != p) return@loadPlaylistEntries
                    overlays.reorderEntries = result.fold(
                        onSuccess = { UiState.Success(it) },
                        onFailure = { UiState.Error(context.getString(R.string.failed)) },
                    )
                }
            }
        },
        onDelete = playlist?.let { p ->
            {
                overlays.browseActions = null
                viewModel.deletePlaylist(p)
            }
        } ?: offlineEditable?.let { p ->
            {
                overlays.browseActions = null
                if (nav.detail?.browseId == p.browseId) viewModel.closeDetail()
                OfflinePlaylists.delete(p.browseId)
            }
        },
        onDeleteDownload = target.downloadId?.let { id ->
            {
                overlays.browseActions = null
                scope.launch { Downloads.deleteCollection(context, id) }
            }
        },
    )
}
