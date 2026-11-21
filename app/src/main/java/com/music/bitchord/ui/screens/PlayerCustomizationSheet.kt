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
                        subtitle = "Sample active album art tones to animate ambient player backdrop",
                        checked = liveSongColorGradient,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            AppSettings.setLiveSongColorGradient(it)
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    PlayerToggleRow(
                        title = "Wavy Progress Scrubber",
                        subtitle = "Kinetic fluid waveform scrubber on full screen player seekbar",
                        checked = wavyScrubber,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            AppSettings.setWavyScrubber(it)
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    PlayerToggleRow(
                        title = "Synced Lyrics",
                        subtitle = "Full-bleed kinetic typography lyrics panel with word-level tracking",
                        checked = syncedLyrics,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            AppSettings.setSyncedLyrics(it)
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    PlayerToggleRow(
                        title = "Full Bleed Artwork",
                        subtitle = "Extend high-resolution album artwork behind controls",
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
                        subtitle = "Swipe horizontally on player card to skip tracks",
                        checked = swipeToPlayNext,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            AppSettings.setSwipeToPlayNext(it)
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    PlayerToggleRow(
                        title = "Hide Volume Bar in Player",
                        subtitle = "Hide the on-screen volume slider to emphasize artwork",
                        checked = hideVolumeBar,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            AppSettings.setHideVolumeBar(it)
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    PlayerToggleRow(
                        title = "Hide Status Bar in Player",
                        subtitle = "Immersive edge-to-edge experience in full screen player",
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
    subtitle: String,
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
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
