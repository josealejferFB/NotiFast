import React from "react";
import { staticFile } from "remotion";
import { FONTS, SUBJECTS } from "../tokens";

interface DacePdfProps {
  width?: number;
  scale?: number;
  style?: React.CSSProperties;
}

const rowIds = ["calculo", "fisica", "programacion", "ingles"];

/**
 * Representacion 100% CSS/SVG del PDF del DACE.
 * Escala 1 = 430 x 540 px, pensada para la escena S01.
 */
export const DacePdf: React.FC<DacePdfProps> = ({ width = 430, scale, style }) => {
  const height = (width / 430) * 540;
  return (
    <div
      style={{
        width,
        height,
        backgroundColor: "#FFFFFF",
        border: "1px solid #E5DFD7",
        borderRadius: 14,
        padding: 24,
        boxSizing: "border-box",
        position: "relative",
        overflow: "hidden",
        boxShadow: "0 12px 24px rgba(24,26,31,0.08)",
        transform: scale ? `scale(${scale})` : undefined,
        transformOrigin: "top left",
        ...style,
      }}
    >
      <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
        <img
          src={staticFile("brand/unerg-ais.png")}
          alt=""
          style={{ width: 34, height: 34, objectFit: "contain", display: "block" }}
        />
        <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <span
            style={{
              fontFamily: FONTS.mono,
              fontSize: 11,
              fontWeight: 700,
              letterSpacing: "0.12em",
              color: "#181A1F",
            }}
          >
            UNERG
          </span>
          <span style={{ fontFamily: FONTS.sans, fontSize: 6.5, color: "#4B5563", letterSpacing: "0.01em" }}>
            Universidad Nacional Experimental
          </span>
        </div>
        <span
          style={{
            marginLeft: "auto",
            fontFamily: FONTS.mono,
            fontSize: 7,
            color: "#8B929E",
            letterSpacing: "0.1em",
          }}
        >
          DACE
        </span>
      </div>

      <div style={{ marginTop: 18, display: "flex", alignItems: "flex-end", justifyContent: "space-between" }}>
        <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
          <span
            style={{
              fontFamily: FONTS.mono,
              fontSize: 42,
              lineHeight: 0.9,
              fontWeight: 700,
              letterSpacing: "-0.04em",
              color: "#9E1B32",
            }}
          >
            DACE
          </span>
          <span style={{ fontFamily: FONTS.sans, fontSize: 8, color: "#4B5563" }}>
            Horario academico
          </span>
        </div>
        <span
          style={{
            fontFamily: FONTS.mono,
            fontSize: 13,
            fontWeight: 600,
            color: "#181A1F",
            letterSpacing: "0.04em",
          }}
        >
          2026-02
        </span>
      </div>

      <div style={{ height: 2, backgroundColor: "#9E1B32", marginTop: 12, marginBottom: 6 }} />

      {rowIds.map((id, index) => {
        const subject = SUBJECTS[id];
        return (
          <div
            key={id}
            style={{
              display: "flex",
              alignItems: "center",
              gap: 10,
              padding: "8px 0",
              borderBottom: index < rowIds.length - 1 ? "1px solid #F1ECE5" : "none",
            }}
          >
            <span
              style={{
                width: 3,
                height: 30,
                borderRadius: 2,
                backgroundColor: subject.color,
                flexShrink: 0,
              }}
            />
            <div style={{ display: "flex", flexDirection: "column", gap: 2, flex: 1, minWidth: 0 }}>
              <span style={{ fontFamily: FONTS.sans, fontSize: 10.5, fontWeight: 600, color: "#181A1F" }}>
                {subject.name}
              </span>
              <span
                style={{
                  fontFamily: FONTS.mono,
                  fontSize: 7.5,
                  color: "#4B5563",
                  whiteSpace: "nowrap",
                }}
              >
                {subject.days} · {subject.time} · {subject.room}
              </span>
            </div>
            <span
              style={{
                fontFamily: FONTS.mono,
                fontSize: 7,
                color: subject.color,
                fontWeight: 600,
                letterSpacing: "0.04em",
              }}
            >
              {subject.section.replace("Sec. ", "S")}
            </span>
          </div>
        );
      })}

      <div
        style={{
          marginTop: 10,
          paddingTop: 8,
          borderTop: "1px solid #E5DFD7",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <span style={{ fontFamily: FONTS.mono, fontSize: 7.5, color: "#4B5563" }}>
          4 materias · 12 clases
        </span>
        <span
          style={{
            fontFamily: FONTS.mono,
            fontSize: 6.5,
            color: "#9E1B32",
            border: "1px solid #9E1B3244",
            borderRadius: 20,
            padding: "2px 7px",
            letterSpacing: "0.1em",
          }}
        >
          PDF · DACE
        </span>
      </div>

      <div
        style={{
          position: "absolute",
          right: -30,
          bottom: 60,
          fontFamily: FONTS.mono,
          fontSize: 90,
          fontWeight: 700,
          color: "#9E1B32",
          opacity: 0.035,
          transform: "rotate(-12deg)",
          letterSpacing: "-0.05em",
          pointerEvents: "none",
        }}
      >
        UNERG
      </div>
    </div>
  );
};
