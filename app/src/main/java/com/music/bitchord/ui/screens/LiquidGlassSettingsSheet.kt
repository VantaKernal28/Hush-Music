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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.components.IosSwitch
import com.music.bitchord.ui.icons.SfIcons
import kotlin.math.roundToInt

/**
 * Dedicated Liquid Glass Customization Screen (video_2.mp4 00:00–00:04):
 * - Live interactive glass preview banner at top reflecting optical controls in real time
 * - Optical Effects: Vibrancy, Blur Radius, Lens Refraction Height, Lens Refraction Amount,
 *   Chromatic Aberration, Depth Effect
 * - Appearance: Surface Tint, Surface Opacity, Highlight Opacity, Adaptive Contrast
 * - Per Component: Glass Mini Player, Glass Navigation Bar
 */
@Composable
fun LiquidGlassSettingsSheet(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val glassTransparency by AppSettings.glassTransparency.collectAsStateWithLifecycle()
    val glassRefraction by AppSettings.glassRefraction.collectAsStateWithLifecycle()
    val glassBlurPercentage by AppSettings.glassBlurPercentage.collectAsStateWithLifecycle()
    val glassVibrancy by AppSettings.glassVibrancy.collectAsStateWithLifecycle()
    val glassLensRefractionHeight by AppSettings.glassLensRefractionHeight.collectAsStateWithLifecycle()
    val glassLensRefractionAmount by AppSettings.glassLensRefractionAmount.collectAsStateWithLifecycle()
    val glassChromaticAberration by AppSettings.glassChromaticAberration.collectAsStateWithLifecycle()
    val glassHighlightOpacity by AppSettings.glassHighlightOpacity.collectAsStateWithLifecycle()
    val glassAdaptiveContrast by AppSettings.glassAdaptiveContrast.collectAsStateWithLifecycle()
    val glassDepthEffect by AppSettings.glassDepthEffect.collectAsStateWithLifecycle()
    val glassMiniPlayerEnabled by AppSettings.glassMiniPlayerEnabled.collectAsStateWithLifecycle()
    val glassNavBarEnabled by AppSettings.glassNavBarEnabled.collectAsStateWithLifecycle()

    val isDark = MaterialTheme.colorScheme.surface.let {
        (0.299f * it.red + 0.587f * it.green + 0.114f * it.blue) < 0.5f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F0F12) else MaterialTheme.colorScheme.background),
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
                    text = "Liquid Glass",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Top Live Interactive Glass Preview Banner (video_2.mp4 00:00–00:04)
            var previewGlassTab by remember { mutableIntStateOf(0) }
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF18181E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    // Vibrant background gradient with layered geometric shapes simulating wallpaper/artwork beneath glass
                    val vibrancyFactor = (0.6f + 1.4f * glassVibrancy).coerceIn(0.6f, 2.0f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF6366F1).copy(alpha = (0.85f * vibrancyFactor).coerceIn(0.4f, 1f)),
                                        Color(0xFFA855F7).copy(alpha = (0.88f * vibrancyFactor).coerceIn(0.4f, 1f)),
                                        Color(0xFFEC4899).copy(alpha = (0.85f * vibrancyFactor).coerceIn(0.4f, 1f)),
                                        Color(0xFF3B82F6).copy(alpha = (0.80f * vibrancyFactor).coerceIn(0.4f, 1f)),
                                    )
                                )
                            ),
                    ) {
                        // Decorative visual contrast circles beneath glass
                        Box(
                            modifier = Modifier
                                .padding(start = 24.dp, top = 20.dp)
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFDE047).copy(alpha = 0.65f * vibrancyFactor.coerceAtMost(1f))),
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 28.dp, bottom = 16.dp)
                                .size(90.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF06B6D4).copy(alpha = 0.55f * vibrancyFactor.coerceAtMost(1f))),
                        )
                    }

                    // Floating Liquid Glass Lens Pill (live optical refraction preview)
                    val effectiveBlur = (glassBlurPercentage * 36f).coerceIn(0f, 36f).dp
                    val surfaceFillAlpha = (0.08f + 0.84f * glassTransparency).coerceIn(0.08f, 0.92f)
                    val glassSurfaceColor = if (isDark) {
                        Color(0xFF242426).copy(alpha = surfaceFillAlpha)
                    } else {
                        Color.White.copy(alpha = surfaceFillAlpha)
                    }
                    val borderAlpha = (glassHighlightOpacity * 0.85f).coerceIn(0f, 0.85f)
                    val lensScale = (0.85f + 0.15f * glassLensRefractionAmount).coerceIn(0.85f, 1.0f)
                    val pillHeight = (58f + 14f * glassLensRefractionHeight).dp
                    val pillShape = RoundedCornerShape(32.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.88f * lensScale)
                            .height(pillHeight)
                            .then(
                                if (glassDepthEffect > 0.05f) {
                                    Modifier.shadow(
                                        elevation = (12f * glassDepthEffect).dp,
                                        shape = pillShape,
                                        ambientColor = Color.Black.copy(alpha = 0.45f),
                                    )
                                } else Modifier
                            )
                            .clip(pillShape)
                            .background(glassSurfaceColor)
                            .blur(effectiveBlur)
                            .then(
                                if (glassChromaticAberration) {
                                    Modifier
                                        .border(2.dp, Color(0xFFFF2A85).copy(alpha = 0.35f), pillShape)
                                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.40f), pillShape)
                                } else Modifier
                            )
                            .border(1.5.dp, Color.White.copy(alpha = borderAlpha), pillShape),
                    )

                    // Interactive preview tab bar over the live liquid glass pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.88f * lensScale)
                            .height(pillHeight),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        listOf(
                            Triple(0, Icons.Rounded.Home, "Home"),
                            Triple(1, Icons.Rounded.Search, "Search"),
                            Triple(2, Icons.Rounded.LibraryMusic, "Library"),
                        ).forEach { (idx, icon, label) ->
                            val isSelected = previewGlassTab == idx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .then(
                                        if (isSelected) {
                                            Modifier
                                                .background(Color.White.copy(alpha = if (isDark) 0.22f else 0.45f))
                                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        } else {
                                            Modifier
                                                .clickable { previewGlassTab = idx }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        }
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
                                        modifier = Modifier.size(18.dp),
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                            ),
                                            color = Color.White,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // Section 1: EFFECTS
            Text(
                text = "EFFECTS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF18181E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SliderRow(
                        icon = SfIcons.AutoAwesome,
                        title = "Vibrancy",
                        value = "${(glassVibrancy * 100).roundToInt()}%",
                        sliderValue = glassVibrancy,
                        onSliderValue = AppSettings::setGlassVibrancy,
                        valueRange = 0f..1f,
                        steps = 20,
                    )
                    SliderRow(
                        icon = SfIcons.BlurOn,
                        title = "Blur Radius",
                        value = "${(glassBlurPercentage * 100).roundToInt()}%",
                        sliderValue = glassBlurPercentage,
                        onSliderValue = AppSettings::setGlassBlurPercentage,
                        valueRange = 0f..1f,
                        steps = 20,
                    )
                    SliderRow(
                        icon = SfIcons.Sliders,
                        title = "Lens Refraction Height",
                        value = "${(glassLensRefractionHeight * 100).roundToInt()}%",
                        sliderValue = glassLensRefractionHeight,
                        onSliderValue = AppSettings::setGlassLensRefractionHeight,
                        valueRange = 0f..1f,
                        steps = 20,
                    )
                    SliderRow(
                        icon = SfIcons.Tune,
                        title = "Lens Refraction Amount",
                        value = "${(glassLensRefractionAmount * 100).roundToInt()}%",
                        sliderValue = glassLensRefractionAmount,
                        onSliderValue = {
                            AppSettings.setGlassLensRefractionAmount(it)
                            AppSettings.setGlassRefraction(it)
                        },
                        valueRange = 0f..1f,
                        steps = 20,
                    )
                    SettingsRow(
                        icon = SfIcons.Gradient,
                        title = "Chromatic Aberration",
                        subtitle = "Simulate optical prism color dispersion",
                        trailing = {
                            IosSwitch(
                                checked = glassChromaticAberration,
                                onCheckedChange = AppSettings::setGlassChromaticAberration,
                            )
                        },
                        onClick = { AppSettings.setGlassChromaticAberration(!glassChromaticAberration) },
                    )
                    SettingsRow(
                        icon = SfIcons.Album,
                        title = "Depth Effect",
                        subtitle = "Multi-layer volumetric glass elevation",
                        trailing = {
                            IosSwitch(
                                checked = glassDepthEffect > 0.05f,
                                onCheckedChange = { AppSettings.setGlassDepthEffect(if (it) 0.65f else 0f) },
                            )
                        },
                        onClick = { AppSettings.setGlassDepthEffect(if (glassDepthEffect > 0.05f) 0f else 0.65f) },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Section 2: APPEARANCE
            Text(
                text = "APPEARANCE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF18181E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SliderRow(
                        icon = SfIcons.Gradient,
                        title = "Surface Opacity",
                        value = "${(glassTransparency * 100).roundToInt()}%",
                        sliderValue = glassTransparency,
                        onSliderValue = AppSettings::setGlassTransparency,
                        valueRange = 0f..1f,
                        steps = 20,
                    )
                    SliderRow(
                        icon = SfIcons.AutoAwesome,
                        title = "Highlight Opacity",
                        value = "${(glassHighlightOpacity * 100).roundToInt()}%",
                        sliderValue = glassHighlightOpacity,
                        onSliderValue = AppSettings::setGlassHighlightOpacity,
                        valueRange = 0f..1f,
                        steps = 20,
                    )
                    SettingsRow(
                        icon = SfIcons.Sliders,
                        title = "Adaptive Contrast",
                        subtitle = "Dynamically boost text contrast over light backgrounds",
                        trailing = {
                            IosSwitch(
                                checked = glassAdaptiveContrast,
                                onCheckedChange = AppSettings::setGlassAdaptiveContrast,
                            )
                        },
                        onClick = { AppSettings.setGlassAdaptiveContrast(!glassAdaptiveContrast) },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Section 3: PER COMPONENT
            Text(
                text = "PER COMPONENT",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF18181E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsRow(
                        icon = SfIcons.Play,
                        title = "Glass Mini Player",
                        subtitle = "Enable liquid glass materials on floating mini player",
                        trailing = {
                            IosSwitch(
                                checked = glassMiniPlayerEnabled,
                                onCheckedChange = AppSettings::setGlassMiniPlayerEnabled,
                            )
                        },
                        onClick = { AppSettings.setGlassMiniPlayerEnabled(!glassMiniPlayerEnabled) },
                    )
                    SettingsRow(
                        icon = SfIcons.Apps,
                        title = "Glass Navigation Bar",
                        subtitle = "Enable liquid glass materials on bottom dock navigation bar",
                        trailing = {
                            IosSwitch(
                                checked = glassNavBarEnabled,
                                onCheckedChange = AppSettings::setGlassNavBarEnabled,
                            )
                        },
                        onClick = { AppSettings.setGlassNavBarEnabled(!glassNavBarEnabled) },
                    )
                }
            }

            Spacer(Modifier.navigationBarsPadding().height(120.dp))
        }
    }
}
