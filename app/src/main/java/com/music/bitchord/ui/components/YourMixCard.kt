package com.music.bitchord.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.components.expressive.M3EShapes
import com.music.bitchord.ui.components.expressive.ScallopedFlowerShape
import com.music.bitchord.ui.components.expressive.m3eSquish
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch

/**
 * Interactive centered floating card for the Home hero "Your Mix" widget.
 * Animates into place with liquid spring motion over a frosted blurred backdrop
 * and offers two 8-leaf M3E action buttons:
 * - Save as Playlist (lighter shade)
 * - Play Mix (darker shade)
 */
@Composable
fun YourMixCard(
    listenerName: String?,
    heroArtworkUri: String?,
    previewSongs: List<Song>,
    hazeState: HazeState,
    onResolveMixSongs: suspend () -> List<Song>,
    onPlayMix: (List<Song>) -> Unit,
    onSaveAsPlaylist: suspend (List<Song>) -> Boolean,
    onDismiss: () -> Unit,
) {
    val reduceBlur by AppSettings.reduceDynamicBlur.collectAsStateWithLifecycle()
    val heroArtShapeName by AppSettings.expressiveAlbumArtShape.collectAsStateWithLifecycle()
    val heroArtShape = M3EShapes.fromName(heroArtShapeName)
    val leaf8Shape = remember { ScallopedFlowerShape(lobes = 8, innerRadiusRatio = 0.80f) }
    val scope = rememberCoroutineScope()

    var mixSongs by remember(previewSongs) { mutableStateOf(previewSongs) }
    var resolving by remember { mutableStateOf(false) }
    var savingPlaylist by remember { mutableStateOf(false) }
    var savedPlaylist by remember { mutableStateOf(false) }

    val liquidScale = remember { Animatable(0.76f) }
    LaunchedEffect(Unit) {
        liquidScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = 320f,
            ),
        )
    }

    LaunchedEffect(Unit) {
        if (mixSongs.size < 8) {
            resolving = true
            val resolved = onResolveMixSongs()
            if (resolved.isNotEmpty()) mixSongs = resolved
            resolving = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mixCardSpin")
    val spin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "mixCardRotation",
    )

    val displayName = listenerName?.takeIf { it.isNotBlank() && it != "Listener" }
    val subtitleText = if (displayName != null) {
        "Personalized mix crafted for $displayName"
    } else {
        "Personalized mix curated from your taste"
    }
    val artistSummary = remember(mixSongs) {
        mixSongs.mapNotNull { it.artist.takeIf { a -> a.isNotBlank() && a != "Unknown artist" } }
            .distinct()
            .take(3)
            .joinToString(" · ")
    }

    // Lighter shade for Save Playlist, deeper darker shade for Play
    val accent = MaterialTheme.colorScheme.primary
    val saveButtonBg = lerp(accent, Color.White, 0.44f)
    val saveButtonFg = Color(0xFF14151A)
    val playButtonBg = lerp(accent, Color(0xFF0A0B10), 0.36f)
    val playButtonFg = Color.White

    HushCenteredCard(
        onDismiss = onDismiss,
        hazeState = hazeState,
        reduceBlur = reduceBlur,
        title = "Your Mix",
        subtitle = "Hush Personal Blend",
        maxWidth = 390.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = liquidScale.value
                    scaleY = liquidScale.value
                }
                .padding(top = 12.dp, bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.32f),
                                Color.Transparent,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(106.dp)
                        .graphicsLayer { rotationZ = spin }
                        .clip(heroArtShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.5.dp, accent.copy(alpha = 0.4f), heroArtShape),
                    contentAlignment = Alignment.Center,
                ) {
                    val cover = heroArtworkUri ?: mixSongs.firstOrNull()?.thumbnailUrl
                    if (!cover.isNullOrBlank()) {
                        AsyncImage(
                            model = cover,
                            contentDescription = "Your Mix Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationZ = -spin
                                    scaleX = 1.42f
                                    scaleY = 1.42f
                                },
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.LibraryMusic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(44.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = subtitleText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = when {
                    resolving && mixSongs.isEmpty() -> "Building your mix..."
                    artistSummary.isNotBlank() && mixSongs.isNotEmpty() ->
                        "${mixSongs.size} tracks · Feat. $artistSummary"
                    mixSongs.isNotEmpty() -> "${mixSongs.size} tracks tailored to your recent listening"
                    else -> "Tap Play to launch a fresh personal station"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            if (mixSongs.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    mixSongs.take(3).forEachIndexed { idx, song ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${idx + 1}. ${song.title}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            if (song.artist.isNotBlank()) {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // Two 8-leaf scalloped action buttons: Save as Playlist (lighter shade) & Play (darker shade)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .m3eSquish()
                            .clip(leaf8Shape)
                            .background(saveButtonBg)
                            .border(1.dp, accent.copy(alpha = 0.28f), leaf8Shape)
                            .clickable(enabled = !savingPlaylist && !savedPlaylist) {
                                savingPlaylist = true
                                scope.launch {
                                    val songsToSave = mixSongs.ifEmpty { onResolveMixSongs() }
                                    if (songsToSave.isNotEmpty()) {
                                        mixSongs = songsToSave
                                        savedPlaylist = onSaveAsPlaylist(songsToSave)
                                    }
                                    savingPlaylist = false
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (savingPlaylist) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = saveButtonFg,
                                strokeWidth = 2.2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = if (savedPlaylist) Icons.Rounded.Check else Icons.Rounded.PlaylistAdd,
                                contentDescription = "Save as Playlist",
                                tint = saveButtonFg,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                    Text(
                        text = if (savedPlaylist) "Saved" else "Save Playlist",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .m3eSquish()
                            .clip(leaf8Shape)
                            .background(playButtonBg)
                            .border(1.dp, Color.White.copy(alpha = 0.14f), leaf8Shape)
                            .clickable(enabled = !resolving) {
                                scope.launch {
                                    val songsToPlay = mixSongs.ifEmpty { onResolveMixSongs() }
                                    if (songsToPlay.isNotEmpty()) {
                                        onPlayMix(songsToPlay)
                                        onDismiss()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Play Mix",
                            tint = playButtonFg,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    Text(
                        text = "Play",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
