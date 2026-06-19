# T1 — Fix deleteSubject Cascade (Bloqueante B1)

## Context
NotiFast es una app Android con Kotlin + Jetpack Compose + Hilt + Firebase Firestore.
Arquitectura: Clean Architecture (data / domain / presentation).

## Problem
`NoteRepositoryImpl.deleteSubject()` borra las notas de la materia y el documento de la materia,
pero NO borra las evaluaciones (en `users/{uid}/evaluations`) ni los schedules
(en `users/{uid}/schedules`) que tienen `subjectName == subjectName`.
Esto deja datos huérfanos que contaminan el promedio global y el calendario.

## Required Change

### File: `data/repository/NoteRepositoryImpl.kt`
Path: `/home/analista/AndroidStudioProjects/NotiFast/app/src/main/java/com/example/notifast/data/repository/NoteRepositoryImpl.kt`

La función `deleteSubject()` debe:
1. Primero obtener todas las notas de la materia (ya lo hace)
2. **NUEVO**: Obtener todas las evaluaciones donde `subjectName == subjectName` de la colección `users/{uid}/evaluations`
3. **NUEVO**: Obtener todos los schedules donde `subjectName == subjectName` de la colección `users/{uid}/schedules`
4. Agregar todos esos documentos al mismo `WriteBatch` (ya usa batch)
5. Commit del batch (ya lo hace)

**IMPORTANTE**: El batch de Firestore tiene un límite de 500 operaciones. 
En condiciones normales de uso esto no será problema, pero agregar un comentario que lo documente.

**NOTA sobre excepciones de imágenes**: Las excepciones de `imageRepository.deleteImage()` 
actualmente se tragan silenciosamente (`catch(e: Exception) {}`). 
En este task, debes cambiar eso para que al menos se loguee con `Log.w("NoteRepository", "Failed to delete image", e)`.

### Imports nuevos necesarios
El repositorio ya tiene `FirebaseFirestore` inyectado como `db`. Solo necesita hacer una query 
extra. No necesita inyectar repositorios adicionales — usa el `db` directamente para hacer 
la query y agregar al batch.

### Contrato de la función (sin cambios de firma)
```kotlin
override suspend fun deleteSubject(userId: String, subjectName: String): Result<Unit>
```

## Global Constraints (AGENTS.md)
- Kotlin + Hilt + Firebase Firestore
- Operaciones de borrado compuestas deben usar `WriteBatch` para atomicidad ✓
- No tragar excepciones con catch vacío — al menos loguear
- No importar con wildcard (.*)
- Retornar `Result<T>` para operaciones write ✓

## Verification
Después de implementar, verificar que el código compila:
```bash
cd /home/analista/AndroidStudioProjects/NotiFast
./gradlew :app:compileDebugKotlin 2>&1 | tail -20
```

## Report File
Escribe tu reporte completo en:
`/home/analista/AndroidStudioProjects/NotiFast/.superpowers/sdd/briefs/task-1-report.md`

Incluye: STATUS (DONE/BLOCKED/NEEDS_CONTEXT), commit hash, resultado de compilación, y self-review.
