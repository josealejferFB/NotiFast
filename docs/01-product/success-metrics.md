# Métricas de Éxito y Calidad: NotiFast

Este documento establece los indicadores clave de rendimiento (KPIs), métricas de calidad de software y parámetros de validación académica que determinan el éxito de NotiFast tanto en el mercado como en la defensa de tesis de grado.

---

## 1. Métricas de Calidad de Ingeniería de Software

| Métrica | Valor Objetivo | Valor Real Obtenido | Estado | Método de Medición |
|---|---|---|---|---|
| **Suite de Tests Automatizada** | $\ge 600$ tests unitarios | **805 tests unitarios** | 🟢 Superado | `./gradlew testDebugUnitTest` (JUnit4, MockK, Turbine, Robolectric) |
| **Tasa de Aprobación de Pruebas** | 100% | **100% (0 fallos, 0 errores, 0 omitidos)** | 🟢 Cumplido | Reportes XML de Gradle / CI Pipeline |
| **Estabilidad de la Aplicación** | Crash-Free Sessions $\ge 99.5\%$ | **100% en testing local y beta** (0 crashes) | 🟢 Cumplido | Firebase Crashlytics + Android Logcat |
| **Tiempo de Arranque en Frío (Cold Start)** | $\le 1200$ ms | **$\approx 780$ ms** en dispositivos gama media | 🟢 Cumplido | Android Vitals / Jetpack Benchmark |
| **Tasa de Cuadros en UI (Compose)** | 60 fps constantes | **60 fps sin frames congelados (jank $< 1\%$)** | 🟢 Cumplido | Profiler de Android Studio (GPU Rendering) |
| **Límite de Tamaño por Composable** | $\le 250$ LOC | **$100\%$ de los composables cumplen SRP** | 🟢 Cumplido | Auditoría estática de código |
| **Cadenas de Texto Hardcodeadas** | 0 cadenas en UI | **0 (100% centralizado en `strings.xml`)** | 🟢 Cumplido | Linter de Android Studio (`HardcodedText`) |

---

## 2. Métricas de Accesibilidad (a11y) y Diseño Visual

| Indicador | Estándar Requerido | Resultado NotiFast 3.2 | Estado |
|---|---|---|---|
| **Ratio de Contraste Cromático** | WCAG AA ($\ge 4.5:1$) / AAA ($\ge 7.0:1$ para lectura) | **$\ge 4.5:1$ garantizado matemáticamente** en todas las combinaciones mediante `ThemePaletteGenerator` | 🟢 Certificado |
| **Dimensiones Mínimas de Toque (Touch Targets)** | $\ge 48 \times 48$ dp según Material Design 3 | **$\ge 48$ dp en todos los botones y campos interactivos** (`NotiFastButton`, `NotiFastTextField`, FAB) | 🟢 Cumplido |
| **Independencia Cromática del Estado** | Nunca transmitir información exclusivamente por color | Todo estado (Al día, En riesgo, Pendiente) combina **color + icono vectorial + etiqueta textual localizada** | 🟢 Cumplido |
| **Apego al Tema Seleccionado** | Cero fugas del modo oscuro del sistema | Respeto estricto del tema interno vía `MaterialTheme.colorScheme.isDark` | 🟢 Cumplido |
| **Soporte de Fuentes y Escalas de Texto** | Escalado tipográfico fluido sin desbordamientos | Tipografías escalables `sp` (`Plus Jakarta Sans` y `JetBrains Mono`) verificadas hasta 130% de tamaño | 🟢 Cumplido |

---

## 3. Métricas de Privacidad y Eficiencia de Recursos

| Parámetro | Criterio de Aceptación | Medición en NotiFast | Estado |
|---|---|---|---|
| **Persistencia 100% Offline** | Cero fallos por falta de conectividad | **100% de operaciones funcionales sin internet** sobre Room SQLite v9 | 🟢 Cumplido |
| **Almacenamiento de Datos Ajeno** | 0 bytes de apuntes o datos personales en servidores ajenos | **0 bytes** (eliminación de Firestore; solo Google Drive privado del usuario) | 🟢 Cumplido |
| **Optimización de Imágenes de Pizarra** | Reducción de peso sin pérdida de legibilidad de texto | Compresión JPEG calidad 80 y lado máximo de 1600 px (ahorro medio del 65% en bytes) | 🟢 Cumplido |
| **Tiempo de Parsing de Horario PDF** | $\le 500$ ms en móvil | **$\le 95$ ms promedio** para flujos ReportLab oficiales de DACE-UNERG | 🟢 Superado |
| **Puntualidad de Alarmas en Reposo (Doze)** | Desviación $\le 60$ segundos | **Disparo en el minuto exacto** mediante `AlarmManager.setExactAndAllowWhileIdle()` | 🟢 Cumplido |

---

## 4. Métricas de Validación Académica (Para Tesis de Grado)

Las siguientes métricas e instrumentos forman parte del protocolo de evaluación en campo durante la prueba piloto con estudiantes del Área de Ingeniería de Sistemas de la UNERG:

| Dimensión | Instrumento Metodológico | Meta de Tesis |
|---|---|---|
| **Usabilidad Percibida del Sistema** | Escala de Usabilidad del Sistema (SUS - *System Usability Scale*) | **Puntaje medio $\ge 85 / 100$** (Rango Excelente / Grado A) |
| **Impacto en la Ansiedad Académica** | Escala de Ansiedad Académica (Cuestionario Likert pre/post prueba) | Reducción estadísticamente significativa en la incertidumbre sobre notas y fechas |
| **Eficacia en la Gestión de Tiempos** | Tasa de entregas a tiempo reportadas por los estudiantes | Incremento $\ge 20\%$ en la preparación anticipada de evaluaciones |
| **Satisfacción con la Personalización** | Encuesta de preferencia visual de temas y lienzos | $\ge 90\%$ de valoración positiva sobre los 6 lienzos de lectura descansada |
| **Adopción de Transferencia P2P** | Tasa de éxito de escaneo de QR y apertura de archivos `.notifast` | $\ge 95\%$ de transferencias completadas en menos de 10 segundos en aula |
