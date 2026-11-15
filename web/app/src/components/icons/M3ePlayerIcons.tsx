export function Rewind30Icon({ size = 26, className = '' }: { size?: number; className?: string }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
      aria-hidden="true"
    >
      <path d="M3.5 13a8.5 8.5 0 1 0 2.5-6L3.5 9.5" />
      <path d="M3.5 4.5v5h5" />
      <text
        x="12"
        y="15.2"
        fontSize="7.5"
        fontWeight="800"
        fontFamily="'Google Sans Flex', 'DM Sans', system-ui, sans-serif"
        textAnchor="middle"
        fill="currentColor"
        stroke="none"
      >
        30
      </text>
    </svg>
  );
}

export function Forward30Icon({ size = 26, className = '' }: { size?: number; className?: string }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
      aria-hidden="true"
    >
      <path d="M20.5 13a8.5 8.5 0 1 1-2.5-6L20.5 9.5" />
      <path d="M20.5 4.5v5h-5" />
      <text
        x="12"
        y="15.2"
        fontSize="7.5"
        fontWeight="800"
        fontFamily="'Google Sans Flex', 'DM Sans', system-ui, sans-serif"
        textAnchor="middle"
        fill="currentColor"
        stroke="none"
      >
        30
      </text>
    </svg>
  );
}
