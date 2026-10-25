/**
 * NotiFast 3.0 — Scrollytelling Engine
 * Hardened with UI/UX Pro Max and Systematic Debugging:
 * - Uses gsap.matchMedia() to enable pinning ONLY on desktop (>= 992px)
 * - Avoids mobile scroll-traps by using tabs & native IntersectionObserver (< 992px)
 * - Respects prefers-reduced-motion
 * - Includes complete fallback if GSAP is blocked/unavailable
 */

(function () {
  'use strict';

  // Screen switching helper
  function switchPhoneScreen(screenName) {
    const screens = document.querySelectorAll('.phone-screen');
    const navItems = document.querySelectorAll('.device-bottom-nav .nav-item');
    const mobileTabs = document.querySelectorAll('.mobile-tab-btn');
    const storySteps = document.querySelectorAll('.story-step');

    // Update phone screens
    screens.forEach(screen => {
      const isTarget = screen.classList.contains(`screen-${screenName}`);
      screen.classList.toggle('active', isTarget);
    });

    // Update bottom nav in mockup
    navItems.forEach(item => {
      const isTarget = item.getAttribute('data-nav') === screenName;
      item.classList.toggle('active', isTarget);
    });

    // Update mobile tab buttons
    mobileTabs.forEach(tab => {
      const isTarget = tab.getAttribute('data-screen') === screenName;
      tab.classList.toggle('active', isTarget);
      tab.setAttribute('aria-selected', isTarget ? 'true' : 'false');
    });

    // Update story step active highlight
    storySteps.forEach(step => {
      const isTarget = step.getAttribute('data-screen') === screenName;
      step.classList.toggle('active-step', isTarget);
    });
  }

  function prefersReducedMotion() {
    return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  }

  // Setup mobile button listeners
  function setupMobileTabs() {
    const mobileTabs = document.querySelectorAll('.mobile-tab-btn');
    mobileTabs.forEach(tab => {
      tab.addEventListener('click', () => {
        const screenName = tab.getAttribute('data-screen');
        switchPhoneScreen(screenName);

        // Scroll to corresponding article respecting motion preferences
        const targetStep = document.getElementById(screenName);
        if (targetStep) {
          targetStep.scrollIntoView({
            behavior: prefersReducedMotion() ? 'auto' : 'smooth',
            block: 'nearest'
          });
        }
      });
    });
  }

  // Native IntersectionObserver Fallback (Zero-Dependency & Mobile)
  function setupNativeObserver() {
    if (!('IntersectionObserver' in window)) return;

    const steps = document.querySelectorAll('.story-step');
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const screenName = entry.target.getAttribute('data-screen');
            if (screenName) {
              switchPhoneScreen(screenName);
            }
          }
        });
      },
      { threshold: 0.5 }
    );

    steps.forEach(step => observer.observe(step));
  }

  // GSAP ScrollTrigger Integration
  function initGSAPScrollytelling() {
    if (prefersReducedMotion()) {
      setupNativeObserver();
      return;
    }

    if (typeof gsap === 'undefined' || typeof ScrollTrigger === 'undefined') {
      console.warn('GSAP or ScrollTrigger not found, using native fallback.');
      setupNativeObserver();
      return;
    }

    gsap.registerPlugin(ScrollTrigger);

    const mm = gsap.matchMedia();

    // Desktop: Pin phone and trigger cross-fades
    mm.add('(min-width: 992px)', () => {
      const steps = document.querySelectorAll('.story-step');

      steps.forEach(step => {
        const screenName = step.getAttribute('data-screen');

        ScrollTrigger.create({
          trigger: step,
          start: 'top 55%',
          end: 'bottom 45%',
          onEnter: () => switchPhoneScreen(screenName),
          onEnterBack: () => switchPhoneScreen(screenName),
        });
      });

      return () => {
        // Cleanup if viewport changes
      };
    });

    // Mobile / Tablet (< 992px): No pinning! Use native observer
    mm.add('(max-width: 991px)', () => {
      setupNativeObserver();
    });
  }

  // Initialize once DOM is ready
  document.addEventListener('DOMContentLoaded', () => {
    setupMobileTabs();
    initGSAPScrollytelling();
  });
})();
