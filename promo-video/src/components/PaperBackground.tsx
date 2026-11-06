import React from "react";
import { AbsoluteFill, staticFile, useCurrentFrame } from "remotion";
import { themeFor } from "../tokens";

interface PaperBackgroundProps {
  mode?: "light" | "dark";
  variant?: "default" | "cta";
  dim?: number;
}

// Official subject and brand color tokens for background elements
const BRAND_COLORS = {
  ruby: "#9E1B32", // Cálculo Diferencial / Brand Primary
  rubyLight: "#C41E3A",
  rubyDark: "#E05A6E",
  aqua: "#00A896", // Física General / Brand Secondary
  aquaLight: "#028090",
  aquaDark: "#2DD4BF",
  indigo: "#4338CA", // Programación I
  indigoLight: "#6366F1",
  amber: "#D97706", // Inglés Instrumental / Ideas
  amberLight: "#F59E0B",
  emerald: "#15803D", // Estadística
  emeraldLight: "#10B981",
  cyan: "#0284C7", // Estudio / Portadas
  cyanLight: "#38BDF8",
};

/**
 * Educational floating subject SVGs with official NotiFast subject colors,
 * large dimensions (88px–126px), prominent visibility and organic floating kinematics.
 */
const FloatingSubjectDoodles: React.FC<{ isLight: boolean }> = ({ isLight }) => {
  const frame = useCurrentFrame();

  const c = {
    ruby: isLight ? BRAND_COLORS.ruby : BRAND_COLORS.rubyDark,
    aqua: isLight ? BRAND_COLORS.aqua : BRAND_COLORS.aquaDark,
    indigo: isLight ? BRAND_COLORS.indigo : BRAND_COLORS.indigoLight,
    amber: isLight ? BRAND_COLORS.amber : BRAND_COLORS.amberLight,
    emerald: isLight ? BRAND_COLORS.emerald : BRAND_COLORS.emeraldLight,
    cyan: isLight ? BRAND_COLORS.cyan : BRAND_COLORS.cyanLight,
  };

  const alphaMultiplier = isLight ? 0.32 : 0.38;

  const DOODLES = [
    // Top-left area (flanking and above headline)
    {
      id: "integral-tl",
      x: 36,
      y: 60,
      size: 96,
      color: c.ruby,
      fill: `${c.ruby}18`,
      alpha: 0.95 * alphaMultiplier,
      speed: 0.026,
      rotSpeed: 0.018,
      phase: 0.4,
      type: "integral",
    },
    {
      id: "flask-tl",
      x: 180,
      y: 110,
      size: 88,
      color: c.aqua,
      fill: `${c.aqua}1A`,
      alpha: 0.9 * alphaMultiplier,
      speed: 0.032,
      rotSpeed: 0.022,
      phase: 1.8,
      type: "flask",
    },

    // Top-right area
    {
      id: "atom-tr",
      x: 880,
      y: 80,
      size: 118,
      color: c.aqua,
      fill: `${c.aqua}1C`,
      alpha: 1.0 * alphaMultiplier,
      speed: 0.028,
      rotSpeed: 0.024,
      phase: 2.5,
      type: "atom",
    },
    {
      id: "cap-tr",
      x: 750,
      y: 40,
      size: 92,
      color: c.ruby,
      fill: `${c.ruby}1A`,
      alpha: 0.88 * alphaMultiplier,
      speed: 0.03,
      rotSpeed: 0.016,
      phase: 0.9,
      type: "cap",
    },

    // Mid-left flank (left of phone screen)
    {
      id: "compass-ml",
      x: 20,
      y: 560,
      size: 104,
      color: c.cyan,
      fill: `${c.cyan}16`,
      alpha: 0.92 * alphaMultiplier,
      speed: 0.027,
      rotSpeed: 0.019,
      phase: 3.4,
      type: "compass",
    },
    {
      id: "code-ml",
      x: 28,
      y: 920,
      size: 98,
      color: c.indigo,
      fill: `${c.indigo}1A`,
      alpha: 0.95 * alphaMultiplier,
      speed: 0.029,
      rotSpeed: 0.021,
      phase: 1.3,
      type: "code",
    },
    {
      id: "pi-ml",
      x: 24,
      y: 1280,
      size: 90,
      color: c.ruby,
      fill: `${c.ruby}18`,
      alpha: 0.9 * alphaMultiplier,
      speed: 0.031,
      rotSpeed: 0.017,
      phase: 4.2,
      type: "pi",
    },
    {
      id: "calc-bl",
      x: 26,
      y: 1620,
      size: 100,
      color: c.emerald,
      fill: `${c.emerald}1A`,
      alpha: 0.92 * alphaMultiplier,
      speed: 0.028,
      rotSpeed: 0.02,
      phase: 2.1,
      type: "calc",
    },

    // Mid-right flank (right of phone screen)
    {
      id: "book-mr",
      x: 934,
      y: 580,
      size: 110,
      color: c.amber,
      fill: `${c.amber}18`,
      alpha: 0.95 * alphaMultiplier,
      speed: 0.027,
      rotSpeed: 0.018,
      phase: 2.8,
      type: "book",
    },
    {
      id: "lightbulb-mr",
      x: 940,
      y: 960,
      size: 94,
      color: c.amber,
      fill: `${c.amber}1C`,
      alpha: 0.92 * alphaMultiplier,
      speed: 0.033,
      rotSpeed: 0.023,
      phase: 0.7,
      type: "lightbulb",
    },
    {
      id: "sigma-mr",
      x: 942,
      y: 1310,
      size: 92,
      color: c.emerald,
      fill: `${c.emerald}18`,
      alpha: 0.9 * alphaMultiplier,
      speed: 0.026,
      rotSpeed: 0.019,
      phase: 3.6,
      type: "sigma",
    },
    {
      id: "atom-br",
      x: 920,
      y: 1640,
      size: 114,
      color: c.aqua,
      fill: `${c.aqua}1A`,
      alpha: 0.95 * alphaMultiplier,
      speed: 0.03,
      rotSpeed: 0.022,
      phase: 1.5,
      type: "atom",
    },
  ];

  return (
    <div style={{ position: "absolute", inset: 0, pointerEvents: "none" }}>
      {DOODLES.map((d) => {
        const floatY = Math.sin(frame * d.speed + d.phase) * 24;
        const floatX = Math.cos(frame * d.speed * 0.7 + d.phase) * 12;
        const rot = Math.sin(frame * d.rotSpeed + d.phase) * 11;
        const scale = 1 + Math.sin(frame * d.speed * 0.5 + d.phase) * 0.04;

        return (
          <div
            key={d.id}
            style={{
              position: "absolute",
              left: d.x + floatX,
              top: d.y + floatY,
              width: d.size,
              height: d.size,
              opacity: d.alpha,
              transform: `rotate(${rot}deg) scale(${scale})`,
              transformOrigin: "center center",
              filter: `drop-shadow(0 6px 14px ${d.color}28)`,
            }}
          >
            {d.type === "flask" && (
              <svg width={d.size} height={d.size} viewBox="0 0 48 48" fill="none" stroke={d.color} strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round">
                <path d="M19 6h10M24 6v12l11 18a4 4 0 0 1-3.4 6H16.4a4 4 0 0 1-3.4-6l11-18V6" fill={d.fill} />
                <path d="M15 32h18" strokeDasharray="3 3" />
                <circle cx="21" cy="36" r="2" fill={d.color} stroke="none" />
                <circle cx="27" cy="34" r="1.5" fill={d.color} stroke="none" />
              </svg>
            )}

            {d.type === "integral" && (
              <svg width={d.size} height={d.size} viewBox="0 0 48 48" fill="none" stroke={d.color} strokeWidth="2.8" strokeLinecap="round">
                <path d="M28 7c-5 0-7 3.5-7 8v18c0 4.5-2 8-7 8" />
                <text x="25" y="27" fill={d.color} fontSize="14" fontWeight="800" fontFamily="sans-serif" stroke="none">
                  f(x)
                </text>
              </svg>
            )}

            {d.type === "atom" && (
              <svg width={d.size} height={d.size} viewBox="0 0 48 48" fill="none" stroke={d.color} strokeWidth="2.4">
                <ellipse cx="24" cy="24" rx="21" ry="8" transform="rotate(30 24 24)" fill={d.fill} />
                <ellipse cx="24" cy="24" rx="21" ry="8" transform="rotate(-30 24 24)" fill={d.fill} />
                <ellipse cx="24" cy="24" rx="21" ry="8" transform="rotate(90 24 24)" fill={d.fill} />
                <circle cx="24" cy="24" r="4" fill={d.color} stroke="none" />
              </svg>
            )}

            {d.type === "cap" && (
              <svg width={d.size} height={d.size} viewBox="0 0 48 48" fill="none" stroke={d.color} strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round">
                <path d="M24 7L3 17l21 11 21-11L24 7z" fill={d.fill} />
                <path d="M9 21v11c0 5 6.7 9 15 9s15-4 15-9V21" fill={d.fill} />
                <path d="M45 19v15" strokeWidth="2.8" />
                <circle cx="45" cy="35" r="2.5" fill={d.color} stroke="none" />
              </svg>
            )}

            {d.type === "compass" && (
              <svg width={d.size} height={d.size} viewBox="0 0 48 48" fill="none" stroke={d.color} strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="24" cy="9" r="4.5" fill={d.fill} />
                <path d="M21 14L9 43M27 14L39 43M14 31h20" />
                <circle cx="24" cy="9" r="2" fill={d.color} stroke="none" />
              </svg>
            )}

            {d.type === "code" && (
              <svg width={d.size} height={d.size} viewBox="0 0 48 48" fill="none" stroke={d.color} strokeWidth="2.8" strokeLinecap="round" strokeLinejoin="round">
                <path d="M15 12L4 24l11 12M33 12l11 12-11 12M28 8L20 40" />
              </svg>
            )}

            {d.type === "pi" && (
              <svg width={d.size} height={d.size} viewBox="0 0 48 48" fill="none" stroke={d.color} strokeWidth="2.8" strokeLinecap="round">
                <path d="M7 13h34M17 13v24M31 13v21c0 3 2.5 4 4.5 4" />
              </svg>
            )}

            {d.type === "book" && (
              <svg width={d.size} height={d.size} viewBox="0 0 48 48" fill="none" stroke={d.color} strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round">
                <path d="M5 9c6.5-3.5 13-2 19 2 6-4 12.5-5.5 19-2v27c-6.5-3.5-13-2-19 2-6-4-12.5-5.5-19-2V9z" fill={d.fill} />
                <path d="M24 11v27" />
              </svg>
            )}

            {d.type === "lightbulb" && (
              <svg width={d.size} height={d.size} viewBox="0 0 48 48" fill="none" stroke={d.color} strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round">
                <path d="M17 35h14M19 40h10M24 6a14 14 0 0 0-10 24c1.8 2.2 2.5 4.5 2.5 7h15c0-2.5.7-4.8 2.5-7A14 14 0 0 0 24 6z" fill={d.fill} />
                <path d="M21 21l3-4 3 4" strokeWidth="2.2" />
              </svg>
            )}

            {d.type === "sigma" && (
              <svg width={d.size} height={d.size} viewBox="0 0 48 48" fill="none" stroke={d.color} strokeWidth="2.8" strokeLinecap="round" strokeLinejoin="round">
                <path d="M38 9H13l13 15-13 15h25" fill={d.fill} />
              </svg>
            )}

            {d.type === "calc" && (
              <svg width={d.size} height={d.size} viewBox="0 0 48 48" fill="none" stroke={d.color} strokeWidth="2.6" strokeLinecap="round">
                <rect x="9" y="5" width="30" height="38" rx="7" fill={d.fill} />
                <rect x="15" y="10" width="18" height="8" rx="2.5" fill={`${d.color}25`} />
                <path d="M15 24h5M28 24h5M15 31h5M28 31h5M15 37h5M28 37h5" strokeWidth="2.4" />
              </svg>
            )}
          </div>
        );
      })}
    </div>
  );
};

/**
 * Dynamic, colorful, and living studio background for NotiFast promo video.
 * Incorporates continuous orbital Ruby & Aqua auroras, layered chromatic mesh,
 * and prominent floating subject iconography matching NotiFast study themes.
 */
export const PaperBackground: React.FC<PaperBackgroundProps> = ({
  mode = "light",
  variant = "default",
  dim = 1,
}) => {
  const frame = useCurrentFrame();
  const theme = themeFor(mode);
  const isLight = mode === "light";

  // Dynamic continuous motion for Ruby and Aqua auroras
  const rubyOrbX = 680 + Math.sin(frame * 0.016) * 190;
  const rubyOrbY = 360 + Math.cos(frame * 0.013) * 150;

  const aquaOrbX = 360 + Math.cos(frame * 0.015 + 1.8) * 210;
  const aquaOrbY = 1240 + Math.sin(frame * 0.017 + 1.2) * 170;

  const indigoOrbX = 540 + Math.sin(frame * 0.012 + 3.4) * 240;
  const indigoOrbY = 820 + Math.cos(frame * 0.014 + 2.1) * 190;

  const amberOrbX = 820 + Math.cos(frame * 0.018 + 0.6) * 160;
  const amberOrbY = 1580 + Math.sin(frame * 0.015 + 2.7) * 130;

  return (
    <AbsoluteFill
      style={{
        overflow: "hidden",
        opacity: dim,
        background: isLight
          ? "linear-gradient(155deg, #FBF2F4 0%, #EEF9F8 32%, #F6F4FA 65%, #FDF3F5 100%)"
          : "linear-gradient(160deg, #090E1A 0%, #0D1627 45%, #0B1322 100%)",
      }}
    >
      {/* Dynamic Ruby Aurora Orb */}
      <div
        style={{
          position: "absolute",
          left: rubyOrbX - 580,
          top: rubyOrbY - 580,
          width: 1160,
          height: 1160,
          borderRadius: "50%",
          background: isLight
            ? "radial-gradient(circle, rgba(158, 27, 50, 0.26) 0%, rgba(196, 30, 58, 0.15) 36%, rgba(225, 29, 72, 0.04) 64%, transparent 74%)"
            : "radial-gradient(circle, rgba(224, 90, 110, 0.28) 0%, rgba(158, 27, 50, 0.14) 40%, transparent 72%)",
          filter: "blur(32px)",
          pointerEvents: "none",
        }}
      />

      {/* Dynamic Aqua Aurora Orb */}
      <div
        style={{
          position: "absolute",
          left: aquaOrbX - 620,
          top: aquaOrbY - 620,
          width: 1240,
          height: 1240,
          borderRadius: "50%",
          background: isLight
            ? "radial-gradient(circle, rgba(0, 168, 150, 0.28) 0%, rgba(45, 212, 191, 0.16) 38%, rgba(13, 148, 136, 0.04) 66%, transparent 75%)"
            : "radial-gradient(circle, rgba(45, 212, 191, 0.30) 0%, rgba(0, 168, 150, 0.15) 42%, transparent 74%)",
          filter: "blur(36px)",
          pointerEvents: "none",
        }}
      />

      {/* Dynamic Indigo Depth Shimmer */}
      <div
        style={{
          position: "absolute",
          left: indigoOrbX - 480,
          top: indigoOrbY - 480,
          width: 960,
          height: 960,
          borderRadius: "50%",
          background: isLight
            ? "radial-gradient(circle, rgba(67, 56, 202, 0.14) 0%, rgba(99, 102, 241, 0.07) 45%, transparent 70%)"
            : "radial-gradient(circle, rgba(99, 102, 241, 0.18) 0%, transparent 68%)",
          filter: "blur(40px)",
          pointerEvents: "none",
        }}
      />

      {/* Dynamic Amber Bottom Highlight */}
      <div
        style={{
          position: "absolute",
          left: amberOrbX - 440,
          top: amberOrbY - 440,
          width: 880,
          height: 880,
          borderRadius: "50%",
          background: isLight
            ? "radial-gradient(circle, rgba(217, 119, 6, 0.16) 0%, rgba(245, 158, 11, 0.06) 48%, transparent 72%)"
            : "radial-gradient(circle, rgba(245, 158, 11, 0.15) 0%, transparent 70%)",
          filter: "blur(42px)",
          pointerEvents: "none",
        }}
      />

      {/* Floating Educational Subject Doodles with Official Subject Colors */}
      <FloatingSubjectDoodles isLight={isLight} />

      {/* CTA Center Glow Enhancement */}
      {variant === "cta" && (
        <div
          style={{
            position: "absolute",
            left: 540 - 500,
            top: 240,
            width: 1000,
            height: 1000,
            borderRadius: "50%",
            background:
              "radial-gradient(circle, rgba(158, 27, 50, 0.22) 0%, rgba(0, 168, 150, 0.12) 48%, transparent 70%)",
            filter: "blur(48px)",
            pointerEvents: "none",
          }}
        />
      )}

      {/* Subtle Studio Grain for Tactile Quality */}
      <AbsoluteFill
        style={{
          backgroundImage: `url(${staticFile("brand/grain.png")})`,
          backgroundRepeat: "repeat",
          opacity: isLight ? 0.038 : 0.032,
          mixBlendMode: isLight ? "multiply" : "screen",
          pointerEvents: "none",
        }}
      />
    </AbsoluteFill>
  );
};
