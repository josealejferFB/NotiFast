# Local Academic Notifications & Graded Alerts Engine (AAA) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reactivate, modernize, and engineer to AAA industry standards the local academic notifications and graded alerts engine of NotiFast, replacing dead code in Settings with Android 13+ runtime permission management, an exact alarm hybrid engine (`AlarmManager.setExactAndAllowWhileIdle()` + `WorkManager`), heads-up alerts with deep-linking, an in-settings tactile test button, and modular Compose architecture (<= 250 LOC).

**Architecture:** 
- **Exact Alarms (Domain & Data):** `ExactAlarmScheduler` interface in domain, `ExactAlarmSchedulerImpl` in data using `AlarmManager.setExactAndAllowWhileIdle()` to guarantee punctuality under Doze Mode.
- **Broadcast Receivers:** Dedicated `AlarmTriggerReceiver` (not exported) receiving exact alarm intents, notifying via `NotificationSender`, and re-arming recurring classes; reinforced `BootCompletedReceiver` handling boot, time changes (`TIME_SET`), and timezone changes (`TIMEZONE_CHANGED`).
- **MVI Contract & Permissions:** `SettingsUiState` tracks `isSystemNotificationPermissionGranted`. Permissions requested dynamically via `ActivityResultContracts.RequestPermission()`, with explanatory rationale dialogs directing to system settings when permanently denied.
- **Deep-Linking & Heads-Up:** Rich notifications on `CHANNEL_CLASSES` and `CHANNEL_EVALUATIONS` with quick action buttons, vibration, heads-up priority, and `MainActivity` intent routing.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose Material 3, Android AlarmManager, AndroidX WorkManager, Hilt 2.59, Coroutines Flow, JUnit4, MockK / Robolectric.

**Spec:** Local Notifications Engine AAA Specification in user request.

## Global Constraints

- Strict Clean Architecture: domain depends on nothing, presentation and data depend on domain.
- MVI with single StateFlow per ViewModel (`_uiState.asStateFlow()`).
- Maximum ~250 lines per Composable file.
- Zero hardcoded strings: 100% in `res/values/strings.xml` in Spanish. Code in English.
- WCAG AAA compliance, touch targets >= 48dp.
- Zero uncaught exceptions: all repository / scheduler operations wrapped in `try/catch` and `Result<T>`.

## Review Focus

1. Permission revocation in OS settings: User preferences (minutes/days) in DataStore must not be wiped when permission is revoked in system settings.
2. Device restart or timezone travel: All exact class alarms and exam alerts must be re-scheduled accurately upon `BOOT_COMPLETED`, `TIME_SET`, and `TIMEZONE_CHANGED`.
3. Doze Mode exactness: Classes scheduled 15 minutes before must fire at the exact minute without WorkManager batching delays.
4. Graded / presented evaluations: If an evaluation was marked presented or graded before the alarm fires, `AlarmTriggerReceiver` must discard the notification silently.
5. Re-arming recurring classes: When a class alert triggers, it must automatically schedule the occurrence for the same day/time next week.

---

### Task 1: Design Tokens, Spanish Strings & Manifest Declarations

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Produces: String resources for notification permissions, test alerts, and quick actions; manifest permissions (`SCHEDULE_EXACT_ALARM`, `VIBRATE`) and receiver declarations (`AlarmTriggerReceiver`, `BootCompletedReceiver` intent filters).

- [ ] **Step 1: Write string resources in `res/values/strings.xml`**
  Add strings for:
  - `notification_action_view_schedule` ("Ver Horario")
  - `notification_action_view_subject` ("Ver Materia")
  - `notification_test_title` ("Prueba de Alertas Académicas")
  - `notification_test_body` ("¡Excelente! Sonido, vibración y banner heads-up configurados con máxima prioridad.")
  - `settings_notifications_test_btn` ("Enviar notificación de prueba")
  - `settings_notifications_test_btn_desc` ("Comprueba el sonido, vibración y banner heads-up en este dispositivo")
  - `settings_notifications_test_sent` ("Notificación de prueba enviada con éxito")
  - `settings_notifications_permission_warning_title` ("Permisos de Notificación Desactivados")
  - `settings_notifications_permission_warning_desc` ("El sistema tiene las notificaciones bloqueadas para NotiFast. Las alertas de clases y evaluaciones no sonarán.")
  - `settings_notifications_permission_enable_btn` ("Habilitar Permisos")
  - `settings_notifications_permission_dialog_title` ("Activar Notificaciones de NotiFast")
  - `settings_notifications_permission_dialog_desc` ("Para que NotiFast pueda avisarte puntualmente antes de tus clases y recordarte tus exámenes importantes, necesitamos permiso para mostrarte alertas prioritarias.")
  - `settings_notifications_permission_open_settings` ("Abrir Ajustes del Sistema")

- [ ] **Step 2: Update `AndroidManifest.xml` permissions and receivers**
  Add:
  - `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />`
  - `<uses-permission android:name="android.permission.VIBRATE" />`
  - In `BootCompletedReceiver`: add `android.intent.action.MY_PACKAGE_REPLACED`, `android.intent.action.TIME_SET`, `android.intent.action.TIMEZONE_CHANGED`.
  - Add `<receiver android:name=".data.notification.AlarmTriggerReceiver" android:exported="false" />`

- [ ] **Step 3: Verify resources and manifest compile**
  Run: `./gradlew processDebugMainManifest`

---

### Task 2: ExactAlarmScheduler Interface, Implementation & DI

**Files:**
- Create: `app/src/main/java/com/notifast/app/domain/notification/ExactAlarmScheduler.kt`
- Create: `app/src/main/java/com/notifast/app/data/notification/ExactAlarmSchedulerImpl.kt`
- Modify: `app/src/main/java/com/notifast/app/di/RepositoryModule.kt` (or `AppModule.kt`)
- Test: `app/src/test/java/com/notifast/app/data/notification/ExactAlarmSchedulerTest.kt`

**Interfaces:**
- Produces: `ExactAlarmScheduler` interface and its implementation:
  ```kotlin
  interface ExactAlarmScheduler {
      fun scheduleClassAlarm(
          scheduleId: String,
          subjectName: String,
          classroom: String,
          startTime: String,
          triggerAtMillis: Long
      )
      fun scheduleEvaluationAlarm(
          evaluationId: String,
          subjectName: String,
          title: String,
          dateText: String,
          weight: Double,
          triggerAtMillis: Long
      )
      fun cancelClassAlarm(scheduleId: String)
      fun cancelEvaluationAlarm(evaluationId: String)
      fun canScheduleExactAlarms(): Boolean
  }
  ```

- [ ] **Step 1: Write failing test in `ExactAlarmSchedulerTest.kt`**
  Verify that scheduling a class alarm and evaluation alarm configures the alarm manager with exact time and allows while idle, and canceling cancels the corresponding PendingIntent.
- [ ] **Step 2: Run test to verify it fails**
  Run: `./gradlew testDebugUnitTest --tests com.notifast.app.data.notification.ExactAlarmSchedulerTest`
- [ ] **Step 3: Implement `ExactAlarmScheduler` and `ExactAlarmSchedulerImpl`**
  Implement exact alarm scheduling using `alarmManager.setExactAndAllowWhileIdle()` with `PendingIntent.getBroadcast` to `AlarmTriggerReceiver`. Wrap in `try/catch` for `SecurityException` and check `canScheduleExactAlarms()`.
- [ ] **Step 4: Bind `ExactAlarmScheduler` in Hilt DI**
- [ ] **Step 5: Run tests and verify they pass**
  Run: `./gradlew testDebugUnitTest --tests com.notifast.app.data.notification.ExactAlarmSchedulerTest`

---

### Task 3: AlarmTriggerReceiver & Rescheduling Resilience

**Files:**
- Create: `app/src/main/java/com/notifast/app/data/notification/AlarmTriggerReceiver.kt`
- Modify: `app/src/main/java/com/notifast/app/data/notification/BootCompletedReceiver.kt`
- Test: `app/src/test/java/com/notifast/app/data/notification/BootCompletedReceiverTest.kt`

**Interfaces:**
- Consumes: `NotificationSender`, `ScheduleRepository`, `EvaluationRepository`, `UserPrefsRepository`, `ScheduleClassAlertUseCase`, `RescheduleAllAlertsUseCase`.
- Produces: Broadcast handling for alarm triggers and system time/boot events.

- [ ] **Step 1: Write unit test for receiver actions**
  Verify `BootCompletedReceiver` calls `rescheduleAllAlertsUseCase` for all supported actions (`BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED`, `MY_PACKAGE_REPLACED`).
- [ ] **Step 2: Run test to verify it fails**
- [ ] **Step 3: Implement `AlarmTriggerReceiver`**
  - Implement `@AndroidEntryPoint class AlarmTriggerReceiver : BroadcastReceiver()`
  - Handle `ACTION_CLASS_ALERT`: verify `classAlertsEnabledFlow.first()`, re-arm schedule for next week using `scheduleClassAlertUseCase`, send notification via `notificationSender.sendClassNotification`.
  - Handle `ACTION_EVALUATION_ALERT`: verify `evalAlertsEnabledFlow.first()`, check evaluation is still active in `evaluationRepository`, send notification via `notificationSender.sendEvaluationNotification`.
- [ ] **Step 4: Update `BootCompletedReceiver` to handle time changes and timezone changes**
- [ ] **Step 5: Run tests and verify they pass**
  Run: `./gradlew testDebugUnitTest --tests com.notifast.app.data.notification.BootCompletedReceiverTest`

---

### Task 4: NotificationSender Modernization with Deep-Linking & Heads-Up Alerts

**Files:**
- Modify: `app/src/main/java/com/notifast/app/data/notification/NotificationSender.kt`
- Modify: `app/src/main/java/com/notifast/app/data/notification/NotificationChannelsManager.kt`
- Modify: `app/src/main/java/com/notifast/app/MainActivity.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/main/MainViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/navigation/NavGraph.kt`
- Test: `app/src/test/java/com/notifast/app/data/notification/NotificationSenderTest.kt`

**Interfaces:**
- Produces:
  `NotificationSender.areNotificationsEnabled(): Boolean`
  `NotificationSender.sendTestNotification(): Boolean`
  Rich heads-up notifications with action buttons and deep link extras (`MainActivity.EXTRA_DESTINATION`).

- [ ] **Step 1: Write failing test in `NotificationSenderTest.kt`**
  Verify that `sendClassNotification`, `sendEvaluationNotification`, and `sendTestNotification` configure proper intents, channels, priority, vibration, and deep link extras.
- [ ] **Step 2: Run test to verify it fails**
- [ ] **Step 3: Update `NotificationChannelsManager.kt` and `NotificationSender.kt`**
  - Configure vibration patterns and high priority in channels.
  - Implement `sendTestNotification()` targeting `CHANNEL_CLASSES` with heads-up banner and deep link to Settings.
  - Add quick action "Ver Horario" in `sendClassNotification()`.
  - Add quick action "Ver Materia" in `sendEvaluationNotification()`.
- [ ] **Step 4: Update `MainActivity.kt` and `MainViewModel.kt` for reactive deep-link navigation**
  - Add `NotificationDeepLink` handling in `MainViewModel`.
  - Observe `pendingDeepLink` in `NavGraph.kt` to navigate to `Schedules`, `SubjectDetail`, or `Settings`.
- [ ] **Step 5: Run tests and verify they pass**
  Run: `./gradlew testDebugUnitTest --tests com.notifast.app.data.notification.NotificationSenderTest`

---

### Task 5: Refactor Alert Use Cases for Hybrid Engine (Exact Alarms + WorkManager)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/domain/usecase/ScheduleClassAlertUseCase.kt`
- Modify: `app/src/main/java/com/notifast/app/domain/usecase/ScheduleEvaluationAlertUseCase.kt`
- Modify: `app/src/main/java/com/notifast/app/domain/usecase/CancelAlertUseCase.kt`
- Modify: `app/src/main/java/com/notifast/app/domain/usecase/RescheduleAllAlertsUseCase.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/ScheduleClassAlertUseCaseTest.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/ScheduleEvaluationAlertUseCaseTest.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/CancelAlertUseCaseTest.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/RescheduleAllAlertsUseCaseTest.kt`

**Interfaces:**
- Consumes: `ExactAlarmScheduler`, `UserPrefsRepository`, `WorkManager`.
- Produces: Exact alarm scheduling for classes and evaluations, combined with WorkManager for daily summary.

- [ ] **Step 1: Update failing tests for exact alarm calls in use case tests**
- [ ] **Step 2: Run tests to verify they fail**
- [ ] **Step 3: Update `ScheduleClassAlertUseCase` to use `exactAlarmScheduler.scheduleClassAlarm()`**
  Calculate exact target timestamp, schedule exact alarm, and cancel legacy WorkManager request.
- [ ] **Step 4: Update `ScheduleEvaluationAlertUseCase` to use `exactAlarmScheduler.scheduleEvaluationAlarm()`**
  Calculate exact target timestamp, schedule exact alarm, and cancel legacy WorkManager request.
- [ ] **Step 5: Update `CancelAlertUseCase` and `RescheduleAllAlertsUseCase`**
  Cancel both exact alarm and WorkManager request in `CancelAlertUseCase`. Keep DailySummaryWorker in `RescheduleAllAlertsUseCase`.
- [ ] **Step 6: Run all use case tests and verify they pass**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.domain.usecase.*Alert*"`

---

### Task 6: Settings MVI & Permission Lifecycle Management

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/settings/SettingsContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/settings/SettingsViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/settings/components/SettingsDialogDispatcher.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/settings/SettingsViewModelTest.kt`

**Interfaces:**
- Produces:
  `SettingsUiState.isSystemNotificationPermissionGranted: Boolean`
  `SettingsDialogType.NOTIFICATION_PERMISSION_REQUIRED`
  `SettingsEvent.RefreshNotificationPermission`, `UpdateNotificationPermission`, `SendTestNotification`, `RequestNotificationPermissionResult`.

- [ ] **Step 1: Write failing test in `SettingsViewModelTest.kt`**
  Verify permission checking on init/refresh, test notification dispatch, and dialog triggers when permission is denied.
- [ ] **Step 2: Run test to verify it fails**
- [ ] **Step 3: Update `SettingsContract.kt`**
  Add state fields, events, and dialog type.
- [ ] **Step 4: Update `SettingsViewModel.kt`**
  Implement permission verification, `SendTestNotification`, and handle permission request results without wiping time preferences when revoked.
- [ ] **Step 5: Update `SettingsDialogDispatcher.kt`**
  Handle `NOTIFICATION_PERMISSION_REQUIRED` dialog with direct intent to `Settings.ACTION_APP_NOTIFICATION_SETTINGS`.
- [ ] **Step 6: Run tests and verify they pass**
  Run: `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.feature.settings.SettingsViewModelTest`

---

### Task 7: Settings Screen & Modular `SettingsNotificationsSection` (AAA Bento UX)

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/settings/components/SettingsNotificationsSection.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/settings/components/SettingsSectionsContent.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/settings/SettingsScreen.kt`

**Interfaces:**
- Produces:
  Extracted modular `SettingsNotificationsSection` (<= 250 LOC).
  Permission warning banner, class and evaluation alert rows with badges, daily summary switch, and Bento test notification button.
  Runtime permission launcher and `ON_RESUME` lifecycle observer in `SettingsScreen.kt`.

- [ ] **Step 1: Create `SettingsNotificationsSection.kt`**
  - Display permission warning card when `!isSystemNotificationPermissionGranted`.
  - Class alerts switch with timing chip dialog trigger.
  - Evaluation alerts switch with timing chip dialog trigger.
  - Daily summary switch.
  - Interactive tactile Bento button "Enviar notificación de prueba" (`SendTestNotification`).
  - Total file lines <= 250 LOC.
- [ ] **Step 2: Clean up `SettingsSectionsContent.kt`**
  Replace inline notifications code with `SettingsNotificationsSection`, keeping `SettingsSectionsContent.kt` well under 250 LOC.
- [ ] **Step 3: Wire permission launcher & lifecycle in `SettingsScreen.kt`**
  - Add `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`.
  - Add `DisposableEffect` / lifecycle observer on `ON_RESUME` to refresh `RefreshNotificationPermission`.
  - Pass permission request callback down to notifications section.

---

### Task 8: End-to-End Verification & Roadmap Sync

**Files:**
- Modify: `docs/prd/STATUS.md`

- [ ] **Step 1: Assemble debug APK**
  Run: `./gradlew assembleDebug`
- [ ] **Step 2: Run full unit test suite**
  Run: `./gradlew testDebugUnitTest` (all tests passing, 0 failures).
- [ ] **Step 3: If device available, install and verify**
  Run: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- [ ] **Step 4: Update `docs/prd/STATUS.md`**
  Document the completion of the Local Academic Notifications Engine AAA.
