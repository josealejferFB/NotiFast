import http.server
import os
import socket
import threading
import time
import unittest
import urllib.request

class TestLandingE2E(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        # Find a free port
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.bind(("", 0))
        cls.port = s.getsockname()[1]
        s.close()

        landing_dir = os.path.abspath("landing")
        
        class Handler(http.server.SimpleHTTPRequestHandler):
            def __init__(self, *args, **kwargs):
                super().__init__(*args, directory=landing_dir, **kwargs)
            def log_message(self, format, *args):
                pass # Suppress logs during test

        cls.httpd = http.server.HTTPServer(("127.0.0.1", cls.port), Handler)
        cls.server_thread = threading.Thread(target=cls.httpd.serve_forever)
        cls.server_thread.daemon = True
        cls.server_thread.start()
        time.sleep(0.1)

    @classmethod
    def tearDownClass(cls):
        cls.httpd.shutdown()
        cls.httpd.server_close()

    def get_url(self, path):
        return f"http://127.0.0.1:{self.port}{path}"

    def test_get_root_html(self):
        url = self.get_url("/")
        with urllib.request.urlopen(url) as response:
            self.assertEqual(response.status, 200)
            content_type = response.headers.get("Content-Type", "")
            self.assertIn("text/html", content_type)
            body = response.read().decode("utf-8")
            self.assertIn("<!DOCTYPE html>", body)
            self.assertIn("NotiFast", body)
            self.assertIn("hero-phone-frame", body)
            self.assertIn("section-notes-flow", body)
            self.assertIn("section-grades-lab", body)
            self.assertIn("section-schedule-radar", body)
            self.assertIn("subjects-gallery", body)
            self.assertIn("bento-grid", body)
            self.assertIn("download-toast", body)

    def test_css_assets_serve_correctly(self):
        for css_file in ["/css/tokens.css", "/css/styles.css", "/css/dynamic-showcase.css"]:
            url = self.get_url(css_file)
            with urllib.request.urlopen(url) as response:
                self.assertEqual(response.status, 200)
                content = response.read().decode("utf-8")
                self.assertGreater(len(content), 100)

    def test_js_assets_serve_correctly(self):
        for js_file in ["/js/background.js", "/js/interactive-showcase.js", "/js/main.js"]:
            url = self.get_url(js_file)
            with urllib.request.urlopen(url) as response:
                self.assertEqual(response.status, 200)
                content = response.read().decode("utf-8")
                self.assertGreater(len(content), 100)

    def test_logo_asset_exists_and_serves(self):
        url = self.get_url("/assets/logo.png")
        with urllib.request.urlopen(url) as response:
            self.assertEqual(response.status, 200)
            self.assertEqual(response.headers.get("Content-Type", ""), "image/png")
            content = response.read()
            self.assertGreater(len(content), 1000)

if __name__ == "__main__":
    unittest.main()
