import React from "react";
import { FONTS, ThemeTokens } from "../../tokens";
import { Icon, IconName } from "./Icon";

export const Avatar: React.FC<{ theme: ThemeTokens; label?: string; size?: number }> = ({
  theme,
  label = "V",
  size = 34,
}) => (
  <div
    style={{
      width: size,
      height: size,
      borderRadius: "50%",
      backgroundColor: theme.primaryContainer,
      border: `1px solid ${theme.primary}33`,
      color: theme.primary,
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      fontFamily: FONTS.sans,
      fontSize: size * 0.4,
      fontWeight: 700,
      flexShrink: 0,
    }}
  >
    {label}
  </div>
);

export const IconButton: React.FC<{
  theme: ThemeTokens;
  name: IconName;
  size?: number;
  box?: number;
  color?: string;
}> = ({ theme, name, size = 15, box = 28, color }) => (
  <div
    style={{
      width: box,
      height: box,
      borderRadius: "50%",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      backgroundColor: theme.surfaceContainer,
      color: color ?? theme.muted,
      flexShrink: 0,
    }}
  >
    <Icon name={name} size={size} color={color ?? theme.muted} />
  </div>
);

export const Card: React.FC<{
  theme: ThemeTokens;
  children: React.ReactNode;
  style?: React.CSSProperties;
  padding?: number;
  radius?: number;
  tone?: string;
  borderColor?: string;
}> = ({ theme, children, style, padding = 14, radius = 20, tone, borderColor }) => (
  <div
    style={{
      backgroundColor: tone ?? theme.surface,
      border: `1px solid ${borderColor ?? theme.border}`,
      borderRadius: radius,
      padding,
      boxSizing: "border-box",
      ...style,
    }}
  >
    {children}
  </div>
);

export type ChipTone = "neutral" | "primary" | "secondary" | "tertiary" | "dark";

export const Chip: React.FC<{
  theme: ThemeTokens;
  label: string;
  tone?: ChipTone;
  size?: "xs" | "sm" | "md";
  icon?: IconName;
  style?: React.CSSProperties;
}> = ({ theme, label, tone = "neutral", size = "sm", icon, style }) => {
  const palette: Record<ChipTone, { bg: string; fg: string; border: string }> = {
    neutral: { bg: theme.surfaceContainer, fg: theme.muted, border: theme.border },
    primary: { bg: theme.primaryContainer, fg: theme.primary, border: `${theme.primary}44` },
    secondary: { bg: theme.secondaryContainer, fg: theme.secondary, border: `${theme.secondary}44` },
    tertiary: { bg: theme.tertiaryContainer, fg: theme.tertiary, border: `${theme.tertiary}44` },
    dark: { bg: theme.surfaceContainerHigh, fg: theme.ink, border: theme.border },
  };
  const p = palette[tone];
  const fontSize = size === "xs" ? 7.5 : size === "sm" ? 8.5 : 10;
  const height = size === "xs" ? 16 : size === "sm" ? 20 : 24;
  return (
    <div
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: 4,
        height,
        padding: size === "xs" ? "0 6px" : "0 8px",
        borderRadius: height / 2,
        backgroundColor: p.bg,
        color: p.fg,
        border: `1px solid ${p.border}`,
        fontFamily: FONTS.sans,
        fontSize,
        fontWeight: 600,
        letterSpacing: size === "xs" ? "0.04em" : "0.01em",
        textTransform: size === "xs" ? "uppercase" : "none",
        whiteSpace: "nowrap",
        ...style,
      }}
    >
      {icon ? <Icon name={icon} size={fontSize + 3} color={p.fg} strokeWidth={2} /> : null}
      {label}
    </div>
  );
};

export const PrimaryButton: React.FC<{
  theme: ThemeTokens;
  label: string;
  icon?: IconName;
  height?: number;
  fontSize?: number;
  style?: React.CSSProperties;
  tone?: string;
  color?: string;
  borderColor?: string;
}> = ({
  theme,
  label,
  icon,
  height = 44,
  fontSize = 12,
  style,
  tone,
  color,
  borderColor,
}) => (
  <div
    style={{
      height,
      borderRadius: 12,
      backgroundColor: tone ?? theme.primary,
      color: color ?? theme.onPrimary,
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      gap: 7,
      fontFamily: FONTS.sans,
      fontSize,
      fontWeight: 700,
      border: borderColor ? `1px solid ${borderColor}` : "none",
      boxSizing: "border-box",
      ...style,
    }}
  >
    {icon ? <Icon name={icon} size={fontSize + 4} color={color ?? theme.onPrimary} strokeWidth={2} /> : null}
    {label}
  </div>
);

export const ScreenHeader: React.FC<{
  theme: ThemeTokens;
  title: string;
  subtitle?: string;
  leading?: "avatar" | "back" | "close" | "none";
  right?: React.ReactNode;
  top?: number;
  onLeadingTap?: number;
}> = ({ theme, title, subtitle, leading = "avatar", right, top = 36 }) => (
  <div
    style={{
      position: "absolute",
      top,
      left: 16,
      right: 16,
      height: 44,
      display: "flex",
      alignItems: "center",
      gap: 9,
    }}
  >
    {leading === "avatar" ? <Avatar theme={theme} /> : null}
    {leading === "back" ? <Icon name="back" size={18} color={theme.ink} strokeWidth={2} /> : null}
    {leading === "close" ? <Icon name="close" size={16} color={theme.ink} strokeWidth={2} /> : null}
    <div style={{ display: "flex", flexDirection: "column", gap: 2, flex: 1, minWidth: 0 }}>
      <span style={{ fontSize: 15, fontWeight: 700, letterSpacing: "-0.01em", lineHeight: 1.1 }}>
        {title}
      </span>
      {subtitle ? (
        <span style={{ fontSize: 8.5, color: theme.muted, fontFamily: FONTS.mono, letterSpacing: "0.02em" }}>
          {subtitle}
        </span>
      ) : null}
    </div>
    <div style={{ display: "flex", alignItems: "center", gap: 6 }}>{right}</div>
  </div>
);

export const Divider: React.FC<{ theme: ThemeTokens; style?: React.CSSProperties }> = ({
  theme,
  style,
}) => <div style={{ height: 1, backgroundColor: theme.border, width: "100%", ...style }} />;

export const Field: React.FC<{
  theme: ThemeTokens;
  label: string;
  value: string;
  mono?: boolean;
  style?: React.CSSProperties;
  accent?: boolean;
}> = ({ theme, label, value, mono, style, accent }) => (
  <div
    style={{
      position: "relative",
      border: `1px solid ${accent ? theme.primary : theme.border}`,
      borderRadius: 12,
      height: 44,
      padding: "8px 11px 0",
      boxSizing: "border-box",
      backgroundColor: theme.surfaceContainer,
      ...style,
    }}
  >
    <div style={{ fontSize: 7.5, color: theme.muted, letterSpacing: "0.02em" }}>{label}</div>
    <div
      style={{
        fontSize: 10.5,
        fontWeight: 600,
        color: theme.ink,
        marginTop: 2,
        fontFamily: mono ? FONTS.mono : FONTS.sans,
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
      }}
    >
      {value}
    </div>
  </div>
);
