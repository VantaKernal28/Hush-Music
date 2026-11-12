import React, { useState, useEffect } from 'react';
import { 
  ChevronRight, 
  ChevronLeft, 
  Sparkles, 
  Check, 
  Wifi, 
  DownloadCloud, 
  Volume2, 
  Zap, 
  Sliders, 
  Music,
  Headphones,
  Disc3,
  HardDrive
} from 'lucide-react';
import { LogoMark } from '@/components/icons/LogoMark';
import './frostGlass.css';

interface OnboardingWizardProps {
  isOpen: boolean;
  onComplete: () => void;
}

export const ONBOARDING_COMPLETED_KEY = 'hasCompletedOnboarding';
export const USER_NAME_KEY = 'hush_user_name';
export const GENRES_KEY = 'hush_selected_genres';
export const AUDIO_QUALITY_KEY = 'hush_audio_quality';
export const SMART_DOWNLOADS_KEY = 'hush_smart_downloads';
export const WIFI_ONLY_KEY = 'hush_wifi_only_downloads';
export const STORAGE_CAP_KEY = 'hush_storage_cap';

const GENRE_LIST = [
  'Lo-Fi',
  'Electronic',
  'UK Garage',
  'Hip-Hop',
  'Ambient',
  'Indie',
  'Synthwave',
  'Classical',
  'Jazz',
  'Bass',
  'R&B',
  'House',
  'Rock',
  'Pop',
  'Techno',
  'Soul',
];

interface QualityOption {
  id: string;
  title: string;
  bitrate: string;
  badge?: string;
  detail: string;
  icon: typeof Volume2;
}

const QUALITY_OPTIONS: QualityOption[] = [
  {
    id: 'standard',
    title: 'Standard',
    bitrate: '128 kbps',
    detail: 'Data saver, smooth playback anywhere without buffering.',
    icon: Volume2,
  },
  {
    id: 'high',
    title: 'High',
    bitrate: '320 kbps',
    badge: 'Recommended',
    detail: 'Crisp, balanced studio-grade streaming for everyday listening.',
    icon: Zap,
  },
  {
    id: 'lossless',
    title: 'Lossless',
    bitrate: 'FLAC / Hi-Res',
    detail: 'Uncompressed bit-perfect audio for audiophiles with dedicated DACs.',
    icon: Disc3,
  },
];

const STORAGE_CAPS = ['2 GB', '5 GB', '10 GB', 'Unlimited'];

export function OnboardingWizard({ isOpen, onComplete }: OnboardingWizardProps) {
  const [step, setStep] = useState(1);
  const [name, setName] = useState('');
  const [selectedGenres, setSelectedGenres] = useState<string[]>(['Lo-Fi', 'Electronic']);
  const [selectedQuality, setSelectedQuality] = useState('high');
  const [smartDownloads, setSmartDownloads] = useState(true);
  const [wifiOnly, setWifiOnly] = useState(true);
  const [storageCap, setStorageCap] = useState('5 GB');
  const [animatingOut, setAnimatingOut] = useState(false);

  useEffect(() => {
    // Read any pre-existing local storage values
    try {
      const storedName = localStorage.getItem(USER_NAME_KEY);
      if (storedName) setName(storedName);

      const storedGenres = localStorage.getItem(GENRES_KEY);
      if (storedGenres) setSelectedGenres(JSON.parse(storedGenres));

      const storedQuality = localStorage.getItem(AUDIO_QUALITY_KEY);
      if (storedQuality) setSelectedQuality(storedQuality);

      const storedSmart = localStorage.getItem(SMART_DOWNLOADS_KEY);
      if (storedSmart !== null) setSmartDownloads(storedSmart === 'true');

      const storedWifi = localStorage.getItem(WIFI_ONLY_KEY);
      if (storedWifi !== null) setWifiOnly(storedWifi === 'true');

      const storedStorage = localStorage.getItem(STORAGE_CAP_KEY);
      if (storedStorage) setStorageCap(storedStorage);
    } catch {
      // Non-blocking fallback
    }
  }, []);

  if (!isOpen) return null;

  const toggleGenre = (genre: string) => {
    setSelectedGenres((prev) =>
      prev.includes(genre) ? prev.filter((g) => g !== genre) : [...prev, genre],
    );
  };

  const handleNext = () => {
    if (step === 1) {
      if (name.trim()) {
        try {
          localStorage.setItem(USER_NAME_KEY, name.trim());
          // Update partySettings nickname if present
          const currentParty = JSON.parse(localStorage.getItem('hush:partySettings') ?? '{}');
          localStorage.setItem('hush:partySettings', JSON.stringify({ ...currentParty, nickname: name.trim() }));
        } catch {}
      }
      setStep(2);
    } else if (step === 2) {
      try {
        localStorage.setItem(GENRES_KEY, JSON.stringify(selectedGenres));
      } catch {}
      setStep(3);
    } else if (step === 3) {
      try {
        localStorage.setItem(AUDIO_QUALITY_KEY, selectedQuality);
      } catch {}
      setStep(4);
    } else if (step === 4) {
      handleFinish();
    }
  };

  const handleBack = () => {
    if (step > 1) setStep((s) => s - 1);
  };

  const handleFinish = () => {
    try {
      localStorage.setItem(ONBOARDING_COMPLETED_KEY, 'true');
      localStorage.setItem(SMART_DOWNLOADS_KEY, String(smartDownloads));
      localStorage.setItem(WIFI_ONLY_KEY, String(wifiOnly));
      localStorage.setItem(STORAGE_CAP_KEY, storageCap);
      if (name.trim()) {
        localStorage.setItem(USER_NAME_KEY, name.trim());
      }
    } catch {}

    setAnimatingOut(true);
    setTimeout(() => {
      onComplete();
    }, 400);
  };

  const canContinue = () => {
    if (step === 1) return name.trim().length > 0;
    if (step === 2) return selectedGenres.length >= 1;
    return true;
  };

  const progressPercent = (step / 4) * 100;

  return (
    <div 
      className={`frost-scrim ${animatingOut ? 'opacity-0 scale-95 pointer-events-none' : 'opacity-100'}`}
      style={{ transition: 'all 0.4s cubic-bezier(0.22, 1, 0.36, 1)' }}
      role="dialog"
      aria-modal="true"
    >
      <div className="frost-wizard-card">
        {/* Ambient Glows */}
        <div className="frost-glow-orb-1" />
        <div className="frost-glow-orb-2" />

        {/* Header Bar with Frosted Progress Indicator */}
        <div className="p-6 pb-4 border-b border-white/10 relative z-10 flex flex-col gap-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="size-8 rounded-xl bg-white/10 flex items-center justify-center border border-white/15 shadow-inner">
                <LogoMark size={18} className="text-white" />
              </div>
              <span className="text-xs font-bold tracking-widest text-white/50 uppercase">
                Hush Music Setup
              </span>
            </div>
            <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-white/5 border border-white/10 text-xs font-semibold text-white/70">
              <span>Step {step} of 4</span>
            </div>
          </div>

          {/* Smooth Interpolating Frosted Progress Bar */}
          <div className="frost-progress-track">
            <div 
              className="frost-progress-bar" 
              style={{ width: `${progressPercent}%` }} 
            />
          </div>
        </div>

        {/* Step Contents */}
        <div className="p-6 md:p-8 flex-1 relative z-10">
          {/* Step 1: Welcome & Identity */}
          {step === 1 && (
            <div className="frost-step-animated flex flex-col items-center text-center">
              {/* Emblem / Audio Wave */}
              <div className="relative mb-6">
                <div className="size-20 rounded-3xl bg-gradient-to-tr from-[#fa586a]/20 to-purple-500/20 border border-white/20 flex items-center justify-center shadow-2xl backdrop-blur-xl">
                  <LogoMark size={40} className="text-white drop-shadow-[0_0_12px_rgba(250,88,106,0.6)]" />
                </div>
                {/* Floating Wave Bars */}
                <div className="absolute -bottom-2 -right-3 px-2 py-1 rounded-full bg-black/60 border border-white/15 backdrop-blur-md">
                  <div className="frost-audio-wave">
                    <span className="frost-wave-bar" style={{ animationDelay: '0.1s' }} />
                    <span className="frost-wave-bar" style={{ animationDelay: '0.35s' }} />
                    <span className="frost-wave-bar" style={{ animationDelay: '0.2s' }} />
                    <span className="frost-wave-bar" style={{ animationDelay: '0.45s' }} />
                    <span className="frost-wave-bar" style={{ animationDelay: '0.15s' }} />
                  </div>
                </div>
              </div>

              <h2 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight mb-2">
                Welcome to Hush Music
              </h2>
              <p className="text-sm md:text-base text-white/60 max-w-sm mb-8 leading-relaxed">
                Bit-perfect high fidelity audio, zero clutter, and pure sonic immersion tailored just for you.
              </p>

              <div className="w-full text-left">
                <label className="block text-xs font-semibold uppercase tracking-wider text-white/70 mb-2 px-1">
                  What should we call you?
                </label>
                <input
                  type="text"
                  className="frost-text-input"
                  placeholder="Your name or handle"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && canContinue()) handleNext();
                  }}
                  autoFocus
                />
                <p className="text-xs text-white/40 mt-2 px-1">
                  We'll use this to personalize your home screen and Listen Together sessions.
                </p>
              </div>
            </div>
          )}

          {/* Step 2: Music Taste & Vibe */}
          {step === 2 && (
            <div className="frost-step-animated flex flex-col">
              <div className="mb-6">
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-semibold mb-2">
                  <Sparkles size={13} />
                  <span>Taste Profile</span>
                </div>
                <h2 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
                  Tailor your sound
                </h2>
                <p className="text-sm text-white/60 mt-1">
                  Select 1 to 3 genres and vibes to shape your personalized home feed and dynamic mixes.
                </p>
              </div>

              {/* Pills Grid / Cloud */}
              <div className="flex flex-wrap gap-2.5 max-h-[290px] overflow-y-auto pr-1 py-1 [scrollbar-width:thin]">
                {GENRE_LIST.map((genre) => {
                  const selected = selectedGenres.includes(genre);
                  return (
                    <button
                      key={genre}
                      type="button"
                      onClick={() => toggleGenre(genre)}
                      className={`frost-pill-item ${selected ? 'frost-pill-item-selected' : ''}`}
                    >
                      {selected && <Check size={14} className="text-white flex-shrink-0" />}
                      <span>{genre}</span>
                    </button>
                  );
                })}
              </div>

              <div className="mt-4 flex items-center justify-between text-xs text-white/50 px-1">
                <span>{selectedGenres.length} selected</span>
                <span>Minimum 1 required</span>
              </div>
            </div>
          )}

          {/* Step 3: Audio Quality */}
          {step === 3 && (
            <div className="frost-step-animated flex flex-col">
              <div className="mb-6">
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 text-xs font-semibold mb-2">
                  <Headphones size={13} />
                  <span>Acoustic Fidelity</span>
                </div>
                <h2 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
                  Choose your fidelity
                </h2>
                <p className="text-sm text-white/60 mt-1">
                  Configure default streaming and playback engine resolution. You can fine-tune this in Settings anytime.
                </p>
              </div>

              {/* Quality Cards */}
              <div className="flex flex-col gap-3">
                {QUALITY_OPTIONS.map((opt) => {
                  const selected = selectedQuality === opt.id;
                  const Icon = opt.icon;
                  return (
                    <div
                      key={opt.id}
                      onClick={() => setSelectedQuality(opt.id)}
                      className={`frost-quality-card ${selected ? 'frost-quality-card-selected' : ''}`}
                    >
                      <div className="flex items-center justify-between mb-1.5">
                        <div className="flex items-center gap-2.5">
                          <div className={`p-2 rounded-xl ${selected ? 'bg-rose-500/20 text-rose-300' : 'bg-white/10 text-white/60'}`}>
                            <Icon size={18} />
                          </div>
                          <div>
                            <span className="font-bold text-white text-base mr-2">{opt.title}</span>
                            <span className="text-xs px-2 py-0.5 rounded-full bg-white/10 text-white/80 font-mono">
                              {opt.bitrate}
                            </span>
                          </div>
                        </div>

                        <div className="flex items-center gap-2">
                          {opt.badge && (
                            <span className="text-[11px] font-bold px-2 py-0.5 rounded-full bg-rose-500/20 border border-rose-500/40 text-rose-300">
                              {opt.badge}
                            </span>
                          )}
                          <div className={`size-5 rounded-full border flex items-center justify-center transition-all ${
                            selected 
                              ? 'bg-rose-500 border-rose-400 shadow-[0_0_10px_rgba(250,88,106,0.6)]' 
                              : 'border-white/20 bg-transparent'
                          }`}>
                            {selected && <Check size={12} className="text-white stroke-[3]" />}
                          </div>
                        </div>
                      </div>
                      <p className="text-xs text-white/60 pl-11">{opt.detail}</p>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Step 4: Smart Downloads & Offline Sync */}
          {step === 4 && (
            <div className="frost-step-animated flex flex-col">
              <div className="mb-5">
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-semibold mb-2">
                  <DownloadCloud size={13} />
                  <span>Offline Sync</span>
                </div>
                <h2 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
                  Never lose the rhythm
                </h2>
                <p className="text-sm text-white/60 mt-1">
                  Keep your favorite songs and daily selections ready even when you lose signal.
                </p>
              </div>

              <div className="flex flex-col gap-3 mb-5">
                {/* Toggle 1: Smart Downloads */}
                <div className="frost-toggle-row">
                  <div className="flex items-start gap-3">
                    <div className="p-2 rounded-xl bg-white/10 text-white/70 mt-0.5">
                      <Zap size={16} />
                    </div>
                    <div>
                      <div className="font-bold text-sm text-white">Smart Downloads</div>
                      <div className="text-xs text-white/60 leading-normal">
                        Automatically cache your top tracks and daily mixes over Wi-Fi.
                      </div>
                    </div>
                  </div>
                  <div 
                    onClick={() => setSmartDownloads(!smartDownloads)} 
                    className={`frost-toggle-track ${smartDownloads ? 'frost-toggle-track-active' : ''}`}
                  >
                    <div className={`frost-toggle-thumb ${smartDownloads ? 'frost-toggle-thumb-active' : ''}`} />
                  </div>
                </div>

                {/* Toggle 2: Wi-Fi Only */}
                <div className="frost-toggle-row">
                  <div className="flex items-start gap-3">
                    <div className="p-2 rounded-xl bg-white/10 text-white/70 mt-0.5">
                      <Wifi size={16} />
                    </div>
                    <div>
                      <div className="font-bold text-sm text-white">Wi-Fi Only</div>
                      <div className="text-xs text-white/60 leading-normal">
                        Prevent mobile data usage for offline caching and automatic track sync.
                      </div>
                    </div>
                  </div>
                  <div 
                    onClick={() => setWifiOnly(!wifiOnly)} 
                    className={`frost-toggle-track ${wifiOnly ? 'frost-toggle-track-active' : ''}`}
                  >
                    <div className={`frost-toggle-thumb ${wifiOnly ? 'frost-toggle-thumb-active' : ''}`} />
                  </div>
                </div>

                {/* Storage Cap Selector */}
                <div className="p-4 rounded-2xl bg-white/[0.04] border border-white/10">
                  <div className="flex items-center justify-between mb-2.5">
                    <div className="flex items-center gap-2 text-xs font-bold text-white uppercase tracking-wider">
                      <HardDrive size={14} className="text-white/60" />
                      <span>Storage Cap</span>
                    </div>
                    <span className="text-xs font-semibold text-rose-400">{storageCap} Allocated</span>
                  </div>

                  <div className="frost-segmented">
                    {STORAGE_CAPS.map((cap) => (
                      <button
                        key={cap}
                        type="button"
                        onClick={() => setStorageCap(cap)}
                        className={`frost-segment-item ${storageCap === cap ? 'frost-segment-item-active' : ''}`}
                      >
                        {cap}
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Footer Navigation Bar */}
        <div className="p-6 pt-4 border-t border-white/10 relative z-10 flex items-center justify-between gap-4">
          {step > 1 ? (
            <button
              type="button"
              onClick={handleBack}
              className="frost-ghost-button"
            >
              <ChevronLeft size={16} />
              <span>Back</span>
            </button>
          ) : (
            <div className="text-xs text-white/40 pl-2">
              Hush Music v1.9.1
            </div>
          )}

          {step < 4 ? (
            <button
              type="button"
              disabled={!canContinue()}
              onClick={handleNext}
              className="frost-cta-button max-w-[200px]"
            >
              <span>Continue</span>
              <ChevronRight size={16} />
            </button>
          ) : (
            <button
              type="button"
              onClick={handleFinish}
              className="frost-cta-button frost-start-cta"
            >
              <Sparkles size={18} />
              <span>Start Listening</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
