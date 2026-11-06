import React from "react";
import { FONTS, ThemeTokens } from "../../tokens";
import { Icon } from "./Icon";

interface ScreenShellProps {
  theme: ThemeTokens;
  children: React.ReactNode;
  time?: string;
  showStatusBar?: boolean;
  showHomeIndicator?: boolean;
  style?: React.CSSProperties;
}

export const StatusBar: React.FC<{
  theme: ThemeTokens;
  time?: string;
}> = ({ theme, time = "9:06" }) => (
  <div
    style={{
      position: "absolute",
      top: 0,
      left: 0,
      right: 0,
      height: 28,
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
      padding: "0 18px",
      color: theme.ink,
      zIndex: 30,
    }}
  >
    <span style={{ fontFamily: FONTS.sans, fontSize: 10.5, fontWeight: 600, letterSpacing: "0.01em" }}>
      {time}
    </span>
    <span style={{ display: "flex", alignItems: "center", gap: 4, opacity: 0.9 }}>
      <Icon name="signal" size={11} color={theme.ink} strokeWidth={1.8} />
      <Icon name="wifi" size={12} color={theme.ink} strokeWidth={1.8} />
      <Icon name="battery" size={15} color={theme.ink} strokeWidth={1.5} />
    </span>
  </div>
);

export const ScreenShell: React.FC<ScreenShellProps> = ({
  theme,
  children,
  time = "9:06",
  showStatusBar = true,
  showHomeIndicator = true,
  style,
}) => (
  <div
    style={{
      width: 360,
      height: 780,
      position: "relative",
      overflow: "hidden",
      backgroundColor: theme.background,
      color: theme.ink,
      fontFamily: FONTS.sans,
      ...style,
    }}
  >
    {showStatusBar ? <StatusBar theme={theme} time={time} /> : null}
    {children}
    {showHomeIndicator ? (
      <div
        style={{
          position: "absolute",
          bottom: 8,
          left: 130,
          width: 100,
          height: 4,
          borderRadius: 2,
          backgroundColor: theme.ink,
          opacity: 0.22,
          zIndex: 40,
        }}
      />
    ) : null}
  </div>
);
