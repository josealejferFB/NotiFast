import React from "react";
import { FONTS, SubjectToken, ThemeTokens } from "../../tokens";
import { Icon } from "./Icon";

export const SubjectMark: React.FC<{
  subject: SubjectToken;
  size?: number;
  iconSize?: number;
  radius?: number;
}> = ({ subject, size = 36, iconSize = 18, radius = 11 }) => (
  <div
    style={{
      width: size,
      height: size,
      borderRadius: radius,
      backgroundColor: subject.soft,
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      flexShrink: 0,
    }}
  >
    <Icon name="book" size={iconSize} color={subject.color} strokeWidth={1.9} filled />
  </div>
);

export const SubjectLine: React.FC<{
  theme: ThemeTokens;
  subject: SubjectToken;
  compact?: boolean;
  showAverage?: boolean;
  style?: React.CSSProperties;
}> = ({ theme, subject, compact, showAverage, style }) => (
  <div style={{ display: "flex", alignItems: "center", gap: 8, ...style }}>
    <span
      style={{
        width: 6,
        height: 6,
        borderRadius: "50%",
        backgroundColor: subject.color,
        flexShrink: 0,
      }}
    />
    <span style={{ fontSize: compact ? 9 : 11, fontWeight: 600, color: theme.ink }}>
      {subject.name}
    </span>
    {showAverage ? (
      <span
        style={{
          marginLeft: "auto",
          fontFamily: FONTS.mono,
          fontSize: compact ? 9 : 12,
          fontWeight: 600,
          color: theme.tertiary,
        }}
      >
        {subject.average}
      </span>
    ) : null}
  </div>
);
