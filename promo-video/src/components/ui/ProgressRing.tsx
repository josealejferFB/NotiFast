import React from "react";

interface ProgressRingProps {
  size: number;
  stroke: number;
  progress: number;
  color: string;
  track: string;
  children?: React.ReactNode;
  rotation?: number;
  style?: React.CSSProperties;
}

export const ProgressRing: React.FC<ProgressRingProps> = ({
  size,
  stroke,
  progress,
  color,
  track,
  children,
  rotation = -90,
  style,
}) => {
  const radius = (size - stroke) / 2;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference * (1 - Math.max(0, Math.min(1, progress)));

  return (
    <div
      style={{
        width: size,
        height: size,
        position: "relative",
        flexShrink: 0,
        ...style,
      }}
    >
      <svg width={size} height={size} style={{ display: "block", transform: `rotate(${rotation}deg)` }}>
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={track}
          strokeWidth={stroke}
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={color}
          strokeWidth={stroke}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
        />
      </svg>
      <div
        style={{
          position: "absolute",
          inset: 0,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          flexDirection: "column",
        }}
      >
        {children}
      </div>
    </div>
  );
};

export const CheckMark: React.FC<{
  size?: number;
  color: string;
  progress?: number;
  strokeWidth?: number;
  style?: React.CSSProperties;
}> = ({ size = 24, color, progress = 1, strokeWidth = 2.2, style }) => (
  <svg
    width={size}
    height={size}
    viewBox="0 0 24 24"
    style={{ display: "block", ...style }}
    aria-hidden="true"
  >
    <path
      d="M4.5 12.4 9.4 17.3 19.8 6.6"
      fill="none"
      stroke={color}
      strokeWidth={strokeWidth}
      strokeLinecap="round"
      strokeLinejoin="round"
      pathLength={1}
      strokeDasharray={1}
      strokeDashoffset={1 - Math.max(0, Math.min(1, progress))}
    />
  </svg>
);
