# Historias de Usuario: NotiFast

> **Formato:** *Como [rol de usuario], quiero [acción / capacidad], para [beneficio / valor esperado].*  
> **Criterios de Aceptación:** Especificados en formato formal Gherkin (`Dado que... / Cuando... / Entonces... / Y...`).  
> **Trazabilidad:** Cada historia referencia directamente los requisitos del [PRD Maestro](./PRD.md).

---

## Épica 1: Apuntes y Cuaderno por Bloques (OneNote-like)

### US-NOTE-01: Creación de apunte estructurado con Markdown y KaTeX · `FR-NOTE-001`, `FR-NOTE-002`
**Como** estudiante universitario de ingeniería,  
**quiero** redactar notas combinando texto enriquecido, formato Markdown y bloques de fórmulas matemáticas KaTeX,  
**para** documentar teoremas, código y demostraciones en un solo lugar estructurado.

```gherkin
Dado que me encuentro en la pantalla de una materia
Cuando pulso el botón de crear nuevo apunte y selecciono el bloque "Fórmula Matemática"
Y escribo una expresión LaTeX como "\int_0^\infty e^{-x^2} dx = \frac{\sqrt{\pi}}{2}"
Entonces el editor renderiza la ecuación matemática formateada con precisión tipográfica
Y el apunte se guarda de forma reactiva en la base de datos local Room.
```

### US-NOTE-02: Anotación visual sobre fotos de pizarra · `FR-NOTE-003`, `FR-NOTE-004`
**Como** estudiante de medicina en aula de clases,  
**quiero** capturar la foto de una lámina o pizarra anatómica y dibujar trazos a mano alzada sobre ella,  
**para** resaltar partes clave de la explicación docente sin salir de la app.

```gherkin
Dado que estoy editando un apunte dentro del modal del editor
Cuando adjunto una fotografía desde la cámara y toco el botón "Dibujar"
Y realizo trazos de color sobre la imagen con el dedo
Entonces los trazos se superponen vectorialmente sobre la fotografía
Y la imagen anotada queda enlazada al bloque de la nota en el almacenamiento local.
```

### US-NOTE-03: Resiliencia ante cierres accidentales (Three-Tier Back) · `FR-NOTE-006`, `FR-NOTE-007`
**Como** estudiante que toma apuntes a ritmo acelerado,  
**quiero** que el sistema no descarte mis cambios si pulso involuntariamente el botón o gesto Atrás,  
**para** evitar la pérdida accidental de notas de clase.

```gherkin
Dado que tengo el editor de notas abierto con cambios sin guardar y el teclado virtual en pantalla
Cuando pulso el botón Atrás por primera vez
Entonces el teclado se oculta, la hoja modal permanece abierta y el borrador no se pierde

Dado que el teclado ya está cerrado y existen cambios sin guardar en la nota
Cuando pulso Atrás o toco fuera de la hoja modal
Entonces se muestra un diálogo de confirmación accesible preguntando si deseo descartar los cambios.
```

---

## Épica 2: Materias y Directorio Docente

### US-SUBJ-01: Registro de materia con Sección y Color Accesible · `FR-SUBJ-001`
**Como** estudiante con múltiples asignaturas semestrales,  
**quiero** catalogar cada materia indicando su código, su sección específica y un color accesible,  
**para** distinguir rápidamente mis cátedras tanto en la agenda como en los apuntes.

```gherkin
Dado que me encuentro en el panel principal
Cuando abro el formulario de creación de materia
Y completo el nombre "Cálculo III", sección "Sección 02" y elijo el color índigo
Entonces la materia se almacena con clave única insensible a mayúsculas
Y aparece de inmediato en la cuadrícula Bento Grid con su chip distintivo de sección.
```

### US-PROF-01: Contacto directo con el profesor vía WhatsApp · `FR-PROF-001`, `FR-PROF-002`
**Como** estudiante que requiere asesoría académica,  
**quiero** acceder al número telefónico y oficina del docente para abrir un chat de WhatsApp con un toque,  
**para** consultar dudas de la cátedra con un mensaje formal preformateado.

```gherkin
Dado que estoy visualizando la pestaña "Profesor" dentro del detalle de una materia
Cuando toco el botón "Contactar por WhatsApp"
Entonces la aplicación abre WhatsApp directamente en el chat del profesor
Con el texto preconfigurado: "Hola profesor/a [Nombre], le escribo como estudiante de la materia [NombreMateria]..."
```

---

## Épica 3: Agenda Semanal y Parser PDF UNERG

### US-SCHED-01: Importación automática de horario en PDF de DACE-UNERG · `FR-SCHED-005`, `FR-SCHED-006`
**Como** estudiante de la UNERG inscrito en el período activo,  
**quiero** cargar mi comprobante de horario oficial en PDF descargado de DACE,  
**para** que NotiFast extraiga e inscriba automáticamente todas mis materias y bloques de clase sin transcribir a mano.

```gherkin
Dado que tengo el archivo PDF "Horario_de_Clases_XXXXX.pdf" en las descargas de mi dispositivo
Cuando selecciono "Importar Horario UNERG" en la pantalla de Agenda
Entonces el parser nativo offline decodifica los flujos ReportLab ASCII85 y Flate en menos de 100 ms
Y despliega la hoja de vista previa mostrando las materias presenciales, secciones y aulas detectadas
Y descarta automáticamente las materias marcadas como "NO PRESENCIAL" o a distancia.
```

### US-SCHED-02: Visualización de clase en vivo en Agenda · `FR-SCHED-003`, `FR-SCHED-004`
**Como** estudiante que se desplaza por el campus universitario,  
**quiero** ver de un vistazo qué clase tengo en curso y cuánto tiempo resta para que termine,  
**para** gestionar mis tiempos y llegar puntual a la siguiente aula.

```gherkin
Dado que son las 08:30 AM de un día martes y tengo clase de 08:00 a 10:00 AM
Cuando abro NotiFast o regreso a la aplicación en primer plano
Entonces la tarjeta "ScheduleLiveStatusCard" se actualiza reactivamente mostrando "En curso: Sistemas Distribuidos"
Y una barra de progreso que indica 25% transcurrido y "Resta 1h 30m en Aula 14".
```

---

## Épica 4: Evaluaciones y Simulador de Calificaciones Multiescala

### US-EVAL-01: Registro de evaluación con porcentaje ponderado · `FR-EVAL-001`, `FR-GRADE-003`
**Como** estudiante universitario,  
**quiero** agendar una entrega o examen fijando su ponderación porcentual dentro del plan de evaluación,  
**para** que el promedio refleje exactamente el peso real acordado con el docente.

```gherkin
Dado que estoy registrando una evaluación para "Física II"
Cuando asigno fecha límite, tipo "Primer Parcial" y ponderación "25%"
Y posteriormente ingreso la calificación obtenida de 16/20
Entonces el sistema calcula el aporte de 4.0 puntos sobre 20 en la materia
Y actualiza el promedio ponderado exacto acumulado en todas las vistas.
```

### US-GRADE-01: Simulación de nota requerida para aprobar · `FR-GRADE-005`
**Como** estudiante a mitad de semestre con notas pendientes,  
**quiero** simular cuánto puntaje necesito obtener en las evaluaciones restantes para alcanzar mi nota deseada,  
**para** planificar mis horas de estudio con base en una meta numérica realista.

```gherkin
Dado que he acumulado 7.5 puntos sobre un total de 50% ya evaluado en escala 0–20
Cuando ingreso a la herramienta de Salud Académica y fijo mi meta deseada en 14 puntos
Entonces el simulador me indica: "Necesitas promediar al menos 13.0 / 20 en el 50% restante para alcanzar tu meta"
Y si la meta supera los 20 puntos, el sistema alerta amablemente: "Meta inalcanzable con los puntos restantes".
```

---

## Épica 5: Motor de Notificaciones Proactivas Híbridas

### US-NOTIF-01: Alerta puntual previa al inicio de clase (Doze-Safe) · `FR-NOTIF-001`, `FR-NOTIF-003`
**Como** estudiante con el teléfono en el bolsillo o en reposo nocturno,  
**quiero** recibir una notificación prioritaria 15 minutos antes de que empiece mi primera clase,  
**para** prepararme y trasladarme al salón a tiempo sin que el modo ahorro de energía de Android suspenda la alerta.

```gherkin
Dado que tengo configurada la preferencia de alerta de clases a 15 minutos antes
Cuando faltan exactamente 15 minutos para las 07:00 AM y el teléfono está en reposo (Doze Mode)
Entonces ExactAlarmScheduler activa la alarma mediante "AlarmManager.setExactAndAllowWhileIdle()"
Y suena la notificación en el canal de alta prioridad mostrando el nombre de la materia y el aula.
```

### US-NOTIF-02: Resumen matutino diario · `FR-NOTIF-002`
**Como** estudiante al despertar,  
**quiero** recibir a las 07:00 AM un resumen consolidado de mi día académico,  
**para** organizar mentalmente mis actividades escolares antes de salir de casa.

```gherkin
Dado que son las 07:00 AM y tengo las alertas diarias activas
Cuando WorkManager ejecuta el "DailySummaryWorker"
Entonces recibo una notificación estilo BigTextStyle detallando las clases de hoy y las evaluaciones de las próximas 48h.
```

---

## Épica 6: Transferencia P2P Offline (.notifast y QR)

### US-P2P-01: Compartir cátedra completa vía Código QR en el aula · `FR-P2P-001`, `FR-P2P-002`, `FR-P2P-003`
**Como** delegado de curso que organizó todo el cronograma de una materia,  
**quiero** generar un código QR en mi pantalla para que mis compañeros lo escaneen sin internet,  
**para** compartirles el programa de clases, profesor y fechas de exámenes en un instante.

```gherkin
Dado que pulso "Compartir Materia vía QR" en NotiFast
Cuando la aplicación genera el paquete binario comprimido y muestra el código QR ZXing
Y mi compañero apunta su cámara con el escáner de NotiFast en su propio teléfono en modo avión
Entonces su aplicación decodifica el paquete .notifast
Y le ofrece incorporar la materia completa con todos sus datos en su base de datos local.
```

---

## Épica 7: Respaldo Privado en Google Drive

### US-CLOUD-01: Respaldo modular hacia la carpeta privada de Google Drive · `FR-CLOUD-001`, `FR-CLOUD-003`, `FR-CLOUD-004`
**Como** estudiante que desea resguardar su cuaderno digital,  
**quiero** sincronizar voluntariamente mis materias hacia mi cuenta personal de Google Drive en un área oculta,  
**para** evitar perder mis apuntes si extravío o cambio de teléfono, sin exponer mis datos a servidores de terceros.

```gherkin
Dado que vinculo mi cuenta de Google mediante el Credential Manager en la hoja "CloudHubSheet"
Cuando selecciono las materias que deseo respaldar y toco "Subir a Google Drive"
Entonces NotiFast genera el archivo ZIP optimizado (.nfbak) con las notas, fotos comprimidas y registros
Y lo almacena en "appDataFolder" de mi propio Drive
Y el indicador de la materia cambia al estado "Sincronizada" mediante la verificación de hash SHA-256.
```

---

## Épica 8: Sistema de Temas y Lienzos Táctiles (WCAG AAA)

### US-THEME-01: Personalización con contraste algorítmico garantizado · `FR-THEME-001`, `FR-THEME-003`
**Como** estudiante que lee apuntes durante sesiones nocturnas de estudio,  
**quiero** seleccionar un lienzo de papel descanso visual (como Menta o Polar) o personalizar mi propio tono Hex,  
**para** estudiar cómodamente sin fatiga visual y con contraste accesible certificado.

```gherkin
Dado que ingreso al Estudio de Temas en Ajustes
Cuando elijo el lienzo de fondo "Papiro" y selecciono un color de énfasis en el slider continuo Hex/HSL
Entonces el motor algorítmico "ThemePaletteGenerator" recalcula dinámicamente la luminancia de textos y botones
Y garantiza en tiempo real una relación de contraste superior a 4.5:1 (WCAG AAA)
Sin alterar el esquema de colores del sistema operativo del dispositivo.
```

---

## Épica 9: Área de Estudio (Study Hub)

### US-STUDY-01: Sesión de estudio estructurada con temporizador Pomodoro · `FR-STUDY-001`
**Como** estudiante preparándome para una semana de exámenes finales,  
**quiero** activar un temporizador de enfoque de 25 minutos con pausas cortas,  
**para** mantener la concentración profunda sin salir de mi cuaderno de notas.

```gherkin
Dado que abro el "Área de Estudio" desde la barra de navegación
Cuando inicio el temporizador Pomodoro de 25 minutos vinculado a la materia "Estructuras de Datos"
Entonces el contador visual avanza con vibración háptica suave y alertas al finalizar cada bloque
Y registra el cumplimiento de mi meta de estudio diaria en el panel de productividad.
```
