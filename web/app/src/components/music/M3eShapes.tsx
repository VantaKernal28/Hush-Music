import React from 'react';

function generateRosettePoints(petals: number, r0 = 0.44, amp = 0.05, steps = 72): string {
  const pts: string[] = [];
  for (let i = 0; i < steps; i++) {
    const theta = (i / steps) * 2 * Math.PI - Math.PI / 2;
    const r = r0 + amp * Math.cos(petals * theta);
    const x = 0.5 + r * Math.cos(theta);
    const y = 0.5 + r * Math.sin(theta);
    pts.push(`${x.toFixed(4)},${y.toFixed(4)}`);
  }
  return pts.join(' ');
}

function generateStarburstPoints(points = 8, rOuter = 0.49, rInner = 0.35): string {
  const pts: string[] = [];
  const total = points * 2;
  for (let i = 0; i < total; i++) {
    const theta = (i / total) * 2 * Math.PI - Math.PI / 2;
    const r = i % 2 === 0 ? rOuter : rInner;
    const x = 0.5 + r * Math.cos(theta);
    const y = 0.5 + r * Math.sin(theta);
    pts.push(`${x.toFixed(4)},${y.toFixed(4)}`);
  }
  return pts.join(' ');
}

function generateConcaveSquirclePoints(steps = 48): string {
  const pts: string[] = [];
  for (let i = 0; i < steps; i++) {
    const theta = (i / steps) * 2 * Math.PI;
    const cos4 = Math.cos(4 * theta);
    const r = 0.44 + 0.05 * cos4;
    const x = 0.5 + r * Math.cos(theta);
    const y = 0.5 + r * Math.sin(theta);
    pts.push(`${x.toFixed(4)},${y.toFixed(4)}`);
  }
  return pts.join(' ');
}

const SCALLOP_12_POINTS = generateRosettePoints(12, 0.44, 0.05, 72);
const SCALLOP_8_POINTS = generateRosettePoints(8, 0.43, 0.06, 64);
const SCALLOP_4_POINTS = generateRosettePoints(4, 0.39, 0.09, 48);
const STARBURST_POINTS = generateStarburstPoints(8, 0.49, 0.35);
const CONCAVE_SQUIRCLE_POINTS = generateConcaveSquirclePoints(64);

/**
 * Global SVG ClipPath definitions for Material 3 Expressive shapes.
 * Mounted once at app root.
 */
export function M3eShapeDefs() {
  return (
    <svg aria-hidden="true" focusable="false" className="absolute size-0 pointer-events-none opacity-0">
      <defs>
        <clipPath id="m3e-shape-scallop-12" clipPathUnits="objectBoundingBox">
          <polygon points={SCALLOP_12_POINTS} />
        </clipPath>
        <clipPath id="m3e-shape-scallop-8" clipPathUnits="objectBoundingBox">
          <polygon points={SCALLOP_8_POINTS} />
        </clipPath>
        <clipPath id="m3e-shape-scallop-4" clipPathUnits="objectBoundingBox">
          <polygon points={SCALLOP_4_POINTS} />
        </clipPath>
        <clipPath id="m3e-shape-starburst" clipPathUnits="objectBoundingBox">
          <polygon points={STARBURST_POINTS} />
        </clipPath>
        <clipPath id="m3e-shape-concave-squircle" clipPathUnits="objectBoundingBox">
          <polygon points={CONCAVE_SQUIRCLE_POINTS} />
        </clipPath>
      </defs>
    </svg>
  );
}

export type M3eShapeKind =
  | 'squircle'
  | 'pill'
  | 'oval'
  | 'arch'
  | 'diagonal-pill'
  | 'teardrop'
  | 'pebble'
  | 'scallop-4'
  | 'scallop-8'
  | 'scallop-12'
  | 'concave-squircle'
  | 'starburst';

export const M3E_SHAPES: M3eShapeKind[] = [
  'squircle',
  'pill',
  'oval',
  'arch',
  'diagonal-pill',
  'teardrop',
  'pebble',
  'scallop-4',
  'scallop-8',
  'scallop-12',
  'concave-squircle',
  'starburst',
];

export function getM3eShapeStyle(indexOrKind: number | string): React.CSSProperties {
  let kind: M3eShapeKind;
  if (typeof indexOrKind === 'number') {
    const idx = Math.abs(indexOrKind) % M3E_SHAPES.length;
    kind = M3E_SHAPES[idx] ?? 'squircle';
  } else if (M3E_SHAPES.includes(indexOrKind as M3eShapeKind)) {
    kind = indexOrKind as M3eShapeKind;
  } else {
    kind = 'squircle';
  }

  switch (kind) {
    case 'squircle':
      return { borderRadius: '28%' };
    case 'pill':
      return { borderRadius: '9999px' };
    case 'oval':
      return { borderRadius: '50% / 38%' };
    case 'arch':
      return { borderRadius: '9999px 9999px 24px 24px' };
    case 'diagonal-pill':
      return { borderRadius: '50% 12% 50% 12%' };
    case 'teardrop':
      return { borderRadius: '50% 50% 12% 50%' };
    case 'pebble':
      return { borderRadius: '60% 40% 30% 70% / 60% 30% 70% 40%' };
    case 'scallop-4':
      return { clipPath: 'url(#m3e-shape-scallop-4)', WebkitClipPath: 'url(#m3e-shape-scallop-4)' };
    case 'scallop-8':
      return { clipPath: 'url(#m3e-shape-scallop-8)', WebkitClipPath: 'url(#m3e-shape-scallop-8)' };
    case 'scallop-12':
      return { clipPath: 'url(#m3e-shape-scallop-12)', WebkitClipPath: 'url(#m3e-shape-scallop-12)' };
    case 'concave-squircle':
      return { clipPath: 'url(#m3e-shape-concave-squircle)', WebkitClipPath: 'url(#m3e-shape-concave-squircle)' };
    case 'starburst':
      return { clipPath: 'url(#m3e-shape-starburst)', WebkitClipPath: 'url(#m3e-shape-starburst)' };
    default:
      return { borderRadius: '24px' };
  }
}
