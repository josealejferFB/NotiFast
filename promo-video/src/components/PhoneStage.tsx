import React from "react";
import { PHONE } from "../tokens";

interface PhoneStageProps {
  children: React.ReactNode;
  x?: number;
  y?: number;
  scale?: number;
  rotate?: number;
  opacity?: number;
  style?: React.CSSProperties;
}

export const PhoneStage: React.FC<PhoneStageProps> = ({
  children,
  x = PHONE.x,
  y = PHONE.y,
  scale = 1,
  rotate = 0,
  opacity = 1,
  style,
}) => {
  return (
    <div
      style={{
        position: "absolute",
        left: x,
        top: y,
        width: PHONE.width,
        height: PHONE.height,
        transform: `scale(${scale}) rotate(${rotate}deg)`,
        transformOrigin: "center top",
        opacity,
        ...style,
      }}
    >
      {children}
    </div>
  );
};
