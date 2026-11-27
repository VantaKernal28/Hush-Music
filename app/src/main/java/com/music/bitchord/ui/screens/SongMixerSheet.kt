package com.music.bitchord.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.bitchord.data.YtMusicRepository
import com.music.bitchord.data.model.SearchFilter
import com.music.bitchord.data.model.SearchResult
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.download.RemixExporter
import com.music.bitchord.playback.EqualizerPreset
import com.music.bitchord.playback.RemixStudioController
import com.music.bitchord.ui.components.HushCenteredCard
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class RemixPreset(
    val id: String,
    val name: String,
    val speed: Float,
    val pitch: Float,
    val crossfadeSeconds: Int,
    val description: String,
)

/**
 * Centered Frosted Remix Studio:
 * - Single Track DSP (Slowed + Reverb, Nightcore, Bass Boost, Vaporwave, Speed, Pitch, Crossfade)
 * - Auto-resets to Normal when the next song starts
 * - Live 2-Song Remix (dual independent deck playback with volume/speed/pitch/seek)
 * - Local 16-bit WAV export into Music/Hush Remixes
 */
@Composable
fun SongMixerSheet(
    song: Song?,
    candidateSongs: List<Song> = emptyList(),
    hazeState: HazeState? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentSpeed by AppSettings.playbackSpeed.collectAsStateWithLifecycle()
    val currentPitch by AppSettings.playbackPitch.collectAsStateWithLifecycle()
    val crossfadeSeconds by AppSettings.crossfadeSeconds.collectAsStateWithLifecycle()
    val activePresetId by RemixStudioController.activePresetId.collectAsStateWithLifecycle()

    val secondTrack by RemixStudioController.secondTrack.collectAsStateWithLifecycle()
    val secondPlaying by RemixStudioController.secondTrackPlaying.collectAsStateWithLifecycle()
    val secondLoading by RemixStudioController.secondTrackLoading.collectAsStateWithLifecycle()
    val secondPositionMs by RemixStudioController.secondTrackPositionMs.collectAsStateWithLifecycle()
    val secondDurationMs by RemixStudioController.secondTrackDurationMs.collectAsStateWithLifecycle()
    val deckAVolume by RemixStudioController.deckAVolume.collectAsStateWithLifecycle()
    val deckBVolume by RemixStudioController.deckBVolume.collectAsStateWithLifecycle()
    val deckBSpeed by RemixStudioController.deckBSpeed.collectAsStateWithLifecycle()
    val deckBPitch by RemixStudioController.deckBPitch.collectAsStateWithLifecycle()

    // 0 = Single Track, 1 = 2-Song Remix
    var studioTab by remember { mutableIntStateOf(if (secondTrack != null) 1 else 0) }
    var exporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableFloatStateOf(0f) }
    var exportStatusMessage by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Song>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.length < 2) {
            searchResults = emptyList()
            searching = false
            return@LaunchedEffect
        }
        searching = true
        delay(320L)
        val found = YtMusicRepository.search(q, SearchFilter.SONGS)
            .getOrDefault(emptyList())
            .filterIsInstance<SearchResult.Track>()
            .map { it.song }
            .filter { it.videoId != song?.videoId }
            .take(12)
        searchResults = found
        searching = false
    }

    val presets = remember {
        listOf(
            RemixPreset("orig", "Original", 1.0f, 1.0f, 0, "Default studio sound"),
            RemixPreset("slow_rev", "Slowed + Reverb", 0.85f, 0.88f, 3, "Deep mellow slowed vibe"),
            RemixPreset("nightcore", "Nightcore", 1.25f, 1.25f, 0, "Fast high-energy cadence"),
            RemixPreset("bass_boost", "Bass Boost", 1.0f, 1.0f, 0, "Heavy punchy low-end EQ"),
            RemixPreset("vaporwave", "Vaporwave", 0.80f, 0.78f, 5, "Retro atmospheric warp"),
            RemixPreset("chipmunk", "Chipmunk", 1.15f, 1.35f, 0, "Bright crisp vocal shift"),
        )
    }

    fun applyPreset(preset: RemixPreset) {
        AppSettings.setPlaybackSpeed(preset.speed)
        AppSettings.setPlaybackPitch(preset.pitch)
        AppSettings.setCrossfadeSeconds(preset.crossfadeSeconds)
        when (preset.id) {
            "bass_boost", "slow_rev" -> {
                RemixStudioController.setActivePreset(preset.id, appliedEqByPreset = true)
                RemixStudioController.applyEqPresetFromRemix(EqualizerPreset.BASS_BOOST)
            }
            "orig" -> {
                if (RemixStudioController.presetAppliedEq) {
                    AppSettings.setEqualizerEnabled(false)
                    AppSettings.setEqualizerPreset(EqualizerPreset.FLAT)
                }
                RemixStudioController.setActivePreset("orig", appliedEqByPreset = false)
            }
            else -> {
                if (RemixStudioController.presetAppliedEq) {
                    AppSettings.setEqualizerEnabled(false)
                    AppSettings.setEqualizerPreset(EqualizerPreset.FLAT)
                }
                RemixStudioController.setActivePreset(preset.id, appliedEqByPreset = false)
            }
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    val surfaceBox = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.78f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    HushCenteredCard(
        onDismiss = onDismiss,
        hazeState = hazeState,
        title = "Remix Studio",
        subtitle = if (secondTrack != null) "2-Song Mix Active" else "Live DSP · Resets on next song",
        maxWidth = 440.dp,
        maxHeight = 640.dp,
        modifier = modifier,
        headerEnd = {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .clickable {
                        AppSettings.setCrossfadeSeconds(0)
                        RemixStudioController.resetForNewTrack()
                        exportStatusMessage = null
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Reset Remix",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = "Reset",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        // Segmented Mode Toggle: Single Track | 2-Song Remix
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf("Single Track" to 0, "2-Song Remix" to 1).forEach { (label, idx) ->
                val selected = studioTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) primary else Color.Transparent)
                        .clickable { studioTab = idx }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (song != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(surfaceBox)
                            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                            .padding(12.dp),
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
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = if (studioTab == 1) "DECK A · ${song.title}" else song.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (activePresetId != "orig" || currentSpeed != 1.0f || currentPitch != 1.0f) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(primary.copy(alpha = 0.16f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = "${"%.2f".format(currentSpeed)}x",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (studioTab == 0) {
                // Single Track: 1-Tap Presets
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "REMIX PRESETS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.9.sp,
                            ),
                            color = primary,
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(presets) { preset ->
                                val isSelected = activePresetId == preset.id ||
                                    (preset.id == "orig" && activePresetId == "orig" && currentSpeed == 1.0f && currentPitch == 1.0f)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) primary
                                            else MaterialTheme.colorScheme.surfaceContainerHigh
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) primary else borderColor,
                                            RoundedCornerShape(14.dp),
                                        )
                                        .clickable { applyPreset(preset) }
                                        .padding(horizontal = 14.dp, vertical = 9.dp),
                                ) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }

                // Speed / Tempo Control
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(surfaceBox)
                            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Speed,
                                        contentDescription = null,
                                        tint = primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "Tempo / Speed",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                Text(
                                    text = "${"%.2f".format(currentSpeed)}x",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = primary,
                                )
                            }

                            Slider(
                                value = currentSpeed,
                                onValueChange = {
                                    RemixStudioController.setActivePreset("custom", RemixStudioController.presetAppliedEq)
                                    AppSettings.setPlaybackSpeed(it)
                                },
                                valueRange = 0.5f..1.5f,
                                steps = 19,
                                colors = SliderDefaults.colors(
                                    thumbColor = primary,
                                    activeTrackColor = primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                ),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("0.5x", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("1.0x Normal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("1.5x", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Pitch Shift Control
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(surfaceBox)
                            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "Pitch Shift",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                Text(
                                    text = "${"%.2f".format(currentPitch)}x",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }

                            Slider(
                                value = currentPitch,
                                onValueChange = {
                                    RemixStudioController.setActivePreset("custom", RemixStudioController.presetAppliedEq)
                                    AppSettings.setPlaybackPitch(it)
                                },
                                valueRange = 0.5f..1.5f,
                                steps = 19,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.secondary,
                                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                ),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("0.5x Deep", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("1.0x Original", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("1.5x High", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Track Crossfade Slider
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(surfaceBox)
                            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "Track Transition Blend",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                Text(
                                    text = if (crossfadeSeconds == 0) "Off" else "${crossfadeSeconds}s",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }

                            Slider(
                                value = crossfadeSeconds.toFloat(),
                                onValueChange = { AppSettings.setCrossfadeSeconds(it.roundToInt()) },
                                valueRange = 0f..12f,
                                steps = 11,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.tertiary,
                                    activeTrackColor = MaterialTheme.colorScheme.tertiary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                ),
                            )
                        }
                    }
                }
            } else {
                // 2-Song Remix Mode
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(surfaceBox)
                            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Deck A Mix Level",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "${(deckAVolume * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = primary,
                                )
                            }
                            Slider(
                                value = deckAVolume,
                                onValueChange = { RemixStudioController.setDeckAVolume(it) },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(thumbColor = primary, activeTrackColor = primary),
                            )
                        }
                    }
                }

                if (secondTrack == null) {
                    // Pick Second Song for Deck B
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(surfaceBox)
                                .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                                .padding(14.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "PICK DECK B SONG",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                                    color = primary,
                                )

                                // Inline Search Input
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search any track to layer in live...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            )
                                        }
                                        BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                            ),
                                            cursorBrush = SolidColor(primary),
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                    if (searching) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    } else if (searchQuery.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { searchQuery = "" },
                                        )
                                    }
                                }

                                val displayPool = remember(searchResults, candidateSongs, song, searchQuery) {
                                    val base = if (searchQuery.trim().length >= 2) {
                                        searchResults
                                    } else {
                                        candidateSongs.filter { it.videoId != song?.videoId }
                                    }
                                    base.distinctBy { it.videoId }.take(12)
                                }

                                if (displayPool.isEmpty() && !searching) {
                                    Text(
                                        text = if (searchQuery.trim().length >= 2) {
                                            "No matching tracks found"
                                        } else {
                                            "Play or queue more songs, or search above"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 210.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        displayPool.take(6).forEach { pick ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f))
                                                    .clickable { RemixStudioController.selectSecondTrack(pick) }
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            ) {
                                                AsyncImage(
                                                    model = pick.thumbnailUrl,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                )
                                                Column(Modifier.weight(1f)) {
                                                    Text(
                                                        text = pick.title,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                    Text(
                                                        text = pick.artist,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(primary.copy(alpha = 0.16f))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                ) {
                                                    Text(
                                                        text = "Load",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = primary,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val deckB = secondTrack!!
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(surfaceBox)
                                .border(1.dp, primary.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                                .padding(14.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    AsyncImage(
                                        model = deckB.thumbnailUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = "DECK B · ${deckB.title}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = deckB.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(primary)
                                            .clickable { RemixStudioController.toggleSecondTrackPlayPause() },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (secondLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (secondPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                                contentDescription = if (secondPlaying) "Pause Deck B" else "Play Deck B",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                            .clickable { RemixStudioController.selectSecondTrack(null) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Unload Deck B",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }

                                // Deck B Seekbar
                                val bFrac = if (secondDurationMs > 0L) {
                                    (secondPositionMs.toFloat() / secondDurationMs.toFloat()).coerceIn(0f, 1f)
                                } else {
                                    0f
                                }
                                Slider(
                                    value = bFrac,
                                    onValueChange = { RemixStudioController.seekSecondTrackToFraction(it) },
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.secondary,
                                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                                    ),
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(formatMixerMs(secondPositionMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(formatMixerMs(secondDurationMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                // Deck B Volume, Speed, Pitch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Deck B Volume", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                                    Text("${(deckBVolume * 100).roundToInt()}%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = primary)
                                }
                                Slider(
                                    value = deckBVolume,
                                    onValueChange = { RemixStudioController.setDeckBVolume(it) },
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Deck B Speed", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                                    Text("${"%.2f".format(deckBSpeed)}x", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                                }
                                Slider(
                                    value = deckBSpeed,
                                    onValueChange = { RemixStudioController.setDeckBSpeed(it) },
                                    valueRange = 0.5f..1.5f,
                                    steps = 19,
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Deck B Pitch", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                                    Text("${"%.2f".format(deckBPitch)}x", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.tertiary)
                                }
                                Slider(
                                    value = deckBPitch,
                                    onValueChange = { RemixStudioController.setDeckBPitch(it) },
                                    valueRange = 0.5f..1.5f,
                                    steps = 19,
                                )
                            }
                        }
                    }
                }
            }

            // Export / Save Remix Locally Card
            if (song != null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(surfaceBox)
                            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (exporting) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Rendering Remix WAV...",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "${(exportProgress * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = primary,
                                )
                            }
                            LinearProgressIndicator(
                                progress = { exportProgress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                            )
                        } else {
                            AnimatedContent(
                                targetState = exportStatusMessage,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "exportBanner",
                            ) { msg ->
                                if (msg != null) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = primary,
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(primary)
                                    .clickable {
                                        if (exporting) return@clickable
                                        exporting = true
                                        exportProgress = 0.02f
                                        exportStatusMessage = null
                                        scope.launch {
                                            val result = RemixExporter.exportRemixToDevice(
                                                context = context,
                                                primarySong = song,
                                                secondTrack = secondTrack,
                                                speed = currentSpeed,
                                                pitch = currentPitch,
                                                deckAVolume = deckAVolume,
                                                deckBVolume = deckBVolume,
                                                deckBSpeed = deckBSpeed,
                                                deckBPitch = deckBPitch,
                                                onProgress = { exportProgress = it },
                                            )
                                            exporting = false
                                            result.onSuccess { savedLabel ->
                                                exportStatusMessage = "Saved to $savedLabel"
                                                Toast.makeText(
                                                    context,
                                                    "Remix saved to $savedLabel",
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }.onFailure { err ->
                                                exportStatusMessage = err.message ?: "Export failed"
                                                Toast.makeText(
                                                    context,
                                                    exportStatusMessage,
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                        }
                                    }
                                    .padding(vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (secondTrack != null) "Download 2-Song Remix Locally" else "Download Remix Locally",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatMixerMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = ms / 1000L
    return "${totalSec / 60L}:${(totalSec % 60L).toString().padStart(2, '0')}"
}
