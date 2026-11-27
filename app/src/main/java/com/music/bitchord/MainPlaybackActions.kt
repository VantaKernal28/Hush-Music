package com.music.bitchord

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.session.MediaController
import com.music.bitchord.data.LocalGroups
import com.music.bitchord.data.LocalMediaRepository
import com.music.bitchord.data.OfflinePlaylists
import com.music.bitchord.data.TopPicks
import com.music.bitchord.data.TrackLog
import com.music.bitchord.data.YtMusicRepository
import com.music.bitchord.data.innertube.InnertubeParser
import com.music.bitchord.data.listentogether.ListenTogether
import com.music.bitchord.data.listentogether.partyQueueIndexOf
import com.music.bitchord.data.model.BrowseType
import com.music.bitchord.data.model.LikeStatus
import com.music.bitchord.data.model.PlaybackSourceType
import com.music.bitchord.data.model.QueueTier
import com.music.bitchord.data.model.SearchFilter
import com.music.bitchord.data.model.SearchResult
import com.music.bitchord.data.model.ShelfItem
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.TopPick
import com.music.bitchord.data.model.TopPickKind
import com.music.bitchord.data.model.TopPickMood
import com.music.bitchord.data.model.durationMillis
import com.music.bitchord.data.model.isUnresolvedSpotify
import com.music.bitchord.data.scrobbling.primaryArtist
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.sources.SourceRegistry
import com.music.bitchord.download.DownloadStore
import com.music.bitchord.download.DownloadTarget
import com.music.bitchord.download.Downloads
import com.music.bitchord.download.MediaTagger
import com.music.bitchord.playback.AudioCache
import com.music.bitchord.playback.BoundedQueuePlanner
import com.music.bitchord.playback.LinkRequest
import com.music.bitchord.playback.MusicLink
import com.music.bitchord.playback.PlayerState
import com.music.bitchord.playback.QueueCoordinator
import com.music.bitchord.playback.QueueCoordinator.asQueueEntry
import com.music.bitchord.playback.QueueSource
import com.music.bitchord.playback.beginRadioQueue
import com.music.bitchord.playback.cast.CastController
import com.music.bitchord.playback.commitRadioQueue
import com.music.bitchord.playback.fromAutoplay
import com.music.bitchord.playback.loadAutoplayTracks
import com.music.bitchord.playback.playBoundedContext
import com.music.bitchord.playback.playSongs
import com.music.bitchord.playback.smart.VersionAudioAligner
import com.music.bitchord.playback.swapToVersion
import com.music.bitchord.playback.toMediaItem
import com.music.bitchord.playback.toSong
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.SearchSource
import com.music.bitchord.ui.components.BrowseTarget
import com.music.bitchord.ui.navigation.AppRoute
import com.music.bitchord.ui.player.MusicVideoEngine
import com.music.bitchord.ui.screens.SPOTIFY_BROWSE_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Tracks temporary audio/video version swaps and in-flight radio generations. */
@Stable
internal class VersionSwitchState(
    val missingVersions: SnapshotStateList<String>,
) {
    var convertedFromVideo by mutableStateOf<Song?>(null)
    var convertedAudioId by mutableStateOf<String?>(null)
    var switchingAudioVersion by mutableStateOf(false)
    var keepVideoId by mutableStateOf<String?>(null)
    var convertedFromAudio by mutableStateOf<Song?>(null)
    var convertedVideoId by mutableStateOf<String?>(null)
    var optimisticVersionSong by mutableStateOf<Song?>(null)
    var playRequestGeneration by mutableIntStateOf(0)
    var activeRadioSeed by mutableStateOf<Pair<String, String>?>(null)

    fun versionAlignmentPending(songId: String, targetId: String): Boolean =
        AppSettings.smartVersionAlignment.value &&
            VersionAudioAligner.getCachedOffsetMs(songId, targetId) == null &&
            YtMusicRepository.versionTimeMap(songId, targetId) == null
}

/** Coordinates downloads, permission launches, WebDAV uploads, and library item routing. */
@Stable
internal class MainDownloadActions(
    private val context: Context,
    private val scope: CoroutineScope,
    private val viewModel: MainViewModel,
    private val overlays: MainOverlayState,
    private val feeds: MainFeedState,
    private val nav: MainNavState,
    private val notifyPermission: ManagedActivityResultLauncher<String, Boolean>,
    private val storagePermission: ManagedActivityResultLauncher<String, Boolean>,
    private val mediaPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
) {
    var downloadPending by mutableStateOf<List<Song>>(emptyList())
    var downloadPendingFrom by mutableStateOf<DownloadTarget?>(null)
    var player by mutableStateOf(PlayerState())
    var removedFromCacheMessage by mutableStateOf("")

    private val mediaPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun uploadToWebDav(songs: List<Song>) {
        scope.launch {
            val summary = com.music.bitchord.data.webdav.WebDavUploads.upload(context, songs)
            if (summary.total > 0) {
                overlays.showQueueNotice(
                    context.getString(
                        R.string.webdav_upload_summary,
                        summary.uploaded,
                        summary.skipped,
                        summary.failed,
                    ),
                )
            }
            if (summary.uploaded > 0 &&
                nav.topDetail?.browseId == com.music.bitchord.data.webdav.WebDavConfig.BROWSE_ID
            ) {
                viewModel.reloadLocalDetail(com.music.bitchord.data.webdav.WebDavConfig.BROWSE_ID)
            }
        }
    }

    val startDownload: (List<Song>, DownloadTarget?) -> Unit = { requested, from ->
        val saved = Downloads.saved.value
        val songs = requested
            .filter { it.videoId !in saved }
            .map { song ->
                val cover = from?.thumbnailUrl
                if (song.thumbnailUrl.isNullOrBlank() && !cover.isNullOrBlank()) {
                    song.copy(thumbnailUrl = cover)
                } else {
                    song
                }
            }
        val blocked = songs.isNotEmpty() && !AppSettings.downloadsAllowedNow &&
            songs.none { Downloads.servableFromCache(it.videoId) }
        if (songs.isNotEmpty() && !blocked) {
            val needsStorage = AppSettings.exportDownloads.value && DownloadStore.needsLegacyPermission() &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ) != PackageManager.PERMISSION_GRANTED
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notifyPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (needsStorage) {
                downloadPending = songs
                downloadPendingFrom = from
                storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                songs.forEach { Downloads.enqueue(context, it, from?.title) }
                if (from != null) Downloads.markRequested(from.id, songs.map { it.videoId })
            }
            if (!songs.all { Downloads.servableFromCache(it.videoId) }) {
                SourceRegistry.addonsLeftOutOfDownloads()
                    .firstOrNull { (config, why) -> explainedDownloadExclusions.add("${config.id}:$why") }
                    ?.let { (config, why) -> overlays.addonLeftOutOfDownload = config.displayName to why }
            }
        }
        if (from != null && !blocked) {
            Downloads.rememberCollection(from, requested)
            if (!from.thumbnailUrl.isNullOrBlank()) {
                scope.launch(Dispatchers.IO) {
                    MediaTagger.cacheArtwork(context.applicationContext, from.thumbnailUrl)
                        ?.let { Downloads.rememberCollectionArtwork(from.id, it) }
                }
            }
        }
        when {
            blocked -> Toast.makeText(
                context,
                context.getString(R.string.wifi_only_download_refusal),
                Toast.LENGTH_LONG,
            ).show()
            requested.size > 1 -> {
                val message = if (songs.isEmpty()) {
                    context.getString(R.string.already_downloaded)
                } else {
                    context.resources.getQuantityString(
                        R.plurals.downloading_song_count,
                        songs.size,
                        songs.size,
                    )
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val downloadFromCache: (List<Song>) -> Unit = { songs ->
        val cleaned = songs.map { it.copy(downloadFormat = null, localDateAddedSeconds = null) }
        Downloads.preferCachedCopy(cleaned.map { it.videoId })
        startDownload(cleaned, null)
    }

    val removeFromCache: (List<Song>) -> Unit = { songs ->
        scope.launch {
            val removed = songs.sumOf { AudioCache.remove(it.videoId) }
            if (removed > 0) overlays.showQueueNotice(removedFromCacheMessage)
        }
    }

    val downloadSong: (Song) -> Unit = { song ->
        val withDuration = if (song.durationMillis() <= 0L &&
            player.song?.videoId == song.videoId && player.durationMs > 0L
        ) {
            song.copy(durationText = formatDurationText(player.durationMs))
        } else {
            song
        }
        startDownload(listOf(withDuration), null)
    }

    val onSearchSourceChange: (SearchSource) -> Unit = { source ->
        if (source == SearchSource.LIBRARY && !LocalMediaRepository.hasStoragePermission(context)) {
            mediaPermissionLauncher.launch(mediaPermission)
        }
        viewModel.setSearchSource(source)
    }

    val onLibraryItemClick: (ShelfItem) -> Unit = { item ->
        item.browseId?.let { id ->
            if (id == SPOTIFY_BROWSE_ID) {
                nav.navigator.push(AppRoute.Spotify)
                return@let
            }
            if ((id == "local:all" || id == "local:downloads") && !LocalMediaRepository.hasStoragePermission(context)) {
                mediaPermissionLauncher.launch(mediaPermission)
            }
            viewModel.openDetail(
                browseId = id,
                title = item.title,
                subtitle = item.subtitle,
                thumbnailUrl = item.thumbnailUrl,
            )
        }
    }
}

@Composable
internal fun rememberMainDownloadActions(
    viewModel: MainViewModel,
    overlays: MainOverlayState,
    feeds: MainFeedState,
    nav: MainNavState,
    player: PlayerState,
): MainDownloadActions {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val holder = remember { arrayOfNulls<MainDownloadActions>(1) }

    val notifyPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Refusing costs the progress notification, not the download. */ }
    val storagePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val actions = holder[0] ?: return@rememberLauncherForActivityResult
        val songs = actions.downloadPending
        val from = actions.downloadPendingFrom
        actions.downloadPending = emptyList()
        actions.downloadPendingFrom = null
        when {
            songs.isEmpty() -> Unit
            granted -> {
                songs.forEach { Downloads.enqueue(context, it, from?.title) }
                if (from != null) Downloads.markRequested(from.id, songs.map { it.videoId })
            }
            else -> Toast
                .makeText(context, context.getString(R.string.storage_required_save), Toast.LENGTH_SHORT)
                .show()
        }
    }
    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.reloadLocalDetail("local:all")
            viewModel.reloadLocalDetail("local:downloads")
            viewModel.loadLibrarySongs()
        } else {
            Toast.makeText(context, context.getString(R.string.storage_required_read), Toast.LENGTH_SHORT).show()
        }
    }

    val actions = remember(viewModel, overlays, feeds, nav, notifyPermission, storagePermission, mediaPermissionLauncher) {
        MainDownloadActions(
            context = context,
            scope = scope,
            viewModel = viewModel,
            overlays = overlays,
            feeds = feeds,
            nav = nav,
            notifyPermission = notifyPermission,
            storagePermission = storagePermission,
            mediaPermissionLauncher = mediaPermissionLauncher,
        ).also { holder[0] = it }
    }
    actions.player = player
    actions.removedFromCacheMessage = stringResource(R.string.removed_from_cache)
    return actions
}

/** Coordinates playback commands, queue insertion, radio stations, and navigation from rows. */
@Stable
internal class MainPlaybackActions(
    private val context: Context,
    private val scope: CoroutineScope,
    private val viewModel: MainViewModel,
    private val overlays: MainOverlayState,
    private val feeds: MainFeedState,
    val versions: VersionSwitchState,
) {
    var controller by mutableStateOf<MediaController?>(null)
    var player by mutableStateOf(PlayerState())
    var playPauseBusy by mutableStateOf(false)
    var controlsLocked by mutableStateOf(false)
    var queueLabel by mutableStateOf("")
    var hostOnlyMessage by mutableStateOf("")

    val showHostOnlyNotice: () -> Unit = { overlays.showQueueNotice(hostOnlyMessage) }

    val refusedByHost: () -> Boolean = {
        val locked = ListenTogether.state.value.controlsLocked
        if (locked) showHostOnlyNotice()
        locked
    }

    val togglePlayPause: () -> Unit = {
        controller?.let { c ->
            val party = ListenTogether.state.value
            if (party.controlsLocked && !party.playback.isPlaying && !c.isPlaying) {
                showHostOnlyNotice()
            } else if (c.isPlaying) {
                c.pause()
            } else {
                c.play()
            }
        }
    }

    val likeStatusOf: (Song) -> LikeStatus = { song ->
        val uri = song.localUri
        when {
            uri != null -> if (uri in feeds.offlineLiked) LikeStatus.LIKE else LikeStatus.INDIFFERENT
            else -> feeds.likeStatuses[song.videoId] ?: LikeStatus.INDIFFERENT
        }
    }

    val toggleLikeOf: (Song) -> Unit = { song ->
        if (song.localUri != null) OfflinePlaylists.toggleLike(song) else viewModel.toggleLike(song.videoId)
    }

    suspend fun switchToMusicOnly(song: Song, pauseWhileResolving: Boolean) {
        val c = controller ?: return
        val index = c.currentMediaItemIndex
        if (index !in 0 until c.mediaItemCount ||
            c.currentMediaItem?.mediaId != song.videoId ||
            versions.switchingAudioVersion
        ) return

        val resumeAfterResolution = pauseWhileResolving && c.playWhenReady
        versions.switchingAudioVersion = true
        versions.keepVideoId = null
        val holdUntilAligned = AppSettings.smartVersionAlignment.value
        if (holdUntilAligned) AppSettings.versionAlignmentInProgress.value = true
        if (pauseWhileResolving) c.pause()
        try {
            TrackLog.d("Player", "audio switch requested for '${song.title}'", song.videoId)
            val audio = runCatching { YtMusicRepository.resolveAudio(song) }.getOrNull()
            val stillCurrent = c.currentMediaItemIndex == index &&
                c.currentMediaItem?.mediaId == song.videoId

            if (audio == null || audio.videoId == song.videoId) {
                TrackLog.w("Player", "audio switch found no distinct official song", song.videoId)
                if (song.videoId !in versions.missingVersions) versions.missingVersions += song.videoId
                if (stillCurrent) overlays.showQueueNotice(context.getString(R.string.no_audio_version))
                if (stillCurrent && resumeAfterResolution) c.play()
                return
            }
            if (!stillCurrent) {
                TrackLog.d("Player", "audio switch discarded; listener changed track", song.videoId)
                return
            }

            versions.convertedFromVideo = song
            versions.convertedAudioId = audio.videoId
            TrackLog.d("Player", "audio switch applying '${audio.title}' (${audio.videoId})", song.videoId)
            val target = audio.copy(
                isVideoOrigin = true,
                queueTier = song.queueTier,
                queueEntryId = song.queueEntryId,
                radioName = song.radioName,
                playbackSource = song.playbackSource,
                playbackSourceType = song.playbackSourceType,
                playbackSourceId = song.playbackSourceId,
            )
            if (!versions.versionAlignmentPending(song.videoId, target.videoId)) {
                versions.optimisticVersionSong = target
            }
            if (holdUntilAligned) AppSettings.versionAlignmentInProgress.value = false
            c.swapToVersion(target)
        } finally {
            if (holdUntilAligned) AppSettings.versionAlignmentInProgress.value = false
            versions.switchingAudioVersion = false
        }
    }

    suspend fun switchToVideo(song: Song, pauseWhileResolving: Boolean) {
        val c = controller ?: return
        val index = c.currentMediaItemIndex
        if (index !in 0 until c.mediaItemCount ||
            c.currentMediaItem?.mediaId != song.videoId ||
            versions.switchingAudioVersion
        ) return

        val resumeAfterResolution = pauseWhileResolving && c.playWhenReady
        versions.switchingAudioVersion = true
        val holdUntilAligned = AppSettings.smartVersionAlignment.value
        if (holdUntilAligned) AppSettings.versionAlignmentInProgress.value = true
        if (pauseWhileResolving) c.pause()
        try {
            TrackLog.d("Player", "video switch requested for '${song.title}'", song.videoId)
            val video = runCatching { YtMusicRepository.resolveVideo(song) }.getOrNull()
            val stillCurrent = c.currentMediaItemIndex == index &&
                c.currentMediaItem?.mediaId == song.videoId

            if (video == null || video.videoId == song.videoId) {
                TrackLog.w("Player", "video switch found no distinct video", song.videoId)
                if (song.videoId !in versions.missingVersions) versions.missingVersions += song.videoId
                if (stillCurrent) overlays.showQueueNotice(context.getString(R.string.no_video_version))
                if (stillCurrent && resumeAfterResolution) c.play()
                return
            }
            if (!stillCurrent) {
                TrackLog.d("Player", "video switch discarded; listener changed track", song.videoId)
                return
            }

            versions.convertedFromAudio = song
            versions.convertedVideoId = video.videoId
            TrackLog.d("Player", "video switch applying '${video.title}' (${video.videoId})", song.videoId)
            val target = video.copy(
                queueTier = song.queueTier,
                queueEntryId = song.queueEntryId,
                radioName = song.radioName,
                playbackSource = song.playbackSource,
                playbackSourceType = song.playbackSourceType,
                playbackSourceId = song.playbackSourceId,
            )
            if (!versions.versionAlignmentPending(song.videoId, target.videoId)) {
                versions.optimisticVersionSong = target
            }
            if (holdUntilAligned) AppSettings.versionAlignmentInProgress.value = false
            c.swapToVersion(target)
        } finally {
            if (holdUntilAligned) AppSettings.versionAlignmentInProgress.value = false
            versions.switchingAudioVersion = false
        }
    }

    fun playFrom(
        allSongs: List<Song>,
        allIndex: Int,
        source: QueueSource,
        boundedContext: Boolean = false,
        continuationToken: String? = null,
    ) {
        if (allSongs.getOrNull(allIndex)?.isUnresolvedSpotify == true) return
        val songs = allSongs.filterNot { it.isUnresolvedSpotify }
        val index = songs.indexOf(allSongs.getOrNull(allIndex)).coerceAtLeast(0)
        versions.playRequestGeneration++
        versions.activeRadioSeed = null
        scope.launch {
            if (refusedByHost()) return@launch
            val c = controller ?: return@launch
            val currentTimeline = player.queue.takeIf { it.size == c.mediaItemCount }
                ?: (0 until c.mediaItemCount).map { c.getMediaItemAt(it).toSong() }
            val currentIndex = c.currentMediaItemIndex

            if (ListenTogether.state.value.inParty) {
                val selectedSong = songs.getOrNull(index) ?: return@launch
                val party = ListenTogether.state.value
                val partyQueue = party.queue.items
                val partyIndex = partyQueueIndexOf(party.queue, party.playback, party.playback.track?.videoId)
                val upcomingPartyTracks = if (partyIndex >= 0) partyQueue.drop(partyIndex + 1) else emptyList()
                val timeline = QueueCoordinator.buildPartyPlaybackQueue(
                    tappedSong = selectedSong,
                    source = source,
                    upcomingPartyTracks = upcomingPartyTracks,
                )
                c.playSongs(timeline, 0)
            } else {
                val bounded = boundedContext || songs.size > BoundedQueuePlanner.MAX_PHYSICAL_CONTEXT
                if (bounded && !CastController.state.value.casting) {
                    c.playBoundedContext(songs, index, source, continuationToken)
                } else {
                    val result = QueueCoordinator.buildContextQueue(
                        currentTimeline = currentTimeline,
                        currentIndex = currentIndex,
                        newContextSongs = songs,
                        selectedIndex = index,
                        contextSource = source,
                    )
                    c.playSongs(result.timeline, result.startIndex)
                }
            }
        }
    }

    val play: (List<Song>, Int) -> Unit = { songs, index ->
        val first = songs.getOrNull(index)
        val source = QueueSource(
            title = first?.playbackSource ?: first?.albumName ?: queueLabel,
            type = first?.playbackSourceType ?: PlaybackSourceType.QUEUE,
            id = first?.playbackSourceId,
        )
        playFrom(songs, index, source)
    }

    val playRadio: (Song, QueueSource) -> Unit = { song, source ->
        versions.playRequestGeneration++
        versions.activeRadioSeed = null
        scope.launch {
            if (refusedByHost()) return@launch
            val c = controller ?: return@launch
            val currentTimeline = player.queue.takeIf { it.size == c.mediaItemCount }
                ?: (0 until c.mediaItemCount).map { c.getMediaItemAt(it).toSong() }
            val currentIndex = c.currentMediaItemIndex
            val oneOffQueue = QueueCoordinator.buildOneOffQueue(
                currentTimeline = currentTimeline,
                currentIndex = currentIndex,
                tappedSong = song,
                source = source,
            )
            c.playSongs(oneOffQueue, 0)
        }
    }

    val startRadio: (Song) -> Unit = { song ->
        val originalController = controller
        if (originalController != null && !refusedByHost()) {
            val request = ++versions.playRequestGeneration
            val originalManualQueue = (0 until originalController.mediaItemCount)
                .map { originalController.getMediaItemAt(it) }
                .filterNot { it.fromAutoplay }
                .map { it.mediaId }
            scope.launch {
                val seed = song.copy(
                    radioName = song.title,
                    playbackSource = song.title,
                    playbackSourceType = PlaybackSourceType.SHARED_LINK,
                    playbackSourceId = song.videoId,
                )
                val related = loadAutoplayTracks(
                    existing = listOf(seed),
                    seedSong = seed,
                    limit = INITIAL_RADIO_TRACKS,
                ).getOrElse {
                    if (request == versions.playRequestGeneration) {
                        Toast.makeText(context, R.string.couldnt_load_tracks, Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                if (related.isEmpty()) {
                    if (request == versions.playRequestGeneration) {
                        Toast.makeText(context, R.string.couldnt_load_tracks, Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                val activeController = controller ?: return@launch
                val activeManualQueue = (0 until activeController.mediaItemCount)
                    .map { activeController.getMediaItemAt(it) }
                    .filterNot { it.fromAutoplay }
                    .map { it.mediaId }
                if (request != versions.playRequestGeneration || activeManualQueue != originalManualQueue) {
                    return@launch
                }
                activeController.beginRadioQueue()
                val currentIndex = activeController.currentMediaItemIndex
                val currentItem = activeController.currentMediaItem
                if (currentIndex >= 0 && currentItem?.mediaId == song.videoId) {
                    activeController.addMediaItems(currentIndex + 1, related.map { it.toMediaItem() })
                    val oldTail = currentIndex + 1 + related.size
                    if (oldTail < activeController.mediaItemCount) {
                        activeController.removeMediaItems(oldTail, activeController.mediaItemCount)
                    }
                    if (currentIndex > 0) activeController.removeMediaItems(0, currentIndex)
                    versions.activeRadioSeed = song.videoId to song.title
                } else {
                    versions.activeRadioSeed = null
                    activeController.playSongs(listOf(seed) + related, 0)
                }
                activeController.commitRadioQueue()
                Toast.makeText(
                    context,
                    context.getString(R.string.radio_started, song.title),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    val openTopPick: (TopPick, String) -> Unit = { pick, label ->
        val browseId = pick.browseId
        if (pick.kind == TopPickKind.LATEST_RELEASE && browseId != null) {
            viewModel.openDetail(
                browseId = browseId,
                title = pick.title,
                subtitle = pick.artistName.orEmpty(),
                thumbnailUrl = pick.images.firstOrNull(),
            )
        } else if (controller != null && !refusedByHost()) {
            val request = ++versions.playRequestGeneration
            scope.launch {
                val songs = TopPicks.queue(pick).getOrNull().orEmpty()
                if (request != versions.playRequestGeneration) return@launch
                val activeController = controller ?: return@launch
                if (songs.isEmpty()) {
                    Toast.makeText(context, R.string.couldnt_load_tracks, Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val sourceId = pick.watch?.playlistId ?: pick.mood?.name
                val station = songs.map {
                    it.copy(
                        radioName = label,
                        playbackSource = label,
                        playbackSourceType = PlaybackSourceType.HOME,
                        playbackSourceId = sourceId,
                    )
                }
                versions.activeRadioSeed = null
                activeController.beginRadioQueue()
                activeController.playSongs(station, 0)
                activeController.commitRadioQueue()
            }
        }
    }

    val playMood: (TopPickMood, String) -> Unit = { mood, title ->
        val hints = feeds.topPicks?.firstOrNull { it.kind == TopPickKind.MOOD }?.hints
        openTopPick(
            if (hints != null) TopPicks.moodStation(mood, hints) else TopPicks.moodStation(mood),
            title,
        )
    }

    private fun enqueueTrack(song: Song, next: Boolean) {
        scope.launch {
            if (refusedByHost()) return@launch
            controller?.let {
                if (ListenTogether.state.value.inParty) {
                    val upcoming = (it.mediaItemCount - (it.currentMediaItemIndex + 1)).coerceAtLeast(0)
                    if (upcoming >= 25) {
                        overlays.showQueueNotice(context.getString(R.string.party_queue_full, 25))
                        return@launch
                    }
                }
                val current = it.currentMediaItem?.toSong()
                val timeline = player.queue.takeIf { q -> q.size == it.mediaItemCount }
                    ?: (0 until it.mediaItemCount).map { idx -> it.getMediaItemAt(idx).toSong() }
                val at = QueueCoordinator.findUserQueueInsertionIndex(
                    timeline = timeline,
                    currentIndex = it.currentMediaItemIndex,
                    isNext = next,
                )
                val queued = song.copy(
                    radioName = current?.radioName,
                    playbackSource = current?.playbackSource ?: queueLabel,
                    playbackSourceType = current?.playbackSourceType ?: PlaybackSourceType.QUEUE,
                    playbackSourceId = current?.playbackSourceId,
                ).asQueueEntry(QueueTier.USER_QUEUE)
                it.addMediaItem(at, queued.toMediaItem())
                overlays.showQueueNotice(
                    context.getString(if (next) R.string.song_will_play_next else R.string.song_added_to_queue),
                )
            }
        }
    }

    val addToQueue: (Song) -> Unit = { song -> enqueueTrack(song, next = false) }
    val playNext: (Song) -> Unit = { song -> enqueueTrack(song, next = true) }
    val onSongSwipe: (Song) -> Unit = { song ->
        if (AppSettings.swipeToPlayNext.value) playNext(song) else addToQueue(song)
    }

    fun openByName(
        browseId: String?,
        name: String,
        subtitle: String?,
        type: BrowseType,
        artwork: String? = null,
    ) {
        if (!browseId.isNullOrBlank()) {
            val cleanId = if (browseId.startsWith("MPLAUC")) browseId.removePrefix("MPLA") else browseId
            val validForType = type != BrowseType.ARTIST || cleanId.startsWith("UC") || cleanId.startsWith(LocalGroups.PREFIX)
            if (validForType) {
                val credit = subtitle ?: context.getString(
                    if (type == BrowseType.ARTIST) R.string.artist else R.string.album,
                )
                viewModel.openDetail(cleanId, name, credit, artwork, type)
                return
            }
        }
        scope.launch {
            val searchFilter = if (type == BrowseType.ARTIST) SearchFilter.ARTISTS else SearchFilter.ALBUMS
            val targetName = if (type == BrowseType.ARTIST) name.primaryArtist() else name
            val q = if (type == BrowseType.ARTIST) targetName else listOfNotNull(name, subtitle).joinToString(" ")
            val hit = YtMusicRepository.search(q, searchFilter).getOrNull()
                ?.filterIsInstance<SearchResult.Browse>()
                ?.firstOrNull()
                ?.item
            if (hit == null) {
                Toast.makeText(context, context.getString(R.string.couldnt_find, name), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.openDetail(
                    hit.browseId,
                    hit.title,
                    hit.subtitle,
                    hit.thumbnailUrl ?: artwork,
                    hit.type,
                )
            }
        }
    }

    val queueSongs: (List<Song>, Boolean) -> Unit = { songs, next ->
        if (songs.isNotEmpty()) {
            scope.launch {
                if (refusedByHost()) return@launch
                val c = controller
                if (c == null || c.mediaItemCount == 0) {
                    play(songs, 0)
                } else {
                    val toAdd = if (ListenTogether.state.value.inParty) {
                        val upcoming = (c.mediaItemCount - (c.currentMediaItemIndex + 1)).coerceAtLeast(0)
                        val slotsLeft = (25 - upcoming).coerceAtLeast(0)
                        if (slotsLeft <= 0) {
                            overlays.showQueueNotice(context.getString(R.string.party_queue_full, 25))
                            return@launch
                        }
                        songs.take(slotsLeft)
                    } else {
                        songs
                    }
                    val timeline = player.queue.takeIf { q -> q.size == c.mediaItemCount }
                        ?: (0 until c.mediaItemCount).map { idx -> c.getMediaItemAt(idx).toSong() }
                    val at = QueueCoordinator.findUserQueueInsertionIndex(
                        timeline = timeline,
                        currentIndex = c.currentMediaItemIndex,
                        isNext = next,
                    )
                    val current = c.currentMediaItem?.toSong()
                    c.addMediaItems(
                        at,
                        toAdd.map {
                            it.copy(
                                radioName = current?.radioName,
                                playbackSource = current?.playbackSource ?: queueLabel,
                                playbackSourceType = current?.playbackSourceType ?: PlaybackSourceType.QUEUE,
                                playbackSourceId = current?.playbackSourceId,
                            ).asQueueEntry(QueueTier.USER_QUEUE).toMediaItem()
                        },
                    )
                    val message = context.resources.getQuantityString(
                        if (next) R.plurals.songs_will_play_next else R.plurals.songs_added_to_queue,
                        toAdd.size,
                        toAdd.size,
                    )
                    overlays.showQueueNotice(message)
                }
            }
        }
    }
    val addSongsToQueue: (List<Song>) -> Unit = { songs -> queueSongs(songs, false) }
    val playSongsNext: (List<Song>) -> Unit = { songs -> queueSongs(songs, true) }

    val onBrowseLongPress: (ShelfItem) -> Unit = { item ->
        val id = item.browseId
        val type = id?.let { viewModel.browseTypeOf(it) }
        if (id != null && type != BrowseType.ARTIST) {
            overlays.openBrowseMenu(
                BrowseTarget(
                    browseId = id,
                    title = item.title,
                    subtitle = item.subtitle,
                    thumbnailUrl = item.thumbnailUrl,
                    type = type ?: BrowseType.OTHER,
                    downloadId = feeds.downloadIdFor(id),
                ),
            )
        }
    }

    val shelfSong: (ShelfItem) -> Song? = { item ->
        item.videoId?.let { videoId ->
            Song(
                videoId = videoId,
                title = item.title,
                artist = InnertubeParser.artistFromSubtitle(item.subtitle),
                thumbnailUrl = item.thumbnailUrl,
                isVideo = item.isVideo,
            )
        }
    }

    val onShelfLongPress: (ShelfItem) -> Unit = { item ->
        val song = shelfSong(item)
        if (song != null) overlays.openSongMenu(song) else onBrowseLongPress(item)
    }

    val withBrowseSongs: (BrowseTarget, (List<Song>) -> Unit) -> Unit = { target, action ->
        val stamp: (List<Song>) -> Unit = { songs ->
            action(
                songs.map { song ->
                    song.copy(
                        albumName = if (target.type == BrowseType.ALBUM) {
                            song.albumName ?: target.title
                        } else {
                            song.albumName
                        },
                        playbackSource = target.title,
                        playbackSourceType = PlaybackSourceType.BROWSE,
                        playbackSourceId = target.browseId,
                    )
                },
            )
        }
        when {
            target.songs.isNotEmpty() -> stamp(target.songs)
            target.browseId == null ->
                Toast.makeText(context, context.getString(R.string.no_tracks_here), Toast.LENGTH_SHORT).show()
            else -> viewModel.collectSongs(target.browseId, target.thumbnailUrl) { result ->
                result.fold(
                    onSuccess = stamp,
                    onFailure = {
                        val message = it.message ?: context.getString(R.string.couldnt_load_tracks)
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberMainPlaybackActions(
    controller: MediaController?,
    player: PlayerState,
    viewModel: MainViewModel,
    overlays: MainOverlayState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    playerDock: PlayerDockState,
): MainPlaybackActions {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    DisposableEffect(controller) {
        MusicVideoEngine.attach(controller)
        onDispose { MusicVideoEngine.attach(null) }
    }
    val awaitingPartyStart by ListenTogether.awaitingStart.collectAsStateWithLifecycle()
    val playPauseBusy = player.isLoading || awaitingPartyStart
    val controlsLocked = feeds.partyState.controlsLocked
    val missingVersions = remember { mutableStateListOf<String>() }
    val versions = remember(missingVersions) { VersionSwitchState(missingVersions) }

    val syncedLyricsEnabled by AppSettings.syncedLyrics.collectAsStateWithLifecycle()
    val lyricsSources by AppSettings.lyricsSources.collectAsStateWithLifecycle()
    val lyricsSourceOrder by AppSettings.lyricsSourceOrder.collectAsStateWithLifecycle()
    val prioritizeSyllableSync by AppSettings.prioritizeSyllableSync.collectAsStateWithLifecycle()
    LaunchedEffect(
        player.song?.videoId,
        player.durationMs,
        syncedLyricsEnabled,
        lyricsSources,
        lyricsSourceOrder,
        prioritizeSyllableSync,
    ) {
        player.song?.let {
            viewModel.loadLyrics(
                it.videoId,
                it.title,
                it.artist,
                player.durationMs.takeIf { ms -> ms > 0L } ?: it.durationMillis(),
                it.albumName,
                it.localUri,
            )
        }
    }
    LaunchedEffect(player.song?.videoId) {
        if (versions.optimisticVersionSong?.videoId == player.song?.videoId ||
            (versions.optimisticVersionSong != null &&
                player.song?.videoId != versions.convertedAudioId &&
                player.song?.videoId != versions.convertedVideoId &&
                player.song?.videoId != versions.keepVideoId)
        ) {
            versions.optimisticVersionSong = null
        }
        if (versions.activeRadioSeed?.first != player.song?.videoId) versions.activeRadioSeed = null
        if (versions.keepVideoId != player.song?.videoId) versions.keepVideoId = null
        if (player.song?.videoId != versions.convertedAudioId) {
            versions.convertedFromVideo = null
            versions.convertedAudioId = null
            versions.switchingAudioVersion = false
        }
        if (player.song?.videoId != versions.convertedVideoId) {
            versions.convertedFromAudio = null
            versions.convertedVideoId = null
            versions.switchingAudioVersion = false
        }
    }

    val actions = remember(context, scope, viewModel, overlays, feeds, versions) {
        MainPlaybackActions(
            context = context,
            scope = scope,
            viewModel = viewModel,
            overlays = overlays,
            feeds = feeds,
            versions = versions,
        )
    }
    actions.controller = controller
    actions.player = player
    actions.playPauseBusy = playPauseBusy
    actions.controlsLocked = controlsLocked
    actions.queueLabel = scroll.queueLabel
    actions.hostOnlyMessage = stringResource(R.string.listen_together_host_only_notice)

    ExternalMusicLinkEffect(
        controller = controller,
        overlays = overlays,
        scroll = scroll,
        playerDock = playerDock,
        playback = actions,
        viewModel = viewModel,
    )
    return actions
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExternalMusicLinkEffect(
    controller: MediaController?,
    overlays: MainOverlayState,
    scroll: MainScrollState,
    playerDock: PlayerDockState,
    playback: MainPlaybackActions,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val linkRequest by MusicLink.pending.collectAsStateWithLifecycle()
    LaunchedEffect(linkRequest, controller) {
        val request = linkRequest ?: return@LaunchedEffect
        val session = controller ?: return@LaunchedEffect
        when (request) {
            is LinkRequest.Track -> {
                val song = YtMusicRepository.trackLinks(request.videoId).getOrNull()
                if (song == null) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.couldnt_open_link),
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    playback.playRadio(
                        song,
                        QueueSource(scroll.sharedLinkLabel, PlaybackSourceType.SHARED_LINK, song.videoId),
                    )
                }
            }
            is LinkRequest.Page -> {
                playerDock.onDismiss()
                viewModel.openDetail(request.browseId, title = "")
            }
            is LinkRequest.Search -> {
                val songs = if (!request.play) null else {
                    YtMusicRepository.search(request.query, SearchFilter.SONGS).getOrNull()
                        ?.filterIsInstance<SearchResult.Track>()
                }
                val top = songs?.firstOrNull()?.song
                if (top != null) {
                    playback.playRadio(top, QueueSource(scroll.searchLabel, PlaybackSourceType.SEARCH))
                } else {
                    playerDock.onDismiss()
                    viewModel.navigator.clear()
                    overlays.selectedTab = TAB_SEARCH
                    viewModel.searchFor(request.query)
                }
            }
            LinkRequest.Resume -> if (session.mediaItemCount > 0) session.play()
            is LinkRequest.LocalFile -> {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        request.uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
                val song = LocalMediaRepository.songFromUri(context, request.uri)
                playback.playFrom(
                    listOf(song),
                    0,
                    QueueSource(scroll.openedFileLabel, PlaybackSourceType.SHARED_LINK, song.localUri),
                )
                overlays.showNowPlaying = true
            }
        }
        MusicLink.handled()
    }
}

@Composable
internal fun rememberResolvedPlayerSong(
    currentSong: Song?,
    overlays: MainOverlayState,
): Song? {
    val playerShowing = overlays.showNowPlaying
    var links by remember { mutableStateOf<Song?>(null) }
    LaunchedEffect(currentSong?.videoId, playerShowing) {
        links = null
        overlays.linksLoading = false
        if (!playerShowing) return@LaunchedEffect
        val current = currentSong ?: return@LaunchedEffect
        if (current.albumId != null && current.artistId != null) return@LaunchedEffect
        overlays.linksLoading = true
        links = YtMusicRepository.trackLinks(current.videoId).getOrNull()
        overlays.linksLoading = false
    }
    val playerSong = currentSong?.let { current ->
        val extra = links?.takeIf { it.videoId == current.videoId } ?: return@let current
        current.copy(
            artistId = current.artistId ?: extra.artistId,
            albumId = current.albumId ?: extra.albumId,
            albumName = current.albumName ?: extra.albumName,
        )
    }
    LaunchedEffect(playerSong) {
        if (playerSong != null && overlays.songActions?.videoId == playerSong.videoId) {
            overlays.songActions = playerSong
        }
    }
    return playerSong
}
