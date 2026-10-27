# ADR-0006: Transferencia P2P Offline de Materias vía Paquete `.notifast` y Código QR

- **Estado:** Aceptado
- **Fecha:** 2026-10-08
- **Módulo:** Compartición P2P (`domain/usecase/share`, `data/backup`, `presentation/feature/share`)

---

## Contexto y Planteamiento del Problema

Al iniciar un semestre universitario o un año escolar, los estudiantes suelen organizarse creando comisiones o delegados que transcriben los cronogramas, las fechas de evaluaciones y los horarios de cada cátedra. Sin embargo, no existía un mecanismo para que un estudiante compartiera esa materia configurada a sus compañeros de curso de forma directa en el aula de clases sin depender de conexión a internet o de un servidor central.

Las soluciones habituales (compartir capturas de pantalla o documentos Word/PDF) obligaban a cada alumno receptor a transcribir nuevamente todos los horarios y fechas a mano.

## Decisión

Se decide diseñar un protocolo de intercambio peer-to-peer (P2P) **100% offline y desacoplado de la nube**:

1. **Estructura Canónica `SubjectPackage`:** Objeto inmutable que agrupa la materia, su sección, color, icono, bloques de horario semanal, evaluaciones con sus ponderaciones, apuntes con bloques de texto y ficha del profesor.
2. **Codec de Compresión Ligero (`SubjectPackageCodec`):**
   - Serialización de `SubjectPackage` a JSON estandarizado.
   - Compresión en memoria mediante `GZIPOutputStream` para reducir el tamaño en más de un 70%.
   - Codificación en cadena `Base64` apta para URLs y códigos QR.
3. **Doble Canal de Transferencia Offline:**
   - **Canal Archivo `.notifast`:** Guardado o envío del archivo binario comprimido mediante la hoja de compartir del sistema Android (Bluetooth, Nearby, almacenamiento USB o mensajería).
   - **Canal Código QR Offline:** Proyección del paquete codificado en la pantalla del emisor mediante un código QR generado con **ZXing 3.5.3** y escaneo instantáneo con **Google Play Services Code Scanner 16.1.0** en el teléfono receptor.
4. **Resolución Determinista de Colisiones (`ResolveSubjectNameUseCase`):** Al importar una materia cuyo nombre ya exista en el dispositivo receptor, el sistema consulta al estudiante si desea **Sobrescribir** o **Crear una nueva asignatura correlativa** (`Cálculo I (1)`).

## Alternativas Consideradas

| Alternativa | Motivo de Descarte |
|---|---|
| **Servidor Central de Enlace (API REST de sincronización)** | Exige conexión a internet en el aula, incurre en costos de servidor y requiere crear cuentas en la nube. |
| **Exportación Únicamente a Documento PDF** | Un PDF no es interpretable directamente por la aplicación; el estudiante tendría que volver a transcribir los datos. |
| **Wi-Fi Direct / Conexión Socket P2P Propietaria** | Requiere emparejamiento manual complejo, solicitudes de permisos de ubicación invasivos en Android y suele fallar entre dispositivos de distintas marcas. |

## Consecuencias

### Positivas
- ✅ **Intercambio Instantáneo en Aula:** Un curso entero de 40 estudiantes puede inscribir una materia escaneando el código QR proyectado en la pizarra en pocos segundos y en modo avión.
- ✅ **Cero Infraestructura Externa:** No depende de ningún servidor ni consume cuotas de red.
- ✅ **Seguridad y Aislamiento:** El codec valida estrictamente la estructura del JSON antes de insertar cualquier dato en Room, evitando inyecciones o registros corruptos.
- ✅ **Integridad:** Se preservan intactos los horarios, fórmulas y ponderaciones de la materia original.

### Negativas y Mitigaciones
- ⚠️ *Límite de capacidad física del Código QR:* Paquetes de materias con docenas de imágenes pesadas no caben en una matriz QR estándar. Mitigado excluyendo imágenes fotográficas binarias del QR (se transmiten como enlaces o mediante el archivo `.notifast`) y reservando el QR para metadatos, horarios, evaluaciones y apuntes de texto/Markdown.
