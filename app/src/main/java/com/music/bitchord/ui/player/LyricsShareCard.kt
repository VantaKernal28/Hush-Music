package com.music.bitchord.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import com.music.bitchord.R
import com.music.bitchord.ui.replay.Fonts
import com.music.bitchord.ui.replay.ellipsised
import com.music.bitchord.ui.replay.loadBitmap
import com.music.bitchord.ui.replay.paletteOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

internal enum class LyricsCardThemeMode(val label: String) {
    ALBUM_GRADIENT("Album Gradient"),
    APP_THEME("App Theme"),
}

internal data class LyricsCardRenderResult(
    val bitmap: Bitmap,
    val topHexColor: String,
    val bottomHexColor: String,
)

/**
 * Renders the Spotify/Apple-inspired Hush Lyrics Card bitmap with smooth
 * Apple continuous squircle corners, selectable App Theme vs Album Gradient background,
 * and custom M3E small top-left album artwork shape.
 */
internal suspend fun renderLyricsShareCard(
    context: Context,
    card: LyricsShareRequest,
    story: Boolean = false,
    themeMode: LyricsCardThemeMode = LyricsCardThemeMode.ALBUM_GRADIENT,
    coverShapeName: String = "Squircle",
    playerPrimaryArgb: Int = 0xFF0077C8.toInt(),
    playerSecondaryArgb: Int = 0xFF5A48D8.toInt(),
    playerSurfaceArgb: Int = 0xFF191B24.toInt(),
): Bitmap = renderLyricsShareCardWithColors(
    context = context,
    card = card,
    story = story,
    themeMode = themeMode,
    coverShapeName = coverShapeName,
    playerPrimaryArgb = playerPrimaryArgb,
    playerSecondaryArgb = playerSecondaryArgb,
    playerSurfaceArgb = playerSurfaceArgb,
).bitmap

internal suspend fun renderLyricsShareCardWithColors(
    context: Context,
    card: LyricsShareRequest,
    story: Boolean = false,
    themeMode: LyricsCardThemeMode = LyricsCardThemeMode.ALBUM_GRADIENT,
    coverShapeName: String = "Squircle",
    playerPrimaryArgb: Int = 0xFF0077C8.toInt(),
    playerSecondaryArgb: Int = 0xFF5A48D8.toInt(),
    playerSurfaceArgb: Int = 0xFF191B24.toInt(),
): LyricsCardRenderResult = withContext(Dispatchers.Default) {
    val type = Fonts(context)
    val cover = card.artworkUrl?.let { loadBitmap(context, it) }

    val extracted = paletteOf(cover)
    val albumLead = vibrantColor(extracted.getOrElse(0) { playerPrimaryArgb })
    val albumAccent = vibrantColor(extracted.getOrElse(1) { albumLead })
    val albumDeep = darkenOrShift(extracted.getOrElse(2) { albumLead }, 0.72f)

    val appPrimary = vibrantColor(playerPrimaryArgb)
    val appSecondary = vibrantColor(playerSecondaryArgb)
    val appIsDark = ColorUtils.calculateLuminance(playerSurfaceArgb) < 0.45

    val cardStartColor: Int
    val cardEndColor: Int
    val storyBgTop: Int
    val storyBgMid: Int
    val storyBgBottom: Int

    if (themeMode == LyricsCardThemeMode.ALBUM_GRADIENT) {
        cardStartColor = albumLead
        cardEndColor = darkenOrShift(blendColors(albumLead, albumAccent, 0.55f), 0.76f)
        storyBgTop = darkenOrShift(albumLead, 0.90f)
        storyBgMid = darkenOrShift(albumAccent, 0.78f)
        storyBgBottom = albumDeep
    } else {
        cardStartColor = if (appIsDark) {
            blendColors(playerSurfaceArgb, appPrimary, 0.24f)
        } else {
            blendColors(0xFFF6F5FA.toInt(), appPrimary, 0.18f)
        }
        cardEndColor = if (appIsDark) {
            blendColors(playerSurfaceArgb, appSecondary, 0.34f)
        } else {
            blendColors(0xFFEDEBF4.toInt(), appSecondary, 0.24f)
        }
        storyBgTop = if (appIsDark) darkenOrShift(appPrimary, 0.85f) else blendColors(appPrimary, 0xFFFFFFFF.toInt(), 0.20f)
        storyBgMid = if (appIsDark) playerSurfaceArgb else blendColors(appSecondary, 0xFFF4F3FA.toInt(), 0.35f)
        storyBgBottom = if (appIsDark) darkenOrShift(appSecondary, 0.62f) else blendColors(appSecondary, 0xFFE5E2F0.toInt(), 0.25f)
    }

    val cardMidLum = ColorUtils.calculateLuminance(blendColors(cardStartColor, cardEndColor, 0.5f))
    val lightInkSurface = cardMidLum > 0.52
    val inkColor = if (lightInkSurface) 0xFF141418.toInt() else Color.WHITE
    val subInkColor = ColorUtils.setAlphaComponent(inkColor, 198)
    val subLyricColor = ColorUtils.setAlphaComponent(inkColor, 215)
    val gapInkColor = ColorUtils.setAlphaComponent(inkColor, 132)

    val cardLeft = if (story) STORY_CARD_INSET else 0f
    val cardWidth = CARD_W - cardLeft * 2f
    val cardPad = if (story) 58f else CARD_MARGIN
    val contentW = cardWidth - cardPad * 2f

    val headHeight = SMALL_COVER_SIZE + 44f
    val footerHeight = BRAND_LOGO_SIZE + 54f
    val maxCardHeight = if (story) (STORY_H - STORY_CARD_TOP_MIN * 2f) else MAX_CARD_H
    val maxLyricsRoom = (maxCardHeight - cardPad * 2f - headHeight - footerHeight).coerceAtLeast(300f)

    val safeLines = card.lines.ifEmpty {
        listOf(
            LyricsShareLine(
                text = card.song.title,
                subText = card.song.artist,
            ),
        )
    }
    val plan = fittestPlan(type, safeLines, maxLyricsRoom, contentW, inkColor, subLyricColor, gapInkColor)

    val cardH = cardPad + headHeight + plan.content + 46f + footerHeight + cardPad
    val outW = CARD_W
    val outH = if (story) STORY_H else cardH.roundToInt().coerceAtMost(MAX_CARD_H.toInt())

    val bitmap = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    if (story) {
        val storyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                outW.toFloat(),
                outH.toFloat(),
                intArrayOf(storyBgTop, storyBgMid, storyBgBottom),
                floatArrayOf(0f, 0.50f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, outW.toFloat(), outH.toFloat(), storyPaint)

        // Subtle atmospheric radial bloom replacing old hardcoded drawBackdrop
        val glowColor = if (themeMode == LyricsCardThemeMode.ALBUM_GRADIENT) albumAccent else appPrimary
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                outW * 0.25f,
                outH * 0.24f,
                outW * 0.85f,
                intArrayOf(ColorUtils.setAlphaComponent(glowColor, 120), ColorUtils.setAlphaComponent(glowColor, 0)),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, outW.toFloat(), outH.toFloat(), glowPaint)
    }

    val cardTop = if (story) ((outH - cardH) / 2f).coerceAtLeast(48f) else 0f
    val cardRect = RectF(cardLeft, cardTop, cardLeft + cardWidth, cardTop + cardH)
    val cornerRadius = if (story) 64f else 70f
    val squirclePath = continuousRoundRectPath(cardRect, radius = cornerRadius)

    if (story) {
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x48000000
            setShadowLayer(44f, 0f, 22f, 0x66000000)
        }
        canvas.drawPath(squirclePath, shadowPaint)
    }

    canvas.save()
    canvas.clipPath(squirclePath)

    // Smooth surface gradient (App Theme vs Album Gradient)
    val surfacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            cardRect.left,
            cardRect.top,
            cardRect.right,
            cardRect.bottom,
            intArrayOf(cardStartColor, cardEndColor),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
        )
    }
    canvas.drawRect(cardRect, surfacePaint)

    if (themeMode == LyricsCardThemeMode.ALBUM_GRADIENT) {
        val bloomColor = extracted.getOrElse(3) { albumAccent }
        val cardBloom = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cardRect.right - cardWidth * 0.16f,
                cardRect.top + cardH * 0.18f,
                cardWidth * 0.72f,
                intArrayOf(ColorUtils.setAlphaComponent(bloomColor, 86), ColorUtils.setAlphaComponent(bloomColor, 0)),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(cardRect, cardBloom)
    } else {
        val accentGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cardRect.left + cardWidth * 0.18f,
                cardRect.top + cardH * 0.14f,
                cardWidth * 0.70f,
                intArrayOf(ColorUtils.setAlphaComponent(appPrimary, 72), ColorUtils.setAlphaComponent(appPrimary, 0)),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(cardRect, accentGlow)
    }

    val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = ColorUtils.setAlphaComponent(inkColor, 34)
    }
    canvas.drawPath(squirclePath, rimPaint)

    // Header inside card: shaped small album cover + song title + artist
    val headerX = cardRect.left + cardPad
    val headerY = cardRect.top + cardPad
    val coverRect = RectF(headerX, headerY, headerX + SMALL_COVER_SIZE, headerY + SMALL_COVER_SIZE)
    drawShapedCover(canvas, cover, coverRect, coverShapeName, inkColor)

    val metaX = coverRect.right + 26f
    val metaRoom = (cardRect.right - cardPad - metaX).coerceAtLeast(100f)
    val titlePaint = type.body(36f, inkColor, bold = true)
    val artistPaint = type.body(28f, subInkColor, bold = false)
    val titleBaseline = coverRect.top + 48f
    val artistBaseline = coverRect.top + 88f
    canvas.drawText(ellipsised(card.song.title, titlePaint, metaRoom), metaX, titleBaseline, titlePaint)
    canvas.drawText(ellipsised(card.song.artist, artistPaint, metaRoom), metaX, artistBaseline, artistPaint)

    // Left-aligned bold lyrics body
    var y = coverRect.bottom + 44f
    for (row in plan.rows) {
        val baseline = y + row.gapBefore - row.paint.ascent() +
            if (row.isGap) row.paint.textSize * GAP_MARKER_OFFSET else 0f
        canvas.drawText(row.text, headerX, baseline, row.paint)
        y += row.gapBefore + row.height
    }

    // Bottom footer inside card: Hush transparent vector + "Hush Music"
    val footerBottom = cardRect.bottom - cardPad
    val logoTop = footerBottom - BRAND_LOGO_SIZE
    val logo = runCatching {
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_hush_transparent_logo, null)
    }.getOrNull()
    if (logo != null) {
        logo.mutate().setTint(inkColor)
        logo.setBounds(
            headerX.toInt(),
            logoTop.toInt(),
            (headerX + BRAND_LOGO_SIZE).toInt(),
            (logoTop + BRAND_LOGO_SIZE).toInt(),
        )
        logo.draw(canvas)
    }
    val brandTextX = headerX + BRAND_LOGO_SIZE + 16f
    val brandPaint = type.body(31f, ColorUtils.setAlphaComponent(inkColor, 235), bold = true)
    val brandBaseline = logoTop + BRAND_LOGO_SIZE * 0.74f
    canvas.drawText(BRAND, brandTextX, brandBaseline, brandPaint)

    canvas.restore()

    LyricsCardRenderResult(
        bitmap = bitmap,
        topHexColor = toHexColor(if (story) storyBgTop else cardStartColor),
        bottomHexColor = toHexColor(if (story) storyBgBottom else cardEndColor),
    )
}

/**
 * Draws an Apple-style continuous corner squircle into an [android.graphics.Path]
 * matching Compose `ContinuousCornerShape(smoothing = 0.607f)`.
 */
private fun continuousRoundRectPath(
    rect: RectF,
    radius: Float,
    smoothing: Float = 0.607f,
): Path {
    val path = Path()
    val w = rect.width()
    val h = rect.height()
    val r = radius.coerceAtMost(min(w, h) / 2f)
    if (r <= 0f) {
        path.addRect(rect, Path.Direction.CW)
        return path
    }
    val l = rect.left
    val t = rect.top
    val ri = rect.right
    val b = rect.bottom

    val smoothH = (smoothing * ((w - 2 * r) / (2 * r)).coerceAtLeast(0f).coerceAtMost(1f)).coerceIn(0f, smoothing)
    val smoothV = (smoothing * ((h - 2 * r) / (2 * r)).coerceAtLeast(0f).coerceAtMost(1f)).coerceIn(0f, smoothing)
    val s = min(smoothH, smoothV).coerceAtLeast(0.25f)

    val turn = Math.toRadians(45.0 * s)
    val edge = (1f + s) * r
    val arcAlong = r - r * sin(turn).toFloat()
    val arcDepth = r * (1f - cos(turn).toFloat())
    val c = r * (tan(turn / 2.0) * cos(turn)).toFloat()
    val kb = (edge - arcAlong - c) / 3f
    val ka = 2f * kb

    fun corner(
        cx: Float,
        cy: Float,
        ix: Float,
        iy: Float,
        ox: Float,
        oy: Float,
    ) {
        val centerX = cx - ix * r + ox * r
        val centerY = cy - iy * r + oy * r
        path.lineTo(cx - ix * edge, cy - iy * edge)
        val asx = cx - ix * arcAlong + ox * arcDepth
        val asy = cy - iy * arcAlong + oy * arcDepth
        path.cubicTo(
            cx - ix * (edge - ka), cy - iy * (edge - ka),
            cx - ix * (edge - ka - kb), cy - iy * (edge - ka - kb),
            asx, asy,
        )
        val aex = cx + ox * arcAlong - ix * arcDepth
        val aey = cy + oy * arcAlong - iy * arcDepth
        val startAngle = Math.toDegrees(atan2((asy - centerY).toDouble(), (asx - centerX).toDouble())).toFloat()
        val sweep = 90f - 90f * s
        path.arcTo(RectF(centerX - r, centerY - r, centerX + r, centerY + r), startAngle, sweep, false)
        path.cubicTo(
            cx + ox * (edge - ka - kb), cy + oy * (edge - ka - kb),
            cx + ox * (edge - ka), cy + oy * (edge - ka),
            cx + ox * edge, cy + oy * edge,
        )
    }

    path.moveTo(l + edge, t)
    corner(ri, t, 1f, 0f, 0f, 1f)
    corner(ri, b, 0f, 1f, -1f, 0f)
    corner(l, b, -1f, 0f, 0f, -1f)
    corner(l, t, 0f, -1f, 1f, 0f)
    path.close()
    return path
}

private fun drawShapedCover(
    canvas: Canvas,
    cover: Bitmap?,
    rect: RectF,
    shapeName: String,
    fallbackTint: Int,
) {
    val clip = clipPathForShape(shapeName, rect)
    canvas.save()
    canvas.clipPath(clip)
    if (cover != null) {
        val srcSize = min(cover.width, cover.height)
        val sx = (cover.width - srcSize) / 2
        val sy = (cover.height - srcSize) / 2
        val srcRect = android.graphics.Rect(sx, sy, sx + srcSize, sy + srcSize)
        canvas.drawBitmap(cover, srcRect, rect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
    } else {
        val fallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ColorUtils.setAlphaComponent(fallbackTint, 36)
        }
        canvas.drawRect(rect, fallbackPaint)
    }
    canvas.restore()
}

private fun clipPathForShape(shapeName: String, rect: RectF): Path {
    val path = Path()
    val w = rect.width()
    val h = rect.height()
    val cx = rect.centerX()
    val cy = rect.centerY()
    val r = min(w, h) / 2f

    when (shapeName) {
        "Squircle" -> return continuousRoundRectPath(rect, w * 0.26f)
        "Rounded" -> path.addRoundRect(rect, w * 0.14f, h * 0.14f, Path.Direction.CW)
        "Circle" -> path.addOval(rect, Path.Direction.CW)
        "Pill" -> path.addRoundRect(rect, r, r, Path.Direction.CW)
        "Puffy" -> {
            val rBig = w * 0.36f
            val rSmall = w * 0.16f
            path.addRoundRect(
                rect,
                floatArrayOf(rBig, rBig, rSmall, rSmall, rBig, rBig, rSmall, rSmall),
                Path.Direction.CW,
            )
        }
        "Heart" -> {
            path.moveTo(rect.left + w * 0.5f, rect.top + h * 0.88f)
            path.cubicTo(
                rect.left + w * 0.14f, rect.top + h * 0.65f,
                rect.left + w * 0.04f, rect.top + h * 0.42f,
                rect.left + w * 0.06f, rect.top + h * 0.28f,
            )
            path.cubicTo(
                rect.left + w * 0.08f, rect.top + h * 0.12f,
                rect.left + w * 0.24f, rect.top + h * 0.07f,
                rect.left + w * 0.37f, rect.top + h * 0.12f,
            )
            path.cubicTo(
                rect.left + w * 0.44f, rect.top + h * 0.15f,
                rect.left + w * 0.48f, rect.top + h * 0.21f,
                rect.left + w * 0.50f, rect.top + h * 0.25f,
            )
            path.cubicTo(
                rect.left + w * 0.52f, rect.top + h * 0.21f,
                rect.left + w * 0.56f, rect.top + h * 0.15f,
                rect.left + w * 0.63f, rect.top + h * 0.12f,
            )
            path.cubicTo(
                rect.left + w * 0.76f, rect.top + h * 0.07f,
                rect.left + w * 0.92f, rect.top + h * 0.12f,
                rect.left + w * 0.94f, rect.top + h * 0.28f,
            )
            path.cubicTo(
                rect.left + w * 0.96f, rect.top + h * 0.42f,
                rect.left + w * 0.86f, rect.top + h * 0.65f,
                rect.left + w * 0.50f, rect.top + h * 0.88f,
            )
            path.close()
        }
        "8-Leaf Clover", "Wavy Octagon" -> addFlowerPath(path, cx, cy, r, lobes = 8, innerRatio = 0.82f)
        "Flower", "12-Lobe Scallop" -> addFlowerPath(path, cx, cy, r, lobes = 12, innerRatio = 0.85f)
        "Sunny", "Sunny Radiance" -> addFlowerPath(path, cx, cy, r, lobes = 16, innerRatio = 0.88f)
        "4-Sided Cookie", "4-Lobe Clover" -> addFlowerPath(path, cx, cy, r, lobes = 4, innerRatio = 0.76f)
        "Starburst" -> addFlowerPath(path, cx, cy, r, lobes = 6, innerRatio = 0.70f)
        "Hexagon" -> addPolygonPath(path, cx, cy, r, sides = 6)
        "Gem", "Diamond" -> addPolygonPath(path, cx, cy, r, sides = 4)
        else -> return continuousRoundRectPath(rect, w * 0.24f)
    }
    return path
}

private fun addFlowerPath(path: Path, cx: Float, cy: Float, radius: Float, lobes: Int, innerRatio: Float) {
    val steps = lobes * 14
    for (i in 0..steps) {
        val theta = (2.0 * PI * i) / steps
        val wave = 0.5f * (1f + cos(lobes * theta).toFloat())
        val rad = radius * (innerRatio + (1f - innerRatio) * wave)
        val x = cx + rad * cos(theta).toFloat()
        val y = cy + rad * sin(theta).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
}

private fun addPolygonPath(path: Path, cx: Float, cy: Float, radius: Float, sides: Int) {
    val startAngle = -PI / 2.0
    for (i in 0 until sides) {
        val theta = startAngle + (2.0 * PI * i) / sides
        val x = cx + radius * cos(theta).toFloat()
        val y = cy + radius * sin(theta).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
}

private fun toHexColor(argb: Int): String = String.format("#%06X", argb and 0x00FFFFFF)

private fun vibrantColor(color: Int): Int {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color, hsl)
    hsl[1] = hsl[1].coerceIn(0.45f, 0.90f)
    hsl[2] = hsl[2].coerceIn(0.28f, 0.54f)
    return ColorUtils.HSLToColor(hsl)
}

private fun darkenOrShift(color: Int, factor: Float): Int {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color, hsl)
    hsl[2] = (hsl[2] * factor).coerceIn(0.14f, 0.72f)
    return ColorUtils.HSLToColor(hsl)
}

private fun blendColors(c1: Int, c2: Int, ratio: Float): Int = ColorUtils.blendARGB(c1, c2, ratio.coerceIn(0f, 1f))

private class Row(
    val text: String,
    val paint: Paint,
    val height: Float,
    val gapBefore: Float,
    val isGap: Boolean = false,
)

private class Plan(val rows: List<Row>, val ascent: Float) {
    val content: Float
        get() = ascent + rows.fold(0f) { total, row -> total + row.gapBefore + row.height }
}

private fun fittestPlan(
    type: Fonts,
    lines: List<LyricsShareLine>,
    room: Float,
    width: Float,
    inkColor: Int,
    subInkColor: Int,
    gapColor: Int,
): Plan {
    for (size in FIT_LADDER) {
        val plan = planAt(type, lines, size, width, inkColor, subInkColor, gapColor)
        if (plan.content <= room) return plan
    }
    return planAt(type, lines, FIT_LADDER.last(), width, inkColor, subInkColor, gapColor)
}

private fun planAt(
    type: Fonts,
    lines: List<LyricsShareLine>,
    size: Float,
    width: Float,
    inkColor: Int,
    subInkColor: Int,
    gapColor: Int,
): Plan {
    val primary = type.heading(size, inkColor).apply { letterSpacing = TRACKING }
    val subSize = (size * SUB_RATIO).coerceAtLeast(SUB_FLOOR)
    val secondary = type.body(subSize, subInkColor, bold = false).apply { letterSpacing = SUB_TRACKING }

    val rows = mutableListOf<Row>()
    lines.forEachIndexed { index, line ->
        if (line.isGap) {
            val marker = type.body(size * 0.65f, gapColor)
            val markerHeight = marker.descent() - marker.ascent()
            rows += Row("(...)", marker, markerHeight, 0f, isGap = true)
            return@forEachIndexed
        }
        var firstOfLine = true
        fun add(text: String, paint: Paint, height: Float) {
            val gap = when {
                rows.isEmpty() -> 0f
                firstOfLine && index > 0 -> size * 0.42f
                else -> 0f
            }
            rows += Row(text, paint, height, gap)
            firstOfLine = false
        }
        wrap(line.text, primary, width).forEach { add(it, primary, size * LEAD) }
        val sub = line.subText
        if (!sub.isNullOrBlank() && sub != line.text) {
            wrap(sub, secondary, width).forEach { add(it, secondary, subSize * SUB_LEAD) }
        }
    }
    return Plan(rows, -primary.ascent())
}

private fun wrap(text: String, paint: Paint, width: Float): List<String> {
    val source = text.trim()
    if (source.isEmpty()) return listOf("")

    val out = mutableListOf<String>()
    var rest = source
    while (rest.isNotEmpty()) {
        val fit = paint.breakText(rest, true, width, null)
        if (fit <= 0) {
            out += rest
            break
        }
        val space = rest.lastIndexOf(' ', fit - 1)
        val take = if (space > 0 && fit < rest.length) space else fit
        if (take <= 0) {
            out += rest
            break
        }
        out += rest.substring(0, take).trimEnd()
        rest = rest.substring(take).trimStart()
    }
    return out.filter { it.isNotEmpty() }.ifEmpty { listOf("") }
}

private const val CARD_W = 1080
private const val STORY_H = 1920
private const val MAX_CARD_H = 1920f
private const val STORY_CARD_INSET = 86f
private const val STORY_CARD_TOP_MIN = 210f

private const val CARD_MARGIN = 64f
private const val SMALL_COVER_SIZE = 108f
private const val BRAND_LOGO_SIZE = 44f
private const val BRAND = "Hush Music"

private const val SUB_FLOOR = 24f
private const val GAP_MARKER_OFFSET = 0.18f

private val FIT_LADDER = floatArrayOf(72f, 64f, 56f, 50f, 44f, 38f, 34f, 30f)

private const val TRACKING = -0.65f / 34f
private const val SUB_TRACKING = -0.5f / 20f
private const val LEAD = 40f / 34f
private const val SUB_LEAD = 25f / 20f
private const val SUB_RATIO = 20f / 34f
