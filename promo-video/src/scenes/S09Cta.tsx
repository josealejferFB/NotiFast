import React from "react";
import { AbsoluteFill, interpolate, staticFile, useCurrentFrame, useVideoConfig } from "remotion";
import { PaperBackground } from "../components/PaperBackground";
import { PhoneFrame } from "../components/PhoneFrame";
import { PhoneStage } from "../components/PhoneStage";
import { Chip } from "../components/ui/Atoms";
import { Icon } from "../components/ui/Icon";
import { ScreenShell } from "../components/ui/Screen";
import { FONTS, LIGHT, SUBJECTS } from "../tokens";
import { clamp, progress, springIn } from "../utils";

const HomeMini: React.FC = () => (
  <ScreenShell theme={LIGHT} time="9:41" showHomeIndicator={false}>
    <div style={{ position: "absolute", top: 36, left: 16, right: 16, display: "flex", alignItems: "center", gap: 9 }}>
      <div style={{ width: 34, height: 34, borderRadius: "50%", backgroundColor: LIGHT.primaryContainer, color: LIGHT.primary, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 14, fontWeight: 700 }}>
        V
      </div>
      <div style={{ display: "flex", flexDirection: "column", gap: 3, flex: 1 }}>
        <span style={{ fontSize: 15, fontWeight: 700 }}>Hola, Valentina</span>
        <span style={{ fontSize: 8.5, color: LIGHT.muted, fontFamily: FONTS.mono }}>Mi agenda · Miércoles</span>
      </div>
      <div style={{ width: 26, height: 26, borderRadius: "50%", backgroundColor: LIGHT.surfaceContainer, display: "flex", alignItems: "center", justifyContent: "center" }}>
        <Icon name="bell" size={12} color={LIGHT.muted} />
      </div>
    </div>

    <div style={{ position: "absolute", top: 88, left: 16, width: 328, height: 200, borderRadius: 22, backgroundColor: LIGHT.surface, border: `1.5px solid ${LIGHT.primary}66`, padding: 14, boxSizing: "border-box" }}>
      <Chip theme={LIGHT} label="En curso" tone="primary" size="xs" />
      <div style={{ marginTop: 10, fontSize: 18, fontWeight: 800 }}>Cálculo Diferencial</div>
      <div style={{ fontFamily: FONTS.mono, fontSize: 8.5, color: LIGHT.muted, marginTop: 4 }}>08:00 – 09:30 · Aula 204</div>
      <div style={{ marginTop: 14, height: 5, borderRadius: 3, backgroundColor: LIGHT.surfaceContainerHigh }}>
        <div style={{ width: "62%", height: 5, borderRadius: 3, backgroundColor: LIGHT.primary }} />
      </div>
      <div style={{ marginTop: 16, height: 42, borderRadius: 12, backgroundColor: LIGHT.primary, color: LIGHT.onPrimary, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 11, fontWeight: 700 }}>
        Tomar Apunte
      </div>
    </div>

    <div style={{ position: "absolute", top: 304, left: 16, width: 328, height: 60, borderRadius: 16, backgroundColor: LIGHT.surface, border: `1px solid ${LIGHT.border}`, display: "flex", alignItems: "center", gap: 10, padding: "0 12px", boxSizing: "border-box" }}>
      <span style={{ fontFamily: FONTS.mono, fontSize: 10, fontWeight: 600, color: LIGHT.ink }}>10:00</span>
      <span style={{ width: 3, height: 34, borderRadius: 2, backgroundColor: SUBJECTS.fisica.color }} />
      <div style={{ display: "flex", flexDirection: "column", gap: 3 }}>
        <span style={{ fontSize: 10.5, fontWeight: 700 }}>Física General</span>
        <span style={{ fontSize: 8, color: LIGHT.muted }}>Lab 3</span>
      </div>
    </div>
  </ScreenShell>
);

const Pill: React.FC<{
  label: string;
  icon?: "check" | "offline" | "phone";
  tone?: "primary" | "secondary" | "neutral";
  scale: number;
  opacity: number;
}> = ({ label, icon, tone = "neutral", scale, opacity }) => {
  const palette = {
    primary: { bg: LIGHT.primaryContainer, fg: LIGHT.primary, border: `${LIGHT.primary}44` },
    secondary: { bg: LIGHT.secondaryContainer, fg: LIGHT.secondary, border: `${LIGHT.secondary}44` },
    neutral: { bg: LIGHT.surface, fg: LIGHT.ink, border: LIGHT.border },
  }[tone];
  return (
    <div
      style={{
        height: 44,
        padding: "0 22px",
        borderRadius: 22,
        backgroundColor: palette.bg,
        border: `1px solid ${palette.border}`,
        color: palette.fg,
        display: "flex",
        alignItems: "center",
        gap: 9,
        fontSize: 20,
        fontWeight: 600,
        opacity,
        transform: `scale(${scale})`,
      }}
    >
      {icon ? <Icon name={icon} size={18} color={palette.fg} strokeWidth={2} /> : null}
      {label}
    </div>
  );
};

export const S09Cta: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const logo = springIn(frame, fps, 0, { damping: 16, stiffness: 120, mass: 0.9 });
  const ring = progress(frame, 0, 24);
  const brand = progress(frame, 12, 28);
  const tagline = progress(frame, 22, 38);
  const pill1 = springIn(frame, fps, 32, { damping: 18, stiffness: 160, mass: 0.7 });
  const pill2 = springIn(frame, fps, 37, { damping: 18, stiffness: 160, mass: 0.7 });
  const pill3 = springIn(frame, fps, 42, { damping: 18, stiffness: 160, mass: 0.7 });
  const button = springIn(frame, fps, 50, { damping: 18, stiffness: 150, mass: 0.8 });
  const url = progress(frame, 62, 78);
  const phone = progress(frame, 38, 82);
  const pulse = interpolate(frame, [92, 100, 108], [1, 1.03, 1], clamp);
  const glow = 0.5 + 0.5 * ring;

  return (
    <AbsoluteFill style={{ fontFamily: FONTS.sans }}>
      <PaperBackground mode="light" variant="cta" />

      <div
        style={{
          position: "absolute",
          left: 540 - 310,
          top: 182,
          width: 620,
          height: 620,
          borderRadius: "50%",
          background: `radial-gradient(circle, rgba(158,27,50,${0.13 * glow}) 0%, rgba(158,27,50,0.035) 48%, rgba(158,27,50,0) 70%)`,
        }}
      />
      <div
        style={{
          position: "absolute",
          left: 540 - 180,
          top: 292,
          width: 360,
          height: 360,
          borderRadius: "50%",
          border: `2px solid ${LIGHT.primary}`,
          opacity: 0.18 * ring,
          transform: `scale(${0.92 + ring * 0.08})`,
        }}
      />
      <div
        style={{
          position: "absolute",
          left: 540 - 150,
          top: 322,
          width: 300,
          height: 300,
          opacity: logo,
          transform: `scale(${interpolate(logo, [0, 1], [0.82, 1])})`,
        }}
      >
        <img
          src={staticFile("brand/notifast-logo.png")}
          alt="NotiFast"
          style={{ width: 300, height: 300, objectFit: "contain", display: "block" }}
        />
      </div>

      <div
        style={{
          position: "absolute",
          top: 632,
          left: 0,
          right: 0,
          textAlign: "center",
          fontSize: 96,
          fontWeight: 800,
          letterSpacing: "-0.04em",
          color: LIGHT.ink,
          opacity: brand,
          transform: `translateY(${interpolate(brand, [0, 1], [14, 0])}px)`,
        }}
      >
        NotiFast<span style={{ color: LIGHT.primary }}>.</span>
      </div>

      <div
        style={{
          position: "absolute",
          top: 756,
          left: 0,
          right: 0,
          textAlign: "center",
          fontSize: 42,
          fontWeight: 500,
          letterSpacing: "-0.02em",
          color: LIGHT.muted,
          opacity: tagline,
          transform: `translateY(${interpolate(tagline, [0, 1], [12, 0])}px)`,
        }}
      >
        Todo tu semestre, en orden.
      </div>

      <div style={{ position: "absolute", top: 842, left: 0, right: 0, display: "flex", justifyContent: "center", gap: 12 }}>
        <Pill label="Gratis" icon="check" tone="primary" scale={interpolate(pill1, [0, 1], [0.85, 1])} opacity={pill1} />
        <Pill label="Android" icon="phone" scale={interpolate(pill2, [0, 1], [0.85, 1])} opacity={pill2} />
        <Pill label="100 % offline" icon="offline" tone="secondary" scale={interpolate(pill3, [0, 1], [0.85, 1])} opacity={pill3} />
      </div>

      <div
        style={{
          position: "absolute",
          left: 280,
          top: 930,
          width: 520,
          height: 92,
          borderRadius: 24,
          backgroundColor: LIGHT.primary,
          color: LIGHT.onPrimary,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          gap: 13,
          fontSize: 30,
          fontWeight: 700,
          letterSpacing: "-0.01em",
          opacity: button,
          transform: `scale(${interpolate(button, [0, 1], [0.9, 1]) * pulse})`,
          boxShadow: "0 10px 22px rgba(158,27,50,0.18)",
        }}
      >
        <Icon name="download" size={26} color={LIGHT.onPrimary} strokeWidth={2} />
        Descargar gratis
      </div>

      <div
        style={{
          position: "absolute",
          top: 1046,
          left: 0,
          right: 0,
          textAlign: "center",
          fontFamily: FONTS.mono,
          fontSize: 24,
          color: LIGHT.muted,
          letterSpacing: "0.02em",
          opacity: url,
        }}
      >
        notifast.pages.dev
      </div>
      <div
        style={{
          position: "absolute",
          top: 1086,
          left: 0,
          right: 0,
          textAlign: "center",
          fontFamily: FONTS.mono,
          fontSize: 19,
          color: LIGHT.faint,
          letterSpacing: "0.06em",
          opacity: url * 0.9,
        }}
      >
        Android · 100 % offline
      </div>

      <PhoneStage x={160} y={1500 + (1 - phone) * 230} scale={0.5} rotate={-7} opacity={phone}>
        <PhoneFrame mode="light">
          <HomeMini />
        </PhoneFrame>
      </PhoneStage>
    </AbsoluteFill>
  );
};
