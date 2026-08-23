# PRODUCT.md — NotiFast: Cuaderno Académico Inteligente & Hub de Estudio

## 1. Registro de Producto

- **Nombre:** NotiFast
- **Categoría:** EdTech / Productividad Académica / Hub Estudiantil
- **Plataforma:** Android Nativo (Kotlin 2.0 + Jetpack Compose + Material 3)
- **Arquetipo Visual:** Tactile Papercraft / Digital Studio Notebook

---

## 2. Usuarios y Contexto Emocional

### Audiencia Primaria
1. **Estudiantes de Secundaria (14–18 años):** Sobrecargados por múltiples materias, tareas simultáneas y calendarios cambiantes. Necesitan simplicidad visual instantánea, recordatorios proactivos y confirmación clara de lo que ya completaron.
2. **Estudiantes Universitarios y de Grado (18–25+ años):** Manejan esquemas de ponderación complejos (porcentajes de notas, promedios acumulados, solapamiento de horarios de clases y laboratorios). Requieren precisión matemática, notas estructuradas y un entorno sin distracciones.
3. **Investigadores / Tesistas (Demostración de Ingeniería):** NotiFast funge como proyecto de graduación con estándares de ingeniería de software rigurosos, arquitectura limpia y métricas de desempeño reales en aula.

### Contexto Emocional y Estado Mental del Usuario
El estudiante recurre a NotiFast en momentos de **alta carga cognitiva y estrés pre-exámenes**. La aplicación debe operar como un **"anclaje de calma"**:
- **Cero ansiedad por alertas estridentes:** Los recordatorios no deben sentirse como alarmas médicas o castigos punitivos.
- **Sensación de control y orden:** La interfaz transmite la tranquilidad de un cuaderno físico de notas de alta gama, ordenado y estructurado, complementado con el poder de la sincronización en la nube y cálculos automatizados.
- **Fricción mínima:** Registrar una nota, un horario o una calificación no debe tomar más de 5 a 10 segundos.

---

## 3. Propósito del Producto & Estrella Polar (North Star)

> **"Transformar la sobrecarga académica en serenidad y enfoque mediante un cuaderno digital táctil, inteligente y libre de fricción."**

El éxito del producto no se mide por el tiempo que el usuario pasa dentro de la app (métrica de engagement tóxico), sino por **la velocidad con la que obtiene claridad sobre su día académico y retoma sus sesiones de estudio o descanso**.

---

## 4. Posicionamiento en el Mercado

NotiFast se sitúa en la intersección dorada entre:
1. **La inmediatez táctil de un cuaderno de papel:** Espacios limpios, texturas suaves, tipografía editorial legible y sensación de trabajo manual enfocado.
2. **La potencia de un asistente académico automatizado:** Cálculo instantáneo de promedios ponderados, detección de colisiones de horarios, sincronización en tiempo real con Firebase Firestore y notificaciones locales inteligentes con WorkManager.

```
┌─────────────────────────────────────────────────────────────┐
│                    NOTIFAST EDTECH HUB                      │
├──────────────────────────────┬──────────────────────────────┤
│    CUADERNO TÁCTIL DIGITAL   │    INTELIGENCIA ACADÉMICA    │
│  - Apuntes limpios           │  - Promedios ponderados      │
│  - Jerarquía tipo Studio     │  - Detección de solapamiento │
│  - Texturas de papelería     │  - Notificaciones locales    │
│  - Enfoque sin distracciones │  - Sincronización Cloud      │
└──────────────────────────────┴──────────────────────────────┘
```

---

## 5. Personalidad de Marca & Tono de Voz

- **Táctil y Artesanal (Studio Craft):** Cuida los detalles, las proporciones y la calidez visual. No es software corporativo genérico.
- **Sereno y Enforcado (Calm Productivity):** Utiliza fondos cálidos tipo papel marfil, tintas profundas de alto contraste y acentos terracota y ámbar que guían sin gritar.
- **Riguroso y Preciso:** Los datos de notas, fechas y horarios son exactos, legibles en fuentes monoespaciadas especializadas cuando se trata de cifras y métricas.
- **Alentador y Empático:** La microcopia celebra los logros (aprobar una materia, completar apuntes) con lenguaje natural y motivador, sin infantilizar al estudiante.

---

## 6. Anti-Referencias Críticas (Lo que NotiFast NUNCA será)

| Anti-Referencia | Por qué se rechaza | Enfoque NotiFast |
|---|---|---|
| **LMS Burocrático Tradicional (ej. Moodle 2010)** | Interfaces densas, formularios grises interminables, tablas saturadas y navegación laberíntica. | Tarjetas tipo papelería, vistas condensadas con Bento Grid, formularios progresivos en 1 toque. |
| **App de Alarmas Médicas / Alerta Roja** | Rojos agresivos (`#BF092F`) y tonos de pánico que disparan la ansiedad del estudiante ante una entrega. | Acentos Terracota Cálido (`#C2410C`), Ámbar Estudio (`#D97706`) y Verde Salvia (`#15803D`) que transmiten prioridad sin generar pánico. |
| **Gamificación Hiperactiva Infantil** | Sonidos estridentes, avatares invasivos y barras de energía que distraen del estudio real. | Micro-interacciones táctiles sutiles (resortes de 150ms, haptics suaves y confetti discreto solo en hitos reales). |
| **Hoja de Cálculo Corporativa / CRM** | Vistas de tablas infinitas con 20 columnas ilegibles en smartphones. | Diseños modulares adaptados a pantallas móviles con áreas de toque mínimas de 48dp y espaciado de 8dp. |

---

## 7. Principios Fundamentales de Diseño y Usabilidad

1. **La Regla del Papel Inteligente (Smart Paper Canvas):** La pantalla es un pliego de estudio. La información se organiza en hojas/tarjetas flotantes con bordes sutiles y calidez tonal, eliminando sombras artificiales pesadas.
2. **Claridad en Menos de 3 Segundos:** Al abrir la aplicación, el estudiante debe identificar de inmediato:
   - Su próxima clase u horario.
   - La evaluación más cercana.
   - Su estado académico global (promedio ponderado actual).
3. **Ergonomía Pulgar-First (Thumb Zone Optimization):** Las acciones principales (crear apunte, agregar nota, filtrar materias) se ubican en la mitad inferior de la pantalla o mediante FAB ergonómico accesible con una sola mano.
4. **Respeto a la Diversidad de Dispositivos:** Interfaces 100% fluidas desde teléfonos compactos (360dp de ancho) hasta tablets en modo horizontal, respetando Safe Areas y Dynamic Type de Android.

---

## 8. Estándares de Accesibilidad e Inclusión (WCAG AAA/AA)

- **Ratio de Contraste Estricto:** Texto normal con contraste mínimo de `4.5:1` y títulos/grandes con `3:1` sobre fondos tanto en Modo Claro (Lienzo Papel) como en Modo Oscuro (Mesa de Estudio Nocturna).
- **No Dependencia Exclusiva del Color:** Todo estado de aprobación, alerta de examen o conflicto de horario combina color semántico + icono vectorial + texto descriptivo.
- **Soporte Completo para Lectores de Pantalla:** Etiquetas accesibles (`contentDescription` contextual) en todos los iconos interactivos, excluyendo los puramente decorativos.
- **Soporte de Texto Dinámico:** La jerarquía tipográfica utiliza unidades `sp` escalables sin romper layouts ni truncar información crítica.
