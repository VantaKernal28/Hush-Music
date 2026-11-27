package com.music.bitchord.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.music.bitchord.MainActivity
import com.music.bitchord.R
import com.music.bitchord.playback.PlayerDeepLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vintage Analog Cassette Tape Widget (4x2), inspired by classic stereo decks and Japanese Lo-Fi aesthetic.
 * Features realistic tape spool reels, magnetic tape window, label badge, subtitle notes card,
 * and physical deck buttons (Rewind, Iconic Terracotta Orange Play/Pause, Fast-Forward).
 */
class CassetteWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        renderAsync(context, ids)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        id: Int,
        newOptions: Bundle?,
    ) {
        renderAsync(context, intArrayOf(id))
    }

    private fun renderAsync(context: Context, ids: IntArray) {
        val pending = goAsync()
        val app = context.applicationContext
        scope.launch {
            try {
                render(app, ids)
            } finally {
                runCatching { pending.finish() }
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        fun refresh(context: Context) {
            val app = context.applicationContext
            scope.launch {
                val manager = runCatching { AppWidgetManager.getInstance(app) }.getOrNull() ?: return@launch
                val ids = runCatching {
                    manager.getAppWidgetIds(ComponentName(app, CassetteWidget::class.java))
                }.getOrNull() ?: return@launch
                if (ids.isNotEmpty()) {
                    render(app, ids)
                }
            }
        }

        private suspend fun render(context: Context, ids: IntArray) {
            val manager = runCatching { AppWidgetManager.getInstance(context) }.getOrNull() ?: return
            val snapshot = MediaWidgetSnapshot.load(context)

            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_cassette)

                // Render the complete retro analog cassette bitmap
                val art = drawCassetteBitmap(
                    widthPx = 800,
                    heightPx = 460,
                    title = if (snapshot.hasTrack) snapshot.title else "Hush Tape Deck C-90",
                    artist = if (snapshot.hasTrack) snapshot.artist else "Analog Sound System",
                    isPlaying = snapshot.isPlaying,
                )
                views.setImageViewBitmap(R.id.widget_cassette_art, art)

                // Transport deck buttons
                views.setOnClickPendingIntent(
                    R.id.widget_cassette_prev,
                    MediaWidgetActions.pendingIntent(context, MediaWidgetActions.ACTION_PREVIOUS),
                )
                views.setOnClickPendingIntent(
                    R.id.widget_cassette_toggle,
                    MediaWidgetActions.pendingIntent(context, MediaWidgetActions.ACTION_TOGGLE),
                )
                views.setOnClickPendingIntent(
                    R.id.widget_cassette_next,
                    MediaWidgetActions.pendingIntent(context, MediaWidgetActions.ACTION_NEXT),
                )

                // Update play/pause glyph
                views.setImageViewResource(
                    R.id.widget_cassette_toggle,
                    if (snapshot.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play,
                )

                // Tap background opens app
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                }
                val openPi = PendingIntent.getActivity(
                    context,
                    id,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                views.setOnClickPendingIntent(R.id.widget_cassette_art, openPi)

                manager.updateAppWidget(id, views)
            }
        }

        /**
         * Draws the authentic Japanese Lo-Fi cassette tape artwork.
         */
        fun drawCassetteBitmap(
            widthPx: Int,
            heightPx: Int,
            title: String,
            artist: String,
            isPlaying: Boolean,
            tapeColor: Int = Color.parseColor("#2B3340"), // Slate charcoal cassette
            cardBgColor: Int = Color.parseColor("#F5F2EB"), // Vintage cream background card
        ): Bitmap {
            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // 1. Warm cream background card with smooth rounded corners
            paint.color = cardBgColor
            canvas.drawRoundRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), 48f, 48f, paint)

            // 2. Japanese header typography ("TRACKS / 作った曲たち")
            paint.color = Color.parseColor("#3A3D42")
            paint.textSize = 24f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            canvas.drawText("TRACKS", 36f, 42f, paint)

            paint.color = Color.parseColor("#7A7E85")
            paint.textSize = 16f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("作った曲たち • Hush Analog", 146f, 40f, paint)

            // 3. The Cassette Tape Body
            val cassetteLeft = 32f
            val cassetteTop = 56f
            val cassetteRight = widthPx - 32f
            val cassetteBottom = 260f

            val cassetteRect = RectF(cassetteLeft, cassetteTop, cassetteRight, cassetteBottom)
            paint.color = tapeColor
            canvas.drawRoundRect(cassetteRect, 24f, 24f, paint)

            // Cassette inner border highlight
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.parseColor("#444F63")
            canvas.drawRoundRect(cassetteRect, 24f, 24f, paint)
            paint.style = Paint.Style.FILL

            // 4. White/Cream Label Area
            val labelRect = RectF(cassetteLeft + 20f, cassetteTop + 14f, cassetteRight - 20f, cassetteBottom - 18f)
            paint.color = Color.parseColor("#FCFAF6")
            canvas.drawRoundRect(labelRect, 16f, 16f, paint)

            // "A" side badge
            paint.color = Color.parseColor("#1E2024")
            val badgeRect = RectF(labelRect.left + 14f, labelRect.top + 10f, labelRect.left + 46f, labelRect.top + 34f)
            canvas.drawRoundRect(badgeRect, 6f, 6f, paint)
            paint.color = Color.WHITE
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("A", badgeRect.left + 10f, badgeRect.top + 18f, paint)

            // Track title on cassette label
            paint.color = Color.parseColor("#1E2024")
            paint.textSize = 19f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            val cleanTitle = if (title.length > 26) title.take(24) + "…" else title
            canvas.drawText(cleanTitle, badgeRect.right + 12f, badgeRect.top + 18f, paint)

            // 5. Center Tape Window & Spool Reels
            val windowWidth = (cassetteRight - cassetteLeft) * 0.54f
            val windowHeight = 84f
            val windowLeft = (widthPx - windowWidth) / 2f
            val windowTop = cassetteTop + 68f
            val windowRect = RectF(windowLeft, windowTop, windowLeft + windowWidth, windowTop + windowHeight)

            // Window cutout
            paint.color = Color.parseColor("#181B20")
            canvas.drawRoundRect(windowRect, 12f, 12f, paint)

            // Brown magnetic ribbon bridge
            paint.color = Color.parseColor("#422A1E")
            val ribbonRect = RectF(windowLeft + 40f, windowTop + 24f, windowLeft + windowWidth - 40f, windowTop + 60f)
            canvas.drawRect(ribbonRect, paint)

            // Left and Right Spool Reels
            val reelRadius = 28f
            val leftReelX = windowLeft + 48f
            val rightReelX = windowLeft + windowWidth - 48f
            val reelY = windowTop + windowHeight / 2f

            for (cx in listOf(leftReelX, rightReelX)) {
                // White outer reel circle
                paint.color = Color.parseColor("#F4F4F6")
                canvas.drawCircle(cx, reelY, reelRadius, paint)

                // Inner hole
                paint.color = Color.parseColor("#181B20")
                canvas.drawCircle(cx, reelY, 12f, paint)

                // 6 Reel gear teeth
                paint.color = Color.parseColor("#181B20")
                paint.strokeWidth = 3f
                paint.style = Paint.Style.STROKE
                for (i in 0 until 6) {
                    val angle = (i * 60.0) * Math.PI / 180.0
                    val x1 = (cx + 12f * cos(angle)).toFloat()
                    val y1 = (reelY + 12f * sin(angle)).toFloat()
                    val x2 = (cx + 22f * cos(angle)).toFloat()
                    val y2 = (reelY + 22f * sin(angle)).toFloat()
                    canvas.drawLine(x1, y1, x2, y2, paint)
                }
                paint.style = Paint.Style.FILL
            }

            // Cassette bottom guide screw dots
            paint.color = Color.parseColor("#7A8290")
            canvas.drawCircle(cassetteLeft + 44f, cassetteBottom - 10f, 4f, paint)
            canvas.drawCircle(cassetteRight - 44f, cassetteBottom - 10f, 4f, paint)
            canvas.drawCircle(widthPx / 2f - 40f, cassetteBottom - 10f, 4f, paint)
            canvas.drawCircle(widthPx / 2f + 40f, cassetteBottom - 10f, 4f, paint)

            // 6. Japanese notes card underneath
            val cardLeft = 32f
            val cardTop = 276f
            val cardRight = widthPx - 170f // Leaves room for the physical deck buttons on the right
            val cardBottom = heightPx - 24f

            val noteRect = RectF(cardLeft, cardTop, cardRight, cardBottom)
            paint.color = Color.parseColor("#ECE7DC")
            canvas.drawRoundRect(noteRect, 16f, 16f, paint)

            // Ruled line in notes
            paint.color = Color.parseColor("#DFD8C8")
            paint.strokeWidth = 1.5f
            canvas.drawLine(cardLeft + 16f, cardTop + 54f, cardRight - 16f, cardTop + 54f, paint)

            // Subtitle text (Japanese Lo-Fi & Artist metadata)
            paint.color = Color.parseColor("#484C54")
            paint.textSize = 15f
            paint.typeface = Typeface.DEFAULT
            val cleanArtist = if (artist.isBlank()) "Hush Hi-Fi Tape C-90" else artist
            canvas.drawText("サンプラーとシンセサイザーの演奏 • $cleanArtist", cardLeft + 16f, cardTop + 34f, paint)

            paint.color = Color.parseColor("#8E939E")
            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            canvas.drawText("2026-11 • Hi-Res Analog Output • 120Hz Deck", cardLeft + 16f, cardTop + 86f, paint)

            // Subtle website/deck tag pill at the bottom
            val tagText = "hush.music"
            paint.color = Color.parseColor("#E0DDD4")
            val tagRect = RectF(cardLeft + 16f, cardTop + 104f, cardLeft + 116f, cardTop + 130f)
            canvas.drawRoundRect(tagRect, 12f, 12f, paint)
            paint.color = Color.parseColor("#6A6E78")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            canvas.drawText(tagText, cardLeft + 30f, cardTop + 122f, paint)

            return bitmap
        }
    }
}
