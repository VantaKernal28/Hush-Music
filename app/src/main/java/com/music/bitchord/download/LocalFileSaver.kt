package com.music.bitchord.download

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.music.bitchord.data.innertube.StreamResolver
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.sources.SourceRegistry
import com.music.bitchord.data.sources.SourceResolver
import com.music.bitchord.playback.AudioCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.Locale

/**
 * Direct local file export helper for tracks, remixes, and playlists.
 * Writes directly to public `Music/Hush Music` (or subfolder) via MediaStore.
 */
object LocalFileSaver {

    data class TempAudio(
        val file: File,
        val extension: String,
        val mimeType: String,
        val deleteAfterUse: Boolean = true,
    )

    fun mimeForExtension(ext: String): String = when (ext.lowercase(Locale.ROOT).trim('.')) {
        "flac" -> "audio/flac"
        "wav", "wave" -> "audio/x-wav"
        "mp3" -> "audio/mpeg"
        "ogg", "opus" -> "audio/ogg"
        "aac" -> "audio/aac"
        else -> "audio/mp4"
    }

    fun extensionFromMime(mime: String?): String = when {
        mime == null -> "m4a"
        "flac" in mime -> "flac"
        "wav" in mime -> "wav"
        "mpeg" in mime || "mp3" in mime -> "mp3"
        "ogg" in mime || "opus" in mime -> "opus"
        "aac" in mime -> "aac"
        else -> "m4a"
    }

    suspend fun saveTrackToPublicMusic(
        context: Context,
        song: Song,
        subfolder: String = DownloadStore.FOLDER,
        onProgress: (Float) -> Unit = {},
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val temp = materializeAudioToTempFile(context, song) { written, total ->
                if (total > 0L) onProgress((written.toFloat() / total.toFloat()).coerceIn(0f, 0.9f))
            }
            try {
                val safeExt = if (temp.extension == "webm") "m4a" else temp.extension
                val displayName = DownloadStore.fileNameFor(song, safeExt)
                val finalMime = mimeForExtension(safeExt)
                writeFileToPublicMusic(
                    context = context,
                    sourceFile = temp.file,
                    displayName = displayName,
                    mimeType = finalMime,
                    subfolder = subfolder,
                )
                onProgress(1f)
                displayName
            } finally {
                if (temp.deleteAfterUse) temp.file.delete()
            }
        }
    }

    suspend fun savePlaylistToPublicMusic(
        context: Context,
        playlistTitle: String,
        songs: List<Song>,
        onTrackDone: (done: Int, total: Int) -> Unit = { _, _ -> },
    ): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val cleanFolder = playlistTitle
            .replace(Regex("""[\\/:*?"<>|\x00-\x1F]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .take(60)
            .ifBlank { "Playlist" }
        val targetSubfolder = "${DownloadStore.FOLDER}/$cleanFolder"
        var savedCount = 0
        songs.forEachIndexed { idx, song ->
            val result = saveTrackToPublicMusic(context, song, subfolder = targetSubfolder)
            if (result.isSuccess) savedCount++
            onTrackDone(idx + 1, songs.size)
        }
        savedCount to songs.size
    }

    suspend fun materializeAudioToTempFile(
        context: Context,
        song: Song,
        onProgress: (written: Long, total: Long) -> Unit = { _, _ -> },
    ): TempAudio = withContext(Dispatchers.IO) {
        val tempDir = File(context.cacheDir, "local_export_tmp").apply { mkdirs() }

        // 1. Existing local file or saved internal download
        val existingUri = song.localUri?.takeIf { it.isNotBlank() }?.let(Uri::parse)
            ?: Downloads.savedUri(context, song.videoId)
        if (existingUri != null) {
            copyUriToTemp(context, existingUri, tempDir, song.videoId)?.let { return@withContext it }
        }

        // 2. Complete cached copy in ExoPlayer cache
        val cached = AudioCache.completeCopyOf(song.videoId)
        if (cached != null) {
            val outFile = File(tempDir, "cache_${song.videoId}_${System.nanoTime()}.${cached.extension}")
            try {
                outFile.outputStream().use { out ->
                    AudioCache.copyOut(cached, out, onProgress)
                }
                if (outFile.length() > 0L) {
                    return@withContext TempAudio(outFile, cached.extension, cached.mimeType)
                }
                outFile.delete()
            } catch (_: Throwable) {
                outFile.delete()
            }
        }

        // 3. Addon source track
        val addonKey = SourceRegistry.parseTrackKey(song.videoId)
        if (addonKey != null) {
            val addonUri = Uri.parse(SourceRegistry.trackUri(addonKey.first, addonKey.second))
            val stream = SourceResolver.resolve(addonUri)
            if (stream != null) {
                val ext = extensionFromMime(stream.format.codec)
                val outFile = File(tempDir, "addon_${song.videoId.hashCode()}_${System.nanoTime()}.$ext")
                try {
                    outFile.outputStream().use { out ->
                        Downloader.fetchDirect(stream.url, stream.headers, out, onProgress)
                    }
                    return@withContext TempAudio(outFile, ext, mimeForExtension(ext))
                } catch (e: Throwable) {
                    outFile.delete()
                    throw e
                }
            }
        }

        // 4. YouTube Music download stream (m4a preferred for MediaStore/AudioExtractor compatibility)
        val quality = AppSettings.downloadQuality.value
        val stream = StreamResolver.resolveForDownload(song.videoId, quality.maxKbps, requireM4a = true)
        val outFile = File(tempDir, "yt_${song.videoId}_${System.nanoTime()}.${stream.downloadExtension}")
        try {
            outFile.outputStream().use { out ->
                Downloader.fetch(song.videoId, stream, quality.maxKbps, requireM4a = true, out, onProgress)
            }
            TempAudio(outFile, stream.downloadExtension, stream.downloadMimeType)
        } catch (e: Throwable) {
            outFile.delete()
            throw e
        }
    }

    private fun copyUriToTemp(
        context: Context,
        uri: Uri,
        tempDir: File,
        videoId: String,
    ): TempAudio? = runCatching {
        val mime = context.contentResolver.getType(uri)
        val extFromUri = uri.lastPathSegment
            ?.substringAfterLast('.', "")
            ?.takeIf { it.length in 2..5 }
            ?.lowercase(Locale.ROOT)
        val ext = extFromUri ?: extensionFromMime(mime)
        if (ext == "m3u8" || ext == "webm") return null

        val outFile = File(tempDir, "local_${videoId.hashCode()}_${System.nanoTime()}.$ext")
        val input: InputStream = if (uri.scheme == "file") {
            File(requireNotNull(uri.path)).inputStream()
        } else {
            context.contentResolver.openInputStream(uri) ?: return null
        }
        input.use { src ->
            outFile.outputStream().use { dst ->
                src.copyTo(dst)
            }
        }
        if (outFile.length() == 0L) {
            outFile.delete()
            return null
        }
        TempAudio(outFile, ext, mime ?: mimeForExtension(ext))
    }.getOrNull()

    fun writeFileToPublicMusic(
        context: Context,
        sourceFile: File,
        displayName: String,
        mimeType: String,
        subfolder: String = DownloadStore.FOLDER,
    ): Uri {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val relPath = "${Environment.DIRECTORY_MUSIC}/$subfolder"
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relPath)
                put(MediaStore.Audio.Media.IS_MUSIC, 1)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val itemUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Could not create MediaStore entry for $displayName")
            try {
                resolver.openOutputStream(itemUri)?.use { out ->
                    sourceFile.inputStream().use { src -> src.copyTo(out) }
                } ?: error("Could not open MediaStore stream for $displayName")
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
                return itemUri
            } catch (e: Throwable) {
                resolver.delete(itemUri, null, null)
                throw e
            }
        } else {
            @Suppress("DEPRECATION")
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                subfolder,
            ).apply { mkdirs() }
            val target = File(dir, displayName)
            sourceFile.inputStream().use { src ->
                target.outputStream().use { out -> src.copyTo(out) }
            }
            MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), arrayOf(mimeType), null)
            return Uri.fromFile(target)
        }
    }
}
