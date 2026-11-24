package com.music.bitchord.ui.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * Real-time audio beat-spike energy tracker driven by [DspChain] PCM analysis.
 *
 * Computes root-mean-square (RMS) audio energy on the playback audio thread,
 * tracks a dynamic exponential moving average baseline, and fires an impulse
 * when a genuine transient energy spike occurs with a minimum refractory period (>= 220ms).
 */
object AudioBeatDetector {
    private val _beatTimestamp = MutableStateFlow(0L)
    val beatTimestamp: StateFlow<Long> = _beatTimestamp.asStateFlow()

    private val _beatEnergy = MutableStateFlow(0f)
    val beatEnergy: StateFlow<Float> = _beatEnergy.asStateFlow()

    @Volatile
    private var rollingBaseline = 0.04f

    @Volatile
    private var lastBeatMs = 0L

    /**
     * Called from the audio processing thread with valid interleaved Float32 PCM samples.
     * Zero-allocation and lock-free for smooth 120Hz audio pipeline execution.
     */
    fun processSamples(samples: FloatArray, count: Int) {
        if (count <= 0) return

        var sumSq = 0.0
        val step = if (count > 512) count / 256 else 1
        var evaluated = 0
        var i = 0
        while (i < count) {
            val s = samples[i]
            sumSq += (s * s)
            evaluated++
            i += step
        }
        if (evaluated == 0) return

        val rms = sqrt(sumSq / evaluated).toFloat()
        val currentBaseline = rollingBaseline
        // Smooth exponential moving average baseline
        val newBaseline = currentBaseline * 0.94f + rms * 0.06f
        rollingBaseline = newBaseline

        val now = System.currentTimeMillis()
        // Threshold: RMS must exceed baseline by 30% and exceed absolute noise floor
        if (rms > 0.045f && rms > currentBaseline * 1.30f && (now - lastBeatMs) >= 220L) {
            lastBeatMs = now
            val energy = ((rms - currentBaseline) / (currentBaseline.coerceAtLeast(0.01f))).coerceIn(0.5f, 1.0f)
            _beatEnergy.value = energy
            _beatTimestamp.value = now
        }
    }

    fun reset() {
        rollingBaseline = 0.04f
        lastBeatMs = 0L
        _beatEnergy.value = 0f
        _beatTimestamp.value = 0L
    }
}
