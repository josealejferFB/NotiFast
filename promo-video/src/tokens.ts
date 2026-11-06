import { Easing } from "remotion";

export const CANVAS = {
  width: 1080,
  height: 1920,
  fps: 30,
  durationInFrames: 1740,
} as const;

export const PHONE = {
  width: 760,
  height: 1600,
  screenWidth: 720,
  screenHeight: 1560,
  logicalWidth: 360,
  logicalHeight: 780,
  contentScale: 2,
  x: 160,
  y: 500,
} as const;

export interface ThemeTokens {
  mode: "light" | "dark";
  background: string;
  surface: string;
  surfaceContainer: string;
  surfaceContainerHigh: string;
  border: string;
  primary: string;
  onPrimary: string;
  primaryContainer: string;
  secondary: string;
  secondaryContainer: string;
  tertiary: string;
  tertiaryContainer: string;
  error: string;
  ink: string;
  muted: string;
  faint: string;
}

export const LIGHT: ThemeTokens = {
  mode: "light",
  background: "#FAF9F6",
  surface: "#FFFFFF",
  surfaceContainer: "#F3EFEA",
  surfaceContainerHigh: "#EBE6DF",
  border: "#E5DFD7",
  primary: "#9E1B32",
  onPrimary: "#FFFFFF",
  primaryContainer: "#FDE8EB",
  secondary: "#2A7B7B",
  secondaryContainer: "#E3F2F1",
  tertiary: "#15803D",
  tertiaryContainer: "#E4F5EA",
  error: "#B91C1C",
  ink: "#181A1F",
  muted: "#4B5563",
  faint: "#8B929E",
};

export const DARK: ThemeTokens = {
  mode: "dark",
  background: "#0E131F",
  surface: "#161E2D",
  surfaceContainer: "#1C2638",
  surfaceContainerHigh: "#233049",
  border: "#2A384E",
  primary: "#E05A6E",
  onPrimary: "#0E131F",
  primaryContainer: "#3A1B25",
  secondary: "#38B2AC",
  secondaryContainer: "#12302F",
  tertiary: "#34D399",
  tertiaryContainer: "#0E2A22",
  error: "#F87171",
  ink: "#F3F5F9",
  muted: "#A6B2C5",
  faint: "#76839A",
};

export const themeFor = (mode: "light" | "dark"): ThemeTokens =>
  mode === "dark" ? DARK : LIGHT;

export interface SubjectToken {
  id: string;
  name: string;
  short: string;
  color: string;
  soft: string;
  section: string;
  room: string;
  time: string;
  days: string;
  teacher: string;
  average: string;
}

export const SUBJECTS: Record<string, SubjectToken> = {
  calculo: {
    id: "calculo",
    name: "Cálculo Diferencial",
    short: "Cálculo",
    color: "#9E1B32",
    soft: "#FDE8EB",
    section: "Sec. 01",
    room: "Aula 204",
    time: "08:00 – 09:30",
    days: "Lun · Mié",
    teacher: "Prof. Luis Rivas",
    average: "17.5",
  },
  fisica: {
    id: "fisica",
    name: "Física General",
    short: "Física",
    color: "#2A7B7B",
    soft: "#E3F2F1",
    section: "Sec. 02",
    room: "Lab 3",
    time: "10:00 – 11:30",
    days: "Mar · Jue",
    teacher: "Prof. Ana Méndez",
    average: "16.0",
  },
  programacion: {
    id: "programacion",
    name: "Programación I",
    short: "Programación",
    color: "#4338CA",
    soft: "#E9E7FB",
    section: "Sec. 01",
    room: "Lab 2",
    time: "14:00 – 15:30",
    days: "Lun · Mié",
    teacher: "Prof. Carlos Pérez",
    average: "18.6",
  },
  ingles: {
    id: "ingles",
    name: "Inglés Instrumental",
    short: "Inglés",
    color: "#C2410C",
    soft: "#FBE9DD",
    section: "Sec. 03",
    room: "Aula 12",
    time: "09:00 – 10:30",
    days: "Vie",
    teacher: "Prof. María Gómez",
    average: "17.0",
  },
};

export const FONTS = {
  sans: '"Plus Jakarta Sans", sans-serif',
  mono: '"JetBrains Mono", monospace',
} as const;

export const SPRINGS = {
  card: { damping: 18, stiffness: 140, mass: 0.9 },
  text: { damping: 20, stiffness: 180, mass: 0.8 },
  camera: { damping: 22, stiffness: 90, mass: 1 },
  logo: { damping: 16, stiffness: 120, mass: 0.9 },
} as const;

export const EASE = Easing.bezier(0.22, 1, 0.36, 1);

export const COPY = {
  hookLine1: "Tu horario DACE,",
  hookLine2: "por fin en orden.",
  importBefore: "Sube tu PDF del DACE.",
  importAfter: "Todo se ordena.",
  schedule: "Tu semana, clara.",
  notificationBefore: "La clase ya empezó.",
  notificationAfter: "Tú ya lo sabes.",
  notes: "Apuntes que no se pierden.",
  teacher: "Tu profe, a un toque.",
  covers: "Portadas listas para entregar.",
  grades: "Tu nota, clara.",
  ctaBrand: "NotiFast.",
  ctaTagline: "Todo tu semestre, en orden.",
  ctaButton: "Descargar gratis",
  ctaUrl: "notifast.pages.dev",
} as const;

export const SCENE_COPY: Record<string, { line1: string; line2?: string }> = {
  S01: { line1: COPY.hookLine1, line2: COPY.hookLine2 },
  S02: { line1: "Sube tu PDF", line2: "del DACE." },
  S03: { line1: "Tu semana,", line2: "clara." },
  S04: { line1: "La clase ya", line2: "empezó." },
  S05: { line1: "Apuntes que", line2: "no se pierden." },
  S06: { line1: "Tu profe,", line2: "a un toque." },
  S07: { line1: "Portadas listas", line2: "para entregar." },
  S08: { line1: "Tu nota,", line2: "clara." },
};

export const HEADLINE_TOP = 266;
