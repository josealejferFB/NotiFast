# Sistema de Diseño NotiFast 3.2: "Tactile Papercraft & Dynamic Academic Studio"

| Metadato | Valor |
|---|---|
| **Versión del Sistema** | 3.2.0 |
| **Arquetipo Visual** | Tactile Papercraft / Digital Studio Notebook (WCAG AAA) |
| **Plataformas** | Android Nativo (Jetpack Compose + Material 3) & Web Showcase (Tailwind + CSS Variables) |
| **Documentos Relacionados** | [Flujos UX](./ux-flows.md) · [Brief de Marca](./brand/logo-brief.md) · [Arquitectura](../03-architecture/system-overview.md) |

---

## 1. Visión y Fundamentos del Sistema

NotiFast consolida una identidad visual **EdTech de autor**: táctil, cálida, de alta precisión y profundamente enfocada en la serenidad cognitiva del estudiante. 

A partir de la versión 3.2, el sistema desacopla por completo el **esquema de acentos interactivos (`ThemePreset`)** del **tono de lienzo de fondo (`BackgroundPreset`)**, permitiendo 36 combinaciones predeterminadas más un **Estudio Espectral Hex/HSL continuo** con calibración matemática de contraste accesible **WCAG AAA ($\ge 4.5:1$)**.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        ARQUITECTURA DE DISEÑO                          │
├───────────────────────────────────┬────────────────────────────────────┤
│     PALETA DE ACENTO INTERACTIVO  │        LIENZO DE PAPEL DE FONDO    │
│  (ThemePreset - Botones, Chips,   │   (BackgroundPreset - Superficies, │
│   Bordes Activos, Focos y Títulos)│    Contenedores, Lienzo Editorial) │
├───────────────────────────────────┴────────────────────────────────────┤
│                   MOTOR DE TEMAS WCAG AAA DINÁMICO                     │
│                ThemePaletteGenerator + HexColorStudioPicker            │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Tokens Semánticos de Color

### 2.1 Tema Base Insignia: NotiFast Classic

#### Modo Claro (Light Canvas)
| Token | Hex | Rol Semántico |
|---|---|---|
| `background` | `#FAF9F6` | Lienzo principal tono Pergamino |
| `surface` | `#FFFFFF` | Superficie pura para tarjetas elevadas |
| `surface-container` | `#F3EFEA` | Contenedor suave para campos de texto y chips |
| `surface-container-high` | `#EBE6DF` | Superficie de contraste intermedio |
| `border` (`outlineVariant`) | `#E5DFD7` | Micro-borde nítido táctil de 1dp |
| `primary` | `#9E1B32` | NotiFast Muted Crimson (Carmesí Tenue de autor) |
| `on-primary` | `#FFFFFF` | Tinta blanca de alto contraste sobre primario |
| `primary-container` | `#FDE8EB` | Contenedor suave de carmesí para estados activos |
| `secondary` | `#2A7B7B` | Studio Aqua (Turquesa académico profundo) |
| `tertiary` | `#15803D` | Verde académico para materias aprobadas |
| `error` | `#B91C1C` | Rojo de alerta académica y materias en riesgo |

#### Modo Oscuro (Dark Canvas)
| Token | Hex | Rol Semántico |
|---|---|---|
| `background` | `#0E131F` | Lienzo nocturno azulado profundo |
| `surface` | `#161E2D` | Superficie de tarjeta nocturna |
| `surface-container` | `#1C2638` | Contenedor nocturno para campos de texto |
| `surface-container-high` | `#202A3C` | Superficie nocturna elevada |
| `border` (`outlineVariant`) | `#2A384E` | Micro-borde táctil oscuro de 1dp |
| `primary` | `#E05A6E` | Carmesí claro calibrado para contraste nocturno |
| `on-primary` | `#3D000F` | Tinta oscura sobre primario nocturno |
| `primary-container` | `#5C1120` | Contenedor carmesí nocturno |
| `secondary` | `#38B2AC` | Aqua cian suave nocturno |
| `tertiary` | `#34D399` | Esmeralda suave para notas aprobadas en oscuro |
| `error` | `#F87171` | Salmón de advertencia nocturna |

---

## 3. Las 6 Colecciones de Acento (`ThemePreset.kt`)

| ID (`presetId`) | Nombre en UI | Primario (Light / Dark) | Secundario (Light / Dark) | Personalidad Académica |
|---|---|---|---|---|
| `classic` | **NotiFast Classic** | `#9E1B32` / `#E05A6E` | `#2A7B7B` / `#38B2AC` | Identidad insignia: Carmesí Tenue & Aqua Studio |
| `forest_sage` | **Campus Verde** | `#15803D` / `#34D399` | `#854D0E` / `#FBBF24` | Botánico y descansado para ciencias biológicas y lectura |
| `midnight_indigo` | **Cyber Nocturno** | `#4338CA` / `#818CF8` | `#0891B2` / `#22D3EE` | Índigo eléctrico y cian para informática y programación |
| `warm_mocha` | **Café Nórdico** | `#C2410C` / `#FB923C` | `#B45309` / `#FCD34D` | Terracota y miel cálida estilo biblioteca clásica |
| `sakura_zen` | **Sakura Zen** | `#6D28D9` / `#A78BFA` | `#BE185D` / `#F472B6` | Violeta suave y flor de cerezo para máxima serenidad |
| `ocean_cyan` | **Minimalista Nórdico** | `#0284C7` / `#38BDF8` | `#0F766E` / `#2DD4BF` | Azul océano y aguamarina de alta claridad estructural |
| `custom` | **Estudio Personalizado** | Hex libre del usuario | Derivado armónico | Calibrado algorítmicamente por `ThemePaletteGenerator` |

---

## 4. Los 6 Lienzos de Papel Independientes (`BackgroundPreset.kt`)

| ID (`presetId`) | Nombre en UI | Lienzo Claro (`bg` / `surface` / `container`) | Lienzo Oscuro (`bg` / `surface` / `container`) |
|---|---|---|---|
| `classic` | **Pergamino** | `#FAF9F6` / `#FFFFFF` / `#F3EFEA` | `#0E131F` / `#161E2D` / `#1C2638` |
| `forest_sage` | **Menta** | `#EAF4ED` / `#F6FAF7` / `#D7EADC` | `#0A1610` / `#12241B` / `#1A3527` |
| `midnight_indigo` | **Polar** | `#E6F0F8` / `#F5F9FD` / `#D1E4F3` | `#09101D` / `#111E35` / `#192B4B` |
| `warm_mocha` | **Papiro** | `#F5ECE1` / `#FCFAF6` / `#E8DAC8` | `#150F0B` / `#221913` / `#32251D` |
| `sakura_zen` | **Lavanda** | `#F2EAF7` / `#FAF6FC` / `#E4D3EE` | `#130B1C` / `#20122E` / `#2F1B43` |
| `ocean_cyan` | **Glaciar** | `#E5F3F5` / `#F5FAFB` / `#CCE8EB` | `#061418` / `#0D2026` / `#142E37` |
| `custom` | **Lienzo Hex Libre** | Color libre del estudiante | Tinta `onBackground` auto-calibrada según luminancia |

---

## 5. Estudio Espectral Continuo y Algoritmo WCAG AAA

En la pantalla de Ajustes, el componente `HexColorStudioPicker` ofrece control milimétrico sobre el color:
- **Sliders Táctiles Continuos:** Barras táctiles de Matiz ($0^\circ–360^\circ$), Saturación ($0\%–100\%$) y Luminosidad ($0\%–100\%$).
- **Entrada Hex Directa:** Campo para pegar o escribir cualquier código `#RRGGBB`.
- **Aislamiento de Estado (`key(CustomStudioTab)`):** Las pestañas de Énfasis y Lienzo operan con estados independientes sin interferencias mutuas.
- **Motor `ThemePaletteGenerator`:**
  - Analiza la luminancia de la combinación seleccionada según la fórmula:
    $$L = 0.2126 \cdot R_{sRGB} + 0.7152 \cdot G_{sRGB} + 0.0722 \cdot B_{sRGB}$$
  - Si el contraste cae por debajo de $4.5:1$ contra el fondo, el generador ajusta de forma imperceptible la luminosidad del acento hasta satisfacer la norma **WCAG AAA**.
  - Determina automáticamente si el lienzo requiere modo oscuro (`isBgDark = bg.luminance() < 0.5f`).

---

## 6. Tipografía y Jerarquía Editorial

NotiFast utiliza dos familias tipográficas complementarias de licencia abierta (OFL):
1. **Plus Jakarta Sans:** Empleada en toda la interfaz de usuario, títulos, botones y bloques de lectura fluida.
2. **JetBrains Mono:** Empleada exclusivamente para datos matemáticos y temporales (calificaciones, porcentajes, cuentas regresivas, códigos de aula y valores Hex), garantizando alineación numérica perfecta con `font-variant-numeric: tabular-nums`.

```
┌─────────────────────────────────────────────────────────────┐
│  NotiFast Typography Scale                                  │
├───────────────────┬──────────┬──────────┬──────────┬────────┤
│ Token             │ Tamaño   │ Interlínea│ Peso    │ Tracking│
├───────────────────┼──────────┼──────────┼──────────┼────────┤
│ Display (Hero)    │ 32sp     │ 38sp     │ Bold 700 │ -0.5sp │
│ Headline          │ 24sp     │ 30sp     │ SemiBold │ -0.25sp│
│ Title             │ 18sp     │ 24sp     │ SemiBold │ 0sp    │
│ Body (Lectura)    │ 15sp     │ 22sp     │ Regular  │ 0.15sp │
│ Body Small        │ 13sp     │ 18sp     │ Regular  │ 0.2sp  │
│ Label (Botones)   │ 12sp     │ 16sp     │ Medium   │ 0.4sp  │
│ Mono Academic     │ 14sp     │ 18sp     │ Medium   │ 0sp    │
│ Mono Academic Sm  │ 11.5sp   │ 16sp     │ Medium   │ 0sp    │
└───────────────────┴──────────┴──────────┴──────────┴────────┘
```

---

## 7. Componentes Atómicos del Sistema (`presentation/atoms/`)

| Componente | Especificaciones Técnicas y Comportamiento |
|---|---|
| **`NotiFastCard`** | Superficie tonal con micro-borde de `1dp` (`outlineVariant`), esquinas redondeadas de `16dp` a `20dp`, sin sombras pesadas, y respuesta háptica con micro-escala elástica a `0.97f` al ser presionada. |
| **`NotiFastButton`** | Altura mínima de $48$ dp, esquinas redondeadas de $12$ dp, micro-escala a `0.97f`, soporte de variantes (Primary, Secondary, Outlined, Danger) y debounce interno de $600$ ms para impedir dobles pulsaciones accidentales. |
| **`NotiFastTextField`** | Altura mínima de $56$ dp, borde `1dp` con foco en color primario, soporte de leading/trailing icons, navegación secuencial de teclado (`ImeAction.Next` / `Done`) e integración con `Modifier.imePadding()`. |
| **`NotiFastDialog`** | Diálogo modal accesible de confirmación con títulos descriptivos, soporte de acciones secundarias de cancelación y protección de descarte ante cambios sin guardar. |
| **`NotiFastExpandableFab`** | Botón flotante expandible en la pantalla de Agenda y Notas que despliega opciones contextuales (Crear apunte, Inscribir materia, Subir PDF UNERG) con animación suave de rotación y elevación. |
| **`NotiFastEmptyState`** | Ilustración vectorial limpia y texto pedagógico que guía al estudiante cuando aún no ha registrado datos, evitando pantallas en blanco desoladas. |

---

## 8. Física Táctil y Ergonomía de Entrada

1. **Flujos Modales Unificados (`ModalBottomSheet`):** Todas las acciones de creación y edición (apuntes, materias, horarios, evaluaciones, temas y respaldo en la nube) viven en hojas modales con `skipPartiallyExpanded = true`.
2. **Barra de Accesorios Acoplada (`DockedAccessoryToolbar`):** La barra de herramientas del editor de notas se acopla dinámicamente justo sobre el teclado virtual mediante `Modifier.imePadding()`, garantizando acceso continuo a Markdown, fórmulas y fotos sin tapar el texto.
3. **Jerarquía Three-Tier Back Handling:** El botón Atrás cierra primero el teclado virtual, luego confirma ante cambios sin guardar y finalmente descarta el modal.
