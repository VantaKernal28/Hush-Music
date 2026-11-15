import { usePlayer } from '@/state/player';
import { useScrubber } from '@/shell/useScrubber';

export function M3eWavyScrubber() {
  const { bar, elapsed, remaining, dragging, handlers } = useScrubber();
  const playing = usePlayer((s) => s.status === 'playing');

  return (
    <div className="w-full flex flex-col gap-2 select-none" data-dragging={dragging || undefined}>
      {/* Wave Scrubber Hit Area */}
      <div
        className="group/wavy relative h-8 flex items-center cursor-pointer touch-none"
        data-scrubber
        {...handlers}
      >
        {/* Background Inactive Track: Straight Line */}
        <div className="absolute inset-x-0 h-1 rounded-full bg-black/10 dark:bg-white/20" />

        {/* Dynamic Progress Container (reads --progress from useScrubber) */}
        <div
          ref={bar}
          className="absolute inset-y-0 left-0 w-full pointer-events-none [--progress:0]"
          style={{ width: '100%' }}
        >
          {/* Active Wavy Track clipped to progress */}
          <div
            className="absolute inset-y-0 left-0 overflow-hidden flex items-center"
            style={{ width: 'calc(var(--progress) * 100%)' }}
          >
            <svg
              className={`w-[800px] h-4 text-primary flex-none ${playing ? 'animate-m3e-wave' : ''}`}
              viewBox="0 0 400 16"
              fill="none"
              stroke="currentColor"
              strokeWidth="3.5"
              strokeLinecap="round"
              strokeLinejoin="round"
              preserveAspectRatio="none"
            >
              <path
                d="M 0 8 Q 10 2, 20 8 T 40 8 T 60 8 T 80 8 T 100 8 T 120 8 T 140 8 T 160 8 T 180 8 T 200 8 T 220 8 T 240 8 T 260 8 T 280 8 T 300 8 T 320 8 T 340 8 T 360 8 T 380 8 T 400 8"
                strokeDasharray="40 0"
              />
            </svg>
          </div>

          {/* Vertical Capsule Playhead Thumb at progress boundary */}
          <div
            className="absolute top-1/2 -translate-y-1/2 -translate-x-1/2 w-1.5 h-5 rounded-full bg-primary shadow-sm pointer-events-none transition-transform group-hover/wavy:scale-125"
            style={{ left: 'calc(var(--progress) * 100%)' }}
          />
        </div>
      </div>

      {/* Timestamps Row */}
      <div className="flex items-center justify-between text-xs font-semibold tabular-nums text-secondary px-0.5">
        <span ref={elapsed}>0:00</span>
        <span ref={remaining}>-0:00</span>
      </div>
    </div>
  );
}
