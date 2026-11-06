import React from "react";
import { FONTS, HEADLINE_TOP } from "../tokens";

interface HeadlineProps {
  lines: string[];
  highlight?: string;
  color: string;
  top?: number;
  size?: number;
  opacity?: number;
  translateY?: number;
  underline?: number;
  align?: "left" | "center";
  monoLabel?: string;
  monoLabelColor?: string;
  monoLabelOpacity?: number;
  width?: number;
}

export const Headline: React.FC<HeadlineProps> = ({
  lines,
  highlight,
  color,
  top = HEADLINE_TOP,
  size = 88,
  opacity = 1,
  translateY = 0,
  underline = 0,
  align = "left",
  monoLabel,
  monoLabelColor,
  monoLabelOpacity = 1,
  width = 864,
}) => {
  return (
    <div
      style={{
        position: "absolute",
        left: 96,
        top,
        width,
        opacity,
        transform: `translateY(${translateY}px)`,
        textAlign: align,
        zIndex: 20,
      }}
    >
      {monoLabel ? (
        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: align === "center" ? "center" : "flex-start",
            gap: 8,
            marginBottom: 18,
            opacity: monoLabelOpacity,
            fontFamily: FONTS.mono,
            fontSize: 24,
            fontWeight: 500,
            letterSpacing: "0.14em",
            color: monoLabelColor ?? color,
            textTransform: "uppercase",
          }}
        >
          <span style={{ width: 7, height: 7, borderRadius: "50%", backgroundColor: monoLabelColor ?? color }} />
          {monoLabel}
        </div>
      ) : null}
      {lines.map((line, index) => {
        const hasHighlight = highlight ? line.includes(highlight) : false;
        const before = hasHighlight && highlight ? line.slice(0, line.indexOf(highlight)) : line;
        const after = hasHighlight && highlight ? line.slice(line.indexOf(highlight) + highlight.length) : "";
        return (
          <div
            key={index}
            style={{
              fontFamily: FONTS.sans,
              fontSize: size,
              lineHeight: 1.05,
              fontWeight: 800,
              letterSpacing: "-0.03em",
              color,
              whiteSpace: "nowrap",
            }}
          >
            {hasHighlight && highlight ? (
              <>
                {before}
                <span style={{ position: "relative", display: "inline-block" }}>
                  {highlight}
                  <svg
                    viewBox="0 0 300 18"
                    preserveAspectRatio="none"
                    style={{
                      position: "absolute",
                      left: -4,
                      bottom: size * 0.02,
                      width: "calc(100% + 8px)",
                      height: size * 0.15,
                      overflow: "visible",
                    }}
                  >
                    <path
                      d="M4 13 C 62 4, 214 4, 296 10"
                      fill="none"
                      stroke="#9E1B32"
                      strokeWidth={7}
                      strokeLinecap="round"
                      pathLength={1}
                      strokeDasharray={1}
                      strokeDashoffset={1 - Math.max(0, Math.min(1, underline))}
                    />
                  </svg>
                </span>
                {after}
              </>
            ) : (
              line
            )}
          </div>
        );
      })}
    </div>
  );
};
