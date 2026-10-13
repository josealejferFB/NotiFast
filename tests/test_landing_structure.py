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

    def test_notifast_central_hub_present(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn("magic-box-showcase", html)
        self.assertIn("notifast-core-hub", html)
        self.assertIn("notifast-core-icon", html)
        self.assertIn("popout-notes", html)
        self.assertIn("popout-grades", html)
        self.assertIn("hub-floating-radar", html)
        self.assertIn("en un solo lugar", html.lower())
        # Verificar que se eliminó la sobrecarga cognitiva de 'desplegado desde modulo'
        self.assertNotIn("section-origin-indicator", html)
        self.assertNotIn("Desplegado desde", html)

    def test_bento_grid_and_cta_elements(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn("bento-grid", html)
        self.assertIn("btn-download-hero", html)
        self.assertIn("btn-download-cta", html)
        self.assertIn("download-toast", html)
        self.assertIn("Google Play", html)

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
        self.assertIn(".popout-subjects:hover", css)
        self.assertIn(".popout-alerts:hover", css)
        self.assertIn("translateX(-50%)", css)

        with open("landing/js/interactive-showcase.js", "r", encoding="utf-8") as f:
            js = f.read()
        self.assertIn("xPercent: -50", js)
        self.assertIn(".popout-subjects, .popout-alerts", js)

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

if __name__ == "__main__":
    unittest.main()
