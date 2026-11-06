import React from "react";
import { AbsoluteFill, interpolate, staticFile, useCurrentFrame, useVideoConfig } from "remotion";
import { Headline } from "../components/Headline";
import { PaperBackground } from "../components/PaperBackground";
import { PhoneFrame } from "../components/PhoneFrame";
import { PhoneStage } from "../components/PhoneStage";
import { TapRipple } from "../components/TapRipple";
import { Avatar, Chip, IconButton } from "../components/ui/Atoms";
import { BottomNav } from "../components/ui/BottomNav";
import { Icon } from "../components/ui/Icon";
import { CheckMark, ProgressRing } from "../components/ui/ProgressRing";
import { ScreenShell } from "../components/ui/Screen";
import { DARK, FONTS, LIGHT, SUBJECTS } from "../tokens";
import { clamp, progress, springIn } from "../utils";

const LockScreen: React.FC<{ frame: number; fps: number }> = ({ frame, fps }) => {
  const drop = springIn(frame, fps, 6, { damping: 16, stiffness: 150, mass: 0.9 });
  const nextIn = springIn(frame, fps, 46, { damping: 18, stiffness: 150, mass: 0.8 });
  const ring = progress(frame, 26, 62);
  const tap = frame >= 86;

  return (
    <ScreenShell theme={DARK} time="08:00">
      <div style={{ position: "absolute", top: 54, left: 0, right: 0, textAlign: "center" }}>
        <div style={{ fontSize: 8.5, color: DARK.muted, letterSpacing: "0.08em", textTransform: "uppercase" }}>
          Miércoles · 4 de noviembre
        </div>
        <div
          style={{
            fontFamily: FONTS.mono,
            fontSize: 54,
            fontWeight: 600,
            letterSpacing: "-0.04em",
            color: DARK.ink,
            lineHeight: 1.25,
            fontVariantNumeric: "tabular-nums",
          }}
        >
          08:00
        </div>
      </div>

      <div
        style={{
          position: "absolute",
          left: 14,
          top: 196,
          width: 332,
          height: 124,
          borderRadius: 22,
          backgroundColor: DARK.surface,
          border: `1px solid ${DARK.border}`,
          boxSizing: "border-box",
          padding: 12,
          opacity: drop,
          transform: `translateY(${interpolate(drop, [0, 1], [-26, 0])}px)`,
          boxShadow: tap ? `0 0 0 1px ${DARK.secondary}66` : "none",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 9 }}>
          <ProgressRing size={40} stroke={2.2} progress={ring} color={DARK.secondary} track={DARK.surfaceContainerHigh}>
            <div
              style={{
                width: 28,
                height: 28,
                borderRadius: 9,
                backgroundColor: "#FFFFFF",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                overflow: "hidden",
              }}
            >
              <img src={staticFile("brand/notifast-logo.png")} alt="" style={{ width: 24, height: 24, objectFit: "contain" }} />
            </div>
          </ProgressRing>
          <div style={{ display: "flex", flexDirection: "column", gap: 3, flex: 1 }}>
            <span style={{ fontFamily: FONTS.mono, fontSize: 6.5, color: DARK.faint, letterSpacing: "0.12em" }}>
              NOTIFAST · AHORA
            </span>
            <span style={{ fontSize: 11, fontWeight: 700, color: DARK.ink }}>Clase en curso</span>
            <span style={{ fontSize: 9, color: DARK.muted }}>Cálculo Diferencial · Aula 204</span>
            <span style={{ fontFamily: FONTS.mono, fontSize: 7.5, color: DARK.muted }}>08:00 – 09:30 · Prof. Rivas</span>
          </div>
        </div>
        <div
          style={{
            position: "absolute",
            left: 12,
            right: 12,
            bottom: 9,
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <span style={{ fontSize: 7.5, color: DARK.secondary, fontWeight: 600 }}>Ver detalles</span>
          <span style={{ fontFamily: FONTS.mono, fontSize: 7, color: DARK.faint }}>09:30</span>
        </div>
        <TapRipple x={166} y={62} start={86} color={DARK.secondary} maxSize={300} duration={20} />
      </div>

      <div
        style={{
          position: "absolute",
          left: 24,
          top: 330,
          width: 312,
          height: 50,
          borderRadius: 16,
          backgroundColor: DARK.surfaceContainer,
          border: `1px solid ${DARK.border}`,
          display: "flex",
          alignItems: "center",
          gap: 9,
          padding: "0 12px",
          boxSizing: "border-box",
          opacity: nextIn * 0.92,
          transform: `translateY(${interpolate(nextIn, [0, 1], [16, 0])}px) scale(${0.98 + nextIn * 0.02})`,
        }}
      >
        <Icon name="clock" size={15} color={DARK.secondary} />
        <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <span style={{ fontSize: 7.5, color: DARK.faint }}>Próxima</span>
          <span style={{ fontSize: 9.5, fontWeight: 600, color: DARK.ink }}>Física General · 10:00</span>
        </div>
        <Icon name="chevronRight" size={12} color={DARK.faint} style={{ marginLeft: "auto" }} />
      </div>

      <div
        style={{
          position: "absolute",
          left: 0,
          right: 0,
          bottom: 54,
          display: "flex",
          justifyContent: "center",
          gap: 26,
          opacity: 0.6,
        }}
      >
        <div style={{ width: 38, height: 38, borderRadius: "50%", backgroundColor: DARK.surfaceContainer, display: "flex", alignItems: "center", justifyContent: "center" }}>
          <Icon name="search" size={15} color={DARK.muted} />
        </div>
        <div style={{ width: 38, height: 38, borderRadius: "50%", backgroundColor: DARK.surfaceContainer, display: "flex", alignItems: "center", justifyContent: "center" }}>
          <Icon name="book" size={15} color={DARK.muted} />
        </div>
      </div>
    </ScreenShell>
  );
};

const TodayScreen: React.FC<{ frame: number }> = ({ frame }) => {
  const bar = progress(frame, 116, 146);
  const notesPulse = interpolate(frame, [198, 208, 218], [1, 1.035, 1], clamp);
  const expand = progress(frame, 210, 226);
  const calculo = SUBJECTS.calculo;

  return (
    <ScreenShell theme={LIGHT} time="08:00">
      <div style={{ position: "absolute", top: 36, left: 16, right: 16, display: "flex", alignItems: "center", gap: 9 }}>
        <Avatar theme={LIGHT} />
        <div style={{ display: "flex", flexDirection: "column", gap: 3, flex: 1 }}>
          <span style={{ fontSize: 15, fontWeight: 700 }}>Hola, Valentina</span>
          <span style={{ fontSize: 8.5, color: LIGHT.muted, fontFamily: FONTS.mono }}>Hoy · Miércoles 4</span>
        </div>
        <IconButton theme={LIGHT} name="bell" box={26} size={13} />
      </div>

      <div
        style={{
          position: "absolute",
          left: 16,
          top: 88,
          width: 328,
          height: 212,
          borderRadius: 22,
          backgroundColor: LIGHT.surface,
          border: `1.5px solid ${LIGHT.primary}66`,
          boxSizing: "border-box",
          padding: 14,
        }}
      >
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <Chip theme={LIGHT} label="En curso" tone="primary" size="xs" />
          <span style={{ fontFamily: FONTS.mono, fontSize: 8, color: LIGHT.muted }}>42 min restantes</span>
        </div>
        <div style={{ marginTop: 10, fontSize: 18, fontWeight: 800, letterSpacing: "-0.02em" }}>Cálculo Diferencial</div>
        <div style={{ display: "flex", gap: 8, marginTop: 4, fontFamily: FONTS.mono, fontSize: 8.5, color: LIGHT.muted }}>
          <span>08:00 – 09:30</span>
          <span>·</span>
          <span>Aula 204</span>
        </div>

        <div style={{ marginTop: 12, height: 5, borderRadius: 3, backgroundColor: LIGHT.surfaceContainerHigh, position: "relative" }}>
          <div style={{ width: `${bar * 62}%`, height: 5, borderRadius: 3, backgroundColor: LIGHT.primary }} />
          <span
            style={{
              position: "absolute",
              left: `calc(${bar * 62}% - 3px)`,
              top: -1.5,
              width: 8,
              height: 8,
              borderRadius: "50%",
              backgroundColor: LIGHT.primary,
              border: `2px solid ${LIGHT.surface}`,
            }}
          />
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: 8, marginTop: 13 }}>
          <div
            style={{
              width: 24,
              height: 24,
              borderRadius: "50%",
              backgroundColor: calculo.soft,
              color: calculo.color,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontSize: 8,
              fontWeight: 700,
            }}
          >
            LR
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 1 }}>
            <span style={{ fontSize: 9, fontWeight: 600 }}>Prof. Luis Rivas</span>
            <span style={{ fontSize: 7.5, color: LIGHT.muted }}>Cálculo Diferencial</span>
          </div>
          <div style={{ marginLeft: "auto" }}>
            <IconButton theme={LIGHT} name="phone" box={26} size={12} color={LIGHT.secondary} />
          </div>
        </div>

        <div
          style={{
            position: "absolute",
            left: 14,
            right: 14,
            bottom: 12,
            height: 42,
            borderRadius: 12,
            backgroundColor: LIGHT.primary,
            color: LIGHT.onPrimary,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            gap: 7,
            fontSize: 11,
            fontWeight: 700,
            transform: `scale(${notesPulse})`,
            boxShadow: "0 4px 12px rgba(158,27,50,0.25)",
          }}
        >
          <Icon name="book" size={14} color={LIGHT.onPrimary} strokeWidth={2} />
          Tomar Apunte
        </div>
      </div>

      <div style={{ position: "absolute", top: 318, left: 16, right: 16, fontSize: 8, fontWeight: 700, color: LIGHT.muted, letterSpacing: "0.1em" }}>
        RESTO DEL DÍA
      </div>

      {[
        { time: "10:00", name: "Física General", room: "Lab 3", color: SUBJECTS.fisica.color, soft: SUBJECTS.fisica.soft },
        { time: "14:00", name: "Programación I", room: "Lab 2", color: SUBJECTS.programacion.color, soft: SUBJECTS.programacion.soft },
      ].map((item, index) => (
        <div
          key={item.name}
          style={{
            position: "absolute",
            left: 16,
            top: 336 + index * 64,
            width: 328,
            height: 56,
            borderRadius: 16,
            backgroundColor: LIGHT.surface,
            border: `1px solid ${LIGHT.border}`,
            display: "flex",
            alignItems: "center",
            padding: "0 12px",
            boxSizing: "border-box",
            gap: 10,
          }}
        >
          <span style={{ fontFamily: FONTS.mono, fontSize: 10, fontWeight: 600, color: LIGHT.ink, width: 36 }}>{item.time}</span>
          <span style={{ width: 3, height: 30, borderRadius: 2, backgroundColor: item.color }} />
          <div style={{ display: "flex", flexDirection: "column", gap: 3, flex: 1 }}>
            <span style={{ fontSize: 10.5, fontWeight: 700 }}>{item.name}</span>
            <span style={{ fontSize: 8, color: LIGHT.muted }}>{item.room} · Presencial</span>
          </div>
          <Icon name="chevronRight" size={12} color={LIGHT.faint} />
        </div>
      ))}

      <div
        style={{
          position: "absolute",
          left: 16,
          top: 470,
          width: 328,
          height: 52,
          borderRadius: 14,
          backgroundColor: LIGHT.surface,
          border: `1px dashed ${LIGHT.border}`,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          gap: 7,
          color: LIGHT.muted,
          fontSize: 8.5,
        }}
      >
        <CheckMark size={12} color={LIGHT.tertiary} progress={1} strokeWidth={2.6} />
        Todo tu semestre, en orden
      </div>

      <BottomNav theme={LIGHT} active={1} top={648} />

      {expand > 0 ? (
        <div
          style={{
            position: "absolute",
            left: 180 - 900 * expand,
            top: 267 - 900 * expand,
            width: 1800 * expand,
            height: 1800 * expand,
            borderRadius: "50%",
            backgroundColor: LIGHT.background,
            border: `3px solid ${LIGHT.primary}`,
            boxSizing: "border-box",
            zIndex: 60,
          }}
        />
      ) : null}
    </ScreenShell>
  );
};

export const S04NotificationsScreen: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const lighten = progress(frame, 106, 130);
  const radius = interpolate(frame, [104, 128], [0, 820], clamp);
  const settle = interpolate(frame, [0, 20], [1.045, 1], clamp);
  const ySettle = interpolate(frame, [0, 20], [484, 500], clamp);

  const darkCopyOut = interpolate(frame, [92, 102], [1, 0], clamp);
  const lightCopyIn = interpolate(frame, [110, 120], [0, 1], clamp) * interpolate(frame, [214, 224], [1, 0], clamp);

  return (
    <AbsoluteFill style={{ fontFamily: FONTS.sans }}>
      <PaperBackground mode="light" />
      <AbsoluteFill style={{ opacity: 1 - lighten, pointerEvents: "none" }}>
        <PaperBackground mode="dark" />
      </AbsoluteFill>

      <PhoneStage y={ySettle} scale={settle}>
        <PhoneFrame mode="light">
          <div style={{ position: "absolute", inset: 0 }}>
            <LockScreen frame={frame} fps={fps} />
            <div
              style={{
                position: "absolute",
                inset: 0,
                clipPath: `circle(${radius}px at 180px 265px)`,
              }}
            >
              <TodayScreen frame={frame} />
            </div>
          </div>
        </PhoneFrame>
      </PhoneStage>

      <Headline
        lines={["La clase ya", "empezó."]}
        color="#F3F5F9"
        top={266}
        opacity={darkCopyOut}
        translateY={interpolate(darkCopyOut, [0, 1], [-10, 0])}
      />
      <Headline
        lines={["Tú ya lo sabes."]}
        color={LIGHT.ink}
        top={266}
        opacity={lightCopyIn}
        translateY={interpolate(lightCopyIn, [0, 1], [8, 0])}
      />
    </AbsoluteFill>
  );
};
