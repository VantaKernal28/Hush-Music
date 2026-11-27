package com.music.bitchord.playback

import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.durationMillis
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.EqualizerMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared coordinator for Remix Studio (single-track remixing + 2-song live blend)
 * observed by [PlaybackService] and [com.music.bitchord.ui.screens.SongMixerSheet].
 */
object RemixStudioController {

    private val _activePresetId = MutableStateFlow("orig")
    val activePresetId: StateFlow<String> = _activePresetId.asStateFlow()

    /** True only when a Remix Studio preset automatically switched the Equalizer on. */
    @Volatile
    var presetAppliedEq: Boolean = false
        private set

    private val _secondTrack = MutableStateFlow<Song?>(null)
    val secondTrack: StateFlow<Song?> = _secondTrack.asStateFlow()

    private val _secondTrackPlaying = MutableStateFlow(false)
    val secondTrackPlaying: StateFlow<Boolean> = _secondTrackPlaying.asStateFlow()

    private val _secondTrackLoading = MutableStateFlow(false)
    val secondTrackLoading: StateFlow<Boolean> = _secondTrackLoading.asStateFlow()

    private val _secondTrackPositionMs = MutableStateFlow(0L)
    val secondTrackPositionMs: StateFlow<Long> = _secondTrackPositionMs.asStateFlow()

    private val _secondTrackDurationMs = MutableStateFlow(0L)
    val secondTrackDurationMs: StateFlow<Long> = _secondTrackDurationMs.asStateFlow()

    private val _deckAVolume = MutableStateFlow(1.0f)
    val deckAVolume: StateFlow<Float> = _deckAVolume.asStateFlow()

    private val _deckBVolume = MutableStateFlow(0.65f)
    val deckBVolume: StateFlow<Float> = _deckBVolume.asStateFlow()

    private val _deckBSpeed = MutableStateFlow(1.0f)
    val deckBSpeed: StateFlow<Float> = _deckBSpeed.asStateFlow()

    private val _deckBPitch = MutableStateFlow(1.0f)
    val deckBPitch: StateFlow<Float> = _deckBPitch.asStateFlow()

    interface ServiceDelegate {
        fun loadSecondTrack(song: Song?)
        fun setSecondTrackPlaying(playing: Boolean)
        fun seekSecondTrackToFraction(fraction: Float)
        fun syncSecondTrackParams()
    }

    @Volatile
    private var delegate: ServiceDelegate? = null

    fun attachDelegate(serviceDelegate: ServiceDelegate?) {
        delegate = serviceDelegate
    }

    fun setActivePreset(presetId: String, appliedEqByPreset: Boolean = false) {
        _activePresetId.value = presetId
        presetAppliedEq = appliedEqByPreset
    }

    fun applyEqPresetFromRemix(preset: EqualizerPreset) {
        presetAppliedEq = true
        AppSettings.setEqualizerMode(EqualizerMode.MANUAL)
        AppSettings.setEqualizerPreset(preset)
        AppSettings.setEqualizerEnabled(true)
    }

    fun selectSecondTrack(song: Song?) {
        _secondTrack.value = song
        _secondTrackPositionMs.value = 0L
        _secondTrackDurationMs.value = song?.durationMillis()?.takeIf { it > 0L } ?: 0L
        if (song == null) {
            _secondTrackPlaying.value = false
            _secondTrackLoading.value = false
        } else {
            _secondTrackLoading.value = true
        }
        delegate?.loadSecondTrack(song)
    }

    fun toggleSecondTrackPlayPause() {
        val next = !_secondTrackPlaying.value
        _secondTrackPlaying.value = next
        delegate?.setSecondTrackPlaying(next)
    }

    fun setSecondTrackPlaying(playing: Boolean) {
        _secondTrackPlaying.value = playing
        delegate?.setSecondTrackPlaying(playing)
    }

    fun seekSecondTrackToFraction(fraction: Float) {
        delegate?.seekSecondTrackToFraction(fraction.coerceIn(0f, 1f))
    }

    fun setDeckAVolume(volume: Float) {
        _deckAVolume.value = volume.coerceIn(0f, 1f)
        delegate?.syncSecondTrackParams()
    }

    fun setDeckBVolume(volume: Float) {
        _deckBVolume.value = volume.coerceIn(0f, 1f)
        delegate?.syncSecondTrackParams()
    }

    fun setDeckBSpeed(speed: Float) {
        _deckBSpeed.value = speed.coerceIn(0.5f, 1.5f)
        delegate?.syncSecondTrackParams()
    }

    fun setDeckBPitch(pitch: Float) {
        _deckBPitch.value = pitch.coerceIn(0.5f, 1.5f)
        delegate?.syncSecondTrackParams()
    }

    internal fun updateSecondTrackProgress(
        playing: Boolean,
        loading: Boolean,
        positionMs: Long,
        durationMs: Long,
    ) {
        _secondTrackPlaying.value = playing
        _secondTrackLoading.value = loading
        _secondTrackPositionMs.value = positionMs.coerceAtLeast(0L)
        if (durationMs > 0L) _secondTrackDurationMs.value = durationMs
    }

    /**
     * Resets single-track and 2-song remix modifiers back to clean defaults
     * whenever a new main song starts.
     */
    fun resetForNewTrack() {
        if (AppSettings.playbackSpeed.value != 1.0f) {
            AppSettings.setPlaybackSpeed(1.0f)
        }
        if (AppSettings.playbackPitch.value != 1.0f) {
            AppSettings.setPlaybackPitch(1.0f)
        }
        if (presetAppliedEq) {
            presetAppliedEq = false
            AppSettings.setEqualizerEnabled(false)
            AppSettings.setEqualizerPreset(EqualizerPreset.FLAT)
        }
        _activePresetId.value = "orig"
        _deckAVolume.value = 1.0f
        if (_secondTrack.value != null) {
            selectSecondTrack(null)
        }
    }
}
