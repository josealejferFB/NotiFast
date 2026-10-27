# Identidad Visual y Brief de Marca: NotiFast

| Metadato | Valor |
|---|---|
| **Nombre de Marca** | NotiFast |
| **Lema (Tagline)** | *Tu Cuaderno Académico Inteligente & Hub de Estudio* |
| **Arquetipo de Personalidad** | El Compañero de Estudio Calmo, Preciso y de Alta Artesanía (*Tactile Academic Studio*) |
| **Versión de Marca** | 3.2.0 |
| **Documentos Relacionados** | [Sistema de Diseño](../design-system.md) · [Visión de Producto](../../00-vision/product-vision.md) |

---

## 1. Semiótica del Isotipo e Identidad Visual

El isotipo de NotiFast sintetiza dos conceptos fundamentales de la experiencia estudiantil:

1. **El Pliego de Papel Táctil (Papercraft Fold):** La esquina doblada de un cuaderno físico de alta gama, que evoca orden, estudio pausado, textura artesanal y concentración libre de pantallas saturadas de estímulos.
2. **El Rayo Notificador Dinámico (Fast Lightning Strike):** La velocidad y proactividad de un asistente académico que avisa al minuto exacto, realiza cálculos instantáneos de promedios y opera a la velocidad del pensamiento sin retrasos por servidores lejanos.

```
       ▲
      / \        [Pliego Superior: Ángulo agudo de rayo]
     /   \
    /     \      [Cuerpo Central: Geometría de pliego de papel]
   /  / \  \
  /__/   \__\    [Esquina Inferior Doblada: Cuaderno de notas]
```

La fusión de ambas figuras produce un símbolo icónico geométrico, simétrico y reconocible a cualquier escala (desde un favicon de 16px hasta un cartel impreso en alta definición).

---

## 2. Paleta Cromática Fundacional

La identidad fundacional de NotiFast huye intencionalmente del azul genérico corporativo y de los púrpuras estridentes de plantillas de desarrollo:

| Rol de Marca | Nombre del Tono | Código Hex | Justificación Psicológica y Semiótica |
|---|---|---|---|
| **Primario Insignia** | NotiFast Muted Crimson | `#9E1B32` | Carmesí sobrio y maduro; transmite la seriedad de una institución académica de tradición y la vitalidad de una fecha límite bien atendida. |
| **Secundario Armónico** | Studio Aqua | `#2A7B7B` | Turquesa descansado e intelectual; aporta serenidad, claridad mental y equilibrio visual en contraste con el carmesí. |
| **Lienzo Principal** | Pergamino Suave | `#FAF9F6` | Papel de imprenta no blanqueado químicamente; reduce el deslumbramiento ocular durante lecturas prolongadas. |
| **Tinta Principal** | Tinta Carbón | `#181A1F` | Negro cálido de alta legibilidad editorial, libre de la estridencia del negro puro `#000000`. |
| **Acento de Éxito** | Verde Cátedra | `#15803D` | Verde esmeralda calmado para calificaciones aprobatorias y metas cumplidas. |

---

## 3. Especificación del Icono Adaptativo de Android (Adaptive Icon)

El icono de NotiFast para la plataforma Android cumple con las directrices de Material You y Adaptive Icons (API 26+ a API 36):

1. **Capa de Fondo (`ic_launcher_background.xml`):**
   - Superficie tonal fija en Pergamino `#FAF9F6` (en modo claro) o Azul Noche `#0E131F` (en modo oscuro), con micro-borde sutil.
2. **Capa Frontal (`ic_launcher_foreground.xml`):**
   - Isotipo vectorial centrado dentro del área segura de $72 \times 72$ dp (viewport total de $108 \times 108$ dp) para permitir máscaras circulares, cuadradas (squircle) o redondeadas según el fabricante del dispositivo.
   - Gradiente tonal sutil entre `#9E1B32` y `#801426` con micro-sombra ambiental de 1dp.
3. **Capa Monocromática (Android 13+ Themed Icons):**
   - Silueta vectorial simplificada de un solo canal que permite a Android tintar el icono automáticamente con el color del tema dinámico del usuario (Material You / Monet Engine).

---

## 4. Tono de Voz y Guía de Redacción (Copywriting)

El lenguaje de NotiFast sigue cuatro directrices editoriales estrictas:

- **Calmo y Empático:** Nunca culpa al usuario ni genera estrés innecesario. Si una materia tiene evaluaciones pendientes, muestra un estado sobrio en lugar de un `0.0` que sugiera reprobación.
- **Técnicamente Preciso:** Emplea los términos académicos reales del estudiante (*Parcial*, *Taller*, *Ponderación %*, *Cátedra*, *Sección*, *Aula*).
- **Directo y Libre de Relleno:** Los botones indican acciones claras (*"Subir a Drive"*, *"Importar Horario"*, *"Calcular Meta"*), evitando verbos ambiguos como *"Aceptar"* o *"Procesar"*.
- **Localizado en Español:** Toda la experiencia de usuario y mensajes del sistema se escriben en español neutro latinoamericano con ortografía rigurosa.

---

## 5. Showcase Web y Presencia Digital

La identidad visual se extiende a la web pública del proyecto:
- **Landing Page Interactiva:** Alojada en Cloudflare Pages ([notifast.pages.dev](https://notifast.pages.dev/)), construida con diseño táctil, tipografías Google Fonts (`Plus Jakarta Sans` y `JetBrains Mono`) y código libre de frameworks pesados.
- **Simulador Interactivo:** Permite a los visitantes probar en vivo en su navegador el cambio de los 6 lienzos de papel, las 6 paletas de acento y la simulación de alarmas proactivas antes de descargar la aplicación.
