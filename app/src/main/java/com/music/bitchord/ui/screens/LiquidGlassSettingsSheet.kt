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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF18181E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    // Vibrant background gradient simulating wallpaper/artwork beneath glass
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF6366F1).copy(alpha = 0.85f * glassVibrancy),
                                        Color(0xFFA855F7).copy(alpha = 0.90f * glassVibrancy),
                                        Color(0xFFEC4899).copy(alpha = 0.85f * glassVibrancy),
                                        Color(0xFF3B82F6).copy(alpha = 0.80f * glassVibrancy),
                                    )
                                )
                            ),
                    )

                    // Floating Liquid Glass Lens Pill (live optical refraction preview)
                    val effectiveBlur = (glassBlurPercentage * 24f).coerceIn(4f, 32f).dp
                    val glassSurfaceColor = if (isDark) {
                        Color.White.copy(alpha = (glassTransparency * 0.28f).coerceIn(0.06f, 0.45f))
                    } else {
                        Color.White.copy(alpha = (glassTransparency * 0.45f).coerceIn(0.15f, 0.65f))
                    }
                    val borderAlpha = (glassHighlightOpacity * 0.35f).coerceIn(0.08f, 0.50f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(glassSurfaceColor)
                            .blur(effectiveBlur)
                            .border(1.5.dp, Color.White.copy(alpha = borderAlpha), RoundedCornerShape(32.dp)),
                    )

                    // Sharp inner text over the blurred glass
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Liquid Glass Engine",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            ),
                            color = Color.White,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Refraction: ${(glassLensRefractionAmount * 100).roundToInt()}% · Blur: ${(glassBlurPercentage * 100).roundToInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = Color.White.copy(alpha = 0.85f),
                        )
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
                shape = RoundedCornerShape(20.dp),
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
                shape = RoundedCornerShape(20.dp),
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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF18181E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsRow(
                        icon = SfIcons.PlayFill,
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

            Spacer(Modifier.height(32.dp))
        }
    }
}
