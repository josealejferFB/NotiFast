import React from "react";
import { AbsoluteFill, interpolate, useCurrentFrame, useVideoConfig } from "remotion";
import { Headline } from "../components/Headline";
import { PaperBackground } from "../components/PaperBackground";
import { PhoneFrame } from "../components/PhoneFrame";
import { PhoneStage } from "../components/PhoneStage";
import { Avatar, IconButton } from "../components/ui/Atoms";
import { BottomNav } from "../components/ui/BottomNav";
import { Icon } from "../components/ui/Icon";
import { ScreenShell } from "../components/ui/Screen";
import { FONTS, LIGHT, SUBJECTS } from "../tokens";
import { clamp, progress, springIn } from "../utils";

const DAYS = [
  { label: "Lun", date: "2", today: false },
  { label: "Mar", date: "3", today: false },
  { label: "Mié", date: "4", today: true },
  { label: "Jue", date: "5", today: false },
  { label: "Vie", date: "6", today: false },
];

const HOURS = [7, 8, 9, 10, 11, 12, 13, 14, 15, 16];
const ROW_HEIGHT = 34;
const GRID_TOP = 118;
const HEADER_H = 22;
const TIME_W = 26;
const DAY_W = 60.4;

const BLOCKS = [
  { id: "calculo", day: 0, start: 1, span: 1.5 },
  { id: "calculo", day: 2, start: 1, span: 1.5 },
  { id: "fisica", day: 1, start: 3, span: 1.5 },
  { id: "fisica", day: 3, start: 3, span: 1.5 },
  { id: "programacion", day: 0, start: 7, span: 1.5 },
  { id: "programacion", day: 2, start: 7, span: 1.5 },
  { id: "ingles", day: 4, start: 2, span: 1.5 },
];

export const S03ScheduleScreen: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const gridOut = interpolate(frame, [132, 152], [1, 0], clamp);
  const gridLift = interpolate(frame, [132, 152], [0, -16], clamp);
  const stripIn = springIn(frame, fps, 138, { damping: 18, stiffness: 150, mass: 0.8 });
  const cardsIn = (index: number) => springIn(frame, fps, 150 + index * 7, { damping: 18, stiffness: 140, mass: 0.9 });
  const todayLine = progress(frame, 74, 100);
  const glow = interpolate(frame, [92, 112], [0, 1], clamp);
  const push = interpolate(frame, [204, 240], [0, 1], { ...clamp, easing: (t) => t * t * (3 - 2 * t) });

  return (
    <AbsoluteFill style={{ fontFamily: FONTS.sans }}>
      <PaperBackground mode="light" />
      <PhoneStage y={500 - 16 * push} scale={1 + 0.045 * push}>
        <PhoneFrame mode="light">
          <ScreenShell theme={LIGHT} time="9:06">
            <div style={{ position: "absolute", top: 36, left: 16, right: 16, display: "flex", alignItems: "center", gap: 9 }}>
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
                  <Icon name="calendar" size={10} color={LIGHT.primary} />
                  <span style={{ fontSize: 7.5, color: LIGHT.muted, fontFamily: FONTS.mono, fontWeight: 600 }}>Mi Agenda</span>
                </div>
              </div>
              <IconButton theme={LIGHT} name="search" box={26} size={13} />
              <IconButton theme={LIGHT} name="phone" box={26} size={13} />
              <IconButton theme={LIGHT} name="bell" box={26} size={13} />
            </div>

            {/* Segmented view switcher matching real app */}
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
                { id: "timeline", label: "Timeline", icon: "list" as const },
                { id: "cal", label: "Calendario", icon: "calendar" as const },
                { id: "semana", label: "Semana", icon: "grid" as const },
              ].map((tab) => {
                const isSelected = frame < 138 ? tab.id === "semana" : tab.id === "timeline";
                return (
                  <div
                    key={tab.id}
                    style={{
                      flex: 1,
                      borderRadius: 14,
                      backgroundColor: isSelected ? LIGHT.surface : "transparent",
                      boxShadow: isSelected ? "0 2px 6px rgba(0,0,0,0.06)" : "none",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      gap: 4,
                      fontSize: 8.5,
                      fontWeight: isSelected ? 700 : 500,
                      color: isSelected ? LIGHT.ink : LIGHT.muted,
                    }}
                  >
                    <Icon name={tab.icon} size={11} color={isSelected ? LIGHT.primary : LIGHT.muted} />
                    {tab.label}
                  </div>
                );
              })}
            </div>

            <div
              style={{
                position: "absolute",
                left: 16,
                top: GRID_TOP,
                width: 328,
                height: 328,
                opacity: gridOut,
                transform: `translateY(${gridLift}px)`,
              }}
            >
              <div style={{ position: "absolute", top: 0, left: TIME_W, display: "flex", width: DAY_W * 5 }}>
                {DAYS.map((day) => (
                  <div
                    key={day.label}
                    style={{
                      width: DAY_W,
                      textAlign: "center",
                      fontSize: 7.5,
                      fontWeight: day.today ? 800 : 600,
                      color: day.today ? LIGHT.primary : LIGHT.muted,
                    }}
                  >
                    {day.label} {day.date}
                    {day.today ? (
                      <span
                        style={{
                          display: "block",
                          width: 4,
                          height: 4,
                          borderRadius: "50%",
                          backgroundColor: LIGHT.primary,
                          margin: "2px auto 0",
                        }}
                      />
                    ) : null}
                  </div>
                ))}
              </div>

              {HOURS.map((hour, i) => (
                <div
                  key={hour}
                  style={{
                    position: "absolute",
                    top: HEADER_H + i * ROW_HEIGHT - 3,
                    left: 0,
                    width: TIME_W - 6,
                    textAlign: "right",
                    fontFamily: FONTS.mono,
                    fontSize: 6.5,
                    color: LIGHT.faint,
                  }}
                >
                  {hour}:00
                </div>
              ))}

              <div
                style={{
                  position: "absolute",
                  left: TIME_W,
                  top: HEADER_H,
                  width: DAY_W * 5,
                  height: ROW_HEIGHT * 9,
                  borderTop: `1px solid ${LIGHT.border}`,
                  borderLeft: `1px solid ${LIGHT.border}`,
                  backgroundColor: "rgba(255,255,255,0.5)",
                }}
              >
                {HOURS.slice(0, 9).map((hour, i) => (
                  <div
                    key={hour}
                    style={{
                      position: "absolute",
                      top: i * ROW_HEIGHT,
                      left: 0,
                      right: 0,
                      height: 1,
                      backgroundColor: LIGHT.border,
                      opacity: 0.75,
                    }}
                  />
                ))}
                {[1, 2, 3, 4].map((i) => (
                  <div
                    key={i}
                    style={{
                      position: "absolute",
                      top: 0,
                      bottom: 0,
                      left: i * DAY_W,
                      width: 1,
                      backgroundColor: LIGHT.border,
                      opacity: 0.75,
                    }}
                  />
                ))}

                <div
                  style={{
                    position: "absolute",
                    left: 2 * DAY_W,
                    top: 0,
                    width: DAY_W,
                    height: "100%",
                    backgroundColor: "rgba(158,27,50,0.035)",
                  }}
                />

                {BLOCKS.map((block, index) => {
                  const subject = SUBJECTS[block.id];
                  const appear = springIn(frame, fps, 12 + index * 3, { damping: 16, stiffness: 160, mass: 0.8 });
                  const isFocus = block.id === "calculo" && block.day === 2;
                  return (
                    <div
                      key={`${block.id}-${block.day}`}
                      style={{
                        position: "absolute",
                        left: block.day * DAY_W + 2,
                        top: (block.start - 1) * ROW_HEIGHT + 2,
                        width: DAY_W - 4,
                        height: ROW_HEIGHT * block.span - 3,
                        borderRadius: 6,
                        backgroundColor: `${subject.color}16`,
                        borderLeft: `2.5px solid ${subject.color}`,
                        padding: "3px 4px",
                        boxSizing: "border-box",
                        opacity: appear * (isFocus ? 1 : 0.94),
                        transform: `translateY(${interpolate(appear, [0, 1], [-14, 0])}px)`,
                        boxShadow: isFocus
                          ? `0 0 0 ${1 + glow}px ${LIGHT.primary}55, 0 0 14px ${LIGHT.primary}${glow > 0.5 ? "33" : "00"}`
                          : "none",
                      }}
                    >
                      <div style={{ fontSize: 6.3, fontWeight: 800, color: subject.color, lineHeight: 1.1 }}>
                        {block.id === "programacion" ? "Programación" : subject.short}
                      </div>
                      <div style={{ fontFamily: FONTS.mono, fontSize: 5.2, color: LIGHT.muted, marginTop: 1 }}>
                        {block.start + 7}:00
                      </div>
                      <div style={{ fontSize: 5, color: LIGHT.muted, marginTop: 1 }}>{subject.room}</div>
                    </div>
                  );
                })}

                <div
                  style={{
                    position: "absolute",
                    left: 0,
                    top: ROW_HEIGHT,
                    width: `${todayLine * 100}%`,
                    height: 1.4,
                    backgroundColor: LIGHT.primary,
                    boxShadow: `0 0 8px ${LIGHT.primary}55`,
                  }}
                >
                  <span
                    style={{
                      position: "absolute",
                      right: -3,
                      top: -2.5,
                      width: 6,
                      height: 6,
                      borderRadius: "50%",
                      backgroundColor: LIGHT.primary,
                      border: "1.5px solid #FFFFFF",
                    }}
                  />
                </div>
              </div>
            </div>

            <div
              style={{
                position: "absolute",
                left: 16,
                top: 118,
                width: 328,
                height: 44,
                borderRadius: 14,
                backgroundColor: LIGHT.surface,
                border: `1px solid ${LIGHT.border}`,
                display: "flex",
                alignItems: "center",
                justifyContent: "space-around",
                opacity: stripIn,
                transform: `translateY(${interpolate(stripIn, [0, 1], [12, 0])}px)`,
                zIndex: 3,
              }}
            >
              {DAYS.map((day) => (
                <div key={day.label} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 4 }}>
                  <span style={{ fontSize: 7.5, fontWeight: day.today ? 800 : 600, color: day.today ? LIGHT.primary : LIGHT.muted }}>
                    {day.label}
                  </span>
                  <span style={{ display: "flex", gap: 2 }}>
                    {day.label === "Lun" ? (
                      <>
                        <span style={{ width: 5, height: 5, borderRadius: "50%", backgroundColor: SUBJECTS.calculo.color }} />
                        <span style={{ width: 5, height: 5, borderRadius: "50%", backgroundColor: SUBJECTS.programacion.color }} />
                      </>
                    ) : null}
                    {day.label === "Mar" || day.label === "Jue" ? (
                      <span style={{ width: 5, height: 5, borderRadius: "50%", backgroundColor: SUBJECTS.fisica.color }} />
                    ) : null}
                    {day.label === "Mié" ? (
                      <>
                        <span style={{ width: 5, height: 5, borderRadius: "50%", backgroundColor: SUBJECTS.calculo.color }} />
                        <span style={{ width: 5, height: 5, borderRadius: "50%", backgroundColor: SUBJECTS.programacion.color }} />
                      </>
                    ) : null}
                    {day.label === "Vie" ? (
                      <span style={{ width: 5, height: 5, borderRadius: "50%", backgroundColor: SUBJECTS.ingles.color }} />
                    ) : null}
                  </span>
                </div>
              ))}
            </div>

            {(["calculo", "fisica", "programacion", "ingles"] as const).map((id, index) => {              const subject = SUBJECTS[id];
              const appear = cardsIn(index);
              return (
                <div
                  key={id}
                  style={{
                    position: "absolute",
                    left: 16,
                    top: 176 + index * 101,
                    width: 328,
                    height: 92,
                    borderRadius: 18,
                    backgroundColor: LIGHT.surface,
                    border: `1px solid ${id === "calculo" ? LIGHT.primary + "55" : LIGHT.border}`,
                    boxSizing: "border-box",
                    opacity: appear,
                    transform: `translateY(${interpolate(appear, [0, 1], [46, 0])}px)`,
                    zIndex: 4,
                  }}
                >
                  <div
                    style={{
                      position: "absolute",
                      left: 12,
                      top: 12,
                      width: 34,
                      height: 34,
                      borderRadius: 10,
                      backgroundColor: subject.soft,
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    <span style={{ fontFamily: FONTS.mono, fontSize: 11, fontWeight: 700, color: subject.color }}>
                      {subject.name.slice(0, 2)}
                    </span>
                  </div>
                  <div style={{ position: "absolute", left: 56, top: 12, display: "flex", flexDirection: "column", gap: 4 }}>
                    <span style={{ fontSize: 11.5, fontWeight: 700, letterSpacing: "-0.01em" }}>{subject.name}</span>
                    <span style={{ fontSize: 8, color: LIGHT.muted }}>
                      {subject.section} · {subject.room} · {subject.teacher}
                    </span>
                    <span style={{ fontFamily: FONTS.mono, fontSize: 8, color: LIGHT.muted, letterSpacing: "0.01em" }}>
                      {subject.days} · {subject.time}
                    </span>
                  </div>
                  <div style={{ position: "absolute", right: 12, top: 14, display: "flex", flexDirection: "column", alignItems: "flex-end" }}>
                    <span style={{ fontFamily: FONTS.mono, fontSize: 18, fontWeight: 700, color: LIGHT.tertiary, lineHeight: 1 }}>
                      {subject.average}
                    </span>
                    <span style={{ fontSize: 6, color: LIGHT.muted, letterSpacing: "0.1em", marginTop: 4 }}>PROMEDIO</span>
                  </div>
                  {id === "calculo" ? (
                    <div
                      style={{
                        position: "absolute",
                        right: 12,
                        bottom: 10,
                        fontSize: 6.5,
                        fontWeight: 700,
                        color: LIGHT.primary,
                        border: `1px solid ${LIGHT.primary}44`,
                        backgroundColor: LIGHT.primaryContainer,
                        borderRadius: 8,
                        padding: "2px 6px",
                        letterSpacing: "0.04em",
                      }}
                    >
                      HOY 08:00
                    </div>
                  ) : null}
                </div>
              );
            })}

            <BottomNav theme={LIGHT} active={1} top={648} showFab={true} />
          </ScreenShell>
        </PhoneFrame>
      </PhoneStage>

      <Headline
        lines={["Tu semana,", "clara."]}
        color={LIGHT.ink}
        top={266}
        opacity={interpolate(frame, [226, 238], [1, 0], clamp)}
      />
    </AbsoluteFill>
  );
};
