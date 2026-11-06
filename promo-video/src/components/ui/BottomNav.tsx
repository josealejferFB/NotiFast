import React from "react";
import { ThemeTokens } from "../../tokens";
import { Icon, IconName } from "./Icon";

const ITEMS: { name: IconName; label: string }[] = [
  { name: "book", label: "Apuntes" },
  { name: "clock", label: "Horario" },
  { name: "document", label: "Estudio" },
  { name: "star", label: "Notas" },
  { name: "settings", label: "Ajustes" },
];

export const BottomNav: React.FC<{
  theme: ThemeTokens;
  active: number;
  top?: number;
  showFab?: boolean;
}> = ({ theme, active, top = 652, showFab = false }) => (
  <>
    {/* Floating Action Button (FAB) matching real NotiFast app */}
    {showFab && (
      <div
        style={{
          position: "absolute",
          right: 20,
          top: top - 32,
          width: 50,
          height: 50,
          borderRadius: "50%",
          backgroundColor: theme.primary,
          boxShadow: "0 6px 16px rgba(158,27,50,0.32)",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          zIndex: 14,
        }}
      >
        <Icon name="plus" size={20} color="#FFFFFF" strokeWidth={2.4} />
      </div>
    )}

    {/* Floating Bottom Navigation Pill Dock */}
    <div
      style={{
        position: "absolute",
        left: 56,
        top,
        width: 248,
        height: 48,
        borderRadius: 24,
        backgroundColor: theme.surface,
        border: `1.2px solid ${theme.border}`,
        boxShadow: "0 6px 20px rgba(24,26,31,0.08)",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-evenly",
        zIndex: 12,
        padding: "0 4px",
        boxSizing: "border-box",
      }}
    >
      {ITEMS.map((item, index) => {
        const isActive = index === active;
        return (
          <div
            key={item.name}
            style={{
              width: isActive ? 42 : 36,
              height: 32,
              borderRadius: 16,
              backgroundColor: isActive ? theme.primaryContainer : "transparent",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              transition: "all 0.2s ease",
            }}
          >
            <Icon
              name={item.name}
              size={isActive ? 16 : 15}
              color={isActive ? theme.primary : theme.muted}
              strokeWidth={isActive ? 2.2 : 1.7}
              filled={isActive && item.name === "star"}
            />
          </div>
        );
      })}
    </div>
  </>
);
