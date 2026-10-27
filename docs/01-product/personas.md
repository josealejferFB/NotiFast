# Arquetipos de Usuario (Personas): NotiFast

Este documento define los perfiles arquetípicos de usuarios que sustentan las decisiones de diseño, flujos y arquitectura de NotiFast.

---

## P1: Alejandro, estudiante universitario de Ingeniería en Informática (7mo semestre, UNERG)

- **Perfil:** 21 años, vive en San Juan de los Morros (Guárico, Venezuela). Cursa asignaturas avanzadas: Redes, Sistemas Distribuidos, Base de Datos II y Electiva Profesional.
- **Dispositivo:** Xiaomi Redmi Note 11 (Android 12), plan de datos móviles limitado y con frecuentes caídas de señal en las aulas del Área de Ingeniería de Sistemas (AIS).
- **Necesidades clave:**
  - Tomar notas técnicas con fórmulas matemáticas complejas y fragmentos de código.
  - Saber con exactitud matemática cuánto necesita sacar en el 40% restante de cada materia para no arriesgar su promedio acumulado (escala 0–20).
  - Cargar rápidamente su horario oficial de DACE-UNERG en PDF sin pasar 45 minutos copiando horas y salones a mano.
  - Que la app funcione de forma inmediata en sótanos y aulas sin cobertura.
- **Frustraciones:**
  - Aplicaciones que se congelan con pantallas de carga infinitas cuando se va el internet.
  - Calcular promedios a mano y equivocarse con los pesos de los laboratorios frente a la teoría.
- **Criterio de Éxito:** Abrir NotiFast en el aula, ver su próxima clase en la tarjeta de progreso en vivo, registrar una nota en 5 segundos y saber que todo está seguro en su base de datos local.

---

## P2: Valentina, estudiante foránea de Medicina (3er año, UNERG)

- **Perfil:** 20 años, originaria de Puerto Cabello, vive en una residencia estudiantil cerca de la sede de Medicina de la UNERG. Carga académica muy pesada: Semiología, Fisiopatología, Farmacología y guardias clínicas hospitalarias.
- **Dispositivo:** Samsung Galaxy A32 (Android 13).
- **Necesidades clave:**
  - Fotografiar láminas de anatomía o pizarras médicas en el hospital y poder subrayar detalles o escribir notas directamente sobre la foto.
  - Alarmas exactas que suenen 15 minutos antes de sus clases de 07:00 AM, sin que el sistema operativo las apague por ahorro de batería.
  - Modo visual descansado (lienzo *Menta* o *Lavanda*) para estudiar de noche sin quemarse los ojos.
  - Tener los contactos de los doctores/profesores a mano para consultas de rotaciones vía WhatsApp.
- **Frustraciones:**
  - Galería del teléfono inundada de fotos de pizarras mezcladas con fotos familiares sin ningún orden por materia.
  - Olvidar la fecha límite de entrega de una historia clínica por no recibir el recordatorio a tiempo.
- **Criterio de Éxito:** Tener sus apuntes organizados por materia con dibujos sobre las fotos, alertas fiables que nunca fallan y respaldo privado en su Google Drive para no perder nada si cambia de teléfono.

---

## P3: Carlos, estudiante de Bachillerato / Educación Media General (4to año)

- **Perfil:** 16 años, cursa 4to año en un liceo público/privado. Maneja 11 materias simultáneas: Matemática, Física, Química, Biología, Historia, Castellano, etc.
- **Dispositivo:** Motorola Moto G22 (Android 12, gama de entrada).
- **Necesidades clave:**
  - Interfaz simple y atractiva que no parezca un software aburrido de oficina.
  - Aliviar el estrés escolar viendo con claridad qué materias están "Al día" y cuáles están "En riesgo".
  - Personalizar los colores de su cuaderno para que coincida con sus gustos personales (ej. paleta *Cyber Nocturno* o *Campus Verde*).
  - Compartir materias y horarios con sus compañeros de clases mediante código QR sin gastar saldo.
- **Frustraciones:**
  - No saber si aprobó la materia hasta el final del lapso cuando el profesor entrega la boleta.
  - Interfaces complejas llenas de configuraciones que no entiende.
- **Criterio de Éxito:** Saber siempre qué examen tiene mañana gracias a la notificación proactiva y ver sus notas en verde con la certeza de que ya pasó la materia.

---

## P4: Prof. Ricardo Mendoza, docente universitario de Cátedra y Laboratorio

- **Perfil:** 48 años, profesor del Área de Ingeniería de Sistemas de la UNERG. Dicta la materia "Algoritmos y Estructuras de Datos" a tres secciones distintas.
- **Habilidad digital:** Media-alta; atiende consultas de estudiantes vía WhatsApp y correo institucional.
- **Necesidades clave:**
  - Que los estudiantes sepan con precisión las fechas de entrega y porcentajes del plan de evaluación para que no haya quejas ni confusiones al cierre de semestre.
  - Que los alumnos puedan exportar o transferirse el programa y cronograma de clases oficial entre ellos de forma digital.
  - Recibir mensajes de WhatsApp identificando claramente el nombre del alumno, la materia y la duda puntual sin rodeos.
- **Frustraciones:**
  - Estudiantes preguntando repetidamente "¿cuándo es la entrega?" o "¿cuánto pesa este taller?".
  - Mensajes de WhatsApp anónimos diciendo "¿profe qué nota saqué?" sin indicar sección ni materia.
- **Criterio de Éxito:** Proporcionar al delegado de curso el plan oficial y que todos los estudiantes lo tengan en NotiFast, recibiendo mensajes de consulta estructurados y claros vía WhatsApp.

---

## P5: Ing. Jurado Evaluador / Tribunal de Tesis de Grado (UNERG - AIS)

- **Perfil:** Docentes e ingenieros miembros del Comité Evaluador de Tesis en la Escuela de Ingeniería en Informática de la UNERG.
- **Rol evaluador:** Juzgan la rigurosidad científica, metodológica y técnica de la aplicación como Proyecto Especial de Grado.
- **Aspectos que auditan:**
  - Arquitectura de software: Clean Architecture estricta, separación de capas (Domain, Data, Presentation), MVI y UDF.
  - Calidad y testing: Cobertura automatizada de pruebas unitarias (más de 800 tests pasando en verde), simulación de hilos y coroutines con Turbine y bases de datos Room in-memory.
  - Resiliencia técnica: Manejo de Doze Mode en Android, parsing de archivos PDF en ReportLab sin librerías de terceros y diffing SHA-256 para sincronización eficiente.
  - Accesibilidad e Inclusión: Certificación WCAG AAA en contrastes visuales dinámicos.
- **Criterio de Éxito:** Verificar que NotiFast es una pieza de ingeniería de software robusta, reproducible, documentada exhaustivamente y con impacto positivo tangible en la comunidad académica real.
