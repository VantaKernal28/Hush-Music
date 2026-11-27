package com.music.bitchord

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.auth.DiscordLoginScreen
import com.music.bitchord.auth.SpotifyLoginScreen
import com.music.bitchord.auth.WebSessionMode
import com.music.bitchord.auth.YtMusicLoginScreen
import com.music.bitchord.data.AppUpdateChecker
import com.music.bitchord.data.TopPicks
import com.music.bitchord.data.listentogether.ListenTogether
import com.music.bitchord.data.model.PlaybackSourceType
import com.music.bitchord.data.model.PlaylistPrivacy
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.TopPickKind
import com.music.bitchord.data.model.UiState
import com.music.bitchord.data.model.UserPlaylist
import com.music.bitchord.data.scrobbling.LastFM
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.LibraryViewType
import com.music.bitchord.data.settings.SongSort
import com.music.bitchord.data.sources.SourceKind
import com.music.bitchord.data.sources.SourceRegistry
import com.music.bitchord.playback.QueueSource
import com.music.bitchord.ui.AddonBlockedAlert
import com.music.bitchord.ui.MainViewModel
import com.music.bitchord.ui.components.AccountProfileSelector
import com.music.bitchord.ui.components.AppLanguageDialog
import com.music.bitchord.ui.components.AppRestartController
import com.music.bitchord.ui.components.AppRestartPromptCard
import com.music.bitchord.ui.components.ChoiceAlert
import com.music.bitchord.ui.components.ConfirmationAlert
import com.music.bitchord.ui.components.DacVolumeHud
import com.music.bitchord.ui.components.FieldConfig
import com.music.bitchord.ui.components.HushLaunchReveal
import com.music.bitchord.ui.components.HushSpotlightTutorial
import com.music.bitchord.ui.components.InfoAlert
import com.music.bitchord.ui.components.LastfmLoginAlert
import com.music.bitchord.ui.components.ListenBrainzTokenAlert
import com.music.bitchord.ui.components.LyricsSourcesDialog
import com.music.bitchord.ui.components.PlaylistPickerSheet
import com.music.bitchord.ui.components.ServerEditorHost
import com.music.bitchord.ui.components.SpotifyImportAlert
import com.music.bitchord.ui.components.TranslationLanguageDialog
import com.music.bitchord.ui.components.UpdateAvailableDialog
import com.music.bitchord.ui.components.WebDavConflictAlert
import com.music.bitchord.ui.components.YourMixCard
import com.music.bitchord.ui.festive.DiwaliGreetingScreen
import com.music.bitchord.ui.navigation.AppRoute
import com.music.bitchord.ui.onboarding.OnboardingScreen
import com.music.bitchord.ui.player.AndroidLyricsShareSheet
import com.music.bitchord.ui.player.StandByDisplayMode
import com.music.bitchord.ui.player.StandByDisplayModePromptCard
import com.music.bitchord.ui.playlist.AiPlaylistMakerSheet
import com.music.bitchord.ui.playlist.PlaylistMakerPromoCard
import com.music.bitchord.ui.replay.ReplayShareSheet
import com.music.bitchord.ui.replay.ReplayStories
import com.music.bitchord.ui.screens.DiscordDialogHost
import com.music.bitchord.ui.screens.PartyServerEditor
import com.music.bitchord.ui.screens.SignatureApiEditor
import com.music.bitchord.ui.screens.SongMixerSheet
import com.music.bitchord.ui.screens.SourceEditorAlert
import com.music.bitchord.ui.screens.SpotifyCanvasAuthScreen
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReplayOverlaysHost(
    overlays: MainOverlayState,
    feeds: MainFeedState,
) {
    overlays.replayStory?.let { start ->
        feeds.replay.summary?.takeUnless { it.isEmpty }?.let { summary ->
            ReplayStories(
                summary = summary,
                start = start,
                onClose = { overlays.replayStory = null },
                onShare = { card ->
                    overlays.replaySharePage = card
                    overlays.showReplayShare = true
                },
                paused = overlays.showReplayShare,
            )
        }
    }
    if (overlays.showReplayShare) {
        feeds.replay.summary?.takeUnless { it.isEmpty }?.let { summary ->
            ModalBottomSheet(
                onDismissRequest = { overlays.showReplayShare = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                ReplayShareSheet(
                    summary = summary,
                    holder = feeds.account?.name.orEmpty(),
                    memberSince = feeds.replay.memberSince,
                    page = overlays.replaySharePage,
                    onDismiss = { overlays.showReplayShare = false },
                )
            }
        }
    }
}

@Composable
internal fun PlaylistAndMixModalsHost(
    overlays: MainOverlayState,
    chrome: MainChromeState,
    nav: MainNavState,
    feeds: MainFeedState,
    playback: MainPlaybackActions,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val player = playback.player
    val lastPlaylistPromptLaunch by AppSettings.lastPlaylistPromptLaunch.collectAsStateWithLifecycle()
    val showPlaylistMakerPromo = !overlays.playlistPromoShownThisLaunch &&
        feeds.onboardingComplete &&
        feeds.tutorialCompleted &&
        overlays.appLaunchCount >= 15 &&
        overlays.appLaunchCount % 15 == 0 &&
        lastPlaylistPromptLaunch < overlays.appLaunchCount &&
        !overlays.showDiwali &&
        !overlays.showUpdateDialog &&
        !overlays.showYourMixCard &&
        !overlays.showAiPlaylistMaker &&
        feeds.pendingRestartReason == null
    if (showPlaylistMakerPromo) {
        PlaylistMakerPromoCard(
            hazeState = chrome.hazeState,
            onTryNow = {
                overlays.playlistPromoShownThisLaunch = true
                AppSettings.markPlaylistPromptShown(overlays.appLaunchCount)
                overlays.showAiPlaylistMaker = true
            },
            onDismiss = {
                overlays.playlistPromoShownThisLaunch = true
                AppSettings.markPlaylistPromptShown(overlays.appLaunchCount)
            },
        )
    }

    if (overlays.showAiPlaylistMaker) {
        val historyState by viewModel.history.collectAsStateWithLifecycle()
        val availableSongs = remember(historyState, nav.topDetail, player.queue, player.song) {
            val fromHistory: List<Song> = (historyState as? UiState.Success<List<Song>>)?.data.orEmpty()
            val fromDetail: List<Song> = (nav.topDetail?.songs as? UiState.Success<List<Song>>)?.data.orEmpty()
            val fromQueue: List<Song> = player.queue
            val current: List<Song> = listOfNotNull(player.song)
            (current + fromHistory + fromDetail + fromQueue).distinctBy { it.videoId }
        }
        AiPlaylistMakerSheet(
            availableSongs = availableSongs,
            onDismiss = { overlays.showAiPlaylistMaker = false },
            onCreatePlaylist = { title, songIds, songs, playImmediately ->
                if (playImmediately && songs.isNotEmpty()) playback.play(songs, 0)
                viewModel.createPlaylistWithVideoIds(
                    title = title,
                    privacy = PlaylistPrivacy.PRIVATE,
                    videoIds = songIds,
                    songs = songs,
                ) { _, createdTitle, _ ->
                    overlays.showQueueNotice("Saved playlist: $createdTitle")
                }
            },
            hazeState = chrome.hazeState,
        )
    }

    if (overlays.showSongMixer) {
        val mixerHistoryState by viewModel.history.collectAsStateWithLifecycle()
        val mixerCandidates: List<Song> = remember(mixerHistoryState, nav.topDetail, player.queue, player.song) {
            val fromHistory: List<Song> = (mixerHistoryState as? UiState.Success<List<Song>>)?.data.orEmpty()
            val fromDetail: List<Song> = (nav.topDetail?.songs as? UiState.Success<List<Song>>)?.data.orEmpty()
            val fromQueue: List<Song> = player.queue
            (fromQueue + fromHistory + fromDetail)
                .distinctBy { it.videoId }
                .filter { it.videoId != player.song?.videoId }
        }
        SongMixerSheet(
            song = player.song,
            candidateSongs = mixerCandidates,
            hazeState = chrome.hazeState,
            onDismiss = { overlays.showSongMixer = false },
        )
    }

    if (overlays.playlistTarget != null || overlays.creatingPlaylist) {
        val target = overlays.playlistTarget
        PlaylistPickerSheet(
            playlists = feeds.playlists,
            loading = feeds.playlistsLoading,
            song = target,
            startCreating = target == null,
            onDismiss = {
                overlays.playlistTarget = null
                overlays.creatingPlaylist = false
            },
            onAdd = { picked ->
                target?.let { song ->
                    viewModel.addToPlaylists(picked, song) { added, alreadyThere, failed ->
                        overlays.showQueueNotice(
                            when {
                                added > 1 -> context.resources.getQuantityString(
                                    R.plurals.added_to_playlists_notice, added, added,
                                )
                                added == 1 -> context.getString(R.string.song_added_to_playlist)
                                alreadyThere > 0 && failed == 0 ->
                                    context.getString(R.string.song_already_in_playlist)
                                else -> context.getString(R.string.failed)
                            },
                        )
                    }
                }
            },
            onCreate = { title, privacy -> viewModel.createPlaylist(title, privacy, target) },
        )
    }

    if (overlays.offlinePlaylistTarget != null || overlays.creatingOfflinePlaylist) {
        val target = overlays.offlinePlaylistTarget
        val choices = remember(feeds.offlinePlaylists) {
            feeds.offlinePlaylists.map { playlist ->
                UserPlaylist(
                    playlistId = playlist.id,
                    title = playlist.title,
                    subtitle = context.resources.getQuantityString(
                        R.plurals.song_count_plural,
                        playlist.songs.size,
                        playlist.songs.size,
                    ),
                    thumbnailUrl = playlist.songs.firstNotNullOfOrNull { it.thumbnailUrl }
                        .takeUnless { playlist.isLiked },
                )
            }
        }
        PlaylistPickerSheet(
            playlists = choices,
            loading = false,
            song = target,
            startCreating = target == null,
            offline = true,
            onDismiss = {
                overlays.offlinePlaylistTarget = null
                overlays.creatingOfflinePlaylist = false
            },
            onAdd = { picked ->
                target?.let { song ->
                    val result = com.music.bitchord.data.OfflinePlaylists.add(
                        picked.mapTo(HashSet()) { it.playlistId },
                        listOf(song),
                    )
                    overlays.showQueueNotice(
                        when {
                            result.added > 1 -> context.resources.getQuantityString(
                                R.plurals.added_to_playlists_notice, result.added, result.added,
                            )
                            result.added == 1 -> context.getString(R.string.song_added_to_playlist)
                            result.alreadyThere > 0 -> context.getString(R.string.song_already_in_playlist)
                            else -> context.getString(R.string.failed)
                        },
                    )
                }
            },
            onCreate = { title, _ ->
                com.music.bitchord.data.OfflinePlaylists.create(title, listOfNotNull(target))
                if (target != null) overlays.showQueueNotice(context.getString(R.string.song_added_to_playlist))
            },
        )
    }

    if (overlays.showSpotifyImportDialog) {
        BackHandler { overlays.showSpotifyImportDialog = false }
        SpotifyImportAlert(
            hazeState = chrome.hazeState,
            signedIn = feeds.signedIn,
            onImported = { title, privacy, songs ->
                viewModel.createPlaylistWithVideoIds(
                    title,
                    privacy,
                    songs.map { it.videoId },
                    songs,
                ) { browseId, pTitle, savedLocally ->
                    overlays.showQueueNotice(
                        context.getString(
                            if (savedLocally && feeds.signedIn) R.string.spotify_import_local_fallback
                            else R.string.spotify_import_done,
                            pTitle,
                        ),
                    )
                    browseId?.let { id ->
                        viewModel.openDetail(id, pTitle, "${songs.size} songs", songs.firstOrNull()?.thumbnailUrl)
                    }
                }
            },
            onDismiss = { overlays.showSpotifyImportDialog = false },
        )
    }
}

@Composable
internal fun OnboardingAndSignInHost(
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    viewModel: MainViewModel,
) {
    AnimatedVisibility(
        visibleState = feeds.onboardingShown,
        enter = EnterTransition.None,
        exit = slideOutVertically(
            androidx.compose.animation.core.tween<IntOffset>(
                durationMillis = 560,
                easing = OnboardingDismissEasing,
            ),
        ) { it } + fadeOut(tween(350)),
    ) {
        OnboardingScreen(
            signedIn = feeds.signedIn,
            initialName = remember {
                AppSettings.userName.value.ifBlank {
                    ListenTogether.nickname().ifBlank { feeds.account?.name.orEmpty() }
                }
            },
            onNameChosen = { name ->
                ListenTogether.setNickname(name)
                AppSettings.setUserName(name)
            },
            onSignIn = { overlays.webSession = WebSessionMode.SIGN_IN },
            onFinish = AppSettings::setOnboardingComplete,
        )
    }

    overlays.webSession?.let { mode ->
        WebSessionLoginOverlay(
            mode = mode,
            overlays = overlays,
            nav = nav,
            feeds = feeds,
            viewModel = viewModel,
        )
    }

    AnimatedVisibility(
        visible = overlays.showDiwali,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(300)),
    ) {
        val dismissDiwali = {
            overlays.showDiwali = false
            AppSettings.setDiwaliGreetingShown()
        }
        BackHandler(onBack = dismissDiwali)
        DiwaliGreetingScreen(onDismiss = dismissDiwali)
    }
}

@Composable
private fun WebSessionLoginOverlay(
    mode: WebSessionMode,
    overlays: MainOverlayState,
    nav: MainNavState,
    feeds: MainFeedState,
    viewModel: MainViewModel,
) {
    BackHandler { overlays.webSession = null }
    var captureRequest by remember(mode) { mutableIntStateOf(0) }
    var captureFailed by remember(mode) { mutableStateOf(false) }
    var pageReady by remember(mode) { mutableStateOf(false) }
    var confirmingProfile by remember(mode) { mutableStateOf(false) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { overlays.webSession = null }) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = when (mode) {
                            WebSessionMode.SIGN_IN -> stringResource(R.string.sign_in_youtube_music)
                            WebSessionMode.SWITCH_CHANNEL -> stringResource(R.string.choose_profile)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (pageReady) {
                        Text(
                            text = if (captureFailed) {
                                stringResource(R.string.profile_unavailable)
                            } else {
                                stringResource(R.string.switch_profile_hint)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                        )
                    }
                }
                if (pageReady) {
                    TextButton(
                        onClick = {
                            captureFailed = false
                            confirmingProfile = true
                            captureRequest++
                        },
                        enabled = !confirmingProfile,
                    ) {
                        Text(
                            if (confirmingProfile) stringResource(R.string.checking)
                            else stringResource(R.string.use_this_profile),
                        )
                    }
                }
            }
            YtMusicLoginScreen(
                mode = mode,
                initialCookie = if (mode == WebSessionMode.SWITCH_CHANNEL) {
                    feeds.googleAccounts.firstOrNull { it.accountId == feeds.activeAccountId }?.cookie
                } else {
                    null
                },
                captureRequest = captureRequest,
                onPageReady = { pageReady = it },
                onCaptureUnavailable = {
                    confirmingProfile = false
                    captureFailed = true
                },
                onCaptured = { session ->
                    viewModel.onWebSession(session, mode) { accepted ->
                        confirmingProfile = false
                        if (accepted) {
                            overlays.webSession = null
                            if (mode == WebSessionMode.SIGN_IN) {
                                nav.navigator.clear()
                                overlays.selectedTab = TAB_LIBRARY
                            }
                        } else {
                            captureFailed = true
                        }
                    }
                },
            )
        }
    }
}

@Composable
internal fun SettingsAndAccountDialogsHost(
    overlays: MainOverlayState,
    chrome: MainChromeState,
    nav: MainNavState,
    feeds: MainFeedState,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (overlays.showUpdateDialog && feeds.onboardingComplete && !overlays.showDiwali) {
        feeds.updateNotice?.let { update ->
            UpdateAvailableDialog(
                version = update.version,
                notes = update.notes,
                hazeState = chrome.hazeState,
                onDismiss = { overlays.showUpdateDialog = false },
                onDownload = {
                    if (update.apkUrl != null) {
                        scope.launch { AppUpdateChecker.downloadApk(context) }
                    } else {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(update.releaseUrl)))
                        overlays.showUpdateDialog = false
                    }
                },
                onCancelDownload = { AppUpdateChecker.cancelDownload() },
                onInstall = {
                    val ready = AppUpdateChecker.download.value as? AppUpdateChecker.DownloadState.Ready
                    ready?.let { AppUpdateChecker.installApk(context, it.file) }
                },
                onOpenReleasePage = {
                    AppUpdateChecker.resetDownload()
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(update.releaseUrl)))
                    overlays.showUpdateDialog = false
                },
            )
        }
    }

    if (overlays.showLyricsSources) {
        BackHandler { overlays.showLyricsSources = false }
        LyricsSourcesDialog(hazeState = chrome.hazeState, onDismiss = { overlays.showLyricsSources = false })
    }

    if (overlays.songSortMenuOpen) {
        BackHandler { overlays.songSortMenuOpen = false }
        FrostedSortMenu(
            hazeState = chrome.hazeState,
            selected = nav.songSort,
            onSelect = { option ->
                nav.detail?.browseId?.let { AppSettings.setDetailSongSort(it, option) }
                overlays.songSortMenuOpen = false
            },
            onFlipDateDirection = {
                nav.detail?.browseId?.let { browseId ->
                    val next = when (nav.songSort) {
                        SongSort.DATE_ADDED_DESC -> SongSort.DATE_ADDED_ASC
                        SongSort.DATE_ADDED_ASC -> SongSort.DATE_ADDED_DESC
                        else -> SongSort.DATE_ADDED_DESC
                    }
                    AppSettings.setDetailSongSort(browseId, next)
                }
            },
            onDismiss = { overlays.songSortMenuOpen = false },
        )
    }

    val menuFolder = nav.detail?.browseId?.takeIf { it.isDeviceFolder() }
    if (overlays.folderMenuOpen && menuFolder != null) {
        BackHandler { overlays.folderMenuOpen = false }
        val downloadsFolder = menuFolder == "local:downloads"
        val viewType = nav.folderViewTypeOf(menuFolder)
        FrostedFolderMenu(
            hazeState = chrome.hazeState,
            sort = nav.folderSortOf(menuFolder),
            viewType = viewType,
            onSort = { option ->
                if (downloadsFolder) AppSettings.setDownloadedMusicSort(option)
                else AppSettings.setLocalMusicSort(option)
                overlays.folderMenuOpen = false
            },
            onToggleViewType = {
                val next = if (viewType == LibraryViewType.GRID) LibraryViewType.LIST else LibraryViewType.GRID
                if (downloadsFolder) AppSettings.setDownloadedMusicViewType(next)
                else AppSettings.setLocalMusicViewType(next)
                overlays.folderMenuOpen = false
            },
            onDismiss = { overlays.folderMenuOpen = false },
        )
    }

    if (overlays.showAccountSelector) BackHandler { overlays.showAccountSelector = false }
    AccountProfileSelector(
        visible = overlays.showAccountSelector,
        accounts = feeds.googleAccounts,
        activeAccountId = feeds.activeAccountId,
        activeProfileId = feeds.activeProfileId,
        hazeState = chrome.hazeState,
        onSelect = { selected, profile -> viewModel.selectProfile(selected.accountId, profile.profileId) },
        onAddAccount = {
            overlays.showAccountSelector = false
            overlays.webSession = WebSessionMode.SIGN_IN
        },
        onRemoveAccount = { selected ->
            viewModel.removeAccount(selected.accountId)
            overlays.showAccountSelector = false
        },
        onOpenSettings = {
            overlays.showAccountSelector = false
            nav.navigator.push(AppRoute.Settings)
        },
        onDismiss = { overlays.showAccountSelector = false },
    )

    if (overlays.showAppLanguage) {
        BackHandler { overlays.showAppLanguage = false }
        AppLanguageDialog(hazeState = chrome.hazeState, onDismiss = { overlays.showAppLanguage = false })
    }
    if (overlays.showTranslationLanguage) {
        BackHandler { overlays.showTranslationLanguage = false }
        TranslationLanguageDialog(hazeState = chrome.hazeState, onDismiss = { overlays.showTranslationLanguage = false })
    }
    if (overlays.showListenBrainzLogin) {
        var tokenInput by remember { mutableStateOf(feeds.listenBrainzToken) }
        ListenBrainzTokenAlert(
            hazeState = chrome.hazeState,
            tokenInput = tokenInput,
            onTokenInputChange = { tokenInput = it },
            onSave = {
                AppSettings.setListenBrainzToken(tokenInput.trim())
                overlays.showListenBrainzLogin = false
            },
            onDismiss = { overlays.showListenBrainzLogin = false },
        )
    }
    if (overlays.showLastfmLogin) LastfmLoginHost(overlays = overlays, hazeState = chrome.hazeState)
    ServerStorageDialogsHost(overlays = overlays, hazeState = chrome.hazeState)
}

@Composable
private fun LastfmLoginHost(
    overlays: MainOverlayState,
    hazeState: HazeState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var lastfmError by remember { mutableStateOf<String?>(null) }
    var lastfmLoading by remember { mutableStateOf(false) }
    LastfmLoginAlert(
        hazeState = hazeState,
        usernameInput = usernameInput,
        onUsernameInputChange = { usernameInput = it },
        passwordInput = passwordInput,
        onPasswordInputChange = { passwordInput = it },
        error = lastfmError,
        loading = lastfmLoading,
        onSignIn = {
            lastfmLoading = true
            lastfmError = null
            scope.launch {
                try {
                    LastFM.initialize(
                        apiKey = AppSettings.lastfmApiKey.value,
                        secret = AppSettings.lastfmSecret.value,
                    )
                    LastFM.getMobileSession(usernameInput.trim(), passwordInput)
                        .onSuccess { auth ->
                            AppSettings.setLastfmSessionKey(auth.session.key)
                            AppSettings.setLastfmUsername(auth.session.name)
                            AppSettings.setLastfmEnabled(true)
                            overlays.showLastfmLogin = false
                        }
                        .onFailure { e ->
                            lastfmError = e.message ?: context.getString(R.string.login_failed)
                        }
                } catch (e: Exception) {
                    lastfmError = e.message ?: context.getString(R.string.login_failed)
                } finally {
                    lastfmLoading = false
                }
            }
        },
        onDismiss = { if (!lastfmLoading) overlays.showLastfmLogin = false },
    )
}

@Composable
private fun ServerStorageDialogsHost(
    overlays: MainOverlayState,
    hazeState: HazeState,
) {
    if (overlays.showWebDavEditor) {
        BackHandler { overlays.showWebDavEditor = false }
        ServerEditorHost(
            hazeState = hazeState,
            title = stringResource(R.string.webdav),
            description = stringResource(R.string.webdav_description),
            fields = listOf(
                FieldConfig(
                    initial = AppSettings.webdavUrl.value,
                    placeholder = stringResource(R.string.webdav_server_url_hint),
                    keyboardType = KeyboardType.Uri,
                ),
                FieldConfig(
                    initial = AppSettings.webdavUsername.value,
                    placeholder = stringResource(R.string.username),
                ),
                FieldConfig(
                    initial = AppSettings.webdavPassword.value,
                    placeholder = stringResource(R.string.password),
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                ),
            ),
            canSubmit = { it[0].isNotBlank() },
            testFailedRes = R.string.webdav_test_failed,
            onTest = { (url, username, password) ->
                com.music.bitchord.data.webdav.WebDavRepository.testConnection(
                    url.trim(),
                    username.trim(),
                    password,
                )
            },
            onSave = { (url, username, password) ->
                AppSettings.setWebDavUrl(url.trim())
                AppSettings.setWebDavUsername(username.trim())
                AppSettings.setWebDavPassword(password)
                overlays.showWebDavEditor = false
            },
            onDismiss = { overlays.showWebDavEditor = false },
        )
    }

    if (overlays.showSmbEditor) {
        BackHandler { overlays.showSmbEditor = false }
        ServerEditorHost(
            hazeState = hazeState,
            title = stringResource(R.string.smb),
            description = stringResource(R.string.smb_description),
            fields = listOf(
                FieldConfig(
                    initial = AppSettings.smbHost.value,
                    placeholder = stringResource(R.string.smb_server_hint),
                    keyboardType = KeyboardType.Uri,
                ),
                FieldConfig(
                    initial = AppSettings.smbShare.value,
                    placeholder = stringResource(R.string.smb_share_hint),
                ),
                FieldConfig(
                    initial = AppSettings.smbBasePath.value,
                    placeholder = stringResource(R.string.smb_folder_hint),
                ),
                FieldConfig(
                    initial = AppSettings.smbUsername.value,
                    placeholder = stringResource(R.string.username),
                ),
                FieldConfig(
                    initial = AppSettings.smbPassword.value,
                    placeholder = stringResource(R.string.password),
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                ),
            ),
            canSubmit = { it[0].isNotBlank() && it[1].isNotBlank() },
            testFailedRes = R.string.smb_test_failed,
            onTest = { (host, share, folder, username, password) ->
                com.music.bitchord.data.smb.SmbRepository.testConnection(
                    host.trim(),
                    share.trim(),
                    folder.trim(),
                    username.trim(),
                    password,
                )
            },
            onSave = { (host, share, folder, username, password) ->
                AppSettings.setSmbHost(host.trim())
                AppSettings.setSmbShare(share.trim())
                AppSettings.setSmbBasePath(folder.trim())
                AppSettings.setSmbUsername(username.trim())
                AppSettings.setSmbPassword(password)
                overlays.showSmbEditor = false
            },
            onDismiss = { overlays.showSmbEditor = false },
        )
    }

    val uploadConflict by com.music.bitchord.data.webdav.WebDavUploads.conflict.collectAsStateWithLifecycle()
    uploadConflict?.let { req ->
        var applyToAll by remember(req) { mutableStateOf(false) }
        val answer: (com.music.bitchord.data.webdav.WebDavUploads.Choice) -> Unit = { choice ->
            req.answer.complete(
                com.music.bitchord.data.webdav.WebDavUploads.Resolution(choice, applyToAll),
            )
        }
        BackHandler { answer(com.music.bitchord.data.webdav.WebDavUploads.Choice.SKIP) }
        WebDavConflictAlert(
            hazeState = hazeState,
            fileName = req.fileName,
            showApplyToAll = req.remaining > 0,
            applyToAll = applyToAll,
            onApplyToAllChange = { applyToAll = it },
            onOverwrite = { answer(com.music.bitchord.data.webdav.WebDavUploads.Choice.OVERWRITE) },
            onKeepBoth = { answer(com.music.bitchord.data.webdav.WebDavUploads.Choice.KEEP_BOTH) },
            onSkip = { answer(com.music.bitchord.data.webdav.WebDavUploads.Choice.SKIP) },
            onDismiss = { answer(com.music.bitchord.data.webdav.WebDavUploads.Choice.SKIP) },
        )
    }
}

@Composable
internal fun IntegrationAndAlertDialogsHost(
    playerSong: Song?,
    overlays: MainOverlayState,
    chrome: MainChromeState,
) {
    val scope = rememberCoroutineScope()
    val hazeState = chrome.hazeState
    val playerRaised = overlays.showNowPlaying && playerSong != null

    if (overlays.showDiscordLogin) {
        BackHandler { overlays.showDiscordLogin = false }
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { overlays.showDiscordLogin = false }) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Text(
                        "Sign in to Discord",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                DiscordLoginScreen(
                    onTokenCaptured = { token ->
                        AppSettings.setDiscordToken(token)
                        overlays.showDiscordLogin = false
                    },
                )
            }
        }
    }

    if (overlays.showSpotifyLogin) {
        BackHandler { overlays.showSpotifyLogin = false }
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { overlays.showSpotifyLogin = false }) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Text(
                        stringResource(R.string.spotify_sign_in),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                SpotifyLoginScreen(
                    modifier = Modifier.navigationBarsPadding(),
                    onConnected = { token ->
                        AppSettings.setSpotifySpdcToken(token)
                        overlays.showSpotifyLogin = false
                    },
                )
            }
        }
    }

    if (overlays.showSpotifyCanvasAuth) {
        BackHandler { overlays.showSpotifyCanvasAuth = false }
        SpotifyCanvasAuthScreen(onNavigateUp = { overlays.showSpotifyCanvasAuth = false })
    }

    overlays.discordDialog?.let { which ->
        DiscordDialogHost(which = which, hazeState = hazeState, onDismiss = { overlays.discordDialog = null })
    }
    if (overlays.editingPartyServer) {
        PartyServerEditor(hazeState = hazeState, onDismiss = { overlays.editingPartyServer = false })
    }
    if (overlays.editingSignatureApi) {
        SignatureApiEditor(hazeState = hazeState, onDismiss = { overlays.editingSignatureApi = false })
    }
    overlays.editingSource?.let { config ->
        SourceEditorAlert(
            hazeState = hazeState,
            config = config,
            onDismiss = { overlays.editingSource = null },
            onSaved = { overlays.editingSource = null },
            onDelete = {
                SourceRegistry.remove(config.id)
                overlays.editingSource = null
            },
            scope = scope,
        )
    }
    overlays.choosingSourceQuality?.let { config ->
        ChoiceAlert(
            hazeState = hazeState,
            title = stringResource(R.string.audio_quality),
            message = null,
            options = config.qualityOptions,
            selected = config.activeQuality,
            label = { it?.label.orEmpty() },
            onSelect = { option ->
                SourceRegistry.config(config.id)?.let {
                    SourceRegistry.update(it.copy(pickedQuality = option?.value))
                }
                overlays.choosingSourceQuality = null
            },
            onDismiss = { overlays.choosingSourceQuality = null },
        )
    }
    overlays.explainingOutdatedAddon?.let { config ->
        InfoAlert(
            hazeState = hazeState,
            title = config.displayName,
            description = stringResource(R.string.source_needs_update),
            onDismiss = { overlays.explainingOutdatedAddon = null },
        )
    }
    if (overlays.confirmJioSaavn) {
        ConfirmationAlert(
            hazeState = hazeState,
            title = stringResource(R.string.enable_jiosaavn),
            description = stringResource(R.string.jiosaavn_mismatch_warning),
            confirmLabel = stringResource(R.string.enable_anyway),
            onConfirm = {
                SourceRegistry.configs.value
                    .firstOrNull { it.kind == SourceKind.JIOSAAVN }
                    ?.let { SourceRegistry.setEnabled(it.id, true) }
                overlays.confirmJioSaavn = false
            },
            onDismiss = { overlays.confirmJioSaavn = false },
        )
    }
    overlays.confirmBitPerfect?.let { mode ->
        ConfirmationAlert(
            hazeState = hazeState,
            title = stringResource(R.string.bit_perfect_confirm_title),
            description = stringResource(R.string.bit_perfect_confirm_message),
            confirmLabel = stringResource(R.string.enable),
            onConfirm = {
                AppSettings.setBitPerfectMode(mode)
                overlays.confirmBitPerfect = null
            },
            onDismiss = { overlays.confirmBitPerfect = null },
        )
    }
    if (overlays.confirmFixedVolume) {
        ConfirmationAlert(
            hazeState = hazeState,
            title = stringResource(R.string.usb_allow_fixed_volume_confirm_title),
            description = stringResource(R.string.usb_allow_fixed_volume_confirm_message),
            confirmLabel = stringResource(R.string.enable),
            onConfirm = {
                AppSettings.setUsbAllowFixedVolume(true)
                overlays.confirmFixedVolume = false
            },
            onDismiss = { overlays.confirmFixedVolume = false },
        )
    }

    DacVolumeHud(hazeState)

    overlays.blockedAddonSong?.let { song ->
        AddonBlockedAlert(
            hazeState = hazeState,
            song = song,
            onDismiss = { overlays.blockedAddonSong = null },
        )
    }
    overlays.addonLeftOutOfDownload?.let { (addon, why) ->
        InfoAlert(
            hazeState = hazeState,
            title = stringResource(R.string.download_addon_left_out_title, addon),
            description = when (why) {
                SourceRegistry.DownloadExclusion.DOWNLOADS_NOT_ALLOWED ->
                    stringResource(R.string.download_addon_not_allowed, addon)
                SourceRegistry.DownloadExclusion.NEEDS_LOSSLESS_OUTPUT ->
                    stringResource(R.string.download_addon_needs_lossless_output, addon)
            },
            onDismiss = { overlays.addonLeftOutOfDownload = null },
            inOwnWindow = playerRaised || overlays.songActions != null || overlays.browseActions != null,
        )
    }
}

@Composable
internal fun DisplayAndTutorialOverlaysHost(
    playerSong: Song?,
    overlays: MainOverlayState,
    chrome: MainChromeState,
    feeds: MainFeedState,
    playerDock: PlayerDockState,
    playback: MainPlaybackActions,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val player = playback.player
    val hazeState = chrome.hazeState
    val playerRaised = overlays.showNowPlaying && playerSong != null

    if (overlays.showYourMixCard) {
        val cachedMixSongs = remember(feeds.visibleHomeState, player.song) {
            buildList {
                player.song?.let(::add)
                (feeds.visibleHomeState as? UiState.Success)?.data
                    ?.flatMap { it.items }
                    ?.mapNotNull { playback.shelfSong(it) }
                    ?.let(::addAll)
            }.distinctBy { it.videoId }.take(25)
        }
        val heroArt = player.song?.thumbnailUrl
            ?: feeds.topPicks?.firstOrNull()?.images?.firstOrNull()
            ?: (feeds.visibleHomeState as? UiState.Success)?.data?.firstOrNull()?.items?.firstOrNull()?.thumbnailUrl
        YourMixCard(
            listenerName = AppSettings.userName.value.ifBlank { feeds.account?.name },
            heroArtworkUri = heroArt,
            previewSongs = cachedMixSongs,
            hazeState = hazeState,
            onResolveMixSongs = {
                val station = feeds.topPicks?.firstOrNull {
                    it.kind == TopPickKind.PERSONAL_STATION || it.kind == TopPickKind.DISCOVERY_STATION
                }
                val fetched = if (station != null) TopPicks.queue(station).getOrNull().orEmpty() else emptyList()
                (cachedMixSongs + fetched).distinctBy { it.videoId }.take(25)
            },
            onPlayMix = { songsToPlay ->
                playback.playFrom(songsToPlay, 0, QueueSource("Your Mix", PlaybackSourceType.HOME))
            },
            onSaveAsPlaylist = { songsToSave ->
                val deferred = CompletableDeferred<Boolean>()
                val dateStamp = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())
                val playlistTitle = "Your Mix · $dateStamp"
                viewModel.createPlaylistWithVideoIds(
                    title = playlistTitle,
                    privacy = PlaylistPrivacy.PRIVATE,
                    videoIds = songsToSave.map { it.videoId },
                    songs = songsToSave,
                ) { _, title, _ ->
                    Toast.makeText(context, "Saved \"$title\" to Library", Toast.LENGTH_SHORT).show()
                    deferred.complete(true)
                }
                deferred.await()
            },
            onDismiss = { overlays.showYourMixCard = false },
        )
    }

    if (overlays.showDisplayModePrompt && !overlays.showStandByDisplayMode) {
        StandByDisplayModePromptCard(
            hazeState = hazeState,
            onEnterDisplayMode = {
                overlays.showDisplayModePrompt = false
                overlays.showStandByDisplayMode = true
            },
            onDismiss = { overlays.showDisplayModePrompt = false },
            onDontShowAgain = {
                AppSettings.setDontShowDisplayModePrompt(true)
                overlays.showDisplayModePrompt = false
            },
        )
    }

    if (overlays.showStandByDisplayMode) {
        StandByDisplayMode(
            song = player.song,
            isPlaying = player.isPlaying,
            positionMs = player.position.positionMs,
            durationMs = player.durationMs,
            onPlayPause = { playback.togglePlayPause() },
            onNext = { playback.controller?.seekToNextMediaItem() },
            onPrevious = { playback.controller?.seekToPrevious() },
            onExit = { overlays.showStandByDisplayMode = false },
        )
    }

    overlays.lyricsCardTarget?.let { req ->
        if (playerRaised) {
            Dialog(
                onDismissRequest = { overlays.lyricsCardTarget = null },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false,
                ),
            ) {
                val view = LocalView.current
                SideEffect {
                    val window = (view.parent as? DialogWindowProvider)?.window
                    if (window != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                            window.attributes = window.attributes.apply {
                                blurBehindRadius = 48
                            }
                        }
                        window.setDimAmount(0.45f)
                    }
                }
                val dialogHaze = remember { HazeState() }
                AndroidLyricsShareSheet(
                    hazeState = dialogHaze,
                    request = req,
                    onDismiss = { overlays.lyricsCardTarget = null },
                )
            }
        } else {
            AndroidLyricsShareSheet(
                hazeState = hazeState,
                request = req,
                onDismiss = { overlays.lyricsCardTarget = null },
            )
        }
    }

    val showTutorialOverlay = feeds.onboardingComplete &&
        !feeds.tutorialCompleted &&
        !overlays.showDiwali &&
        !overlays.showUpdateDialog &&
        !overlays.showYourMixCard &&
        !overlays.showDisplayModePrompt &&
        !overlays.showStandByDisplayMode &&
        !overlays.showAiPlaylistMaker &&
        feeds.pendingRestartReason == null
    if (showTutorialOverlay) {
        HushSpotlightTutorial(
            hazeState = hazeState,
            onComplete = { AppSettings.setTutorialCompleted(true) },
        )
    }

    feeds.pendingRestartReason?.let { reason ->
        AppRestartPromptCard(
            reason = reason,
            hazeState = hazeState,
            onRestart = { AppRestartController.restartApp(context) },
            onDismiss = { AppRestartController.dismiss() },
        )
    }

    HushLaunchReveal(
        enabled = feeds.onboardingComplete && overlays.appLaunchCount >= 1,
        reduceAnimation = playerDock.reducePlayerMotion,
    )
}
