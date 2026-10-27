# NotiFast: Documentación Integral del Sistema

> **Hub Educativo Integral, Cuaderno Académico Inteligente y Asistente Offline-First para Estudiantes de Secundaria y Universidad.**  
> *NotiFast es un desarrollo independiente de software y proyecto de tesis de grado (Ingeniería en Informática, UNERG).*

| Metadato | Valor Canónico |
|---|---|
| **Versión del Sistema** | NotiFast `1.0 RC` (Design System `3.2.0`) |
| **Fecha de Consolidación** | Octubre 2026 |
| **Estado de Pruebas** | 🟢 805 tests unitarios en verde (100% éxito, 0 fallos) |
| **Plataforma Objetivo** | Android 8.0+ (`minSdk 26`, `compileSdk 36`, `targetSdk 36`) |
| **Arquitectura** | Clean Architecture + MVI Estricto + 100% Offline-First |
| **Stack Técnico** | Kotlin `2.0.21`, Jetpack Compose Material 3, Room SQLite `v9`, Hilt `2.59` |
| **Web Showcase** | [notifast.pages.dev](https://notifast.pages.dev/) (Cloudflare Pages) |
| **Licencia de Software** | Apache License 2.0 |
| **Idioma Canónico** | Español (Documentación e Interfaz de Usuario) |

---

## 🧭 Mapa de Navegación de la Documentación

Esta documentación adopta una jerarquía modular, canónica y estructurada para guiar tanto el desarrollo continuo del producto comercial como la defensa académica y metodológica de la tesis de grado:

| Módulo | Propósito y Contenido | Destinatarios Principales |
|---|---|---|
| **[00-vision](./00-vision/)** | Fundamento de existencia, propósito dual (comercial + tesis), problemas que resuelve y glosario terminológico de dominio. | Todo el equipo, Jurado de Tesis, Dirección |
| **[01-product](./01-product/)** | **PRD Maestro consolidado (Fases 0 a 5)**, historias de usuario en Gherkin, personas arquetípicas, roadmap y métricas de éxito. | Producto, Diseño, Ingeniería, Jurado |
| **[02-design](./02-design/)** | Sistema de Diseño 3.2 ("Tactile Papercraft"), tokens semánticos, motor de temas WCAG AAA, flujos UX críticos y brief de marca. | Diseño de Producto, Frontend Compose |
| **[03-architecture](./03-architecture/)** | Visión del sistema (C4), modelo de datos Room SQLite v9, integraciones y **registro canónico de ADRs (ADR-0001 a 0007)**. | Arquitectura, Backend, Ingeniería Android |
| **[04-quality-privacy](./04-quality-privacy/)** | Estrategia de pruebas automatizadas (805 tests), pirámide de verificación, y blindaje de privacidad 100% Offline-First. | QA, Seguridad, Cumplimiento, Jurado |
| **[05-legal](./05-legal/)** | Licencia de código abierto, política de privacidad escolar/universitaria, notas institucionales y declaración de independencia. | Legal, Usuarios, Dirección Académica |

---

## 🎯 Documento Maestro de Requisitos

Para consultar la especificación funcional completa, la matriz de trazabilidad y los criterios de aceptación técnicos:  
➡️ **[PRD Maestro: Product Requirements Document](./01-product/PRD.md)**

---

## 📐 Convenciones Documentales y de Ingeniería

1. **Identificadores Estables de Requisitos:**
   - Funcionales: `FR-<MÓDULO>-NNN` (ej. `FR-NOTE-001`, `FR-GRADE-002`, `FR-CLOUD-004`).
   - No Funcionales: `NFR-NNN` (ej. `NFR-001` Rendimiento, `NFR-002` Offline-First).
2. **Registro de Decisiones Arquitecturales (ADR):**
   - Toda decisión estructural relevante se documenta en formato MADR numerado secuencialmente en [`03-architecture/adr/`](./03-architecture/adr/).
3. **Referencias Relativas y Trazabilidad:**
   - Todos los hipervínculos entre archivos Markdown son estrictamente relativos (`./` y `../`) para garantizar su portabilidad en repositorios locales y visualizadores web sin dependencias de rutas absolutas.
4. **Separación Lingüística:**
   - **Documentación y Textos de UI:** Español neutro/latinoamericano riguroso (`res/values/strings.xml`).
   - **Código Fuente y Metadatos Técnicos:** Inglés técnico para clases, variables, métodos y commits de Git.
5. **Verificación Continua:**
   - Todo cambio reflejado en esta documentación corresponde con el código fuente real verificado mediante `./gradlew assembleDebug` y `./gradlew testDebugUnitTest`.
