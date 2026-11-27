package com.music.bitchord.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.components.HushCenteredCard
import com.music.bitchord.ui.components.expressive.M3EShapes
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centered Frosted Prompt Card displayed when rotating into landscape:
 * "Do you want to go into Display Mode?" with Yes / Not Now / Do not show again.
 */
@Composable
fun StandByDisplayModePromptCard(
    hazeState: HazeState?,
    onEnterDisplayMode: () -> Unit,
    onDismiss: () -> Unit,
    onDontShowAgain: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    HushCenteredCard(
        onDismiss = onDismiss,
        hazeState = hazeState,
        title = "Display Mode",
        subtitle = "Apple StandBy-style Horizon Clock",
        maxWidth = 390.dp,
        maxHeight = 360.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(28.dp),
                )
            }

            Text(
                text = "Do you want to go into Display Mode? Enjoy a full-screen StandBy desk clock paired with live album art.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Not Now",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(primary)
                        .clickable(onClick = onEnterDisplayMode)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Enter Display",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            Text(
                text = "Do not show again",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onDontShowAgain)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

/**
 * Full-screen Apple StandBy-style Display Mode for horizontal dock listening.
 */
@Composable
fun StandByDisplayMode(
    song: Song?,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onExit: () -> Unit,
) {
    BackHandler(enabled = true, onBack = onExit)

    val artShapeName by AppSettings.expressivePlayerArtShape.collectAsStateWithLifecycle()
    val albumShape = remember(artShapeName) { M3EShapes.fromName(artShapeName) }
    val isRotational = remember(artShapeName) { M3EShapes.isRotationalName(artShapeName) }

    val artPalette = rememberArtworkColors(song?.thumbnailUrl)
    val dominant = artPalette.colors.getOrNull(0) ?: Color(0xFF3E4B76)
    val vibrant = artPalette.colors.getOrNull(1) ?: dominant

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val secFormat = remember { SimpleDateFormat("ss", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }
    val dateObj = remember(nowMillis) { Date(nowMillis) }

    // Smooth cover rotation when playing
    val rotationAnim = remember { Animatable(0f) }
    LaunchedEffect(isPlaying, isRotational, song?.videoId) {
        if (!isPlaying || !isRotational) return@LaunchedEffect
        while (true) {
            val current = rotationAnim.value
            rotationAnim.animateTo(
                targetValue = current + 360f,
                animationSpec = tween(durationMillis = 28000, easing = LinearEasing),
            )
        }
    }

    // Gentle ambient pulse only while actively playing to stay lightweight when idle
    val pulse = if (isPlaying) {
        val infinite = rememberInfiniteTransition(label = "standbyPulse")
        val anim by infinite.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(3800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulse",
        )
        anim
    } else {
        1f
    }

    var showControls by remember { mutableStateOf(true) }
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000L)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(920f)
            .background(Color(0xFF060609))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { showControls = !showControls },
    ) {
        // Blurred Ambient Album Cover Layer (cached offscreen so clock ticks don't re-blur)
        if (song?.thumbnailUrl != null) {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                        alpha = 0.34f
                    }
                    .blur(28.dp),
            )
        }

        // Rich Dual-Tone Ambient Radial Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            vibrant.copy(alpha = 0.28f),
                            dominant.copy(alpha = 0.16f),
                            Color(0xFF060609).copy(alpha = 0.92f),
                        ),
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = 36.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // Left Pane: Expressive Rotating Artwork
            Box(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(216.dp)
                        .graphicsLayer {
                            val s = if (isPlaying) pulse else 1f
                            scaleX = s
                            scaleY = s
                            if (isRotational) rotationZ = rotationAnim.value
                        }
                        .clip(albumShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.18f), albumShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (song?.thumbnailUrl != null) {
                        AsyncImage(
                            model = song.thumbnailUrl,
                            contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    if (isRotational) {
                                        rotationZ = -rotationAnim.value
                                        scaleX = 1.40f
                                        scaleY = 1.40f
                                    }
                                },
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp),
                        )
                    }
                }
            }

            // Right Pane: Large Apple StandBy Clock + Song Info + Transport
            Column(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = dateFormat.format(dateObj).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 13.sp,
                    ),
                    color = Color.White.copy(alpha = 0.65f),
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = timeFormat.format(dateObj),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 86.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-2).sp,
                        ),
                        color = Color.White,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = secFormat.format(dateObj),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                        ),
                        color = vibrant.copy(alpha = 0.90f),
                        modifier = Modifier.padding(bottom = 14.dp),
                    )
                }

                Spacer(Modifier.height(14.dp))

                if (song != null) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(12.dp))

                    val progress = if (durationMs > 0L) {
                        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        color = vibrant,
                        trackColor = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                    )
                } else {
                    Text(
                        text = "Hush StandBy · Ready to Play",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                }

                AnimatedVisibility(
                    visible = showControls && song != null,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(240)),
                ) {
                    Row(
                        modifier = Modifier.padding(top = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StandByCircleControl(Icons.Rounded.SkipPrevious, "Previous", onPrevious)
                        StandByCircleControl(
                            icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            label = if (isPlaying) "Pause" else "Play",
                            filled = true,
                            accent = vibrant,
                            onClick = onPlayPause,
                        )
                        StandByCircleControl(Icons.Rounded.SkipNext, "Next", onNext)
                    }
                }
            }
        }

        // Top-right Exit StandBy Pill (shown on tap or initially)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(240)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 18.dp, end = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                    .clickable(onClick = onExit)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Exit Display Mode",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Exit Display",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun StandByCircleControl(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    filled: Boolean = false,
    accent: Color = Color.White,
) {
    Box(
        modifier = Modifier
            .size(if (filled) 52.dp else 44.dp)
            .clip(CircleShape)
            .background(if (filled) accent else Color.White.copy(alpha = 0.14f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(if (filled) 26.dp else 22.dp),
        )
    }
}
