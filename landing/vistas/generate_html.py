#!/usr/bin/env python3
"""
Genera landing/vistas/index.html a partir de screens.json + capture-manifest.json + capturas PNG reales.
Acomoda las vistas en un grid balanceado:
- Las vistas con Claro y Oscuro (is-dual) ocupan span 2 con ambos screenshots lado a lado.
- Las vistas individuales ocupan span 1.
- Todas las capturas mantienen la misma escala nativa y proporción 720×1600.
- Compatible con visualización web responsive y exportación a PDF (A4 Landscape) sin cortes ni desbordes.
"""
import json
import os
import html

BASE = os.path.dirname(os.path.abspath(__file__))
SCREENS_FILE = os.path.join(BASE, 'screens.json')
MANIFEST_FILE = os.path.join(BASE, 'capture-manifest.json')
SCREENS_DIR = os.path.join(BASE, 'screens')

screens = json.load(open(SCREENS_FILE, encoding='utf-8'))['screens']
manifest = {}
if os.path.exists(MANIFEST_FILE):
    m = json.load(open(MANIFEST_FILE, encoding='utf-8'))
    entries = m if isinstance(m, list) else m.get('captures', [])
    for e in entries:
        manifest[e.get('id')] = e

# Estructura lógica por flujo y subflujo académico (11 páginas A4 de alta fidelidad)
SECTIONS = [
    ('Acceso · Flujo Inicial', 'Onboarding → Home · Inicialización y Bienvenida',
     ['01-onboarding', '23-splash']),
    ('Apuntes · Navegación y Panorama', 'Home → Detalle de Materia · Bento Grid',
     ['02-notes-home', '03-add-subject', '04-subject-detail']),
    ('Apuntes · Edición y Creación', 'Editor de Bloques · Sheet de Materia',
     ['05-note-editor', '06-add-subject-sheet']),
    ('Agenda · Horarios Semanales', 'Timeline L-V · Creación y Edición de Clases',
     ['07-schedules', '08-add-schedule', '09-edit-schedule']),
    ('Agenda · Importación Académica', 'Parser de Horarios PDF UNERG · FAB contextual',
     ['20-unerg-import']),
    ('Estudio · Centro de Documentos', 'StudyHub · Plantillas APA · Previsualización',
     ['10-study-hub', '25-cover-template-editor', '26-study-document-preview']),
    ('Estudio · Gestión Documental', 'Acciones de Documento · Organización de Archivos',
     ['27-study-document-actions']),
    ('Calificaciones · Promedios y Ponderación', 'Promedio Global · Evaluación y Ponderaciones',
     ['11-grades', '12-add-evaluation', '13-edit-evaluation']),
    ('Ajustes · Perfil y Respaldo Cloud', 'Perfil Estudiantil · Cloud Hub Google Drive',
     ['14-settings', '15-edit-profile', '17-cloud-hub']),
    ('Ajustes · Personalización y Privacidad', 'Privacidad Offline-First · Selector de Tema',
     ['18-privacy-about', '19-theme-selector']),
    ('Búsqueda y Compartir · Herramientas Globales', 'Búsqueda Multi-Entidad · P2P QR y Archivo',
     ['16-global-search', '21-share-subject', '24-import-subject']),
]

TYPE_LABEL = {'screen': 'pantalla', 'sheet': 'sheet', 'dialog': 'dialog'}
by_id = {s['id']: s for s in screens}

def esc(t):
    return html.escape(str(t), quote=True)

def shot(sid, s, mode):
    fname = s['expectedCaptureFile'] if mode == 'light' else s.get('darkCaptureFile')
    label = 'Claro' if mode == 'light' else 'Oscuro'
    if not s.get('reachable', True):
        return ''
    path = os.path.join(SCREENS_DIR, fname) if fname and fname != 'N/A' else None
    entry = manifest.get(sid, {})
    if path and os.path.exists(path):
        return (
            f'<figure class="shot"><span class="shot-label">{label}</span>'
            f'<div class="phone-frame"><img src="screens/{esc(fname)}" alt="{esc(s["title"])} — {label}" '
            f'loading="eager" decoding="async" width="720" height="1600"></div></figure>'
        )
    if mode == 'dark':
        return ''
    reason = entry.get('notes') or entry.get('reason') or 'No capturada'
    return (
        f'<figure class="shot"><span class="shot-label">{label}</span>'
        f'<div class="shot-missing">{esc(reason)}</div></figure>'
    )

def card(sid):
    s = by_id.get(sid)
    if not s:
        return f'<!-- MISSING {sid} -->'
    reachable = s.get('reachable', True)
    typ = TYPE_LABEL.get(s.get('type', 'screen'), s.get('type', ''))
    
    # Comprobar si tiene modo oscuro disponible en disco
    dark_file = s.get('darkCaptureFile')
    has_dark = bool(dark_file and dark_file != 'N/A' and os.path.exists(os.path.join(SCREENS_DIR, dark_file)))
    
    is_dual = reachable and has_dark
    cls_card = 'view-card' + (' is-dual' if is_dual else '') + ('' if reachable else ' is-unreachable')
    cls_pair = 'shot-pair' + (' is-dual' if is_dual else '')
    
    note = s.get('note') or (s.get('unreachableReason') if not reachable else '') or (s['interestingStates'][0] if s.get('interestingStates') else '')
    note = ' '.join(str(note).split())[:160]
    
    if reachable:
        light_shot = shot(sid, s, 'light')
        dark_shot = shot(sid, s, 'dark') if has_dark else ''
        figs = f'<div class="{cls_pair}">{light_shot}{dark_shot}</div>'
    else:
        figs = f'<div class="{cls_pair}"><figure class="shot"><div class="shot-missing">No alcanzable — sin ruta en NavGraph</div></figure></div>'
        
    return f'''    <article class="{cls_card}">
      <div class="view-header">
        <h3>{esc(s['title'])}</h3>
        <div class="view-meta">
          <code class="view-route">{esc(s.get('route', ''))}</code>
          <span class="view-type {esc(typ)}">{esc(typ)}</span>
        </div>
      </div>
      <p class="view-note">{esc(note)}</p>
      {figs}
    </article>'''

parts = []
for title, path, ids in SECTIONS:
    cards = '\n'.join(card(i) for i in ids)
    parts.append(f'''  <section class="flow">
    <div class="flow-head">
      <h2>{esc(title)}</h2>
      <span class="flow-path">{esc(path)}</span>
    </div>
    <div class="view-grid">
{cards}
    </div>
  </section>''')

total_ok = sum(1 for s in screens if s.get('reachable', True))
total_dark = sum(1 for s in screens if s.get('darkCaptureFile') and os.path.exists(os.path.join(SCREENS_DIR, s['darkCaptureFile'])))

doc = f'''<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>NotiFast · Mapa de vistas</title>
<link rel="stylesheet" href="styles.css">
</head>
<body>
<header class="map-topbar">
  <div class="map-brand-group">
    <a href="/" class="btn-back-landing" title="Volver a la Landing Page principal">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>
      <span>Volver a la Landing</span>
    </a>
    <p class="map-title"><span class="accent">NotiFast</span> · Mapa de vistas</p>
  </div>
  <a href="vistas.pdf" download="NotiFast-Vistas.pdf" class="btn-pdf-download" title="Descargar documento completo en PDF A4">
    <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/></svg>
    <span>Descargar PDF</span>
  </a>
</header>
<p class="map-legend">
  <span class="legend-item"><span class="legend-dot"></span>Claro ({total_ok})</span>
  <span class="legend-item"><span class="legend-dot dark"></span>Oscuro ({total_dark})</span>
  <span class="legend-item"><span class="legend-type">pantalla</span></span>
  <span class="legend-item"><span class="legend-type">sheet</span></span>
  <span class="legend-item"><span class="legend-type">dialog</span></span>
  <span class="legend-item">{total_ok} vistas mapeadas · SM-A065M 720×1600 nativo</span>
</p>
<main>
{chr(10).join(parts)}
</main>
</body>
</html>
'''

out_path = os.path.join(BASE, 'index.html')
with open(out_path, 'w', encoding='utf-8') as f:
    f.write(doc)

print(f'Generado exitosamente {out_path} ({len(doc)} bytes, {total_ok} vistas, {total_dark} oscuras).')
