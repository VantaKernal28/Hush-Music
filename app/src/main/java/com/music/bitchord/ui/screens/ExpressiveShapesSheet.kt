/*
 * Ported from cromaguy/Rhythm (GPL-3.0)
 * Material 3 Expressive Shapes Customization Screen (video_2.mp4 & video_3.mp4).
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.music.bitchord.ui.components.PillSegmentedControl
import kotlinx.coroutines.launch

private val AVAILABLE_SHAPES = listOf(
    "Wavy Octagon" to "8-lobed organic flower rosette",
    "Squircle" to "Continuous smooth curved squircle",
    "4-Lobe Clover" to "Organic four-leaf clover",
    "12-Lobe Scallop" to "Dense scalloped perimeter",
    "Stadium Pill" to "Smooth 50% capsule shape",
    "Architectural Arch" to "Rounded top with stable flat base",
    "Diamond" to "Diagonal rounded lozenge",
    "Hexagon" to "Geometric rounded 6-sided polygon",
    "Starburst" to "6-pointed starburst flare",
    "Sunny Radiance" to "16-point radiant blossom",
    "Heart" to "Organic heart shape for artists",
    "Circle" to "Classic circular geometry",
)

/**
 * Dedicated Expressive Shapes Settings Screen reproducing cromaguy/Rhythm exactly (video_2.mp4):
 * - Master toggle for Material 3 Expressive Shapes
 * - Shape Presets: Default, Friendly, Cheerful
 * - Randomize All Shapes button
 * - Per-component target shape pickers (Album Art, Player Art, Song Art, Playlist Art, Artist Art, Controls)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveShapesSheet(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val haptic = LocalHapticFeedback.current
    val expressiveUiEnabled by AppSettings.expressiveUiEnabled.collectAsStateWithLifecycle()
    val shapePreset by AppSettings.expressiveShapePreset.collectAsStateWithLifecycle()
    val albumArtShape by AppSettings.expressiveAlbumArtShape.collectAsStateWithLifecycle()
    val artistArtShape by AppSettings.expressiveArtistArtShape.collectAsStateWithLifecycle()

    var pickerTarget by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

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
                    text = "Expressive Shapes",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(Modifier.height(10.dp))

            // Master Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Draw,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Material 3 Expressive Shapes",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Switch(
                        checked = expressiveUiEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            AppSettings.setExpressiveUiEnabled(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Shape Presets
            Text(
                text = "Shape Presets",
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    val presets = listOf("Default", "Friendly", "Cheerful")
                    PillSegmentedControl(
                        options = presets,
                        selectedIndex = presets.indexOf(shapePreset).coerceAtLeast(0),
                        onSelect = { idx ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val selected = presets[idx]
                            AppSettings.setExpressiveShapePreset(selected)
                            when (selected) {
                                "Default" -> {
                                    AppSettings.setExpressiveAlbumArtShape("Wavy Octagon")
                                    AppSettings.setExpressiveArtistArtShape("Heart")
                                }
                                "Friendly" -> {
                                    AppSettings.setExpressiveAlbumArtShape("Squircle")
                                    AppSettings.setExpressiveArtistArtShape("Squircle")
                                }
                                "Cheerful" -> {
                                    AppSettings.setExpressiveAlbumArtShape("Sunny Radiance")
                                    AppSettings.setExpressiveArtistArtShape("Heart")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val randomAlbum = AVAILABLE_SHAPES.random().first
                                val randomArtist = listOf("Heart", "Squircle", "Circle", "Wavy Octagon").random()
                                AppSettings.setExpressiveAlbumArtShape(randomAlbum)
                                AppSettings.setExpressiveArtistArtShape(randomArtist)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Casino,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Randomize All Shapes",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Per-Target Shapes
            Text(
                text = "Element Shapes",
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
                    ShapeTargetRow(
                        title = "Album Artwork",
                        currentShape = albumArtShape,
                        onClick = { pickerTarget = "ALBUM_ART" },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ShapeTargetRow(
                        title = "Player Artwork",
                        currentShape = albumArtShape,
                        onClick = { pickerTarget = "PLAYER_ART" },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ShapeTargetRow(
                        title = "Artist Artwork",
                        currentShape = artistArtShape,
                        onClick = { pickerTarget = "ARTIST_ART" },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ShapeTargetRow(
                        title = "Player Controls",
                        currentShape = "9-Lobe Scallop",
                        onClick = { pickerTarget = "CONTROLS" },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ShapeTargetRow(
                        title = "Mini Player Dock",
                        currentShape = "M3E Rounded 24dp",
                        onClick = { pickerTarget = "MINI_PLAYER" },
                    )
                }
            }

            Spacer(Modifier.height(36.dp))
        }

        // Shape Picker Bottom Sheet
        if (pickerTarget != null) {
            ModalBottomSheet(
                onDismissRequest = { pickerTarget = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .padding(bottom = 24.dp),
                ) {
                    Text(
                        text = "Choose Shape",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(14.dp))

                    AVAILABLE_SHAPES.forEach { (name, desc) ->
                        val isSelected = when (pickerTarget) {
                            "ALBUM_ART", "PLAYER_ART" -> albumArtShape == name
                            "ARTIST_ART" -> artistArtShape == name
                            else -> false
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else Color.Transparent,
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    when (pickerTarget) {
                                        "ALBUM_ART", "PLAYER_ART" -> AppSettings.setExpressiveAlbumArtShape(name)
                                        "ARTIST_ART" -> AppSettings.setExpressiveArtistArtShape(name)
                                    }
                                    scope.launch {
                                        sheetState.hide()
                                        pickerTarget = null
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShapeTargetRow(
    title: String,
    currentShape: String,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = currentShape,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
