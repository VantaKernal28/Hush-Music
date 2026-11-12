package com.music.bitchord.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.R
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.AudioQuality
import com.music.bitchord.data.settings.DownloadQuality
import com.music.bitchord.sharedui.resources.Res
import com.music.bitchord.sharedui.resources.ic_google_g
import com.music.bitchord.sharedui.resources.ic_logo
import org.jetbrains.compose.resources.painterResource
import com.music.bitchord.ui.icons.BitChordIcons
import com.music.bitchord.ui.player.MeshGradientBackground
import com.music.bitchord.ui.player.MESH_CYCLE_MILLIS
import com.music.bitchord.ui.player.MeshPalette
import com.music.bitchord.ui.player.WelcomeMeshPalettes
import com.music.bitchord.ui.player.cyclingMeshPalette
import kotlinx.coroutines.launch

private val FrostEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

private const val STEP_WELCOME = 0
private const val STEP_VIBE = 1
private const val STEP_QUALITY = 2
private const val STEP_DOWNLOADS = 3
private const val TOTAL_STEPS = 4

private const val NAME_LIMIT = 40

private val GENRE_PRESETS = listOf(
    "Lo-Fi", "Electronic", "UK Garage", "Hip-Hop", 
    "Ambient", "Indie", "Synthwave", "Classical", 
    "Jazz", "Bass", "R&B", "Rock", "House", "Pop"
)

/**
 * Modern Frost Glass (Glassmorphism) 4-Step Onboarding Wizard for Hush Music.
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
    var step by rememberSaveable { mutableIntStateOf(STEP_WELCOME) }
    var name by rememberSaveable { mutableStateOf(initialName) }
    val selectedGenres = remember { mutableStateListOf("Lo-Fi", "Electronic") }
    var selectedQuality by rememberSaveable { mutableStateOf(AudioQuality.HIGH) }
    var smartDownloads by rememberSaveable { mutableStateOf(true) }
    var wifiOnly by rememberSaveable { mutableStateOf(true) }
    var storageCapIndex by rememberSaveable { mutableIntStateOf(1) } // 0: 2GB, 1: 5GB, 2: 10GB, 3: Unlimited

    val focusManager = LocalFocusManager.current

    BackHandler(enabled = step > STEP_WELCOME) {
        step -= 1
    }

    val palette = cyclingMeshPalette(WelcomeMeshPalettes)

    Box(
        modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D11))
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
    ) {
        // Dynamic Mesh Gradient Backdrop
        MeshGradientBackground(
            palette = palette,
            trackKey = step,
            continuous = true,
            driftMillis = 6_000,
            colorFadeMillis = MESH_CYCLE_MILLIS,
        )

        // Glassmorphism Deep Frost Scrim with Vignette
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.45f),
                        0.5f to Color(0xFF0F0E17).copy(alpha = 0.65f),
                        1f to Color.Black.copy(alpha = 0.88f),
                    ),
                ),
        )

        // Ambient Glow Orbs
        AmbientGlow(
            color = Color(0xFFFA586A).copy(alpha = 0.22f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(340.dp)
                .graphicsLayer { translationY = -120.dp.toPx() },
        )

        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
        ) {
            // Sleek Frosted Progress Indicator & Top Header
            FrostProgressHeader(
                currentStep = step,
                totalSteps = TOTAL_STEPS,
                canGoBack = step > STEP_WELCOME,
                onBack = { if (step > STEP_WELCOME) step -= 1 },
            )

            // Step Carousel with Spring / Cubic-Bezier Transitions
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        val forward = targetState > initialState
                        val slideSpec = tween<IntOffset>(durationMillis = 440, easing = FrostEasing)
                        val fadeSpec = tween<Float>(durationMillis = 320, easing = FrostEasing)
                        (slideInHorizontally(slideSpec) { if (forward) it / 2 else -it / 2 } + fadeIn(fadeSpec))
                            .togetherWith(
                                slideOutHorizontally(slideSpec) { if (forward) -it / 2 else it / 2 } + fadeOut(fadeSpec),
                            )
                    },
                    label = "frostStepAnimation",
                    modifier = Modifier.fillMaxSize(),
                ) { currentStep ->
                    OnboardingContainer {
                        when (currentStep) {
                            STEP_WELCOME -> StepWelcome(
                                name = name,
                                onNameChange = { name = it.take(NAME_LIMIT) },
                                onNext = {
                                    val chosen = name.trim()
                                    if (chosen.isNotEmpty()) {
                                        focusManager.clearFocus()
                                        onNameChosen(chosen)
                                        step = STEP_VIBE
                                    }
                                },
                            )
                            STEP_VIBE -> StepTaste(
                                selectedGenres = selectedGenres,
                                onToggleGenre = { genre ->
                                    if (selectedGenres.contains(genre)) selectedGenres.remove(genre)
                                    else selectedGenres.add(genre)
                                },
                                onNext = { step = STEP_QUALITY },
                            )
                            STEP_QUALITY -> StepFidelity(
                                selected = selectedQuality,
                                onSelect = { quality ->
                                    selectedQuality = quality
                                    AppSettings.audioQualityWifi.value = quality
                                    AppSettings.audioQualityCellular.value = quality
                                    AppSettings.downloadQuality.value = when (quality) {
                                        AudioQuality.LOSSLESS -> DownloadQuality.LOSSLESS
                                        AudioQuality.HIGH -> DownloadQuality.HIGH
                                        else -> DownloadQuality.STANDARD
                                    }
                                },
                                onNext = { step = STEP_DOWNLOADS },
                            )
                            STEP_DOWNLOADS -> StepOfflineSync(
                                smartDownloads = smartDownloads,
                                onSmartDownloadsChange = {
                                    smartDownloads = it
                                    AppSettings.syncDownloadedPlaylists.value = it
                                },
                                wifiOnly = wifiOnly,
                                onWifiOnlyChange = {
                                    wifiOnly = it
                                    AppSettings.wifiOnlyDownloads.value = it
                                },
                                storageCapIndex = storageCapIndex,
                                onStorageCapChange = {
                                    storageCapIndex = it
                                    val bytes = when (it) {
                                        0 -> 2L * 1024 * 1024 * 1024
                                        1 -> 5L * 1024 * 1024 * 1024
                                        2 -> 10L * 1024 * 1024 * 1024
                                        else -> Long.MAX_VALUE
                                    }
                                    AppSettings.setAudioCacheLimitBytes(bytes)
                                },
                                signedIn = signedIn,
                                onSignIn = onSignIn,
                                onFinish = onFinish,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Container capped for phones, tablets, and wide screens. */
@Composable
private fun OnboardingContainer(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

/** Top Header with Back button and Frosted Interpolating Progress Track */
@Composable
private fun FrostProgressHeader(
    currentStep: Int,
    totalSteps: Int,
    canGoBack: Boolean,
    onBack: () -> Unit,
) {
    val progress = (currentStep + 1).toFloat() / totalSteps.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400, easing = FrostEasing),
        label = "progressAnim",
    )

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (canGoBack) {
                FrostIconButton(onClick = onBack) {
                    Icon(
                        imageVector = BitChordIcons.ChevronRight,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { scaleX = -1f },
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic_logo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = "HUSH MUSIC",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                }
            }

            // Step Indicator Badge
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "${currentStep + 1} of $totalSteps",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Frosted Progress Track
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFA586A), Color(0xFFFF7A8A), Color(0xFFC084FC)),
                        ),
                    ),
            )
        }
    }
}

/** Step 1: Welcome & Identity */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.StepWelcome(
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit,
) {
    val keyboardUp = WindowInsets.isImeVisible

    Spacer(Modifier.height(16.dp))

    // Animated Frosted Emblem & Wave
    AnimatedVisibility(
        visible = !keyboardUp,
        enter = fadeIn(tween(300)) + expandVertically(),
        exit = fadeOut(tween(200)) + shrinkVertically(),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.04f)),
                        ),
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                )
            }

            Spacer(Modifier.height(14.dp))
            AnimatedAudioWave()
        }
    }

    Spacer(Modifier.height(20.dp))

    Text(
        text = "Welcome to Hush Music",
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.8).sp,
        ),
        color = Color.White,
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(10.dp))

    Text(
        text = "Pure acoustics, bit-perfect streaming, and zero distraction.",
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 15.sp,
        color = Color.White.copy(alpha = 0.65f),
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(36.dp))

    // Frosted Name Field
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "What should we call you?",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )

        FrostTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "Your name or handle",
            onDone = onNext,
        )

        Text(
            text = "This personalizes your greeting and Listen Together parties.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.45f),
            modifier = Modifier.padding(start = 4.dp, top = 8.dp),
        )
    }

    Spacer(Modifier.height(36.dp))

    FrostPrimaryButton(
        label = "Continue",
        onClick = onNext,
        enabled = name.isNotBlank(),
    )

    Spacer(Modifier.height(20.dp))
}

/** Step 2: Music Taste & Vibe */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.StepTaste(
    selectedGenres: List<String>,
    onToggleGenre: (String) -> Unit,
    onNext: () -> Unit,
) {
    Spacer(Modifier.height(12.dp))

    Text(
        text = "Tailor your sound",
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            letterSpacing = (-0.6).sp,
        ),
        color = Color.White,
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "Select 1 to 3 genres and vibes to shape your daily mixes.",
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 15.sp,
        color = Color.White.copy(alpha = 0.65f),
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(28.dp))

    // Interactive Frosted Pills Cloud
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GENRE_PRESETS.forEach { genre ->
            val isSelected = selectedGenres.contains(genre)
            FrostPill(
                label = genre,
                selected = isSelected,
                onClick = { onToggleGenre(genre) },
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    Text(
        text = "${selectedGenres.size} selected · Minimum 1 required",
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.5f),
    )

    Spacer(Modifier.height(32.dp))

    FrostPrimaryButton(
        label = "Continue",
        onClick = onNext,
        enabled = selectedGenres.isNotEmpty(),
    )

    Spacer(Modifier.height(20.dp))
}

/** Step 3: Audio Quality */
@Composable
private fun ColumnScope.StepFidelity(
    selected: AudioQuality,
    onSelect: (AudioQuality) -> Unit,
    onNext: () -> Unit,
) {
    Spacer(Modifier.height(12.dp))

    Text(
        text = "Choose your fidelity",
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            letterSpacing = (-0.6).sp,
        ),
        color = Color.White,
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "Fine-tune your playback engine. You can change this anytime.",
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 15.sp,
        color = Color.White.copy(alpha = 0.65f),
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(26.dp))

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FidelityCard(
            title = "Standard (128 kbps)",
            badge = null,
            detail = "Data saver, smooth playback anywhere without buffering.",
            selected = selected == AudioQuality.MEDIUM || selected == AudioQuality.LOW,
            onClick = { onSelect(AudioQuality.MEDIUM) },
        )

        FidelityCard(
            title = "High (320 kbps)",
            badge = "Recommended",
            detail = "Crisp, balanced studio-grade streaming for everyday listening.",
            selected = selected == AudioQuality.HIGH,
            onClick = { onSelect(AudioQuality.HIGH) },
        )

        FidelityCard(
            title = "Lossless (FLAC / Hi-Res)",
            badge = null,
            detail = "Uncompressed bit-perfect audio for audiophiles with dedicated DACs.",
            selected = selected == AudioQuality.LOSSLESS,
            onClick = { onSelect(AudioQuality.LOSSLESS) },
        )
    }

    Spacer(Modifier.height(32.dp))

    FrostPrimaryButton(
        label = "Continue",
        onClick = onNext,
    )

    Spacer(Modifier.height(20.dp))
}

/** Step 4: Smart Downloads & Offline Sync */
@Composable
private fun ColumnScope.StepOfflineSync(
    smartDownloads: Boolean,
    onSmartDownloadsChange: (Boolean) -> Unit,
    wifiOnly: Boolean,
    onWifiOnlyChange: (Boolean) -> Unit,
    storageCapIndex: Int,
    onStorageCapChange: (Int) -> Unit,
    signedIn: Boolean,
    onSignIn: () -> Unit,
    onFinish: () -> Unit,
) {
    Spacer(Modifier.height(12.dp))

    Text(
        text = "Never lose the rhythm",
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            letterSpacing = (-0.6).sp,
        ),
        color = Color.White,
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "Automatic caching keeps your music going even offline.",
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 15.sp,
        color = Color.White.copy(alpha = 0.65f),
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(24.dp))

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Toggle 1: Smart Downloads
        FrostToggleRow(
            title = "Smart Downloads",
            detail = "Automatically cache your top tracks and daily mixes over Wi-Fi.",
            checked = smartDownloads,
            onCheckedChange = onSmartDownloadsChange,
        )

        // Toggle 2: Wi-Fi Only
        FrostToggleRow(
            title = "Wi-Fi Only",
            detail = "Prevent mobile data usage for offline caching and track sync.",
            checked = wifiOnly,
            onCheckedChange = onWifiOnlyChange,
        )

        // Storage Cap Segmented Control
        FrostSegmentedCard(
            title = "Storage Cap",
            options = listOf("2 GB", "5 GB", "10 GB", "Unlimited"),
            selectedIndex = storageCapIndex,
            onSelect = onStorageCapChange,
        )
    }

    Spacer(Modifier.height(28.dp))

    // Glowing CTA "Start Listening"
    FrostGlowButton(
        label = "Start Listening",
        onClick = onFinish,
    )

    Spacer(Modifier.height(12.dp))

    if (!signedIn) {
        FrostGhostButton(
            label = "Connect Google Account (Optional)",
            onClick = onSignIn,
        )
    }

    Spacer(Modifier.height(20.dp))
}

// -----------------------------------------------------------------------------
// Frost Glass UI Components
// -----------------------------------------------------------------------------

@Composable
private fun FrostTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onDone: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                color = Color.White.copy(alpha = 0.38f),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            ),
            cursorBrush = SolidColor(Color(0xFFFA586A)),
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

@Composable
private fun FrostPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else if (selected) 1.03f else 1f,
        animationSpec = spring(stiffness = 800f, dampingRatio = 0.7f),
        label = "pillScale",
    )

    val bgColor = if (selected) Color(0xFFFA586A).copy(alpha = 0.28f) else Color.White.copy(alpha = 0.07f)
    val borderColor = if (selected) Color(0xFFFA586A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.14f)

    Box(
        Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            ),
            color = if (selected) Color.White else Color.White.copy(alpha = 0.85f),
        )
    }
}

@Composable
private fun FidelityCard(
    title: String,
    badge: String?,
    detail: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 900f),
        label = "cardScale",
    )

    val bg = if (selected) {
        Brush.linearGradient(
            listOf(Color(0xFFFA586A).copy(alpha = 0.2f), Color.White.copy(alpha = 0.06f)),
        )
    } else {
        SolidColor(Color.White.copy(alpha = 0.05f))
    }
    val borderColor = if (selected) Color(0xFFFA586A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.12f)

    Box(
        Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .padding(18.dp),
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        ),
                        color = Color.White,
                    )
                    badge?.let {
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFFA586A).copy(alpha = 0.25f))
                                .border(1.dp, Color(0xFFFA586A).copy(alpha = 0.45f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8FA3),
                            )
                        }
                    }
                }

                // Check indicator
                Box(
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (selected) Color(0xFFFA586A) else Color.Transparent)
                        .border(
                            1.5.dp,
                            if (selected) Color(0xFFFA586A) else Color.White.copy(alpha = 0.25f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.65f),
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun FrostToggleRow(
    title: String,
    detail: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 17.sp,
                )
            }

            // Custom Frosted Toggle Switch
            val thumbOffset by animateFloatAsState(
                targetValue = if (checked) 20f else 0f,
                animationSpec = spring(stiffness = 900f),
                label = "toggleThumb",
            )
            Box(
                Modifier
                    .size(width = 48.dp, height = 28.dp)
                    .clip(CircleShape)
                    .background(if (checked) Color(0xFFFA586A) else Color.White.copy(alpha = 0.16f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                    .padding(3.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(
                    Modifier
                        .graphicsLayer { translationX = thumbOffset.dp.toPx() }
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .shadow(4.dp, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun FrostSegmentedCard(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "${options[selectedIndex]} Allocated",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF8FA3),
                )
            }

            Spacer(Modifier.height(12.dp))

            // Segmented Bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                options.forEachIndexed { index, opt ->
                    val isSelected = selectedIndex == index
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) Color.White.copy(alpha = 0.22f) else Color.Transparent)
                            .clickable { onSelect(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                            ),
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
                        )
                    }
                }
            }
        }
    }
}

/** Glowing High-Contrast CTA Button */
@Composable
private fun FrostGlowButton(
    label: String,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = 900f),
        label = "glowScale",
    )

    Box(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFA586A), Color(0xFFE11D48)),
                ),
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                letterSpacing = (-0.2).sp,
            ),
            color = Color.White,
        )
    }
}

@Composable
private fun FrostPrimaryButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = 900f),
        label = "primScale",
    )

    Box(
        Modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.4f
            }
            .clip(CircleShape)
            .background(Color.White)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            ),
            color = Color(0xFF0F172A),
        )
    }
}

@Composable
private fun FrostGhostButton(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            ),
            color = Color.White.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun FrostIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.09f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** Animated wave bars with spring loop */
@Composable
private fun AnimatedAudioWave() {
    val transition = rememberInfiniteTransition(label = "waveTransition")
    val h1 by transition.animateFloat(
        initialValue = 6f, targetValue = 26f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h1",
    )
    val h2 by transition.animateFloat(
        initialValue = 18f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h2",
    )
    val h3 by transition.animateFloat(
        initialValue = 10f, targetValue = 30f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h3",
    )
    val h4 by transition.animateFloat(
        initialValue = 24f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h4",
    )
    val h5 by transition.animateFloat(
        initialValue = 8f, targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h5",
    )

    Row(
        Modifier.height(34.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(h1, h2, h3, h4, h5).forEach { h ->
            Box(
                Modifier
                    .width(3.5.dp)
                    .height(h.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFA586A), Color(0xFFFF8FA3)),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun AmbientGlow(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .blur(60.dp)
            .background(color, CircleShape),
    )
}
