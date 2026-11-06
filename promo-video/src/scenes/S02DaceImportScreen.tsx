import React from "react";
import { AbsoluteFill, interpolate, useCurrentFrame, useVideoConfig } from "remotion";
import { DacePdf } from "../components/DacePdf";
import { Headline } from "../components/Headline";
import { PaperBackground } from "../components/PaperBackground";
import { PhoneFrame } from "../components/PhoneFrame";
import { PhoneStage } from "../components/PhoneStage";
import { TapRipple } from "../components/TapRipple";
import { Avatar, Chip, IconButton } from "../components/ui/Atoms";
import { CheckMark } from "../components/ui/ProgressRing";
import { Icon } from "../components/ui/Icon";
import { ScreenShell } from "../components/ui/Screen";
import { FONTS, LIGHT, SUBJECTS } from "../tokens";
import { clamp, fadeIn, progress, springIn } from "../utils";

const rows = ["calculo", "fisica", "programacion", "ingles"] as const;

export const S02DaceImportScreen: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const sheetIn = springIn(frame, fps, 0, { damping: 20, stiffness: 160, mass: 0.8 });
  const scan = progress(frame, 10, 30);
  const scanVisible = frame >= 9 && frame <= 31;
  const rowsBase = 34;

  const fill = progress(frame, 128, 150);
  const success = frame >= 164;
  const toastIn = springIn(frame, fps, 170, { damping: 18, stiffness: 170, mass: 0.7 });
  const wipe = progress(frame, 196, 214);

  const copyOut = interpolate(frame, [92, 102], [1, 0], clamp);
  const copyIn = interpolate(frame, [108, 118], [0, 1], clamp) * interpolate(frame, [198, 208], [1, 0], clamp);

  return (
    <AbsoluteFill style={{ fontFamily: FONTS.sans }}>
      <PaperBackground mode="light" />
      <PhoneStage scale={1 + frame * 0.00004}>
        <PhoneFrame mode="light">
          <ScreenShell theme={LIGHT} time="9:06">
            <div style={{ position: "absolute", top: 36, left: 16, right: 16, display: "flex", alignItems: "center", gap: 9 }}>
              <Avatar theme={LIGHT} />
              <div style={{ display: "flex", flexDirection: "column", gap: 3, flex: 1 }}>
                <span style={{ fontSize: 15, fontWeight: 700 }}>Mi agenda</span>
                <span style={{ fontSize: 8.5, color: LIGHT.muted, fontFamily: FONTS.mono }}>Miércoles · 4 nov</span>
              </div>
              <IconButton theme={LIGHT} name="bell" />
            </div>

            <div style={{ position: "absolute", top: 84, left: 16, right: 16, display: "flex", gap: 6 }}>
              {["Lun", "Mar", "Mié", "Jue", "Vie"].map((day, i) => (
                <div
                  key={day}
                  style={{
                    flex: 1,
                    height: 28,
                    borderRadius: 9,
                    backgroundColor: i === 2 ? LIGHT.primaryContainer : LIGHT.surface,
                    border: `1px solid ${i === 2 ? LIGHT.primary + "55" : LIGHT.border}`,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontSize: 8,
                    fontWeight: 600,
                    color: i === 2 ? LIGHT.primary : LIGHT.muted,
                  }}
                >
                  {day}
                </div>
              ))}
            </div>

            <div
              style={{
                position: "absolute",
                top: 126,
                left: 16,
                right: 16,
                height: 56,
                borderRadius: 14,
                border: `1px dashed ${LIGHT.border}`,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                gap: 8,
                color: LIGHT.faint,
                fontSize: 9,
              }}
            >
              <Icon name="calendar" size={16} color={LIGHT.faint} />
              Sin clases todavía
            </div>

            <div
              style={{
                position: "absolute",
                top: 28,
                left: 0,
                right: 0,
                bottom: 0,
                backgroundColor: "rgba(24,26,31,0.30)",
                opacity: interpolate(sheetIn, [0, 1], [0, 1]),
                zIndex: 5,
              }}
            />

            <div
              style={{
                position: "absolute",
                left: 0,
                right: 0,
                top: 150,
                bottom: 0,
                backgroundColor: LIGHT.surface,
                borderRadius: "28px 28px 0 0",
                borderTop: `1px solid ${LIGHT.border}`,
                transform: `translateY(${interpolate(sheetIn, [0, 1], [26, 0])}px)`,
                zIndex: 10,
                overflow: "hidden",
              }}
            >
              <div style={{ position: "absolute", top: 8, left: 160, width: 40, height: 4, borderRadius: 2, backgroundColor: LIGHT.border }} />

              <div style={{ position: "absolute", top: 22, left: 16, right: 16, display: "flex", alignItems: "center" }}>
                <div style={{ display: "flex", flexDirection: "column", gap: 3, flex: 1 }}>
                  <span style={{ fontSize: 16, fontWeight: 700, letterSpacing: "-0.01em" }}>Importar horario</span>
                  <span style={{ fontSize: 8, color: LIGHT.muted, fontFamily: FONTS.mono, letterSpacing: "0.04em" }}>
                    DACE · UNERG · 2026-02
                  </span>
                </div>
                <Chip theme={LIGHT} label="100 % offline" tone="secondary" size="xs" icon="offline" />
              </div>

              <div
                style={{
                  position: "absolute",
                  top: 64,
                  left: 16,
                  width: 328,
                  height: 118,
                  borderRadius: 16,
                  border: `1.5px dashed ${frame > 30 ? LIGHT.tertiary : "#C9C1B6"}`,
                  backgroundColor: frame > 30 ? LIGHT.tertiaryContainer : LIGHT.surfaceContainer,
                  boxSizing: "border-box",
                  overflow: "hidden",
                }}
              >
                <div
                  style={{
                    position: "absolute",
                    left: 10,
                    top: 9,
                    transform: `scale(${interpolate(frame, [0, 10], [1.22, 1], clamp)})`,
                    transformOrigin: "top left",
                  }}
                >
                  <DacePdf width={430} scale={0.13} />
                </div>
                <div style={{ position: "absolute", left: 78, top: 24, display: "flex", flexDirection: "column", gap: 5 }}>
                  <span style={{ fontSize: 10.5, fontWeight: 700 }}>DACE_2026-02.pdf</span>
                  <span style={{ fontSize: 8, color: LIGHT.muted, fontFamily: FONTS.mono }}>4 materias · 12 clases</span>
                  <span
                    style={{
                      fontSize: 7.5,
                      fontWeight: 600,
                      color: frame > 30 ? LIGHT.tertiary : LIGHT.secondary,
                      display: "flex",
                      alignItems: "center",
                      gap: 4,
                    }}
                  >
                    {frame > 30 ? <Icon name="check" size={9} color={LIGHT.tertiary} strokeWidth={2.6} /> : null}
                    {frame > 30 ? "Materias detectadas" : "Escaneando documento…"}
                  </span>
                </div>
                {scanVisible ? (
                  <div
                    style={{
                      position: "absolute",
                      left: 4,
                      right: 4,
                      top: 6 + scan * 104,
                      height: 2,
                      background: `linear-gradient(90deg, ${LIGHT.secondary}00, ${LIGHT.secondary}, ${LIGHT.secondary}00)`,
                      opacity: Math.sin(scan * Math.PI) * 0.9,
                      boxShadow: `0 0 12px ${LIGHT.secondary}77`,
                    }}
                  />
                ) : null}
              </div>

              {rows.map((id, index) => {
                const subject = SUBJECTS[id];
                const appear = springIn(frame, fps, rowsBase + index * 7, { damping: 18, stiffness: 150, mass: 0.8 });
                const check = progress(frame, 56 + index * 7, 70 + index * 7);
                return (
                  <div
                    key={id}
                    style={{
                      position: "absolute",
                      top: 192 + index * 54,
                      left: 16,
                      width: 328,
                      height: 48,
                      borderRadius: 13,
                      border: `1px solid ${LIGHT.border}`,
                      backgroundColor: LIGHT.surface,
                      display: "flex",
                      alignItems: "center",
                      gap: 9,
                      padding: "0 10px",
                      boxSizing: "border-box",
                      opacity: appear,
                      transform: `translateY(${interpolate(appear, [0, 1], [12, 0])}px)`,
                    }}
                  >
                    <div
                      style={{
                        width: 26,
                        height: 26,
                        borderRadius: 8,
                        backgroundColor: subject.soft,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                    >
                      <Icon name="book" size={14} color={subject.color} strokeWidth={2} filled />
                    </div>
                    <div style={{ display: "flex", flexDirection: "column", gap: 2, flex: 1, minWidth: 0 }}>
                      <span style={{ fontSize: 9.5, fontWeight: 700 }}>{subject.name}</span>
                      <span style={{ fontSize: 7, color: LIGHT.muted, fontFamily: FONTS.mono, whiteSpace: "nowrap" }}>
                        {subject.days} · {subject.time} · {subject.room}
                      </span>
                    </div>
                    <div
                      style={{
                        width: 16,
                        height: 16,
                        borderRadius: "50%",
                        backgroundColor: check > 0 ? LIGHT.tertiary : "transparent",
                        border: `1.4px solid ${check > 0 ? LIGHT.tertiary : LIGHT.border}`,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                    >
                      <CheckMark size={10} color="#FFFFFF" progress={check} strokeWidth={2.8} />
                    </div>
                  </div>
                );
              })}

              <div style={{ position: "absolute", top: 412, left: 16, right: 16, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                <span style={{ fontSize: 8, color: LIGHT.muted, fontFamily: FONTS.mono }}>4 materias · 12 clases</span>
                <span style={{ fontSize: 8, color: LIGHT.tertiary, fontWeight: 700, display: "flex", gap: 4, alignItems: "center" }}>
                  <Icon name="sparkle" size={10} color={LIGHT.tertiary} />
                  Todo se ordena
                </span>
              </div>

              <div
                style={{
                  position: "absolute",
                  top: 434,
                  left: 16,
                  width: 328,
                  height: 48,
                  borderRadius: 13,
                  backgroundColor: success ? LIGHT.tertiary : LIGHT.surfaceContainer,
                  border: `1px solid ${success ? LIGHT.tertiary : LIGHT.primary + "66"}`,
                  overflow: "hidden",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                }}
              >
                {!success ? (
                  <div
                    style={{
                      position: "absolute",
                      left: 0,
                      top: 0,
                      bottom: 0,
                      width: `${fill * 100}%`,
                      backgroundColor: LIGHT.primary,
                    }}
                  />
                ) : null}
                <div
                  style={{
                    position: "relative",
                    display: "flex",
                    alignItems: "center",
                    gap: 6,
                    color: success ? LIGHT.onPrimary : fill > 0.55 ? LIGHT.onPrimary : LIGHT.primary,
                    fontSize: 11.5,
                    fontWeight: 700,
                  }}
                >
                  {success ? <CheckMark size={14} color="#FFFFFF" progress={progress(frame, 164, 174)} strokeWidth={2.8} /> : null}
                  {success ? "Horario importado" : "Importar 4 materias"}
                </div>
                <TapRipple x={164} y={24} start={152} color={LIGHT.onPrimary} maxSize={140} />
              </div>

              <div
                style={{
                  position: "absolute",
                  top: 498,
                  left: 0,
                  right: 0,
                  display: "flex",
                  justifyContent: "center",
                  opacity: toastIn,
                  transform: `translateY(${interpolate(toastIn, [0, 1], [-10, 0])}px)`,
                }}
              >
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 6,
                    height: 30,
                    padding: "0 12px",
                    borderRadius: 15,
                    backgroundColor: LIGHT.ink,
                    color: "#FFFFFF",
                    fontSize: 9,
                    fontWeight: 600,
                  }}
                >
                  <div
                    style={{
                      width: 14,
                      height: 14,
                      borderRadius: "50%",
                      backgroundColor: LIGHT.tertiary,
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    <CheckMark size={9} color="#FFFFFF" progress={1} strokeWidth={3} />
                  </div>
                  Horario importado
                </div>
              </div>

            </div>

            {wipe > 0 ? (
              <div
                style={{
                  position: "absolute",
                  left: 180 - 750 * wipe,
                  top: 608 - 750 * wipe,
                  width: 1500 * wipe,
                  height: 1500 * wipe,
                  borderRadius: "50%",
                  backgroundColor: LIGHT.background,
                  zIndex: 50,
                }}
              />
            ) : null}
          </ScreenShell>
        </PhoneFrame>
      </PhoneStage>

      <Headline
        lines={["Sube tu PDF", "del DACE."]}
        color={LIGHT.ink}
        top={266}
        opacity={copyOut}
        translateY={interpolate(copyOut, [0, 1], [-8, 0])}
      />
      <Headline
        lines={["Todo se ordena."]}
        color={LIGHT.ink}
        top={266}
        opacity={copyIn}
        translateY={interpolate(copyIn, [0, 1], [8, 0])}
      />
    </AbsoluteFill>
  );
};
