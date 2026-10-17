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

        # 4. Compartir materia sin conexión (Nearby / QR)
        self.assertIn("section-nearby-share", html)
        self.assertIn("Android Nearby", html)
        self.assertIn(".notifast", html)

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
        self.assertIn("hero-phone-frame", html)
        self.assertIn("phone-bottom-nav", html)
        self.assertIn("phone-screen-body", html)
        self.assertIn("hub-floating-radar", html)
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

        # 2. Simulador y Dashboard interactivo fiel a GradeSimulatorCard y AcademicAnalyticsContent
        self.assertIn("analytics-dashboard-container", html)
        self.assertIn("analytics-hero-banner", html)
        self.assertIn("TASA DE ÉXITO PROYECTADA", html)
        self.assertIn("interactive-grade-simulator", html)
        self.assertIn("Simulador de Nota Requerida", html)
        self.assertIn("sim-target-slider", html)
        self.assertIn("histogram-preview-card", html)
        self.assertIn("Distribución por Rangos de Desempeño", html)

        # 3. Integración en navegación y radar
        self.assertIn('href="#analiticas"', html)
        self.assertIn('data-target="#analiticas"', html)

        # 4. Lógica de cálculo en JS
        with open("landing/js/interactive-showcase.js", "r", encoding="utf-8") as f:
            js = f.read()
        self.assertIn("setupAnalyticsDashboard", js)
        self.assertIn("calculateRequiredGrade", js)
        self.assertIn("#analiticas", js)

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
        self.assertIn("studio-hue-slider", html)
        self.assertIn("studio-hex-input", html)

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
        self.assertIn("hero-phone-frame", html)

        with open("landing/js/interactive-showcase.js", "r", encoding="utf-8") as f:
            js = f.read()
        self.assertIn("setupNotificationsSimulator", js)
        self.assertIn("setupHeroPhoneMockup", js)

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

if __name__ == "__main__":
    unittest.main()

