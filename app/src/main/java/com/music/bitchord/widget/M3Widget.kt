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
 * Material 3 Expressive Widget (4x1 / 4x2):
 * Features squircle corners, dynamic surface container, rounded album cover artwork,
 * bold typography, and expressive M3 pill transport controls.
 */
class M3Widget : AppWidgetProvider() {

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
        private const val COVER_DP = 68f
        private const val COVER_RADIUS_DP = 16f
        private const val RENDER_TIMEOUT_MS = 6_000L

        fun refresh(context: Context) {
            val app = context.applicationContext
            scope.launch {
                val manager = runCatching { AppWidgetManager.getInstance(app) }.getOrNull() ?: return@launch
                val ids = runCatching {
                    manager.getAppWidgetIds(ComponentName(app, M3Widget::class.java))
                }.getOrNull() ?: return@launch
                if (ids.isNotEmpty()) {
                    render(app, ids)
                }
            }
        }

        private suspend fun render(context: Context, ids: IntArray) {
            val manager = runCatching { AppWidgetManager.getInstance(context) }.getOrNull() ?: return
            val snapshot = MediaWidgetSnapshot.load(context)
            val density = context.resources.displayMetrics.density
            val coverPx = (COVER_DP * density).toInt().coerceIn(48, 256)
            val radiusPx = COVER_RADIUS_DP * density
            val bg = drawM3Background(widthPx = 800, heightPx = 240)
            val coverKey = snapshot.artworkUrl

            val coverBitmap = coverKey?.let { key ->
                MediaWidgetArt.peekRoundedSquare(key, coverPx, radiusPx)
                    ?: withTimeoutOrNull(RENDER_TIMEOUT_MS) {
                        MediaWidgetArt.roundedSquare(context, key, coverPx, radiusPx, key)
                    }
            }

            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_m3)

                views.setTextViewText(
                    R.id.widget_m3_title,
                    if (snapshot.hasTrack) snapshot.title else context.getString(R.string.widget_nothing_played),
                )
                views.setTextViewText(
                    R.id.widget_m3_artist,
                    if (snapshot.hasTrack) snapshot.artist else "Hush Music",
                )

                views.setImageViewBitmap(R.id.widget_m3_art_background, bg)
                views.setInt(R.id.widget_m3_root, "setBackgroundResource", 0)

                if (coverBitmap != null) {
                    views.setImageViewBitmap(R.id.widget_m3_cover, coverBitmap)
                } else {
                    views.setImageViewResource(R.id.widget_m3_cover, R.drawable.widget_pill_art_placeholder)
                }

                views.setOnClickPendingIntent(
                    R.id.widget_m3_previous,
                    MediaWidgetActions.pendingIntent(context, MediaWidgetActions.ACTION_PREVIOUS),
                )
                views.setOnClickPendingIntent(
                    R.id.widget_m3_toggle,
                    MediaWidgetActions.pendingIntent(context, MediaWidgetActions.ACTION_TOGGLE),
                )
                views.setOnClickPendingIntent(
                    R.id.widget_m3_next,
                    MediaWidgetActions.pendingIntent(context, MediaWidgetActions.ACTION_NEXT),
                )

                views.setImageViewResource(
                    R.id.widget_m3_toggle,
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
                views.setOnClickPendingIntent(R.id.widget_m3_root, openPi)

                manager.updateAppWidget(id, views)
            }
        }

        private fun drawM3Background(widthPx: Int, heightPx: Int): Bitmap {
            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // M3 expressive container with warm Hush charcoal tonal depth
            paint.shader = LinearGradient(
                0f, 0f, widthPx.toFloat(), heightPx.toFloat(),
                intArrayOf(Color.parseColor("#242129"), Color.parseColor("#1B1921")),
                null,
                Shader.TileMode.CLAMP,
            )
            canvas.drawRoundRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), 48f, 48f, paint)
            paint.shader = null

            // Subtle tonal accent border
            paint.color = Color.argb(64, 255, 110, 64)
            paint.strokeWidth = 3f
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(RectF(1.5f, 1.5f, widthPx - 1.5f, heightPx - 1.5f), 48f, 48f, paint)

            return bitmap
        }
    }
}
