import { Pause, Play, Volume2, VolumeX, Volume1, ListMusic, Speaker } from 'lucide-react';
import {
  BackwardIcon,
  ForwardIcon,
  RepeatIcon,
  RepeatOneIcon,
  ShuffleIcon,
} from '@/components/icons/Transport';
import { Rewind30Icon, Forward30Icon } from '@/components/icons/M3ePlayerIcons';
import { getAudio, usePlayer } from '@/state/player';
import { useUi } from '@/state/ui';

export function M3eTransport({ onToggleQueue }: { onToggleQueue?: () => void }) {
  const hasTrack = usePlayer((s) => s.nowPlaying !== null);
  const playing = usePlayer((s) => s.status === 'playing' || s.status === 'buffering');
  const hasNext = usePlayer((s) => s.index + 1 < s.queue.length || s.repeat !== 'off' || (s.autoplay && s.index >= 0));
  const shuffle = usePlayer((s) => s.shuffle);
  const repeat = usePlayer((s) => s.repeat);
  const volume = usePlayer((s) => s.volume);
  const muted = usePlayer((s) => s.muted);

  const { togglePlay, next, previous, toggleShuffle, cycleRepeat, seek, setVolume, toggleMute } = usePlayer.getState();
  const toggleSidePanel = useUi((s) => s.toggleSidePanel);

  const handleRewind30 = () => {
    const audio = getAudio();
    if (!audio) return;
    seek(Math.max(0, audio.currentTime - 30));
  };

  const handleForward30 = () => {
    const audio = getAudio();
    if (!audio) return;
    seek(Math.min(audio.duration || 0, audio.currentTime + 30));
  };

  const adjustVolume = (delta: number) => {
    const nextVol = Math.max(0, Math.min(1, volume + delta));
    setVolume(nextVol);
  };

  return (
    <div className="w-full max-w-[460px] mx-auto flex flex-col gap-5 pt-3 pb-2 select-none">
      {/* Secondary Row: Shuffle, Previous, Next, Repeat */}
      <div className="flex items-center justify-between px-6 text-secondary">
        <button
          type="button"
          aria-label="Shuffle"
          aria-pressed={shuffle}
          onClick={toggleShuffle}
          className="size-9 rounded-full flex items-center justify-center hover:bg-white/10 aria-pressed:text-primary aria-pressed:bg-white/15 transition-all active:scale-90"
        >
          <ShuffleIcon size={24} />
        </button>

        <button
          type="button"
          disabled={!hasTrack}
          aria-label="Previous"
          onClick={previous}
          className="size-10 rounded-full flex items-center justify-center hover:bg-white/10 text-primary disabled:opacity-40 transition-all active:scale-90"
        >
          <BackwardIcon size={28} />
        </button>

        <button
          type="button"
          disabled={!hasTrack || !hasNext}
          aria-label="Next"
          onClick={next}
          className="size-10 rounded-full flex items-center justify-center hover:bg-white/10 text-primary disabled:opacity-40 transition-all active:scale-90"
        >
          <ForwardIcon size={28} />
        </button>

        <button
          type="button"
          aria-label={repeat === 'one' ? 'Repeat one' : repeat === 'all' ? 'Repeat all' : 'Repeat'}
          aria-pressed={repeat !== 'off'}
          onClick={cycleRepeat}
          className="size-9 rounded-full flex items-center justify-center hover:bg-white/10 aria-pressed:text-primary aria-pressed:bg-white/15 transition-all active:scale-90"
        >
          {repeat === 'one' ? <RepeatOneIcon size={24} /> : <RepeatIcon size={24} />}
        </button>
      </div>

      {/* Main Playback Row: Rewind 30s, Morphing Squircle Play/Pause, Forward 30s */}
      <div className="flex items-center justify-center gap-4">
        {/* Rewind 30s Squarish Button */}
        <button
          type="button"
          disabled={!hasTrack}
          aria-label="Rewind 30 seconds"
          onClick={handleRewind30}
          className="size-16 sm:size-18 rounded-[24px] bg-white/10 hover:bg-white/15 active:scale-90 flex items-center justify-center text-primary disabled:opacity-40 shadow-sm transition-all"
        >
          <Rewind30Icon size={28} />
        </button>

        {/* Morphing Squircle Play/Pause Button (Pixel QS style) */}
        <button
          type="button"
          disabled={!hasTrack}
          aria-label={playing ? 'Pause' : 'Play'}
          onClick={togglePlay}
          className="h-16 sm:h-18 w-24 sm:w-28 rounded-[28px] bg-[#d0bcff] hover:bg-[#e8def8] active:scale-90 flex items-center justify-center text-[#381e72] shadow-md transition-all disabled:opacity-50"
        >
          {playing ? (
            <Pause size={34} fill="currentColor" strokeWidth={0} />
          ) : (
            <Play size={34} fill="currentColor" strokeWidth={0} className="ml-1" />
          )}
        </button>

        {/* Forward 30s Squarish Button */}
        <button
          type="button"
          disabled={!hasTrack}
          aria-label="Forward 30 seconds"
          onClick={handleForward30}
          className="size-16 sm:size-18 rounded-[24px] bg-white/10 hover:bg-white/15 active:scale-90 flex items-center justify-center text-primary disabled:opacity-40 shadow-sm transition-all"
        >
          <Forward30Icon size={28} />
        </button>
      </div>

      {/* Volume Row: Pill Capsules */}
      <div className="flex items-center justify-center gap-2.5 pt-1">
        {/* Mute Pill */}
        <button
          type="button"
          onClick={toggleMute}
          aria-label={muted ? 'Unmute' : 'Mute'}
          className={`size-11 rounded-full flex items-center justify-center transition-all ${
            muted
              ? 'bg-red-500/20 text-red-400 border border-red-500/30'
              : 'bg-white/10 hover:bg-white/15 text-primary'
          }`}
        >
          {muted || volume === 0 ? <VolumeX size={18} /> : <Volume2 size={18} />}
        </button>

        {/* Volume Down Capsule */}
        <button
          type="button"
          onClick={() => adjustVolume(-0.1)}
          aria-label="Volume down"
          className="flex-1 max-w-[130px] h-11 px-4 rounded-full bg-white/10 hover:bg-white/15 active:scale-95 flex items-center justify-center gap-2 text-primary transition-all text-xs font-semibold"
        >
          <Volume1 size={17} />
          <span>Lower</span>
        </button>

        {/* Volume Up Capsule */}
        <button
          type="button"
          onClick={() => adjustVolume(0.1)}
          aria-label="Volume up"
          className="flex-1 max-w-[130px] h-11 px-4 rounded-full bg-white/10 hover:bg-white/15 active:scale-95 flex items-center justify-center gap-2 text-primary transition-all text-xs font-semibold"
        >
          <Volume2 size={17} />
          <span>Higher</span>
        </button>
      </div>

      {/* Output & Queue Pill Chips */}
      <div className="flex items-center justify-between px-2 pt-2 text-xs font-semibold">
        {/* Output Device Pill */}
        <div className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-white/10 border border-white/10 text-primary/90">
          <Speaker size={14} />
          <span>Living room</span>
        </div>

        {/* Queue Pill */}
        <button
          type="button"
          onClick={onToggleQueue ?? (() => toggleSidePanel('up-next'))}
          className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-white/10 hover:bg-white/15 border border-white/10 text-primary/90 transition-all active:scale-95 cursor-pointer"
        >
          <ListMusic size={14} />
          <span>Queue</span>
        </button>
      </div>
    </div>
  );
}
