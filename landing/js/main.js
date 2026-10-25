/**
 * NotiFast 3.0 — Main Application Script
 * Manages interactive CTA toasts, smooth navigation, and UI state.
 */

(function () {
  'use strict';

  // Modal / Toast for Google Play Download CTA
  function setupDownloadModal() {
    const downloadButtons = document.querySelectorAll('a[href="#descargar"]');
    const toast = document.getElementById('download-toast');
    const toastCloseBtn = document.getElementById('toast-close-btn');

    if (!toast) return;

    function showToast(e) {
      if (e) e.preventDefault();
      toast.classList.add('visible');
      toast.setAttribute('aria-hidden', 'false');

      // Auto focus close button for accessibility
      if (toastCloseBtn) {
        toastCloseBtn.focus();
      }
    }

    function hideToast() {
      toast.classList.remove('visible');
      toast.setAttribute('aria-hidden', 'true');
    }

    downloadButtons.forEach(btn => {
      btn.addEventListener('click', showToast);
    });

    if (toastCloseBtn) {
      toastCloseBtn.addEventListener('click', hideToast);
    }

    // Close on Escape key
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && toast.classList.contains('visible')) {
        hideToast();
      }
    });

    // Close on backdrop click
    toast.addEventListener('click', (e) => {
      if (e.target === toast) {
        hideToast();
      }
    });
  }

  // Smooth scroll for internal links
  function setupSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
      anchor.addEventListener('click', function (e) {
        const targetId = this.getAttribute('href');
        if (targetId === '#' || targetId === '#descargar') return;

        const targetEl = document.querySelector(targetId);
        if (targetEl) {
          e.preventDefault();
          const isReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
          targetEl.scrollIntoView({
            behavior: isReducedMotion ? 'auto' : 'smooth',
            block: 'start'
          });
        }
      });
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    setupDownloadModal();
    setupSmoothScroll();
  });
})();
