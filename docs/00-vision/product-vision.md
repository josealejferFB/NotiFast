# Visión del Producto: NotiFast

## 1. Declaración de Visión y Estrella Polar (North Star)

> **"Transformar la sobrecarga académica y la ansiedad pre-exámenes en serenidad, enfoque y orden mediante un cuaderno digital táctil, personalizable, 100% offline-first y matemáticamente riguroso."**

El éxito de NotiFast no se mide por métricas de retención o tiempo de uso adictivo, sino por **la velocidad y tranquilidad con la que el estudiante obtiene claridad sobre su panorama académico (clase en curso, entregas inminentes y promedio real) para retomar de inmediato su estudio o descanso.**

---

## 2. Propósito Dual del Proyecto

NotiFast nace con una vocación dual articulada orgánicamente:

| Dimensión | Propósito y Alcance |
|---|---|
| **1. Producto de Software Comercial** | Aplicación móvil nativa Android de alta calidad para distribución gratuita en Google Play Store, optimizada para dispositivos de gama media y baja, con cero publicidad invasiva, cero venta de datos y respaldo privado opcional en Google Drive. |
| **2. Proyecto Académico de Tesis de Grado** | Demostración rigurosa del ciclo de vida completo de la ingeniería de software moderna en el marco de la Universidad Nacional Experimental Rómulo Gallegos (UNERG): Clean Architecture, MVI, persistencia relacional local (Room SQLite), diffing incremental SHA-256, motor de alarmas Doze-safe y **805 pruebas unitarias automatizadas**. |

---

## 3. El Problema: Fragmentación y Ansiedad Académica

El estudiante universitario y de secundaria en América Latina enfrenta diariamente un ecosistema fragmentado y estresante:

1. **Ansiedad y Fatiga Pre-Exámenes:** El estudiante no tiene certeza de qué calificación necesita para aprobar o mantener su beca. Suele calcular promedios en libretas o notas sueltas, cometiendo errores aritméticos graves (como promediar porcentajes directamente o promediar notas parciales de diferente peso).
2. **Apuntes Desorganizados y Pérdida de Información:** Apuntes divididos entre fotos borrosas de pizarras en la galería del teléfono, notas de voz, hojas sueltas y chats de WhatsApp. Las fórmulas matemáticas y diagramas quedan inaccesibles o ilegibles.
3. **Pésima Conectividad en el Aula:** Las universidades y colegios presentan con frecuencia zonas sin cobertura celular ni Wi-Fi estable (sótanos, laboratorios, aulas alejadas). Las aplicaciones basadas en nube (LMS tradicionales o Notion) se congelan con spinners infinitos o impiden tomar apuntes.
4. **Horarios Cambiantes y Olvidos Involuntarios:** Fechas de entrega, evaluaciones reprogramadas y aulas rotativas provocan inasistencias y entregas tardías. Los sistemas de alarma habituales son silenciados por las optimizaciones de batería de Android (Doze Mode).
5. **Pérdida de Identidad y Rigidez Visual:** Las herramientas académicas suelen ser grises, burocráticas y frías, sin permitir que el estudiante adapte el entorno visual a su estilo personal de estudio ni a la lectura nocturna prolongada.

---

## 4. La Propuesta de Valor de NotiFast

| Actor | Propuesta de Valor |
|---|---|
| **Estudiante Universitario** | Cuaderno estructurado por bloques con Markdown, fórmulas KaTeX y dibujo anotado sobre fotos de pizarra; simulador interactivo de meta (*«¿cuánto necesito sacar en el examen final?»*); agenda reactiva con importación directa del comprobante PDF de DACE-UNERG; y respaldo privado en su propio Google Drive sin depender de servidores ajenos. |
| **Estudiante de Secundaria** | Panel visual intuitivo tipo Bento Grid; confirmación visual de materias al día; alertas exactas de clases con salón y profesor; y soporte de múltiples escalas de notas (0–20, 0–10, 0–100%). |
| **Compañeros de Estudio** | Paquetes de materias portables `.notifast` transferibles sin conexión mediante código QR o Bluetooth/Sharesheet, permitiendo compartir el programa de la cátedra con un solo toque. |
| **Profesor / Docente** | Directorio docente integrado con enlace directo a WhatsApp y correo institucional preformateado para consultas académicas sin pérdida de tiempo. |
| **Comunidad Académica / Jurado** | Un referente público de excelencia en ingeniería de software para Android, comprobando que es posible construir herramientas de clase mundial 100% offline-first con Jetpack Compose. |

---

## 5. Pilares Fundamentales del Producto

```
┌────────────────────────────────────────────────────────────────────────┐
│                        PILARESS DE NOTIFAST 3.2                        │
├───────────────────┬───────────────────┬────────────────────────────────┤
│ 1. TACTIL & CALMO │ 2. 100% OFFLINE   │ 3. RIGOR MATEMÁTICO            │
│ Lienzos de papel, │ Room SQLite v9    │ GradePolicy única, promedios   │
│ micro-bordes de   │ local, cero nube  │ ponderados IEEE 754,           │
│ 1dp y física de   │ obligatoria y     │ simulador predictivo de nota   │
│ toque a 0.97f.    │ Pack P2P offline. │ restante en tiempo real.       │
├───────────────────┼───────────────────┼────────────────────────────────┤
│ 4. RESPETO PRIVADO│ 5. WCAG AAA DINÁMICO│ 6. RESILIENCIA OPERATIVA     │
│ Cero Firestore,   │ 6 Acentos, 6      │ Alarmas exactas Doze-safe,     │
│ respaldo privado  │ Lienzos, Hex libre│ Three-Tier Back handling y     │
│ en Google Drive   │ con contraste     │ cero pérdida de borradores en  │
│ del usuario.      │ algorítmico $\ge 4.5:1$│ reinicios de proceso.         │
└───────────────────┴───────────────────┴────────────────────────────────┘
```

---

## 6. Qué NO es NotiFast (Límites y Anti-Patrones Rechazados)

- **NO es un LMS burocrático:** No emula sistemas universitarios pesados (como Moodle o Blackboard) con menús interminables y formularios grises.
- **NO es una aplicación esclava de la nube:** No requiere conexión ni login para arrancar; no muestra pantallas bloqueantes de carga ni falla cuando se va la luz o no hay datos móviles.
- **NO es una red social ni plataforma publicitaria:** No tiene feeds de distracción, no vende datos a anunciantes ni bombardea al estudiante con notificaciones de retención vacía.
- **NO es una app de gamificación infantil:** No incluye barras de energía, avatares caricaturescos ni sonidos estridentes que desconcentren durante una jornada de estudio seria.
- **NO es un software cerrado ni privativo:** Todo su núcleo se apoya en estándares abiertos, código auditable y datos portables bajo control absoluto del usuario.
