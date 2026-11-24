package com.music.bitchord.ui.components.expressive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.components.BottomTab
import com.music.bitchord.ui.components.NAV_BAR_GUTTER
import com.music.bitchord.ui.components.NAV_BAR_LIFT
import com.music.bitchord.ui.components.PILL_INSET
import com.music.bitchord.ui.components.floatingBarInsets
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.hazeEffect

/**
 * Material 3 Expressive (M3E) Navigation Bar.
 *
 * Visual & Motion Craft:
 * - Floating tactile stadium pill with bouncy spring animated indicator.
 * - Indicator travels between tabs using [ExpressiveMotion.SpatialSpring] with natural inertia.
 * - Individual tab press squish & overshoot recoil using [expressiveClickable] and tactile haptics.
 * - Bold chunky icon and label hierarchy matching M3E standards.
 */
@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalFoundationApi::class)
@Composable
fun ExpressiveNavBar(
    tabs: List<BottomTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    onTabLongClick: ((Int) -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val materialUi by AppSettings.materialUiMode.collectAsStateWithLifecycle()
    val frostedGlass by AppSettings.frostedGlass.collectAsStateWithLifecycle()
    val pillShape = remember { RoundedCornerShape(percent = 50) }

    if (materialUi && tabs.size >= 4) {
        val coreTabs = remember(tabs) { tabs.take(3) }
        val searchTab = tabs.getOrNull(3)
        val circleShape = remember { CircleShape }

        Row(
            modifier = modifier
                .windowInsetsPadding(floatingBarInsets)
                .padding(horizontal = NAV_BAR_GUTTER)
                .padding(bottom = NAV_BAR_LIFT)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Main tabs pill (Home, Explore, Library)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .shadow(elevation = 8.dp, shape = pillShape, ambientColor = Color.Black.copy(alpha = 0.25f))
                    .clip(pillShape)
                    .then(
                        if (frostedGlass && hazeState != null) {
                            Modifier
                                .hazeEffect(
                                    state = hazeState,
                                    style = HazeMaterials.regular(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f)),
                                )
                                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
                        } else {
                            Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        shape = pillShape,
                    )
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    coreTabs.forEachIndexed { index, tab ->
                        val isSelected = index == selectedIndex
                        val interactionSource = remember { MutableInteractionSource() }

                        Box(
                            modifier = Modifier
                                .clip(pillShape)
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    } else {
                                        Modifier
                                            .clickable(
                                                interactionSource = interactionSource,
                                                indication = null,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onTabSelected(index)
                                                },
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    }
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(24.dp),
                                )
                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = fadeIn(spring(stiffness = 500f)) + expandHorizontally(spring(stiffness = 500f)),
                                    exit = fadeOut(spring(stiffness = 500f)) + shrinkHorizontally(spring(stiffness = 500f)),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Detached Circular Search Button (56dp)
            if (searchTab != null) {
                val isSearchSelected = selectedIndex == 3
                val searchInteraction = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(elevation = 8.dp, shape = circleShape, ambientColor = Color.Black.copy(alpha = 0.25f))
                        .clip(circleShape)
                        .then(
                            if (isSearchSelected) {
                                Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
                            } else if (frostedGlass && hazeState != null) {
                                Modifier
                                    .hazeEffect(
                                        state = hazeState,
                                        style = HazeMaterials.regular(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f)),
                                    )
                                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
                            } else {
                                Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            shape = circleShape,
                        )
                        .clickable(
                            interactionSource = searchInteraction,
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTabSelected(3)
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = searchTab.icon,
                        contentDescription = searchTab.label,
                        tint = if (isSearchSelected) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
        return
    }

    val tabCount = tabs.size

    var rowSize by remember { mutableStateOf(IntSize.Zero) }
    val gapPx = with(density) { 6.dp.toPx() }

    val tabWidthPx = if (rowSize.width > 0 && tabCount > 0) {
        (rowSize.width - gapPx * (tabCount - 1)) / tabCount
    } else 0f
    val tabStepPx = if (rowSize.width > 0 && tabCount > 0) {
        (rowSize.width + gapPx) / tabCount
    } else 0f

    val indicatorOffsetPx by animateFloatAsState(
        targetValue = selectedIndex * tabStepPx,
        animationSpec = ExpressiveMotion.SpatialSpring,
        label = "m3eIndicatorOffset",
    )

    Box(
        modifier = modifier
            .windowInsetsPadding(floatingBarInsets)
            .padding(horizontal = NAV_BAR_GUTTER)
            .padding(bottom = NAV_BAR_LIFT)
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = pillShape, ambientColor = Color.Black.copy(alpha = 0.25f))
            .clip(pillShape)
            .then(
                if (materialUi || hazeState == null) {
                    if (frostedGlass && hazeState != null) {
                        Modifier
                            .hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.regular(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f)),
                            )
                            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                    }
                } else {
                    Modifier.hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.regular(MaterialTheme.colorScheme.surfaceContainerHigh),
                    )
                },
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = pillShape,
            )
            .padding(horizontal = PILL_INSET, vertical = PILL_INSET),
    ) {
        // Sliding Active Indicator Pill
        if (tabWidthPx > 0f) {
            Box(
                modifier = Modifier
                    .width(with(density) { tabWidthPx.toDp() })
                    .height(with(density) { rowSize.height.toDp() })
                    .graphicsLayer { translationX = indicatorOffsetPx }
                    .clip(pillShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            )
        }

        // Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { rowSize = it },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                val tabScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.88f else 1.0f,
                    animationSpec = ExpressiveMotion.TactilePressSpring,
                    label = "tabScale_$index",
                )

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                    },
                    animationSpec = spring(stiffness = 500f),
                    label = "tabColor_$index",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(pillShape)
                        .combinedClickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                if (!isSelected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onTabSelected(index)
                                }
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTabLongClick?.invoke(index)
                            },
                        )
                        .graphicsLayer {
                            scaleX = tabScale
                            scaleY = tabScale
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                            ),
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
