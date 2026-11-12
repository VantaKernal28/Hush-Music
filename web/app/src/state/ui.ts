import { create } from 'zustand';

export type SidePanel = 'up-next' | 'lyrics' | null;

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
  /** The interactive 4-step Frost Glass onboarding wizard. */
  onboardingOpen: boolean;
  openOnboarding: () => void;
  closeOnboarding: () => void;
}

const BANNER_KEY = 'hush:install-banner-dismissed';
const ONBOARDING_KEY = 'hasCompletedOnboarding';

export const useUi = create<UiState>()((set) => ({
  sidePanel: null,
  toggleSidePanel: (panel) => set((s) => ({ sidePanel: s.sidePanel === panel ? null : panel })),
  closeSidePanel: () => set({ sidePanel: null }),
  fullScreen: false,
  setFullScreen: (fullScreen) => set({ fullScreen }),
  drawerOpen: false,
  setDrawerOpen: (drawerOpen) => set({ drawerOpen }),
  installBannerDismissed: sessionStorage.getItem(BANNER_KEY) === '1',
  dismissInstallBanner: () => {
    sessionStorage.setItem(BANNER_KEY, '1');
    set({ installBannerDismissed: true });
  },
  onboardingOpen: typeof window !== 'undefined' ? localStorage.getItem(ONBOARDING_KEY) !== 'true' : false,
  openOnboarding: () => set({ onboardingOpen: true }),
  closeOnboarding: () => {
    try {
      localStorage.setItem(ONBOARDING_KEY, 'true');
    } catch {}
    set({ onboardingOpen: false });
  },
}));

// For browser-driven tests, next to __bcPlayer.
if (import.meta.env.DEV) Object.assign(window, { __hushUi: useUi });
