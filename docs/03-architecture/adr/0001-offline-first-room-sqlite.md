# ADR-0001: Arquitectura 100% Offline-First con Room SQLite y DataStore

- **Estado:** Aceptado
- **Fecha:** 2026-10-05
- **Módulo:** Capa de Datos (`data/local`, `data/repository`)

---

## Contexto y Planteamiento del Problema

La versión inicial del prototipo de NotiFast dependía de Firebase Firestore (`users/{uid}/...`) y Firebase Auth para la persistencia y autenticación de usuarios. En el entorno real de las aulas y laboratorios universitarios (como el Área de Ingeniería de Sistemas de la UNERG), la conectividad a internet móvil es deficiente, inestable o inexistente. 

Las operaciones con `.await()` en Firestore generaban bloqueos con spinners infinitos; la creación de materias requería consultas remotas (`get()`) que fallaban sin caché; y el primer uso de la aplicación era imposible sin red para autenticar una cuenta de Google o correo. Esta dependencia de la nube violaba el propósito de NotiFast de ser un cuaderno ágil y confiable.

## Decisión

Se decide migrar la arquitectura de NotiFast a un modelo **100% Offline-First incondicional**:

1. **Room SQLite como Fuente de Verdad Primaria:** Se adopta Room SQLite (`NotiFastDatabase`, esquema evolucionado de v1 a v9) para todas las entidades del dominio: materias, notas, evaluaciones, horarios, docentes y documentos.
2. **Reactividad Local:** Los DAOs devuelven `Flow<List<T>>`, logrando que la interfaz de usuario se actualice de inmediato ante cualquier inserción o mutación local sin depender de listeners remotos.
3. **Identidad Local Desacoplada:** El primer arranque del estudiante crea un perfil local en Room con un identificador UUID y nombre/apellido, sin requerir contraseñas ni autenticación externa. La sesión activa se persiste en Jetpack DataStore Preferences.
4. **Almacenamiento Local de Archivos:** Las imágenes capturadas se almacenan directamente en el sandbox interno de la app (`filesDir/images/`), aplicando borrado de archivos huérfanos al actualizar o eliminar notas.
5. **Borrado Atómico en Cascada:** Se implementa borrado transaccional mediante `RoomDatabase.withTransaction`, garantizando que al eliminar una materia se borren limpiamente sus apuntes, evaluaciones y horarios en una sola operación atómica.

## Alternativas Consideradas

| Alternativa | Motivo de Descarte |
|---|---|
| **Firestore con Caché Offline Habilitada** | La caché de Firestore no garantiza consistencia relacional ni transaccionalidad atómica; las escrituras con `.await()` quedan bloqueadas en segundo plano y el SDK añade un peso desproporcionado al APK. |
| **SQLDelight** | Aunque es multiplataforma, no ofrece la integración directa y madura con Coroutines Flow, Hilt y Room Testing que ya proporciona el ecosistema oficial de Android Jetpack. |
| **ObjectBox / Realm** | Motores NoSQL con dependencias nativas C++ propietarias que complican la minificación con R8 y no se alinean con los estándares académicos de bases de datos relacionales exigidos en la tesis. |

## Consecuencias

### Positivas
- ✅ **Latencia Cero:** Apertura instantánea de apuntes y horarios en menos de 50 ms.
- ✅ **Independencia Total de Red:** La aplicación opera al 100% en modo avión.
- ✅ **Privacidad por Diseño:** Ningún dato personal del estudiante sale del dispositivo sin su consentimiento explícito.
- ✅ **Integridad Referencial:** Claves foráneas con borrado en cascada y claves compuestas sin riesgo de registros huérfanos.

### Negativas y Mitigaciones
- ⚠️ *Riesgo de pérdida de datos si el usuario pierde o formatea su dispositivo:* Mitigado mediante el desarrollo de la Fase de Respaldo Privado en Google Drive `appDataFolder` ([ADR-0002](./0002-google-drive-appdatafolder-cloud-backup.md)) y el Paquete P2P Offline ([ADR-0006](./0006-p2p-offline-pack-qr-transfer.md)).
