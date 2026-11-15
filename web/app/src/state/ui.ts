import { create } from 'zustand';

export type SidePanel = 'up-next' | 'lyrics' | null;
export type ThemeMode = 'classic' | 'm3e';

const THEME_KEY = 'hush:theme-mode';
const BANNER_KEY = 'hush:install-banner-dismissed';

interface UiState {
  /** The right-hand glass panel (report §7); one of Up Next or Lyrics at a time. */
  sidePanel: SidePanel;
  toggleSidePanel: (panel: Exclude<SidePanel, null>) => void;
  closeSidePanel: () => void;
  /** The full-screen player: artwork, controls and lyrics over the artwork's mesh (report §7). */
  fullScreen: boolean;
  setFullScreen: (open: boolean) => void;
  /** The sidebar as a drawer, below 484px where it's otherwise hidden. */
  drawerOpen: boolean;
  setDrawerOpen: (open: boolean) => void;
  /** The extension install banner, dismissed for this tab only. */
  installBannerDismissed: boolean;
  dismissInstallBanner: () => void;
  /** UI Style: 'classic' (BitChord Apple Music Frosted Glass) or 'm3e' (Hush Material 3 Expressive) */
  themeMode: ThemeMode;
  setThemeMode: (mode: ThemeMode) => void;
  toggleThemeMode: () => void;
  /** AI Playlist Maker Modal */
  aiPlaylistModalOpen: boolean;
  openAiPlaylistModal: () => void;
  closeAiPlaylistModal: () => void;
}

const initialTheme = (typeof window !== 'undefined' && (localStorage.getItem(THEME_KEY) as ThemeMode)) || 'm3e';

export const useUi = create<UiState>()((set) => ({
  sidePanel: null,
  toggleSidePanel: (panel) => set((s) => ({ sidePanel: s.sidePanel === panel ? null : panel })),
  closeSidePanel: () => set({ sidePanel: null }),
  fullScreen: false,
  setFullScreen: (fullScreen) => set({ fullScreen }),
  drawerOpen: false,
  setDrawerOpen: (drawerOpen) => set({ drawerOpen }),
  installBannerDismissed: typeof window !== 'undefined' && sessionStorage.getItem(BANNER_KEY) === '1',
  dismissInstallBanner: () => {
    sessionStorage.setItem(BANNER_KEY, '1');
    set({ installBannerDismissed: true });
  },
  themeMode: initialTheme,
  setThemeMode: (themeMode) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem(THEME_KEY, themeMode);
      document.documentElement.dataset.theme = themeMode;
    }
    set({ themeMode });
  },
  toggleThemeMode: () =>
    set((s) => {
      const next: ThemeMode = s.themeMode === 'm3e' ? 'classic' : 'm3e';
      if (typeof window !== 'undefined') {
        localStorage.setItem(THEME_KEY, next);
        document.documentElement.dataset.theme = next;
      }
      return { themeMode: next };
    }),
  aiPlaylistModalOpen: false,
  openAiPlaylistModal: () => set({ aiPlaylistModalOpen: true }),
  closeAiPlaylistModal: () => set({ aiPlaylistModalOpen: false }),
}));

if (typeof window !== 'undefined') {
  document.documentElement.dataset.theme = initialTheme;
}

// For browser-driven tests, next to __bcPlayer.
if (import.meta.env.DEV) Object.assign(window, { __bcUi: useUi });
