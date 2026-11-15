import { useState, useTransition } from 'react';
import { Sparkles, Play, Plus, X, Loader2, Music } from 'lucide-react';
import { usePlayer } from '@/state/player';
import { useUi } from '@/state/ui';
import { useHome, useLibrary } from '@/data/music/queries';
import type { Track } from '@/data/music/types';
import { sizedArtwork } from '@/lib/artwork';

const VIBE_PRESETS = [
  { name: 'Workout Energy', keywords: ['workout', 'energy', 'pump', 'power', 'run', 'fast', 'bass', 'beat', 'hype', 'club', 'dance', 'electronic', 'rock'] },
  { name: 'Late Night Chill', keywords: ['night', 'chill', 'calm', 'slow', 'ambient', 'dream', 'quiet', 'sleep', 'moon', 'lo-fi', 'r&b', 'soul'] },
  { name: 'Deep Focus', keywords: ['focus', 'study', 'work', 'instrumental', 'piano', 'jazz', 'lofi', 'beats', 'peace', 'flow'] },
  { name: 'Nostalgia Rewind', keywords: ['classic', 'old', 'remember', 'retro', 'summer', 'golden', 'timeless', 'love', 'memory', 'acoustic'] },
  { name: 'Melancholy Moods', keywords: ['sad', 'rain', 'cry', 'lonely', 'dark', 'heart', 'tears', 'blue', 'acoustic', 'autumn'] },
  { name: 'Party Anthems', keywords: ['party', 'hit', 'fire', 'drop', 'summer', 'dance', 'wild', 'turn', 'radio', 'pop', 'remix'] },
  { name: 'Road Trip', keywords: ['drive', 'road', 'trip', 'highway', 'sun', 'travel', 'sing', 'chorus', 'cruising'] },
  { name: 'Morning Acoustic', keywords: ['morning', 'coffee', 'wake', 'sunrise', 'acoustic', 'guitar', 'folk', 'peaceful', 'fresh'] },
];

function trackArtist(t: Track): string {
  return t.artists.map((a) => a.name).join(', ') || 'Unknown Artist';
}

export function AiPlaylistMakerModal() {
  const open = useUi((s) => s.aiPlaylistModalOpen);
  const close = useUi((s) => s.closeAiPlaylistModal);
  const [, startTransition] = useTransition();

  const [selectedVibe, setSelectedVibe] = useState(VIBE_PRESETS[0]?.name ?? 'Workout Energy');
  const [customPrompt, setCustomPrompt] = useState('');
  const [curatedTracks, setCuratedTracks] = useState<Track[]>([]);
  const [isGenerating, setIsGenerating] = useState(false);
  const [isLiveAi, setIsLiveAi] = useState(false);
  const [hasGenerated, setHasGenerated] = useState(false);

  const home = useHome();
  const history = useLibrary('recent');
  const queue = usePlayer((s) => s.queue);

  if (!open) return null;

  // Gather candidate songs from library history, home feeds, and current queue
  const gatherCandidates = (): Track[] => {
    const list: Track[] = [];
    const seen = new Set<string>();

    const add = (t: Track | undefined | null) => {
      if (!t || !t.videoId || seen.has(t.videoId)) return;
      seen.add(t.videoId);
      list.push(t);
    };

    queue.forEach((q) => add(q.track));
    const recent = history.data?.pages[0]?.data.tracks ?? [];
    recent.forEach(add);

    const homeShelves = home.data?.pages.flatMap((p) => p.data) ?? [];
    for (const shelf of homeShelves) {
      if ('items' in shelf && Array.isArray(shelf.items)) {
        for (const item of shelf.items) {
          if (item.id && (item.kind === 'song' || item.kind === 'video')) {
            add({
              videoId: item.id,
              title: item.title,
              artists: item.artists ?? [],
              album: item.album ?? null,
              durationSec: item.durationSec ?? null,
              thumbnail: item.thumbnail ?? null,
              explicit: item.explicit ?? false,
              index: null,
              plays: null,
            });
          }
        }
      }
    }
    return list.slice(0, 40);
  };

  const handleGenerate = async () => {
    setIsGenerating(true);
    setIsLiveAi(false);
    const targetPrompt = customPrompt.trim() || selectedVibe;
    const candidates = gatherCandidates();

    if (candidates.length === 0) {
      setIsGenerating(false);
      setCuratedTracks([]);
      setHasGenerated(true);
      return;
    }

    try {
      const candidateSummary = candidates
        .map((s, idx) => `${idx}: ${s.title.slice(0, 35)} by ${trackArtist(s).slice(0, 25)}`)
        .join('\n');

      const payload = {
        messages: [
          {
            role: 'system',
            content:
              'You are an expert music AI curator. Given a target mood/vibe and candidate songs [index: title by artist], select the 10 to 20 songs that best fit the mood, tempo, and style, in optimal listening sequence. Return ONLY a comma-separated list of the matching index numbers (e.g. 0, 3, 7, 12, 14). Do not include any explanations, words, or markdown.',
          },
          {
            role: 'user',
            content: `Target Vibe: ${targetPrompt}\nCandidate Songs:\n${candidateSummary}`,
          },
        ],
      };

      const res = await fetch('https://text.pollinations.ai/', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Agent': 'HushMusic/1.3',
        },
        body: JSON.stringify(payload),
      });

      if (res.ok) {
        const text = await res.text();
        const matches = text.match(/\d+/g);
        if (matches && matches.length >= 3) {
          const indices = matches.map((m) => parseInt(m, 10)).filter((i) => i >= 0 && i < candidates.length);
          const uniqueIndices = Array.from(new Set(indices));
          if (uniqueIndices.length >= 3) {
            const picked = uniqueIndices.map((i) => candidates[i]).filter((t): t is Track => t !== undefined);
            setCuratedTracks(picked);
            setIsLiveAi(true);
            setHasGenerated(true);
            setIsGenerating(false);
            return;
          }
        }
      }
    } catch {
      // Fall through to heuristic curation
    }

    // Heuristic fallback
    const preset = VIBE_PRESETS.find((v) => v.name === selectedVibe);
    const keywords = [
      ...(preset?.keywords ?? []),
      ...targetPrompt
        .toLowerCase()
        .split(/[\s,-]+/)
        .filter((w) => w.length > 2),
    ];

    const scored = candidates.map((track) => {
      let score = 0;
      const text = `${track.title} ${trackArtist(track)}`.toLowerCase();
      keywords.forEach((kw) => {
        if (text.includes(kw)) score += 3;
      });
      return { track, score: score + Math.random() };
    });

    scored.sort((a, b) => b.score - a.score);
    const chosen = scored.slice(0, 15).map((s) => s.track);
    setCuratedTracks(chosen);
    setIsLiveAi(false);
    setHasGenerated(true);
    setIsGenerating(false);
  };

  const handlePlayNow = () => {
    if (curatedTracks.length === 0) return;
    startTransition(() => {
      usePlayer.getState().playTracks(curatedTracks, 0, { title: 'AI Mix', type: 'home' });
      close();
    });
  };

  const handleAddToQueue = () => {
    if (curatedTracks.length === 0) return;
    usePlayer.getState().addToQueue(curatedTracks);
    close();
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="AI Playlist Maker"
      className="fixed inset-0 z-[var(--z-modal)] flex items-center justify-center p-4 bg-black/60 backdrop-blur-md animate-fade"
    >
      <div className="relative w-full max-w-[620px] max-h-[90vh] flex flex-col rounded-[28px] bg-white dark:bg-[#1e1c24] border border-black/10 dark:border-white/10 shadow-2xl overflow-hidden animate-zoom">
        {/* Header */}
        <div className="flex items-center justify-between px-6 pt-6 pb-4 border-b border-black/5 dark:border-white/5">
          <div className="flex items-center gap-2.5">
            <div className="flex items-center justify-center size-9 rounded-2xl bg-[#d0bcff]/25 text-[#707c2a] dark:text-[#d0bcff]">
              <Sparkles size={20} />
            </div>
            <div>
              <h2 className="text-xl font-bold tracking-tight text-primary">AI Playlist Maker</h2>
              <p className="text-xs text-secondary">Realtime neural playlist curation</p>
            </div>
          </div>
          <button
            onClick={close}
            aria-label="Close"
            className="flex items-center justify-center size-8 rounded-full text-secondary hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        {/* Scrollable Content */}
        <div className="flex-1 overflow-y-auto px-6 py-4 space-y-5">
          {/* Vibe Chips */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-secondary mb-2">
              Select Vibe
            </label>
            <div className="flex flex-wrap gap-2">
              {VIBE_PRESETS.map((v) => (
                <button
                  key={v.name}
                  type="button"
                  onClick={() => {
                    setSelectedVibe(v.name);
                    setCustomPrompt('');
                  }}
                  className={`px-3.5 py-1.5 rounded-full text-xs font-semibold transition-all ${
                    selectedVibe === v.name && !customPrompt
                      ? 'bg-[#707c2a] text-white shadow-sm scale-105'
                      : 'bg-black/5 dark:bg-white/5 text-primary hover:bg-black/10 dark:hover:bg-white/10'
                  }`}
                >
                  {v.name}
                </button>
              ))}
            </div>
          </div>

          {/* Custom Prompt */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-secondary mb-2">
              Custom Prompt (Optional)
            </label>
            <input
              type="text"
              placeholder="e.g. cinematic post-rock for late night writing"
              value={customPrompt}
              onChange={(e) => setCustomPrompt(e.target.value)}
              className="w-full px-4 py-2.5 rounded-2xl bg-black/5 dark:bg-white/5 border border-black/10 dark:border-white/10 text-sm text-primary placeholder:text-secondary/60 focus:border-[#707c2a] focus:ring-1 focus:ring-[#707c2a] outline-none transition-all"
            />
          </div>

          {/* Generate Button */}
          <button
            type="button"
            disabled={isGenerating}
            onClick={handleGenerate}
            className="w-full flex items-center justify-center gap-2 py-3 rounded-full bg-[#707c2a] hover:bg-[#82922e] active:scale-[0.98] text-white text-sm font-bold shadow-md transition-all disabled:opacity-50"
          >
            {isGenerating ? (
              <>
                <Loader2 size={16} className="animate-spin" />
                <span>Curating with Neural AI…</span>
              </>
            ) : (
              <>
                <Sparkles size={16} />
                <span>Generate Playlist</span>
              </>
            )}
          </button>

          {/* Results List */}
          {hasGenerated && (
            <div className="space-y-3 pt-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold uppercase tracking-wider text-secondary">
                  Curated Tracks ({curatedTracks.length})
                </span>
                <span
                  className={`text-[11px] font-semibold px-2 py-0.5 rounded-full ${
                    isLiveAi
                      ? 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20'
                      : 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border border-amber-500/20'
                  }`}
                >
                  {isLiveAi ? 'Live Neural AI' : 'Vibe Heuristic'}
                </span>
              </div>

              {curatedTracks.length === 0 ? (
                <div className="py-8 text-center text-secondary text-sm">
                  No matching tracks found. Try adding songs to your library or queue first.
                </div>
              ) : (
                <div className="max-h-[220px] overflow-y-auto space-y-1.5 pr-1">
                  {curatedTracks.map((t, idx) => (
                    <div
                      key={`${t.videoId}-${idx}`}
                      className="flex items-center gap-3 p-2 rounded-xl bg-black/5 dark:bg-white/5 hover:bg-black/10 dark:hover:bg-white/10 transition-colors"
                    >
                      <div className="size-10 rounded-lg overflow-hidden bg-black/20 flex-none">
                        {t.thumbnail ? (
                          <img
                            src={sizedArtwork(t.thumbnail, 40)}
                            alt=""
                            className="size-full object-cover"
                            decoding="async"
                          />
                        ) : (
                          <div className="size-full flex items-center justify-center text-secondary">
                            <Music size={16} />
                          </div>
                        )}
                      </div>
                      <div className="min-w-0 flex-1">
                        <p className="text-xs font-semibold text-primary truncate">{t.title}</p>
                        <p className="text-[11px] text-secondary truncate">{trackArtist(t)}</p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Actions Footer */}
        {hasGenerated && curatedTracks.length > 0 && (
          <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-black/5 dark:border-white/5 bg-black/[0.02] dark:bg-white/[0.02]">
            <button
              type="button"
              onClick={handleAddToQueue}
              className="flex items-center gap-1.5 px-4 py-2 rounded-full border border-black/10 dark:border-white/10 text-xs font-semibold text-primary hover:bg-black/5 dark:hover:bg-white/5 transition-all"
            >
              <Plus size={14} />
              <span>Add to Queue</span>
            </button>
            <button
              type="button"
              onClick={handlePlayNow}
              className="flex items-center gap-1.5 px-5 py-2 rounded-full bg-[#707c2a] hover:bg-[#82922e] text-white text-xs font-bold shadow-sm transition-all"
            >
              <Play size={14} fill="currentColor" />
              <span>Play Now</span>
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
