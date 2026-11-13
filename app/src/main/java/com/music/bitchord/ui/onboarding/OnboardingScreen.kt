package com.music.bitchord.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.music.bitchord.R
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.DEFAULT_CACHE_LIMIT_BYTES
import com.music.bitchord.data.settings.UNLIMITED_CACHE_LIMIT_BYTES
import com.music.bitchord.sharedui.resources.Res
import com.music.bitchord.sharedui.resources.ic_google_g
import org.jetbrains.compose.resources.painterResource as composePainterResource
import com.music.bitchord.ui.components.IosSwitch
import com.music.bitchord.ui.icons.BitChordIcons
import com.music.bitchord.ui.player.MeshGradientBackground
import com.music.bitchord.ui.player.MESH_CYCLE_MILLIS
import com.music.bitchord.ui.player.MeshPalette
import com.music.bitchord.ui.player.cyclingMeshPalette
import com.music.bitchord.ui.screens.GoogleSignInScreen

/**
 * Hush Music — Multi-step First-Boot Onboarding Experience
 *
 * Modeled accurately after the user's inspiration flow:
 * 1. Step 1 of 4: "Welcome to Hush Music" with clean animated emblem reveal and name input.
 * 2. Step 2 of 4: "Tailor your sound" with tactile bouncy genre pills and theme accent selector.
 * 3. Step 3 of 4: "Never lose the rhythm" with Smart Downloads, Wi-Fi Only, and Storage Cap segmented control.
 * 4. Step 4 of 4: Dedicated Google / YouTube Music connection screen with kinetic floating cards.
 */
@Composable
fun OnboardingScreen(
    signedIn: Boolean,
    initialName: String,
    onNameChosen: (String) -> Unit,
    onSignIn: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var page by rememberSaveable { mutableIntStateOf(PAGE_WELCOME) }
    var name by rememberSaveable { mutableStateOf(initialName) }
    var selectedGenres by remember {
        mutableStateOf(AppSettings.selectedGenres.value.ifEmpty { setOf("Lo-Fi", "Electronic") })
    }
    var themeAccent by remember { mutableStateOf(AppSettings.themeAccent.value) }
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = page > PAGE_WELCOME) {
        page--
    }

    LaunchedEffect(signedIn, page) {
        if (signedIn && page == PAGE_GOOGLE) onFinish()
    }

    val palette = cyclingMeshPalette(
        when (page) {
            PAGE_WELCOME -> SunsetMeshPalettes
            PAGE_SOUND -> SoundMeshPalettes
            PAGE_CACHING -> CachingMeshPalettes
            else -> GoogleMeshPalettes
        }
    )

    Box(
        modifier
            .fillMaxSize()
            .background(Color(0xFF090A0D))
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
    ) {
        MeshGradientBackground(
            palette = palette,
            trackKey = page,
            continuous = true,
            driftMillis = 7_000,
            colorFadeMillis = MESH_CYCLE_MILLIS,
        )

        // Gradient vignette for dark AMOLED contrast
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xFF090A0D).copy(alpha = 0.35f),
                        0.5f to Color(0xFF090A0D).copy(alpha = 0.20f),
                        1f to Color(0xFF090A0D).copy(alpha = 0.85f),
                    ),
                ),
        )

        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
        ) {
            if (page < PAGE_GOOGLE) {
                StepProgressHeader(
                    currentPage = page,
                    totalPages = 4,
                    onBack = { page-- },
                    showBack = page > PAGE_WELCOME,
                )
            }

            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    val forward = targetState > initialState
                    val slide = tween<IntOffset>(380, easing = FastOutSlowInEasing)
                    (slideInHorizontally(slide) { if (forward) it / 2 else -it / 2 } + fadeIn(tween(300, 60)))
                        .togetherWith(
                            slideOutHorizontally(slide) { if (forward) -it / 2 else it / 2 } + fadeOut(tween(180)),
                        )
                },
                label = "onboardingSlide",
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) { targetPage ->
                when (targetPage) {
                    PAGE_WELCOME -> OnboardingContainer {
                        WelcomeStep(
                            name = name,
                            onNameChange = { name = it.take(NAME_LIMIT) },
                            onContinue = {
                                val chosen = name.trim().ifBlank { "Listener" }
                                focusManager.clearFocus()
                                onNameChosen(chosen)
                                page = PAGE_SOUND
                            },
                        )
                    }

                    PAGE_SOUND -> OnboardingContainer {
                        TailorSoundStep(
                            selectedGenres = selectedGenres,
                            onToggleGenre = { genre ->
                                selectedGenres = if (selectedGenres.contains(genre)) {
                                    if (selectedGenres.size > 1) selectedGenres - genre else selectedGenres
                                } else {
                                    if (selectedGenres.size < 3) selectedGenres + genre else selectedGenres
                                }
                            },
                            selectedAccent = themeAccent,
                            onAccentChange = { themeAccent = it },
                            onContinue = {
                                AppSettings.setSelectedGenres(selectedGenres)
                                AppSettings.setThemeAccent(themeAccent)
                                page = PAGE_CACHING
                            },
                        )
                    }

                    PAGE_CACHING -> OnboardingContainer {
                        SmartCachingStep(
                            onStartListening = onFinish,
                            onConnectGoogle = { page = PAGE_GOOGLE },
                        )
                    }

                    else -> GoogleSignInScreen(
                        onSignIn = onSignIn,
                        onSkip = onFinish,
                        onBack = { page = PAGE_CACHING },
                        stepLabel = "Connection — 4 of 4",
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingContainer(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .padding(horizontal = 24.dp),
            content = content,
        )
    }
}

/** Header matching screen_record_5s.jpg with Soundwave glyph, Brand Wordmark, and Step Pill */
@Composable
private fun StepProgressHeader(
    currentPage: Int,
    totalPages: Int,
    onBack: () -> Unit,
    showBack: Boolean,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showBack) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)), CircleShape)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = BitChordIcons.ChevronRight,
                            contentDescription = stringResource(R.string.onboarding_back),
                            tint = Color.White,
                            modifier = Modifier
                                .size(14.dp)
                                .graphicsLayer { scaleX = -1f },
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                } else {
                    // Small Soundwave emblem
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_hush_emblem),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    text = "HUSH MUSIC",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.W800,
                    letterSpacing = 1.6.sp,
                    color = Color.White.copy(alpha = 0.90f),
                )
            }

            // Step Indicator Badge ("1 of 4", "2 of 4", etc.)
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${currentPage + 1} of $totalPages",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.W700,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Progress Bar
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
        ) {
            val progressFraction = (currentPage + 1f) / totalPages.toFloat()
            val animatedWidth by animateFloatAsState(
                targetValue = progressFraction,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
                label = "stepProgress",
            )
            Box(
                Modifier
                    .fillMaxWidth(animatedWidth)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFF6E40), // Sunset Orange
                                Color(0xFFFFAB91), // Peach
                                Color(0xFFB388FF), // Deep Lavender
                            ),
                        ),
                    ),
            )
        }
    }
}

/** Step 1: Welcome to Hush Music & Dynamic Personalization */
@Composable
private fun ColumnScope.WelcomeStep(
    name: String,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit,
) {
    val keyboardUp = WindowInsets.isImeVisible

    Spacer(Modifier.height(18.dp))

    // Animated Brand Emblem Reveal with zero checkerboard artifacts
    val infiniteTransition = rememberInfiniteTransition(label = "brandPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
                .shadow(24.dp, RoundedCornerShape(26.dp), spotColor = Color(0xFFFF6E40).copy(alpha = 0.40f))
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF1E1824),
                            Color(0xFF121318),
                        ),
                    ),
                )
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFFF6E40),
                                Color(0xFFFFAB91),
                                Color(0xFFB388FF),
                            ),
                        ),
                    ),
                    RoundedCornerShape(26.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_hush_emblem),
                contentDescription = "Hush Music Emblem",
                modifier = Modifier.size(56.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Welcome to Hush Music",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp,
                lineHeight = 34.sp,
                letterSpacing = (-0.6).sp,
            ),
            fontWeight = FontWeight.W800,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Pure acoustics, bit-perfect streaming, and zero distraction.",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            color = Color.White.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
        )
    }

    if (!keyboardUp) {
        Spacer(Modifier.weight(1f))
    } else {
        Spacer(Modifier.height(20.dp))
    }

    // "What should we call you?" Name input section
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "What should we call you?",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.W700,
            color = Color.White.copy(alpha = 0.85f),
        )

        Spacer(Modifier.height(10.dp))

        FrostedNameField(
            value = name,
            onValueChange = onNameChange,
            onDone = onContinue,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "This personalizes your greeting and Listen Together parties.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.50f),
            fontSize = 12.sp,
        )
    }

    if (!keyboardUp) {
        Spacer(Modifier.weight(1f))
    } else {
        Spacer(Modifier.height(20.dp))
    }

    BouncyActionButton(
        label = "Continue",
        onClick = onContinue,
        primary = true,
        enabled = name.isNotBlank(),
    )

    Spacer(Modifier.height(16.dp))
}

/** Step 2: Tailor Your Sound (Genre Pills + Theme Accent) */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.TailorSoundStep(
    selectedGenres: Set<String>,
    onToggleGenre: (String) -> Unit,
    selectedAccent: String,
    onAccentChange: (String) -> Unit,
    onContinue: () -> Unit,
) {
    Spacer(Modifier.height(14.dp))

    Text(
        text = "Tailor your sound",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.6).sp,
        ),
        fontWeight = FontWeight.W800,
        color = Color.White,
    )

    Spacer(Modifier.height(6.dp))

    Text(
        text = "Select 1 to 3 genres and vibes to shape your daily mixes.",
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = Color.White.copy(alpha = 0.65f),
    )

    Spacer(Modifier.height(20.dp))

    Column(
        Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Exactly the 14 genres matching screen_record_12s.jpg & 13s.jpg
        val genres = listOf(
            "Lo-Fi", "Electronic", "UK Garage", "Hip-Hop",
            "Ambient", "Indie", "Synthwave", "Classical",
            "Jazz", "Bass", "R&B", "Rock",
            "House", "Pop",
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            genres.forEach { genre ->
                val isSelected = selectedGenres.contains(genre)
                GenrePill(
                    label = genre,
                    selected = isSelected,
                    onClick = { onToggleGenre(genre) },
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "${selectedGenres.size} selected · Minimum 1 required",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.W600,
            color = Color.White.copy(alpha = 0.50f),
            fontSize = 12.sp,
        )

        Spacer(Modifier.height(8.dp))

        // Signature Accent Color Palette
        Text(
            text = "SIGNATURE ACCENT",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.W800,
            letterSpacing = 1.2.sp,
            color = Color.White.copy(alpha = 0.60f),
        )

        val accents = listOf(
            Triple("sunset", "Sunset", Color(0xFFFF6E40)),
            Triple("peach", "Peach", Color(0xFFFFAB91)),
            Triple("lavender", "Lavender", Color(0xFFB388FF)),
            Triple("charcoal", "Charcoal", Color(0xFF2B2C33)),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            accents.forEach { (key, label, color) ->
                val isSelected = selectedAccent == key
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = if (isSelected) 0.16f else 0.07f))
                        .border(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) color else Color.White.copy(alpha = 0.12f),
                            RoundedCornerShape(16.dp),
                        )
                        .clickable { onAccentChange(key) }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.W700 else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.70f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(14.dp))

    BouncyActionButton(
        label = "Continue",
        onClick = onContinue,
        primary = true,
        enabled = selectedGenres.isNotEmpty(),
    )

    Spacer(Modifier.height(16.dp))
}

/** Step 3: Never Lose the Rhythm (Smart Downloads & Offline Storage Cap) */
@Composable
private fun ColumnScope.SmartCachingStep(
    onStartListening: () -> Unit,
    onConnectGoogle: () -> Unit,
) {
    var smartDownloads by remember { mutableStateOf(true) }
    var wifiOnly by remember { mutableStateOf(AppSettings.wifiOnlyDownloads.value) }

    // Storage Cap Options: 2 GB, 5 GB, 10 GB, Unlimited
    val currentLimit = AppSettings.audioCacheLimitBytes.value
    var selectedCapIndex by remember {
        mutableIntStateOf(
            when (currentLimit) {
                2L * 1024 * 1024 * 1024 -> 0
                5L * 1024 * 1024 * 1024 -> 1
                10L * 1024 * 1024 * 1024 -> 2
                UNLIMITED_CACHE_LIMIT_BYTES -> 3
                else -> 1 // Default to 5 GB
            }
        )
    }

    Spacer(Modifier.height(14.dp))

    Text(
        text = "Never lose the rhythm",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.6).sp,
        ),
        fontWeight = FontWeight.W800,
        color = Color.White,
    )

    Spacer(Modifier.height(6.dp))

    Text(
        text = "Automatic caching keeps your music going even offline.",
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = Color.White.copy(alpha = 0.65f),
    )

    Spacer(Modifier.height(20.dp))

    Column(
        Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Frosted Card 1: Smart Downloads
        FrostedToggleCard(
            title = "Smart Downloads",
            subtitle = "Automatically cache your top tracks and daily mixes over Wi-Fi.",
            checked = smartDownloads,
            onCheckedChange = {
                smartDownloads = it
                AppSettings.setSyncDownloadedPlaylists(it)
            },
        )

        // Frosted Card 2: Wi-Fi Only
        FrostedToggleCard(
            title = "Wi-Fi Only",
            subtitle = "Prevent mobile data usage for offline caching and track sync.",
            checked = wifiOnly,
            onCheckedChange = {
                wifiOnly = it
                AppSettings.setWifiOnlyDownloads(it)
            },
        )

        // Frosted Card 3: Storage Cap Segmented Control
        val capLabels = listOf("2 GB", "5 GB", "10 GB", "Unlimited")
        val capBytes = listOf(
            2L * 1024 * 1024 * 1024,
            5L * 1024 * 1024 * 1024,
            10L * 1024 * 1024 * 1024,
            UNLIMITED_CACHE_LIMIT_BYTES,
        )

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.07f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(22.dp))
                .padding(18.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Storage Cap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.W700,
                    color = Color.White,
                    fontSize = 16.sp,
                )
                Text(
                    text = "${capLabels[selectedCapIndex]} Allocated",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.W600,
                    color = Color(0xFFFFAB91),
                    fontSize = 13.sp,
                )
            }

            Spacer(Modifier.height(14.dp))

            // 4-pill segmented control
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                capLabels.forEachIndexed { index, label ->
                    val active = selectedCapIndex == index
                    val interaction = remember { MutableInteractionSource() }
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (active) Color.White.copy(alpha = 0.22f) else Color.Transparent)
                            .border(
                                if (active) 1.dp else 0.dp,
                                if (active) Color.White.copy(alpha = 0.35f) else Color.Transparent,
                                RoundedCornerShape(11.dp),
                            )
                            .clickable(
                                interactionSource = interaction,
                                indication = null,
                                onClick = {
                                    selectedCapIndex = index
                                    AppSettings.setAudioCacheLimitBytes(capBytes[index])
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (active) FontWeight.W800 else FontWeight.W500,
                            color = if (active) Color.White else Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(14.dp))

    // Primary Action: Start Listening with Sunset Gradient
    BouncyActionButton(
        label = "Start Listening",
        onClick = onStartListening,
        primary = true,
    )

    Spacer(Modifier.height(10.dp))

    // Secondary Action: Connect Google Account (Optional)
    BouncyActionButton(
        label = "Connect Google Account (Optional)",
        onClick = onConnectGoogle,
        primary = false,
    )

    Spacer(Modifier.height(16.dp))
}

/** Tactile Bouncy Genre Pill */
@Composable
private fun GenrePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 650f),
        label = "pillScale",
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF6E40),
                            Color(0xFFFF5722),
                        ),
                    )
                } else {
                    SolidColor(Color.White.copy(alpha = 0.08f))
                },
            )
            .border(
                1.2.dp,
                if (selected) Color(0xFFFFAB91) else Color.White.copy(alpha = 0.14f),
                RoundedCornerShape(20.dp),
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.W800 else FontWeight.W500,
            color = Color.White,
            fontSize = 14.sp,
        )
    }
}

/** Frosted Glass Toggle Card with IosSwitch */
@Composable
private fun FrostedToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.W700,
                color = Color.White,
                fontSize = 16.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
        Spacer(Modifier.width(14.dp))
        IosSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

/** Tactile Frosted Glass Name Field */
@Composable
private fun FrostedNameField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    val style = MaterialTheme.typography.titleLarge.copy(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.4).sp,
        color = Color.White,
        fontWeight = FontWeight.W700,
    )

    Box(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.09f))
            .border(BorderStroke(1.2.dp, Color.White.copy(alpha = 0.18f)), RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(
                text = "Your name or handle",
                style = style,
                color = Color.White.copy(alpha = 0.35f),
                maxLines = 1,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = style,
            cursorBrush = SolidColor(Color(0xFFFF6E40)),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Primary / Secondary Tactile Spring Action Button */
@Composable
private fun BouncyActionButton(
    label: String,
    onClick: () -> Unit,
    primary: Boolean,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 650f),
        label = "btnScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.45f,
        animationSpec = tween(180),
        label = "btnAlpha",
    )

    Row(
        Modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clip(CircleShape)
            .background(
                if (primary) {
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF6E40),
                            Color(0xFFFF5722),
                        ),
                    )
                } else {
                    SolidColor(Color.White.copy(alpha = 0.10f))
                },
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (primary) Color(0xFFFFAB91).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f),
                ),
                CircleShape,
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.W800,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

private const val PAGE_WELCOME = 0
private const val PAGE_SOUND = 1
private const val PAGE_CACHING = 2
private const val PAGE_GOOGLE = 3

private const val NAME_LIMIT = 32

private val SunsetMeshPalettes = listOf(
    MeshPalette(listOf(Color(0xFFFF6E40), Color(0xFFFFAB91), Color(0xFFB388FF), Color(0xFF090A0D))),
    MeshPalette(listOf(Color(0xFFFF5722), Color(0xFFFF8A65), Color(0xFF7E57C2), Color(0xFF101116))),
)

private val SoundMeshPalettes = listOf(
    MeshPalette(listOf(Color(0xFFFF5722), Color(0xFF9C27B0), Color(0xFF3F51B5), Color(0xFF090A0D))),
    MeshPalette(listOf(Color(0xFFFF6E40), Color(0xFF673AB7), Color(0xFF2196F3), Color(0xFF101116))),
)

private val CachingMeshPalettes = listOf(
    MeshPalette(listOf(Color(0xFF00897B), Color(0xFF00ACC1), Color(0xFF3949AB), Color(0xFF090A0D))),
    MeshPalette(listOf(Color(0xFF00695C), Color(0xFF00838F), Color(0xFF283593), Color(0xFF101116))),
)

private val GoogleMeshPalettes = listOf(
    MeshPalette(listOf(Color(0xFF4A90E2), Color(0xFFFF6E40), Color(0xFF7E2A4C), Color(0xFF090A0D))),
    MeshPalette(listOf(Color(0xFF2E6AB3), Color(0xFFFF5722), Color(0xFF4D142B), Color(0xFF101116))),
)
