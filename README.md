# 📚 NotiFast

> **Tu aprendizaje, a la velocidad de tus ideas.**

NotiFast es un **hub educativo** para estudiantes — un cuaderno académico con esteroides. Organiza tus apuntes por materia, gestiona evaluaciones y calificaciones, consulta tu calendario académico y mantén tu rendimiento bajo control.

---

## ✨ Features

| Feature | Descripción |
|---|---|
| 🔐 **Autenticación** | Login con email/contraseña y Google Sign-In |
| 📝 **Apuntes** | Crea, edita y elimina notas con imágenes por materia |
| 📚 **Materias** | Organiza contenido con icono y color personalizables |
| 📊 **Calificaciones** | Seguimiento de evaluaciones con promedio ponderado |
| 📅 **Calendario** | Vista mensual con evaluaciones y notas por día |
| 🕐 **Horarios** | Visualización de horarios semanales por materia |
| 👤 **Perfil** | Edición de nombre, apellido y foto de perfil |
| ⚙️ **Ajustes** | Gestión de cuenta y cierre de sesión |

---

## 🏗️ Arquitectura

```
Clean Architecture + MVVM + MVI Contracts
```

```
app/src/main/java/com/example/notifast/
├── data/                    # Capa de datos
│   ├── model/               # Data classes (Note, Evaluation, Schedule, User, SubjectData)
│   └── repository/          # Implementaciones de repositorios (Firebase)
│
├── di/                      # Inyección de dependencias (Hilt)
│   ├── AppModule.kt         # Provee FirebaseFirestore, FirebaseAuth
│   └── RepositoryModule.kt  # Bindings de interfaces → implementaciones
│
├── domain/                  # Capa de dominio
│   └── repository/          # Interfaces de repositorios
│
├── presentation/            # Capa de presentación
│   ├── atoms/               # Design system (15 componentes reutilizables)
│   ├── feature/             # Features (14 pantallas con ViewModel + Contract)
│   │   ├── auth/            # Login / Sign-up
│   │   ├── notes/           # Lista de materias y notas
│   │   ├── addnote/         # Crear nota
│   │   ├── editnote/        # Editar nota
│   │   ├── addsubject/      # Crear materia
│   │   ├── subjectdetail/   # Detalle de materia
│   │   ├── calendar/        # Calendario académico
│   │   ├── grades/          # Rendimiento y calificaciones
│   │   ├── schedules/       # Horarios semanales
│   │   ├── addevaluation/   # Crear evaluación
│   │   ├── editevaluation/  # Editar evaluación
│   │   ├── settings/        # Ajustes
│   │   └── editprofile/     # Editar perfil
│   ├── navigation/          # NavGraph + Screen routes
│   └── theme/               # Color, Typography, Theme
│
├── MainActivity.kt          # Entry point (@AndroidEntryPoint)
└── NotiFastApplication.kt   # @HiltAndroidApp
```

---

## 🛠️ Tech Stack

| Tecnología | Versión | Propósito |
|---|---|---|
| **Kotlin** | 2.0.21 | Lenguaje principal |
| **Jetpack Compose** | BOM 2024.09.00 | UI declarativa |
| **Material 3** | BOM | Design system |
| **Firebase Auth** | BOM 33.9.0 | Autenticación |
| **Firebase Firestore** | BOM 33.9.0 | Base de datos en la nube |
| **Firebase Storage** | BOM 33.9.0 | Almacenamiento de archivos |
| **Hilt** | 2.59 | Inyección de dependencias |
| **Coil** | 2.7.0 | Carga de imágenes |
| **Navigation Compose** | 2.8.5 | Navegación |
| **AGP** | 9.0.1 | Build system |
| **KSP** | 2.0.21-1.0.28 | Procesamiento de anotaciones |

---

## 🚀 Setup

### Prerrequisitos
- Android Studio (Ladybug o superior)
- JDK 17+
- Cuenta de Firebase

### Instalación

1. **Clona el repositorio:**
   ```bash
   git clone https://github.com/josealejferFB/NotiFast.git
   cd NotiFast
   ```

2. **Configura Firebase:**
   - Crea un proyecto en [Firebase Console](https://console.firebase.google.com/)
   - Habilita Authentication (Email/Password + Google)
   - Habilita Cloud Firestore
   - Descarga `google-services.json` y colócalo en `app/`

3. **Compila y ejecuta:**
   ```bash
   ./gradlew assembleDebug
   ```
   O abre el proyecto en Android Studio y presiona ▶️

---

## 📁 Estructura de Firestore

```
users/
└── {userId}/
    ├── firstName, lastName, email, profilePictureUrl
    ├── subjects/
    │   └── {subjectName}/
    │       ├── name, icon, colorHex
    │       └── notes/
    │           └── {noteId}/
    │               ├── title, content, subject, imageUrl, timestamp
    ├── evaluations/
    │   └── {evalId}/
    │       ├── title, description, subjectName, date, grade, weight, isPresented, imageUrl, quickTips
    └── schedules/
        └── {scheduleId}/
            ├── subjectName, dayOfWeek, startTime, endTime, classroom, teacher, color
```

---

## 🎨 Identidad Visual

| Color | Hex | Uso |
|---|---|---|
| **Ruby** | `#BF092F` | Color primario, acciones principales |
| **Aqua** | `#3B9797` | Color secundario, estados positivos |

---

## 📖 Documentación

| Documento | Ubicación |
|---|---|
| PRD Maestro v2.0 | [`docs/prd/PRD-MAESTRO-NOTIFAST.md`](docs/prd/PRD-MAESTRO-NOTIFAST.md) |
| Estado del Roadmap | [`docs/prd/STATUS.md`](docs/prd/STATUS.md) |
| Kickoff Fase 4 (Siguiente) | [`docs/prd/KICKOFF_FASE_4.md`](docs/prd/KICKOFF_FASE_4.md) |
| Auditoría completa | [`docs/audit/`](docs/audit/) |
| Planes de implementación | [`docs/superpowers/plans/`](docs/superpowers/plans/) |
| Specs de diseño | [`docs/superpowers/specs/`](docs/superpowers/specs/) |
| Guía para agentes AI | [`AGENTS.md`](AGENTS.md) |

---

## 📊 Métricas

- **~8,300** líneas de código Kotlin
- **56** archivos fuente
- **14** features implementadas
- **15** componentes del design system
- **5** repositorios
- **12** ViewModels

---

## ⚠️ Estado Actual

Este proyecto está en **desarrollo activo**. Consulta la [auditoría](docs/audit/2026-06-18-full-audit.md) para ver el estado actual de calidad, seguridad y próximos pasos.

---

## 📄 Licencia

Este proyecto es privado y de uso exclusivo del equipo de desarrollo de NotiFast.
