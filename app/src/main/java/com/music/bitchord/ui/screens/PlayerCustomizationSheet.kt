/*
 * Ported from cromaguy/Rhythm (GPL-3.0)
 * Material 3 Expressive Player Customization Screen (video_2.mp4 & video_3.mp4).
 */

package com.music.bitchord.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.ProgressLineStyle

/**
 * Dedicated Player Customization Screen reproducing cromaguy/Rhythm exactly (video_2.mp4 & video_3.mp4):
 * - Live Song-Reactive Gradient toggle
 * - Wavy Seekbar scrubber toggle
 * - Synced Lyrics toggle
 * - Full Bleed Artwork toggle
 * - Player transport & gesture settings
 */
@Composable
fun PlayerCustomizationSheet(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val haptic = LocalHapticFeedback.current
    val liveSongColorGradient by AppSettings.liveSongColorGradient.collectAsStateWithLifecycle()
    val wavyScrubber by AppSettings.wavyScrubber.collectAsStateWithLifecycle()
    val progressLineStyle by AppSettings.progressLineStyle.collectAsStateWithLifecycle()
    val fullBleedArtwork by AppSettings.fullBleedArtwork.collectAsStateWithLifecycle()
    val syncedLyrics by AppSettings.syncedLyrics.collectAsStateWithLifecycle()
    val swipeToPlayNext by AppSettings.swipeToPlayNext.collectAsStateWithLifecycle()
    val hideVolumeBar by AppSettings.hideVolumeBar.collectAsStateWithLifecycle()
    val hideStatusBarInPlayer by AppSettings.hideStatusBarInPlayer.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(horizontal = 20.dp),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            CircleShape,
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Player",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(Modifier.height(10.dp))

            // Progress Line Style
            Text(
                text = "Progress Line Style",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProgressLineStyle.entries.forEach { style ->
                    val isSelected = progressLineStyle == style
                    val primary = MaterialTheme.colorScheme.primary
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) primary else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                AppSettings.setProgressLineStyle(style)
                            }
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ProgressLinePreviewBox(style = style, isSelected = isSelected, primary = primary)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = style.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // Appearance & Visuals
            Text(
                text = "Look & Visuals",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(Modifier.fillMaxWidth()) {
                    PlayerToggleRow(
                        title = "Live Song-Reactive Gradient",
                        checked = liveSongColorGradient,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            AppSettings.setLiveSongColorGradient(it)
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    PlayerToggleRow(
                        title = "Wavy Progress Scrubber",
                        checked = wavyScrubber,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            AppSettings.setWavyScrubber(it)
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    PlayerToggleRow(
                        title = "Synced Lyrics",
                        checked = syncedLyrics,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            AppSettings.setSyncedLyrics(it)
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    PlayerToggleRow(
                        title = "Full Bleed Artwork",
                        checked = fullBleedArtwork,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            AppSettings.setFullBleedArtwork(it)
                        },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Gestures & Layout
            Text(
                text = "Gestures & Display",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(Modifier.fillMaxWidth()) {
                    PlayerToggleRow(
                        title = "Swipe to Play Next",
                        checked = swipeToPlayNext,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            AppSettings.setSwipeToPlayNext(it)
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    PlayerToggleRow(
                        title = "Hide Volume Bar in Player",
                        checked = hideVolumeBar,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            AppSettings.setHideVolumeBar(it)
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    PlayerToggleRow(
                        title = "Hide Status Bar in Player",
                        checked = hideStatusBarInPlayer,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            AppSettings.setHideStatusBarInPlayer(it)
                        },
                    )
                }
            }

            Spacer(Modifier.height(36.dp))
        }
    }
}

@Composable
private fun PlayerToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

@Composable
private fun ProgressLinePreviewBox(
    style: ProgressLineStyle,
    isSelected: Boolean,
    primary: Color,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "previewAnim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) primary.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(24.dp).padding(horizontal = 4.dp)) {
            val w = size.width
            val h = size.height
            val midY = h / 2f
            val activeColor = primary
            val inactiveColor = Color.White.copy(alpha = 0.25f)
            val activeW = w * 0.65f

            when (style) {
                ProgressLineStyle.WAVY -> {
                    val p = Path()
                    val waveLength = w / 2.2f
                    val waveAmp = 4.dp.toPx()
                    p.moveTo(0f, midY)
                    var x = 0f
                    while (x <= activeW) {
                        val angle = (x / waveLength) * (2 * PI).toFloat() - phase
                        p.lineTo(x, midY + sin(angle) * waveAmp)
                        x += 2f
                    }
                    drawPath(p, activeColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                    drawLine(inactiveColor, Offset(activeW, midY), Offset(w, midY), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                    drawCircle(Color.White, radius = 3.5.dp.toPx(), center = Offset(activeW, midY))
                }
                ProgressLineStyle.STRAIGHT -> {
                    drawLine(inactiveColor, Offset(0f, midY), Offset(w, midY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(activeColor, Offset(0f, midY), Offset(activeW, midY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                    drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(activeW, midY))
                }
                ProgressLineStyle.DOTTED -> {
                    val count = 8
                    val gap = w / count
                    for (i in 0 until count) {
                        val dotX = i * gap + gap / 2f
                        val isDotActive = dotX <= activeW
                        drawCircle(
                            color = if (isDotActive) activeColor else inactiveColor,
                            radius = if (isDotActive) 2.5.dp.toPx() else 1.8.dp.toPx(),
                            center = Offset(dotX, midY),
                        )
                    }
                    drawCircle(Color.White, radius = 3.5.dp.toPx(), center = Offset(activeW, midY))
                }
                ProgressLineStyle.PULSE -> {
                    val p = Path()
                    p.moveTo(0f, midY)
                    var px = 0f
                    while (px <= activeW) {
                        val pulseFactor = (sin(px * 0.22f - phase * 1.5f) * cos(px * 0.12f)).toFloat()
                        p.lineTo(px, midY + pulseFactor * 5.dp.toPx())
                        px += 2f
                    }
                    drawPath(p, activeColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                    drawLine(inactiveColor, Offset(activeW, midY), Offset(w, midY), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                    drawCircle(Color.White, radius = 3.5.dp.toPx(), center = Offset(activeW, midY))
                }
            }
        }
    }
}
