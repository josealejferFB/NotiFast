#!/usr/bin/env python3
"""
Script oficial de exportación a PDF para landing/vistas.
Convierte landing/vistas/index.html en un PDF A4 apaisado (landscape) de alta fidelidad,
asegurando que ninguna tarjeta ni marco de teléfono se corte, desborde o pierda estilos.

Uso:
    python3 landing/vistas/export_pdf.py
"""
import asyncio
import os
import sys
import base64
from playwright.async_api import async_playwright

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
HTML_PATH = os.path.join(BASE_DIR, 'index.html')
PDF_PATH = os.path.join(BASE_DIR, 'vistas.pdf')
SCREENS_DIR = os.path.join(BASE_DIR, 'screens')

async def export():
    if not os.path.exists(HTML_PATH):
        print(f"Error: No se encuentra {HTML_PATH}", file=sys.stderr)
        sys.exit(1)

    print(f"Iniciando exportación de {HTML_PATH} a PDF...")

    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        # Usamos viewport de 1440x900 para emular resolución de escritorio
        page = await browser.new_page(viewport={'width': 1440, 'height': 900})

        # Cargar index.html
        url = f"file://{HTML_PATH}"
        await page.goto(url, wait_until='networkidle')

        # 1. Esperar a que las fuentes locales (Plus Jakarta Sans y JetBrains Mono) estén listas
        await page.evaluate("() => document.fonts.ready")

        # 2. Convertir imágenes locales a Data URLs inline para garantizar renderizado instantáneo en el motor de impresión de Chromium
        images_data = []
        for f in os.listdir(SCREENS_DIR):
            if f.endswith('.png'):
                full_p = os.path.join(SCREENS_DIR, f)
                with open(full_p, 'rb') as img_f:
                    b64 = base64.b64encode(img_f.read()).decode('utf-8')
                    images_data.append([f"screens/{f}", f"data:image/png;base64,{b64}"])

        inlined_count = await page.evaluate("""(entries) => {
            const map = new Map(entries);
            const imgs = [...document.querySelectorAll('img')];
            for (const img of imgs) {
                const key = img.getAttribute('src');
                const dataUrl = map.get(key);
                if (dataUrl) {
                    img.src = dataUrl;
                }
                img.loading = 'eager';
            }
            return imgs.length;
        }""", images_data)

        # 3. Forzar decodificación síncrona de todas las imágenes
        await page.evaluate("""async () => {
            const imgs = [...document.querySelectorAll('img')];
            await Promise.all(imgs.map(i => i.decode().catch(() => {})));
        }""")

        # 4. Scroll progresivo para pintar capas y layouts
        await page.evaluate("""async () => {
            const step = Math.floor(window.innerHeight * 0.8);
            for (let y = 0; y < document.documentElement.scrollHeight; y += step) {
                window.scrollTo(0, y);
                await new Promise(r => setTimeout(r, 60));
            }
            window.scrollTo(0, 0);
            await new Promise(r => setTimeout(r, 150));
        }""")

        await page.wait_for_timeout(300)

        # 5. Generar PDF A4 apaisado con gráficos de fondo y márgenes exactos
        await page.pdf(
            path=PDF_PATH,
            format='A4',
            landscape=True,
            print_background=True,
            margin={'top': '8mm', 'bottom': '8mm', 'left': '10mm', 'right': '10mm'},
            prefer_css_page_size=True
        )

        await browser.close()

    file_size_kb = os.path.getsize(PDF_PATH) / 1024
    print(f"PDF generado con éxito: {PDF_PATH} ({file_size_kb:.1f} KB, {inlined_count} imágenes inlineadas).")

if __name__ == '__main__':
    asyncio.run(export())
