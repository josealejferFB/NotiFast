# ADR-0002: Respaldo Privado Modular en Google Drive `appDataFolder` con OkHttp

- **Estado:** Aceptado
- **Fecha:** 2026-10-15
- **Módulo:** Capa de Datos (`data/backup`, `data/remote/drive`, `domain/usecase/cloud`)

---

## Contexto y Planteamiento del Problema

Tras adoptar la arquitectura 100% Offline-First en Room SQLite ([ADR-0001](./0001-offline-first-room-sqlite.md)), los estudiantes expresaron la necesidad crítica de resguardar sus apuntes ante el extravío, formateo o reemplazo de su teléfono móvil. Sin embargo, un backend propio (servidores dedicados, bases de datos PostgreSQL o instancias en la nube) implicaba costos operativos insostenibles para un proyecto gratuito y riesgos severos de privacidad (almacenamiento centralizado de apuntes y notas de estudiantes).

Se requería una solución de respaldo en la nube que fuera de **costo cero permanente**, **privada por diseño**, con **control modular por materia** y sin añadir sobrecarga excesiva al tamaño de la aplicación.

## Decisión

Se decide implementar un sistema de respaldo modular basado en el propio **Google Drive del usuario** utilizando el espacio oculto **`appDataFolder`**:

1. **Autenticación sin Firebase:** Identidad gestionada mediante **Google Credential Manager** (`androidx.credentials`) y obtención de token de acceso OAuth2 mediante `Identity.getAuthorizationClient()` solicitando exclusivamente el alcance `https://www.googleapis.com/auth/drive.appdata`.
2. **Cliente REST v3 Delgado sobre OkHttp:** Se descarta el SDK oficial de Google Drive en favor de un cliente ligero implementado en `DriveRestClient.kt` sobre OkHttp 4.12.0, ejecutando llamadas en streaming multipart directo (`files.create`, `files.list`, `files.get?alt=media`, `files.delete`).
3. **Copia Modular por Asignatura (`.nfbak`):** Cada materia se empaqueta en un archivo ZIP independiente que contiene su definición normalizada en JSON y sus imágenes asociadas optimizadas a 1600 px y calidad JPEG 80 (sin metadatos EXIF).
4. **Diffing Incremental SHA-256:** Se implementa `ContentHasher.kt` para calcular el hash determinista de cada materia local, permitiendo saber en milisegundos si existen *«Cambios sin subir»* sin tener que descargar o comparar archivos pesados en la red.
5. **Resolución de Conflictos Humana:** Al restaurar una materia existente en el dispositivo, el sistema nunca sobrescribe en silencio; presenta al usuario un diálogo accesible para elegir entre **Sobrescribir** o **Agregar con sufijo correlativo** (`Cálculo (1)`).

## Alternativas Consideradas

| Alternativa | Motivo de Descarte |
|---|---|
| **Backend Centralizado Propio (Supabase / AWS / Firebase)** | Genera costos recurrentes de infraestructura, costos de almacenamiento por gigabyte y riesgos de seguridad al custodiar datos privados de terceros. |
| **SDK Oficial `google-api-services-drive`** | Agrega más de 15 MB al binario final, arrastra dependencias obsoletas de `google-http-client` y genera advertencias severas con el minificador R8. |
| **Exportación Únicamente Manual a Descargas** | Poca adopción por parte de los estudiantes, riesgo de borrado accidental en carpetas públicas y dificultad para transferir entre dispositivos sin cables. |

## Consecuencias

### Positivas
- ✅ **Costo Cero Permanente:** Utiliza la cuota de almacenamiento que Google ya provee gratuitamente a cada estudiante en su cuenta.
- ✅ **Privacidad Total:** La aplicación solo puede acceder a su propia carpeta oculta; no puede ver los archivos personales de Drive del usuario, y ningún tercero puede ver las notas de NotiFast.
- ✅ **Bajo Consumo de Datos Móviles:** El diffing SHA-256 asegura que solo se transmitan las materias que realmente fueron modificadas.
- ✅ **Binario Optimizado:** Cero incremento desmedido en el tamaño del APK.

### Negativas y Mitigaciones
- ⚠️ *Dependencia de los Servicios de Google Play:* Mitigado manteniendo la app 100% operativa en modo offline y ofreciendo la exportación/importación P2P mediante paquetes `.notifast` ([ADR-0006](./0006-p2p-offline-pack-qr-transfer.md)) para dispositivos sin Google Play Services.
