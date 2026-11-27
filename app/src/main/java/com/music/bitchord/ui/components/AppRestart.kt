package com.music.bitchord.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.MainActivity
import com.music.bitchord.R
import com.music.bitchord.data.LocaleTags
import com.music.bitchord.data.settings.AppSettings
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

object AppLocaleHelper {
    private const val PREFS_NAME = "bitchord_settings"
    private const val KEY_APP_LANGUAGE_TAG = "app_language_tag"

    fun resolveSavedLanguageTag(context: Context): String? {
        val delegateTag = runCatching {
            AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag()
        }.getOrNull()?.takeIf { it.isNotBlank() }

        val prefsTag = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_LANGUAGE_TAG, null)
            ?.takeIf { it.isNotBlank() }

        val raw = delegateTag ?: prefsTag ?: return null
        return normalizeLanguageTag(raw)
    }

    fun normalizeLanguageTag(raw: String): String {
        val normalized = LocaleTags.normalizeAppTag(raw)
        return when (normalized) {
            "zh-Hant" -> "zh-TW"
            "zh-Hans" -> "zh-CN"
            "in" -> "id"
            else -> normalized
        }
    }

    fun wrapBaseContext(base: Context): Context {
        val tag = resolveSavedLanguageTag(base) ?: return base
        val locale = Locale.forLanguageTag(tag)
        if (locale.language.isBlank()) return base
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLocales(LocaleList(locale))
        return base.createConfigurationContext(config)
    }

    fun syncLocaleOnAppCreate(context: Context) {
        val delegateTag = runCatching {
            AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag()
        }.getOrNull()?.takeIf { it.isNotBlank() }
        val settingsTag = AppSettings.appLanguageTag.value?.takeIf { it.isNotBlank() }

        val resolved = when {
            delegateTag != null -> {
                val normalized = normalizeLanguageTag(delegateTag)
                if (normalized != settingsTag) AppSettings.setAppLanguageTag(normalized)
                normalized
            }
            settingsTag != null -> {
                val normalized = normalizeLanguageTag(settingsTag)
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(normalized))
                normalized
            }
            else -> null
        }

        if (resolved != null) {
            val locale = Locale.forLanguageTag(resolved)
            if (locale.language.isNotBlank()) {
                Locale.setDefault(locale)
            }
        }
    }

    fun applyLanguageSelection(context: Context, rawTag: String) {
        val tag = normalizeLanguageTag(rawTag)
        AppSettings.setAppLanguageTag(tag)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APP_LANGUAGE_TAG, tag)
            .commit()
        val locale = Locale.forLanguageTag(tag)
        if (locale.language.isNotBlank()) {
            Locale.setDefault(locale)
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}

object AppRestartController {
    val pendingReason = MutableStateFlow<String?>(null)

    fun requestRestart(reason: String) {
        pendingReason.value = reason
    }

    fun dismiss() {
        pendingReason.value = null
    }

    fun restartApp(context: Context) {
        pendingReason.value = null
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.component
            ?.let { Intent.makeRestartActivityTask(it) }
            ?: Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        context.startActivity(launchIntent)
        if (context is Activity) {
            context.finishAffinity()
        }
        exitProcess(0)
    }
}

@Composable
fun AppRestartPromptCard(
    reason: String,
    hazeState: HazeState,
    onRestart: () -> Unit,
    onDismiss: () -> Unit,
) {
    val reduceBlur = AppSettings.reduceDynamicBlur.collectAsStateWithLifecycle().value
    HushCenteredCard(
        onDismiss = onDismiss,
        hazeState = hazeState,
        reduceBlur = reduceBlur,
        title = "Restart Hush",
        subtitle = "Recommended for full effect",
        maxWidth = 380.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Later",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onRestart),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Restart App",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

/**
 * Smooth cold-start entrance reveal shown briefly once after cold launch when onboarding
 * is already complete so the transition from the system launch surface into Hush is fluid.
 */
@Composable
fun HushLaunchReveal(
    enabled: Boolean,
    reduceAnimation: Boolean,
) {
    var done by rememberSaveable { mutableStateOf(!enabled || reduceAnimation) }
    if (done) return

    val converge = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }
    val emblemScale = remember { Animatable(0.92f) }

    LaunchedEffect(Unit) {
        launch {
            converge.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 360,
                    easing = CubicBezierEasing(0.20f, 1.0f, 0.30f, 1f),
                ),
            )
        }
        launch {
            emblemScale.animateTo(
                targetValue = 1.04f,
                animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            )
        }
        delay(240L)
        overlayAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        )
        done = true
    }

    if (overlayAlpha.value <= 0.005f) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(90f)
            .graphicsLayer { alpha = overlayAlpha.value }
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1A1B20), Color(0xFF0A0A0C)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.graphicsLayer {
                scaleX = emblemScale.value
                scaleY = emblemScale.value
            },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val t = converge.value.coerceIn(0f, 1f)
                val baseRadius = size.minDimension * (0.34f - 0.07f * t)
                val dotRadius = 3.8.dp.toPx()
                for (i in 0 until 10) {
                    val angle = ((i.toFloat() * (2.0 * PI / 10.0)) - (PI / 2.0) + (1f - t) * 0.55f).toFloat()
                    val center = Offset(cx + baseRadius * cos(angle), cy + baseRadius * sin(angle))
                    drawCircle(
                        color = Color.White.copy(alpha = 0.18f),
                        radius = dotRadius * 1.6f,
                        center = center,
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.96f),
                        radius = dotRadius,
                        center = center,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Hush",
                fontFamily = FontFamily(Font(R.font.cherry_bomb_one_regular)),
                fontSize = 30.sp,
                color = Color.White,
                letterSpacing = (-0.4).sp,
            )
        }
    }
}
