import { Play, Settings, Shuffle, Sparkles } from 'lucide-react';
import { PrefetchLink } from '@/components/PrefetchLink';
import { sizedArtwork } from '@/lib/artwork';
import type { Track } from '@/data/music/types';
import { useUi } from '@/state/ui';
import { getM3eShapeStyle } from './M3eShapes';

interface M3eHomeHeroProps {
  tracks: Track[];
  onShufflePlay: () => void;
  onTrackPlay: (track: Track, index: number) => void;
}

export function M3eHomeHero({ tracks, onShufflePlay, onTrackPlay }: M3eHomeHeroProps) {
  const openAiPlaylist = useUi((s) => s.openAiPlaylistModal);

  const leadTrack = tracks[0] ?? null;
  const sat1 = tracks[1] ?? null;
  const sat2 = tracks[2] ?? null;

  return (
    <div className="relative w-full max-w-[1040px] mx-auto px-4 pt-2 pb-8 overflow-hidden">
      {/* Top action row */}
      <div className="flex items-center justify-between mb-6">
        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-black/10 dark:bg-white/10 border border-black/10 dark:border-white/15 text-xs font-semibold text-primary">
          <span className="text-[14px]">β</span> Beta
        </span>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={openAiPlaylist}
            className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-black/5 dark:bg-white/10 hover:bg-black/10 dark:hover:bg-white/15 text-xs font-bold text-primary transition-all active:scale-95"
            aria-label="AI Mix Generator"
          >
            <Sparkles size={14} className="text-[#707c2a] dark:text-[#d0bcff]" />
            <span>AI Mix</span>
          </button>
          <PrefetchLink
            to="/settings"
            aria-label="Settings"
            className="flex items-center justify-center size-9 rounded-full bg-black/5 dark:bg-white/10 hover:bg-black/10 dark:hover:bg-white/15 text-primary transition-all active:scale-95"
          >
            <Settings size={17} />
          </PrefetchLink>
        </div>
      </div>

      {/* Main headline and Shuffle FAB */}
      <div className="flex items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-5xl sm:text-6xl md:text-7xl font-extrabold tracking-tight text-primary font-['Google_Sans_Flex','Google_Sans_Rounded',sans-serif]">
            Your Mix
          </h1>
          <p className="text-base sm:text-lg text-secondary font-medium mt-1">Today's Mix for you</p>
        </div>

        {/* Circular Olive Shuffle FAB button */}
        <button
          type="button"
          onClick={onShufflePlay}
          className="size-16 sm:size-20 rounded-full bg-[#707c2a] hover:bg-[#859432] active:scale-90 text-white shadow-xl flex items-center justify-center transition-all flex-none group cursor-pointer"
          aria-label="Shuffle Mix"
        >
          <Shuffle size={32} className="transition-transform group-hover:rotate-12 group-active:scale-95" />
        </button>
      </div>

      {/* Hero Center Organic Stage */}
      {leadTrack && (
        <div className="relative flex items-center justify-center min-h-[360px] sm:min-h-[420px] my-2 select-none">
          {/* Background Ambient Glow */}
          <div className="absolute inset-0 max-w-[500px] mx-auto rounded-full bg-[#707c2a]/15 dark:bg-[#d0bcff]/10 blur-3xl pointer-events-none" />

          {/* Top-Left Satellite Bubble */}
          {sat1 && (
            <button
              type="button"
              onClick={() => onTrackPlay(sat1, 1)}
              style={getM3eShapeStyle('scallop-8')}
              className="absolute left-2 sm:left-12 top-4 sm:top-6 size-20 sm:size-28 z-10 shadow-lg cursor-pointer overflow-hidden animate-m3e-satellite-1 group transition-transform hover:scale-110 active:scale-95"
              aria-label={`Play ${sat1.title}`}
            >
              {sat1.thumbnail ? (
                <img
                  src={sizedArtwork(sat1.thumbnail, 120)}
                  alt={sat1.title}
                  className="size-full object-cover transition-transform group-hover:scale-105"
                  decoding="async"
                />
              ) : (
                <div className="size-full bg-[#707c2a]/20" />
              )}
              <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity text-white">
                <Play size={20} fill="currentColor" />
              </div>
            </button>
          )}

          {/* Center Diagonal Stadium Pill Artwork */}
          <button
            type="button"
            onClick={() => onTrackPlay(leadTrack, 0)}
            className="relative z-20 w-[190px] sm:w-[240px] md:w-[270px] aspect-[1/1.55] rounded-[9999px] rotate-[-35deg] overflow-hidden shadow-2xl border-[5px] border-white/20 dark:border-white/10 animate-m3e-breathe cursor-pointer group transition-all"
            aria-label={`Play ${leadTrack.title} by ${leadTrack.artists.map((a) => a.name).join(', ')}`}
          >
            {leadTrack.thumbnail ? (
              <img
                src={sizedArtwork(leadTrack.thumbnail, 400)}
                alt={leadTrack.title}
                className="size-full object-cover scale-[1.3] rotate-[35deg] transition-transform duration-500 group-hover:scale-[1.4]"
                decoding="async"
              />
            ) : (
              <div className="size-full bg-[#4f378b]" />
            )}
            <div className="absolute inset-0 bg-black/30 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity text-white">
              <div className="size-14 rounded-full bg-white/20 backdrop-blur-md flex items-center justify-center">
                <Play size={28} fill="currentColor" className="ml-1" />
              </div>
            </div>
          </button>

          {/* Bottom-Right Satellite Bubble */}
          {sat2 && (
            <button
              type="button"
              onClick={() => onTrackPlay(sat2, 2)}
              style={getM3eShapeStyle('pebble')}
              className="absolute right-2 sm:right-16 bottom-2 sm:bottom-4 size-20 sm:size-26 z-10 shadow-lg cursor-pointer overflow-hidden animate-m3e-satellite-2 group transition-transform hover:scale-110 active:scale-95"
              aria-label={`Play ${sat2.title}`}
            >
              {sat2.thumbnail ? (
                <img
                  src={sizedArtwork(sat2.thumbnail, 120)}
                  alt={sat2.title}
                  className="size-full object-cover transition-transform group-hover:scale-105"
                  decoding="async"
                />
              ) : (
                <div className="size-full bg-[#3b4858]" />
              )}
              <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity text-white">
                <Play size={18} fill="currentColor" />
              </div>
            </button>
          )}
        </div>
      )}
    </div>
  );
}
