/* ==========================================================================
   NOTIFAST 3.2 DOCUMENTATION PORTAL LOGIC (landing/js/docs.js)
   Zero Emojis • Instant Search • Client-Side SPA Router • Tactile Papercraft
   ========================================================================== */

(function () {
  'use strict';

  // State
  let docsData = window.NOTIFAST_DOCS_DATA || { docs: [], categories: [] };
  let currentDoc = null;
  let activeFilter = 'all';
  let searchSelectedIndex = 0;
  let currentSearchResults = [];

  // DOM Elements
  const sidebarNav = document.getElementById('docs-sidebar-nav');
  const sidebarFilterInput = document.getElementById('docs-sidebar-filter');
  const docContainer = document.getElementById('docs-content-body');
  const docTitleEl = document.getElementById('docs-current-title');
  const docBreadcrumbEl = document.getElementById('docs-breadcrumb-current');
  const docMetaCategoryEl = document.getElementById('docs-meta-category');
  const docMetaStatsEl = document.getElementById('docs-meta-stats');
  const tocList = document.getElementById('docs-toc-list');
  const paginationContainer = document.getElementById('docs-pagination');
  const mobileToggleBtn = document.getElementById('docs-mobile-toggle');
  const sidebarEl = document.getElementById('docs-sidebar');
  const sidebarBackdrop = document.getElementById('docs-sidebar-backdrop');
  const themeToggleBtn = document.getElementById('docs-theme-toggle');
  const searchTriggerBtn = document.getElementById('docs-search-trigger');
  const searchModal = document.getElementById('docs-search-modal');
  const searchInput = document.getElementById('docs-search-input');
  const searchResultsEl = document.getElementById('docs-search-results');
  const searchFilterChips = document.querySelectorAll('.search-filter-chip');
  const copyDocLinkBtn = document.getElementById('docs-copy-link-btn');

  // Slugifier Helper
  function slugify(text) {
    return text
      .toString()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9\s-]/g, '')
      .trim()
      .replace(/[\s-]+/g, '-');
  }

  // Find doc by id, slug, or filename
  function findDoc(query) {
    if (!query) return docsData.docs[0];
    const cleanQuery = query.replace(/^#/, '').trim();
    if (!cleanQuery) return docsData.docs[0];

    // 1. Direct ID match
    let doc = docsData.docs.find(d => d.id === cleanQuery);
    if (doc) return doc;

    // 2. Slug match (e.g. 01-product/PRD.md or 01-product/PRD)
    doc = docsData.docs.find(d => d.slug.toLowerCase() === cleanQuery.toLowerCase() || d.slug.toLowerCase().replace('.md', '') === cleanQuery.toLowerCase());
    if (doc) return doc;

    // 3. Basename match
    const baseName = cleanQuery.split('/').pop().replace('.md', '');
    doc = docsData.docs.find(d => d.id === baseName || d.slug.toLowerCase().endsWith(cleanQuery.toLowerCase()));
    if (doc) return doc;

    return docsData.docs[0];
  }

  // --------------------------------------------------------------------------
  // THEME MANAGEMENT (Sincronizado con NotiFast Landing y LocalStorage)
  // --------------------------------------------------------------------------
  function initTheme() {
    const savedTheme = localStorage.getItem('notifast_theme_mode');
    if (savedTheme === 'dark') {
      document.documentElement.setAttribute('data-theme-mode', 'dark');
      updateThemeLabel('Modo Claro');
    } else {
      document.documentElement.removeAttribute('data-theme-mode');
      updateThemeLabel('Modo Oscuro');
    }
  }

  function toggleTheme() {
    const isDark = document.documentElement.getAttribute('data-theme-mode') === 'dark';
    if (isDark) {
      document.documentElement.removeAttribute('data-theme-mode');
      localStorage.setItem('notifast_theme_mode', 'light');
      updateThemeLabel('Modo Oscuro');
    } else {
      document.documentElement.setAttribute('data-theme-mode', 'dark');
      localStorage.setItem('notifast_theme_mode', 'dark');
      updateThemeLabel('Modo Claro');
    }
    // Re-render Mermaid diagrams if present
    renderMermaid();
  }

  function updateThemeLabel(label) {
    const labelSpan = document.getElementById('docs-theme-label');
    if (labelSpan) labelSpan.textContent = label;
  }

  // --------------------------------------------------------------------------
  // SIDEBAR RENDERING
  // --------------------------------------------------------------------------
  function renderSidebar(filterText = '') {
    if (!sidebarNav) return;
    const cleanFilter = filterText.toLowerCase().trim();

    let html = '';
    docsData.categories.forEach(cat => {
      // Filter docs
      const matchingDocs = cat.docs.filter(d => {
        if (!cleanFilter) return true;
        return d.title.toLowerCase().includes(cleanFilter) || d.shortTitle.toLowerCase().includes(cleanFilter) || d.id.toLowerCase().includes(cleanFilter);
      });

      if (matchingDocs.length === 0) return;

      const isCurrentCategory = currentDoc && currentDoc.categoryKey === cat.key;
      html += `
        <div class="docs-nav-category" data-cat="${cat.key}">
          <div class="docs-category-header" onclick="this.nextElementSibling.classList.toggle('hidden')">
            <span>${cat.name}</span>
            <span class="docs-category-badge">${matchingDocs.length}</span>
          </div>
          <ul class="docs-nav-list ${isCurrentCategory || cleanFilter ? '' : ''}">
      `;

      matchingDocs.forEach(d => {
        const isActive = currentDoc && currentDoc.id === d.id;
        html += `
          <li class="docs-nav-item">
            <a href="#${d.id}" class="docs-nav-link ${isActive ? 'active' : ''}" data-doc-id="${d.id}">
              <span class="docs-nav-item-text">${d.shortTitle}</span>
              ${d.badge ? `<span class="docs-item-badge">${d.badge}</span>` : ''}
            </a>
          </li>
        `;
      });

      html += `
          </ul>
        </div>
      `;
    });

    sidebarNav.innerHTML = html;

    // Attach click events to close mobile drawer on select
    sidebarNav.querySelectorAll('.docs-nav-link').forEach(link => {
      link.addEventListener('click', () => {
        closeMobileSidebar();
      });
    });
  }

  // --------------------------------------------------------------------------
  // MARKDOWN POST-PROCESSING & RENDERING
  // --------------------------------------------------------------------------
  function processMarkdown(rawMarkdown) {
    if (typeof marked === 'undefined') {
      return `<pre class="docs-raw-fallback">${escapeHtml(rawMarkdown)}</pre>`;
    }

    // Configure marked options
    marked.setOptions({
      gfm: true,
      breaks: false,
      headerIds: true,
      mangle: false
    });

    let html = marked.parse(rawMarkdown);

    // 1. Process Code Blocks with Custom Header & Copy Button
    html = html.replace(/<pre><code class="language-([a-zA-Z0-9_-]+)">([\s\S]*?)<\/code><\/pre>/g, (match, lang, code) => {
      if (lang === 'mermaid') {
        return `<div class="docs-mermaid-container"><pre class="mermaid">${code}</pre></div>`;
      }
      return `
        <div class="docs-code-card">
          <div class="docs-code-header">
            <span class="docs-code-lang">${lang}</span>
            <button class="docs-copy-btn" onclick="copyCodeSnippet(this)">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"/></svg>
              <span>Copiar código</span>
            </button>
          </div>
          <pre><code class="language-${lang}">${code}</code></pre>
        </div>
      `;
    });

    // 2. Wrap plain pre blocks
    html = html.replace(/<pre><code>([\s\S]*?)<\/code><\/pre>/g, (match, code) => {
      return `
        <div class="docs-code-card">
          <div class="docs-code-header">
            <span class="docs-code-lang">TEXTO</span>
            <button class="docs-copy-btn" onclick="copyCodeSnippet(this)">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"/></svg>
              <span>Copiar</span>
            </button>
          </div>
          <pre><code>${code}</code></pre>
        </div>
      `;
    });

    // 3. Wrap Tables with Responsive Scroll Container
    html = html.replace(/<table>([\s\S]*?)<\/table>/g, '<div class="docs-table-wrapper"><table>$1</table></div>');

    // 4. Admonitions / Callouts (GitHub Alert style)
    html = html.replace(/<blockquote>\s*<p>\[!NOTE\]([\s\S]*?)<\/blockquote>/g, '<blockquote class="admonition-note"><p><strong>Nota:</strong> $1</blockquote>');
    html = html.replace(/<blockquote>\s*<p>\[!TIP\]([\s\S]*?)<\/blockquote>/g, '<blockquote class="admonition-tip"><p><strong>Consejo:</strong> $1</blockquote>');
    html = html.replace(/<blockquote>\s*<p>\[!IMPORTANT\]([\s\S]*?)<\/blockquote>/g, '<blockquote class="admonition-important"><p><strong>Importante:</strong> $1</blockquote>');
    html = html.replace(/<blockquote>\s*<p>\[!WARNING\]([\s\S]*?)<\/blockquote>/g, '<blockquote class="admonition-warning"><p><strong>Advertencia:</strong> $1</blockquote>');
    html = html.replace(/<blockquote>\s*<p>\[!CAUTION\]([\s\S]*?)<\/blockquote>/g, '<blockquote class="admonition-caution"><p><strong>Precaución:</strong> $1</blockquote>');

    // 5. Rewrite Relative Internal Markdown Links to SPA Hash Links
    html = html.replace(/<a href="([^"]+\.md(?:#[^"]*)?)"/g, (match, target) => {
      const parts = target.split('#');
      const filePath = parts[0];
      const anchor = parts[1] ? `#${parts[1]}` : '';
      const matched = findDoc(filePath);
      if (matched) {
        return `<a href="#${matched.id}${anchor}" class="docs-internal-link"`;
      }
      return match;
    });

    return html;
  }

  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  // Copy code helper (globally exposed)
  window.copyCodeSnippet = function (btn) {
    const codeCard = btn.closest('.docs-code-card');
    if (!codeCard) return;
    const codeEl = codeCard.querySelector('pre code');
    if (!codeEl) return;
    const text = codeEl.textContent;

    navigator.clipboard.writeText(text).then(() => {
      btn.classList.add('copied');
      const label = btn.querySelector('span');
      if (label) label.textContent = '¡Copiado!';
      setTimeout(() => {
        btn.classList.remove('copied');
        if (label) label.textContent = 'Copiar código';
      }, 2000);
    });
  };

  // --------------------------------------------------------------------------
  // MERMAID & KATEX RENDERING
  // --------------------------------------------------------------------------
  function renderMermaid() {
    if (typeof mermaid === 'undefined') return;
    const isDark = document.documentElement.getAttribute('data-theme-mode') === 'dark';
    try {
      mermaid.initialize({
        startOnLoad: false,
        theme: isDark ? 'dark' : 'neutral',
        securityLevel: 'loose',
        fontFamily: 'Plus Jakarta Sans, sans-serif'
      });
      mermaid.run({
        nodes: document.querySelectorAll('.docs-mermaid-container .mermaid')
      });
    } catch (e) {
      console.warn('Mermaid rendering skipped or failed:', e);
    }
  }

  function renderKaTeX() {
    if (typeof renderMathInElement === 'undefined' || !docContainer) return;
    try {
      renderMathInElement(docContainer, {
        delimiters: [
          { left: '$$', right: '$$', display: true },
          { left: '$', right: '$', display: false },
          { left: '\\[', right: '\\]', display: true },
          { left: '\\(', right: '\\)', display: false }
        ],
        throwOnError: false
      });
    } catch (e) {
      console.warn('KaTeX rendering error:', e);
    }
  }

  // --------------------------------------------------------------------------
  // TOC ("EN ESTE DOCUMENTO") & SCROLLSPY
  // --------------------------------------------------------------------------
  function renderTOC(doc) {
    if (!tocList) return;
    if (!doc.headings || doc.headings.length === 0) {
      tocList.innerHTML = '<li class="docs-toc-empty" style="color:var(--text-muted);font-size:0.8rem;padding:4px 10px;">Vista general</li>';
      return;
    }

    let html = '';
    doc.headings.forEach(h => {
      html += `
        <li class="docs-toc-item level-${h.level}">
          <a href="#${h.id}" class="docs-toc-link" data-heading-id="${h.id}">${h.text}</a>
        </li>
      `;
    });
    tocList.innerHTML = html;

    // Attach click for smooth scroll
    tocList.querySelectorAll('.docs-toc-link').forEach(link => {
      link.addEventListener('click', (e) => {
        e.preventDefault();
        const targetId = link.getAttribute('data-heading-id');
        const targetEl = document.getElementById(targetId);
        if (targetEl) {
          targetEl.scrollIntoView({ behavior: 'smooth' });
          history.replaceState(null, '', `#${currentDoc.id}#${targetId}`);
        }
      });
    });

    setupScrollSpy();
  }

  function setupScrollSpy() {
    const headingElements = docContainer.querySelectorAll('h2, h3');
    if (headingElements.length === 0) return;

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const id = entry.target.id;
            tocList.querySelectorAll('.docs-toc-link').forEach(l => {
              if (l.getAttribute('data-heading-id') === id) {
                l.classList.add('active');
              } else {
                l.classList.remove('active');
              }
            });
          }
        });
      },
      { rootMargin: '0px 0px -70% 0px' }
    );

    headingElements.forEach(el => observer.observe(el));
  }

  // --------------------------------------------------------------------------
  // PAGINATION (ANTERIOR / SIGUIENTE)
  // --------------------------------------------------------------------------
  function renderPagination(doc) {
    if (!paginationContainer) return;
    const currentIndex = docsData.docs.findIndex(d => d.id === doc.id);
    if (currentIndex === -1) {
      paginationContainer.innerHTML = '';
      return;
    }

    const prevDoc = currentIndex > 0 ? docsData.docs[currentIndex - 1] : null;
    const nextDoc = currentIndex < docsData.docs.length - 1 ? docsData.docs[currentIndex + 1] : null;

    let html = '';
    if (prevDoc) {
      html += `
        <a href="#${prevDoc.id}" class="docs-page-card prev">
          <span class="docs-page-sub">← Documento anterior</span>
          <span class="docs-page-title">${prevDoc.shortTitle}</span>
        </a>
      `;
    } else {
      html += `<div></div>`;
    }

    if (nextDoc) {
      html += `
        <a href="#${nextDoc.id}" class="docs-page-card next">
          <span class="docs-page-sub">Siguiente documento →</span>
          <span class="docs-page-title">${nextDoc.shortTitle}</span>
        </a>
      `;
    }

    paginationContainer.innerHTML = html;
  }

  // --------------------------------------------------------------------------
  // DOCUMENT VIEW CONTROLLER
  // --------------------------------------------------------------------------
  function displayDoc(docId, targetHeadingId = null) {
    const doc = findDoc(docId);
    if (!doc) return;
    currentDoc = doc;

    // Update document title and metadata
    document.title = `${doc.title} — Documentación NotiFast`;
    if (docTitleEl) docTitleEl.textContent = doc.title;
    if (docBreadcrumbEl) docBreadcrumbEl.textContent = doc.shortTitle;
    if (docMetaCategoryEl) docMetaCategoryEl.textContent = doc.category;
    if (docMetaStatsEl) {
      docMetaStatsEl.textContent = `~${doc.readTimeMinutes} min de lectura • ${doc.wordCount.toLocaleString()} palabras`;
    }

    // Process and render Markdown body
    const processedHtml = processMarkdown(doc.content);
    if (docContainer) {
      docContainer.innerHTML = processedHtml;

      // Assign IDs to H2 and H3 tags to match headings array
      const hElements = docContainer.querySelectorAll('h2, h3');
      hElements.forEach(h => {
        const cleanId = slugify(h.textContent);
        h.id = cleanId;
      });
    }

    // Render Sub-components
    renderSidebar(sidebarFilterInput ? sidebarFilterInput.value : '');
    renderTOC(doc);
    renderPagination(doc);

    // Apply Syntax Highlighting via Prism if available
    if (typeof Prism !== 'undefined' && docContainer) {
      Prism.highlightAllUnder(docContainer);
    }

    // Apply KaTeX and Mermaid
    renderKaTeX();
    renderMermaid();

    // Scroll handling
    if (targetHeadingId) {
      setTimeout(() => {
        const target = document.getElementById(targetHeadingId);
        if (target) target.scrollIntoView({ behavior: 'smooth' });
      }, 100);
    } else {
      window.scrollTo({ top: 0, behavior: 'instant' });
    }
  }

  // --------------------------------------------------------------------------
  // ROUTING & HASH HANDLING
  // --------------------------------------------------------------------------
  function handleHashChange() {
    const rawHash = window.location.hash.replace(/^#/, '');
    if (!rawHash) {
      displayDoc('readme');
      return;
    }

    // Check for double hash (e.g. #prd#fr-001 or #prd/fr-001)
    const parts = rawHash.split(/[#/]/);
    const docId = parts[0];
    const headingId = parts.length > 1 ? parts[1] : null;

    displayDoc(docId, headingId);
  }

  // --------------------------------------------------------------------------
  // INSTANT SEARCH MODAL (Ctrl + K)
  // --------------------------------------------------------------------------
  function openSearchModal() {
    if (!searchModal) return;
    searchModal.removeAttribute('hidden');
    searchModal.classList.add('open');
    searchModal.style.display = 'flex';
    if (searchInput) {
      searchInput.value = '';
      setTimeout(() => {
        searchInput.focus();
      }, 50);
    }
    performSearch('');
  }

  function closeSearchModal() {
    if (!searchModal) return;
    searchModal.setAttribute('hidden', '');
    searchModal.classList.remove('open');
    searchModal.style.display = 'none';
  }

  function performSearch(query) {
    if (!searchResultsEl) return;
    const cleanQuery = query.toLowerCase().trim();

    let matches = [];

    docsData.docs.forEach(doc => {
      // Category filter check
      if (activeFilter === 'requirements' && !doc.category.includes('Producto')) return;
      if (activeFilter === 'adr' && !doc.category.includes('ADR')) return;
      if (activeFilter === 'architecture' && !doc.category.includes('Arquitectura')) return;
      if (activeFilter === 'glossary' && doc.id !== 'glossary') return;

      if (!cleanQuery) {
        // Show all in default order
        matches.push({
          doc,
          score: 1,
          snippet: doc.headings.length ? doc.headings.map(h => h.text).slice(0, 3).join(' • ') : 'Documento técnico'
        });
        return;
      }

      let score = 0;
      let matchedSnippet = '';

      // Title match
      if (doc.title.toLowerCase().includes(cleanQuery)) {
        score += 50;
      }
      if (doc.shortTitle.toLowerCase().includes(cleanQuery)) {
        score += 40;
      }
      // ID match
      if (doc.id.toLowerCase().includes(cleanQuery)) {
        score += 30;
      }
      // Search tokens match (FR-XXX, NFR-XXX, ADR-XXX)
      if (doc.searchTokens && doc.searchTokens.toLowerCase().includes(cleanQuery)) {
        score += 35;
      }
      // Content match
      const contentIdx = doc.content.toLowerCase().indexOf(cleanQuery);
      if (contentIdx !== -1) {
        score += 15;
        const start = Math.max(0, contentIdx - 40);
        const end = Math.min(doc.content.length, contentIdx + cleanQuery.length + 80);
        let excerpt = doc.content.substring(start, end).replace(/\n/g, ' ');
        // Highlight query in snippet
        const regex = new RegExp(`(${cleanQuery})`, 'gi');
        matchedSnippet = excerpt.replace(regex, '<mark>$1</mark>');
      }

      if (score > 0) {
        matches.push({
          doc,
          score,
          snippet: matchedSnippet || (doc.headings.length ? doc.headings.map(h => h.text).slice(0, 3).join(' • ') : '')
        });
      }
    });

    // Sort by score
    matches.sort((a, b) => b.score - a.score);
    currentSearchResults = matches.slice(0, 20);
    searchSelectedIndex = 0;

    renderSearchResults();
  }

  function renderSearchResults() {
    if (!searchResultsEl) return;
    if (currentSearchResults.length === 0) {
      searchResultsEl.innerHTML = `
        <div class="search-empty-state">
          <p>No se encontraron resultados para la búsqueda.</p>
          <span style="font-size:0.75rem;color:var(--text-muted);">Prueba con términos como: PRD, FR-001, ADR, Room SQLite, GradePolicy, Tesis</span>
        </div>
      `;
      return;
    }

    let html = '';
    currentSearchResults.forEach((res, idx) => {
      const isSelected = idx === searchSelectedIndex;
      html += `
        <li class="search-result-item ${isSelected ? 'selected' : ''}" data-idx="${idx}" onclick="selectSearchResult(${idx})">
          <div class="search-res-top">
            <span class="search-res-title">${res.doc.shortTitle}</span>
            <span class="search-res-category">${res.doc.category}</span>
          </div>
          <div class="search-res-snippet">${res.snippet || 'Ver documento...'}</div>
        </li>
      `;
    });

    searchResultsEl.innerHTML = html;
  }

  window.selectSearchResult = function (idx) {
    if (!currentSearchResults[idx]) return;
    const targetDoc = currentSearchResults[idx].doc;
    closeSearchModal();
    window.location.hash = `#${targetDoc.id}`;
  };

  // Keyboard navigation inside search modal
  function handleSearchKeyDown(e) {
    if (searchModal.hasAttribute('hidden')) return;

    if (e.key === 'Escape') {
      closeSearchModal();
      return;
    }
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      searchSelectedIndex = (searchSelectedIndex + 1) % Math.max(1, currentSearchResults.length);
      renderSearchResults();
      scrollSelectedResultIntoView();
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      searchSelectedIndex = (searchSelectedIndex - 1 + currentSearchResults.length) % Math.max(1, currentSearchResults.length);
      renderSearchResults();
      scrollSelectedResultIntoView();
    } else if (e.key === 'Enter') {
      e.preventDefault();
      selectSearchResult(searchSelectedIndex);
    }
  }

  function scrollSelectedResultIntoView() {
    const selectedEl = searchResultsEl.querySelector('.search-result-item.selected');
    if (selectedEl) {
      selectedEl.scrollIntoView({ block: 'nearest' });
    }
  }

  // --------------------------------------------------------------------------
  // MOBILE SIDEBAR DRAWER
  // --------------------------------------------------------------------------
  function openMobileSidebar() {
    if (sidebarEl) sidebarEl.classList.add('open');
    if (sidebarBackdrop) {
      sidebarBackdrop.removeAttribute('hidden');
      sidebarBackdrop.classList.add('open');
      sidebarBackdrop.style.display = 'block';
    }
  }

  function closeMobileSidebar() {
    if (sidebarEl) sidebarEl.classList.remove('open');
    if (sidebarBackdrop) {
      sidebarBackdrop.setAttribute('hidden', '');
      sidebarBackdrop.classList.remove('open');
      sidebarBackdrop.style.display = 'none';
    }
  }

  // --------------------------------------------------------------------------
  // COPY DOCUMENT LINK
  // --------------------------------------------------------------------------
  function setupCopyDocLink() {
    if (!copyDocLinkBtn) return;
    copyDocLinkBtn.addEventListener('click', () => {
      const url = window.location.href;
      navigator.clipboard.writeText(url).then(() => {
        const originalText = copyDocLinkBtn.textContent;
        copyDocLinkBtn.textContent = '¡Enlace copiado!';
        setTimeout(() => {
          copyDocLinkBtn.textContent = originalText;
        }, 2000);
      });
    });
  }

  // --------------------------------------------------------------------------
  // INITIALIZATION
  // --------------------------------------------------------------------------
  function init() {
    initTheme();

    // Event Listeners
    window.addEventListener('hashchange', handleHashChange);

    if (themeToggleBtn) {
      themeToggleBtn.addEventListener('click', toggleTheme);
    }

    if (searchTriggerBtn) {
      searchTriggerBtn.addEventListener('click', openSearchModal);
    }

    if (searchInput) {
      searchInput.addEventListener('input', (e) => {
        performSearch(e.target.value);
      });
      searchInput.addEventListener('keydown', handleSearchKeyDown);
    }

    // Global shortcut Ctrl+K / Cmd+K
    window.addEventListener('keydown', (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        const isOpen = searchModal && !searchModal.hasAttribute('hidden') && searchModal.classList.contains('open');
        if (isOpen) {
          closeSearchModal();
        } else {
          openSearchModal();
        }
      }
      if (e.key === 'Escape') {
        closeSearchModal();
        closeMobileSidebar();
      }
    });

    // Close modal when clicking backdrop outside card
    if (searchModal) {
      searchModal.addEventListener('click', (e) => {
        if (e.target === searchModal) {
          closeSearchModal();
        }
      });
    }

    const searchCloseBtn = document.getElementById('docs-search-close-btn');
    if (searchCloseBtn) {
      searchCloseBtn.addEventListener('click', closeSearchModal);
    }

    // Search filters
    searchFilterChips.forEach(chip => {
      chip.addEventListener('click', () => {
        searchFilterChips.forEach(c => c.classList.remove('active'));
        chip.classList.add('active');
        activeFilter = chip.getAttribute('data-filter') || 'all';
        performSearch(searchInput ? searchInput.value : '');
      });
    });

    // Sidebar filter
    if (sidebarFilterInput) {
      sidebarFilterInput.addEventListener('input', (e) => {
        renderSidebar(e.target.value);
      });
    }

    // Mobile menu toggle
    if (mobileToggleBtn) {
      mobileToggleBtn.addEventListener('click', () => {
        if (sidebarEl && sidebarEl.classList.contains('open')) {
          closeMobileSidebar();
        } else {
          openMobileSidebar();
        }
      });
    }
    if (sidebarBackdrop) {
      sidebarBackdrop.addEventListener('click', closeMobileSidebar);
    }

    setupCopyDocLink();

    // Initial render
    handleHashChange();
  }

  // Run on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

})();
