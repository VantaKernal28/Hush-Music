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
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Bundle
import android.widget.RemoteViews
import com.music.bitchord.MainActivity
import com.music.bitchord.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Hush Classic Liquid Glass Widget (4x2):
 * Features full-bleed blurred album artwork frosted backdrop, specular rim border,
 * glowing gradient, and minimalist typography.
 */
class ClassicWidget : AppWidgetProvider() {

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
        private const val RENDER_TIMEOUT_MS = 6_000L

        fun refresh(context: Context) {
            val app = context.applicationContext
            scope.launch {
                val manager = runCatching { AppWidgetManager.getInstance(app) }.getOrNull() ?: return@launch
                val ids = runCatching {
                    manager.getAppWidgetIds(ComponentName(app, ClassicWidget::class.java))
                }.getOrNull() ?: return@launch
                if (ids.isNotEmpty()) {
                    render(app, ids)
                }
            }
        }

        private suspend fun render(context: Context, ids: IntArray) {
            val manager = runCatching { AppWidgetManager.getInstance(context) }.getOrNull() ?: return
            val snapshot = MediaWidgetSnapshot.load(context)
            val widthPx = 800
            val heightPx = 360
            val bandPx = 150
            val key = snapshot.artworkUrl

            val baseBackdrop = key?.let { url ->
                MediaWidgetArt.peek(url, widthPx, heightPx, bandPx)
                    ?: withTimeoutOrNull(RENDER_TIMEOUT_MS) {
                        MediaWidgetArt.render(
                            context = context,
                            artworkUrl = url,
                            widthPx = widthPx,
                            heightPx = heightPx,
                            bandPx = bandPx,
                            key = url,
                            cornerRadiusPx = 44f,
                        )
                    }
            }
            val bg = drawClassicGlassBitmap(widthPx, heightPx, baseBackdrop)

            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_classic)

                views.setTextViewText(
                    R.id.widget_classic_title,
                    if (snapshot.hasTrack) snapshot.title else context.getString(R.string.widget_nothing_played),
                )
                views.setTextViewText(
                    R.id.widget_classic_artist,
                    if (snapshot.hasTrack) snapshot.artist else "Hush Music",
                )

                views.setImageViewBitmap(R.id.widget_classic_art, bg)
                views.setInt(R.id.widget_classic_root, "setBackgroundResource", 0)

                views.setOnClickPendingIntent(
                    R.id.widget_classic_previous,
                    MediaWidgetActions.pendingIntent(context, MediaWidgetActions.ACTION_PREVIOUS),
                )
                views.setOnClickPendingIntent(
                    R.id.widget_classic_toggle,
                    MediaWidgetActions.pendingIntent(context, MediaWidgetActions.ACTION_TOGGLE),
                )
                views.setOnClickPendingIntent(
                    R.id.widget_classic_next,
                    MediaWidgetActions.pendingIntent(context, MediaWidgetActions.ACTION_NEXT),
                )

                views.setImageViewResource(
                    R.id.widget_classic_toggle,
                    if (snapshot.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play,
                )

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
                views.setOnClickPendingIntent(R.id.widget_classic_root, openPi)

                manager.updateAppWidget(id, views)
            }
        }

        private fun drawClassicGlassBitmap(widthPx: Int, heightPx: Int, coverArt: Bitmap?): Bitmap {
            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            if (coverArt != null) {
                canvas.drawBitmap(coverArt, 0f, 0f, paint)
            } else {
                paint.shader = LinearGradient(
                    0f, 0f, 0f, heightPx.toFloat(),
                    intArrayOf(Color.parseColor("#E6181920"), Color.parseColor("#F2101116")),
                    null,
                    Shader.TileMode.CLAMP,
                )
                canvas.drawRoundRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), 44f, 44f, paint)
            }

            // Specular Liquid Glass rim highlight
            paint.shader = LinearGradient(
                0f, 0f, widthPx.toFloat(), heightPx.toFloat(),
                intArrayOf(Color.parseColor("#66FFFFFF"), Color.parseColor("#14FFFFFF"), Color.parseColor("#33FFFFFF")),
                null,
                Shader.TileMode.CLAMP,
            )
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.5f
            canvas.drawRoundRect(RectF(1.25f, 1.25f, widthPx - 1.25f, heightPx - 1.25f), 44f, 44f, paint)

            return bitmap
        }
    }
}
