import React from "react";
import { AbsoluteFill, interpolate, useCurrentFrame, useVideoConfig } from "remotion";
import { Headline } from "../components/Headline";
import { PaperBackground } from "../components/PaperBackground";
import { PhoneFrame } from "../components/PhoneFrame";
import { PhoneStage } from "../components/PhoneStage";
import { Avatar, IconButton } from "../components/ui/Atoms";
import { BottomNav } from "../components/ui/BottomNav";
import { CheckMark } from "../components/ui/ProgressRing";
import { Icon } from "../components/ui/Icon";
import { ScreenShell } from "../components/ui/Screen";
import { FONTS, LIGHT, SUBJECTS } from "../tokens";
import { clamp, progress, springIn } from "../utils";

const SUBJECT_ITEMS = [
  { name: "Cálculo Diferencial", evals: "4 evaluaciones", score: 17.5, color: SUBJECTS.calculo.color, soft: SUBJECTS.calculo.soft, icon: "book" as const },
  { name: "Estadística", evals: "3 evaluaciones", score: 16.8, color: "#C2410C", soft: "#FFEDD5", icon: "book" as const },
  { name: "Física General", evals: "3 evaluaciones", score: 16.0, color: SUBJECTS.fisica.color, soft: SUBJECTS.fisica.soft, icon: "book" as const },
  { name: "Programación I", evals: "4 evaluaciones", score: 18.5, color: SUBJECTS.programacion.color, soft: SUBJECTS.programacion.soft, icon: "code" as const },
];

export const S08GradesScreen: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const ring = progress(frame, 12, 54);
  const score = 17.5 * ring;
  const bar = progress(frame, 20, 64);
  const aura = progress(frame, 158, 180);

  // Fade out headline cleanly before S09 starts at frame 180
  const headlineOpacity = interpolate(frame, [166, 178], [1, 0], clamp);

  return (
    <AbsoluteFill style={{ fontFamily: FONTS.sans }}>
      <PaperBackground mode="light" />
      <PhoneStage scale={1 + frame * 0.00005}>
        <PhoneFrame mode="light">
          <ScreenShell theme={LIGHT} time="9:07">
            {/* Header matching 11-grades-light.png */}
            <div style={{ position: "absolute", top: 36, left: 16, right: 16, height: 36, display: "flex", alignItems: "center", gap: 9 }}>
              <Avatar theme={LIGHT} />
              <div style={{ display: "flex", flexDirection: "column", gap: 2, flex: 1 }}>
                <span style={{ fontSize: 14, fontWeight: 700 }}>Hola, Valentina</span>
                <div
                  style={{
                    display: "inline-flex",
                    alignItems: "center",
                    gap: 4,
                    backgroundColor: LIGHT.surfaceContainer,
                    borderRadius: 10,
                    padding: "2px 7px",
                    width: "fit-content",
                  }}
                >
                  <Icon name="star" size={10} color={LIGHT.primary} filled />
                  <span style={{ fontSize: 7.5, color: LIGHT.muted, fontFamily: FONTS.mono, fontWeight: 600 }}>Mis Calificaciones</span>
                </div>
              </div>
              <IconButton theme={LIGHT} name="search" box={26} size={13} />
              <IconButton theme={LIGHT} name="phone" box={26} size={13} />
              <IconButton theme={LIGHT} name="bell" box={26} size={13} />
            </div>

            {/* Segmented view switcher matching 11-grades-light.png */}
            <div
              style={{
                position: "absolute",
                top: 86,
                left: 16,
                right: 16,
                height: 32,
                borderRadius: 16,
                backgroundColor: LIGHT.surfaceContainer,
                display: "flex",
                padding: 2,
                boxSizing: "border-box",
                zIndex: 5,
              }}
            >
              {[
                { id: "materias", label: "Materias", active: true },
                { id: "analiticas", label: "Analíticas & Proyecciones", active: false },
              ].map((tab) => (
                <div
                  key={tab.id}
                  style={{
                    flex: 1,
                    borderRadius: 14,
                    backgroundColor: tab.active ? LIGHT.surface : "transparent",
                    boxShadow: tab.active ? "0 2px 6px rgba(0,0,0,0.06)" : "none",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontSize: 8.5,
                    fontWeight: tab.active ? 700 : 500,
                    color: tab.active ? LIGHT.primary : LIGHT.muted,
                  }}
                >
                  {tab.label}
                </div>
              ))}
            </div>

            {/* Promedio Global Hero Card matching 11-grades-light.png */}
            <div
              style={{
                position: "absolute",
                left: 16,
                top: 130,
                width: 328,
                height: 118,
                borderRadius: 20,
                backgroundColor: LIGHT.surface,
                border: `1px solid ${LIGHT.border}`,
                boxSizing: "border-box",
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                boxShadow: "0 4px 14px rgba(24,26,31,0.04)",
                padding: "10px 18px",
              }}
            >
              <span
                style={{
                  fontFamily: FONTS.mono,
                  fontSize: 7.5,
                  letterSpacing: "0.14em",
                  color: LIGHT.primary,
                  fontWeight: 700,
                  textTransform: "uppercase",
                }}
              >
                Promedio Global
              </span>

              <div
                style={{
                  fontFamily: FONTS.mono,
                  fontSize: 44,
                  fontWeight: 800,
                  letterSpacing: "-0.04em",
                  color: LIGHT.ink,
                  lineHeight: 1.15,
                  margin: "2px 0",
                  fontVariantNumeric: "tabular-nums",
                }}
              >
                {score.toFixed(1)}
              </div>

              {/* Progress bar and dot */}
              <div style={{ width: "100%", height: 6, borderRadius: 3, backgroundColor: LIGHT.surfaceContainerHigh, position: "relative", marginTop: 4 }}>
                <div style={{ width: `${bar * 87.5}%`, height: 6, borderRadius: 3, backgroundColor: LIGHT.tertiary }} />
                <span
                  style={{
                    position: "absolute",
                    left: `calc(${bar * 87.5}% - 4px)`,
                    top: -2,
                    width: 10,
                    height: 10,
                    borderRadius: "50%",
                    backgroundColor: LIGHT.tertiary,
                    border: `2px solid ${LIGHT.surface}`,
                  }}
                />
              </div>

              <span
                style={{
                  fontSize: 8.5,
                  fontWeight: 600,
                  color: LIGHT.tertiary,
                  display: "flex",
                  alignItems: "center",
                  gap: 4,
                  marginTop: 6,
                }}
              >
                ¡Excelente rendimiento académico!
              </span>
            </div>

            {/* Subject List Cards matching 11-grades-light.png */}
            <div style={{ position: "absolute", left: 16, top: 260, right: 16, display: "flex", flexDirection: "column", gap: 8 }}>
              {SUBJECT_ITEMS.map((item, index) => {
                const appear = springIn(frame, fps, 30 + index * 8, { damping: 18, stiffness: 150, mass: 0.8 });
                const fill = progress(frame, 40 + index * 8, 70 + index * 8);

                return (
                  <div
                    key={item.name}
                    style={{
                      height: 52,
                      borderRadius: 16,
                      backgroundColor: LIGHT.surface,
                      border: `1px solid ${index === 0 ? LIGHT.primary + "44" : LIGHT.border}`,
                      display: "flex",
                      alignItems: "center",
                      padding: "0 12px",
                      gap: 10,
                      opacity: appear,
                      transform: `translateY(${interpolate(appear, [0, 1], [14, 0])}px)`,
                      boxShadow: "0 2px 8px rgba(24,26,31,0.03)",
                    }}
                  >
                    {/* Birrete / Subject Icon in Soft Circle */}
                    <div
                      style={{
                        width: 32,
                        height: 32,
                        borderRadius: 10,
                        backgroundColor: item.soft,
                        color: item.color,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                    >
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke={item.color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M22 10v6M2 10l10-5 10 5-10 5z" />
                        <path d="M6 12v5c3 3 9 3 12 0v-5" />
                      </svg>
                    </div>

                    <div style={{ display: "flex", flexDirection: "column", gap: 1, flex: 1 }}>
                      <span style={{ fontSize: 10.5, fontWeight: 700, color: LIGHT.ink }}>{item.name}</span>
                      <span style={{ fontSize: 7.5, color: LIGHT.muted }}>{item.evals}</span>
                    </div>

                    <span
                      style={{
                        fontFamily: FONTS.mono,
                        fontSize: 16,
                        fontWeight: 700,
                        color: LIGHT.tertiary,
                        fontVariantNumeric: "tabular-nums",
                      }}
                    >
                      {(item.score * fill).toFixed(1)}
                    </span>
                  </div>
                );
              })}
            </div>

            {/* Bottom Navigation Dock with Notas active */}
            <BottomNav theme={LIGHT} active={3} top={648} />

            {/* Transition Aura */}
            {aura > 0 ? (
              <div
                style={{
                  position: "absolute",
                  left: 180 - 160 * aura,
                  top: 180 - 160 * aura,
                  width: 320 * aura,
                  height: 320 * aura,
                  borderRadius: "50%",
                  border: `2px solid ${LIGHT.primary}`,
                  opacity: 0.6 * (1 - aura),
                  zIndex: 20,
                }}
              />
            ) : null}
          </ScreenShell>
        </PhoneFrame>
      </PhoneStage>

      <Headline
        lines={["Tu nota,", "clara."]}
        color={LIGHT.ink}
        top={266}
        opacity={headlineOpacity}
      />
    </AbsoluteFill>
  );
};
