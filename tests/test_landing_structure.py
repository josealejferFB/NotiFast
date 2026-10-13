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
        self.assertIn("prefers-color-scheme: dark", css)
        self.assertIn("prefers-reduced-motion", css)

    def test_accessibility_and_touch_styles(self):
        self.assertTrue(os.path.exists("landing/css/styles.css"))
        with open("landing/css/styles.css", "r", encoding="utf-8") as f:
            styles = f.read()
        self.assertIn(":focus-visible", styles)
        self.assertIn(".skip-link", styles)
        self.assertIn("min-height", styles)

    def test_stickers_and_background_js_exist(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        required_icons = ["icon-book", "icon-school", "icon-science", "icon-calculator", "icon-bulb", "icon-pencil"]
        for icon in required_icons:
            self.assertIn(f'id="{icon}"', html)

        self.assertTrue(os.path.exists("landing/js/background.js"))
        with open("landing/js/background.js", "r", encoding="utf-8") as f:
            bg_js = f.read()
        self.assertIn("prefers-reduced-motion", bg_js)

if __name__ == "__main__":
    unittest.main()
