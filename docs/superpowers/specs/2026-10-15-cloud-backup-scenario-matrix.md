# Matriz de Escenarios — Cloud Backup (Google Drive)

> Acompaña a [`2026-10-15-cloud-backup-google-drive-design.md`](2026-10-15-cloud-backup-google-drive-design.md). Cada fila se convierte en un caso de prueba (unitario cuando es lógica pura y manual en dos dispositivos para E7).

## A. Cuenta y acceso

| # | Situación | Acción | Resultado esperado |
|---|---|---|---|
| A1 | Primera instalación | Abrir la app | Onboarding local (nombre/apellido). No se menciona Google. |
| A2 | Sin cuenta vinculada | Tocar la nube del dashboard | `LinkAccountPrompt` con "Continuar con Google". TopBar con icono de teléfono. |
| A3 | Prompt abierto | Cancelar el selector de cuenta | Vuelve al prompt sin error. Nada cambia. |
| A4 | Cuenta elegida | Rechazar el permiso de Drive | Mensaje "Necesitamos permiso para guardar en tu Drive". La cuenta **no** queda vinculada. |
| A5 | Vinculación OK, Drive vacío | — | Panel con todas las materias como *Solo en el teléfono*. |
| A6 | Vinculación OK, Drive con N copias | — | Banner "Encontramos N materias en tu nube". |
| A7 | Dispositivo sin Play Services | Tocar "Continuar con Google" | "Tu dispositivo no permite iniciar sesión con Google". La app sigue usable. |
| A8 | Cuenta vinculada | Ajustes → Desvincular | Credenciales borradas y `subject_cloud_state` de esa cuenta eliminado. Datos locales intactos. Drive intacto. TopBar con teléfono. |
| A9 | Cuenta X vinculada | Ajustes → Cambiar a cuenta Y | Estados de X ocultos. Con Y todo es *Solo en el teléfono* o lo que Y tenga en Drive. Al volver a X, sus estados reaparecen (se guardan por `accountId`). |
| A10 | Token revocado desde la cuenta Google | Abrir el panel | Un reintento con token nuevo. Si falla: "Vuelve a vincular tu cuenta". |
| A11 | Cuenta vinculada | Usar la app sin red | Todo funciona. El panel muestra el último estado conocido con las acciones deshabilitadas ("Sin conexión"). |

## B. Subida

| # | Estado de la materia | Acción | Resultado esperado |
|---|---|---|---|
| B1 | Solo en el teléfono | Subir | Archivo creado en `appDataFolder` → *Sincronizada*. |
| B2 | Sincronizada | Editar una nota | El chip cambia a *Cambios sin subir* (sin red). TopBar con punto. |
| B3 | Cambios sin subir | Subir | `PATCH` del mismo `driveFileId` (sin duplicado) → *Sincronizada*. |
| B4 | Sincronizada | Subir de nuevo sin cambios | Acción no aplicable o no-op inmediato. No se consume red. |
| B5 | Nube más reciente (otro teléfono subió) | Subir | Confirmación "La nube es más reciente (<dispositivo>, <fecha>)". Si acepta, reemplaza. Si cancela, no cambia nada. |
| B6 | En ambos, sin vincular (mismo `subjectId`, sin estado local) | Subir | Igual que B5 (confirmación de reemplazo). |
| B7 | Materia con 30 fotos | Subir | Imágenes ≤ 1600 px y JPEG 80. Las originales locales no se tocan. Temporales borrados. |
| B8 | Lote de 5, la red cae en la 3.ª | Subir todas | 1 y 2 OK. 3, 4 y 5 marcadas como fallidas con "Reintentar fallidas". Nada queda a medias. |
| B9 | Datos móviles y lote > 20 MB | Subir | Aviso único con el tamaño estimado y Continuar/Cancelar. |
| B10 | Drive lleno | Subir | "Tu Google Drive está lleno". La materia sigue en su estado previo. |
| B11 | Archivo borrado en Drive desde otro teléfono (404 al hacer PATCH) | Subir | Se crea un archivo nuevo y se actualiza `driveFileId`. |
| B12 | Perfil y ajustes | Subir | `global.nfbak` creado o actualizado con perfil, prefs, profesores y avatar. |

## C. Restauración

| # | Situación | Acción | Resultado esperado |
|---|---|---|---|
| C1 | Solo en la nube, sin choque de nombre | Restaurar | Materia completa (notas, imágenes, evaluaciones, horarios) con el mismo `subjectId` → *Sincronizada*. Alertas reprogramadas. |
| C2 | Choque de nombre ("Cálculo" local vs nube) | Restaurar → **Sobrescribir** | Cascada + inserción en una transacción. Imágenes viejas borradas tras el commit → *Sincronizada*. |
| C3 | Choque de nombre | Restaurar → **Agregar como** | Prellenado `Cálculo (1)`. Se crea con `subjectId` nuevo → *Solo en el teléfono*. La copia de la nube sigue *Solo en la nube*. |
| C4 | Ya existen "Cálculo" y "Cálculo (1)" | Agregar como | Prellenado `Cálculo (2)`. |
| C5 | Diálogo "Agregar como" | Escribir "cálculo" (otra capitalización) | Error "Ya existe una materia con ese nombre" y botón deshabilitado. |
| C6 | Diálogo "Agregar como" | Dejar vacío / solo espacios / exceder la longitud | Error de validación y botón deshabilitado. El nombre se guarda recortado (trim). |
| C7 | Choque de nombre | Omitir | Nada cambia en esa materia y el lote continúa. |
| C8 | 3 materias en conflicto | Sobrescribir + "Aplicar a las demás" | Las 3 se sobrescriben sin más diálogos. |
| C9 | Cambios sin subir (mismo `subjectId`) | Restaurar | Tratado como conflicto: diálogo con advertencia "Perderás los cambios no subidos de este teléfono". |
| C10 | Archivo corrupto o error de validación (peso > 100, día inválido) | Restaurar | Ítem "Copia no compatible". Rollback sin restos (incluidas las imágenes copiadas). |
| C11 | `schemaVersion` mayor que el soportado | Restaurar | "Actualiza NotiFast para restaurar esta copia". Nada cambia. |
| C12 | Red cae durante la descarga | Restaurar | El ítem falla sin escribir nada en Room. Se puede reintentar. |
| C13 | Perfil y ajustes | Restaurar | Confirmación. Nombre, foto y prefs reemplazados. Profesores fusionados. `userId` local sin cambio. |
| C14 | Teléfono nuevo: onboarding local hecho → vincular | Restaurar todas | Todas *Sincronizadas* con su contenido. Prefs y profesores restaurados si se incluyó el ítem global. |

## D. Borrado

| # | Situación | Acción | Resultado esperado |
|---|---|---|---|
| D1 | Sin cuenta | Borrar materia | Diálogo actual (una sola confirmación). Borrado local. |
| D2 | Con cuenta, materia sin copia | Borrar materia | Diálogo actual. Borrado local. |
| D3 | Con cuenta y copia | Borrar → Solo en este teléfono | Borrado local y fila de estado eliminada. En el panel aparece como *Solo en la nube*. |
| D4 | Con cuenta y copia | Borrar → Teléfono y nube | `files.delete` OK → borrado local. Desaparece del panel. |
| D5 | Como D4 pero sin red o con error de Drive | Borrar → Teléfono y nube | "No se pudo borrar de la nube. ¿Borrar solo en este teléfono?". Nada se borra sin confirmación. |
| D6 | Como D4, Drive responde 404 | — | Se considera borrado en la nube y se borra en local. |
| D7 | Solo en la nube | Panel → Borrar de la nube | Confirmación → `files.delete` → desaparece. |

## E. Coexistencia con el Pack P2P

| # | Situación | Acción | Resultado esperado |
|---|---|---|---|
| E1 | Cualquier estado de nube | Compartir materia por QR/Sharesheet | Formato GZIP+Base64 idéntico al actual. Funciona sin cuenta ni red. |
| E2 | Importar un Pack con choque de nombre (estrategia RENAME) | Importar | Nombre `Materia (1)` (unificado con la nube, T5). La materia queda *Solo en el teléfono*. |
| E3 | Materia importada por QR | Subir a la nube | Igual que B1. |
| E4 | Suite de tests de Pack existente | `./gradlew test` | Verde (solo se ajusta la aserción del sufijo). |

## F. Indicador del TopBar

| Estado global | Icono / tinte |
|---|---|
| Sin cuenta | Teléfono, neutro (`onSurfaceVariant`) |
| Cuenta, todo sincronizado | Nube con check, Aqua (`tertiary`) |
| Cuenta, ≥ 1 con cambios sin subir o solo en el teléfono | Nube con punto, Ruby (`primary`) |
