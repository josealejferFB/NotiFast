/* ==========================================================================
   NOTIFAST — POLÍTICAS DE PRIVACIDAD (landing/js/privacy.js)
   Lógica de la página: modo claro/oscuro compartido, sidebar móvil,
   scroll-spy del TOC y copiado de enlace directo.
   ========================================================================== */
(function () {
  'use strict';

  function initThemeToggle() {
    var toggleBtn = document.getElementById('privacy-theme-toggle');
    var labelSpan = document.getElementById('privacy-theme-label');

    function isDark() {
      return document.documentElement.getAttribute('data-theme-mode') === 'dark';
    }

    function applyLabel() {
      if (!labelSpan) return;
      labelSpan.textContent = isDark() ? 'Modo Claro' : 'Modo Oscuro';
    }

    var savedTheme = localStorage.getItem('notifast_theme_mode');
    if (savedTheme === 'dark') {
      document.documentElement.setAttribute('data-theme-mode', 'dark');
    } else {
      document.documentElement.removeAttribute('data-theme-mode');
    }
    applyLabel();

    if (toggleBtn) {
      toggleBtn.addEventListener('click', function () {
        if (isDark()) {
          document.documentElement.removeAttribute('data-theme-mode');
          localStorage.setItem('notifast_theme_mode', 'light');
        } else {
          document.documentElement.setAttribute('data-theme-mode', 'dark');
          localStorage.setItem('notifast_theme_mode', 'dark');
        }
        applyLabel();
      });
    }
  }

  function initMobileSidebar() {
    var toggleBtn = document.getElementById('privacy-mobile-toggle');
    var sidebar = document.getElementById('privacy-sidebar');
    var backdrop = document.getElementById('privacy-sidebar-backdrop');
    if (!toggleBtn || !sidebar) return;

    function openSidebar() {
      sidebar.classList.add('open');
      if (backdrop) {
        backdrop.hidden = false;
        backdrop.style.display = 'block';
        backdrop.classList.add('open');
      }
    }

    function closeSidebar() {
      sidebar.classList.remove('open');
      if (backdrop) {
        backdrop.classList.remove('open');
        backdrop.hidden = true;
        backdrop.style.display = 'none';
      }
    }

    toggleBtn.addEventListener('click', function () {
      if (sidebar.classList.contains('open')) {
        closeSidebar();
      } else {
        openSidebar();
      }
    });

    if (backdrop) {
      backdrop.addEventListener('click', closeSidebar);
    }

    sidebar.querySelectorAll('a[href^="#"]').forEach(function (link) {
      link.addEventListener('click', closeSidebar);
    });
  }

  function initScrollSpy() {
    var sections = document.querySelectorAll('.privacy-section[id]');
    var tocLinks = document.querySelectorAll('#privacy-toc-list .docs-toc-link');
    var sidebarLinks = document.querySelectorAll('#privacy-sidebar .docs-nav-link[href^="#"]');
    if (!sections.length) return;

    var linkMap = {};
    tocLinks.forEach(function (link) {
      linkMap[link.getAttribute('href')] = link;
    });
    sidebarLinks.forEach(function (link) {
      linkMap[link.getAttribute('href')] = linkMap[link.getAttribute('href')] || link;
    });

    function setActive(hash) {
      tocLinks.forEach(function (link) {
        link.classList.toggle('active', link.getAttribute('href') === hash);
      });
      sidebarLinks.forEach(function (link) {
        link.classList.toggle('active', link.getAttribute('href') === hash);
      });
    }

    var observer = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          setActive('#' + entry.target.id);
        }
      });
    }, { rootMargin: '-80px 0px -65% 0px', threshold: 0 });

    sections.forEach(function (section) {
      observer.observe(section);
    });
  }

  function initCopyLink() {
    var copyBtn = document.getElementById('privacy-copy-link-btn');
    if (!copyBtn) return;

    copyBtn.addEventListener('click', function () {
      var url = window.location.origin + '/privacy';
      function done() {
        var label = copyBtn.querySelector('span');
        if (!label) return;
        var original = label.textContent;
        label.textContent = 'Copiado';
        copyBtn.classList.add('copied');
        setTimeout(function () {
          label.textContent = original;
          copyBtn.classList.remove('copied');
        }, 1600);
      }
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(url).then(done);
      } else {
        done();
      }
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    initThemeToggle();
    initMobileSidebar();
    initScrollSpy();
    initCopyLink();
  });
})();
