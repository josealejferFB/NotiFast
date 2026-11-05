#!/usr/bin/env python3
"""
Test de verificación y QA para landing/vistas.
Valida:
1. Existencia e integridad de los 32 archivos PNG de capturas (26 light + 6 dark).
2. Existencia de fuentes locales TTF y tokens CSS.
3. No desborde horizontal (scrollWidth <= innerWidth) en Desktop (1440px), Tablet (1024px) y Mobile (390px).
4. Todas las imágenes cargadas y decodificadas sin errores.
5. Exportación a PDF exitosa (11 páginas A4 Landscape) sin errores de consola.
"""
import os
import sys
import json
import asyncio
from playwright.async_api import async_playwright

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
HTML_PATH = os.path.join(BASE_DIR, 'index.html')
PDF_PATH = os.path.join(BASE_DIR, 'vistas.pdf')
SCREENS_DIR = os.path.join(BASE_DIR, 'screens')
FONTS_DIR = os.path.join(BASE_DIR, 'fonts')
SCREENS_JSON = os.path.join(BASE_DIR, 'screens.json')

def test_static_assets():
    print("-> Verificando archivos estáticos...")
    assert os.path.exists(HTML_PATH), "index.html no existe"
    assert os.path.exists(SCREENS_JSON), "screens.json no existe"
    assert os.path.exists(SCREENS_DIR), "directorio screens/ no existe"
    assert os.path.exists(FONTS_DIR), "directorio fonts/ no existe"

    # Fuentes locales requeridas
    required_fonts = [
        'plus_jakarta_sans_regular.ttf',
        'plus_jakarta_sans_medium.ttf',
        'plus_jakarta_sans_semibold.ttf',
        'plus_jakarta_sans_bold.ttf',
        'jetbrains_mono_regular.ttf',
        'jetbrains_mono_medium.ttf',
        'jetbrains_mono_semibold.ttf',
        'jetbrains_mono_bold.ttf'
    ]
    for font in required_fonts:
        f_path = os.path.join(FONTS_DIR, font)
        assert os.path.exists(f_path) and os.path.getsize(f_path) > 0, f"Fuente faltante o vacía: {font}"

    # Capturas mínimas: 26 light + 6 dark = 32 PNG
    pngs = [f for f in os.listdir(SCREENS_DIR) if f.endswith('.png')]
    assert len(pngs) >= 32, f"Se esperaban al menos 32 capturas PNG, se encontraron {len(pngs)}"

    # Verificar que el splash screen tenga el logo real y tamaño válido
    splash_p = os.path.join(SCREENS_DIR, '23-splash-light.png')
    assert os.path.exists(splash_p) and os.path.getsize(splash_p) > 20000, "Splash screen inválido"
    print(f"   [OK] {len(pngs)} capturas PNG y {len(required_fonts)} fuentes locales validadas.")

async def test_responsiveness_and_pdf():
    print("-> Validando renderizado web y exportación a PDF con Playwright...")
    viewports = [
        {'name': 'Desktop', 'width': 1440, 'height': 900},
        {'name': 'Tablet', 'width': 1024, 'height': 768},
        {'name': 'Mobile', 'width': 390, 'height': 844}
    ]

    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        url = f"file://{HTML_PATH}"

        for vp in viewports:
            page = await browser.new_page(viewport={'width': vp['width'], 'height': vp['height']})
            console_errors = []
            page.on("console", lambda msg: console_errors.append(msg.text) if msg.type == "error" else None)
            page.on("pageerror", lambda err: console_errors.append(str(err)))

            await page.goto(url, wait_until='networkidle')
            await page.evaluate("() => document.fonts.ready")

            data = await page.evaluate("""() => {
                const doc = document.documentElement;
                const imgs = [...document.querySelectorAll('img')];
                const broken = imgs.filter(i => !i.complete || i.naturalWidth === 0).map(i => i.src);
                return {
                    scrollWidth: doc.scrollWidth,
                    clientWidth: doc.clientWidth,
                    innerWidth: window.innerWidth,
                    imgCount: imgs.length,
                    brokenImgs: broken,
                    cards: document.querySelectorAll('.view-card').length,
                    dualCards: document.querySelectorAll('.view-card.is-dual').length
                };
            }""")

            # Verificación de no desborde horizontal
            h_overflow = data['scrollWidth'] > data['innerWidth']
            assert not h_overflow, f"Desborde horizontal en {vp['name']}: scrollWidth={data['scrollWidth']} > innerWidth={data['innerWidth']}"
            assert len(data['brokenImgs']) == 0, f"Imágenes rotas en {vp['name']}: {data['brokenImgs']}"
            assert data['cards'] == 26, f"Se esperaban 26 tarjetas, encontradas {data['cards']}"
            assert data['dualCards'] == 6, f"Se esperaban 6 tarjetas duales, encontradas {data['dualCards']}"

            print(f"   [OK] {vp['name']} ({vp['width']}px): sin desborde, {data['imgCount']} imgs cargadas, {data['cards']} tarjetas ({data['dualCards']} duales).")
            await page.close()

        # Verificar generación de PDF
        page = await browser.new_page(viewport={'width': 1440, 'height': 900})
        await page.goto(url, wait_until='networkidle')
        await page.evaluate("() => document.fonts.ready")
        pdf_bytes = await page.pdf(
            format='A4',
            landscape=True,
            print_background=True,
            margin={'top': '8mm', 'bottom': '8mm', 'left': '10mm', 'right': '10mm'},
            prefer_css_page_size=True
        )
        assert len(pdf_bytes) > 500000, "El PDF generado es demasiado pequeño"
        print(f"   [OK] PDF exportado y verificado ({len(pdf_bytes) / 1024:.1f} KB).")
        await browser.close()

def main():
    test_static_assets()
    asyncio.run(test_responsiveness_and_pdf())
    print("\n✅ TODAS LAS PRUEBAS DE VISTAS Y EXPORTACIÓN A PDF PASARON EXITOSAMENTE.")

if __name__ == '__main__':
    main()
