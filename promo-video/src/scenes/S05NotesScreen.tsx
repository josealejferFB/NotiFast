import React from "react";
import { AbsoluteFill, interpolate, useCurrentFrame, useVideoConfig } from "remotion";
import { Headline } from "../components/Headline";
import { PaperBackground } from "../components/PaperBackground";
import { PhoneFrame } from "../components/PhoneFrame";
import { PhoneStage } from "../components/PhoneStage";
import { TapRipple } from "../components/TapRipple";
import { CheckMark } from "../components/ui/ProgressRing";
import { Icon } from "../components/ui/Icon";
import { ScreenShell } from "../components/ui/Screen";
import { FONTS, LIGHT } from "../tokens";
import { clamp, progress, springIn } from "../utils";

/**
 * Simulación de fotografía real de pizarra escolar / universitaria adjunta al apunte,
 * conforme al diseño de bloque de foto de NotiFast (05-note-editor-light.png).
 */
const ChalkboardPhoto: React.FC<{ draw: number; circle: number; arrow: number }> = ({ draw, circle, arrow }) => (
  <div
    style={{
      borderRadius: 14,
      backgroundColor: "#182426",
      border: "3px solid #8B5E3C", // marco de madera de pizarra
      boxShadow: "0 6px 16px rgba(0,0,0,0.22)",
      position: "relative",
      overflow: "hidden",
      height: 124,
    }}
  >
    {/* Badge de foto de cámara adjunta */}
    <div
      style={{
        position: "absolute",
        left: 8,
        top: 6,
        display: "flex",
        alignItems: "center",
        gap: 5,
        backgroundColor: "rgba(0,0,0,0.55)",
        borderRadius: 8,
        padding: "2px 7px",
        zIndex: 5,
      }}
    >
      <Icon name="image" size={10} color="#F3EFEA" />
      <span style={{ fontFamily: FONTS.mono, fontSize: 6.5, color: "#F3EFEA", fontWeight: 600 }}>
        Foto de Pizarra · Aula 204
      </span>
    </div>

    {/* Textura de pizarra de tiza y cuadrícula sutil */}
    <svg width="320" height="124" viewBox="0 0 320 124" style={{ position: "absolute", inset: 0 }}>
      <defs>
        <pattern id="chalkboardGrid" width="20" height="20" patternUnits="userSpaceOnUse">
          <path d="M 20 0 L 0 0 0 20" fill="none" stroke="#FFFFFF" strokeOpacity="0.035" strokeWidth="1" />
        </pattern>
      </defs>
      <rect width="320" height="124" fill="url(#chalkboardGrid)" />

      {/* Ejes cartesianos en tiza blanca */}
      <path d="M 20 104 H 296" stroke="#E2E8F0" strokeOpacity="0.5" strokeWidth="1.4" strokeLinecap="round" />
      <path d="M 34 114 V 16" stroke="#E2E8F0" strokeOpacity="0.5" strokeWidth="1.4" strokeLinecap="round" />
      <text x="290" y="114" fill="#CBD5E1" fontSize="7.5" fontFamily={FONTS.mono}>X</text>
      <text x="24" y="20" fill="#CBD5E1" fontSize="7.5" fontFamily={FONTS.mono}>Y</text>

      {/* Curva f(x) trazada con tiza blanca */}
      <path
        d="M 42 98 C 76 70, 106 30, 144 52 C 178 72, 204 78, 234 42 C 258 18, 276 22, 292 32"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="2.2"
        strokeLinecap="round"
        strokeDasharray="1"
        pathLength={1}
        strokeDashoffset={1 - draw}
      />

      {/* Recta tangente en tiza amarilla */}
      <path
        d="M 126 78 L 240 24"
        fill="none"
        stroke="#FEF08A"
        strokeWidth="1.7"
        strokeLinecap="round"
        strokeDasharray="4 3"
        opacity={0.85 * draw}
      />
      <circle cx="183" cy="51" r="3" fill="#FEF08A" opacity={draw} />
      <text x="190" y="49" fill="#FEF08A" fontSize="7" fontFamily={FONTS.mono} opacity={draw}>P(c, f(c))</text>

      {/* Círculo de tiza rosada resaltando el punto de derivada */}
      <circle
        cx="183"
        cy="51"
        r="15"
        fill="none"
        stroke="#FDA4AF"
        strokeWidth="1.7"
        pathLength={1}
        strokeDasharray={1}
        strokeDashoffset={1 - circle}
      />

      {/* Fórmula manuscrita en tiza */}
      <g opacity={arrow}>
        <text x="44" y="30" fill="#FDE047" fontSize="8.5" fontFamily={FONTS.mono} fontWeight="700">
          f'(c) = [f(b) − f(a)] / (b − a)
        </text>
        <text x="44" y="42" fill="#E2E8F0" fontSize="6.5" fontFamily={FONTS.mono} opacity="0.8">
          m_tangente = m_secante
        </text>
      </g>
    </svg>
  </div>
);

export const S05NotesScreen: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const cardIn = springIn(frame, fps, 8, { damping: 18, stiffness: 140, mass: 0.9 });
  const draw = progress(frame, 70, 110);
  const circle = progress(frame, 110, 134);
  const arrow = progress(frame, 130, 150);
  const save = progress(frame, 168, 184);
  const toast = springIn(frame, fps, 178, { damping: 18, stiffness: 170, mass: 0.7 });
  const pulse = interpolate(frame, [162, 168, 174], [1, 0.94, 1], clamp);

  // Fade out headline cleanly before S06 enters (duration 252, S06 starts at 240)
  const headlineOpacity = interpolate(frame, [226, 238], [1, 0], clamp);

  return (
    <AbsoluteFill style={{ fontFamily: FONTS.sans }}>
      <PaperBackground mode="light" />
      <PhoneStage scale={1 + frame * 0.00005} y={500 - frame * 0.015}>
        <PhoneFrame mode="light">
          <ScreenShell theme={LIGHT} time="9:12">
            {/* Modal Bottom Sheet Drag Handle matching 05-note-editor-light.png */}
            <div
              style={{
                position: "absolute",
                top: 28,
                left: 160,
                width: 40,
                height: 4,
                borderRadius: 2,
                backgroundColor: LIGHT.border,
              }}
            />

            {/* Header row: Close, Title, Preview Eye, Guardar */}
            <div style={{ position: "absolute", top: 38, left: 16, right: 16, height: 36, display: "flex", alignItems: "center", gap: 10 }}>
              <div
                style={{
                  width: 32,
                  height: 32,
                  borderRadius: 10,
                  backgroundColor: LIGHT.surfaceContainer,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                }}
              >
                <Icon name="close" size={14} color={LIGHT.ink} strokeWidth={2.2} />
              </div>

              <div style={{ fontSize: 15, fontWeight: 700, flex: 1, letterSpacing: "-0.01em" }}>
                Editar apunte
              </div>

              <Icon name="eye" size={16} color={LIGHT.muted} />

              <div
                style={{
                  height: 32,
                  padding: "0 12px",
                  borderRadius: 16,
                  backgroundColor: LIGHT.primary,
                  color: LIGHT.onPrimary,
                  display: "flex",
                  alignItems: "center",
                  gap: 5,
                  fontSize: 9.5,
                  fontWeight: 700,
                  transform: `scale(${pulse})`,
                  position: "relative",
                  boxShadow: "0 2px 8px rgba(158,27,50,0.25)",
                }}
              >
                <CheckMark size={11} color={LIGHT.onPrimary} progress={save} strokeWidth={2.8} />
                Guardar
                <TapRipple x={65} y={16} start={162} color="#FFFFFF" maxSize={90} duration={14} />
              </div>
            </div>

            {/* Subject Selector Pills row matching real app */}
            <div style={{ position: "absolute", top: 80, left: 16, right: 16, display: "flex", gap: 7 }}>
              <div
                style={{
                  height: 26,
                  padding: "0 10px",
                  borderRadius: 13,
                  backgroundColor: "#4338CA",
                  color: "#FFFFFF",
                  display: "flex",
                  alignItems: "center",
                  gap: 5,
                  fontSize: 8.5,
                  fontWeight: 700,
                }}
              >
                <Icon name="book" size={11} color="#FFFFFF" />
                Cálculo II
              </div>

              <div
                style={{
                  height: 26,
                  padding: "0 10px",
                  borderRadius: 13,
                  backgroundColor: LIGHT.surfaceContainer,
                  color: LIGHT.ink,
                  border: `1px solid ${LIGHT.border}`,
                  display: "flex",
                  alignItems: "center",
                  gap: 5,
                  fontSize: 8.5,
                  fontWeight: 600,
                }}
              >
                <Icon name="book" size={11} color={LIGHT.muted} />
                Estadística
              </div>

              <div
                style={{
                  height: 26,
                  padding: "0 10px",
                  borderRadius: 13,
                  backgroundColor: LIGHT.surfaceContainer,
                  color: LIGHT.ink,
                  border: `1px solid ${LIGHT.border}`,
                  display: "flex",
                  alignItems: "center",
                  gap: 5,
                  fontSize: 8.5,
                  fontWeight: 600,
                }}
              >
                <Icon name="book" size={11} color={LIGHT.muted} />
                Física I
              </div>
            </div>

            {/* Title input card matching 05-note-editor-light.png */}
            <div
              style={{
                position: "absolute",
                top: 114,
                left: 16,
                right: 16,
                height: 44,
                borderRadius: 14,
                backgroundColor: LIGHT.surfaceContainer,
                border: `1px solid ${LIGHT.border}`,
                padding: "5px 12px",
                boxSizing: "border-box",
                opacity: cardIn,
              }}
            >
              <div style={{ fontSize: 7, color: LIGHT.muted, fontWeight: 500 }}>Título del apunte</div>
              <div style={{ fontSize: 11.5, fontWeight: 700, marginTop: 1, color: LIGHT.ink }}>
                Teorema de Lagrange
              </div>
            </div>

            {/* Markdown content card matching 05-note-editor-light.png */}
            <div
              style={{
                position: "absolute",
                top: 164,
                left: 16,
                right: 16,
                height: 112,
                borderRadius: 14,
                backgroundColor: LIGHT.surfaceContainer,
                border: `1px solid ${LIGHT.border}`,
                padding: "7px 12px",
                boxSizing: "border-box",
                opacity: cardIn,
                transform: `translateY(${interpolate(cardIn, [0, 1], [12, 0])}px)`,
              }}
            >
              <div style={{ fontSize: 7, color: LIGHT.muted, fontWeight: 500, marginBottom: 2 }}>
                Contenido en Markdown
              </div>
              <div style={{ fontSize: 10.5, fontWeight: 800, color: LIGHT.primary, letterSpacing: "-0.01em" }}>
                ## Teorema del valor medio
              </div>
              <div style={{ fontSize: 7.5, lineHeight: 1.3, color: LIGHT.muted, marginTop: 2 }}>
                Si f es continua en [a, b] y derivable en (a, b), existe al menos un c donde f'(c) = [f(b) − f(a)] / (b − a).
              </div>

              {/* Checklist items */}
              <div style={{ display: "flex", flexDirection: "column", gap: 3.5, marginTop: 6 }}>
                {[
                  { text: "Verificar continuidad en [a, b]", checked: true },
                  { text: "Verificar derivabilidad en (a, b)", checked: true },
                  { text: "Despejar c igualando pendiente media", checked: save > 0.5 },
                ].map((item, index) => (
                  <div key={item.text} style={{ display: "flex", alignItems: "center", gap: 5 }}>
                    <div
                      style={{
                        width: 11,
                        height: 11,
                        borderRadius: 3,
                        border: `1.2px solid ${item.checked ? LIGHT.primary : LIGHT.border}`,
                        backgroundColor: item.checked ? LIGHT.primaryContainer : LIGHT.surface,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                    >
                      {item.checked && <CheckMark size={7} color={LIGHT.primary} progress={1} strokeWidth={2.8} />}
                    </div>
                    <span style={{ fontSize: 7.2, color: item.checked ? LIGHT.ink : LIGHT.muted }}>
                      {item.text}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            {/* Foto de Pizarra / Referencia block matching real app 05-note-editor-light.png */}
            <div
              style={{
                position: "absolute",
                top: 284,
                left: 16,
                right: 16,
                borderRadius: 16,
                backgroundColor: LIGHT.surface,
                border: `1px solid ${LIGHT.border}`,
                padding: "7px 10px 9px 10px",
                boxSizing: "border-box",
                opacity: cardIn,
                transform: `translateY(${interpolate(cardIn, [0, 1], [16, 0])}px)`,
              }}
            >
              <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 5 }}>
                <span style={{ fontSize: 7.5, fontWeight: 700, color: LIGHT.ink }}>
                  Foto de Pizarra / Referencia
                </span>
                <span style={{ fontSize: 6.5, color: LIGHT.secondary, fontFamily: FONTS.mono, fontWeight: 600 }}>
                  ✓ Adjunta
                </span>
              </div>

              <ChalkboardPhoto draw={draw} circle={circle} arrow={arrow} />
            </div>

            {/* Markdown formatting toolbar matching real app footer */}
            <div
              style={{
                position: "absolute",
                left: 16,
                right: 16,
                top: 442,
                height: 34,
                borderRadius: 12,
                backgroundColor: LIGHT.surfaceContainer,
                border: `1px solid ${LIGHT.border}`,
                display: "flex",
                alignItems: "center",
                justifyContent: "space-around",
                padding: "0 6px",
                zIndex: 10,
              }}
            >
              {["B", "I", "\"", "H1", "H2", "☑", "☰", "∑"].map((tool) => (
                <div
                  key={tool}
                  style={{
                    width: 26,
                    height: 24,
                    borderRadius: 6,
                    backgroundColor: tool === "B" ? LIGHT.surface : "transparent",
                    color: tool === "B" ? LIGHT.primary : LIGHT.ink,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontSize: 9,
                    fontWeight: 700,
                  }}
                >
                  {tool}
                </div>
              ))}
            </div>

            {/* Toast notification */}
            {toast > 0 && (
              <div
                style={{
                  position: "absolute",
                  top: 486,
                  left: 0,
                  right: 0,
                  display: "flex",
                  justifyContent: "center",
                  opacity: toast,
                  transform: `translateY(${interpolate(toast, [0, 1], [10, 0])}px)`,
                  zIndex: 30,
                }}
              >
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 6,
                    height: 30,
                    padding: "0 13px",
                    borderRadius: 15,
                    backgroundColor: LIGHT.ink,
                    color: "#FFFFFF",
                    fontSize: 8.5,
                    fontWeight: 600,
                    boxShadow: "0 4px 12px rgba(0,0,0,0.2)",
                  }}
                >
                  <div
                    style={{
                      width: 13,
                      height: 13,
                      borderRadius: "50%",
                      backgroundColor: LIGHT.tertiary,
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    <CheckMark size={7.5} color="#FFFFFF" progress={1} strokeWidth={3} />
                  </div>
                  Guardado · disponible offline
                </div>
              </div>
            )}
          </ScreenShell>
        </PhoneFrame>
      </PhoneStage>

      <Headline
        lines={["Apuntes que", "no se pierden."]}
        color={LIGHT.ink}
        top={266}
        opacity={headlineOpacity}
      />
    </AbsoluteFill>
  );
};
