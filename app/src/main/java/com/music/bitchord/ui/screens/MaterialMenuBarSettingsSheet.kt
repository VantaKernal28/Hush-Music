package com.music.bitchord.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.components.expressive.M3EShapes
import com.music.bitchord.ui.icons.SfIcons
import kotlin.math.roundToInt

private val MENU_BAR_SHAPES = listOf(
    "Pill" to "Classic floating rounded stadium capsule",
    "Rounded Stadium" to "Full 50% border radius stadium dock",
    "Squircle Dock" to "Smooth continuous curvature squircle",
    "Expressive Clover-Edge" to "Organic scalloped clover-edged container",
)

/**
 * Dedicated Material UI Menu Bar Settings Screen:
 * - Live interactive M3E Menu Bar preview banner at top reflecting geometry & optics in real time
 * - Shape Selector: Pill, Rounded Stadium, Squircle Dock, Expressive Clover-Edge
 * - Corner Radius slider (12dp–40dp)
 * - Dimension sliders: Width (70%–100%), Height (52dp–80dp), Padding (4dp–18dp)
 * - Surface & Optics sliders: Transparency (0%–100%), Frosted Blur (0%–100%)
 */
@Composable
fun MaterialMenuBarSettingsSheet(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val navBarShape by AppSettings.m3eNavBarShape.collectAsStateWithLifecycle()
    val navBarCornerRadius by AppSettings.m3eNavBarCornerRadius.collectAsStateWithLifecycle()
    val navBarWidth by AppSettings.m3eNavBarWidth.collectAsStateWithLifecycle()
    val navBarHeight by AppSettings.m3eNavBarHeight.collectAsStateWithLifecycle()
    val navBarPadding by AppSettings.m3eNavBarPadding.collectAsStateWithLifecycle()
    val navBarTransparency by AppSettings.m3eNavBarTransparency.collectAsStateWithLifecycle()
    val navBarFrostedBlur by AppSettings.m3eNavBarFrostedBlur.collectAsStateWithLifecycle()

    val isDark = MaterialTheme.colorScheme.surface.let {
        (0.299f * it.red + 0.587f * it.green + 0.114f * it.blue) < 0.5f
    }

    val previewShape = when (navBarShape) {
        "Pill", "Rounded Stadium" -> RoundedCornerShape(percent = 50)
        "Squircle Dock" -> RoundedCornerShape(navBarCornerRadius.dp)
        "Expressive Clover-Edge" -> M3EShapes.ScallopedClover
        else -> RoundedCornerShape(navBarCornerRadius.dp)
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
                    text = "Material Menu Bar",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Top Live Interactive M3E Menu Bar Preview Banner
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = navBarPadding.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    // Simulated home cards background to show frosted blur effect
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(70.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(70.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                            )
                        }
                    }

                    // Live M3E Menu Bar Preview
                    val barSurface = if (isDark) {
                        Color(0xFF222228).copy(alpha = navBarTransparency.coerceIn(0.2f, 1f))
                    } else {
                        Color(0xFFF0EFF4).copy(alpha = navBarTransparency.coerceIn(0.2f, 1f))
                    }
                    val effectiveBlur = (navBarFrostedBlur * 28f).coerceIn(0f, 32f).dp

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(navBarWidth)
                            .height(navBarHeight.dp)
                            .clip(previewShape)
                            .background(barSurface)
                            .blur(effectiveBlur)
                            .border(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                                previewShape,
                            ),
                    )

                    // Sharp inner tab icons over the blurred dock
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(navBarWidth)
                            .height(navBarHeight.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Home tab (active)
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .width(56.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Home,
                                contentDescription = "Home",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        // Search tab
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp),
                        )

                        // Library tab
                        Icon(
                            imageVector = Icons.Rounded.LibraryMusic,
                            contentDescription = "Library",
                            tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // Section 1: SHAPE & GEOMETRY
            Text(
                text = "SHAPE & GEOMETRY",
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
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Menu Bar Shape",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MENU_BAR_SHAPES.forEach { (name, _) ->
                            val isSelected = navBarShape == name
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else if (isDark) Color.White.copy(alpha = 0.08f)
                                        else Color.Black.copy(alpha = 0.06f)
                                    )
                                    .clickable { AppSettings.setM3eNavBarShape(name) }
                                    .padding(horizontal = 16.dp, vertical = 9.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    SliderRow(
                        icon = SfIcons.Sliders,
                        title = "Corner Radius",
                        value = "$navBarCornerRadius dp",
                        sliderValue = navBarCornerRadius.toFloat(),
                        onSliderValue = { AppSettings.setM3eNavBarCornerRadius(it.roundToInt()) },
                        valueRange = 12f..40f,
                        steps = 28,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Section 2: DIMENSIONS
            Text(
                text = "DIMENSIONS",
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
                        icon = SfIcons.Tune,
                        title = "Menu Bar Width",
                        value = "${(navBarWidth * 100).roundToInt()}%",
                        sliderValue = navBarWidth,
                        onSliderValue = AppSettings::setM3eNavBarWidth,
                        valueRange = 0.70f..1.0f,
                        steps = 30,
                    )
                    SliderRow(
                        icon = SfIcons.Sliders,
                        title = "Menu Bar Height",
                        value = "$navBarHeight dp",
                        sliderValue = navBarHeight.toFloat(),
                        onSliderValue = { AppSettings.setM3eNavBarHeight(it.roundToInt()) },
                        valueRange = 52f..80f,
                        steps = 28,
                    )
                    SliderRow(
                        icon = SfIcons.Apps,
                        title = "Menu Bar Bottom Padding",
                        value = "$navBarPadding dp",
                        sliderValue = navBarPadding.toFloat(),
                        onSliderValue = { AppSettings.setM3eNavBarPadding(it.roundToInt()) },
                        valueRange = 4f..18f,
                        steps = 14,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Section 3: SURFACE & OPTICS
            Text(
                text = "SURFACE & OPTICS",
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
                        title = "Surface Transparency",
                        value = "${(navBarTransparency * 100).roundToInt()}%",
                        sliderValue = navBarTransparency,
                        onSliderValue = AppSettings::setM3eNavBarTransparency,
                        valueRange = 0f..1f,
                        steps = 20,
                    )
                    SliderRow(
                        icon = SfIcons.BlurOn,
                        title = "Frosted Blur Intensity",
                        value = "${(navBarFrostedBlur * 100).roundToInt()}%",
                        sliderValue = navBarFrostedBlur,
                        onSliderValue = {
                            AppSettings.setM3eNavBarFrostedBlur(it)
                            AppSettings.setM3eFrostedGlassIntensity(it)
                        },
                        valueRange = 0f..1f,
                        steps = 20,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
