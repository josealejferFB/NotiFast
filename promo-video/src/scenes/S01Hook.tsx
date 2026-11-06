import React from "react";
import { AbsoluteFill, interpolate, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { DacePdf } from "../components/DacePdf";
import { Headline } from "../components/Headline";
import { PaperBackground } from "../components/PaperBackground";
import { PhoneFrame } from "../components/PhoneFrame";
import { PhoneStage } from "../components/PhoneStage";
import { ScreenShell } from "../components/ui/Screen";
import { FONTS, LIGHT } from "../tokens";
import { clamp, lerp } from "../utils";

const SkeletonSchedule: React.FC = () => (
  <ScreenShell theme={LIGHT} showHomeIndicator={false}>
    <div style={{ position: "absolute", inset: 0, opacity: 0.62 }}>
      <div style={{ position: "absolute", left: 16, top: 44, width: 34, height: 34, borderRadius: "50%", backgroundColor: LIGHT.primaryContainer }} />
      <div style={{ position: "absolute", left: 60, top: 48, width: 110, height: 9, borderRadius: 5, backgroundColor: LIGHT.surfaceContainerHigh }} />
      <div style={{ position: "absolute", left: 60, top: 63, width: 74, height: 6, borderRadius: 3, backgroundColor: LIGHT.surfaceContainer }} />
      <div style={{ position: "absolute", left: 16, top: 92, right: 16, height: 20, borderRadius: 10, backgroundColor: LIGHT.surfaceContainer }} />
      {[0, 1, 2, 3, 4].map((i) => (
        <div
          key={i}
          style={{
            position: "absolute",
            left: 16 + i * 66,
            top: 122,
            width: 58,
            height: 30,
            borderRadius: 10,
            backgroundColor: i === 2 ? LIGHT.primaryContainer : LIGHT.surface,
            border: `1px solid ${i === 2 ? LIGHT.primary + "55" : LIGHT.border}`,
          }}
        />
      ))}
      {[0, 1, 2, 3, 4, 5, 6, 7].map((row) => (
        <div
          key={row}
          style={{
            position: "absolute",
            left: 16,
            right: 16,
            top: 170 + row * 34,
            height: 1,
            backgroundColor: LIGHT.border,
            opacity: 0.7,
          }}
        />
      ))}
      {[
        { left: 50, top: 170, color: "#9E1B32" },
        { left: 116, top: 272, color: "#2A7B7B" },
        { left: 50, top: 374, color: "#4338CA" },
        { left: 314, top: 238, color: "#C2410C" },
      ].map((b, i) => (
        <div
          key={i}
          style={{
            position: "absolute",
            left: b.left,
            top: b.top,
            width: 58,
            height: 50,
            borderRadius: 7,
            backgroundColor: `${b.color}18`,
            borderLeft: `3px solid ${b.color}`,
          }}
        />
      ))}
      <div style={{ position: "absolute", left: 16, top: 470, width: 328, height: 82, borderRadius: 16, backgroundColor: LIGHT.surface, border: `1px solid ${LIGHT.border}` }} />
      <div style={{ position: "absolute", left: 16, top: 564, width: 328, height: 82, borderRadius: 16, backgroundColor: LIGHT.surface, border: `1px solid ${LIGHT.border}` }} />
    </div>
  </ScreenShell>
);

export const S01Hook: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const phoneY = interpolate(frame, [0, 22], [650, 500], clamp);
  const settle = interpolate(frame, [0, 14], [2, 0], clamp);
  const underline = interpolate(frame, [20, 34], [0, 1], clamp);

  const drop = spring({
    frame: frame - 2,
    fps,
    config: { damping: 15, stiffness: 150, mass: 1 },
    durationInFrames: 30,
  });
  const baseY = lerp(1500, 1080, Math.max(0, Math.min(1, drop)));
  const flight = interpolate(frame, [56, 75], [0, 1], { ...clamp, easing: (t) => t * t * (3 - 2 * t) });
  const pdfY = lerp(baseY, 952, flight);
  const pdfX = lerp(325, 232, flight);
  const pdfScale = lerp(1, 0.26, flight);
  const pdfRotate = lerp(-3.5, 3, flight);

  const headlineOpacity = interpolate(frame, [66, 74], [1, 0], clamp);

  return (
    <AbsoluteFill style={{ fontFamily: FONTS.sans }}>
      <PaperBackground mode="light" />
      <PhoneStage y={phoneY} opacity={0.96}>
        <PhoneFrame mode="light">
          <SkeletonSchedule />
        </PhoneFrame>
      </PhoneStage>

      <div
        style={{
          position: "absolute",
          left: pdfX,
          top: pdfY,
          transform: `rotate(${pdfRotate}deg) scale(${pdfScale})`,
          transformOrigin: "top left",
          zIndex: 30,
        }}
      >
        <DacePdf width={430} />
      </div>

      <Headline
        lines={["Tu horario DACE,", "por fin en orden."]}
        highlight="orden"
        color={LIGHT.ink}
        top={238}
        monoLabel="UNERG · DACE"
        monoLabelColor={LIGHT.primary}
        underline={underline}
        translateY={settle}
        opacity={headlineOpacity}
      />
    </AbsoluteFill>
  );
};
