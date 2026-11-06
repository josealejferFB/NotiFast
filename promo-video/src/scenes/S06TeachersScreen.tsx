import React from "react";
import { AbsoluteFill, interpolate, useCurrentFrame, useVideoConfig } from "remotion";
import { Headline } from "../components/Headline";
import { PaperBackground } from "../components/PaperBackground";
import { PhoneFrame } from "../components/PhoneFrame";
import { PhoneStage } from "../components/PhoneStage";
import { TapRipple } from "../components/TapRipple";
import { Chip, IconButton } from "../components/ui/Atoms";
import { CheckMark } from "../components/ui/ProgressRing";
import { Icon } from "../components/ui/Icon";
import { ScreenShell, StatusBar } from "../components/ui/Screen";
import { FONTS, LIGHT, SUBJECTS } from "../tokens";
import { clamp, progress, springIn } from "../utils";

const MESSAGE = "Buenas tardes, profe. ¿Revisa mi avance?";

const TeacherScreen: React.FC<{ frame: number }> = ({ frame }) => {
  const calculo = SUBJECTS.calculo;
  return (
    <ScreenShell theme={LIGHT} time="9:06">
      <div style={{ position: "absolute", top: 36, left: 16, right: 16, height: 36, display: "flex", alignItems: "center", gap: 10 }}>
        <Icon name="back" size={18} color={LIGHT.ink} strokeWidth={2} />
        <span style={{ fontSize: 14, fontWeight: 700, flex: 1 }}>Profesor</span>
        <IconButton theme={LIGHT} name="search" box={28} size={13} />
        <IconButton theme={LIGHT} name="bell" box={28} size={13} />
      </div>

      <div
        style={{
          position: "absolute",
          top: 88,
          left: 16,
          width: 328,
          height: 182,
          borderRadius: 22,
          backgroundColor: LIGHT.surface,
          border: `1px solid ${LIGHT.border}`,
          padding: 14,
          boxSizing: "border-box",
        }}
      >
        <div style={{ display: "flex", gap: 11 }}>
          <div
            style={{
              width: 54,
              height: 54,
              borderRadius: "50%",
              backgroundColor: LIGHT.primaryContainer,
              border: `1px solid ${LIGHT.primary}33`,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontFamily: FONTS.mono,
              fontSize: 16,
              fontWeight: 700,
              color: LIGHT.primary,
              position: "relative",
              flexShrink: 0,
            }}
          >
            LR
            <span
              style={{
                position: "absolute",
                right: 0,
                bottom: 1,
                width: 11,
                height: 11,
                borderRadius: "50%",
                backgroundColor: LIGHT.tertiary,
                border: `2px solid ${LIGHT.surface}`,
              }}
            />
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 4, flex: 1 }}>
            <span style={{ fontSize: 15, fontWeight: 800, letterSpacing: "-0.01em" }}>Prof. Luis Rivas</span>
            <span style={{ fontSize: 9, color: LIGHT.muted }}>Cálculo Diferencial · Sec. 01</span>
            <div style={{ display: "flex", gap: 5, marginTop: 2 }}>
              <Chip theme={LIGHT} label="Docente" size="xs" tone="neutral" />
              <Chip theme={LIGHT} label="Aula 204" size="xs" tone="secondary" icon="pin" />
            </div>
          </div>
        </div>

        <div
          style={{
            marginTop: 14,
            height: 34,
            borderRadius: 10,
            backgroundColor: LIGHT.surfaceContainer,
            display: "flex",
            alignItems: "center",
            gap: 7,
            padding: "0 10px",
            fontFamily: FONTS.mono,
            fontSize: 10,
            color: LIGHT.ink,
            letterSpacing: "0.01em",
          }}
        >
          <Icon name="phone" size={12} color={LIGHT.secondary} />
          +58 412 123 4567
          <span style={{ marginLeft: "auto", fontSize: 7, color: LIGHT.muted, fontFamily: FONTS.sans }}>Disponible</span>
        </div>

        <div style={{ display: "flex", gap: 7, marginTop: 10 }}>
          <div
            style={{
              flex: 1,
              height: 38,
              borderRadius: 11,
              backgroundColor: LIGHT.secondary,
              color: "#FFFFFF",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: 6,
              fontSize: 10,
              fontWeight: 700,
              position: "relative",
            }}
          >
            <Icon name="whatsapp" size={14} color="#FFFFFF" />
            WhatsApp
            <TapRipple x={100} y={19} start={60} color="#FFFFFF" maxSize={160} duration={16} />
          </div>
          <div
            style={{
              flex: 1,
              height: 38,
              borderRadius: 11,
              backgroundColor: LIGHT.surfaceContainer,
              border: `1px solid ${LIGHT.border}`,
              color: LIGHT.ink,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: 6,
              fontSize: 10,
              fontWeight: 600,
            }}
          >
            <Icon name="phone" size={13} color={calculo.color} />
            Llamar
          </div>
        </div>
      </div>

      <div
        style={{
          position: "absolute",
          left: 16,
          top: 288,
          width: 328,
          height: 74,
          borderRadius: 16,
          backgroundColor: LIGHT.surface,
          border: `1px solid ${LIGHT.border}`,
          display: "flex",
          alignItems: "center",
          gap: 10,
          padding: "0 12px",
          boxSizing: "border-box",
        }}
      >
        <div style={{ width: 32, height: 32, borderRadius: 10, backgroundColor: LIGHT.secondaryContainer, display: "flex", alignItems: "center", justifyContent: "center" }}>
          <Icon name="clock" size={16} color={LIGHT.secondary} />
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 3 }}>
          <span style={{ fontSize: 9.5, fontWeight: 700 }}>Horario de atención</span>
          <span style={{ fontFamily: FONTS.mono, fontSize: 8, color: LIGHT.muted }}>Miércoles · 10:00 – 11:00</span>
        </div>
        <Icon name="chevronRight" size={12} color={LIGHT.faint} style={{ marginLeft: "auto" }} />
      </div>

      <div
        style={{
          position: "absolute",
          left: 16,
          top: 376,
          width: 328,
          height: 68,
          borderRadius: 16,
          backgroundColor: LIGHT.primaryContainer,
          border: `1px solid ${LIGHT.primary}22`,
          padding: "12px 14px",
          boxSizing: "border-box",
        }}
      >
        <div style={{ fontSize: 8.5, fontWeight: 700, color: LIGHT.primary }}>Mensaje guardado</div>
        <div style={{ fontSize: 8.5, color: LIGHT.muted, marginTop: 5 }}>
          "Recuerda enviarme el avance antes del viernes."
        </div>
        <div style={{ position: "absolute", right: 12, top: 12, fontFamily: FONTS.mono, fontSize: 6.5, color: LIGHT.faint }}>09:12</div>
      </div>
    </ScreenShell>
  );
};

const ChatScreen: React.FC<{ frame: number; fps: number }> = ({ frame, fps }) => {
  const slideIn = springIn(frame, fps, 66, { damping: 20, stiffness: 150, mass: 0.9 });
  const chars = Math.floor(interpolate(frame, [108, 148], [0, MESSAGE.length], clamp));
  const typed = MESSAGE.slice(0, chars);
  const sending = frame >= 156;
  const sent = springIn(frame, fps, 160, { damping: 18, stiffness: 150, mass: 0.9 });
  const checks = progress(frame, 174, 188);
  const bubbleOut = progress(frame, 192, 208);
  const sendColor = chars > 0 ? LIGHT.secondary : LIGHT.surfaceContainerHigh;

  return (
    <div
      style={{
        position: "absolute",
        inset: 0,
        backgroundColor: LIGHT.background,
        transform: `translateX(${interpolate(slideIn, [0, 1], [360, 0])}px)`,
        zIndex: 20,
      }}
    >
      <StatusBar theme={LIGHT} time="9:06" />
      <div style={{ position: "absolute", top: 28, left: 0, right: 0, height: 52, display: "flex", alignItems: "center", gap: 9, padding: "0 14px", backgroundColor: LIGHT.surface, borderBottom: `1px solid ${LIGHT.border}`, boxSizing: "border-box" }}>
        <Icon name="back" size={17} color={LIGHT.ink} strokeWidth={2} />
        <div style={{ width: 30, height: 30, borderRadius: "50%", backgroundColor: LIGHT.primaryContainer, color: LIGHT.primary, display: "flex", alignItems: "center", justifyContent: "center", fontFamily: FONTS.mono, fontSize: 9, fontWeight: 700 }}>
          LR
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 2, flex: 1 }}>
          <span style={{ fontSize: 10.5, fontWeight: 700 }}>Prof. Luis Rivas</span>
          <span style={{ fontSize: 7, color: LIGHT.tertiary, display: "flex", alignItems: "center", gap: 3 }}>
            <span style={{ width: 4, height: 4, borderRadius: "50%", backgroundColor: LIGHT.tertiary }} />
            en línea
          </span>
        </div>
        <div style={{ width: 26, height: 26, borderRadius: "50%", backgroundColor: LIGHT.secondaryContainer, display: "flex", alignItems: "center", justifyContent: "center" }}>
          <Icon name="whatsapp" size={14} color="#25D366" />
        </div>
        <Icon name="phone" size={15} color={LIGHT.muted} />
      </div>

      <div style={{ position: "absolute", top: 94, left: 0, right: 0, display: "flex", justifyContent: "center" }}>
        <span style={{ fontSize: 6.5, fontFamily: FONTS.mono, color: LIGHT.faint, backgroundColor: LIGHT.surfaceContainer, borderRadius: 8, padding: "3px 8px" }}>
          HOY
        </span>
      </div>

      <div style={{ position: "absolute", left: 14, top: 118, width: 214, padding: "9px 11px", borderRadius: "14px 14px 14px 5px", backgroundColor: LIGHT.surface, border: `1px solid ${LIGHT.border}`, boxSizing: "border-box" }}>
        <div style={{ fontSize: 9.5, lineHeight: 1.35, color: LIGHT.ink }}>Recuerda enviarme el avance antes del viernes.</div>
        <div style={{ fontSize: 6.5, color: LIGHT.faint, marginTop: 4, fontFamily: FONTS.mono, textAlign: "right" }}>09:12</div>
      </div>

      <div style={{ position: "absolute", right: 14, top: 190, width: 198, padding: "9px 11px", borderRadius: "14px 14px 5px 14px", backgroundColor: LIGHT.primaryContainer, boxSizing: "border-box" }}>
        <div style={{ fontSize: 9.5, lineHeight: 1.35, color: LIGHT.ink }}>Sí, profe. Ya casi termino la guía.</div>
        <div style={{ fontSize: 6.5, color: LIGHT.muted, marginTop: 4, fontFamily: FONTS.mono, textAlign: "right", display: "flex", gap: 4, justifyContent: "flex-end", alignItems: "center" }}>
          09:13
          <CheckMark size={8} color={LIGHT.tertiary} progress={1} strokeWidth={3} />
          <CheckMark size={8} color={LIGHT.tertiary} progress={1} strokeWidth={3} style={{ marginLeft: -7 }} />
        </div>
      </div>

      {frame >= 156 ? (
        <div
          style={{
            position: "absolute",
            right: 14,
            top: 250,
            width: 226,
            padding: "9px 11px",
            borderRadius: "14px 14px 5px 14px",
            backgroundColor: LIGHT.primary,
            boxSizing: "border-box",
            opacity: sent,
            transform: `translateY(${interpolate(sent, [0, 1], [330, 0])}px) scale(${interpolate(sent, [0, 1], [0.9, 1])})`,
            zIndex: 30,
          }}
        >
          <div style={{ fontSize: 9.5, lineHeight: 1.4, color: LIGHT.onPrimary }}>{MESSAGE}</div>
          <div style={{ fontSize: 6.5, color: "#FFFFFFCC", marginTop: 4, fontFamily: FONTS.mono, textAlign: "right", display: "flex", justifyContent: "flex-end", alignItems: "center", gap: 2 }}>
            09:14
            <CheckMark size={8} color="#FFFFFF" progress={checks} strokeWidth={3} />
            <CheckMark size={8} color="#FFFFFF" progress={checks} strokeWidth={3} style={{ marginLeft: -7 }} />
          </div>
        </div>
      ) : null}

      <div
        style={{
          position: "absolute",
          left: 12,
          right: 12,
          bottom: 74,
          height: 44,
          borderRadius: 22,
          backgroundColor: LIGHT.surface,
          border: `1px solid ${LIGHT.border}`,
          display: "flex",
          alignItems: "center",
          padding: "0 5px 0 10px",
          boxSizing: "border-box",
          gap: 7,
        }}
      >
        <Icon name="plus" size={14} color={LIGHT.muted} />
        <div style={{ flex: 1, fontSize: 8.5, color: typed ? LIGHT.ink : LIGHT.faint, lineHeight: 1.3, maxHeight: 30, overflow: "hidden" }}>
          {typed || "Escribe un mensaje…"}
          {frame < 150 && frame > 108 ? <span style={{ opacity: 0.55 }}>|</span> : null}
        </div>
        <div
          style={{
            width: 32,
            height: 32,
            borderRadius: "50%",
            backgroundColor: sending ? LIGHT.secondary : sendColor,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            position: "relative",
            transform: `scale(${sending ? 0.94 : 1})`,
          }}
        >
          <Icon name="send" size={13} color={sending ? "#FFFFFF" : LIGHT.muted} strokeWidth={2} />
          <TapRipple x={16} y={16} start={156} color={LIGHT.secondary} maxSize={100} duration={14} />
        </div>
      </div>

      <div
        style={{
          position: "absolute",
          right: 14,
          top: 250,
          width: 226,
          height: 68,
          borderRadius: 14,
          backgroundColor: LIGHT.background,
          border: `1px solid ${LIGHT.border}`,
          opacity: bubbleOut > 0 ? 1 : 0,
          transform: `scale(${1 + bubbleOut * 14})`,
          transformOrigin: "center center",
          zIndex: 60,
        }}
      />
    </div>
  );
};

export const S06TeachersScreen: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const headlineOpacity = interpolate(frame, [196, 208], [1, 0], clamp);

  return (
    <AbsoluteFill style={{ fontFamily: FONTS.sans }}>
      <PaperBackground mode="light" />
      <PhoneStage scale={1 + frame * 0.00004}>
        <PhoneFrame mode="light">
          <TeacherScreen frame={frame} />
          <div style={{ position: "absolute", inset: 0 }}>
            <ChatScreen frame={frame} fps={fps} />
          </div>
        </PhoneFrame>
      </PhoneStage>

      <Headline
        lines={["Tu profe,", "a un toque."]}
        color={LIGHT.ink}
        top={266}
        opacity={headlineOpacity}
      />
    </AbsoluteFill>
  );
};
