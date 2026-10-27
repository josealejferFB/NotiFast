# ADR-0003: Motor Híbrido de Notificaciones: AlarmManager Doze-Safe + WorkManager

- **Estado:** Aceptado
- **Fecha:** 2026-10-17
- **Módulo:** Notificaciones (`domain/notification`, `data/notification`, `data/worker`)

---

## Contexto y Planteamiento del Problema

Un cuaderno académico inteligente debe avisar al estudiante con puntualidad milimétrica antes de que comience una clase (ej. 15 minutos antes) o antes de una evaluación decisiva. En las versiones iniciales, se intentó utilizar exclusivamente Android WorkManager (`OneTimeWorkRequest`).

Sin embargo, a partir de Android 6.0 y endurecido en Android 12+, el sistema operativo activa el **Doze Mode** (modo de suspensión profunda de batería). WorkManager agrupa las tareas en segundo plano (*batching*), provocando retrasos impredecibles de entre 10 y 30 minutos. Un estudiante cuya clase empezaba a las 08:00 AM recibía la notificación a las 08:25 AM, inutilizando el propósito de la funcionalidad.

## Decisión

Se decide implementar una **arquitectura híbrida de notificaciones proactivas**:

1. **Alarmas Exactas con AlarmManager (`ExactAlarmSchedulerImpl`):** Para eventos temporales críticos e inmediatos (clases y exámenes), se utiliza `AlarmManager.setExactAndAllowWhileIdle()`, garantizando la ejecución de la alarma en el minuto exacto incluso con el dispositivo en Doze Mode.
2. **Receptor Dedicado no Exportado (`AlarmTriggerReceiver`):** Un `BroadcastReceiver` interno recibe el Intent exacto, valida que la evaluación no haya sido ya calificada o descartada, construye la notificación `NotificationCompat` en el canal adecuado y reprograma automáticamente la siguiente ocurrencia semanal de la clase.
3. **WorkManager para Tareas Periódicas Diferibles:** Se reserva WorkManager (`DailySummaryWorker`) exclusivamente para la notificación matutina de las 07:00 AM y tareas de mantenimiento donde una ventana de tolerancia de algunos minutos no perjudica al estudiante.
4. **Reprogramación Automática (`BootCompletedReceiver`):** Manejador del sistema que escucha `BOOT_COMPLETED`, `ACTION_TIME_CHANGED` y `ACTION_TIMEZONE_CHANGED` para restaurar todas las alarmas en el `AlarmManager` tras un reinicio del teléfono.
5. **Permisos Dinámicos Android 13+:** Manejo explícito de `POST_NOTIFICATIONS` y `SCHEDULE_EXACT_ALARM` con diálogos pedagógicos accesibles.

## Alternativas Consideradas

| Alternativa | Motivo de Descarte |
|---|---|
| **Solo Android WorkManager** | Los retrasos inducidos por el sistema de gestión de energía de Android rompen la promesa de valor de puntualidad en clases presenciales. |
| **Servicio en Primer Plano Permanente (Foreground Service)** | Mantiene una notificación fija invasiva en la barra de estado y agota prematuramente la batería del teléfono del estudiante. |
| **Push Notifications Remotas (Firebase Cloud Messaging - FCM)** | Requiere conexión a internet obligatoria y servidores intermedios en la nube, violando la directiva de arquitectura 100% Offline-First. |

## Consecuencias

### Positivas
- ✅ **Puntualidad Absoluta:** Las alertas suenan en el minuto exacto independientemente del estado de ahorro de batería.
- ✅ **Eficiencia Energética:** La app permanece inactiva en reposo y solo se despierta durante unos milisegundos para emitir la notificación.
- ✅ **Resiliencia ante Reinicios:** Ninguna alarma se pierde tras apagar o reiniciar el teléfono.
- ✅ **100% Offline:** Las alertas se computan y despachan localmente sin gastar datos móviles.

### Negativas y Mitigaciones
- ⚠️ *Exigencia de permisos especiales (`SCHEDULE_EXACT_ALARM`) en Android 12+:* Mitigado mediante la inclusión de un botón de prueba táctil en Ajustes y un diálogo educativo que explica la importancia del permiso para el éxito académico.
