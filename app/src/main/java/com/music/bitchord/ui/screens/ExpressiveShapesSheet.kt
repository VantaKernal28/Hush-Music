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
import com.music.bitchord.ui.components.expressive.M3EShapes
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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import com.music.bitchord.ui.components.expressive.m3eBreathing
import com.music.bitchord.ui.components.expressive.m3eRotating
import androidx.compose.ui.unit.sp

private val AVAILABLE_SHAPES = listOf(
    "8-Leaf Clover" to "8-lobed organic clover rosette",
    "Flower" to "12-lobed floral scalloped silhouette",
    "Sunny" to "16-point radiant sun blossom",
    "4-Sided Cookie" to "Organic four-leaf cookie clover",
    "Puffy" to "Asymmetric soft organic blob",
    "Gem" to "Faceted rounded diamond gem",
    "Heart" to "Expressive rounded heart silhouette",
    "Pill" to "Smooth 50% stadium pill capsule",
    "Squircle" to "Continuous smooth curved squircle",
    "Starburst" to "6-pointed dynamic starburst flare",
    "Architectural Arch" to "Rounded arch with flat base",
    "Hexagon" to "Geometric rounded 6-sided polygon",
    "Circle" to "Classic circular geometry",
)

/**
 * Dedicated Expressive Shapes Settings Screen:
 * - Large Live Animated Preview Card showing real-time rotation, breathing, and shape updates
 * - Master toggle for Material 3 Expressive Shapes
 * - Shape Presets: Default, Friendly, Cheerful + Randomize All Shapes
 * - Interactive quick shape strip + per-component target pickers
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
    val playerArtShape by AppSettings.expressivePlayerArtShape.collectAsStateWithLifecycle()
    val artistArtShape by AppSettings.expressiveArtistArtShape.collectAsStateWithLifecycle()
    val controlsShape by AppSettings.expressiveControlsShape.collectAsStateWithLifecycle()
    val miniPlayerShape by AppSettings.expressiveMiniPlayerShape.collectAsStateWithLifecycle()

    var activeTarget by remember { mutableStateOf("PLAYER_ART") }
    var pickerTarget by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val currentPreviewShapeName = when (activeTarget) {
        "ALBUM_ART" -> albumArtShape
        "PLAYER_ART" -> playerArtShape
        "ARTIST_ART" -> artistArtShape
        "CONTROLS" -> controlsShape
        "MINI_PLAYER" -> miniPlayerShape
        else -> playerArtShape
    }
    val currentTargetLabel = when (activeTarget) {
        "ALBUM_ART" -> "Album Artwork"
        "PLAYER_ART" -> "Player Artwork"
        "ARTIST_ART" -> "Artist Artwork"
        "CONTROLS" -> "Player Controls"
        "MINI_PLAYER" -> "Mini Player Dock"
        else -> "Player Artwork"
    }

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

            // Live Animated Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "LIVE PREVIEW · ${currentTargetLabel.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = currentPreviewShapeName,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    AnimatedContent(
                        targetState = currentPreviewShapeName,
                        transitionSpec = {
                            (fadeIn() + scaleIn(initialScale = 0.82f, animationSpec = spring(dampingRatio = 0.68f, stiffness = 360f)))
                                .togetherWith(fadeOut() + scaleOut(targetScale = 0.85f))
                        },
                        label = "shapeLivePreview",
                    ) { shapeName ->
                        val previewShape = M3EShapes.fromName(shapeName)
                        val isRot = expressiveUiEnabled && M3EShapes.isRotationalName(shapeName)
                        Box(
                            modifier = Modifier
                                .size(156.dp)
                                .m3eRotating(enabled = isRot, durationMs = 16000)
                                .m3eBreathing(enabled = expressiveUiEnabled)
                                .clip(previewShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary,
                                            MaterialTheme.colorScheme.secondary,
                                        ),
                                    ),
                                )
                                .border(2.dp, Color.White.copy(alpha = 0.25f), previewShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = shapeName,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(52.dp)
                                    .graphicsLayer { alpha = 0.92f },
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Quick inline shape strip so tapping any shape updates live preview & selected target immediately
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AVAILABLE_SHAPES.forEach { (name, _) ->
                            val isSelected = currentPreviewShapeName == name
                            val chipShape = M3EShapes.fromName(name)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else MaterialTheme.colorScheme.surfaceContainer,
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.06f),
                                        shape = RoundedCornerShape(16.dp),
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        when (activeTarget) {
                                            "ALBUM_ART" -> AppSettings.setExpressiveAlbumArtShape(name)
                                            "PLAYER_ART" -> AppSettings.setExpressivePlayerArtShape(name)
                                            "ARTIST_ART" -> AppSettings.setExpressiveArtistArtShape(name)
                                            "CONTROLS" -> AppSettings.setExpressiveControlsShape(name)
                                            "MINI_PLAYER" -> AppSettings.setExpressiveMiniPlayerShape(name)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(chipShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                        ),
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = name.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Master Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                                    AppSettings.setExpressiveAlbumArtShape("8-Leaf Clover")
                                    AppSettings.setExpressivePlayerArtShape("8-Leaf Clover")
                                    AppSettings.setExpressiveArtistArtShape("Heart")
                                    AppSettings.setExpressiveControlsShape("Flower")
                                    AppSettings.setExpressiveMiniPlayerShape("Pill")
                                }
                                "Friendly" -> {
                                    AppSettings.setExpressiveAlbumArtShape("4-Sided Cookie")
                                    AppSettings.setExpressivePlayerArtShape("4-Sided Cookie")
                                    AppSettings.setExpressiveArtistArtShape("Puffy")
                                    AppSettings.setExpressiveControlsShape("Squircle")
                                    AppSettings.setExpressiveMiniPlayerShape("Squircle")
                                }
                                "Cheerful" -> {
                                    AppSettings.setExpressiveAlbumArtShape("Sunny")
                                    AppSettings.setExpressivePlayerArtShape("Sunny")
                                    AppSettings.setExpressiveArtistArtShape("Heart")
                                    AppSettings.setExpressiveControlsShape("Starburst")
                                    AppSettings.setExpressiveMiniPlayerShape("Pill")
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
                                AppSettings.setExpressiveAlbumArtShape(AVAILABLE_SHAPES.random().first)
                                AppSettings.setExpressivePlayerArtShape(AVAILABLE_SHAPES.random().first)
                                AppSettings.setExpressiveArtistArtShape(listOf("Heart", "Puffy", "Gem", "Circle", "8-Leaf Clover").random())
                                AppSettings.setExpressiveControlsShape(listOf("Flower", "Sunny", "4-Sided Cookie", "Squircle", "Pill").random())
                                AppSettings.setExpressiveMiniPlayerShape(listOf("Pill", "Squircle", "Architectural Arch").random())
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
                text = "Element Shapes (Tap to Customize & Preview)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            ) {
                Column(Modifier.fillMaxWidth()) {
                    ShapeTargetRow(
                        title = "Player Artwork",
                        currentShape = playerArtShape,
                        onClick = {
                            activeTarget = "PLAYER_ART"
                            pickerTarget = "PLAYER_ART"
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ShapeTargetRow(
                        title = "Album Artwork",
                        currentShape = albumArtShape,
                        onClick = {
                            activeTarget = "ALBUM_ART"
                            pickerTarget = "ALBUM_ART"
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ShapeTargetRow(
                        title = "Artist Artwork",
                        currentShape = artistArtShape,
                        onClick = {
                            activeTarget = "ARTIST_ART"
                            pickerTarget = "ARTIST_ART"
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ShapeTargetRow(
                        title = "Player Controls",
                        currentShape = controlsShape,
                        onClick = {
                            activeTarget = "CONTROLS"
                            pickerTarget = "CONTROLS"
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ShapeTargetRow(
                        title = "Mini Player Dock",
                        currentShape = miniPlayerShape,
                        onClick = {
                            activeTarget = "MINI_PLAYER"
                            pickerTarget = "MINI_PLAYER"
                        },
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
                        .verticalScroll(rememberScrollState())
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
                            "ALBUM_ART" -> albumArtShape == name
                            "PLAYER_ART" -> playerArtShape == name
                            "ARTIST_ART" -> artistArtShape == name
                            "CONTROLS" -> controlsShape == name
                            "MINI_PLAYER" -> miniPlayerShape == name
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
                                        "ALBUM_ART" -> AppSettings.setExpressiveAlbumArtShape(name)
                                        "PLAYER_ART" -> AppSettings.setExpressivePlayerArtShape(name)
                                        "ARTIST_ART" -> AppSettings.setExpressiveArtistArtShape(name)
                                        "CONTROLS" -> AppSettings.setExpressiveControlsShape(name)
                                        "MINI_PLAYER" -> AppSettings.setExpressiveMiniPlayerShape(name)
                                    }
                                    scope.launch {
                                        sheetState.hide()
                                        pickerTarget = null
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val shape = M3EShapes.fromName(name)
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(shape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.4f)),
                                )
                            }
                            Spacer(Modifier.width(14.dp))
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
