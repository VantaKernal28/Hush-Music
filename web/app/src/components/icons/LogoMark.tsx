import hushLogoUrl from '@/assets/hush-logo.png';

/**
 * Hush Music logo mark from the Android application assets.
 */
export function LogoMark({ size = 22, className }: { size?: number; className?: string }) {
  return (
    <img
      src={hushLogoUrl}
      alt="Hush Music"
      className={`inline-block shrink-0 ${className || ''}`}
      style={{ width: size, height: size, objectFit: 'contain' }}
    />
  );
}
