import React from "react";
import { PHONE, themeFor } from "../tokens";

interface PhoneFrameProps {
  children: React.ReactNode;
  mode?: "light" | "dark";
  showStatus?: boolean;
}

/**
 * Marco fisico de 760 x 1600 con pantalla de 720 x 1560.
 * La app se dibuja en un lienzo logico de 360 x 780 escalado 2x.
 */
export const PhoneFrame: React.FC<PhoneFrameProps> = ({
  children,
  mode = "light",
}) => {
  const theme = themeFor(mode);

  return (
    <div
      style={{
        width: PHONE.width,
        height: PHONE.height,
        borderRadius: 56,
        backgroundColor: "#0B0F17",
        border: "2px solid #1F2937",
        boxSizing: "border-box",
        position: "relative",
        boxShadow: "0 18px 24px rgba(24,26,31,0.10)",
      }}
    >
      <div
        style={{
          position: "absolute",
          left: 20,
          top: 20,
          width: PHONE.screenWidth,
          height: PHONE.screenHeight,
          borderRadius: 44,
          overflow: "hidden",
          backgroundColor: theme.background,
        }}
      >
        <div style={{ width: 720, height: 1560, overflow: "hidden", position: "relative" }}>
          <div
            style={{
              width: PHONE.logicalWidth,
              height: PHONE.logicalHeight,
              transform: `scale(${PHONE.contentScale})`,
              transformOrigin: "top left",
              position: "absolute",
              left: 0,
              top: 0,
              overflow: "hidden",
              backgroundColor: theme.background,
            }}
          >
            {children}
          </div>
        </div>
      </div>

      <div
        style={{
          position: "absolute",
          left: 380 - 32,
          top: 7,
          width: 64,
          height: 6,
          borderRadius: 3,
          backgroundColor: "#1F2937",
        }}
      />
      <div
        style={{
          position: "absolute",
          left: 380 - 4,
          top: 4,
          width: 8,
          height: 8,
          borderRadius: "50%",
          backgroundColor: "#0B0F17",
          border: "1px solid #243044",
        }}
      />

      <div
        style={{
          position: "absolute",
          left: -4,
          top: 260,
          width: 4,
          height: 62,
          borderRadius: "2px 0 0 2px",
          backgroundColor: "#1F2937",
        }}
      />
      <div
        style={{
          position: "absolute",
          left: -4,
          top: 342,
          width: 4,
          height: 96,
          borderRadius: "2px 0 0 2px",
          backgroundColor: "#1F2937",
        }}
      />
      <div
        style={{
          position: "absolute",
          right: -4,
          top: 318,
          width: 4,
          height: 132,
          borderRadius: "0 2px 2px 0",
          backgroundColor: "#1F2937",
        }}
      />
    </div>
  );
};
