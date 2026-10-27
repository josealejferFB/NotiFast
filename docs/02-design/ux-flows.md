# Flujos de Experiencia de Usuario (UX Flows): NotiFast

Este documento detalla la arquitectura de interacción y los diagramas de flujo paso a paso para los cinco recorridos críticos del estudiante dentro de NotiFast.

---

## Flujo 1: Cuaderno de Estudio y Editor por Bloques (OneNote-like)

```mermaid
flowchart TD
    A["Pantalla de Materia / Dashboard"] --> B["Tocar 'Nuevo Apunte' (FAB)"]
    B --> C["Apertura de NoteEditorBottomSheet"]
    C --> D["Escribir Título y Párrafo Inicial"]
    D --> E{"¿Insertar Contenido Especial?"}
    E -- "Fórmula Matemática" --> F["Tocar Icono KaTeX en Docked Toolbar"]
    F --> G["Escribir Ecuación LaTeX en Diálogo"]
    G --> H["Renderizado de Ecuación KaTeX en Bloque"]
    E -- "Foto de Pizarra" --> I["Tomar Foto o Elegir de Galería"]
    I --> J["Optimización Local a JPEG 1600px"]
    J --> K{"¿Desea Dibujar o Anotar?"}
    K -- Sí --> L["Abrir Lienzo Vectorial de Dibujo"]
    L --> M["Trazar Subrayados con el Dedo"]
    M --> N["Guardar Capa de Dibujo sobre la Foto"]
    K -- No --> N
    E -- "Checklist / Tareas" --> O["Insertar Bloque de Casilla Verificable"]
    H --> P["Auto-guardado reactivo en Room"]
    N --> P
    O --> P
    P --> Q{"Pulsación de Botón Atrás"}
    Q -- "Teclado Desplegado" --> R["Nivel 1: Ocultar Teclado (Sheet Permanece)"]
    Q -- "Teclado Cerrado con Cambios" --> S["Nivel 2: Diálogo 'Descartar Cambios'"]
    Q -- "Sin Cambios Pendientes" --> T["Nivel 3: Cierre Suave del Sheet"]
    S -- "Confirmar Descarte" --> T
    S -- "Seguir Editando" --> D
```

### Características de Interacción Clave:
- **`DockedAccessoryToolbar`:** Barra de herramientas suspendida sobre el teclado mediante `Modifier.imePadding()`.
- **Three-Tier Back Handling:** El primer toque de atrás siempre repliega el teclado virtual sin cerrar la hoja ni disparar advertencias innecesarias.
- **Persistencia en `SavedStateHandle`:** Los borradores se mantienen vivos aunque Android destruya la actividad por falta de memoria mientras el usuario consulta un PDF en otra app.

---

## Flujo 2: Agenda Reactiva e Importación de Horarios UNERG (PDF DACE)

```mermaid
flowchart TD
    A["Pantalla Agenda Semanal"] --> B["Desplegar FAB Expandible"]
    B --> C["Seleccionar 'Importar Horario UNERG (PDF)'"]
    C --> D["Abrir Selector de Documentos de Android"]
    D --> E["Seleccionar comprobante DACE-UNERG"]
    E --> F["Ejecución UnergPdfScheduleParser (100% Offline)"]
    F --> G["Descompresión ASCII85 / Flate (zlib)"]
    G --> H["Mapeo de Escapes Octales ISO-8859-1"]
    H --> I["Filtrar materias presenciales (descartar 'NO PRESENCIAL')"]
    I --> J["Despliegue de UnergScheduleImportSheet (Vista Previa)"]
    J --> K["Revisar materias detectadas, secciones y bloques horarios"]
    K --> L{"¿Existen materias ya registradas?"}
    L -- Conflicto detectado --> M["Elegir estrategia: Reemplazar horarios o Mantener"]
    L -- Sin conflictos --> N["Confirmar Importación Masiva"]
    M --> N
    N --> O["Inserción transaccional en Room (Materias + Horarios)"]
    O --> P["Programación masiva de alarmas exactas (ExactAlarmScheduler)"]
    P --> Q["Actualización de ScheduleLiveStatusCard en Agenda"]
```

### Características de Interacción Clave:
- **Cero Entrada Manual:** Una inscripción de 7 materias con 18 bloques de clase se importa en menos de 2 segundos.
- **Asignación Semántica Inteligente:** El parser deduce automáticamente el color accesible y el icono (ej. *Cálculo* $\rightarrow$ `Calculate`, *Programación* $\rightarrow$ `Computer`).
- **Puntualidad Doze-Safe:** Cada clase presencial queda de inmediato agendada en `AlarmManager` para alertar 15 minutos antes.

---

## Flujo 3: Calificaciones Multiescala y Simulador Predictivo

```mermaid
flowchart TD
    A["Pantalla de Materia"] --> B["Pestaña 'Evaluaciones'"]
    B --> C["Tocar 'Nueva Evaluación'"]
    C --> D["Asignar Tipo, Fecha y Peso Porcentual (ej. 25%)"]
    D --> E["Guardar Evaluación en Estado Pendiente"]
    E --> F["Rendir Evaluación y Recibir Nota"]
    F --> G["Ingresar Calificación Obtenida (ej. 17 / 20)"]
    G --> H["Ejecución de GradePolicy en Dominio"]
    H --> I["Cálculo de Aporte Ponderado y Promedio Acumulado"]
    I --> J["Abrir 'Simulador de Salud Académica'"]
    J --> K["Fijar Meta Deseada (ej. 15 puntos)"]
    K --> L["GradePolicy resuelve la Nota Requerida en lo restante"]
    L --> M{"Evaluación del Resultado"}
    M -- "Nota $\le$ Escala Máxima" --> N["Mostrar: 'Necesitas 13.5 en el 40% restante' (Verde)"]
    M -- "Meta ya Aprobada" --> O["Mostrar: '¡Materia ya aprobada con éxito!' (Azul)"]
    M -- "Nota > Escala Máxima" --> P["Mostrar: 'Meta matemáticamente inalcanzable' (Ámbar)"]
```

### Características de Interacción Clave:
- **Rigor IEEE 754:** Cero errores de redondeo acumulado.
- **Tipografía Monoespaciada Tabular:** Todos los cálculos se muestran en `JetBrains Mono` para comparación visual inmediata.
- **Empatía Cognitiva:** Materias sin evaluar muestran `—` en lugar de `0.0` para no generar ansiedad infundada.

---

## Flujo 4: Respaldo Privado en Google Drive (`CloudHubSheet`)

```mermaid
flowchart TD
    A["Icono de Nube en Barra Superior"] --> B["Despliegue de CloudHubSheet"]
    B --> C{"¿Cuenta Google Vinculada?"}
    C -- No --> D["Tocar 'Conectar Google Drive'"]
    D --> E["Google Credential Manager + Authorization Client"]
    E --> F["Otorgar permiso al scope 'drive.appdata'"]
    F --> G["Verificación de token y perfil"]
    C -- Sí --> G
    G --> H["Cálculo de Hashes SHA-256 canónicos por materia"]
    H --> I["Listado con chips: Sincronizada / Cambios / Solo Local / Solo Nube"]
    I --> J{"Acción del Estudiante"}
    J -- "Subir Materias Seleccionadas" --> K["Compresión ZIP (.nfbak) + Imágenes"]
    K --> L["Envío Multipart Streaming a Drive REST v3 (OkHttp)"]
    L --> M["Actualización de subject_cloud_state en Room"]
    J -- "Restaurar desde Drive" --> N["Descarga del archivo .nfbak"]
    N --> O{"¿Existe materia local con el mismo nombre?"}
    O -- Sí --> P["Diálogo: Sobrescribir o Agregar como 'Nombre (1)'"]
    O -- No --> Q["Restauración limpia en Room"]
    P --> Q
    Q --> R["Notificación de Sincronización Exitosa"]
```

### Características de Interacción Clave:
- **Privacidad Total:** Los datos viajan a la carpeta oculta `appDataFolder` del propio Drive del alumno; ningún servidor ajeno tiene acceso.
- **Control Granular:** El estudiante puede elegir subir una sola materia, todas o solo restaurar las notas de un semestre anterior.

---

## Flujo 5: Transferencia P2P Offline (.notifast y QR)

```mermaid
flowchart TD
    A["Detalle de Materia"] --> B["Menú de Opciones -> 'Compartir Materia'"]
    B --> C{"Método de Transferencia"}
    C -- "Código QR (Sin Internet)" --> D["Generar SubjectPackage JSON comprimido"]
    D --> E["Codificación QR de alta densidad con ZXing"]
    E --> F["Mostrar Código QR en Pantalla del Teléfono Emisor"]
    F --> G["Compañero escanea con NotiFast (Google Code Scanner)"]
    C -- "Archivo .notifast" --> H["Serializar a GZIP + Base64 (.notifast)"]
    H --> I["Compartir vía Bluetooth / WhatsApp / Nearby"]
    I --> J["Compañero abre el archivo .notifast"]
    G --> K["Recepción y Descompresión del Paquete"]
    J --> K
    K --> L["Resolución de Colisión de Nombres (ResolveSubjectNameUseCase)"]
    L --> M["Inserción directa en Room SQLite"]
    M --> N["Materia disponible de inmediato en el teléfono receptor"]
```

### Características de Interacción Clave:
- **100% Sin Infraestructura:** Funciona en sótanos universitarios sin red celular ni Wi-Fi.
- **Portabilidad Total:** Incluye apuntes, horarios y plan de evaluación en un solo archivo ligero.
