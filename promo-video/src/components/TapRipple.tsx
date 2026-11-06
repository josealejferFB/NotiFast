import React from "react";
import { useCurrentFrame } from "remotion";

interface TapRippleProps {
  x: number;
  y: number;
  start: number;
  color?: string;
  maxSize?: number;
  duration?: number;
}

/** Onda tactil determinista, en coordenadas logicas del telefono. */
export const TapRipple: React.FC<TapRippleProps> = ({
  x,
  y,
  start,
  color = "#FFFFFF",
  maxSize = 150,
  duration = 18,
}) => {
  const frame = useCurrentFrame();
  const t = (frame - start) / duration;
  if (t < 0 || t > 1) {
    return null;
  }
  const size = 8 + t * maxSize;
  return (
    <div
      style={{
        position: "absolute",
        left: x - size / 2,
        top: y - size / 2,
        width: size,
        height: size,
        borderRadius: "50%",
        border: `1.5px solid ${color}`,
        opacity: 0.55 * (1 - t),
        pointerEvents: "none",
        zIndex: 90,
      }}
    />
  );
};
