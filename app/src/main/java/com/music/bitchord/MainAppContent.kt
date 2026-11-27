package com.music.bitchord

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Upgrade
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.auth.WebSessionMode
import com.music.bitchord.data.TopPicks
import com.music.bitchord.data.YtMusicRepository
import com.music.bitchord.data.innertube.InnertubeParser
import com.music.bitchord.data.listentogether.ListenTogether
import com.music.bitchord.data.listentogether.ServerConnectionState
import com.music.bitchord.data.model.BrowseType
import com.music.bitchord.data.model.DetailPage
import com.music.bitchord.data.model.EntityType
import com.music.bitchord.data.model.PlaybackSourceType
import com.music.bitchord.data.model.SearchHistoryEntity
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.TopPickKind
import com.music.bitchord.data.model.UiState
import com.music.bitchord.data.model.ShelfItem
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.LibrarySort
import com.music.bitchord.data.sources.SourceRegistry
import com.music.bitchord.data.spotify.SPOTIFY_PAGE_PREFIX
import com.music.bitchord.download.Downloads
import com.music.bitchord.playback.QueueShuffle
import com.music.bitchord.playback.QueueSource
import com.music.bitchord.playback.cast.CastController
import com.music.bitchord.playback.contextEntryKey
import com.music.bitchord.sharedui.resources.Res as SharedRes
import com.music.bitchord.sharedui.resources.search_addons_hint
import com.music.bitchord.sharedui.resources.search_hint
import com.music.bitchord.sharedui.resources.search_library_hint
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.SearchSource
import com.music.bitchord.ui.components.AiPlaylistPillButton
import com.music.bitchord.ui.components.BottomFadeScrim
import com.music.bitchord.ui.components.BrowseTarget
import com.music.bitchord.ui.components.DownloadManagerPage
import com.music.bitchord.ui.components.FLOATING_BAR_MAX_WIDTH
import com.music.bitchord.ui.components.FloatingBarsTapGuard
import com.music.bitchord.ui.components.FrostedTopBar
import com.music.bitchord.ui.components.GlassNavBar
import com.music.bitchord.ui.components.LocalPlayingBarsMotion
import com.music.bitchord.ui.components.QueueActionNoticeHost
import com.music.bitchord.ui.components.ReorderPlaylistPage
import com.music.bitchord.ui.components.SearchField
import com.music.bitchord.ui.components.TopBarAccountButton
import com.music.bitchord.ui.components.TopBarDownloadButton
import com.music.bitchord.ui.components.TopFadeBlur
import com.music.bitchord.ui.components.backdrop.backdrops.layerBackdrop
import com.music.bitchord.ui.components.backdrop.backdrops.snapshotBackdropSource
import com.music.bitchord.ui.components.expressive.ExpressiveMiniPlayer
import com.music.bitchord.ui.components.expressive.ExpressiveNavBar
import com.music.bitchord.ui.components.menuAnchor
import com.music.bitchord.ui.icons.BitChordIcons
import com.music.bitchord.ui.navigation.AppDestination
import com.music.bitchord.ui.navigation.AppRoute
import com.music.bitchord.ui.replay.ReplayScreen
import com.music.bitchord.ui.replay.ReplayStoryPage
import com.music.bitchord.ui.screens.AccountAndScrobblingScreen
import com.music.bitchord.ui.screens.DetailScreen
import com.music.bitchord.ui.screens.DiscordScreen
import com.music.bitchord.ui.screens.EqualizerPresetPage
import com.music.bitchord.ui.screens.EqualizerScreen
import com.music.bitchord.ui.screens.ExploreScreen
import com.music.bitchord.ui.screens.HistoryScreen
import com.music.bitchord.ui.screens.HomeScreen
import com.music.bitchord.ui.screens.LibraryGridPage
import com.music.bitchord.ui.screens.LibraryReplayEntry
import com.music.bitchord.ui.screens.LibraryScreen
import com.music.bitchord.ui.screens.ListenTogetherScreen
import com.music.bitchord.ui.screens.LocalFolder
import com.music.bitchord.ui.screens.LocalFolderState
import com.music.bitchord.ui.screens.LocalFolderTab
import com.music.bitchord.ui.screens.MoodGenrePlaylistsScreen
import com.music.bitchord.ui.screens.MoodGridPage
import com.music.bitchord.ui.screens.SearchScreen
import com.music.bitchord.ui.screens.SegmentedControl
import com.music.bitchord.ui.screens.SettingsChoicePage
import com.music.bitchord.ui.screens.SettingsScreen
import com.music.bitchord.ui.screens.SourcesScreen
import com.music.bitchord.ui.screens.SpotifyLibraryScreen
import com.music.bitchord.ui.screens.WearOsScreen
import com.music.bitchord.ui.screens.CACHE_FOLDER_BROWSE_ID
import com.music.bitchord.ui.screens.libraryDeviceItems
import com.music.bitchord.ui.screens.libraryLinks
import com.music.bitchord.ui.screens.sortedForDetail
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch

/** Registers back handlers in exact lowest-to-highest precedence order. */
@Composable
internal fun MainBackHandlers(
    nav: MainNavState,
    feeds: MainFeedState,
    overlays: MainOverlayState,
    viewModel: MainViewModel,
) {
    val canGoBack = nav.canGoBack(feeds.selectedMoodGenre, overlays.selectedTab)
    BackHandler(enabled = canGoBack) {
        if (!nav.navigator.pop() && !viewModel.closeMoodGenre()) {
            overlays.selectedTab = TAB_HOME
        }
    }
    val replayDrawable = feeds.replay.summary?.isEmpty == false
    BackHandler(enabled = replayDrawable && overlays.replayStory != null && !overlays.showReplayShare) {
        overlays.replayStory = null
    }
    BackHandler(enabled = replayDrawable && overlays.showReplayShare) {
        overlays.showReplayShare = false
    }
    BackHandler(enabled = overlays.showUpdateDialog) { overlays.showUpdateDialog = false }
    BackHandler(enabled = overlays.showListenBrainzLogin) { overlays.showListenBrainzLogin = false }
    BackHandler(enabled = overlays.showLastfmLogin) { overlays.showLastfmLogin = false }
    BackHandler(enabled = overlays.discordDialog != null) { overlays.discordDialog = null }
    BackHandler(enabled = overlays.editingSource != null) { overlays.editingSource = null }
    BackHandler(enabled = overlays.choosingSourceQuality != null) { overlays.choosingSourceQuality = null }
    BackHandler(enabled = overlays.explainingOutdatedAddon != null) { overlays.explainingOutdatedAddon = null }
    BackHandler(enabled = overlays.editingPartyServer) { overlays.editingPartyServer = false }
    BackHandler(enabled = overlays.editingSignatureApi) { overlays.editingSignatureApi = false }
}

/** Lays out the main navigation viewport, floating top chrome, AI playlist pill, and bottom bars. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainContentShell(
    windowWidth: Dp,
    artworkMotion: Boolean,
    chrome: MainChromeState,
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    playerDock: PlayerDockState,
    playback: MainPlaybackActions,
    downloads: MainDownloadActions,
    viewModel: MainViewModel,
) {
    Row(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = chrome.modeMorphScale
                scaleY = chrome.modeMorphScale
                alpha = chrome.modeMorphAlpha
            }
            .then(if (chrome.modeMorphBlur > 0.dp) Modifier.blur(chrome.modeMorphBlur) else Modifier)
            .snapshotBackdropSource(chrome.pageBackdrop, floor = MaterialTheme.colorScheme.background),
    ) {
        Box(Modifier.weight(1f).fillMaxHeight()) {
            MainAnimatedDestination(
                windowWidth = windowWidth,
                artworkMotion = artworkMotion,
                chrome = chrome,
                overlays = overlays,
                nav = nav,
                feeds = feeds,
                scroll = scroll,
                playback = playback,
                downloads = downloads,
                viewModel = viewModel,
            )

            MainTopChrome(
                chrome = chrome,
                overlays = overlays,
                nav = nav,
                feeds = feeds,
                scroll = scroll,
                viewModel = viewModel,
            )

            MainBottomChrome(
                chrome = chrome,
                overlays = overlays,
                nav = nav,
                feeds = feeds,
                scroll = scroll,
                playerDock = playerDock,
                playback = playback,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun MainAnimatedDestination(
    windowWidth: Dp,
    artworkMotion: Boolean,
    chrome: MainChromeState,
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    playback: MainPlaybackActions,
    downloads: MainDownloadActions,
    viewModel: MainViewModel,
) {
    val visibleEntry = nav.visibleEntry
    val detail = nav.detail
    val destinationTarget = when {
        visibleEntry != null -> AppDestination.Route(visibleEntry)
        detail != null -> AppDestination.Release(detail.browseId)
        else -> AppDestination.Tab(overlays.selectedTab)
    }
    AnimatedContent(
        targetState = destinationTarget,
        contentKey = { it.key },
        transitionSpec = {
            val initial = initialState
            val target = targetState
            val tabSwap = initial is AppDestination.Tab && target is AppDestination.Tab
            val libraryTab = AppDestination.Tab(TAB_LIBRARY)
            fun AppDestination.isShowAll() =
                (this as? AppDestination.Route)?.entry?.route is AppRoute.LibraryShowAll
            val libraryShowAllSwap = (initial.isShowAll() && target == libraryTab) ||
                (target.isShowAll() && initial == libraryTab)
            if (tabSwap && AppSettings.materialUiMode.value) {
                val forward = (target as AppDestination.Tab).index > (initial as AppDestination.Tab).index
                val slideOffset = 80
                (slideInHorizontally(spring(stiffness = 380f, dampingRatio = 0.82f)) { if (forward) slideOffset else -slideOffset } +
                    fadeIn(tween(220)) +
                    scaleIn(initialScale = 0.96f, animationSpec = spring(stiffness = 380f, dampingRatio = 0.82f)))
                    .togetherWith(
                        slideOutHorizontally(spring(stiffness = 380f, dampingRatio = 0.82f)) { if (forward) -slideOffset else slideOffset } +
                            fadeOut(tween(180)) +
                            scaleOut(targetScale = 0.96f, animationSpec = spring(stiffness = 380f, dampingRatio = 0.82f)),
                    )
            } else if (tabSwap || libraryShowAllSwap) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                fadeIn(tween(180)) togetherWith fadeOut(tween(180))
            }
        },
        modifier = Modifier
            .hazeSource(chrome.hazeState)
            .then(
                if (chrome.glassActive && chrome.glassSamplesBackdrop) {
                    Modifier.layerBackdrop(chrome.appBackdrop)
                } else {
                    Modifier
                },
            )
            .nestedScroll(chrome.navBarScroll),
        label = "content",
    ) { destination ->
        val key = destination.key
        val pageRoute = (destination as? AppDestination.Route)?.entry?.route
        val live = (destination as? AppDestination.Release)?.let { release ->
            nav.detailStack.lastOrNull()?.takeIf { it.browseId == release.browseId }
        }
        val held = remember(key) { mutableStateOf(live) }
        if (live != null) held.value = live
        val page = held.value
        val stateKey = (destination as? AppDestination.Release)?.browseId ?: key
        val pageDetailListState = remember(key) {
            scroll.detailListStates.getOrPut(stateKey) { LazyListState() }
        }
        val barsMotion = artworkMotion && transition.targetState == EnterExitState.Visible
        CompositionLocalProvider(LocalPlayingBarsMotion provides barsMotion) {
            if (destination is AppDestination.Tab) {
                MainDestinationPage(
                    destination = destination,
                    pageRoute = pageRoute,
                    page = page,
                    pageDetailListState = pageDetailListState,
                    windowWidth = windowWidth,
                    artworkMotion = artworkMotion,
                    overlays = overlays,
                    nav = nav,
                    feeds = feeds,
                    scroll = scroll,
                    playback = playback,
                    downloads = downloads,
                    viewModel = viewModel,
                )
            } else {
                scroll.pageKeysSeen += key
                scroll.pageStates.SaveableStateProvider(key) {
                    MainDestinationPage(
                        destination = destination,
                        pageRoute = pageRoute,
                        page = page,
                        pageDetailListState = pageDetailListState,
                        windowWidth = windowWidth,
                        artworkMotion = artworkMotion,
                        overlays = overlays,
                        nav = nav,
                        feeds = feeds,
                        scroll = scroll,
                        playback = playback,
                        downloads = downloads,
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}

@Composable
private fun MainDestinationPage(
    destination: AppDestination,
    pageRoute: AppRoute?,
    page: DetailPage?,
    pageDetailListState: LazyListState,
    windowWidth: Dp,
    artworkMotion: Boolean,
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    playback: MainPlaybackActions,
    downloads: MainDownloadActions,
    viewModel: MainViewModel,
) {
    when {
        pageRoute != null -> RouteDestinationPage(
            pageRoute = pageRoute,
            windowWidth = windowWidth,
            overlays = overlays,
            feeds = feeds,
            scroll = scroll,
            playback = playback,
            downloads = downloads,
            viewModel = viewModel,
        )
        page != null -> DetailDestinationPage(
            page = page,
            pageDetailListState = pageDetailListState,
            overlays = overlays,
            nav = nav,
            feeds = feeds,
            scroll = scroll,
            playback = playback,
            downloads = downloads,
            viewModel = viewModel,
        )
        else -> when ((destination as? AppDestination.Tab)?.index ?: overlays.selectedTab) {
            TAB_HOME -> HomeDestinationTab(
                artworkMotion = artworkMotion,
                overlays = overlays,
                nav = nav,
                feeds = feeds,
                scroll = scroll,
                playback = playback,
                viewModel = viewModel,
            )
            TAB_EXPLORE -> ExploreDestinationTab(
                feeds = feeds,
                scroll = scroll,
                playback = playback,
                viewModel = viewModel,
            )
            TAB_SEARCH -> SearchDestinationTab(
                overlays = overlays,
                feeds = feeds,
                scroll = scroll,
                playback = playback,
                downloads = downloads,
                viewModel = viewModel,
            )
            else -> LibraryDestinationTab(
                overlays = overlays,
                nav = nav,
                feeds = feeds,
                scroll = scroll,
                playback = playback,
                downloads = downloads,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun RouteDestinationPage(
    pageRoute: AppRoute,
    windowWidth: Dp,
    overlays: MainOverlayState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    playback: MainPlaybackActions,
    downloads: MainDownloadActions,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val player = playback.player
    when (pageRoute) {
        AppRoute.History -> HistoryScreen(
            state = feeds.historyState,
            listState = scroll.historyListState,
            currentSong = player.song,
            isPlaying = player.isPlaying,
            onSongClick = { songs, index ->
                playback.playFrom(songs, index, QueueSource(scroll.historyLabel, PlaybackSourceType.HISTORY))
            },
            onSongLongPress = { song ->
                overlays.menuFromHistory = true
                overlays.openSongMenu(song)
            },
            onSongSwipe = playback.onSongSwipe,
            onRetry = viewModel::loadHistory,
            contentPadding = scroll.listPadding,
        )
        is AppRoute.LibraryShowAll -> pageRoute.shelf.let { shelf ->
            LibraryGridPage(
                shelf = shelf,
                gridState = scroll.libraryShowAllGridState,
                onItemClick = downloads.onLibraryItemClick,
                onItemLongPress = playback.onBrowseLongPress,
                onNewPlaylist = if (shelf.title == YtMusicRepository.PLAYLISTS_SHELF) {
                    { overlays.creatingPlaylist = true }
                } else {
                    null
                },
                contentPadding = scroll.listPadding,
            )
        }
        AppRoute.Moods -> MoodGridPage(
            gridState = scroll.moodsGridState,
            onClick = playback.playMood,
            contentPadding = scroll.listPadding,
        )
        AppRoute.Replay -> ReplayScreen(
            state = feeds.replay,
            holder = feeds.account?.name.orEmpty(),
            onPeriodChange = feeds.setReplayPeriod,
            onOpenStory = { overlays.replayStory = it },
            onPlaySong = { song ->
                playback.playRadio(song, QueueSource(scroll.replayLabel, PlaybackSourceType.REPLAY))
            },
            onOpenArtist = { id, name ->
                playback.openByName(id, name, null, BrowseType.ARTIST)
            },
            onOpenAlbum = { id, title, artist, art ->
                playback.openByName(id, title, artist, BrowseType.ALBUM, art)
            },
            onShare = {
                overlays.replaySharePage = null
                overlays.showReplayShare = true
            },
            contentPadding = scroll.listPadding,
            listState = scroll.replayListState,
            landingPage = overlays.replayLandingPage,
        )
        AppRoute.Spotify -> SpotifyLibraryScreen(
            onOpenPlaylist = { playlist ->
                viewModel.openDetail(
                    browseId = SPOTIFY_PAGE_PREFIX + playlist.id,
                    title = playlist.name,
                    subtitle = playlist.owner ?: context.getString(R.string.spotify),
                    thumbnailUrl = playlist.imageUrl,
                    type = BrowseType.PLAYLIST,
                )
            },
            onOpenLogin = { overlays.showSpotifyLogin = true },
            onOpenTokenSetup = { overlays.showSpotifyCanvasAuth = true },
            contentPadding = scroll.listPadding,
        )
        else -> SettingsAndIntegrationsRoutePage(
            pageRoute = pageRoute,
            windowWidth = windowWidth,
            overlays = overlays,
            feeds = feeds,
            scroll = scroll,
            playback = playback,
            viewModel = viewModel,
        )
    }
}

@Composable
private fun SettingsAndIntegrationsRoutePage(
    pageRoute: AppRoute,
    windowWidth: Dp,
    overlays: MainOverlayState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    playback: MainPlaybackActions,
    viewModel: MainViewModel,
) {
    val player = playback.player
    val navigator = viewModel.navigator
    when (pageRoute) {
        AppRoute.Discord -> DiscordScreen(
            song = player.song,
            positionMs = player.position.positionMs,
            durationMs = player.durationMs,
            onOpenLogin = { overlays.showDiscordLogin = true },
            onOpenDialog = { overlays.discordDialog = it },
            contentPadding = scroll.listPadding,
        )
        AppRoute.AccountScrobbling -> AccountAndScrobblingScreen(
            signedIn = feeds.signedIn,
            account = feeds.account,
            channelName = feeds.selectedChannelName,
            onSignIn = {
                navigator.clear()
                overlays.webSession = WebSessionMode.SIGN_IN
            },
            onSwitchChannel = {
                viewModel.loadChannels()
                overlays.showAccountSelector = true
            },
            onSignOut = { viewModel.signOut() },
            onOpenListenBrainzLogin = { overlays.showListenBrainzLogin = true },
            onOpenLastfmLogin = { overlays.showLastfmLogin = true },
            onOpenDiscord = { navigator.push(AppRoute.Discord) },
            onOpenSpotify = { navigator.push(AppRoute.Spotify) },
            contentPadding = scroll.listPadding,
        )
        AppRoute.Sources -> SourcesScreen(
            contentPadding = scroll.listPadding,
            onEditSource = { overlays.editingSource = it },
            onChooseSourceQuality = { overlays.choosingSourceQuality = it },
            onExplainOutdatedAddon = { overlays.explainingOutdatedAddon = it },
            onEditWebDav = { overlays.showWebDavEditor = true },
            onEditSmb = { overlays.showSmbEditor = true },
            onConfirmJioSaavn = { overlays.confirmJioSaavn = true },
        )
        AppRoute.Downloads -> DownloadManagerPage(
            onCleared = { navigator.pop() },
            contentPadding = scroll.listPadding,
        )
        AppRoute.WearOs -> WearOsScreen(contentPadding = scroll.listPadding)
        AppRoute.ListenTogether -> ListenTogetherScreen(
            signedIn = feeds.signedIn,
            inviteCode = overlays.activeJamInviteCode,
            inviteServer = overlays.activeJamInviteServer,
            onInviteHandled = {
                overlays.activeJamInviteCode = null
                overlays.activeJamInviteServer = null
            },
            onSignIn = {
                navigator.clear()
                overlays.webSession = WebSessionMode.SIGN_IN
            },
            contentPadding = scroll.listPadding,
            onEditServer = { overlays.editingPartyServer = true },
        )
        AppRoute.Equalizer -> EqualizerScreen(
            contentPadding = scroll.listPadding,
            onOpenPresets = { navigator.push(AppRoute.EqualizerPresets) },
        )
        AppRoute.EqualizerPresets -> EqualizerPresetPage(
            onChosen = { navigator.pop() },
            contentPadding = scroll.listPadding,
        )
        is AppRoute.SettingsChoice -> SettingsChoicePage(
            choice = pageRoute.choice,
            onChosen = { navigator.pop() },
            onConfirmBitPerfect = { overlays.confirmBitPerfect = it },
            contentPadding = scroll.listPadding,
        )
        AppRoute.Settings, is AppRoute.SettingsSection -> SettingsScreen(
            section = (pageRoute as? AppRoute.SettingsSection)?.category,
            onOpenSection = { navigator.push(AppRoute.SettingsSection(it)) },
            onOpenChoice = { navigator.push(AppRoute.SettingsChoice(it)) },
            windowWidth = windowWidth,
            signedIn = feeds.signedIn,
            account = feeds.account,
            onSignIn = {
                navigator.clear()
                overlays.webSession = WebSessionMode.SIGN_IN
            },
            onSignOut = { viewModel.signOut() },
            onAccountScrobbling = { navigator.push(AppRoute.AccountScrobbling) },
            onEqualizer = { navigator.push(AppRoute.Equalizer) },
            onOpenReplay = {
                overlays.replayLandingPage = ReplayStoryPage.INTRO
                navigator.push(AppRoute.Replay)
            },
            onLyricsSources = { overlays.showLyricsSources = true },
            onTranslationLanguage = { overlays.showTranslationLanguage = true },
            onSources = { navigator.push(AppRoute.Sources) },
            onListenTogether = { navigator.push(AppRoute.ListenTogether) },
            onWearOs = { navigator.push(AppRoute.WearOs) },
            onSpotifyCanvasAuth = { overlays.showSpotifyCanvasAuth = true },
            onAppLanguage = { overlays.showAppLanguage = true },
            onEditSignatureApi = { overlays.editingSignatureApi = true },
            onConfirmBitPerfect = { overlays.confirmBitPerfect = it },
            onConfirmFixedVolume = { overlays.confirmFixedVolume = true },
            onBack = { navigator.pop() },
            contentPadding = scroll.listPadding,
        )
        else -> Unit
    }
}

@Composable
private fun DetailDestinationPage(
    page: DetailPage,
    pageDetailListState: LazyListState,
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    playback: MainPlaybackActions,
    downloads: MainDownloadActions,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val player = playback.player
    val folder = if (page.browseId.isDeviceFolder()) {
        val isDownloads = page.browseId == "local:downloads"
        val isCache = page.browseId == CACHE_FOLDER_BROWSE_ID
        val isDevice = page.browseId == "local:all"
        val folderSongs = (page.songs as? UiState.Success)?.data.orEmpty()
        val downloadCollections = remember(folderSongs, feeds.savedCollections, isDownloads) {
            if (isDownloads) Downloads.collectionsAmong(folderSongs) else emptyList()
        }
        LocalFolder(
            state = scroll.localFolderStates.getOrPut(page.browseId) { LocalFolderState() },
            sort = nav.folderSortOf(page.browseId),
            viewType = nav.folderViewTypeOf(page.browseId),
            collections = downloadCollections,
            selectable = isDownloads || isCache,
            onOpenGroup = viewModel::openLocalGroup,
            onGroupLongPress = { group ->
                overlays.openBrowseMenu(
                    BrowseTarget(
                        browseId = group.browseId,
                        title = group.title,
                        subtitle = group.subtitle,
                        thumbnailUrl = group.thumbnailUrl,
                        type = group.type,
                        songs = group.songs,
                        downloadId = group.downloadId,
                    ),
                )
            },
            onDelete = if (isDownloads) {
                { selected ->
                    scope.launch {
                        selected.forEach { song -> Downloads.delete(context, song.videoId) }
                    }
                }
            } else {
                null
            },
            onUpload = if (isDownloads &&
                com.music.bitchord.data.webdav.WebDavConfig.isConfigured(feeds.webdavUrl)
            ) {
                { selected -> downloads.uploadToWebDav(selected) }
            } else {
                null
            },
            onDownload = if (isCache) downloads.downloadFromCache else null,
            onRemoveFromCache = if (isCache) downloads.removeFromCache else null,
            tabs = if (isDevice) LocalFolderTab.DEVICE else LocalFolderTab.BASIC,
            playlists = if (isDevice) feeds.offlinePlaylistGroups else emptyList(),
            onCreatePlaylist = if (isDevice) ({ overlays.creatingOfflinePlaylist = true }) else null,
        )
    } else {
        null
    }

    val withAlbum: (Song) -> Song = { song ->
        if (page.type == BrowseType.ALBUM) {
            song.copy(albumName = song.albumName ?: page.title)
        } else {
            song
        }
    }

    fun playDetailSongs(songs: List<Song>, index: Int) {
        if (ListenTogether.state.value.controlsLocked) {
            playback.refusedByHost()
            return
        }
        val source = QueueSource(page.title, PlaybackSourceType.BROWSE, page.browseId)
        val selected = songs.getOrNull(index)
        val pageSongKeys =
            (page.songs as? UiState.Success)?.data.orEmpty().mapTo(HashSet()) { it.contextEntryKey() }
        val useBounded =
            (page.type == BrowseType.ALBUM || page.type == BrowseType.PLAYLIST) &&
                selected?.contextEntryKey()?.let(pageSongKeys::contains) == true &&
                !CastController.state.value.casting
        val latestPage = if (useBounded) viewModel.takePlaybackContext(page.browseId) else null
        val latestSongs = latestPage
            ?.let { (it.songs as? UiState.Success)?.data.orEmpty() }
            ?.map(withAlbum)
            ?.takeIf { it.isNotEmpty() }
        val queue = latestSongs ?: songs.map(withAlbum)
        if (queue.isEmpty()) return
        val queueIndex = selected?.let { tapped ->
            queue.indexOfFirst { it.contextEntryKey() == tapped.contextEntryKey() }
        }?.takeIf { it >= 0 } ?: index
        playback.playFrom(
            queue,
            queueIndex.coerceIn(queue.indices),
            source,
            boundedContext = useBounded,
            continuationToken = latestPage?.continuation ?: page.continuation,
        )
    }

    val reordering = overlays.reorderTarget?.takeIf { it.browseId == page.browseId }
    DetailScreen(
        page = page,
        currentSong = player.song,
        isPlaying = player.isPlaying,
        listState = pageDetailListState,
        activeShelf = nav.detailActiveShelf,
        onActiveShelfChange = { shelf ->
            if (shelf != null) nav.navigator.push(AppRoute.ArtistShelf(page.browseId, shelf))
            else nav.navigator.pop()
        },
        onSongClick = ::playDetailSongs,
        onSongLongPress = { overlays.openSongMenu(withAlbum(it)) },
        onSongSwipe = { playback.onSongSwipe(withAlbum(it)) },
        onShuffle = { songs ->
            if (songs.isNotEmpty()) {
                QueueShuffle.enableForNextQueue()
                playDetailSongs(songs, songs.indices.random())
            }
        },
        onSectionItemClick = { item ->
            val video = playback.shelfSong(item)?.takeIf { item.browseId == null }
            if (video != null) {
                playback.playRadio(video, QueueSource(page.title, PlaybackSourceType.BROWSE, page.browseId))
            } else {
                item.browseId?.let { id ->
                    viewModel.openDetail(
                        browseId = id,
                        title = item.title,
                        subtitle = item.subtitle,
                        thumbnailUrl = item.thumbnailUrl,
                        type = MainViewModel.browseTypeOf(id),
                    )
                }
            }
        },
        onSectionItemLongPress = playback.onShelfLongPress,
        searching = nav.detailSearching && page.browseId == nav.detail?.browseId,
        onSearchingChange = { nav.detailSearching = it },
        onArtistClick = { id, name ->
            playback.openByName(
                id.takeIf { it.isNotBlank() },
                name,
                context.getString(R.string.artist),
                BrowseType.ARTIST,
            )
        },
        onAddSuggested = { song -> viewModel.addSuggestedSong(page.browseId, song) },
        onToggleLibrary = if (feeds.signedIn) ({ viewModel.toggleLibrary(page.browseId) }) else null,
        onToggleSubscription = if (feeds.signedIn) ({ viewModel.toggleSubscription(page.browseId) }) else null,
        releaseLibrary = feeds.releaseLibrary,
        onLoadReleaseLibrary = viewModel::loadReleaseLibrary,
        onToggleReleaseLibrary = if (feeds.signedIn) viewModel::toggleReleaseLibrary else null,
        songSort = nav.songSort,
        folder = folder,
        contentPadding = scroll.listPadding,
        reorder = if (reordering != null) {
            { palette ->
                ReorderPlaylistPage(
                    playlist = reordering,
                    entries = overlays.reorderEntries,
                    saving = overlays.reorderSaving,
                    palette = palette,
                    contentPadding = PaddingValues(bottom = scroll.listPadding.calculateBottomPadding()),
                    onClose = { overlays.reorderTarget = null },
                    onSave = { reordered ->
                        val original = (overlays.reorderEntries as? UiState.Success)?.data.orEmpty()
                        overlays.reorderSaving = true
                        viewModel.reorderPlaylist(reordering, original, reordered) { saved ->
                            overlays.reorderSaving = false
                            overlays.showQueueNotice(
                                context.getString(if (saved) R.string.playlist_reordered else R.string.reorder_failed),
                            )
                            if (saved && overlays.reorderTarget == reordering) overlays.reorderTarget = null
                        }
                    },
                )
            }
        } else {
            null
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeDestinationTab(
    artworkMotion: Boolean,
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    playback: MainPlaybackActions,
    viewModel: MainViewModel,
) {
    val scope = rememberCoroutineScope()
    val player = playback.player
    HomeScreen(
        state = feeds.visibleHomeState,
        listState = scroll.homeListState,
        currentSong = player.song,
        isPlaying = player.isPlaying,
        title = if (AppSettings.materialUiMode.collectAsStateWithLifecycle().value) {
            "Discover"
        } else {
            stringResource(R.string.listen_now)
        },
        festiveLogo = feeds.diwaliFestive,
        onFestiveLogoClick = { overlays.showDiwali = true },
        signedIn = feeds.signedIn,
        onSignIn = { overlays.webSession = WebSessionMode.SIGN_IN },
        onItemClick = { item, shelfTitle ->
            val song = playback.shelfSong(item)
            val browseId = item.browseId
            when {
                song != null -> playback.playRadio(song, QueueSource(shelfTitle, PlaybackSourceType.HOME))
                browseId != null -> viewModel.openDetail(
                    browseId = browseId,
                    title = item.title,
                    subtitle = item.subtitle,
                    thumbnailUrl = item.thumbnailUrl,
                )
            }
        },
        onItemLongPress = playback.onShelfLongPress,
        onRetry = viewModel::loadHome,
        refreshing = MainViewModel.Feed.HOME in scroll.refreshing,
        onRefresh = { viewModel.refresh(MainViewModel.Feed.HOME) },
        pullState = scroll.homePull,
        contentPadding = scroll.listPadding,
        onLoadMore = viewModel::loadMoreHome,
        loadingMore = feeds.homeLoadingMore,
        recentlyPlayedLoading = feeds.homeRecentlyPlayedLoading && !feeds.hideRecents,
        topPicks = feeds.topPicks,
        topPicksLoading = feeds.topPicksLoading,
        listenerName = AppSettings.userName.value.ifBlank { feeds.account?.name },
        onTopPickClick = playback.openTopPick.takeUnless { feeds.hideTopPicks },
        onMoodClick = playback.playMood.takeUnless { feeds.hideHomeMoods },
        onMoodsShowAll = { nav.navigator.push(AppRoute.Moods) },
        onShuffleMix = {
            val cached = (feeds.visibleHomeState as? UiState.Success)?.data
                ?.flatMap { it.items }
                ?.mapNotNull { playback.shelfSong(it) }
                ?.distinctBy { it.videoId }
                .orEmpty()
            if (cached.isNotEmpty()) {
                QueueShuffle.enableForNextQueue()
                val shuffled = cached.shuffled()
                playback.playFrom(shuffled, 0, QueueSource("Your Mix", PlaybackSourceType.HOME))
            } else {
                val station = feeds.topPicks?.firstOrNull {
                    it.kind == TopPickKind.PERSONAL_STATION || it.kind == TopPickKind.DISCOVERY_STATION
                }
                if (station != null) {
                    scope.launch {
                        val fetched = TopPicks.queue(station).getOrNull().orEmpty()
                        if (fetched.isNotEmpty()) {
                            QueueShuffle.enableForNextQueue()
                            playback.playFrom(fetched.shuffled(), 0, QueueSource("Your Mix", PlaybackSourceType.HOME))
                        }
                    }
                }
            }
        },
        onHeroMixClick = { overlays.showYourMixCard = true },
        artworkMotion = artworkMotion && !overlays.showDiwali,
        onTopPickLongPress = { pick ->
            pick.browseId?.let { browseId ->
                playback.onShelfLongPress(
                    ShelfItem(
                        title = pick.title,
                        subtitle = pick.artistName.orEmpty(),
                        thumbnailUrl = pick.images.firstOrNull(),
                        videoId = null,
                        browseId = browseId,
                    ),
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreDestinationTab(
    feeds: MainFeedState,
    scroll: MainScrollState,
    playback: MainPlaybackActions,
    viewModel: MainViewModel,
) {
    feeds.selectedMoodGenre?.let { category ->
        MoodGenrePlaylistsScreen(
            title = category.title,
            state = feeds.moodGenreShelves,
            listState = scroll.moodGenreListState,
            onItemClick = { item ->
                item.videoId?.let { videoId ->
                    playback.playRadio(
                        Song(
                            videoId = videoId,
                            title = item.title,
                            artist = InnertubeParser.artistFromSubtitle(item.subtitle),
                            thumbnailUrl = item.thumbnailUrl,
                            isVideo = item.isVideo,
                        ),
                        QueueSource(category.title, PlaybackSourceType.EXPLORE, category.browseId),
                    )
                } ?: item.browseId?.let { browseId ->
                    viewModel.openDetail(
                        browseId = browseId,
                        title = item.title,
                        subtitle = item.subtitle,
                        thumbnailUrl = item.thumbnailUrl,
                    )
                }
            },
            onRetry = { viewModel.openMoodGenre(category) },
            contentPadding = scroll.listPadding,
        )
    } ?: ExploreScreen(
        state = feeds.exploreState,
        listState = scroll.exploreListState,
        onCategoryClick = viewModel::openMoodGenre,
        onRetry = viewModel::loadExplore,
        refreshing = MainViewModel.Feed.EXPLORE in scroll.refreshing,
        onRefresh = { viewModel.refresh(MainViewModel.Feed.EXPLORE) },
        pullState = scroll.explorePull,
        contentPadding = scroll.listPadding,
    )
}

@Composable
private fun SearchDestinationTab(
    overlays: MainOverlayState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    playback: MainPlaybackActions,
    downloads: MainDownloadActions,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val player = playback.player
    SearchScreen(
        query = feeds.query,
        onQueryChange = viewModel::onQueryChange,
        filter = feeds.filter,
        currentSong = player.song,
        isPlaying = player.isPlaying,
        onFilterChange = viewModel::onFilterChange,
        results = feeds.results,
        loadingMore = feeds.searchLoadingMore,
        onLoadMore = viewModel::loadMoreSearchResults,
        listState = scroll.searchListState,
        scrollResetTrigger = feeds.searchScrollReset,
        focusRequested = overlays.searchFocusRequested,
        onFocusHandled = { overlays.searchFocusRequested = false },
        onSongClick = { songs, index ->
            songs.getOrNull(index)?.let { song ->
                viewModel.recordEntity(
                    SearchHistoryEntity(
                        id = song.videoId,
                        title = song.title,
                        subtitle = song.artist.ifEmpty { "" },
                        artworkUrl = song.thumbnailUrl,
                        entityType = EntityType.TRACK,
                    ),
                )
                playback.playRadio(song, QueueSource(scroll.searchLabel, PlaybackSourceType.SEARCH))
            }
        },
        onSongLongPress = overlays::openSongMenu,
        onSongSwipe = playback.onSongSwipe,
        onTopResultPlay = { song ->
            viewModel.recordEntity(
                SearchHistoryEntity(
                    id = song.videoId,
                    title = song.title,
                    subtitle = song.artist.ifEmpty { "" },
                    artworkUrl = song.thumbnailUrl,
                    entityType = EntityType.TRACK,
                ),
            )
            playback.playRadio(song, QueueSource(scroll.searchLabel, PlaybackSourceType.SEARCH))
        },
        onTopResultPlaylist = { song ->
            viewModel.recordEntity(
                SearchHistoryEntity(
                    id = song.videoId,
                    title = song.title,
                    subtitle = song.artist.ifEmpty { "" },
                    artworkUrl = song.thumbnailUrl,
                    entityType = EntityType.TRACK,
                ),
            )
            viewModel.loadPlaylists()
            overlays.playlistTarget = song
        },
        onBrowseClick = { item ->
            viewModel.recordEntity(
                SearchHistoryEntity(
                    id = item.browseId ?: "",
                    title = item.title,
                    subtitle = item.subtitle.ifBlank { "" },
                    artworkUrl = item.thumbnailUrl,
                    entityType = when (item.type) {
                        BrowseType.ALBUM -> EntityType.ALBUM
                        BrowseType.ARTIST -> EntityType.ARTIST
                        BrowseType.PLAYLIST -> EntityType.PLAYLIST
                        BrowseType.OTHER -> EntityType.OTHER
                    },
                ),
            )
            viewModel.openDetail(
                browseId = item.browseId,
                title = item.title,
                subtitle = item.subtitle,
                thumbnailUrl = item.thumbnailUrl,
                type = item.type,
            )
        },
        onBrowseLongPress = { item ->
            if (item.type != BrowseType.ARTIST) {
                overlays.openBrowseMenu(
                    BrowseTarget(
                        browseId = item.browseId,
                        title = item.title,
                        subtitle = item.subtitle,
                        thumbnailUrl = item.thumbnailUrl,
                        type = item.type,
                        downloadId = feeds.downloadIdFor(item.browseId),
                    ),
                )
            }
        },
        history = feeds.searchHistory,
        suggestions = feeds.searchSuggestions,
        typeaheadResults = viewModel.typeaheadResults.collectAsStateWithLifecycle().value,
        onSubmit = viewModel::submitSearch,
        onSuggestionClick = viewModel::searchFor,
        onHistoryClick = { entity ->
            when (entity.entityType) {
                EntityType.TRACK -> playback.playRadio(
                    Song(
                        videoId = entity.id,
                        title = entity.title,
                        artist = entity.subtitle,
                        thumbnailUrl = entity.artworkUrl,
                    ),
                    QueueSource(entity.title, PlaybackSourceType.SEARCH),
                )
                EntityType.ALBUM, EntityType.ARTIST, EntityType.PLAYLIST, EntityType.OTHER -> {
                    viewModel.openDetail(
                        browseId = entity.id,
                        title = entity.title,
                        subtitle = entity.subtitle,
                        thumbnailUrl = entity.artworkUrl,
                    )
                }
            }
        },
        onHistoryRemove = viewModel::removeSearch,
        onHistoryClear = viewModel::clearSearchHistory,
        onTypeaheadLongPress = overlays::openSongMenu,
        contentPadding = scroll.listPadding,
        topPadding = com.music.bitchord.ui.components.topBarHeight() + 4.dp,
        showField = false,
        sourceSwitcher = {
            SegmentedControl(
                options = listOf(
                    stringResource(R.string.search_source_youtube),
                    stringResource(R.string.library),
                    stringResource(R.string.search_source_addons),
                ),
                selectedIndex = feeds.searchSource.ordinal,
                onSelect = { index -> downloads.onSearchSourceChange(SearchSource.entries[index]) },
            )
        },
        searchingLibrary = feeds.searchSource == SearchSource.LIBRARY,
        libraryResults = feeds.libraryResults,
        searchingAddons = feeds.searchSource == SearchSource.ADDONS,
        addonResults = feeds.addonResults,
        isBlocked = { song ->
            val configId = SourceRegistry.parseTrackKey(song.videoId)?.first
            !SourceRegistry.permitsForPlayback(configId, feeds.addonCeiling)
        },
        onBlockedSongClick = { song -> overlays.blockedAddonSong = song },
        addonHistory = viewModel.addonSearchHistory.collectAsStateWithLifecycle().value,
        onAddonHistoryClick = { term ->
            viewModel.onQueryChange(term)
            viewModel.submitSearch()
        },
        onAddonHistoryRemove = viewModel::removeAddonSearch,
        onAddonHistoryClear = viewModel::clearAddonSearchHistory,
        onLibrarySongClick = { songs, index ->
            playback.playFrom(
                songs,
                index,
                if (feeds.searchSource == SearchSource.ADDONS) {
                    QueueSource(
                        context.getString(R.string.search_source_addons),
                        PlaybackSourceType.BROWSE,
                        "addons:search",
                    )
                } else {
                    QueueSource(
                        context.getString(R.string.local_music),
                        PlaybackSourceType.BROWSE,
                        "local:all",
                    )
                },
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryDestinationTab(
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    playback: MainPlaybackActions,
    downloads: MainDownloadActions,
    viewModel: MainViewModel,
) {
    LibraryScreen(
        signedIn = feeds.signedIn,
        state = feeds.libraryState,
        listState = scroll.libraryListState,
        onShelfItemClick = downloads.onLibraryItemClick,
        onShelfItemLongPress = playback.onBrowseLongPress,
        onNewPlaylist = { overlays.creatingPlaylist = true },
        onImportSpotifyPlaylist = { overlays.showSpotifyImportDialog = true },
        onShowAll = { shelf -> nav.navigator.push(AppRoute.LibraryShowAll(shelf)) },
        replay = {
            LibraryReplayEntry(
                cards = feeds.replayCards,
                loading = feeds.replay.loading,
                holder = feeds.account?.name.orEmpty(),
                memberSince = feeds.replay.memberSince,
                onOpenReplay = { page ->
                    overlays.replayLandingPage = page
                    nav.navigator.push(AppRoute.Replay)
                },
            )
        },
        onSignIn = { overlays.webSession = WebSessionMode.SIGN_IN },
        onRetry = viewModel::loadLibrary,
        refreshing = MainViewModel.Feed.LIBRARY in scroll.refreshing,
        onRefresh = { viewModel.refresh(MainViewModel.Feed.LIBRARY) },
        pullState = scroll.libraryPull,
        contentPadding = scroll.listPadding,
        links = libraryLinks(),
        deviceItems = libraryDeviceItems(feeds.downloadedReleases) + feeds.localPlaylistItems,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxScope.MainTopChrome(
    chrome: MainChromeState,
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    viewModel: MainViewModel,
) {
    val currentDetail = nav.detail
    val isDetailVisible = currentDetail != null &&
        (currentDetail.type == BrowseType.ALBUM ||
            currentDetail.type == BrowseType.PLAYLIST ||
            currentDetail.type == BrowseType.ARTIST) &&
        !nav.isLocalDetail
    val chromePageColor = if (isDetailVisible) {
        feeds.detailPalette.background
    } else {
        MaterialTheme.colorScheme.background
    }

    val artistListState = currentDetail
        ?.takeIf { isDetailVisible && it.type == BrowseType.ARTIST }
        ?.let { scroll.detailListStates.getOrPut(it.browseId) { LazyListState() } }
    val homeHeadingVisible = nav.route == null && nav.detail == null && overlays.selectedTab == TAB_HOME
    val headerListState = artistListState ?: scroll.homeListState.takeIf { homeHeadingVisible }
    val fadeStart = if (artistListState != null) TOP_FADE_START else 0f
    val fadeRamp = if (artistListState != null) TOP_FADE_RAMP else 1f
    val topFadeAlpha by remember(headerListState, fadeStart, fadeRamp) {
        derivedStateOf {
            val list = headerListState ?: return@derivedStateOf 1f
            if (list.firstVisibleItemIndex > 0) return@derivedStateOf 1f
            val header = list.layoutInfo.visibleItemsInfo
                .firstOrNull { it.index == 0 }?.size?.toFloat()
                ?: return@derivedStateOf 0f
            ((list.firstVisibleItemScrollOffset - header * fadeStart) /
                (header * fadeRamp)).coerceIn(0f, 1f)
        }
    }
    val route = nav.route
    val isSettingsPage = route == AppRoute.Settings ||
        route is AppRoute.SettingsSection ||
        route is AppRoute.SettingsChoice ||
        (route != null && AppRoute.Settings in nav.navigator)
    if (!isSettingsPage) {
        if (chrome.materialUiMode || (chrome.progressiveBlur && chrome.glassActive && !chrome.reduceDynamicBlur)) {
            TopFadeBlur(
                hazeState = chrome.hazeState,
                pageColor = chromePageColor,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer { alpha = topFadeAlpha },
            )
        } else {
            BottomFadeScrim(
                pageColor = chromePageColor,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .rotate(180f)
                    .graphicsLayer { alpha = topFadeAlpha },
            )
        }
    }

    val goBack: () -> Unit = {
        if (!nav.navigator.pop() && !viewModel.closeMoodGenre()) {
            overlays.selectedTab = TAB_HOME
        }
    }

    FrostedTopBar(
        title = resolveTopBarTitle(nav, overlays, feeds, scroll),
        transparentBackdrop = true,
        artworkPageChrome = true,
        backButtonHazeState = chrome.hazeState,
        trailingTitle = if (currentDetail != null && nav.detailActiveShelf != null) currentDetail.title else null,
        scrolled = {
            when {
                route == AppRoute.Replay -> scroll.replayScrolled
                route != null -> true
                currentDetail != null && nav.detailActiveShelf != null -> true
                currentDetail != null -> scroll.detailScrolled
                feeds.selectedMoodGenre != null -> true
                else -> scroll.scrolled || overlays.selectedTab == TAB_SEARCH
            }
        },
        refreshing = scroll.currentFeed != null && scroll.currentFeed in scroll.refreshing,
        pullFraction = { scroll.currentPull?.distanceFraction ?: 0f },
        onBack = when {
            isSettingsPage -> null
            route != null || currentDetail != null || feeds.selectedMoodGenre != null -> goBack
            else -> null
        },
        modifier = Modifier.align(Alignment.TopCenter),
        accessory = if (
            overlays.selectedTab == TAB_SEARCH &&
            route == null &&
            currentDetail == null &&
            feeds.selectedMoodGenre == null
        ) {
            {
                MainTopBarSearchAccessory(overlays = overlays, feeds = feeds, viewModel = viewModel)
            }
        } else {
            null
        },
        actions = {
            MainTopBarActions(
                overlays = overlays,
                nav = nav,
                feeds = feeds,
                viewModel = viewModel,
            )
        },
    )
}

@Composable
private fun resolveTopBarTitle(
    nav: MainNavState,
    overlays: MainOverlayState,
    feeds: MainFeedState,
    scroll: MainScrollState,
): String = when (val route = nav.route) {
    AppRoute.Spotify -> stringResource(R.string.spotify)
    AppRoute.Downloads -> stringResource(R.string.downloads)
    AppRoute.Discord -> "Discord"
    AppRoute.History -> stringResource(R.string.history)
    is AppRoute.LibraryShowAll -> route.shelf.title
    is AppRoute.ArtistShelf -> route.shelf.title
    AppRoute.Moods -> stringResource(R.string.find_your_mood)
    AppRoute.AccountScrobbling -> stringResource(R.string.account_scrobbling)
    AppRoute.Sources -> stringResource(R.string.sources)
    AppRoute.ListenTogether -> stringResource(R.string.listen_together)
    AppRoute.WearOs -> stringResource(R.string.wear_os_page_title)
    AppRoute.Equalizer -> stringResource(R.string.equalizer)
    AppRoute.Settings -> stringResource(R.string.settings)
    is AppRoute.SettingsSection -> stringResource(route.category.title)
    is AppRoute.SettingsChoice -> stringResource(route.choice.title)
    AppRoute.EqualizerPresets -> stringResource(R.string.equalizer_preset)
    AppRoute.Replay -> stringResource(R.string.replay)
    null -> {
        val currentDetail = nav.detail
        when {
            currentDetail != null && nav.detailActiveShelf != null -> nav.detailActiveShelf?.title.orEmpty()
            currentDetail != null -> currentDetail.title
            feeds.selectedMoodGenre != null -> feeds.selectedMoodGenre?.title.orEmpty()
            else -> scroll.tabs[overlays.selectedTab].let {
                if (it.label == "Play") stringResource(R.string.listen_now) else it.label
            }
        }
    }
}

@Composable
private fun MainTopBarSearchAccessory(
    overlays: MainOverlayState,
    feeds: MainFeedState,
    viewModel: MainViewModel,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(overlays.searchFocusRequested) {
        if (overlays.searchFocusRequested) {
            overlays.searchFieldFocus.requestFocus()
            keyboard?.show()
            overlays.searchFocusRequested = false
        }
    }
    SearchField(
        query = feeds.query,
        onQueryChange = viewModel::onQueryChange,
        onSubmit = viewModel::submitSearch,
        focusRequester = overlays.searchFieldFocus,
        placeholder = org.jetbrains.compose.resources.stringResource(
            if (feeds.searchSource == SearchSource.LIBRARY) {
                SharedRes.string.search_library_hint
            } else if (feeds.searchSource == SearchSource.ADDONS) {
                SharedRes.string.search_addons_hint
            } else {
                SharedRes.string.search_hint
            },
        ),
    )
}

@Composable
private fun MainTopBarActions(
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val route = nav.route
    val detail = nav.detail
    if (route == AppRoute.ListenTogether) {
        ListenTogetherPingIndicator(feeds.partyServerStatus)
    }
    if (route == null && detail == null && overlays.selectedTab == TAB_HOME) {
        feeds.updateNotice?.let { update ->
            IconButton(onClick = { overlays.showUpdateDialog = true }) {
                Icon(
                    Icons.Rounded.Upgrade,
                    contentDescription = stringResource(R.string.update_available, update.version),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
    if (!(route != null && AppRoute.Settings in nav.navigator)) {
        if (route == null && detail == null && overlays.selectedTab == TAB_LIBRARY) {
            IconButton(
                onClick = {
                    nav.navigator.push(AppRoute.History)
                    viewModel.loadHistory()
                },
            ) {
                Icon(
                    Icons.Rounded.History,
                    contentDescription = stringResource(R.string.listening_history),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (route is AppRoute.LibraryShowAll) {
            Box {
                IconButton(onClick = { overlays.librarySortMenuOpen = true }) {
                    Icon(
                        Icons.Rounded.Sort,
                        contentDescription = stringResource(R.string.sort_library),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                DropdownMenu(
                    expanded = overlays.librarySortMenuOpen,
                    onDismissRequest = { overlays.librarySortMenuOpen = false },
                ) {
                    LibrarySort.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.localizedLabel()) },
                            trailingIcon = if (option == feeds.librarySort) {
                                { Icon(Icons.Rounded.Check, contentDescription = null) }
                            } else {
                                null
                            },
                            onClick = {
                                AppSettings.setLibrarySort(option)
                                overlays.librarySortMenuOpen = false
                            },
                        )
                    }
                }
            }
        }
        if ((nav.isReleaseDetail || nav.isLocalDetail) && detail != null) {
            val releaseSongs = (detail.songs as? UiState.Success)?.data.orEmpty()
            if (releaseSongs.isNotEmpty()) {
                IconButton(onClick = { nav.detailSearching = !nav.detailSearching }) {
                    Icon(
                        if (nav.detailSearching) Icons.Rounded.Close else BitChordIcons.Search,
                        contentDescription = stringResource(
                            if (nav.detailSearching) R.string.close_search else R.string.search_this_list,
                        ),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            IconButton(
                onClick = {
                    if (nav.isLocalDetail) overlays.folderMenuOpen = true else overlays.songSortMenuOpen = true
                },
            ) {
                Icon(
                    Icons.Rounded.Sort,
                    contentDescription = stringResource(
                        if (nav.isLocalDetail) R.string.sort_music else R.string.sort_songs,
                    ),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (nav.isReleaseDetail && releaseSongs.isNotEmpty()) {
                IconButton(
                    modifier = Modifier.menuAnchor(),
                    onClick = {
                        overlays.openBrowseMenu(
                            BrowseTarget(
                                browseId = detail.browseId,
                                title = detail.title,
                                subtitle = detail.subtitle,
                                thumbnailUrl = detail.thumbnailUrl,
                                type = detail.type,
                                songs = releaseSongs.sortedForDetail(nav.songSort).map { song ->
                                    if (detail.type == BrowseType.ALBUM) {
                                        song.copy(albumName = song.albumName ?: detail.title)
                                    } else {
                                        song
                                    }
                                },
                                fromCard = false,
                                downloadId = feeds.downloadIdFor(detail.browseId),
                            ),
                        )
                    },
                ) {
                    Icon(
                        Icons.Rounded.MoreHoriz,
                        contentDescription = stringResource(R.string.more),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        if (detail != null && !nav.isLocalDetail && detail.type == BrowseType.ARTIST &&
            nav.detailActiveShelf == null && detail.browseId.startsWith("UC")
        ) {
            IconButton(
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "https://music.youtube.com/channel/${detail.browseId}",
                        )
                    }
                    context.startActivity(Intent.createChooser(sendIntent, detail.title))
                },
            ) {
                Icon(
                    Icons.Rounded.IosShare,
                    contentDescription = stringResource(R.string.share),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (route != AppRoute.Downloads) {
            TopBarDownloadButton(onClick = { nav.navigator.push(AppRoute.Downloads) })
        }
        if (!nav.isReleaseDetail) {
            TopBarAccountButton(
                account = feeds.account,
                onClick = {
                    if (feeds.signedIn) {
                        viewModel.loadChannels()
                        overlays.showAccountSelector = true
                    } else {
                        nav.navigator.push(AppRoute.Settings)
                    }
                },
                onSwipeProfile = { forward -> viewModel.stepProfile(forward) },
            )
        }
    }
}

@Composable
private fun ListenTogetherPingIndicator(partyServerStatus: ServerConnectionState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp),
    ) {
        when (partyServerStatus) {
            is ServerConnectionState.CustomFallback -> {
                Icon(
                    Icons.Rounded.CloudOff,
                    contentDescription = stringResource(
                        R.string.listen_together_top_bar_fallback,
                        stringResource(R.string.listen_together_ping, partyServerStatus.latencyMs),
                    ),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.listen_together_ping, partyServerStatus.latencyMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                )
            }
            is ServerConnectionState.CustomOnline -> Text(
                text = stringResource(R.string.listen_together_ping, partyServerStatus.latencyMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            is ServerConnectionState.DefaultOnline -> Text(
                text = stringResource(R.string.listen_together_ping, partyServerStatus.latencyMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            ServerConnectionState.Checking -> Text(
                text = stringResource(R.string.listen_together_server_checking),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            ServerConnectionState.Offline -> Text(
                text = stringResource(R.string.listen_together_server_offline_dash),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun BoxScope.MainBottomChrome(
    chrome: MainChromeState,
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    playerDock: PlayerDockState,
    playback: MainPlaybackActions,
    viewModel: MainViewModel,
) {
    val player = playback.player
    val currentDetail = nav.detail
    val isDetailVisible = currentDetail != null &&
        (currentDetail.type == BrowseType.ALBUM ||
            currentDetail.type == BrowseType.PLAYLIST ||
            currentDetail.type == BrowseType.ARTIST) &&
        !nav.isLocalDetail
    val chromePageColor = if (isDetailVisible) {
        feeds.detailPalette.background
    } else {
        MaterialTheme.colorScheme.background
    }
    if (chrome.materialUiMode) {
        BottomFadeScrim(
            withMiniPlayer = player.song != null,
            pageColor = chromePageColor,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    val floatingBarsHeight = remember { mutableIntStateOf(0) }
    FloatingBarsTapGuard(
        barsHeight = { floatingBarsHeight.intValue },
        modifier = Modifier.align(Alignment.BottomCenter),
    )

    val onTabSelected: (Int) -> Unit = { index ->
        nav.navigator.clear()
        viewModel.closeMoodGenre()
        overlays.selectedTab = index
        if (index == TAB_SEARCH) {
            viewModel.onQueryChange("")
            overlays.searchFocusRequested = true
        }
    }

    AnimatedVisibility(
        visible = nav.route == null && nav.detail == null &&
            feeds.selectedMoodGenre == null && overlays.selectedTab != TAB_SEARCH,
        enter = fadeIn(tween(240)),
        exit = fadeOut(tween(200)),
        modifier = Modifier
            .align(Alignment.TopStart)
            .statusBarsPadding()
            .padding(start = 16.dp, top = 12.dp),
    ) {
        AiPlaylistPillButton(onClick = { overlays.showAiPlaylistMaker = true })
    }

    AnimatedContent(
        targetState = chrome.materialUiMode,
        transitionSpec = {
            (fadeIn(tween(340)) + scaleIn(
                initialScale = 0.94f,
                animationSpec = spring(dampingRatio = 0.80f, stiffness = 340f),
            )) togetherWith
                (fadeOut(tween(240)) + scaleOut(targetScale = 1.03f))
        },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .widthIn(max = FLOATING_BAR_MAX_WIDTH)
            .fillMaxWidth(),
        label = "bottomBarModeTransition",
    ) { isMaterial ->
        val controller = playback.controller
        val onExpand: () -> Unit = if (playerDock.reducePlayerMotion) {
            { overlays.showNowPlaying = true }
        } else {
            playerDock.playerSheetMotion::open
        }
        val pull = playerDock.playerSheetMotion.takeIf { !playerDock.reducePlayerMotion }
        if (!isMaterial) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { floatingBarsHeight.intValue = it.height },
            ) {
                QueueActionNoticeHost(overlays.queueNotice)
                GlassNavBar(
                    tabs = scroll.tabs,
                    selectedIndex = overlays.selectedTab,
                    onTabSelected = onTabSelected,
                    scrollConnection = chrome.navBarScroll,
                    song = player.song,
                    isPlaying = player.isPlaying,
                    isLoading = playback.playPauseBusy,
                    onPlayPause = { playback.togglePlayPause() },
                    onNext = { controller?.seekToNextMediaItem() },
                    onPrevious = { controller?.seekToPrevious() },
                    onExpand = onExpand,
                    controlsLocked = playback.controlsLocked,
                    onBlockedControl = playback.showHostOnlyNotice,
                    dock = playerDock.activeDock,
                    pull = pull,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { floatingBarsHeight.intValue = it.height },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                QueueActionNoticeHost(overlays.queueNotice)
                player.song?.let { song ->
                    ExpressiveMiniPlayer(
                        song = song,
                        isPlaying = player.isPlaying,
                        isLoading = playback.playPauseBusy,
                        hazeState = chrome.hazeState,
                        onPlayPause = { playback.togglePlayPause() },
                        onNext = { controller?.seekToNextMediaItem() },
                        onPrevious = { controller?.seekToPrevious() },
                        onExpand = onExpand,
                        controlsLocked = playback.controlsLocked,
                        onBlockedControl = playback.showHostOnlyNotice,
                        dock = playerDock.activeDock,
                        pull = pull,
                        progressProvider = {
                            if (player.durationMs > 0L) {
                                player.position.positionMs.toFloat() / player.durationMs.toFloat()
                            } else {
                                0f
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(10.dp))
                }
                ExpressiveNavBar(
                    tabs = scroll.tabs,
                    selectedIndex = overlays.selectedTab,
                    hazeState = chrome.hazeState,
                    onTabSelected = onTabSelected,
                )
            }
        }
    }
}
