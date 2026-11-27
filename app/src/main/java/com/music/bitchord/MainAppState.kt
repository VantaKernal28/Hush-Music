package com.music.bitchord

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.auth.GoogleAccountSession
import com.music.bitchord.auth.WebSessionMode
import com.music.bitchord.data.LocalGroups
import com.music.bitchord.data.OfflinePlaylist
import com.music.bitchord.data.OfflinePlaylists
import com.music.bitchord.data.YtMusicRepository
import com.music.bitchord.data.canvas.AppleArtistArtRepository
import com.music.bitchord.data.canvas.keyColors
import com.music.bitchord.data.listentogether.JamInviteLink
import com.music.bitchord.data.listentogether.ListenTogether
import com.music.bitchord.data.listentogether.ServerConnectionState
import com.music.bitchord.data.lyrics.LyricLine
import com.music.bitchord.data.lyrics.LyricsSource
import com.music.bitchord.data.model.Account
import com.music.bitchord.data.model.BrowseType
import com.music.bitchord.data.model.DetailPage
import com.music.bitchord.data.model.HomeShelf
import com.music.bitchord.data.model.LibraryPage
import com.music.bitchord.data.model.LibraryState
import com.music.bitchord.data.model.LikeStatus
import com.music.bitchord.data.model.MoodGenre
import com.music.bitchord.data.model.MoodGenreSection
import com.music.bitchord.data.model.SearchFilter
import com.music.bitchord.data.model.SearchHistoryEntity
import com.music.bitchord.data.model.SearchResult
import com.music.bitchord.data.model.ShelfItem
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.TopPick
import com.music.bitchord.data.model.UiState
import com.music.bitchord.data.model.UserPlaylist
import com.music.bitchord.data.model.withoutVideos
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.AudioQuality
import com.music.bitchord.data.settings.BitPerfectMode
import com.music.bitchord.data.settings.LibrarySort
import com.music.bitchord.data.settings.LibraryViewType
import com.music.bitchord.data.settings.LocalMusicSort
import com.music.bitchord.data.settings.SongSort
import com.music.bitchord.data.sources.SourceConfig
import com.music.bitchord.data.sources.SourceRegistry
import com.music.bitchord.data.spotify.LocalPlaylist
import com.music.bitchord.data.spotify.LocalPlaylistStore
import com.music.bitchord.data.toGroup
import com.music.bitchord.download.Downloads
import com.music.bitchord.download.SavedCollection
import com.music.bitchord.playback.PlayerDeepLink
import com.music.bitchord.playback.QueueShuffle
import com.music.bitchord.playback.autoplayEnabledFor
import com.music.bitchord.ui.LyricsProviderState
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.SearchSource
import com.music.bitchord.ui.components.BottomTab
import com.music.bitchord.ui.components.BrowseTarget
import com.music.bitchord.ui.components.HeldItem
import com.music.bitchord.ui.components.LocalLiquidGlassEnabled
import com.music.bitchord.ui.components.LongPressOrigin
import com.music.bitchord.ui.components.MenuAnchor
import com.music.bitchord.ui.components.QueueActionNotice
import com.music.bitchord.ui.components.VolumeHud
import com.music.bitchord.ui.components.backdrop.backdrops.LayerBackdrop
import com.music.bitchord.ui.components.backdrop.backdrops.SnapshotBackdrop
import com.music.bitchord.ui.components.backdrop.backdrops.rememberLayerBackdrop
import com.music.bitchord.ui.components.backdrop.backdrops.rememberSnapshotBackdrop
import com.music.bitchord.ui.components.floatingtabbar.FloatingTabBarInlineBehavior
import com.music.bitchord.ui.components.floatingtabbar.FloatingTabBarScrollConnection
import com.music.bitchord.ui.components.floatingtabbar.rememberFloatingTabBarScrollConnection
import com.music.bitchord.ui.components.isGlassSupported
import com.music.bitchord.ui.components.topBarContentPadding
import com.music.bitchord.ui.festive.DiwaliGate
import com.music.bitchord.ui.icons.BitChordIcons
import com.music.bitchord.ui.navigation.AppDestination
import com.music.bitchord.ui.navigation.AppNavigator
import com.music.bitchord.ui.navigation.AppRoute
import com.music.bitchord.ui.navigation.RouteEntry
import com.music.bitchord.ui.player.LyricsShareRequest
import com.music.bitchord.ui.player.PlayerDock
import com.music.bitchord.ui.player.PlayerSheetMotion
import com.music.bitchord.ui.replay.ReplayHeroCard
import com.music.bitchord.ui.replay.ReplayPeriod
import com.music.bitchord.ui.replay.ReplayState
import com.music.bitchord.ui.replay.ReplayStoryPage
import com.music.bitchord.ui.replay.cards
import com.music.bitchord.ui.replay.rememberReplayState
import com.music.bitchord.ui.screens.CACHE_FOLDER_BROWSE_ID
import com.music.bitchord.ui.screens.DiscordDialog
import com.music.bitchord.ui.screens.LocalFolderState
import com.music.bitchord.ui.theme.ArtworkPalette
import com.music.bitchord.ui.theme.rememberArtworkPalette
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay

/** Holds backdrop recording, glass toggles, and UI-mode morph state for [BitChordApp]. */
@Stable
internal class MainChromeState(
    val hazeState: HazeState,
    val appBackdrop: LayerBackdrop,
    val playerBackdrop: SnapshotBackdrop,
    val livePlayerBackdrop: LayerBackdrop,
    val pageBackdrop: SnapshotBackdrop,
    val navBarScroll: FloatingTabBarScrollConnection,
) {
    var playerLiveCanvas by mutableStateOf(false)
    var glassActive by mutableStateOf(false)
    var reduceDynamicBlur by mutableStateOf(false)
    var progressiveBlur by mutableStateOf(false)
    var expressiveUiEnabled by mutableStateOf(true)
    var materialUiMode by mutableStateOf(false)
    var modeMorphScale by mutableStateOf(1f)
    var modeMorphBlur by mutableStateOf(0.dp)
    var modeMorphAlpha by mutableStateOf(1f)

    val glassSamplesBackdrop: Boolean
        get() = glassActive && !reduceDynamicBlur
}

@Composable
internal fun rememberMainChromeState(appBackdrop: LayerBackdrop): MainChromeState {
    val hazeState = remember { HazeState() }
    val playerBackdrop = rememberSnapshotBackdrop()
    val livePlayerBackdrop = rememberLayerBackdrop()
    val pageBackdrop = rememberSnapshotBackdrop()
    val navBarScroll = rememberFloatingTabBarScrollConnection(
        inlineBehavior = FloatingTabBarInlineBehavior.OnScrollDown,
    )
    val state = remember(appBackdrop, navBarScroll) {
        MainChromeState(
            hazeState = hazeState,
            appBackdrop = appBackdrop,
            playerBackdrop = playerBackdrop,
            livePlayerBackdrop = livePlayerBackdrop,
            pageBackdrop = pageBackdrop,
            navBarScroll = navBarScroll,
        )
    }

    val glassActive = LocalLiquidGlassEnabled.current && isGlassSupported()
    val reduceDynamicBlur by AppSettings.reduceDynamicBlur.collectAsStateWithLifecycle()
    val progressiveBlur by AppSettings.progressiveBlur.collectAsStateWithLifecycle()
    val expressiveUiEnabled by AppSettings.expressiveUiEnabled.collectAsStateWithLifecycle()
    val materialUiMode by AppSettings.materialUiMode.collectAsStateWithLifecycle()

    var isFirstModeCheck by remember { mutableStateOf(true) }
    var modeMorphing by remember { mutableStateOf(false) }
    LaunchedEffect(materialUiMode) {
        if (isFirstModeCheck) {
            isFirstModeCheck = false
        } else {
            modeMorphing = true
            delay(280)
            modeMorphing = false
        }
    }
    val modeMorphScale by animateFloatAsState(
        targetValue = if (modeMorphing) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.80f, stiffness = 380f),
        label = "ModeMorphScale",
    )
    val modeMorphBlur by animateDpAsState(
        targetValue = if (modeMorphing) 10.dp else 0.dp,
        animationSpec = tween(durationMillis = 220),
        label = "ModeMorphBlur",
    )
    val modeMorphAlpha by animateFloatAsState(
        targetValue = if (modeMorphing) 0.84f else 1f,
        animationSpec = tween(durationMillis = 220),
        label = "ModeMorphAlpha",
    )

    state.glassActive = glassActive
    state.reduceDynamicBlur = reduceDynamicBlur
    state.progressiveBlur = progressiveBlur
    state.expressiveUiEnabled = expressiveUiEnabled
    state.materialUiMode = materialUiMode
    state.modeMorphScale = modeMorphScale
    state.modeMorphBlur = modeMorphBlur
    state.modeMorphAlpha = modeMorphAlpha
    return state
}

/** Holds transient modal/menu flags and saveable overlay selections for [BitChordApp]. */
@Stable
internal class MainOverlayState(
    selectedTabState: MutableState<Int>,
    replayLandingPageState: MutableState<ReplayStoryPage>,
    replayStoryState: MutableState<ReplayStoryPage?>,
    showReplayShareState: MutableState<Boolean>,
    replaySharePageState: MutableState<ReplayStoryPage?>,
    showDiwaliState: MutableState<Boolean>,
    diwaliAutoShownState: MutableState<Boolean>,
    updateDialogShownState: MutableState<Boolean>,
    displayModePromptedState: MutableState<Boolean>,
    playlistPromoShownState: MutableState<Boolean>,
    activeJamInviteCodeState: MutableState<String?>,
    activeJamInviteServerState: MutableState<String?>,
    val appLaunchCount: Int,
    val searchFieldFocus: FocusRequester,
) {
    var selectedTab by selectedTabState
    var replayLandingPage by replayLandingPageState
    var replayStory by replayStoryState
    var showReplayShare by showReplayShareState
    var replaySharePage by replaySharePageState
    var showDiwali by showDiwaliState
    var diwaliAutoShown by diwaliAutoShownState
    var updateDialogShown by updateDialogShownState
    var displayModePromptedThisLaunch by displayModePromptedState
    var playlistPromoShownThisLaunch by playlistPromoShownState
    var activeJamInviteCode by activeJamInviteCodeState
    var activeJamInviteServer by activeJamInviteServerState

    var showNowPlaying by mutableStateOf(false)
    var webSession by mutableStateOf<WebSessionMode?>(null)
    var showSpotifyCanvasAuth by mutableStateOf(false)

    var editingSource by mutableStateOf<SourceConfig?>(null)
    var choosingSourceQuality by mutableStateOf<SourceConfig?>(null)
    var explainingOutdatedAddon by mutableStateOf<SourceConfig?>(null)
    var confirmJioSaavn by mutableStateOf(false)
    var confirmBitPerfect by mutableStateOf<BitPerfectMode?>(null)
    var confirmFixedVolume by mutableStateOf(false)
    var blockedAddonSong by mutableStateOf<Song?>(null)
    var addonLeftOutOfDownload by mutableStateOf<Pair<String, SourceRegistry.DownloadExclusion>?>(null)
    var editingPartyServer by mutableStateOf(false)
    var editingSignatureApi by mutableStateOf(false)
    var librarySortMenuOpen by mutableStateOf(false)
    var songSortMenuOpen by mutableStateOf(false)
    var folderMenuOpen by mutableStateOf(false)
    var showLyricsSources by mutableStateOf(false)
    var showAppLanguage by mutableStateOf(false)
    var showTranslationLanguage by mutableStateOf(false)
    var showAccountSelector by mutableStateOf(false)
    var showListenBrainzLogin by mutableStateOf(false)
    var showLastfmLogin by mutableStateOf(false)
    var showWebDavEditor by mutableStateOf(false)
    var showSmbEditor by mutableStateOf(false)
    var showDiscordLogin by mutableStateOf(false)
    var showSpotifyLogin by mutableStateOf(false)
    var discordDialog by mutableStateOf<DiscordDialog?>(null)

    var songActions by mutableStateOf<Song?>(null)
    var showLyricsOffset by mutableStateOf(false)
    var menuFromPlayer by mutableStateOf(false)
    var menuFromHistory by mutableStateOf(false)
    var songMenuOrigin by mutableStateOf<HeldItem?>(null)
    var songMenuAnchor by mutableStateOf<Rect?>(null)
    var linksLoading by mutableStateOf(false)

    var playlistTarget by mutableStateOf<Song?>(null)
    var creatingPlaylist by mutableStateOf(false)
    var offlinePlaylistTarget by mutableStateOf<Song?>(null)
    var creatingOfflinePlaylist by mutableStateOf(false)
    var showSpotifyImportDialog by mutableStateOf(false)
    var showAiPlaylistMaker by mutableStateOf(false)
    var showSongMixer by mutableStateOf(false)

    var browseActions by mutableStateOf<BrowseTarget?>(null)
    var browseMenuOrigin by mutableStateOf<HeldItem?>(null)
    var browseMenuAnchor by mutableStateOf<Rect?>(null)
    var browseRenameInSheet by mutableStateOf(false)
    var reorderTarget by mutableStateOf<UserPlaylist?>(null)
    var reorderEntries by mutableStateOf<UiState<List<Song>>>(UiState.Loading)
    var reorderSaving by mutableStateOf(false)

    var searchFocusRequested by mutableStateOf(false)
    var showUpdateDialog by mutableStateOf(false)
    var showYourMixCard by mutableStateOf(false)
    var lyricsCardTarget by mutableStateOf<LyricsShareRequest?>(null)
    var showDisplayModePrompt by mutableStateOf(false)
    var showStandByDisplayMode by mutableStateOf(false)

    var queueNotice by mutableStateOf<QueueActionNotice?>(null)
    var queueNoticeId by mutableIntStateOf(0)

    fun showQueueNotice(message: String) {
        queueNoticeId += 1
        queueNotice = QueueActionNotice(queueNoticeId, message)
    }

    /** Holding a row anywhere but the player — the menu without the player's rows. */
    fun openSongMenu(song: Song) {
        menuFromPlayer = false
        songMenuOrigin = LongPressOrigin.consume()
        songMenuAnchor = MenuAnchor.consume()
        songActions = song
    }

    /** Opening the three-dot menu from the full player sheet. */
    fun openSongMenuFromPlayer(
        song: Song,
        captureSnapshot: Boolean,
        playerBackdrop: SnapshotBackdrop,
    ) {
        menuFromPlayer = true
        songMenuOrigin = null
        songMenuAnchor = MenuAnchor.consume()
        songActions = song
        if (captureSnapshot) playerBackdrop.capture()
    }

    /** An album or playlist: lifted when it was a hold, else hung off the ⋯ that was tapped. */
    fun openBrowseMenu(target: BrowseTarget) {
        browseMenuOrigin = LongPressOrigin.consume()
        browseMenuAnchor = MenuAnchor.consume()
        browseRenameInSheet = false
        browseActions = target
    }
}

@Composable
internal fun rememberMainOverlayState(): MainOverlayState {
    val selectedTabState = rememberSaveable { mutableIntStateOf(TAB_HOME) }
    val replayLandingPageState = rememberSaveable { mutableStateOf(ReplayStoryPage.INTRO) }
    val replayStoryState = rememberSaveable { mutableStateOf<ReplayStoryPage?>(null) }
    val showReplayShareState = rememberSaveable { mutableStateOf(false) }
    val replaySharePageState = rememberSaveable { mutableStateOf<ReplayStoryPage?>(null) }
    val appLaunchCount = rememberSaveable { AppSettings.recordAppLaunch() }
    val showDiwaliState = rememberSaveable { mutableStateOf(false) }
    val diwaliAutoShownState = rememberSaveable { mutableStateOf(false) }
    val updateDialogShownState = rememberSaveable { mutableStateOf(false) }
    val displayModePromptedState = rememberSaveable { mutableStateOf(false) }
    val activeJamInviteCodeState = rememberSaveable { mutableStateOf<String?>(null) }
    val activeJamInviteServerState = rememberSaveable { mutableStateOf<String?>(null) }
    val playlistPromoShownState = rememberSaveable { mutableStateOf(false) }
    val searchFieldFocus = remember { FocusRequester() }

    val overlays = remember(appLaunchCount, searchFieldFocus) {
        MainOverlayState(
            selectedTabState = selectedTabState,
            replayLandingPageState = replayLandingPageState,
            replayStoryState = replayStoryState,
            showReplayShareState = showReplayShareState,
            replaySharePageState = replaySharePageState,
            showDiwaliState = showDiwaliState,
            diwaliAutoShownState = diwaliAutoShownState,
            updateDialogShownState = updateDialogShownState,
            displayModePromptedState = displayModePromptedState,
            playlistPromoShownState = playlistPromoShownState,
            activeJamInviteCodeState = activeJamInviteCodeState,
            activeJamInviteServerState = activeJamInviteServerState,
            appLaunchCount = appLaunchCount,
            searchFieldFocus = searchFieldFocus,
        )
    }

    SideEffect { VolumeHud.suppressed = overlays.showNowPlaying }
    return overlays
}

/** Coordinates the docked mini-player artwork transition and player sheet dismissal. */
@Stable
internal class PlayerDockState(
    val playerDock: PlayerDock,
    val playerSheetMotion: PlayerSheetMotion,
    coveredByPlayerState: State<Boolean>,
) {
    var reducePlayerMotion by mutableStateOf(false)
    val coveredByPlayer by coveredByPlayerState
    val activeDock: PlayerDock?
        get() = playerDock.takeIf { !reducePlayerMotion }

    var onDismiss: () -> Unit = {}
}

@Composable
internal fun rememberPlayerDockState(overlays: MainOverlayState): PlayerDockState {
    val playerDock = remember { PlayerDock() }
    val playerSheetScope = rememberCoroutineScope()
    val playerSheetMotion = remember(playerDock, playerSheetScope, overlays) {
        PlayerSheetMotion(
            scope = playerSheetScope,
            dock = playerDock,
            shown = { overlays.showNowPlaying },
            setShown = { overlays.showNowPlaying = it },
        )
    }
    playerSheetMotion.windowInfo = LocalWindowInfo.current
    val coveredByPlayerState = remember(playerDock, overlays) {
        derivedStateOf {
            overlays.showNowPlaying && playerDock.attached && playerDock.openFraction() >= 1f
        }
    }
    val reducePlayerMotion by AppSettings.reduceAnimation.collectAsStateWithLifecycle()
    playerSheetMotion.flingVelocity = with(LocalDensity.current) { PLAYER_PULL_FLING_VELOCITY.toPx() }

    val state = remember(playerDock, playerSheetMotion, coveredByPlayerState) {
        PlayerDockState(playerDock, playerSheetMotion, coveredByPlayerState)
    }
    state.reducePlayerMotion = reducePlayerMotion
    state.onDismiss = {
        if (reducePlayerMotion) overlays.showNowPlaying = false else playerSheetMotion.close()
    }
    return state
}

/** Holds back-stack entries, detail sort/view settings, and folder helpers. */
@Stable
internal class MainNavState(
    val navigator: AppNavigator,
    detailSearchingState: MutableState<Boolean>,
) {
    var detailStack by mutableStateOf<List<DetailPage>>(emptyList())
    var visibleEntry by mutableStateOf<RouteEntry?>(null)
    var detailActiveShelf by mutableStateOf<HomeShelf?>(null)
    var route by mutableStateOf<AppRoute?>(null)
    var topDetail by mutableStateOf<DetailPage?>(null)
    var detail by mutableStateOf<DetailPage?>(null)
    var isLocalDetail by mutableStateOf(false)
    var isReleaseDetail by mutableStateOf(false)
    var songSort by mutableStateOf(SongSort.DEFAULT)
    var downloadedMusicSort by mutableStateOf(LocalMusicSort.DATE_ADDED)
    var localMusicSort by mutableStateOf(LocalMusicSort.TITLE)
    var downloadedMusicViewType by mutableStateOf(LibraryViewType.LIST)
    var localMusicViewType by mutableStateOf(LibraryViewType.LIST)
    var detailSearching by detailSearchingState

    val folderSortOf: (String) -> LocalMusicSort = { id ->
        if (id == "local:downloads") downloadedMusicSort else localMusicSort
    }
    val folderViewTypeOf: (String) -> LibraryViewType = { id ->
        if (id == "local:downloads") downloadedMusicViewType else localMusicViewType
    }

    fun canGoBack(selectedMoodGenre: MoodGenre?, selectedTab: Int): Boolean =
        navigator.entries.isNotEmpty() || detailStack.isNotEmpty() ||
            selectedMoodGenre != null || selectedTab != TAB_HOME
}

@Composable
internal fun rememberMainNavState(viewModel: MainViewModel): MainNavState {
    val navigator = viewModel.navigator
    val detailStack by viewModel.detailStack.collectAsStateWithLifecycle()
    val topEntry = navigator.visibleEntry(detailStack.size)
    val artistShelfRoute = topEntry?.route as? AppRoute.ArtistShelf
    val visibleEntry = topEntry?.takeIf { artistShelfRoute == null }
    val detailActiveShelf = artistShelfRoute?.shelf
    val route = visibleEntry?.route
    val topDetail = detailStack.lastOrNull()
    val detail = topDetail?.takeIf { visibleEntry == null }
    val isLocalDetail = detail?.browseId.isDeviceFolder()
    val isReleaseDetail = detail != null && !isLocalDetail && detail.type != BrowseType.ARTIST
    val detailSongSorts by AppSettings.detailSongSorts.collectAsStateWithLifecycle()
    val songSort = topDetail?.browseId?.let { detailSongSorts[it] } ?: SongSort.DEFAULT
    val downloadedMusicSort by AppSettings.downloadedMusicSort.collectAsStateWithLifecycle()
    val localMusicSort by AppSettings.localMusicSort.collectAsStateWithLifecycle()
    val downloadedMusicViewType by AppSettings.downloadedMusicViewType.collectAsStateWithLifecycle()
    val localMusicViewType by AppSettings.localMusicViewType.collectAsStateWithLifecycle()
    val detailSearchingState = rememberSaveable(topDetail?.browseId) { mutableStateOf(false) }

    val state = remember(navigator, detailSearchingState) {
        MainNavState(navigator, detailSearchingState)
    }
    state.detailStack = detailStack
    state.visibleEntry = visibleEntry
    state.detailActiveShelf = detailActiveShelf
    state.route = route
    state.topDetail = topDetail
    state.detail = detail
    state.isLocalDetail = isLocalDetail
    state.isReleaseDetail = isReleaseDetail
    state.songSort = songSort
    state.downloadedMusicSort = downloadedMusicSort
    state.localMusicSort = localMusicSort
    state.downloadedMusicViewType = downloadedMusicViewType
    state.localMusicViewType = localMusicViewType
    return state
}

/** Holds list/grid scroll states, pull-to-refresh states, saved page state, and tabs. */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
internal class MainScrollState(
    val homeListState: LazyListState,
    val exploreListState: LazyListState,
    val moodGenreListState: LazyListState,
    val libraryListState: LazyListState,
    val historyListState: LazyListState,
    val libraryShowAllGridState: LazyGridState,
    val moodsGridState: LazyGridState,
    val searchListState: LazyListState,
    val homePull: PullToRefreshState,
    val explorePull: PullToRefreshState,
    val libraryPull: PullToRefreshState,
    val replayListState: LazyListState,
    val detailListStates: MutableMap<String, LazyListState>,
    val localFolderStates: MutableMap<String, LocalFolderState>,
    val pageStates: SaveableStateHolder,
    val pageKeysSeen: MutableSet<String>,
    scrolledState: State<Boolean>,
    replayScrolledState: State<Boolean>,
    detailScrolledState: State<Boolean>,
) {
    var detailListState by mutableStateOf(homeListState)
    var currentFeed by mutableStateOf<MainViewModel.Feed?>(null)
    var refreshing by mutableStateOf<Set<MainViewModel.Feed>>(emptySet())
    var listPadding by mutableStateOf(PaddingValues(0.dp))
    var tabs by mutableStateOf<List<BottomTab>>(emptyList())
    var homeLabel by mutableStateOf("")
    var playLabel by mutableStateOf("")
    var exploreLabel by mutableStateOf("")
    var libraryLabel by mutableStateOf("")
    var searchLabel by mutableStateOf("")
    var historyLabel by mutableStateOf("")
    var replayLabel by mutableStateOf("")
    var queueLabel by mutableStateOf("")
    var sharedLinkLabel by mutableStateOf("")
    var openedFileLabel by mutableStateOf("")

    val scrolled by scrolledState
    val replayScrolled by replayScrolledState
    val detailScrolled by detailScrolledState

    val currentPull: PullToRefreshState?
        get() = when (currentFeed) {
            MainViewModel.Feed.HOME -> homePull
            MainViewModel.Feed.EXPLORE -> explorePull
            MainViewModel.Feed.LIBRARY -> libraryPull
            null -> null
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberMainScrollState(
    viewModel: MainViewModel,
    nav: MainNavState,
    overlays: MainOverlayState,
    selectedMoodGenre: MoodGenre?,
    hasPlayerSong: Boolean,
    materialUiMode: Boolean,
): MainScrollState {
    val homeListState = rememberLazyListState()
    val exploreListState = rememberLazyListState()
    val moodGenreListState = rememberLazyListState()
    val libraryListState = rememberLazyListState()
    val historyListState = rememberLazyListState()
    val libraryShowAllGridState = rememberLazyGridState()
    val moodsGridState = rememberLazyGridState()
    val searchListState = rememberLazyListState()
    val currentListState = when (overlays.selectedTab) {
        TAB_HOME -> homeListState
        TAB_EXPLORE -> if (selectedMoodGenre == null) exploreListState else moodGenreListState
        TAB_LIBRARY -> libraryListState
        else -> searchListState
    }
    val homePull = rememberPullToRefreshState()
    val explorePull = rememberPullToRefreshState()
    val libraryPull = rememberPullToRefreshState()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val currentFeed = when {
        nav.route != null || nav.detail != null -> null
        overlays.selectedTab == TAB_HOME -> MainViewModel.Feed.HOME
        overlays.selectedTab == TAB_EXPLORE -> MainViewModel.Feed.EXPLORE
        overlays.selectedTab == TAB_LIBRARY -> MainViewModel.Feed.LIBRARY
        else -> null
    }
    val scrolledState = remember(currentListState) {
        derivedStateOf {
            currentListState.firstVisibleItemIndex > 0 ||
                currentListState.firstVisibleItemScrollOffset > 24
        }
    }

    val replayEntryId = nav.navigator.entries.lastOrNull { it.route == AppRoute.Replay }?.id
    val replayListState = remember(replayEntryId, overlays.replayLandingPage) { LazyListState() }
    val replayScrolledState = remember(replayListState) {
        derivedStateOf {
            replayListState.firstVisibleItemIndex > 0 ||
                replayListState.firstVisibleItemScrollOffset > 24
        }
    }

    val detailListStates = remember { mutableMapOf<String, LazyListState>() }
    val fallbackDetailListState = remember { LazyListState() }
    val detailListState = nav.topDetail?.browseId?.let { browseId ->
        detailListStates.getOrPut(browseId) { LazyListState() }
    } ?: fallbackDetailListState
    val localFolderStates = remember { mutableMapOf<String, LocalFolderState>() }
    val pageStates = rememberSaveableStateHolder()
    val pageKeysSeen = remember { mutableSetOf<String>() }

    val detailTitleDrop = with(LocalDensity.current) { DETAIL_TITLE_DROP.toPx() }
    val detailScrolledState = remember(detailListState, detailTitleDrop) {
        derivedStateOf {
            detailListState.firstVisibleItemIndex > 0 ||
                detailListState.firstVisibleItemScrollOffset > detailTitleDrop
        }
    }

    val homeLabel = stringResource(R.string.home)
    val playLabel = stringResource(R.string.play)
    val exploreLabel = stringResource(R.string.explore)
    val libraryLabel = stringResource(R.string.library)
    val searchLabel = stringResource(R.string.search)
    val historyLabel = stringResource(R.string.history)
    val replayLabel = stringResource(R.string.replay)
    val queueLabel = stringResource(R.string.queue)
    val sharedLinkLabel = stringResource(R.string.shared_link)
    val openedFileLabel = stringResource(R.string.opened_file)
    val tabs = remember(homeLabel, exploreLabel, libraryLabel, searchLabel) {
        listOf(
            BottomTab(homeLabel, BitChordIcons.Home),
            BottomTab(exploreLabel, BitChordIcons.TabExplore),
            BottomTab(libraryLabel, BitChordIcons.TabLibrary),
            BottomTab(searchLabel, BitChordIcons.TabSearch),
        )
    }

    val navBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listPadding = PaddingValues(
        top = topBarContentPadding(),
        bottom = (if (hasPlayerSong) (if (materialUiMode) 270.dp else 240.dp) else (if (materialUiMode) 190.dp else 160.dp)) + navBottomInset,
    )

    val state = remember(
        scrolledState,
        replayScrolledState,
        detailScrolledState,
        replayListState,
    ) {
        MainScrollState(
            homeListState = homeListState,
            exploreListState = exploreListState,
            moodGenreListState = moodGenreListState,
            libraryListState = libraryListState,
            historyListState = historyListState,
            libraryShowAllGridState = libraryShowAllGridState,
            moodsGridState = moodsGridState,
            searchListState = searchListState,
            homePull = homePull,
            explorePull = explorePull,
            libraryPull = libraryPull,
            replayListState = replayListState,
            detailListStates = detailListStates,
            localFolderStates = localFolderStates,
            pageStates = pageStates,
            pageKeysSeen = pageKeysSeen,
            scrolledState = scrolledState,
            replayScrolledState = replayScrolledState,
            detailScrolledState = detailScrolledState,
        )
    }
    state.detailListState = detailListState
    state.currentFeed = currentFeed
    state.refreshing = refreshing
    state.listPadding = listPadding
    state.tabs = tabs
    state.homeLabel = homeLabel
    state.playLabel = playLabel
    state.exploreLabel = exploreLabel
    state.libraryLabel = libraryLabel
    state.searchLabel = searchLabel
    state.historyLabel = historyLabel
    state.replayLabel = replayLabel
    state.queueLabel = queueLabel
    state.sharedLinkLabel = sharedLinkLabel
    state.openedFileLabel = openedFileLabel
    return state
}

/** Collects active screen feeds, library/account data, settings, and Replay state. */
@Stable
internal class MainFeedState(
    val onboardingShown: MutableTransitionState<Boolean>,
) {
    var autoplay by mutableStateOf(true)
    var partyState by mutableStateOf(ListenTogether.State())
    var autoplayEnabled by mutableStateOf(true)
    var partyServerStatus by mutableStateOf<ServerConnectionState>(ServerConnectionState.Checking)
    var listenBrainzToken by mutableStateOf("")
    var onboardingComplete by mutableStateOf(false)
    var onboardingVisible by mutableStateOf(true)
    var pendingRestartReason by mutableStateOf<String?>(null)
    var diwaliFestive by mutableStateOf(false)
    var homeState by mutableStateOf<UiState<List<HomeShelf>>>(UiState.Loading)
    var visibleHomeState by mutableStateOf<UiState<List<HomeShelf>>>(UiState.Loading)
    var hideRecents by mutableStateOf(false)
    var hideHomeVideos by mutableStateOf(false)
    var hideTopPicks by mutableStateOf(false)
    var hideHomeMoods by mutableStateOf(false)
    var homeLoadingMore by mutableStateOf(false)
    var homeRecentlyPlayedLoading by mutableStateOf(false)
    var topPicks by mutableStateOf<List<TopPick>?>(null)
    var topPicksLoading by mutableStateOf(false)
    var dontShowDisplayModePrompt by mutableStateOf(false)
    var tutorialCompleted by mutableStateOf(false)
    var updateNotice by mutableStateOf<com.music.bitchord.data.AppUpdateChecker.UpdateInfo?>(null)
    var query by mutableStateOf("")
    var results by mutableStateOf<UiState<List<SearchResult>>?>(null)
    var exploreState by mutableStateOf<UiState<List<MoodGenreSection>>>(UiState.Loading)
    var selectedMoodGenre by mutableStateOf<MoodGenre?>(null)
    var moodGenreShelves by mutableStateOf<UiState<List<HomeShelf>>>(UiState.Loading)
    var libraryState by mutableStateOf<UiState<LibraryPage>>(UiState.Loading)
    var filter by mutableStateOf(SearchFilter.ALL)
    var searchSource by mutableStateOf(SearchSource.YOUTUBE)
    var libraryResults by mutableStateOf<UiState<List<Song>>?>(null)
    var addonResults by mutableStateOf<UiState<List<Song>>?>(null)
    var addonCeiling by mutableStateOf(AudioQuality.HIGH)
    var signedIn by mutableStateOf(false)
    var account by mutableStateOf<Account?>(null)
    var selectedChannelName by mutableStateOf<String?>(null)
    var googleAccounts by mutableStateOf<List<GoogleAccountSession>>(emptyList())
    var activeAccountId by mutableStateOf<String?>(null)
    var activeProfileId by mutableStateOf<String?>(null)
    var historyState by mutableStateOf<UiState<List<Song>>>(UiState.Loading)
    var lyrics by mutableStateOf<List<LyricLine>?>(null)
    var lyricsSource by mutableStateOf<LyricsSource?>(null)
    var lyricsChecked by mutableStateOf(false)
    var lyricsProviderStates by mutableStateOf<Map<LyricsSource, LyricsProviderState>>(emptyMap())
    var searchHistory by mutableStateOf<List<SearchHistoryEntity>>(emptyList())
    var searchSuggestions by mutableStateOf<List<String>>(emptyList())
    var searchLoadingMore by mutableStateOf(false)
    var searchScrollReset by mutableIntStateOf(0)
    var releaseLibrary by mutableStateOf<Map<String, LibraryState>>(emptyMap())
    var likeStatuses by mutableStateOf<Map<String, LikeStatus>>(emptyMap())
    var offlineLiked by mutableStateOf<Set<String>>(emptySet())
    var offlinePlaylists by mutableStateOf<List<OfflinePlaylist>>(emptyList())
    var offlinePlaylistGroups by mutableStateOf<List<com.music.bitchord.data.LocalGroup>>(emptyList())
    var pinnedToOriginal by mutableStateOf<Set<String>>(emptySet())
    var qualityUpgradesInFlight by mutableStateOf<Set<String>>(emptySet())
    var playlists by mutableStateOf<List<UserPlaylist>>(emptyList())
    var playlistsLoading by mutableStateOf(false)
    var savedDownloads by mutableStateOf<Map<String, String>>(emptyMap())
    var localMusicFolderUri by mutableStateOf<String?>(null)
    var filterNonMusicAudio by mutableStateOf(false)
    var webdavUrl by mutableStateOf("")
    var smbHost by mutableStateOf("")
    var librarySort by mutableStateOf(LibrarySort.DEFAULT)
    var savedCollections by mutableStateOf<Map<String, SavedCollection>>(emptyMap())
    var downloadedReleases by mutableStateOf<List<SavedCollection>>(emptyList())
    var localPlaylists by mutableStateOf<List<LocalPlaylist>>(emptyList())
    var localPlaylistItems by mutableStateOf<List<ShelfItem>>(emptyList())
    var shuffleEnabled by mutableStateOf(false)
    lateinit var detailPalette: ArtworkPalette
    lateinit var replay: ReplayState
    var setReplayPeriod: (ReplayPeriod) -> Unit = {}
    var replayCards by mutableStateOf<List<ReplayHeroCard>>(emptyList())

    val downloadIdFor: (String?) -> String? = { id ->
        id?.let { Downloads.recordIdOf(it) ?: it }?.takeIf { it in savedCollections }
    }
}

@Composable
internal fun rememberMainFeedState(
    viewModel: MainViewModel,
    overlays: MainOverlayState,
    nav: MainNavState,
    appForeground: Boolean,
): MainFeedState {
    val context = LocalContext.current
    val onboardingComplete by AppSettings.onboardingComplete.collectAsStateWithLifecycle()
    val onboardingShown = remember { MutableTransitionState(!onboardingComplete) }
    onboardingShown.targetState = !onboardingComplete

    val state = remember(onboardingShown) { MainFeedState(onboardingShown) }
    val autoplay by AppSettings.autoplay.collectAsStateWithLifecycle()
    val partyState by ListenTogether.state.collectAsStateWithLifecycle()
    val homeState by viewModel.home.collectAsStateWithLifecycle()
    val hideRecents by AppSettings.hideRecents.collectAsStateWithLifecycle()
    val hideHomeVideos by AppSettings.hideHomeVideos.collectAsStateWithLifecycle()
    val hideTopPicks by AppSettings.hideTopPicks.collectAsStateWithLifecycle()
    val hideHomeMoods by AppSettings.hideHomeMoods.collectAsStateWithLifecycle()

    state.autoplay = autoplay
    state.partyState = partyState
    state.autoplayEnabled = autoplayEnabledFor(partyState, autoplay)
    state.partyServerStatus = ListenTogether.serverConnectionState.collectAsStateWithLifecycle().value
    state.listenBrainzToken = AppSettings.listenBrainzToken.collectAsStateWithLifecycle().value
    state.onboardingComplete = onboardingComplete
    state.onboardingVisible = onboardingShown.currentState || onboardingShown.targetState
    state.pendingRestartReason = com.music.bitchord.ui.components.AppRestartController.pendingReason.collectAsStateWithLifecycle().value
    state.diwaliFestive = remember(appForeground) {
        DiwaliGate.isIndianTimezone() && DiwaliGate.isDiwaliDay()
    }
    state.homeState = homeState
    state.hideRecents = hideRecents
    state.hideHomeVideos = hideHomeVideos
    state.hideTopPicks = hideTopPicks
    state.hideHomeMoods = hideHomeMoods
    state.visibleHomeState = remember(homeState, hideHomeVideos, hideRecents) {
        val loaded = homeState as? UiState.Success
        if (loaded != null && (hideHomeVideos || hideRecents)) {
            var shelves = loaded.data
            if (hideRecents) shelves = shelves.filterNot { it.title == YtMusicRepository.RECENT_TITLE }
            if (hideHomeVideos) shelves = shelves.withoutVideos()
            UiState.Success(shelves)
        } else {
            homeState
        }
    }
    state.homeLoadingMore = viewModel.homeLoadingMore.collectAsStateWithLifecycle().value
    state.homeRecentlyPlayedLoading = viewModel.homeRecentlyPlayedLoading.collectAsStateWithLifecycle().value
    state.topPicks = viewModel.topPicks.collectAsStateWithLifecycle().value
    state.topPicksLoading = viewModel.topPicksLoading.collectAsStateWithLifecycle().value
    state.dontShowDisplayModePrompt = AppSettings.dontShowDisplayModePrompt.collectAsStateWithLifecycle().value
    state.tutorialCompleted = AppSettings.tutorialCompleted.collectAsStateWithLifecycle().value
    state.updateNotice = viewModel.updateAvailable.collectAsStateWithLifecycle().value
    state.query = viewModel.query.collectAsStateWithLifecycle().value
    state.results = viewModel.results.collectAsStateWithLifecycle().value
    state.exploreState = viewModel.explore.collectAsStateWithLifecycle().value
    state.selectedMoodGenre = viewModel.selectedMoodGenre.collectAsStateWithLifecycle().value
    state.moodGenreShelves = viewModel.moodGenreShelves.collectAsStateWithLifecycle().value
    state.libraryState = viewModel.library.collectAsStateWithLifecycle().value
    state.filter = viewModel.filter.collectAsStateWithLifecycle().value
    state.searchSource = viewModel.searchSource.collectAsStateWithLifecycle().value
    state.libraryResults = viewModel.libraryResults.collectAsStateWithLifecycle().value
    state.addonResults = viewModel.addonResults.collectAsStateWithLifecycle().value
    state.addonCeiling = AppSettings.ceilingFor(
        wifi = AppSettings.audioQualityWifi.collectAsStateWithLifecycle().value,
        cellular = AppSettings.audioQualityCellular.collectAsStateWithLifecycle().value,
        metered = AppSettings.meteredConnection.collectAsStateWithLifecycle().value,
        override = AppSettings.streamCeilingOverride.collectAsStateWithLifecycle().value,
    )
    state.signedIn = viewModel.signedIn.collectAsStateWithLifecycle().value
    state.account = viewModel.account.collectAsStateWithLifecycle().value
    state.selectedChannelName = viewModel.selectedChannelName.collectAsStateWithLifecycle().value
    state.googleAccounts = viewModel.googleAccounts.collectAsStateWithLifecycle().value
    state.activeAccountId = viewModel.activeAccountId.collectAsStateWithLifecycle().value
    state.activeProfileId = viewModel.activeProfileId.collectAsStateWithLifecycle().value
    state.historyState = viewModel.history.collectAsStateWithLifecycle().value
    state.lyrics = viewModel.lyrics.collectAsStateWithLifecycle().value
    state.lyricsSource = viewModel.lyricsSource.collectAsStateWithLifecycle().value
    state.lyricsChecked = viewModel.lyricsChecked.collectAsStateWithLifecycle().value
    state.lyricsProviderStates = viewModel.lyricsProviderStates.collectAsStateWithLifecycle().value
    state.searchHistory = viewModel.searchHistory.collectAsStateWithLifecycle().value
    state.searchSuggestions = viewModel.suggestions.collectAsStateWithLifecycle().value
    state.searchLoadingMore = viewModel.searchLoadingMore.collectAsStateWithLifecycle().value
    state.searchScrollReset = viewModel.searchScrollReset.collectAsStateWithLifecycle().value
    state.releaseLibrary = viewModel.releaseLibrary.collectAsStateWithLifecycle().value
    state.likeStatuses = viewModel.likeStatuses.collectAsStateWithLifecycle().value
    state.offlineLiked = OfflinePlaylists.liked.collectAsStateWithLifecycle().value
    val offlinePlaylists by OfflinePlaylists.playlists.collectAsStateWithLifecycle()
    state.offlinePlaylists = offlinePlaylists
    state.offlinePlaylistGroups = remember(offlinePlaylists) { offlinePlaylists.map { it.toGroup() } }
    state.pinnedToOriginal = com.music.bitchord.playback.OriginalVersion.pinned.collectAsStateWithLifecycle().value
    state.qualityUpgradesInFlight = com.music.bitchord.data.NerdStats.racingLossless.collectAsStateWithLifecycle().value
    state.playlists = viewModel.playlists.collectAsStateWithLifecycle().value
    state.playlistsLoading = viewModel.playlistsLoading.collectAsStateWithLifecycle().value
    val savedDownloads by Downloads.saved.collectAsStateWithLifecycle()
    val savedCollections by Downloads.collections.collectAsStateWithLifecycle()
    state.savedDownloads = savedDownloads
    state.savedCollections = savedCollections
    state.downloadedReleases = remember(savedCollections, savedDownloads) { Downloads.savedReleases() }
    state.localMusicFolderUri = AppSettings.localMusicFolderUri.collectAsStateWithLifecycle().value
    state.filterNonMusicAudio = AppSettings.filterNonMusicAudio.collectAsStateWithLifecycle().value
    state.webdavUrl = AppSettings.webdavUrl.collectAsStateWithLifecycle().value
    state.smbHost = AppSettings.smbHost.collectAsStateWithLifecycle().value
    state.librarySort = AppSettings.librarySort.collectAsStateWithLifecycle().value
    val localPlaylists by LocalPlaylistStore.playlists.collectAsStateWithLifecycle()
    state.localPlaylists = localPlaylists
    state.localPlaylistItems = localPlaylists.map { playlist ->
        ShelfItem(
            title = playlist.title,
            subtitle = stringResource(R.string.local_playlist_subtitle, playlist.songs.size),
            thumbnailUrl = playlist.songs.firstOrNull { !it.thumbnailUrl.isNullOrBlank() }?.thumbnailUrl,
            videoId = null,
            browseId = playlist.browseId,
        )
    }
    state.shuffleEnabled = QueueShuffle.enabled.collectAsStateWithLifecycle().value

    val appleArtVersion by AppleArtistArtRepository.updates.collectAsStateWithLifecycle()
    val detailApple = remember(nav.topDetail?.browseId, nav.topDetail?.type, appleArtVersion) {
        nav.topDetail?.takeIf { it.type == BrowseType.ARTIST }
            ?.let { AppleArtistArtRepository.cached(it.browseId) }
    }
    state.detailPalette = rememberArtworkPalette(
        imageUrl = detailApple?.heroUrl ?: nav.topDetail?.thumbnailUrl,
        keyColors = detailApple?.keyColors(),
    )

    val replayOpen = nav.route == AppRoute.Replay ||
        overlays.replayStory != null ||
        overlays.showReplayShare ||
        (overlays.selectedTab == TAB_LIBRARY && nav.detail == null && nav.route == null)
    val (replay, setReplayPeriod) = rememberReplayState(replayOpen)
    state.replay = replay
    state.setReplayPeriod = setReplayPeriod
    state.replayCards = remember(replay.summary) {
        replay.summary?.takeUnless { it.isEmpty }?.cards(context).orEmpty()
    }
    return state
}

@Composable
internal fun MainNavigationAndPromptEffects(
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    playerDock: PlayerDockState,
    appForeground: Boolean,
    viewModel: MainViewModel,
) {
    LaunchedEffect(overlays.selectedTab) {
        if (overlays.selectedTab == TAB_EXPLORE) viewModel.onExploreShown()
    }
    LaunchedEffect(appForeground) {
        if (appForeground) viewModel.refreshTopPicksIfStale()
    }
    val openPlayerRequested by PlayerDeepLink.pending.collectAsStateWithLifecycle()
    LaunchedEffect(openPlayerRequested) {
        if (openPlayerRequested) {
            overlays.showNowPlaying = true
            PlayerDeepLink.handled()
        }
    }
    LaunchedEffect(feeds.diwaliFestive, feeds.onboardingComplete) {
        if (feeds.diwaliFestive && feeds.onboardingComplete && !overlays.diwaliAutoShown) {
            overlays.diwaliAutoShown = true
            if (!AppSettings.diwaliGreetingShown.value) overlays.showDiwali = true
        }
    }
    LaunchedEffect(nav.detail?.browseId) {
        if (overlays.reorderTarget?.browseId != nav.detail?.browseId) overlays.reorderTarget = null
    }
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscapeOrientation =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    LaunchedEffect(isLandscapeOrientation, feeds.onboardingComplete, feeds.dontShowDisplayModePrompt) {
        if (!isLandscapeOrientation) {
            overlays.showDisplayModePrompt = false
            overlays.showStandByDisplayMode = false
        } else if (feeds.onboardingComplete &&
            !feeds.dontShowDisplayModePrompt &&
            !overlays.displayModePromptedThisLaunch &&
            !overlays.showStandByDisplayMode
        ) {
            overlays.displayModePromptedThisLaunch = true
            overlays.showDisplayModePrompt = true
        }
    }
    LaunchedEffect(feeds.tutorialCompleted, feeds.onboardingComplete) {
        if (feeds.onboardingComplete && !feeds.tutorialCompleted) {
            while (nav.navigator.pop()) {}
            overlays.selectedTab = TAB_HOME
        }
    }
    LaunchedEffect(feeds.updateNotice) {
        if (feeds.updateNotice != null && !overlays.updateDialogShown) {
            overlays.updateDialogShown = true
            overlays.showUpdateDialog = true
        }
    }
    val incomingJamInvite by JamInviteLink.pending.collectAsStateWithLifecycle()
    LaunchedEffect(incomingJamInvite) {
        val invite = incomingJamInvite ?: return@LaunchedEffect
        overlays.activeJamInviteCode = invite.code
        overlays.activeJamInviteServer = invite.serverUrl
        playerDock.onDismiss()
        overlays.replayStory = null
        overlays.showReplayShare = false
        overlays.webSession = null
        nav.navigator.reset(AppRoute.Settings, AppRoute.ListenTogether)
        JamInviteLink.handled()
    }
    LaunchedEffect(feeds.signedIn, overlays.activeJamInviteCode) {
        if (feeds.signedIn && overlays.activeJamInviteCode != null) {
            nav.navigator.pushAll(AppRoute.Settings, AppRoute.ListenTogether)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainStorageAndFeedEffects(
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    scroll: MainScrollState,
    viewModel: MainViewModel,
) {
    LaunchedEffect(feeds.savedDownloads, feeds.savedCollections, nav.topDetail?.browseId) {
        val openPage = nav.topDetail ?: return@LaunchedEffect
        val open = openPage.browseId
        val folder = LocalGroups.folderOf(open) ?: open
        if (openPage.songs !is UiState.Loading &&
            (folder == "local:downloads" || folder == CACHE_FOLDER_BROWSE_ID ||
                Downloads.recordIdOf(open) != null ||
                LocalPlaylistStore.getPlaylist(open) != null)
        ) {
            viewModel.reloadLocalDetail(open)
        }
    }
    LaunchedEffect(nav.topDetail?.browseId, feeds.offlinePlaylists) {
        val openPage = nav.topDetail ?: return@LaunchedEffect
        if (openPage.songs !is UiState.Loading && OfflinePlaylists.isOfflinePlaylist(openPage.browseId)) {
            viewModel.reloadLocalDetail(openPage.browseId)
        }
    }
    LaunchedEffect(feeds.localMusicFolderUri, feeds.filterNonMusicAudio) {
        val open = nav.topDetail?.browseId
        if (open == "local:all" || LocalGroups.folderOf(open) == "local:all") {
            viewModel.reloadLocalDetail(open!!)
        }
    }
    LaunchedEffect(feeds.webdavUrl) {
        if (nav.topDetail?.browseId == com.music.bitchord.data.webdav.WebDavConfig.BROWSE_ID) {
            viewModel.reloadLocalDetail(com.music.bitchord.data.webdav.WebDavConfig.BROWSE_ID)
        }
    }
    LaunchedEffect(feeds.smbHost) {
        if (nav.topDetail?.browseId == com.music.bitchord.data.smb.SmbConfig.BROWSE_ID) {
            viewModel.reloadLocalDetail(com.music.bitchord.data.smb.SmbConfig.BROWSE_ID)
        }
    }
    LaunchedEffect(overlays.queueNotice?.id) {
        val shown = overlays.queueNotice ?: return@LaunchedEffect
        delay(3_000)
        if (overlays.queueNotice?.id == shown.id) overlays.queueNotice = null
    }
    LaunchedEffect(scroll.currentFeed) {
        if (scroll.currentFeed == MainViewModel.Feed.HOME) viewModel.onHomeShown()
        if (scroll.currentFeed == MainViewModel.Feed.LIBRARY) viewModel.onLibraryShown()
    }
    LaunchedEffect(nav.detailStack.map { it.browseId }) {
        val open = nav.detailStack.mapTo(HashSet()) { it.browseId }
        scroll.detailListStates.keys.retainAll(open)
        scroll.localFolderStates.keys.retainAll(open)
    }
    val livePageKeys = nav.navigator.entries.map { AppDestination.Route(it).key } +
        nav.detailStack.map { AppDestination.Release(it.browseId).key }
    LaunchedEffect(livePageKeys) {
        val gone = scroll.pageKeysSeen.filter { it !in livePageKeys }
        gone.forEach(scroll.pageStates::removeState)
        scroll.pageKeysSeen.removeAll(gone.toSet())
    }
}
