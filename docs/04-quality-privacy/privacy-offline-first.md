# Privacidad Radical y Arquitectura Offline-First: NotiFast

| Metadato | Valor |
|---|---|
| **Principio Rector** | Soberanía Total de Datos del Estudiante (Data Ownership by Design) |
| **Almacenamiento Centralizado Ajeno** | **0 bytes** (Cero servidores propietarios, cero bases de datos compartidas) |
| **Copia en la Nube** | Exclusivamente en el Google Drive personal del usuario (`appDataFolder`) |
| **Telemetría** | Exclusivamente datos técnicos anónimos de estabilidad (Firebase Crashlytics) |
| **Documentos Relacionados** | [Visión del Producto](../00-vision/product-vision.md) · [Estrategia de Pruebas](./testing-strategy.md) · [Notas Legales](../05-legal/legal-notes.md) |

---

## 1. Filosofía de Privacidad de NotiFast

Las herramientas de productividad académica convencionales (como Notion, Google Classroom o plataformas LMS institucionales) almacenan la totalidad de los apuntes, horarios, hábitos de estudio y calificaciones de los estudiantes en servidores corporativos remotos, sujetos a minería de datos, caídas de servicio o brechas de seguridad masivas.

NotiFast adopta una postura ética y técnica de **Privacidad Radical**:
> **"Tus apuntes, tus materias, tus calificaciones y tus rutinas pertenecen exclusivamente a ti y a tu dispositivo. NotiFast no sabe qué materias cursas ni qué notas obtienes."**

---

## 2. Los Cuatro Pilares del Blindaje de Privacidad

```
┌────────────────────────────────────────────────────────────────────────┐
│                   LOS 4 PILARES DE PRIVACIDAD NOTIFAST                 │
├───────────────────┬───────────────────┬────────────────────────────────┤
│ 1. CERO BASE DE   │ 2. SANDBOX LOCAL  │ 3. DRIVE PRIVADO AISLADO       │
│    DATOS AJENA    │    SEGURO         │    (appDataFolder)             │
│ Cero Firestore,   │ Room SQLite en el │ Respaldo cifrado en tránsito   │
│ cero backend en   │ almacenamiento    │ hacia la cuenta de Google del  │
│ la nube propio.   │ privado interno.  │ alumno, invisible a terceros.  │
├───────────────────┴───────────────────┴────────────────────────────────┤
│ 4. TELEMETRÍA ANÓNIMA SIN PII                                          │
│    Firebase Crashlytics limitado a logs de excepción técnica.          │
│    Cero rastreo de nombres de materias, profesores o calificaciones.   │
└────────────────────────────────────────────────────────────────────────┘
```

### Pilar 1: Cero Servidores Propietarios y Cero Firestore
- NotiFast no posee servidores intermedios, clústeres de base de datos ni cuentas de Firebase Firestore para almacenar registros de usuario.
- No existe ningún registro de inicio de sesión obligatorio (login) que vincule al estudiante con un perfil en la nube para poder usar la aplicación.

### Pilar 2: Almacenamiento Local en Sandbox Seguro de Android
- Todas las bases de datos (`notifast.db`), preferencias (`DataStore`) y fotografías adjuntas a las notas (`filesDir/images/`) residen dentro del almacenamiento privado de la aplicación asignado por el sistema operativo Linux/Android.
- Ninguna otra aplicación instalada en el dispositivo tiene permisos para acceder a esta información.

### Pilar 3: Copia en la Nube Privada y Oculta (`appDataFolder`)
- Cuando el estudiante decide voluntariamente activar la copia de seguridad, los datos viajan cifrados en tránsito mediante HTTPS/TLS directamente hacia su propia cuenta de Google Drive.
- El alcance solicitado (`drive.appdata`) almacena los archivos en la carpeta oculta de la aplicación, lo que garantiza que:
  - Ni el equipo de NotiFast ni ningún tercero tienen acceso a los archivos.
  - Otras aplicaciones con permisos para Google Drive común no pueden leer la carpeta de NotiFast.
  - El estudiante no puede borrar ni corromper accidentalmente los archivos desde la web de Drive.

### Pilar 4: Transferencia P2P Local sin Huella de Red
- El intercambio de materias mediante códigos QR o paquetes `.notifast` ocurre de forma estrictamente local entre los dos dispositivos mediante señales ópticas (pantalla-cámara) o transferencias directas de archivo (Bluetooth / Sharesheet).
- No se emite ninguna llamada a internet para registrar quién compartió o quién recibió una materia.

---

## 3. Telemetría Técnica y Protección contra PII

Para asegurar la calidad del software (identificación temprana de caídas o incompatibilidades en versiones de Android), NotiFast incluye Firebase Crashlytics y Firebase Analytics bajo restricciones severas:

1. **Cero Información de Identificación Personal (No PII):** Está estrictamente prohibido adjuntar nombres de estudiantes, correos electrónicos, títulos de apuntes, nombres de materias o calificaciones en los metadatos de Crashlytics.
2. **Reportes Técnicos Anónimos:** Únicamente se recolectan trazas de pila (*stacktraces*) de excepciones no controladas, modelo del dispositivo, versión del sistema operativo y métricas agregadas de rendimiento de Compose.
3. **Opt-Out Respetado:** Si el usuario utiliza la app en modo offline o bloquea conexiones de telemetría a nivel de DNS/firewall, la aplicación no altera en lo absoluto su funcionamiento.

---

## 4. Derecho al Olvido y Soberanía del Usuario

NotiFast garantiza el control absoluto del ciclo de vida de la información:

- **Borrado en Cascada Inmediato:** Al eliminar una materia, nota o profesor, el borrado en Room SQLite y la eliminación física de las imágenes en disco se ejecutan en el acto (`withTransaction`).
- **Borrado en la Nube con Un Solo Toque:** En el diálogo de eliminación, el estudiante puede elegir *"Borrar solo local"* o *"Borrar en teléfono y en Google Drive"*, invocando `DriveRestClient.deleteFile` para eliminar la copia en la nube de forma irreversible.
- **Desvinculación Limpia:** El usuario puede desvincular su cuenta de Google desde Ajustes en cualquier momento sin que se altere ni un solo byte de sus datos locales.
