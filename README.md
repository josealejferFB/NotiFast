# 📚 NotiFast — Cuaderno Académico Inteligente & Hub de Estudio

> **Todo tu semestre, desplegado con calma táctil. 100% Offline-First, personalizable y libre de anuncios.**
>
> 🌐 **Landing Page Interactiva en Vivo (Cloudflare Pages):** [**https://notifast.pages.dev/**](https://notifast.pages.dev/)

**NotiFast** es una aplicación Android nativa escrita en **Kotlin 2.0** con **Jetpack Compose** y **Material 3**, diseñada para transformar la sobrecarga académica de estudiantes de secundaria y universidad en serenidad, control y enfoque. Combina un editor de apuntes por bloques (estilo OneNote) con cálculo matemático exacto de promedios ponderados, un motor de temas personalizables WCAG AAA, alertas proactivas exactas y respaldo cifrado en tu propio Google Drive.

---

## ✨ Funcionalidades Principales (v1.0 RC — Design System 3.2)

| Módulo | Capacidades Reales en la App |
|---|---|
| 📝 **Editor de Apuntes por Bloques & Fotos Anotadas** | `NoteEditorBottomSheet` expandible con barra `DockedAccessoryToolbar` acoplada sobre el teclado, bloques ricos (encabezados, listas ordenadas/viñetas, checklists interactivos, citas, bloques matemáticos LaTeX) y **lienzo de dibujo a mano alzada sobre fotos de pizarra (`PhotoAnnotatorScreen`)**. |
| 🎨 **Estudio de Temas Personalizables & Lienzos AAA** | **6 Paletas de Acento (`ThemePreset`):** *NotiFast Classic*, *Campus Verde*, *Cyber Nocturno*, *Café Nórdico*, *Sakura Zen* y *Minimalista Nórdico*. **6 Lienzos de Papel Independientes (`BackgroundPreset`):** *Pergamino*, *Menta*, *Polar*, *Papiro*, *Lavanda* y *Glaciar*, más **Estudio Espectral Hex/HSL continuo** con calibración automática de contraste **WCAG AAA**. |
| 🔔 **Motor de Notificaciones Proactivas (AlarmManager + WorkManager)** | Alertas locales puntuales en 3 canales (`IMPORTANCE_HIGH`): **Alertas de Clases** (`10`, `15` o `30 min antes`), **Alertas de Evaluaciones** (`1`, `2` o `3 días antes` con su peso porcentual) y **Resumen Matutino (`07:00 AM`)**, con deep-linking reactivo y botón verificador de notificación de prueba en Ajustes. |
| 📊 **Calificaciones Multiescala & Simulador de Meta** | Motor único `GradePolicy` con precisión IEEE 754 adaptable a **4 escalas** (`0–10`, `0–20`, `0–100%` y letras `A–F`), **Simulador Interactivo de Nota Requerida** en tiempo real (*«¿Cuánto necesito sacar en el % restante?»*) y lista visual de Salud Académica por asignatura. |
| 🕐 **Agenda en Vivo & Calendario de Evaluaciones** | Baliza en tiempo real de **Clase en Curso (`ScheduleLiveStatusCard`)** con temporizador en `JetBrains Mono` y acceso directo al apunte de la sesión, matriz mensual interactiva de exámenes y `NotiFastExpandableFab`. |
| 👨‍🏫 **Directorio de Profesores & WhatsApp Contextual** | Gestión de docentes de **Cátedra y Laboratorio** vinculados a materias y horarios, con cubículo, correo, teléfono y acción en 1 toque a **WhatsApp con saludo académico pre-rellenado**. |
| ☁️ **100% Offline-First + Google Drive Cloud Backup Privado** | Base de datos local **Room SQLite v4** ultrarrápida y privada, con sincronización diferencial inteligente (`SHA-256`) en la carpeta oculta `appDataFolder` de tu propio **Google Drive (`CloudHubSheet`)**. |
| 📦 **Pack Académico P2P (`.notifast` & Código QR Offline)** | Exportación e importación de materias completas (apuntes, horarios, evaluaciones y docente) mediante archivos comprimidos `.notifast` (`GZIP + Base64`) o **Código QR Offline** con resolución de conflictos. |
| 🔍 **Búsqueda Global Instantánea** | Motor multi-entidad que indexa apuntes, bloques de texto, etiquetas `#tags`, materias, profesores y evaluaciones en milisegundos. |
| 🌐 **Landing Page Interactiva ([`https://notifast.pages.dev/`](https://notifast.pages.dev/))** | Desplegada en **Cloudflare Pages** con CI/CD automatizado: teléfono Android interactivo de 4 pestañas, Estudio de Temas en Vivo que recolorea toda la web en tiempo real y simulador de notificaciones Heads-Up. |

---

## 🏗️ Arquitectura del Sistema

```
Clean Architecture + MVVM + MVI Unidirectional StateFlow
```

```
app/src/main/java/com/notifast/app/
├── data/                    # Capa de Datos (100% Offline-First + Google Drive Backup)
│   ├── backup/              # Google Drive REST API v3 (appDataFolder), CloudDiffEngine, GlobalSnapshot
│   ├── local/               # Room SQLite v4 (Entities, DAOs, Migrations 1->2->3->4) + DataStore Prefs
│   ├── model/               # Modelos de datos (Note, NoteBlock, Evaluation, Schedule, SubjectData, Teacher)
│   ├── notification/        # ExactAlarmSchedulerImpl, AlarmTriggerReceiver, BootCompletedReceiver, Workers
│   └── repository/          # Implementaciones locales (Room/DataStore) y CloudBackupRepositoryImpl
│
├── domain/                  # Capa de Dominio (Reglas de Negocio Puras)
│   ├── model/               # ThemePreset, BackgroundPreset, GradeScale, AcademicPack, CloudSyncModels
│   ├── notification/        # ExactAlarmScheduler, NotificationSender, NotificationChannelsManager
│   ├── policy/              # GradePolicy (fórmula unificada de promedios y ponderaciones)
│   ├── repository/          # Contratos de repositorios
│   ├── usecase/             # Casos de uso (CalculateAcademicStats, ScheduleAlerts, Export/ImportPack, etc.)
│   └── util/                # ReminderTimeCalculator, TeacherFormatters, TextSpanOps
│
├── presentation/            # Capa de Presentación (Jetpack Compose + Material 3)
│   ├── atoms/               # Design System 3.2 (NotiFastCard, NotiFastButton, NotiFastTopBar, etc.)
│   ├── feature/             # 17 módulos de presentación con ViewModel + MVI Contract (<= 250 LOC por archivo)
│   ├── navigation/          # NavGraph, Screen routes y Deep-Linking reactivo
│   └── theme/               # ThemePaletteGenerator, Color, Typography, Shape, ThemeUtils
│
├── di/                      # Inyección de Dependencias con Hilt (AppModule, RepositoryModule, UseCaseModule)
├── MainActivity.kt          # Entry point (@AndroidEntryPoint) con soporte de Deep-Links
└── NotiFastApplication.kt   # @HiltAndroidApp + Inicialización de canales y WorkManager
```

---

## 🛠️ Stack Tecnológico Oficial

| Tecnología | Versión | Propósito |
|---|---|---|
| **Kotlin** | `2.0.21` | Lenguaje principal de producción y tests |
| **Jetpack Compose** | `BOM 2024.09.00` | UI declarativa reactiva + Material 3 |
| **Room SQLite** | `2.7.2` | Base de datos relacional local 100% Offline-First (Esquema `v4`) |
| **Jetpack DataStore** | `1.1.2` | Persistencia reactiva de preferencias, temas y configuración de alertas |
| **WorkManager + AlarmManager** | `2.10.0` | Motor híbrido de notificaciones exactas y resumen matutino 07:00 AM |
| **Google Drive REST API (OkHttp)** | `4.12.0` | Respaldo privado en la nube (`appDataFolder`) con diffing SHA-256 |
| **Credential Manager & Google ID** | `1.3.0` / `1.1.1` | Autenticación OAuth2 nativa para respaldo en Google Drive |
| **ZXing & Play Code Scanner** | `3.5.3` / `16.1.0` | Generación y escaneo de Códigos QR Offline para paquetes P2P `.notifast` |
| **Hilt (Dagger)** | `2.59` | Inyección de dependencias en UI, Repositorios y Workers |
| **Coil** | `2.7.0` | Carga eficiente de imágenes y fotografías anotadas |
| **Navigation Compose** | `2.8.5` | Navegación declarativa, BottomSheets modales y Deep-Links |
| **Firebase Crashlytics & Analytics** | `BOM 33.9.0` | Monitoreo de estabilidad y salud de la aplicación |
| **AGP & KSP** | `9.0.1` / `2.0.21-1.0.28` | Sistema de compilación (`compileSdk 36`, `minSdk 26`) |

---

## 🚀 Instalación y Ejecución

### Prerrequisitos
- **Android Studio** Ladybug o superior
- **JDK 17+**
- Dispositivo físico o emulador con **Android 8.0+ (API 26+)**

### Pasos Rápidos

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/josealejferFB/NotiFast.git
   cd NotiFast
   ```

2. **Compilar el APK Debug (Funciona 100% Offline de inmediato):**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Ejecutar la suite completa de pruebas unitarias (684 tests):**
   ```bash
   ./gradlew testDebugUnitTest
   ```

4. **Explorar la Landing Page Interactiva:**
   Abre [`landing/index.html`](landing/index.html) en cualquier navegador moderno o sírvela localmente para interactuar con el Estudio de Temas AAA en vivo, el simulador de notificaciones y el teléfono Android interactivo.

---

## 🎨 Sistema de Diseño 3.2: "Tactile Papercraft & Dynamic Academic Studio"

NotiFast incluye un motor cromático algorítmico (`ThemePaletteGenerator`) que permite combinar **6 paletas de acento** con **6 tonos de lienzo de papel** o cualquier color hexadecimal personalizado:

| Colección (`ThemePreset`) | Primario Claro / Oscuro | Secundario Claro / Oscuro | Lienzo Asociado (`BackgroundPreset`) |
|---|---|---|---|
| **NotiFast Classic** | `#9E1B32` / `#E05A6E` | `#2A7B7B` / `#38B2AC` | **Pergamino** (`#FAF9F6` / `#0E131F`) |
| **Campus Verde** | `#15803D` / `#34D399` | `#854D0E` / `#FBBF24` | **Menta** (`#EAF4ED` / `#0A1610`) |
| **Cyber Nocturno** | `#4338CA` / `#818CF8` | `#0891B2` / `#22D3EE` | **Polar** (`#E6F0F8` / `#09101D`) |
| **Café Nórdico** | `#C2410C` / `#FB923C` | `#B45309` / `#FCD34D` | **Papiro** (`#F5ECE1` / `#150F0B`) |
| **Sakura Zen** | `#6D28D9` / `#A78BFA` | `#BE185D` / `#F472B6` | **Lavanda** (`#F2EAF7` / `#130B1C`) |
| **Minimalista Nórdico** | `#0284C7` / `#38BDF8` | `#0F766E` / `#2DD4BF` | **Glaciar** (`#E5F3F5` / `#061418`) |

---

## 📖 Documentación del Proyecto (Estándar Modular)

La documentación técnica y de tesis de NotiFast se organiza bajo una jerarquía modular estricta en el directorio [`docs/`](docs/):

| Módulo | Documento Clave | Descripción |
|---|---|---|
| 🧭 **Índice Maestro** | [`docs/README.md`](docs/README.md) | Mapa de navegación completo, guía de convenciones y trazabilidad. |
| 🎯 **00 - Visión** | [`docs/00-vision/product-vision.md`](docs/00-vision/product-vision.md) • [`glossary.md`](docs/00-vision/glossary.md) | Propósito dual (comercial + tesis), problemas reales y glosario canónico. |
| 📋 **01 - Producto** | [`docs/01-product/PRD.md`](docs/01-product/PRD.md) • [`roadmap.md`](docs/01-product/roadmap.md) | PRD consolidado (Fases 0 a 5), historias en Gherkin, personas y métricas. |
| 🎨 **02 - Diseño** | [`docs/02-design/design-system.md`](docs/02-design/design-system.md) • [`ux-flows.md`](docs/02-design/ux-flows.md) | Tokens semánticos 3.2, ThemePaletteGenerator WCAG AAA y flujos UX críticos. |
| 🏗️ **03 - Arquitectura** | [`docs/03-architecture/system-overview.md`](docs/03-architecture/system-overview.md) • [`adr/`](docs/03-architecture/adr/) | Clean Architecture + MVI, modelo de datos Room v9 e índice de ADRs (0001 a 0007). |
| 🛡️ **04 - Calidad & Privacidad** | [`docs/04-quality-privacy/testing-strategy.md`](docs/04-quality-privacy/testing-strategy.md) • [`privacy-offline-first.md`](docs/04-quality-privacy/privacy-offline-first.md) | Pirámide de pruebas (805 tests) y soberanía de datos 100% Offline-First. |
| ⚖️ **05 - Legal** | [`docs/05-legal/legal-notes.md`](docs/05-legal/legal-notes.md) | Licencia Apache 2.0, política de privacidad escolar y notas institucionales UNERG. |

---

## 📊 Métricas Actuales de Ingeniería

- **~56,800** líneas de código Kotlin totales (producción y pruebas)
- **445** archivos fuente Kotlin modulares (cumpliendo $\le 250$ LOC por archivo `@Composable`)
- **122** suites de pruebas automatizadas con **805 tests unitarios pasando al 100% (0 fallos)**
- **17** módulos de presentación con arquitectura MVI + Clean Architecture
- **9** versiones de esquema Room SQLite con migraciones no destructivas probadas en memoria (`1➔2➔...➔9`)

---

## 📄 Licencia

Este proyecto se distribuye bajo la licencia **Apache License, Version 2.0**. Consulta [`docs/05-legal/legal-notes.md`](docs/05-legal/legal-notes.md) para más detalles.
