#!/usr/bin/env python3
"""
build_docs_data.py
Compila los 25 archivos Markdown de docs/ en landing/js/docs-data.js
para permitir búsqueda instantánea, renderizado offline y SPA sin dependencias de Node.
"""

import os
import re
import glob
import json
import unicodedata

REPO_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
DOCS_DIR = os.path.join(REPO_ROOT, "docs")
OUTPUT_FILE = os.path.join(REPO_ROOT, "landing", "js", "docs-data.js")

def slugify(text):
    text = unicodedata.normalize('NFKD', text).encode('ascii', 'ignore').decode('utf-8')
    text = re.sub(r'[^\w\s-]', '', text.lower()).strip()
    return re.sub(r'[-\s]+', '-', text)

def clean_title(raw_title):
    # Remove leading '# ' and any trailing markers
    t = re.sub(r'^#+\s*', '', raw_title).strip()
    return t

def extract_headings(markdown_text):
    headings = []
    lines = markdown_text.splitlines()
    in_code_block = False
    for line in lines:
        if line.strip().startswith("```"):
            in_code_block = not in_code_block
            continue
        if in_code_block:
            continue
        m = re.match(r'^(#{2,3})\s+(.+)$', line)
        if m:
            level = len(m.group(1))
            raw_text = m.group(2).strip()
            # Clean text from links, markdown formatting
            clean_text = re.sub(r'\[([^\]]+)\]\([^)]+\)', r'\1', raw_text)
            clean_text = re.sub(r'[`*_]', '', clean_text)
            h_id = slugify(clean_text)
            headings.append({
                "level": level,
                "id": h_id,
                "text": clean_text
            })
    return headings

def extract_search_tokens(markdown_text):
    tokens = set()
    # Functional / Non-functional / ADR codes
    for match in re.findall(r'\b(?:FR|NFR|ADR|CR|UC|US)-\d+\b', markdown_text):
        tokens.add(match)
    # Technical keywords
    for match in re.findall(r'\b[A-Z][a-zA-Z0-9_]{3,}\b', markdown_text):
        tokens.add(match)
    return " ".join(sorted(list(tokens)))

def get_doc_metadata(rel_path):
    p = rel_path.replace("\\", "/")
    if p == "README.md":
        return {
            "id": "readme",
            "category": "Inicio",
            "categoryKey": "inicio",
            "categoryOrder": -1,
            "order": 0,
            "badge": "Índice Maestro",
            "shortTitle": "Índice Maestro"
        }
    elif "00-vision" in p:
        is_vision = "product-vision" in p
        return {
            "id": "product-vision" if is_vision else "glossary",
            "category": "00 - Visión",
            "categoryKey": "00-vision",
            "categoryOrder": 0,
            "order": 0 if is_vision else 1,
            "badge": "Visión",
            "shortTitle": "Visión del Producto" if is_vision else "Glosario Canónico"
        }
    elif "01-product" in p:
        order_map = {
            "PRD.md": (0, "prd", "PRD Maestro", "PRD Maestro"),
            "user-stories.md": (1, "user-stories", "Historias de Usuario", "Historias de Usuario"),
            "personas.md": (2, "personas", "Personas & Arquetipos", "Personas"),
            "roadmap.md": (3, "roadmap", "Roadmap & Fases", "Roadmap"),
            "success-metrics.md": (4, "success-metrics", "Métricas de Éxito", "Métricas")
        }
        fname = os.path.basename(p)
        info = order_map.get(fname, (9, slugify(fname), fname, "Producto"))
        return {
            "id": info[1],
            "category": "01 - Producto",
            "categoryKey": "01-product",
            "categoryOrder": 1,
            "order": info[0],
            "badge": "Producto",
            "shortTitle": info[2]
        }
    elif "02-design" in p:
        order_map = {
            "design-system.md": (0, "design-system", "Sistema de Diseño 3.2", "Design System"),
            "ux-flows.md": (1, "ux-flows", "Flujos de Usuario", "Flujos UX"),
            "logo-brief.md": (2, "logo-brief", "Brief de Marca & Logo", "Brief Logo")
        }
        fname = os.path.basename(p)
        info = order_map.get(fname, (9, slugify(fname), fname, "Diseño"))
        return {
            "id": info[1],
            "category": "02 - Diseño",
            "categoryKey": "02-design",
            "categoryOrder": 2,
            "order": info[0],
            "badge": "Diseño",
            "shortTitle": info[2]
        }
    elif "03-architecture/adr" in p:
        fname = os.path.basename(p)
        if fname == "README.md":
            return {
                "id": "adr-index",
                "category": "03 - Arquitectura (ADRs)",
                "categoryKey": "03-architecture-adr",
                "categoryOrder": 4,
                "order": 0,
                "badge": "ADRs",
                "shortTitle": "Índice de ADRs"
            }
        else:
            m = re.search(r"(\d+)", fname)
            num = int(m.group(1)) if m else 99
            adr_code = f"ADR-{num:04d}"
            # short titles for ADRs
            adr_short_titles = {
                1: "ADR-0001: Room SQLite",
                2: "ADR-0002: Drive Backup",
                3: "ADR-0003: Notificaciones",
                4: "ADR-0004: Temas Dinámicos",
                5: "ADR-0005: GradePolicy",
                6: "ADR-0006: P2P & QR Pack",
                7: "ADR-0007: Parser Horario PDF"
            }
            return {
                "id": f"adr-{num:04d}",
                "category": "03 - Arquitectura (ADRs)",
                "categoryKey": "03-architecture-adr",
                "categoryOrder": 4,
                "order": num,
                "badge": adr_code,
                "shortTitle": adr_short_titles.get(num, adr_code)
            }
    elif "03-architecture" in p:
        order_map = {
            "system-overview.md": (0, "system-overview", "Arquitectura del Sistema", "Clean Arch"),
            "data-model.md": (1, "data-model", "Modelo de Datos SQLite v9", "Room SQLite v9"),
            "integrations.md": (2, "integrations", "Integraciones & Drive", "Integraciones")
        }
        fname = os.path.basename(p)
        info = order_map.get(fname, (9, slugify(fname), fname, "Arquitectura"))
        return {
            "id": info[1],
            "category": "03 - Arquitectura",
            "categoryKey": "03-architecture",
            "categoryOrder": 3,
            "order": info[0],
            "badge": "Arquitectura",
            "shortTitle": info[2]
        }
    elif "04-quality-privacy" in p:
        order_map = {
            "testing-strategy.md": (0, "testing-strategy", "Estrategia de Pruebas", "805 Tests"),
            "privacy-offline-first.md": (1, "privacy-offline-first", "Privacidad Offline-First", "Privacidad")
        }
        fname = os.path.basename(p)
        info = order_map.get(fname, (9, slugify(fname), fname, "Calidad"))
        return {
            "id": info[1],
            "category": "04 - Calidad & Privacidad",
            "categoryKey": "04-quality-privacy",
            "categoryOrder": 5,
            "order": info[0],
            "badge": "Calidad" if info[0] == 1 else "805 Tests",
            "shortTitle": info[2]
        }
    elif "05-legal" in p:
        return {
            "id": "legal-notes",
            "category": "05 - Legal",
            "categoryKey": "05-legal",
            "categoryOrder": 6,
            "order": 0,
            "badge": "Legal",
            "shortTitle": "Notas Legales & Tesis"
        }
    else:
        return {
            "id": slugify(os.path.basename(p).replace(".md", "")),
            "category": "General",
            "categoryKey": "general",
            "categoryOrder": 9,
            "order": 0,
            "badge": "Doc",
            "shortTitle": os.path.basename(p)
        }

def build_docs():
    raw_files = sorted(list(set(glob.glob(os.path.join(DOCS_DIR, "**", "*.md"), recursive=True) + glob.glob(os.path.join(DOCS_DIR, "*.md")))))
    
    docs = []
    
    for fpath in raw_files:
        rel_path = os.path.relpath(fpath, DOCS_DIR)
        with open(fpath, "r", encoding="utf-8") as fp:
            content = fp.read()
            
        m = re.search(r"^#\s+(.+)$", content, re.MULTILINE)
        raw_title = m.group(1).strip() if m else os.path.basename(fpath).replace(".md", "")
        title = clean_title(raw_title)
        
        meta = get_doc_metadata(rel_path)
        headings = extract_headings(content)
        search_tokens = extract_search_tokens(content)
        
        words = len(content.split())
        read_time = max(1, round(words / 200))
        
        docs.append({
            "id": meta["id"],
            "slug": rel_path.replace("\\", "/"),
            "title": title,
            "shortTitle": meta["shortTitle"],
            "category": meta["category"],
            "categoryKey": meta["categoryKey"],
            "categoryOrder": meta["categoryOrder"],
            "order": meta["order"],
            "badge": meta["badge"],
            "headings": headings,
            "wordCount": words,
            "readTimeMinutes": read_time,
            "searchTokens": search_tokens,
            "content": content
        })

    # Sort docs: first by categoryOrder, then by order
    docs.sort(key=lambda d: (d["categoryOrder"], d["order"], d["title"]))

    # Group into categories for sidebar navigation
    categories_map = {}
    for d in docs:
        ck = d["categoryKey"]
        if ck not in categories_map:
            categories_map[ck] = {
                "key": ck,
                "name": d["category"],
                "order": d["categoryOrder"],
                "docs": []
            }
        categories_map[ck]["docs"].append({
            "id": d["id"],
            "slug": d["slug"],
            "title": d["title"],
            "shortTitle": d["shortTitle"],
            "badge": d["badge"],
            "readTimeMinutes": d["readTimeMinutes"]
        })

    categories = sorted(list(categories_map.values()), key=lambda c: c["order"])

    data = {
        "generatedAt": "2026-10-27T11:40:00Z",
        "totalDocs": len(docs),
        "categories": categories,
        "docs": docs
    }

    os.makedirs(os.path.dirname(OUTPUT_FILE), exist_ok=True)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as fp:
        fp.write("/* Auto-generated by scripts/build_docs_data.py - DO NOT EDIT MANUALLY */\n")
        fp.write("window.NOTIFAST_DOCS_DATA = ")
        json.dump(data, fp, ensure_ascii=False, indent=2)
        fp.write(";\n")

    print(f"✅ Successfully compiled {len(docs)} documents into {OUTPUT_FILE}")
    print(f"   Categories: {len(categories)}")

if __name__ == "__main__":
    build_docs()
