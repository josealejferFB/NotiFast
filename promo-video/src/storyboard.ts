export interface SceneMeta {
  id: string;
  title: string;
  from: number;
  duration: number;
  copy: string[];
  theme: "light" | "dark";
}

/**
 * Timeline maestro. Los primeros 8 segmentos incluyen 12 frames extra para
 * el crossfade de TransitionSeries; el total exacto es 1740 frames (58 s).
 */
export const SCENES: SceneMeta[] = [
  {
    id: "S01",
    title: "Hook DACE",
    from: 0,
    duration: 87,
    copy: ["Tu horario DACE,", "por fin en orden."],
    theme: "light",
  },
  {
    id: "S02",
    title: "Importar DACE",
    from: 75,
    duration: 222,
    copy: ["Sube tu PDF del DACE.", "Todo se ordena."],
    theme: "light",
  },
  {
    id: "S03",
    title: "Horario y materias",
    from: 285,
    duration: 252,
    copy: ["Tu semana, clara."],
    theme: "light",
  },
  {
    id: "S04",
    title: "Notificación en curso",
    from: 525,
    duration: 237,
    copy: ["La clase ya empezó.", "Tú ya lo sabes."],
    theme: "dark",
  },
  {
    id: "S05",
    title: "Apuntes",
    from: 750,
    duration: 252,
    copy: ["Apuntes que no se pierden."],
    theme: "light",
  },
  {
    id: "S06",
    title: "Profesor + WhatsApp",
    from: 990,
    duration: 222,
    copy: ["Tu profe, a un toque."],
    theme: "light",
  },
  {
    id: "S07",
    title: "Portadas",
    from: 1200,
    duration: 222,
    copy: ["Portadas listas para entregar."],
    theme: "light",
  },
  {
    id: "S08",
    title: "Nota",
    from: 1410,
    duration: 192,
    copy: ["Tu nota, clara."],
    theme: "light",
  },
  {
    id: "S09",
    title: "CTA",
    from: 1590,
    duration: 150,
    copy: [
      "NotiFast.",
      "Todo tu semestre, en orden.",
      "Descargar gratis",
      "Android · 100 % offline",
    ],
    theme: "light",
  },
];

export const TRANSITION_FRAMES = 12;
export const TOTAL_FRAMES = 1740;
export const FPS = 30;
