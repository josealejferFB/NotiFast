# NotiFast — Video Promocional Oficial (Vertical)

Video promocional vertical (9:16) de alta fidelidad para **NotiFast**, diseñado para estudiantes universitarios (especialmente de la **UNERG**) y optimizado para consumo sin audio en TikTok, Instagram Reels y YouTube Shorts.

---

## 📊 Especificaciones Técnicas

| Parámetro | Especificación |
|---|---|
| **Resolución** | 1080 × 1920 px (Vertical 9:16) |
| **Frame Rate** | 30 fps |
| **Duración** | 1740 fotogramas = exactamente 58.0 s |
| **Audio** | Cero streams de audio (`-an`, verificado sin pista silenciosa) |
| **Codec / Formato** | H.264 / MP4 (`yuv420p`, CRF 18) |
| **Metodología** | 100 % animación programada en React + Remotion (cero grabaciones de pantalla, cero footage de stock, cero IA generativa) |
| **Zonas Seguras** | Top 220 px, Bottom 340 px, Left 96 px, Right 120 px |

---

## 🎯 Estructura Narrativa (9 Escenas en Toma Continua)

| Escena | Tiempos | Copy Exterior | Concepto Visual |
|---|---|---|---|
| **S01 · Hook DACE** | 0:00 – 0:02.5 (75 f) | *"Tu horario DACE, por fin en orden."* | Documento PDF DACE UNERG 2026-02 cayendo con spring hacia el mockup del dispositivo |
| **S02 · Importar DACE** | 0:02.5 – 0:09.5 (210 f) | *"Sube tu PDF del DACE."* ➔ *"Todo se ordena."* | Bottom sheet con zona punteada, láser de escaneo, chip 100% Offline, carga de 4 materias y botón carmesí con tap ripple |
| **S03 · Horario Semanal** | 0:09.5 – 0:17.5 (240 f) | *"Tu semana, clara."* | Grilla semanal Lun–Vie interactiva con bloques por materia, línea de tiempo 'Hoy Miércoles' a las 08:00 y tarjetas de asignaturas |
| **S04 · Notificación** | 0:17.5 – 0:25.0 (225 f) | *"La clase ya empezó."* ➔ *"Tú ya lo sabes."* | Lock screen nocturna, tarjeta de notificación en curso (Cálculo Aula 204), progreso circular y apertura de vista 'Hoy' |
| **S05 · Apuntes** | 0:25.0 – 0:33.0 (240 f) | *"Apuntes que no se pierden."* | Editor por bloques: fórmula matemática de derivadas renderizada con wipe, checklist y pizarra ilustrada en SVG |
| **S06 · WhatsApp Profe** | 0:33.0 – 0:40.0 (210 f) | *"Tu profe, a un toque."* | Directorio docente (Prof. Luis Rivas), apertura directa de chat simulado, tipeo reactivo y envío con doble check |
| **S07 · Portadas UNERG** | 0:40.0 – 0:47.0 (210 f) | *"Portadas listas para entregar."* | Selector abanicado de plantillas APA 7, IEEE y UNERG oficial con membrete y sello institucional de grado |
| **S08 · Notas & Promedio** | 0:47.0 – 0:53.0 (180 f) | *"Tu nota, clara."* | Cálculo de promedio ponderado (17.5/20, 87.5%), desglose de 3 parciales y badge 'Aprobado' |
| **S09 · CTA Final** | 0:53.0 – 0:58.0 (150 f) | *"NotiFast. Todo tu semestre, en orden."* | Logo principal con aura, píldoras 'Gratis / Android / 100% offline', botón de descarga, URL `notifast.pages.dev` y hold estático |

---

## 🛠️ Comandos de Ejecución

### 1. Iniciar Remotion Studio (Vista Previa Interactiva)
```bash
npm run start
# Abre http://localhost:3000 con scrubbing en tiempo real
```

### 2. Renderizar Video Completo (1080×1920)
```bash
npm run render
# Produce out/notifast-promo-1080x1920.mp4 con H.264 CRF 18
```

### 3. Extraer Fotogramas Clave (Stills)
```bash
npm run still
# Genera el fotograma 0 en out/poster.jpg
```

### 4. Verificación de Tipos TypeScript
```bash
npm run typecheck
```

---

## 📁 Estructura del Proyecto

```text
promo-video/
├── package.json
├── remotion.config.ts
├── tsconfig.json
├── public/
│   ├── brand/
│   │   ├── notifast-logo.png
│   │   ├── unerg-ais.png
│   │   └── unerg-banner.png
│   └── fonts/
│       ├── PlusJakartaSans-*.ttf
│       └── JetBrainsMono-*.ttf
├── src/
│   ├── index.ts
│   ├── Root.tsx
│   ├── Video.tsx
│   ├── tokens.ts
│   ├── storyboard.ts
│   ├── utils.ts
│   ├── components/
│   │   ├── DacePdf.tsx
│   │   ├── Headline.tsx
│   │   ├── PaperBackground.tsx
│   │   ├── PhoneFrame.tsx
│   │   ├── PhoneStage.tsx
│   │   ├── TapRipple.tsx
│   │   └── ui/
│   │       ├── Atoms.tsx
│   │       ├── BottomNav.tsx
│   │       ├── Icon.tsx
│   │       ├── ProgressRing.tsx
│   │       ├── Screen.tsx
│   │       └── SubjectMark.tsx
│   └── scenes/
│       ├── S01Hook.tsx
│       ├── S02DaceImportScreen.tsx
│       ├── S03ScheduleScreen.tsx
│       ├── S04NotificationsScreen.tsx
│       ├── S05NotesScreen.tsx
│       ├── S06TeachersScreen.tsx
│       ├── S07CoversScreen.tsx
│       ├── S08GradesScreen.tsx
│       └── S09Cta.tsx
├── out/
│   ├── notifast-promo-1080x1920.mp4
│   ├── poster.jpg
│   ├── contact-sheet.jpg
│   └── qa/
│       └── frame-*.jpg
├── README.md
└── QA.md
```

---

## 🎨 Activos y Créditos

- **Tipografía:**
  - *Plus Jakarta Sans* (Google Fonts / SIL Open Font License) — UI y titulares.
  - *JetBrains Mono* (JetBrains / Apache 2.0) — Horas, notas, código, porcentajes y badges.
- **Identidad de Marca:**
  - Logo oficial NotiFast (`landing/assets/logo.png`).
  - Escudos institucionales UNERG AIS (`app/src/main/res/drawable/`).
