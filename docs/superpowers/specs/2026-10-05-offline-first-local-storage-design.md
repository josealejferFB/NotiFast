# Diseño — Fase 3.5: NotiFast Offline-First (100% Local)

> **Fecha:** 2026-10-05
> **Estado:** Aprobado (decisiones confirmadas por el usuario en sesión de planificación)
> **Plan de implementación:** [`docs/superpowers/plans/2026-10-05-offline-first-local-storage.md`](../plans/2026-10-05-offline-first-local-storage.md)

---

## 1. Objetivo

NotiFast debe funcionar **al 100% sin conexión a internet** desde el primer arranque: sesión, persistencia de datos, imágenes, cálculos, notificaciones, búsqueda y Pack P2P. La sincronización con la nube se diseñará en una fase posterior (“Fase Cloud Sync”); esta fase deja la base lista sin implementarla (YAGNI).

## 2. Diagnóstico del estado actual

| Área | Estado actual | Problema offline |
|---|---|---|
| Notas, materias, evaluaciones, horarios | Firestore (`users/{uid}/...`) con `.await()` en escrituras | Las escrituras con `await()` **no completan hasta que el servidor confirma** → spinners infinitos sin red. `addSubject` hace `get()` al servidor → falla sin caché. |
| Sesión | Firebase Auth (email/Google) obligatorio | Primer uso imposible sin red. |
| Perfil de usuario | Documento Firestore `users/{uid}` | Lectura falla sin caché. |
| Imágenes | Copia local JPEG en `filesDir/images/` | ✅ Ya es local. Fugas: no se borra la imagen anterior al reemplazarla; `EditNoteViewModel` re-copia la imagen en cada autoguardado. |
| Preferencias | DataStore | ✅ Ya es local. |
| Cálculos (promedios, analíticas, búsqueda) | Use cases puros sobre `Flow` de repositorios | ✅ Funcionan offline si los repositorios emiten datos locales. |
| Notificaciones (WorkManager) | Leen repositorios + `getCurrentUserId()` | Dependen de Firestore/Auth. |
| Tipografías | Google Fonts descargables (GMS provider) | Primer arranque offline → fuente del sistema (rompe el diseño). |
| Indicador TopBar | `isCloudSynced = true` por defecto → icono “nube sincronizada” | Mensaje falso en modo local. |
| Escáner QR (Pack P2P) | Google Code Scanner (módulo de Play Services) | Si el módulo no se descargó, falla sin red; existe fallback de pegar código, pero el error no lo indica. |
| Backup | `backup_rules.xml` / `data_extraction_rules.xml` de ejemplo | Sin reglas explícitas para BD + imágenes + sesión. |

## 3. Decisiones confirmadas

1. **Sesión = Perfil local.** Primer arranque → Onboarding (nombre, apellido opcional, foto opcional). Se genera un `userId` UUID local y se guarda como sesión activa en DataStore. Sin email/contraseña. La vinculación con Firebase se hará en la Fase Cloud Sync.
2. **Empezar limpio.** Los datos existentes en Firestore (testers) se ignoran; se recuperarán en la fase de sincronización.
3. **Login reemplazado por Onboarding local.** El código Firebase Auth se conserva como `data/remote/FirebaseAuthDataSource.kt` (no inyectado en el grafo de Hilt). Se eliminan `LoginScreen/LoginViewModel/LoginContract`.
4. **“Cerrar sesión” se elimina** (Ajustes y Editar Perfil): con un perfil local no tiene sentido y crearía datos huérfanos.

## 4. Arquitectura

```
presentation (sin cambios salvo Onboarding/Settings/TopBar/EditNote)
      │  onEvent / StateFlow
domain/repository  (interfaces: NoteRepository, EvaluationRepository, ScheduleRepository,
      │             ImageRepository, AuthRepository ← reformulada a sesión local)
data/repository    (implementaciones sobre Room + DataStore + filesDir)
      │
data/local         (NotiFastDatabase, entity/, dao/, mapper/, Converters)
data/remote        (FirebaseAuthDataSource — reservado, NO inyectado)
```

- **Fuente de verdad:** Room (`notifast.db`, versión 1, `exportSchema = true`, esquemas en `app/schemas/`).
- **Reactividad:** los DAO exponen `Flow`, así que la UI se actualiza en tiempo real igual que con los snapshots de Firestore.
- **Interfaces de dominio:** se conservan las firmas de `NoteRepository`, `EvaluationRepository` y `ScheduleRepository` → ViewModels y use cases no cambian.
- **IDs:** `String` UUID generados en el repositorio. **Timestamps:** `Long` epoch millis en entidades; `java.util.Date` en modelos de dominio. Se eliminan las anotaciones `@ServerTimestamp` (Firestore) de los modelos.
- **Materias:** clave natural `(userId, name)` con `COLLATE NOCASE` (unicidad sin distinguir mayúsculas ASCII, alineada con `DeleteSubjectCascadeUseCase`).
- **Borrado en cascada:** transacción Room (`withTransaction`) que borra notas, evaluaciones, horarios y la materia; las imágenes se borran **después** del commit.
- **Imágenes:** `ImageRepository.saveNoteImage/saveProfileImage` (renombrados desde `upload*`). Al reemplazar o borrar se elimina el archivo huérfano (nunca falla la operación principal).
- **Sesión:** `AuthRepository` pasa a ser la fuente de verdad del perfil local: `authState: Flow<Boolean>`, `createLocalProfile`, `getCurrentUserId`, `getUserProfile`, `updateProfile`. Implementación `LocalAuthRepositoryImpl` (Room `users` + DataStore `active_local_user_id`).
- **Tipografías:** TTF empaquetados en `res/font/` (Plus Jakarta Sans y JetBrains Mono, licencia OFL); se elimina `ui-text-google-fonts`.
- **Backup:** Auto Backup y transferencia entre dispositivos incluyen BD, `files/images/` y `files/datastore/`.

## 5. Fuera de alcance (Fase Cloud Sync)

- Subida/descarga a Firestore, resolución de conflictos, tombstones, cola de pendientes, vinculación de cuenta Google.
- Migración de datos de Firestore existentes.
- Exportación/importación manual de backup completo (zip).
- Bloqueo con PIN/biometría.

## 6. Criterios de aceptación

1. Instalación limpia en modo avión: Onboarding → crear materia → nota con imagen → evaluación con nota → horario → todo visible al instante.
2. Matar la app y reabrir (modo avión): datos, imágenes y sesión persisten; no vuelve a pedir Onboarding.
3. Promedios, analíticas, búsqueda global y Pack P2P (exportar QR / importar pegando código) funcionan offline.
4. Notificaciones programadas se disparan y se reprograman tras reinicio sin red.
5. Ninguna clase de `data/repository`, `domain` o `presentation` importa `com.google.firebase.firestore` ni `com.google.firebase.auth` (test de arquitectura).
6. `./gradlew testDebugUnitTest assembleDebug` en verde.
