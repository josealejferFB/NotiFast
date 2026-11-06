import React from "react";
import { AbsoluteFill, interpolate, staticFile, useCurrentFrame, useVideoConfig } from "remotion";
import { Headline } from "../components/Headline";
import { PaperBackground } from "../components/PaperBackground";
import { PhoneFrame } from "../components/PhoneFrame";
import { PhoneStage } from "../components/PhoneStage";
import { Chip, IconButton } from "../components/ui/Atoms";
import { CheckMark } from "../components/ui/ProgressRing";
import { Icon } from "../components/ui/Icon";
import { ScreenShell } from "../components/ui/Screen";
import { FONTS, LIGHT } from "../tokens";
import { clamp, progress, springIn } from "../utils";

const MiniCover: React.FC<{
  label: string;
  color: string;
  active?: boolean;
  style?: React.CSSProperties;
}> = ({ label, color, active, style }) => (
  <div
    style={{
      width: 94,
      height: 122,
      borderRadius: 10,
      backgroundColor: "#FFFFFF",
      border: `1.4px solid ${active ? color : LIGHT.border}`,
      padding: 8,
      boxSizing: "border-box",
      position: "absolute",
      boxShadow: "0 8px 14px rgba(24,26,31,0.06)",
      ...style,
    }}
  >
    <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
      <img src={staticFile("brand/unerg-ais.png")} alt="" style={{ width: 13, height: 13, objectFit: "contain" }} />
      <span style={{ fontFamily: FONTS.mono, fontSize: 4.5, fontWeight: 700, letterSpacing: "0.1em", color: LIGHT.ink }}>UNERG</span>
    </div>
    <div style={{ height: 1, backgroundColor: color, marginTop: 5, opacity: 0.5 }} />
    <div style={{ marginTop: 9, height: 5, width: "78%", borderRadius: 2, backgroundColor: LIGHT.surfaceContainerHigh }} />
    <div style={{ marginTop: 3, height: 5, width: "60%", borderRadius: 2, backgroundColor: LIGHT.surfaceContainerHigh }} />
    <div style={{ marginTop: 12, height: 3, width: "85%", borderRadius: 2, backgroundColor: LIGHT.border }} />
    <div style={{ marginTop: 3, height: 3, width: "70%", borderRadius: 2, backgroundColor: LIGHT.border }} />
    <div style={{ marginTop: 3, height: 3, width: "76%", borderRadius: 2, backgroundColor: LIGHT.border }} />
    <div
      style={{
        position: "absolute",
        left: 8,
        right: 8,
        bottom: 8,
        height: 14,
        borderRadius: 4,
        backgroundColor: active ? color : LIGHT.surfaceContainer,
        color: active ? "#FFFFFF" : LIGHT.muted,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontFamily: FONTS.mono,
        fontSize: 5,
        fontWeight: 700,
        letterSpacing: "0.1em",
      }}
    >
      {label}
    </div>
  </div>
);

const CoverPreview: React.FC<{ frame: number; fps: number; exit: number }> = ({ frame, fps, exit }) => {
  const appear = springIn(frame, fps, 56, { damping: 18, stiffness: 130, mass: 0.9 });
  const line1 = progress(frame, 84, 100);
  const line2 = progress(frame, 92, 108);
  const line3 = progress(frame, 100, 116);
  const stamp = progress(frame, 150, 166);
  const ready = progress(frame, 172, 188);
  const stampScale = interpolate(stamp, [0, 1], [1.9, 1], clamp);

  return (
    <div
      style={{
        position: "absolute",
        left: 42,
        top: 244,
        width: 276,
        height: 390,
        borderRadius: 6,
        backgroundColor: "#FFFEFB",
        border: `1px solid ${LIGHT.border}`,
        boxShadow: "0 10px 18px rgba(24,26,31,0.07)",
        opacity: appear * (1 - exit),
        transform: `translateY(${interpolate(appear, [0, 1], [34, 0]) + exit * 54}px) scale(${interpolate(appear, [0, 1], [0.72, 1])})`,
        transformOrigin: "center top",
        padding: 18,
        boxSizing: "border-box",
        overflow: "hidden",
      }}
    >
      <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
        <img src={staticFile("brand/unerg-ais.png")} alt="" style={{ width: 28, height: 28, objectFit: "contain" }} />
        <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <span style={{ fontFamily: FONTS.mono, fontSize: 7.5, fontWeight: 700, letterSpacing: "0.12em" }}>UNERG</span>
          <span style={{ fontSize: 4.5, color: LIGHT.muted }}>Universidad Nacional Experimental</span>
          <span style={{ fontSize: 4.5, color: LIGHT.muted }}>de los Llanos Occidentales</span>
        </div>
        <span style={{ marginLeft: "auto", fontFamily: FONTS.mono, fontSize: 5, color: LIGHT.faint }}>2026</span>
      </div>

      <div style={{ height: 1, backgroundColor: LIGHT.border, marginTop: 12 }} />

      <div style={{ marginTop: 14, textAlign: "center", fontFamily: FONTS.mono, fontSize: 4.5, letterSpacing: "0.22em", color: LIGHT.muted }}>
        TRABAJO DE GRADO
      </div>
      <div style={{ marginTop: 6, textAlign: "center", fontSize: 14, fontWeight: 800, letterSpacing: "-0.02em", color: LIGHT.ink }}>
        Trabajo de Grado
      </div>
      <div
        style={{
          margin: "7px auto 0",
          height: 2,
          width: 110,
          backgroundColor: LIGHT.primary,
          transform: `scaleX(${line1})`,
          transformOrigin: "left center",
        }}
      />
      <div style={{ marginTop: 7, textAlign: "center", fontSize: 6, color: LIGHT.muted }}>
        Ingeniería Informática
      </div>

      <div style={{ position: "absolute", left: 18, right: 18, top: 168, display: "flex", flexDirection: "column", gap: 12 }}>
        {[
          { label: "AUTORA", value: "María F. Pérez", progress: line1 },
          { label: "TUTOR", value: "Prof. L. Rivas", progress: line2 },
        ].map((field) => (
          <div key={field.label} style={{ display: "flex", flexDirection: "column", gap: 3 }}>
            <span style={{ fontFamily: FONTS.mono, fontSize: 4.5, letterSpacing: "0.16em", color: LIGHT.faint }}>{field.label}</span>
            <span style={{ fontSize: 9, fontWeight: 600, color: LIGHT.ink, opacity: field.progress }}>{field.value}</span>
            <div style={{ height: 1, backgroundColor: LIGHT.border, transform: `scaleX(${field.progress})`, transformOrigin: "left center" }} />
          </div>
        ))}
      </div>

      <div style={{ position: "absolute", left: 0, right: 0, bottom: 28, textAlign: "center", opacity: line3 }}>
        <span style={{ fontFamily: FONTS.mono, fontSize: 7, color: LIGHT.muted, letterSpacing: "0.08em" }}>Noviembre 2026</span>
      </div>

      <div
        style={{
          position: "absolute",
          right: 10,
          top: 60,
          width: 58,
          height: 58,
          borderRadius: "50%",
          border: `1.5px solid ${LIGHT.primary}AA`,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          gap: 1,
          color: LIGHT.primary,
          opacity: stamp,
          transform: `rotate(-11deg) scale(${stampScale})`,
          backgroundColor: "rgba(255,255,255,0.72)",
        }}
      >
        <span style={{ fontFamily: FONTS.mono, fontSize: 7, fontWeight: 700, letterSpacing: "0.1em" }}>UNERG</span>
        <span style={{ fontFamily: FONTS.mono, fontSize: 3.5, letterSpacing: "0.06em" }}>2026-02</span>
      </div>

      <div
        style={{
          position: "absolute",
          left: 18,
          right: 18,
          bottom: 10,
          height: 1,
          backgroundColor: LIGHT.border,
        }}
      />
      <div
        style={{
          position: "absolute",
          left: 18,
          right: 18,
          bottom: 3,
          display: "flex",
          justifyContent: "space-between",
          fontFamily: FONTS.mono,
          fontSize: 4,
          color: LIGHT.faint,
        }}
      >
        <span>UNERG · DACE</span>
        <span>1 / 1</span>
      </div>
      <div style={{ position: "absolute", left: 18, top: 246, width: 190, height: 8, borderRadius: 2, backgroundColor: LIGHT.primaryContainer, opacity: ready * 0.5 }} />
    </div>
  );
};

export const S07CoversScreen: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const fan = springIn(frame, fps, 4, { damping: 18, stiffness: 140, mass: 0.9 });
  const unergOut = interpolate(frame, [54, 68], [1, 0], clamp);
  const othersOut = interpolate(frame, [54, 74], [0.9, 0.45], clamp);
  const exit = progress(frame, 202, 222);
  const docx = springIn(frame, fps, 170, { damping: 18, stiffness: 150, mass: 0.8 });

  return (
    <AbsoluteFill style={{ fontFamily: FONTS.sans }}>
      <PaperBackground mode="light" />
      <PhoneStage scale={1 + frame * 0.00003} y={500 + frame * 0.015}>
        <PhoneFrame mode="light">
          <ScreenShell theme={LIGHT} time="9:36">
            <div style={{ position: "absolute", top: 36, left: 16, right: 16, height: 36, display: "flex", alignItems: "center", gap: 9 }}>
              <Icon name="back" size={18} color={LIGHT.ink} strokeWidth={2} />
              <div style={{ display: "flex", flexDirection: "column", gap: 2, flex: 1 }}>
                <span style={{ fontSize: 14, fontWeight: 700 }}>Estudio & Portadas</span>
                <span style={{ fontSize: 7.5, color: LIGHT.muted, fontFamily: FONTS.mono }}>Micro-app · documentos</span>
              </div>
              <IconButton theme={LIGHT} name="search" box={27} size={13} />
            </div>

            <div
              style={{
                position: "absolute",
                top: 74,
                left: 16,
                width: 328,
                height: 28,
                borderRadius: 9,
                backgroundColor: LIGHT.surfaceContainer,
                display: "flex",
                padding: 3,
                boxSizing: "border-box",
              }}
            >
              <div style={{ flex: 1, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 7.5, color: LIGHT.muted, gap: 4 }}>
                <Icon name="document" size={10} color={LIGHT.muted} />
                Mis documentos
              </div>
              <div
                style={{
                  flex: 1,
                  borderRadius: 7,
                  backgroundColor: LIGHT.surface,
                  border: `1px solid ${LIGHT.border}`,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: 7.5,
                  fontWeight: 700,
                  color: LIGHT.primary,
                  gap: 4,
                }}
              >
                <Icon name="layers" size={10} color={LIGHT.primary} />
                Plantillas de portada
              </div>
            </div>

            <div style={{ position: "absolute", left: 0, right: 0, top: 114, height: 132, opacity: othersOut }}>
              <MiniCover
                label="APA 7"
                color="#4338CA"
                style={{
                  left: 28,
                  top: 12,
                  transform: `rotate(-12deg) translateY(${interpolate(fan, [0, 1], [22, 0])}px)`,
                }}
              />
              <MiniCover
                label="IEEE"
                color="#2A7B7B"
                style={{
                  right: 26,
                  top: 12,
                  transform: `rotate(12deg) translateY(${interpolate(fan, [0, 1], [22, 0])}px)`,
                }}
              />
            </div>

            <div style={{ position: "absolute", left: 133, top: 116, opacity: unergOut, transform: `rotate(${(1 - fan) * -8}deg) translateY(${interpolate(fan, [0, 1], [24, 0])}px)` }}>
              <MiniCover label="UNERG" color={LIGHT.primary} active />
            </div>

            <CoverPreview frame={frame} fps={fps} exit={exit} />

            <div
              style={{
                position: "absolute",
                left: 16,
                right: 16,
                top: 652,
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                opacity: docx,
                transform: `translateY(${interpolate(docx, [0, 1], [10, 0])}px)`,
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: 7 }}>
                <div style={{ width: 30, height: 30, borderRadius: 9, backgroundColor: LIGHT.primaryContainer, display: "flex", alignItems: "center", justifyContent: "center" }}>
                  <Icon name="document" size={15} color={LIGHT.primary} />
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
                  <span style={{ fontSize: 9, fontWeight: 700 }}>Portada_UNERG.docx</span>
                  <span style={{ fontFamily: FONTS.mono, fontSize: 6.5, color: LIGHT.muted }}>APA 7 · Times New Roman 12</span>
                </div>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: 4, fontSize: 8, fontWeight: 700, color: LIGHT.tertiary }}>
                <CheckMark size={12} color={LIGHT.tertiary} progress={progress(frame, 174, 186)} strokeWidth={2.6} />
                Listo
              </div>
            </div>

            <Chip theme={LIGHT} label="Listo para entregar" tone="tertiary" size="xs" style={{ position: "absolute", left: 16, top: 690, opacity: progress(frame, 180, 194) }} />
          </ScreenShell>
        </PhoneFrame>
      </PhoneStage>

      <Headline
        lines={["Portadas listas", "para entregar."]}
        color={LIGHT.ink}
        top={266}
        opacity={interpolate(frame, [196, 208], [1, 0], clamp)}
      />
    </AbsoluteFill>
  );
};
