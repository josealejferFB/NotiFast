import json
import os
import unittest

class TestLandingStructure(unittest.TestCase):
    def test_firebase_hosting_configured(self):
        self.assertTrue(os.path.exists("firebase.json"))
        with open("firebase.json", "r", encoding="utf-8") as f:
            data = json.load(f)
        self.assertIn("hosting", data)
        self.assertEqual(data["hosting"].get("public"), "landing")

    def test_landing_index_exists_and_valid(self):
        self.assertTrue(os.path.exists("landing/index.html"))
        with open("landing/index.html", "r", encoding="utf-8") as f:
            content = f.read()
        self.assertIn("<!DOCTYPE html>", content)
        self.assertIn("<meta name=\"viewport\"", content)
        self.assertIn("NotiFast", content)
        self.assertIn("skip-link", content)
        self.assertIn("<header", content)
        self.assertIn("<main id=\"main-content\"", content)
        self.assertIn("<footer", content)
        self.assertIn("css/tokens.css", content)
        self.assertIn("css/styles.css", content)

    def test_css_tokens_defined(self):
        self.assertTrue(os.path.exists("landing/css/tokens.css"))
        with open("landing/css/tokens.css", "r", encoding="utf-8") as f:
            css = f.read()
        self.assertIn("#FAF9F6", css)
        self.assertIn("#9E1B32", css)
        self.assertIn("#2A7B7B", css)

    def test_accessibility_and_touch_styles(self):
        self.assertTrue(os.path.exists("landing/css/styles.css"))
        with open("landing/css/styles.css", "r", encoding="utf-8") as f:
            styles = f.read()
        self.assertIn(":focus-visible", styles)
        self.assertIn(".skip-link", styles)
        self.assertIn("min-height", styles)

    def test_study_background_icons_exist(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        # Verificar iconos oficiales de NotiFastStudyBackground.kt
        for icon in [
            "icon-menubook", "icon-school", "icon-science", "icon-calculate",
            "icon-lightbulb", "icon-functions", "icon-emojievents", "icon-edit",
            "icon-historyedu", "icon-autostories", "icon-psychology", "icon-biotech"
        ]:
            self.assertIn(f'id="{icon}"', html)

        self.assertTrue(os.path.exists("landing/js/background.js"))
        with open("landing/js/background.js", "r", encoding="utf-8") as f:
            bg_js = f.read()
        self.assertIn("icon-menubook", bg_js)
        self.assertIn("icon-functions", bg_js)

    def test_app_faithful_sections_exist(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        # 1. Horario en curso con baliza, barra de progreso y botón tomar apuntes
        self.assertIn("EN CURSO AHORA", html)
        self.assertIn("Tomar apuntes", html)
        self.assertIn("schedule-progress-bar", html)
        self.assertIn("08:00 - 10:00", html)

        # 2. Editor de texto en Markdown con barra docked accessory
        self.assertIn("markdown-toolbar", html)
        self.assertIn("rich-editor-sheet", html)
        self.assertIn("Teorema", html)
        self.assertIn("#calculo", html)

        # 3. Calificaciones fiel a MainAverageDashboard
        self.assertIn("PROMEDIO GLOBAL", html)
        self.assertIn("8.70", html)
        self.assertIn("¡Excelente rendimiento!", html)

        # 4. Compartir materia sin conexión (QR Offline / Pack .notifast / Drive)
        self.assertIn("section-nearby-share", html)
        self.assertIn("Código QR Offline", html)
        self.assertIn(".notifast", html)
        self.assertNotIn("Android Nearby", html)

        # 5. Materias con iconos oficiales de SubjectIconMapper
        self.assertIn("Cálculo", html)
        self.assertIn("Física", html)
        self.assertIn("Literatura", html)

    def test_grade_scales_adaptation_exists(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn("grade-scale-selector", html)
        self.assertIn("0 a 10", html)
        self.assertIn("0 a 20", html)
        self.assertIn("100%", html)
        self.assertIn("A - F", html)

        with open("landing/js/interactive-showcase.js", "r", encoding="utf-8") as f:
            js = f.read()
        self.assertIn("GRADE_SCALES", js)
        self.assertIn("0-20", js)
        self.assertIn("0-100", js)
        self.assertIn("A-F", js)

    def test_notifast_hero_showcase_and_radar_present(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn("hero-logo-stage", html)
        self.assertIn("emblem-shield", html)
        self.assertIn("emblem-logo-img", html)
        self.assertIn("hub-floating-radar", html)
        # Verificar que se eliminó el simulador de smartphone
        self.assertNotIn("hero-phone-frame", html)
        # Verificar que se eliminó el hub orbital redundante y la sobrecarga cognitiva
        self.assertNotIn("magic-box-showcase", html)
        self.assertNotIn("notifast-core-hub", html)
        self.assertNotIn("section-origin-indicator", html)
        self.assertNotIn("Desplegado desde", html)

    def test_bento_grid_and_cta_elements(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn("bento-grid", html)
        self.assertIn("btn-download-hero", html)
        self.assertIn("btn-download-cta", html)
        self.assertIn("download-toast", html)
        self.assertIn("Descargar APK", html)

        # Hub de descarga final enriquecido
        self.assertIn("cta-logo-badge", html)
        self.assertIn("cta-eyebrow", html)
        self.assertIn("cta-actions", html)
        self.assertIn("btn-cta-hero", html)
        self.assertIn("btn-cta-github", html)
        self.assertIn("cta-trust-row", html)
        self.assertIn("cta-glow-ruby", html)
        self.assertIn("cta-glow-aqua", html)

        # Radar inferior mejorado
        self.assertIn("radar-progress-fill", html)
        self.assertIn("radar-cta-chip", html)
        self.assertIn('data-target="#importador"', html)
        self.assertIn('data-target="#estudio"', html)
        self.assertIn('data-target="#docentes"', html)

        with open("landing/css/styles.css", "r", encoding="utf-8") as f:
            styles = f.read()
        self.assertIn(".btn-cta-hero", styles)
        self.assertIn(".cta-trust-row", styles)

        with open("landing/css/dynamic-showcase.css", "r", encoding="utf-8") as f:
            css = f.read()
        self.assertIn(".radar-progress-fill", css)
        self.assertIn(".radar-cta-chip", css)

        with open("landing/js/interactive-showcase.js", "r", encoding="utf-8") as f:
            js = f.read()
        self.assertIn("radar-progress-fill", js)
        self.assertIn("#docentes", js)

    def test_evaluations_calendar_section_exists(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        # 1. Sección y títulos de evaluaciones
        self.assertIn('id="evaluaciones"', html)
        self.assertIn("Calendario &amp; Evaluaciones", html)
        self.assertIn("Fechas de examen y entregas", html)
        self.assertIn("eval-filters-strip", html)
        self.assertIn("Parcial II", html)

        # 2. Componente de calendario interactivo fiel a AgendaMonthCalendarView
        self.assertIn("evaluations-calendar-container", html)
        self.assertIn("calendar-month-header", html)
        self.assertIn("Octubre 2026", html)
        self.assertIn("calendar-days-grid", html)
        self.assertIn("daily-evaluations-panel", html)
        self.assertIn("eval-cards-list", html)

        # 3. Integración en navegación y radar
        self.assertIn('href="#evaluaciones"', html)
        self.assertIn('data-target="#evaluaciones"', html)

        # 4. Datos y funciones interactivas en JS
        with open("landing/js/interactive-showcase.js", "r", encoding="utf-8") as f:
            js = f.read()
        self.assertIn("EVALUATIONS_DATA", js)
        self.assertIn("setupEvaluationsCalendar", js)
        self.assertIn("Parcial II: Derivadas y Optimización", js)
        self.assertIn("#evaluaciones", js)

    def test_analytics_and_projections_section_exists(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        # 1. Sección y títulos de Analíticas
        self.assertIn('id="analiticas"', html)
        self.assertIn("Dashboard Predictivo &amp; Proyecciones", html)
        self.assertIn("simula tu nota antes de rendir", html)
        self.assertIn("sim-presets-strip", html)
        self.assertIn("Meta: 8.5 pts", html)

        # 2. Simulador y Dashboard predictivo fiel a GradeSimulatorCard y AcademicAnalyticsContent
        self.assertIn("analytics-dashboard-container", html)
        self.assertIn("analytics-hero-banner", html)
        self.assertIn("TASA DE ÉXITO PROYECTADA", html)
        self.assertIn("interactive-grade-simulator", html)
        self.assertIn("Simulador de Calificaciones", html)
        self.assertIn("Nota Requerida en pendientes", html)
        self.assertIn("sim-target-slider", html)
        self.assertIn("analytics-status-list", html)
        self.assertIn("Alertas de Diagnóstico Temprano", html)
        self.assertIn("Recomendaciones Tácticas", html)

        # 3. Integración en navegación y radar
        self.assertIn('href="#analiticas"', html)
        self.assertIn('data-target="#analiticas"', html)

        # 4. Lógica de cálculo en JS
        with open("landing/js/interactive-showcase.js", "r", encoding="utf-8") as f:
            js = f.read()
        self.assertIn("setupAnalyticsDashboard", js)
        self.assertIn("calculateRequiredGrade", js)
        self.assertIn("#analiticas", js)

    def test_share_pack_and_drive_backup_section_exists(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn('id="compartir"', html)
        self.assertIn("share-cloud-stack", html)
        self.assertIn("share-pack-card", html)
        self.assertIn("Pack QR de Asignatura", html)
        self.assertIn("Exportar Pack .notifast", html)
        self.assertIn("Código QR Offline", html)
        self.assertIn("Copiar Resumen Markdown", html)
        self.assertIn("Incluir mis apuntes personales", html)
        self.assertIn("qr-preview-stage", html)

        self.assertIn("cloud-backup-card", html)
        self.assertIn("Copia de Seguridad", html)
        self.assertIn("Google Drive (Privado)", html)
        self.assertIn("Sincronizada", html)
        self.assertIn("Solo en el teléfono", html)
        self.assertIn("Cambios sin subir", html)
        self.assertIn("Subir a la nube", html)
        self.assertIn("Restaurar al teléfono", html)

        # Sin claims de Nearby / radio P2P
        self.assertNotIn("Android Nearby", html)
        self.assertNotIn("p2p-simulation-stage", html)

        with open("landing/css/dynamic-showcase.css", "r", encoding="utf-8") as f:
            css = f.read()
        self.assertIn(".share-cloud-stack", css)
        self.assertIn(".cloud-backup-card", css)
        self.assertIn(".qr-preview-stage", css)

    def test_subjects_gallery_bento_alignment(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn("subject-card-top-row", html)
        self.assertIn("subject-grade-pill", html)
        self.assertIn("subject-progress-block", html)
        self.assertIn("subj-progress-track", html)
        self.assertIn("subject-tags-strip", html)
        self.assertIn("#derivadas", html)
        self.assertIn("#newton", html)

        with open("landing/css/dynamic-showcase.css", "r", encoding="utf-8") as f:
            css = f.read()
        self.assertIn("repeat(4, 1fr)", css)
        self.assertIn(".subject-card-top-row", css)
        self.assertIn(".subject-grade-pill", css)

    def test_showcase_hover_bounds_and_centering(self):
        with open("landing/css/dynamic-showcase.css", "r", encoding="utf-8") as f:
            css = f.read()
        self.assertIn(".hub-floating-radar", css)
        self.assertIn("translateX(-50%)", css)

        with open("landing/js/interactive-showcase.js", "r", encoding="utf-8") as f:
            js = f.read()
        self.assertIn("setupFloatingRadarDock", js)
        self.assertIn("hub-floating-radar", js)

    def test_enhanced_footer_structure(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn("footer-main-grid", html)
        self.assertIn("footer-col-brand", html)
        self.assertIn("footer-mission-text", html)
        self.assertIn("footer-trust-chips", html)
        self.assertIn("footer-bottom-bar", html)
        self.assertIn("footer-back-to-top", html)

        with open("landing/css/styles.css", "r", encoding="utf-8") as f:
            css = f.read()
        self.assertIn(".footer-main-grid", css)
        self.assertIn(".footer-trust-chips", css)
        self.assertIn(".footer-bottom-bar", css)

    def test_theme_studio_section_exists(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn('id="temas"', html)
        self.assertIn("theme-studio-workbench", html)
        self.assertIn('data-accent-preset="classic"', html)
        self.assertIn('data-accent-preset="forest_sage"', html)
        self.assertIn('data-accent-preset="midnight_indigo"', html)
        self.assertIn('data-accent-preset="warm_mocha"', html)
        self.assertIn('data-accent-preset="sakura_zen"', html)
        self.assertIn('data-accent-preset="ocean_cyan"', html)
        self.assertIn('data-bg-preset="classic"', html)
        # Verificar que se removió la barra de selección hex para simplificar la experiencia estudiantil
        self.assertNotIn("studio-hue-slider", html)
        self.assertNotIn("studio-hex-input", html)

        with open("landing/js/interactive-showcase.js", "r", encoding="utf-8") as f:
            js = f.read()
        self.assertIn("ACCENT_PRESETS", js)
        self.assertIn("CANVAS_PRESETS", js)
        self.assertIn("setupLiveThemeStudio", js)

    def test_proactive_notifications_section_exists(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn('id="notificaciones"', html)
        self.assertIn("notifications-simulator-card", html)
        self.assertIn("live-android-notif-banner", html)
        self.assertIn('data-notif-channel="class"', html)
        self.assertIn('data-notif-channel="exam"', html)
        self.assertIn('data-notif-channel="morning"', html)
        self.assertIn("btn-test-notification", html)

        with open("landing/js/interactive-showcase.js", "r", encoding="utf-8") as f:
            js = f.read()
        self.assertIn("setupNotificationsSimulator", js)
        self.assertIn("setupHeroLogoMotion", js)

    def test_dace_schedule_importer_section_exists(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn('id="importador"', html)
        self.assertIn("section-dace-import", html)
        self.assertIn("dace-import-card", html)
        self.assertIn("comprobante_dace_2026-01.pdf", html)
        self.assertIn("VISTA PREVIA DE IMPORTACIÓN", html)
        self.assertIn("NO PRESENCIAL", html)
        self.assertIn("Combinación inteligente", html)
        self.assertIn("Reemplazo total", html)
        self.assertIn("dace-strategy-chip", html)
        self.assertIn("icon-upload", html)

        # Integración en navegación y radar
        self.assertIn('href="#importador"', html)
        self.assertIn('data-target="#importador"', html)

        # Zig-zag: fila row-reverse como el resto del showcase
        self.assertIn('class="dynamic-feature-row row-reverse section-dace-import"', html)

        # Estilos del mockup en dynamic-showcase.css
        with open("landing/css/dynamic-showcase.css", "r", encoding="utf-8") as f:
            css = f.read()
        self.assertIn(".dace-import-card", css)
        self.assertIn(".dace-subject-row", css)
        self.assertIn(".dace-strategy-box", css)

    def test_study_hub_documents_and_covers_section_exists(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn('id="estudio"', html)
        self.assertIn("section-study-hub", html)
        self.assertIn("study-docs-card", html)
        self.assertIn("cover-creator-card", html)
        self.assertIn("Documentos por Materia", html)
        self.assertIn("Creador de Portadas", html)
        self.assertIn("APA 7.ª", html)
        self.assertIn("IEEE", html)
        self.assertIn("Tesis", html)
        self.assertIn("Vista previa en vivo", html)
        self.assertIn("Generar .docx", html)
        self.assertIn("Abrir en Google Docs", html)
        self.assertIn("icon-description", html)

        # Integración en navegación y radar
        self.assertIn('href="#estudio"', html)
        self.assertIn('data-target="#estudio"', html)

        # Zig-zag: fila row-reverse como el resto del showcase
        self.assertIn('class="dynamic-feature-row row-reverse section-study-hub"', html)

        # Estilos del mockup en dynamic-showcase.css
        with open("landing/css/dynamic-showcase.css", "r", encoding="utf-8") as f:
            css = f.read()
        self.assertIn(".study-hub-stack", css)
        self.assertIn(".study-docs-card", css)
        self.assertIn(".cover-creator-card", css)
        self.assertIn(".cover-page-preview", css)

    def test_notes_blocks_section_matches_app_features(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn('id="apuntes"', html)
        self.assertIn("apuntes por bloques", html.lower())
        self.assertIn("board-photo-block", html)
        self.assertIn("md-highlight", html)
        self.assertIn("Pizarra anotada", html)
        self.assertIn("math-latex-card", html)
        self.assertIn("interactive-todo-list", html)

        with open("landing/css/dynamic-showcase.css", "r", encoding="utf-8") as f:
            css = f.read()
        self.assertIn(".board-photo-block", css)
        self.assertIn(".md-highlight", css)

    def test_subjects_gallery_shows_color_section_and_teacher(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn('id="materias"', html)
        self.assertIn("subject-palette-strip", html)
        self.assertIn("subject-section-chip", html)
        self.assertIn("subject-teacher-chip", html)
        self.assertIn("Sec. 2", html)
        self.assertIn("Paleta de 10 colores", html)

        with open("landing/css/dynamic-showcase.css", "r", encoding="utf-8") as f:
            css = f.read()
        self.assertIn(".subject-palette-strip", css)
        self.assertIn(".subject-section-chip", css)
        self.assertIn(".subject-teacher-chip", css)

    def test_teachers_directory_section_exists(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn('id="docentes"', html)
        self.assertIn("section-teachers-directory", html)
        self.assertIn("teachers-directory-card", html)
        self.assertIn("Profesores", html)
        self.assertIn("teacher-mini-card", html)
        self.assertIn("teacher-contact-row", html)
        self.assertIn("WhatsApp", html)
        self.assertIn("Llamar", html)
        self.assertIn("SMS", html)
        self.assertIn("Correo", html)
        self.assertIn("wa-greeting-bubble", html)
        self.assertIn("le escribo con respecto a la materia", html)

        # Navegación y radar
        self.assertIn('href="#docentes"', html)
        self.assertIn('data-target="#docentes"', html)

        with open("landing/css/dynamic-showcase.css", "r", encoding="utf-8") as f:
            css = f.read()
        self.assertIn(".teachers-directory-card", css)
        self.assertIn(".teacher-mini-card", css)
        self.assertIn(".wa-greeting-bubble", css)

    def test_cloudflare_pages_metadata(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn("https://notifast.pages.dev/", html)

    def test_mobile_responsiveness_and_zero_emojis(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        with open("landing/js/interactive-showcase.js", "r", encoding="utf-8") as f:
            js = f.read()
        with open("landing/css/styles.css", "r", encoding="utf-8") as f:
            styles = f.read()
        with open("landing/css/dynamic-showcase.css", "r", encoding="utf-8") as f:
            showcase_css = f.read()

        # 1. Mobile navigation drawer and button
        self.assertIn('id="btn-mobile-menu"', html)
        self.assertIn('id="mobile-nav-drawer"', html)
        self.assertIn("setupMobileNavigation", js)
        self.assertIn(".mobile-menu-toggle", styles)
        self.assertIn(".mobile-nav-drawer", styles)

        # 2. Responsive media queries for mobile & tablet viewports
        self.assertIn("@media (max-width: 992px)", showcase_css)
        self.assertIn("@media (max-width: 640px)", showcase_css)

        # 3. Zero emojis across HTML and JS
        for forbidden_emoji in ["⏰", "☑", "📷", "📱", "📅", "📝", "☀️", "🚀", "✨", "🔥", "💡"]:
            self.assertNotIn(forbidden_emoji, html)
            self.assertNotIn(forbidden_emoji, js)

    def test_docs_html_structure_and_integration(self):
        self.assertTrue(os.path.exists("landing/docs.html"))
        with open("landing/docs.html", "r", encoding="utf-8") as f:
            html = f.read()

        self.assertIn("<!DOCTYPE html>", html)
        self.assertIn("<meta name=\"viewport\"", html)
        self.assertIn("Documentación Técnica", html)
        self.assertIn("https://notifast.pages.dev/docs", html)

        # Componentes del Layout de 3 columnas
        self.assertIn('id="docs-header"', html)
        self.assertIn('id="docs-sidebar"', html)
        self.assertIn('id="docs-main-content"', html)
        self.assertIn('id="docs-toc"', html)
        self.assertIn('id="docs-search-modal"', html)

        # Enlace de regreso a la landing
        self.assertIn('Volver a la Landing', html)
        self.assertIn('href="/"', html)

        # Dependencias de diseño y scripts
        self.assertIn("css/tokens.css", html)
        self.assertIn("css/docs.css", html)
        self.assertIn("js/docs-data.js", html)
        self.assertIn("js/docs.js", html)

        # CDN de renderizado matemático y de diagramas
        self.assertIn("katex", html)
        self.assertIn("mermaid", html)
        self.assertIn("prism", html)
        self.assertIn("marked", html)

    def test_docs_assets_exist(self):
        self.assertTrue(os.path.exists("landing/css/docs.css"))
        with open("landing/css/docs.css", "r", encoding="utf-8") as f:
            css = f.read()
        self.assertIn(".docs-header", css)
        self.assertIn(".docs-sidebar", css)
        self.assertIn(".docs-toc", css)
        self.assertIn(".search-modal-backdrop", css)

        self.assertTrue(os.path.exists("landing/js/docs.js"))
        with open("landing/js/docs.js", "r", encoding="utf-8") as f:
            js = f.read()
        self.assertIn("NOTIFAST_DOCS_DATA", js)
        self.assertIn("performSearch", js)
        self.assertIn("renderMermaid", js)
        self.assertIn("renderKaTeX", js)

        self.assertTrue(os.path.exists("landing/js/docs-data.js"))
        with open("landing/js/docs-data.js", "r", encoding="utf-8") as f:
            data_content = f.read()
        self.assertIn("window.NOTIFAST_DOCS_DATA", data_content)
        self.assertIn('"totalDocs": 25', data_content)

    def test_landing_docs_navigation_integration(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()

        # Botón Documentación en navbar desktop y drawer móvil
        self.assertIn("nav-desktop-docs", html)
        self.assertIn('href="docs.html"', html)
        self.assertIn("mobile-nav-highlight", html)

        # Enlaces en footer a documentos clave
        self.assertIn('href="docs.html#prd"', html)
        self.assertIn('href="docs.html#system-overview"', html)
        self.assertIn('href="docs.html#testing-strategy"', html)
        self.assertIn('href="docs.html#legal-notes"', html)

        # Redirecciones en Cloudflare Pages y Firebase
        self.assertTrue(os.path.exists("landing/_redirects"))
        with open("landing/_redirects", "r", encoding="utf-8") as f:
            redirects = f.read()
        self.assertIn("/docs /docs.html 200", redirects)

    def test_zero_emojis_in_docs(self):
        forbidden = ["⏰", "☑", "📷", "📱", "📅", "📝", "☀️", "🚀", "✨", "🔥", "💡"]
        for filepath in ["landing/docs.html", "landing/css/docs.css", "landing/js/docs.js"]:
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()
            for emoji in forbidden:
                self.assertNotIn(emoji, content, f"Emoji prohibido {emoji} encontrado en {filepath}")

if __name__ == "__main__":
    unittest.main()

