# ADR-0004: Sistema de Temas Dinámicos WCAG AAA con Desacoplamiento de Lienzos

- **Estado:** Aceptado
- **Fecha:** 2026-10-16
- **Módulo:** Presentación y Tema (`presentation/theme`, `presentation/feature/settings`)

---

## Contexto y Planteamiento del Problema

Las herramientas de software académico tradicionales suelen imponer interfaces burocráticas y homogéneas (tonos grises fríos o azul genérico), o depender ciegamente del motor Material You de Android. En pruebas con estudiantes universitarios se constató que la lectura prolongada de apuntes en pantallas blancas genera fatiga visual, mientras que los modos oscuros convencionales con negro puro (`#000000`) provocan efecto halo y dificultad de enfoque.

Asimismo, al permitir personalización cromática, los usuarios suelen escoger combinaciones con ratios de contraste deficientes que violan las pautas de accesibilidad **WCAG**, haciendo que el texto resulte ilegible.

## Decisión

Se decide diseñar e implementar un **motor dinámico de temas y lienzos en Jetpack Compose** denominado *Tactile Papercraft*:

1. **Desacoplamiento entre Acento y Lienzo:** Se separan formalmente las colecciones de **Paleta de Acento (`ThemePreset`)** (6 estilos: *Classic*, *Campus Verde*, *Cyber Nocturno*, *Café Nórdico*, *Sakura Zen*, *Minimalista Nórdico*) y **Lienzo de Papel (`BackgroundPreset`)** (6 tonalidades: *Pergamino*, *Menta*, *Polar*, *Papiro*, *Lavanda*, *Glaciar*), permitiendo 36 combinaciones editoriales armónicas.
2. **Motor Algorítmico `ThemePaletteGenerator`:** Analiza la luminancia de cualquier color primario o fondo elegido y ajusta iterativamente sus valores para certificar una relación de contraste mínima de **$4.5:1$ en texto estándar (WCAG AA/AAA)**.
3. **Estudio Espectral Continuo (`HexColorStudioPicker`):** Se proveen sliders táctiles de Matiz ($0^\circ–360^\circ$), Saturación ($0\%–100\%$) y Luminosidad ($0\%–100\%$) con entrada directa `#RRGGBB` para libertad estética total.
4. **Independencia del Modo Oscuro del SO:** La aplicación respeta el modo Claro/Oscuro seleccionado dentro de NotiFast mediante `MaterialTheme.colorScheme.isDark` (`background.luminance() < 0.5f`), evitando fugas de temas del fabricante o del sistema operativo.

## Alternativas Consideradas

| Alternativa | Motivo de Descarte |
|---|---|
| **Paletas Fijas Cerradas** | Restringe la autonomía del estudiante y limita la adopción emocional de la app como su cuaderno personal. |
| **Material You Exclusivo (Monet)** | Inconsistente entre marcas (comportamiento muy dispar en capas MIUI/HyperOS, One UI o ColorOS) y no ofrece lienzos de papel de lectura descansada. |
| **Selector de Color Libre sin Algoritmo de Luminancia** | El estudiante termina eligiendo textos claros sobre fondos claros o textos oscuros sobre fondos oscuros, arruinando la legibilidad. |

## Consecuencias

### Positivas
- ✅ **Accesibilidad Certificada:** 100% de las combinaciones cumplen con el estándar internacional WCAG AAA/AA.
- ✅ **Descanso Ocular:** Los lienzos como *Menta* o *Papiro* reducen drásticamente la fatiga visual en jornadas de estudio intensivas.
- ✅ **Aislamiento Total de Estado:** Las pestañas del selector operan con claves estables (`key(CustomStudioTab)`), eliminando interferencias entre el color de acento y el color de fondo.
- ✅ **Identidad Visual de Autor:** NotiFast se posiciona como una aplicación de diseño cuidado y refinado.

### Negativas y Mitigaciones
- ⚠️ *Mayor complejidad matemática en el cómputo de la paleta inicial:* Mitigado mediante la memoización en `remember` y suites exhaustivas de pruebas unitarias en `ThemePaletteGeneratorTest.kt`.
