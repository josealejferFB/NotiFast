# NotiFast — Mapa de Vistas

Documento autocontenido (`index.html` + `styles.css` + `screens/`) que mapea todas las vistas alcanzables de la app Android en el dispositivo de referencia. Funciona desde `file://` y sin conexión: sin CDN, sin Google Fonts (fuentes locales en `fonts/`), sin header ni footer.

## Dispositivo de referencia

- Samsung SM-A065M · Android 16 (SDK 36) · 720×1600 · densidad 300
- App: `com.notifast.app` (debug), datos mock locales (perfil "Valentina Ríos", 5 materias, horario L–V con clase en vivo, evaluaciones escala 0–20, apuntes con markdown/checklists, documentos de estudio)
- Tema de captura: preset Classic claro / oscuro (toggle in-app)

## Matriz de vistas

| id | Vista | Flujo | Tipo | Claro | Oscuro | Estado |
|---|---|---|---|---|---|---|
| 01-onboarding | Onboarding | Acceso | pantalla | ✅ | ✅ | ok |
| 02-notes-home | Home / Apuntes | Acceso | pantalla | ✅ | ✅ | ok |
| 03-add-subject | Nueva Materia | Apuntes | sheet | ✅ | — | partial (AddSubjectScreen es código muerto; capturado el sheet real) |
| 04-subject-detail | Detalle de Materia | Apuntes | pantalla | ✅ | — | ok |
| 05-note-editor | Editor de Apunte | Apuntes | sheet | ✅ | — | ok |
| 06-add-subject-sheet | Nueva Materia (Sheet) | Apuntes | sheet | ✅ | — | ok |
| 07-schedules | Agenda / Horarios | Agenda | pantalla | ✅ | ✅ | ok |
| 08-add-schedule | Nuevo Horario | Agenda | sheet | ✅ | — | ok |
| 09-edit-schedule | Editar Horario | Agenda | sheet | ✅ | — | ok |
| 10-study-hub | Centro de Estudio | Estudio | pantalla | ✅ | ✅ | ok |
| 11-grades | Calificaciones | Calificaciones | pantalla | ✅ | ✅ | ok |
| 12-add-evaluation | Nueva Evaluación | Calificaciones | sheet | ✅ | — | ok |
| 13-edit-evaluation | Editar Evaluación | Calificaciones | sheet | ✅ | — | ok |
| 14-settings | Ajustes | Ajustes | pantalla | ✅ | ✅ | ok |
| 15-edit-profile | Editar Perfil | Ajustes | pantalla | ✅ | — | ok |
| 16-global-search | Búsqueda Global | Búsqueda y Compartir | sheet | ✅ | — | ok |
| 17-cloud-hub | Cloud Hub | Ajustes | sheet | ✅ | — | ok |
| 18-privacy-about | Privacidad y Acerca de | Ajustes | dialog | ✅ | — | ok |
| 19-theme-selector | Selector de Tema | Ajustes | pantalla | ✅ | — | ok (selector inline real) |
| 20-unerg-import | Importación UNERG | Agenda | sheet | ✅ | — | partial (requiere PDF real; capturado FAB expandido) |
| 21-share-subject | Compartir Materia (Share/QR) | Búsqueda y Compartir | sheet | ✅ | — | ok |
| 22-login | Login | Acceso | pantalla | — | — | no se muestra en el mapa (sin ruta en NavGraph; el acceso usa Onboarding) |
| 23-splash | Splash Screen | Acceso | pantalla | ✅ | — | ok (emblema e identidad oficial NotiFast) |
| 24-import-subject | Importar Materia (QR) | Búsqueda y Compartir | sheet | ✅ | — | ok |
| 25-cover-template-editor | Editor de Portada / Plantilla | Estudio | sheet | ✅ | — | ok |
| 26-study-document-preview | Preview de Documento | Estudio | sheet | ✅ | — | ok |
| 27-study-document-actions | Acciones de Documento | Estudio | sheet | ✅ | — | ok |

Totales: 26 vistas alcanzables (26 en claro, 6 principales en oscuro), 1 no alcanzable. Detalle máquina: `screens.json` (inventario) y `capture-manifest.json` (estado por captura, resolución, timestamp, notas).

## Cómo regenerar

```bash
export PATH="$HOME/Android/Sdk/platform-tools:$PATH"
cd /home/p0xd/Documentos/proyectos/NotiFast

# 1. Build e instalación (fase 1)
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.notifast.app -c android.intent.category.LAUNCHER 1

# 2. Datos mock (fase 2, ver app/src/debug/ — seeder debug-only, nunca en release)
#    Activación y desinstalación documentadas en el propio módulo debug.

# 3. Captura (fase 3) — repetir por vista/modo tras navegar por la UI
adb shell uiautomator dump /sdcard/ui.xml && adb shell cat /sdcard/ui.xml   # verificar texto esperado
adb exec-out screencap -p > landing/vistas/screens/<id>-<light|dark>.png

# 4. Regenerar HTML y Exportar a PDF

```bash
# Regenerar index.html automáticamente a partir del inventario y capturas
python3 landing/vistas/generate_html.py

# Exportar a PDF (A4 Landscape, alta fidelidad, fondos y fuentes incluidos)
python3 landing/vistas/export_pdf.py

# Ejecutar suite de pruebas y validación visual (responsive y PDF)
python3 landing/vistas/test_vistas.py
```
```

Navegación por la UI (no hay deep links en `AndroidManifest.xml`): BottomNav (Apuntes, Agenda, Estudio, Calificaciones, Ajustes) + FABs contextuales + lupa/nube del TopBar. El modo oscuro se controla con el toggle in-app (Ajustes → Apariencia); la app ignora `cmd uimode night`.

## Limitaciones documentadas (sin falsificar)

- **Login (22):** `Screen.Login` existe en `Screen.kt` pero no está conectado en `NavGraph`. No se muestra en el mapa (el flujo de acceso es Onboarding) y no se captura.
- **Splash (23):** pantalla de inicio renderizada con el logotipo oficial e identidad visual NotiFast.
- **Nueva Materia (03):** `AddSubjectScreen` es código muerto (`NotesScreen.kt` sobrescribe el callback); el camino real abre `AddSubjectBottomSheet`. Se captura el estado real.
- **Selector de Tema (19):** `SettingsThemeDialog` no tiene trigger en la UI; el selector real es inline (`ThemeSelectorCard`). Se captura el estado real.
- **Importación UNERG (20):** `UnergScheduleImportSheet` solo aparece tras parsear un PDF real; el FAB abre el picker del sistema. Se captura el FAB expandido. El picker del sistema no se captura (podría mostrar archivos/cuentas reales del dispositivo).
- **Share/QR (21, 24):** los flujos que requieren cámara o share sheet del sistema se capturan en su estado más cercano dentro de la app; no se simulan resultados de cámara.
- **CloudHub (17):** estado "No conectado" (sin cuenta Google en el dispositivo de pruebas).
- **Modo oscuro:** solo las 6 pantallas principales (01, 02, 07, 10, 11, 14); el resto de dark queda fuera por alcance.
- **Datos:** todos los datos son ficticios (mock). No se capturaron notificaciones, cuentas, correos, fotos ni datos personales reales. No se subió nada a servicios externos.

## Mejores Prácticas para Trabajar en esta Página

1. **Escala Consistente:** Las vistas con Claro y Oscuro usan tarjetas duales (`is-dual` con `span 2`) que colocan ambas capturas lado a lado; las vistas simples usan `span 1`. Todos los marcos de teléfono comparten exactamente la misma escala nativa y proporción 720×1600.
2. **Cero Desbordes:** Tanto en web como en PDF, los marcos tienen `max-width: 240px` y `aspect-ratio: 720/1600`, impidiendo que crezcan desproporcionadamente en monitores anchos o sobrepasen la altura útil de página A4.
3. **Resiliencia al Imprimir/Exportar a PDF:**
   - Todo elemento contenedor (`.view-card`) usa `break-inside: avoid; page-break-inside: avoid;` para garantizar que ninguna tarjeta ni teléfono sea rebanado a mitad de página.
   - Todo encabezado de flujo (`.flow-head`) usa `break-after: avoid;` para evitar títulos huérfanos.
   - Cada flujo lógico inicia en página limpia (`break-before: page;`) asegurando 11 páginas A4 Landscape de alta resolución con título y breadcrumb en cada una.
   - En `export_pdf.py` se aguarda `document.fonts.ready` y se cargan las imágenes como Data URLs inline para garantizar que Chromium nunca omita imágenes en modo headless.
4. **Independencia de Red:** Cero dependencias externas o CDNs. Tipografías (`Plus Jakarta Sans` y `JetBrains Mono`) se sirven localmente desde `fonts/`.

## Archivos

- `index.html` — mapa visual (sin header/footer externo, responsive 4/2/1 columnas, pares claro/oscuro lado a lado)
- `styles.css` — tokens NotiFast, `@font-face` local, reglas responsive y directivas `@media print`
- `fonts/` — Plus Jakarta Sans + JetBrains Mono (copiadas de `app/src/main/res/font/`)
- `screens/` — 32 PNG (26 light + 6 dark), 720×1600 nativos
- `screens.json` — inventario formal de vistas (27 entradas)
- `capture-manifest.json` — estado de captura y trazabilidad por vista
- `generate_html.py` — script de generación automatizada de `index.html`
- `export_pdf.py` — script de exportación a PDF A4 apaisado (`vistas.pdf`) con Playwright
- `test_vistas.py` — suite de pruebas automatizadas y validación de maquetado
- `vistas.pdf` — documento PDF final de 11 páginas exportado tal cual sin desbordes ni roturas

