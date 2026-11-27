package com.music.bitchord.ui.player

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.R
import com.music.bitchord.data.lyrics.LyricsRepository
import com.music.bitchord.data.model.durationMillis
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.components.ContinuousCornerShape
import com.music.bitchord.ui.components.HushCenteredCard
import com.music.bitchord.ui.components.expressive.M3EShapes
import com.music.bitchord.ui.haptics.Haptic
import com.music.bitchord.ui.haptics.rememberHaptics
import com.music.bitchord.ui.replay.ShareAction
import com.music.bitchord.ui.replay.cacheForSharing
import com.music.bitchord.ui.replay.saveToGallery
import com.music.bitchord.ui.replay.sendIntent
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Lyric Card Maker:
 * - Compact centered frosted Squircle geometry (ContinuousCornerShape) floating over backdrop blur
 * - Background Choice toggle: Album Gradient vs active App Theme
 * - Line range / quick count selector (automatically loads track lyrics when opened from any 3-dots menu)
 * - Small top-left Album Cover M3E shape customizer
 * - Dedicated Add to Story (Instagram/Facebook Stories intent + Story-compatible fallback), Save & Share
 */
@Composable
internal fun AndroidLyricsShareSheet(
    hazeState: HazeState,
    request: LyricsShareRequest,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val reduceBlur by AppSettings.reduceDynamicBlur.collectAsStateWithLifecycle()
    val syncedLyricsEnabled by AppSettings.syncedLyrics.collectAsStateWithLifecycle()
    val lyricsSources by AppSettings.lyricsSources.collectAsStateWithLifecycle()
    val lyricsSourceOrder by AppSettings.lyricsSourceOrder.collectAsStateWithLifecycle()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val playerPrimaryArgb = primaryColor.toArgb()
    val playerSecondaryArgb = secondaryColor.toArgb()
    val playerSurfaceArgb = surfaceColor.toArgb()

    var loadedAllLines by remember(request) { mutableStateOf(request.allLines) }
    var loadingLyrics by remember(request) { mutableStateOf(request.allLines.isEmpty() && request.lines.isEmpty()) }
    var selectedIndices by remember(request) {
        mutableStateOf(
            request.selectedIndices
                .ifEmpty { request.lines.indices.toList() }
                .toSet(),
        )
    }

    // Fetch song lyrics automatically when Lyric Card Maker is opened directly from 3-dots menu
    LaunchedEffect(request.song.videoId) {
        if (request.allLines.isEmpty() && request.lines.isEmpty()) {
            loadingLyrics = true
            val fetched = withContext(Dispatchers.IO) {
                LyricsRepository.lyrics(
                    videoId = request.song.videoId,
                    title = request.song.title,
                    artist = request.song.artist,
                    durationMs = request.song.durationMillis(),
                    album = request.song.albumName,
                    sources = if (syncedLyricsEnabled) lyricsSources else lyricsSources,
                    order = lyricsSourceOrder,
                )
            }
            val parsed = fetched?.lines
                .orEmpty()
                .filter { it.text.isNotBlank() }
                .map { LyricsShareLine(text = it.text, subText = null) }
            loadedAllLines = parsed
            if (parsed.isNotEmpty()) {
                selectedIndices = (0 until minOf(4, parsed.size)).toSet()
            }
            loadingLyrics = false
        }
    }

    var themeMode by remember(request) { mutableStateOf(LyricsCardThemeMode.ALBUM_GRADIENT) }
    var coverShapeName by remember(request) { mutableStateOf("Squircle") }
    var storyPreview by remember(request) { mutableStateOf(false) }
    var pickingLines by remember(request) { mutableStateOf(false) }
    var storyBusy by remember { mutableStateOf(false) }

    val activeLines = remember(request, loadedAllLines, selectedIndices) {
        val pool = loadedAllLines
        if (pool.isEmpty()) {
            request.lines
        } else {
            val sorted = selectedIndices.filter { it in pool.indices }.sorted()
            if (sorted.isEmpty()) {
                pool.take(minOf(4, pool.size))
            } else {
                buildList {
                    sorted.forEachIndexed { pos, idx ->
                        if (pos > 0 && idx > sorted[pos - 1] + 1) {
                            add(LyricsShareLine(text = "", subText = null, isGap = true))
                        }
                        pool.getOrNull(idx)?.let(::add)
                    }
                }
            }
        }
    }

    val effectiveRequest = remember(request, activeLines, loadedAllLines) {
        request.copy(
            lines = activeLines,
            allLines = loadedAllLines,
        )
    }

    var renderResult by remember(effectiveRequest) { mutableStateOf<LyricsCardRenderResult?>(null) }
    var failed by remember(effectiveRequest) { mutableStateOf(false) }
    var saved by remember(effectiveRequest) { mutableStateOf(false) }

    LaunchedEffect(
        effectiveRequest,
        storyPreview,
        themeMode,
        coverShapeName,
        playerPrimaryArgb,
        playerSecondaryArgb,
        playerSurfaceArgb,
    ) {
        failed = false
        saved = false
        kotlinx.coroutines.delay(65L)
        renderResult = runCatching {
            renderLyricsShareCardWithColors(
                context = context,
                card = effectiveRequest,
                story = storyPreview,
                themeMode = themeMode,
                coverShapeName = coverShapeName,
                playerPrimaryArgb = playerPrimaryArgb,
                playerSecondaryArgb = playerSecondaryArgb,
                playerSurfaceArgb = playerSurfaceArgb,
            )
        }
            .onFailure { failed = true }
            .getOrNull()
    }

    val shapeOptions = remember {
        listOf(
            "Squircle",
            "Rounded",
            "8-Leaf Clover",
            "Flower",
            "Sunny",
            "4-Sided Cookie",
            "Circle",
            "Puffy",
            "Heart",
            "Hexagon",
            "Starburst",
        )
    }

    HushCenteredCard(
        onDismiss = onDismiss,
        hazeState = hazeState,
        reduceBlur = reduceBlur,
        title = stringResource(R.string.lyrics_share_title),
        subtitle = if (loadedAllLines.isNotEmpty()) {
            "${activeLines.count { !it.isGap }} lines selected"
        } else {
            request.song.artist
        },
        maxWidth = 372.dp,
        maxHeight = 610.dp,
        cornerShape = ContinuousCornerShape(30.dp),
        modifier = modifier,
        headerEnd = {
            if (loadedAllLines.size > 1) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (pickingLines) primaryColor
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        )
                        .clickable { pickingLines = !pickingLines }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = if (pickingLines) Icons.Rounded.Check else Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = if (pickingLines) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = if (pickingLines) "Done" else "Lines",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (pickingLines) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (loadingLyrics) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                        Text(
                            text = "Loading lyrics...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else if (pickingLines && loadedAllLines.isNotEmpty()) {
                // Quick Range Count Selector + Individual Line Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Range:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    listOf(2, 4, 6).forEach { count ->
                        val anchor = selectedIndices.minOrNull()?.coerceIn(0, (loadedAllLines.lastIndex).coerceAtLeast(0)) ?: 0
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .clickable {
                                    haptics.play(Haptic.Tap)
                                    val end = (anchor + count - 1).coerceAtMost(loadedAllLines.lastIndex)
                                    selectedIndices = (anchor..end).toSet()
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                        ) {
                            Text(
                                text = "$count lines",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    loadedAllLines.forEachIndexed { idx, line ->
                        if (line.text.isBlank()) return@forEachIndexed
                        val checked = idx in selectedIndices
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ContinuousCornerShape(12.dp))
                                .background(
                                    if (checked) primaryColor.copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (checked) primaryColor else Color.Transparent,
                                    shape = ContinuousCornerShape(12.dp),
                                )
                                .clickable {
                                    haptics.play(Haptic.Tap)
                                    selectedIndices = if (checked) {
                                        if (selectedIndices.size > 1) selectedIndices - idx else selectedIndices
                                    } else {
                                        selectedIndices + idx
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (checked) primaryColor
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (checked) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                            }
                            Text(
                                text = line.text,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            } else {
                if (loadedAllLines.size > 1) {
                    Text(
                        text = "Tap preview to pick line range",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                val bitmap = renderResult?.bitmap
                val ratio = bitmap?.let { it.width.toFloat() / it.height } ?: (9f / 16f)
                val previewModifier = Modifier
                    .widthIn(max = if (storyPreview) 186.dp else 270.dp)
                    .fillMaxWidth()
                    .aspectRatio(ratio)
                    .shadow(14.dp, ContinuousCornerShape(22.dp))
                    .clip(ContinuousCornerShape(22.dp))
                    .clickable(enabled = loadedAllLines.size > 1) { pickingLines = true }

                if (bitmap == null) {
                    Box(
                        modifier = previewModifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    }
                } else {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.your_lyrics_card),
                        modifier = previewModifier,
                    )
                }
            }

            // Background Choice Toggle: Album Gradient vs App Theme
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ContinuousCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                LyricsCardThemeMode.entries.forEach { mode ->
                    val selected = themeMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(ContinuousCornerShape(11.dp))
                            .background(if (selected) primaryColor else Color.Transparent)
                            .clickable {
                                haptics.play(Haptic.Tap)
                                themeMode = mode
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }

            // Small Top-Left Cover Shape Customizer (M3EShapes)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "COVER ART SHAPE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    shapeOptions.forEach { shapeName ->
                        val selected = coverShapeName == shapeName
                        val previewShape = when (shapeName) {
                            "Squircle" -> ContinuousCornerShape(6.dp)
                            "Rounded" -> RoundedCornerShape(4.dp)
                            else -> M3EShapes.fromName(shapeName)
                        }
                        Row(
                            modifier = Modifier
                                .clip(ContinuousCornerShape(12.dp))
                                .background(
                                    if (selected) primaryColor.copy(alpha = 0.22f)
                                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) primaryColor else Color.Transparent,
                                    shape = ContinuousCornerShape(12.dp),
                                )
                                .clickable {
                                    haptics.play(Haptic.Tap)
                                    coverShapeName = shapeName
                                }
                                .padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(previewShape)
                                    .background(if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)),
                            )
                            Text(
                                text = shapeName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                ),
                                color = if (selected) primaryColor else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            // Toggle Preview Ratio (Card vs 9:16 Story Frame)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ContinuousCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable {
                        haptics.play(Haptic.Tap)
                        storyPreview = !storyPreview
                    }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhoneIphone,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (storyPreview) "Switch to Compact Card View" else "Preview Full 9:16 Story Frame",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            AnimatedVisibility(visible = failed, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = stringResource(R.string.couldnt_draw_picture),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Dedicated "Add to Story" Button (Instagram / Facebook Stories intent + fallback)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ContinuousCornerShape(16.dp))
                .background(primaryColor.copy(alpha = if (renderResult != null && !storyBusy) 1f else 0.45f))
                .clickable(enabled = renderResult != null && !storyBusy) {
                    haptics.play(Haptic.Tap)
                    storyBusy = true
                    scope.launch {
                        val renderedStory = runCatching {
                            renderLyricsShareCardWithColors(
                                context = context,
                                card = effectiveRequest,
                                story = true,
                                themeMode = themeMode,
                                coverShapeName = coverShapeName,
                                playerPrimaryArgb = playerPrimaryArgb,
                                playerSecondaryArgb = playerSecondaryArgb,
                                playerSurfaceArgb = playerSurfaceArgb,
                            )
                        }.getOrNull()
                        val uri = renderedStory?.let {
                            cacheForSharing(context, it.bitmap, STORY_FILE_NAME)
                        }
                        storyBusy = false
                        if (renderedStory == null || uri == null) {
                            failed = true
                        } else {
                            launchAddToStory(
                                context = context,
                                storyUri = uri,
                                topColorHex = renderedStory.topHexColor,
                                bottomColorHex = renderedStory.bottomHexColor,
                            )
                        }
                    }
                }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (storyBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.width(8.dp))
            } else {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = "Add to Story",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ShareAction(
                label = stringResource(R.string.save),
                icon = Icons.Rounded.Download,
                accent = false,
                enabled = renderResult != null,
                modifier = Modifier.weight(1f),
                saved = saved,
                savedLabel = stringResource(R.string.saved_bang),
                onClick = {
                    val drawn = renderResult?.bitmap
                    if (drawn != null) {
                        haptics.play(Haptic.Tap)
                        scope.launch {
                            saved = saveToGallery(
                                context,
                                drawn,
                                request.song.title,
                                LYRICS_FILE_PREFIX,
                            )
                        }
                    }
                },
            )
            ShareAction(
                label = stringResource(R.string.share),
                icon = Icons.Rounded.IosShare,
                accent = false,
                enabled = renderResult != null,
                modifier = Modifier.weight(1f),
                onClick = {
                    val drawn = renderResult?.bitmap
                    if (drawn != null) {
                        haptics.play(Haptic.Tap)
                        scope.launch {
                            val uri = cacheForSharing(context, drawn, LYRICS_FILE_NAME)
                            if (uri == null) {
                                failed = true
                                saved = false
                            } else {
                                context.startActivity(
                                    Intent.createChooser(
                                        sendIntent(uri),
                                        context.getString(R.string.share_your_lyrics),
                                    ),
                                )
                            }
                        }
                    }
                },
            )
        }
    }
}

private fun launchAddToStory(
    context: Context,
    storyUri: Uri,
    topColorHex: String,
    bottomColorHex: String,
) {
    val pm = context.packageManager

    val igIntent = Intent("com.instagram.share.ADD_TO_STORY").apply {
        setDataAndType(storyUri, "image/png")
        putExtra("source_application", context.packageName)
        putExtra("top_background_color", topColorHex)
        putExtra("bottom_background_color", bottomColorHex)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val fbIntent = Intent("com.facebook.stories.ADD_TO_STORY").apply {
        setDataAndType(storyUri, "image/png")
        putExtra("com.facebook.platform.extra.APPLICATION_ID", context.packageName)
        putExtra("top_background_color", topColorHex)
        putExtra("bottom_background_color", bottomColorHex)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val hasIg = pm.queryIntentActivities(igIntent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()
    val hasFb = pm.queryIntentActivities(fbIntent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()

    listOf("com.instagram.android", "com.facebook.katana", "com.facebook.lite").forEach { pkg ->
        runCatching { context.grantUriPermission(pkg, storyUri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    }

    val fallbackSend = sendIntent(storyUri)

    runCatching {
        when {
            hasIg && hasFb -> {
                val chooser = Intent.createChooser(fallbackSend, "Add to Story").apply {
                    putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(igIntent, fbIntent))
                }
                context.startActivity(chooser)
            }
            hasIg -> {
                val chooser = Intent.createChooser(fallbackSend, "Add to Story").apply {
                    putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(igIntent))
                }
                context.startActivity(chooser)
            }
            hasFb -> {
                val chooser = Intent.createChooser(fallbackSend, "Add to Story").apply {
                    putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(fbIntent))
                }
                context.startActivity(chooser)
            }
            else -> {
                context.startActivity(Intent.createChooser(fallbackSend, "Add to Story"))
            }
        }
    }.onFailure {
        runCatching {
            context.startActivity(Intent.createChooser(fallbackSend, "Add to Story"))
        }
    }
}

private const val LYRICS_FILE_NAME = "lyrics.png"
private const val STORY_FILE_NAME = "lyrics-story.png"
private const val LYRICS_FILE_PREFIX = "hush-lyrics"
