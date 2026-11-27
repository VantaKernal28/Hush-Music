package com.music.bitchord.download

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.EqualizerMode
import com.music.bitchord.playback.EqualizerProcessor
import com.music.bitchord.playback.audio.AudioBlock
import com.music.bitchord.playback.manualCurve
import com.music.bitchord.playback.toneCurve
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.coroutineContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.tanh

/**
 * Offline renderer that bakes Remix Studio settings (speed, pitch, active EQ,
 * and optional 2-song Deck A + Deck B mix levels) into a lossless 16-bit PCM WAV
 * file saved directly into public `Music/Hush Remixes`.
 */
object RemixExporter {

    private const val TARGET_SAMPLE_RATE = 44100
    private const val TARGET_CHANNELS = 2
    private const val TIMEOUT_US = 10_000L

    suspend fun exportRemixToDevice(
        context: Context,
        primarySong: Song,
        secondTrack: Song?,
        speed: Float,
        pitch: Float,
        deckAVolume: Float,
        deckBVolume: Float,
        deckBSpeed: Float,
        deckBPitch: Float,
        onProgress: (Float) -> Unit,
    ): Result<String> = withContext(Dispatchers.IO) {
        var tempA: LocalFileSaver.TempAudio? = null
        var tempB: LocalFileSaver.TempAudio? = null
        val outWav = File(context.cacheDir, "remix_out_${System.nanoTime()}.wav")
        try {
            onProgress(0.05f)
            tempA = LocalFileSaver.materializeAudioToTempFile(context, primarySong) { w, t ->
                if (t > 0) onProgress(0.05f + 0.25f * (w.toFloat() / t.toFloat()).coerceIn(0f, 1f))
            }
            if (secondTrack != null) {
                tempB = LocalFileSaver.materializeAudioToTempFile(context, secondTrack) { w, t ->
                    if (t > 0) onProgress(0.30f + 0.15f * (w.toFloat() / t.toFloat()).coerceIn(0f, 1f))
                }
            }

            onProgress(0.48f)
            val pcmA = decodeAndProcessDeck(
                file = tempA.file,
                speed = speed.coerceIn(0.5f, 1.5f),
                pitch = pitch.coerceIn(0.5f, 1.5f),
                applyEqualizer = true,
            )
            coroutineContext.ensureActive()
            onProgress(0.70f)

            val pcmB = if (tempB != null) {
                decodeAndProcessDeck(
                    file = tempB.file,
                    speed = deckBSpeed.coerceIn(0.5f, 1.5f),
                    pitch = deckBPitch.coerceIn(0.5f, 1.5f),
                    applyEqualizer = false,
                )
            } else {
                null
            }
            coroutineContext.ensureActive()
            onProgress(0.84f)

            val totalSamples = pcmA.size
            RandomAccessFile(outWav, "rw").use { raf ->
                writeWavHeader(raf, 0L, TARGET_SAMPLE_RATE, TARGET_CHANNELS)
                val chunkBytes = ByteArray(16384 * 2)
                var pos = 0
                while (pos < totalSamples) {
                    coroutineContext.ensureActive()
                    val count = min(16384, totalSamples - pos)
                    var bi = 0
                    for (i in 0 until count) {
                        val a = pcmA[pos + i] * deckAVolume
                        val b = if (pcmB != null && (pos + i) < pcmB.size) pcmB[pos + i] * deckBVolume else 0f
                        val mixed = if (pcmB != null) tanh((a + b).toDouble()).toFloat() else a.coerceIn(-1f, 1f)
                        val s = (mixed.coerceIn(-1f, 1f) * 32767f).roundToInt().toShort()
                        chunkBytes[bi++] = (s.toInt() and 0xFF).toByte()
                        chunkBytes[bi++] = ((s.toInt() shr 8) and 0xFF).toByte()
                    }
                    raf.write(chunkBytes, 0, bi)
                    pos += count
                }
                val dataBytes = totalSamples.toLong() * 2L
                raf.seek(0)
                writeWavHeader(raf, dataBytes, TARGET_SAMPLE_RATE, TARGET_CHANNELS)
            }

            onProgress(0.94f)
            val baseStem = if (secondTrack != null) {
                "${primarySong.title} x ${secondTrack.title} (Hush Remix)"
            } else {
                "${primarySong.title} (Hush Remix)"
            }
            val fakeRemixSong = primarySong.copy(title = baseStem)
            val displayName = DownloadStore.fileNameFor(fakeRemixSong, "wav")
            LocalFileSaver.writeFileToPublicMusic(
                context = context,
                sourceFile = outWav,
                displayName = displayName,
                mimeType = "audio/x-wav",
                subfolder = "Hush Remixes",
            )
            onProgress(1f)
            Result.success(displayName)
        } catch (e: Throwable) {
            Result.failure(e)
        } finally {
            outWav.delete()
            if (tempA?.deleteAfterUse == true) tempA.file.delete()
            if (tempB?.deleteAfterUse == true) tempB.file.delete()
        }
    }

    private fun decodeAndProcessDeck(
        file: File,
        speed: Float,
        pitch: Float,
        applyEqualizer: Boolean,
    ): FloatArray {
        val raw = decodeToNormalizedStereo44k(file)
        if (raw.isEmpty()) return raw

        val processed = resampleSpeedAndPitch(raw, speed = speed, pitch = pitch)
        if (!applyEqualizer || !AppSettings.equalizerEnabled.value) return processed

        val eq = EqualizerProcessor()
        val curve = when (AppSettings.equalizerMode.value) {
            EqualizerMode.DYNAMIC -> toneCurve(
                x = AppSettings.equalizerToneX.value,
                y = AppSettings.equalizerToneY.value,
                focused = AppSettings.equalizerFocused.value,
            )
            EqualizerMode.MANUAL -> manualCurve(AppSettings.equalizerBands.value)
        }
        eq.configure(TARGET_SAMPLE_RATE, TARGET_CHANNELS)
        eq.setTuning(true, curve, AppSettings.equalizerBalance.value)

        val blockFrames = AudioBlock.DEFAULT_CAPACITY_FRAMES
        val block = AudioBlock(channelCount = TARGET_CHANNELS, capacityFrames = blockFrames)
        val totalFrames = processed.size / TARGET_CHANNELS
        var frameOffset = 0
        while (frameOffset < totalFrames) {
            val frames = min(blockFrames, totalFrames - frameOffset)
            val sampleOffset = frameOffset * TARGET_CHANNELS
            val sampleCount = frames * TARGET_CHANNELS
            System.arraycopy(processed, sampleOffset, block.samples, 0, sampleCount)
            block.setFrameCount(frames)
            eq.process(block)
            System.arraycopy(block.samples, 0, processed, sampleOffset, sampleCount)
            frameOffset += frames
        }
        return processed
    }

    private fun decodeToNormalizedStereo44k(file: File): FloatArray {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        try {
            extractor.setDataSource(file.absolutePath)
            var audioTrackIndex = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                val mime = f.getString(MediaFormat.KEY_MIME).orEmpty()
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    format = f
                    break
                }
            }
            if (audioTrackIndex < 0 || format == null) return FloatArray(0)
            extractor.selectTrack(audioTrackIndex)

            val mime = format.getString(MediaFormat.KEY_MIME)!!
            var srcRate = if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) format.getInteger(MediaFormat.KEY_SAMPLE_RATE) else TARGET_SAMPLE_RATE
            var srcChannels = if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 2

            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            val chunks = ArrayList<FloatArray>()
            var totalStereoSamples = 0

            while (!outputDone) {
                if (!inputDone) {
                    val inIdx = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inIdx >= 0) {
                        val buf = codec.getInputBuffer(inIdx)!!
                        val read = extractor.readSampleData(buf, 0)
                        if (read < 0) {
                            codec.queueInputBuffer(inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(inIdx, 0, read, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }
                val outIdx = codec.dequeueOutputBuffer(info, TIMEOUT_US)
                when {
                    outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val outFmt = codec.outputFormat
                        if (outFmt.containsKey(MediaFormat.KEY_SAMPLE_RATE)) srcRate = outFmt.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        if (outFmt.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) srcChannels = outFmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    }
                    outIdx >= 0 -> {
                        val outBuf = codec.getOutputBuffer(outIdx)
                        if (outBuf != null && info.size > 0) {
                            outBuf.position(info.offset)
                            outBuf.limit(info.offset + info.size)
                            val shortCount = info.size / 2
                            val shorts = ShortArray(shortCount)
                            outBuf.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
                            val frames = shortCount / srcChannels.coerceAtLeast(1)
                            val stereo = FloatArray(frames * 2)
                            for (f in 0 until frames) {
                                val l = shorts[f * srcChannels] / 32768f
                                val r = if (srcChannels > 1) shorts[f * srcChannels + 1] / 32768f else l
                                stereo[f * 2] = l
                                stereo[f * 2 + 1] = r
                            }
                            chunks.add(stereo)
                            totalStereoSamples += stereo.size
                        }
                        codec.releaseOutputBuffer(outIdx, false)
                        if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) outputDone = true
                    }
                }
            }

            val joined = FloatArray(totalStereoSamples)
            var offset = 0
            for (c in chunks) {
                System.arraycopy(c, 0, joined, offset, c.size)
                offset += c.size
            }
            if (srcRate == TARGET_SAMPLE_RATE) return joined

            val rateRatio = srcRate.toDouble() / TARGET_SAMPLE_RATE.toDouble()
            return linearResampleStereo(joined, step = rateRatio)
        } finally {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            runCatching { extractor.release() }
        }
    }

    private fun resampleSpeedAndPitch(
        stereo: FloatArray,
        speed: Float,
        pitch: Float,
    ): FloatArray {
        if (kotlin.math.abs(speed - 1f) < 0.005f && kotlin.math.abs(pitch - 1f) < 0.005f) {
            return stereo
        }
        // First apply pitch resampling; then time-stretch remaining factor (speed / pitch) using Hann OLA
        val pitched = if (kotlin.math.abs(pitch - 1f) >= 0.005f) {
            linearResampleStereo(stereo, step = pitch.toDouble())
        } else {
            stereo
        }
        val tempoFactor = (speed / pitch).toDouble()
        if (kotlin.math.abs(tempoFactor - 1.0) < 0.01) return pitched

        val srcFrames = pitched.size / 2
        val winSize = 1024
        val hopOut = winSize / 2
        val hopIn = (hopOut * tempoFactor).coerceAtLeast(64.0)
        val estOutFrames = ((srcFrames / tempoFactor).toInt() + winSize).coerceAtLeast(1)
        val out = FloatArray(estOutFrames * 2)
        val hann = FloatArray(winSize) { i ->
            (0.5 * (1.0 - cos(2.0 * PI * i / (winSize - 1)))).toFloat()
        }

        var inPos = 0.0
        var outFrame = 0
        while (inPos.toInt() + winSize < srcFrames && outFrame + winSize < estOutFrames) {
            val baseIn = inPos.toInt()
            for (i in 0 until winSize) {
                val w = hann[i]
                val sIdx = (baseIn + i) * 2
                val dIdx = (outFrame + i) * 2
                out[dIdx] += pitched[sIdx] * w
                out[dIdx + 1] += pitched[sIdx + 1] * w
            }
            inPos += hopIn
            outFrame += hopOut
        }
        return out.copyOf((outFrame * 2).coerceAtMost(out.size))
    }

    private fun linearResampleStereo(input: FloatArray, step: Double): FloatArray {
        val srcFrames = input.size / 2
        if (srcFrames < 2) return input
        val dstFrames = ((srcFrames - 1) / step).toInt().coerceAtLeast(1)
        val out = FloatArray(dstFrames * 2)
        var pos = 0.0
        for (i in 0 until dstFrames) {
            val idx0 = pos.toInt().coerceIn(0, srcFrames - 2)
            val frac = (pos - idx0).toFloat()
            val s0 = idx0 * 2
            val s1 = s0 + 2
            out[i * 2] = input[s0] + (input[s1] - input[s0]) * frac
            out[i * 2 + 1] = input[s0 + 1] + (input[s1 + 1] - input[s0 + 1]) * frac
            pos += step
        }
        return out
    }

    private fun writeWavHeader(
        raf: RandomAccessFile,
        dataLength: Long,
        sampleRate: Int,
        channels: Int,
    ) {
        val byteRate = sampleRate * channels * 2
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray(Charsets.US_ASCII))
        header.putInt((36L + dataLength).toInt())
        header.put("WAVE".toByteArray(Charsets.US_ASCII))
        header.put("fmt ".toByteArray(Charsets.US_ASCII))
        header.putInt(16)
        header.putShort(1.toShort())
        header.putShort(channels.toShort())
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort((channels * 2).toShort())
        header.putShort(16.toShort())
        header.put("data".toByteArray(Charsets.US_ASCII))
        header.putInt(dataLength.toInt())
        raf.write(header.array())
    }
}
