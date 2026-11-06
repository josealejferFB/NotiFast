import { interpolate, spring } from "remotion";
import { EASE, SPRINGS } from "./tokens";

export const clamp = {
  extrapolateLeft: "clamp" as const,
  extrapolateRight: "clamp" as const,
};

export const fadeIn = (frame: number, delay = 0, duration = 12): number =>
  interpolate(frame, [delay, delay + duration], [0, 1], {
    ...clamp,
    easing: EASE,
  });

export const fadeOut = (frame: number, start: number, duration = 12): number =>
  interpolate(frame, [start, start + duration], [1, 0], {
    ...clamp,
    easing: EASE,
  });

export const progress = (
  frame: number,
  start: number,
  end: number,
): number =>
  interpolate(frame, [start, end], [0, 1], {
    ...clamp,
    easing: EASE,
  });

export const springIn = (
  frame: number,
  fps: number,
  delay = 0,
  config: { damping: number; stiffness: number; mass: number } = SPRINGS.card,
): number =>
  spring({
    frame: frame - delay,
    fps,
    config,
    durationInFrames: 34,
  });

export const say = (frame: number, start: number, end: number): boolean =>
  frame >= start && frame <= end;

export const mix = (frame: number, start: number, end: number) =>
  interpolate(frame, [start, end], [0, 1], clamp);

export const pad2 = (value: number): string =>
  value < 10 ? `0${value}` : `${value}`;

export const lerp = (a: number, b: number, t: number): number =>
  a + (b - a) * t;
