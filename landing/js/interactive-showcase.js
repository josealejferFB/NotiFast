/**
 * NotiFast 3.0 — Interactive Showcase Script
 * Duolingo playful vector dynamics, unboxing animations, grade gauge counter, and interactive subjects.
 */

(function () {
  'use strict';

  // 1. Unboxing Animation (Magic Study Chest)
  function setupMagicChest() {
    const chestContainer = document.querySelector('.study-chest-container');
    if (!chestContainer) return;

    // Open on click for instant delight
    chestContainer.addEventListener('click', () => {
      chestContainer.classList.toggle('chest-open');
    });

    // Auto-open as user scrolls into view
    if ('IntersectionObserver' in window) {
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            chestContainer.classList.add('chest-open');
          }
        });
      }, { threshold: 0.3 });

      observer.observe(chestContainer);
    } else {
      chestContainer.classList.add('chest-open');
    }
  }

  // 2. Animated Grade Gauge & Number Counter
  function setupGradeGauge() {
    const gaugeCircle = document.querySelector('.gauge-fill-circle');
    const gaugeNumber = document.querySelector('.gauge-number');
    if (!gaugeCircle || !gaugeNumber) return;

    let animated = false;

    function runAnimation() {
      if (animated) return;
      animated = true;

      // Circle fill animation (565 is circumference, 85 is ~8.7/10)
      gaugeCircle.style.strokeDashoffset = '85';

      // Number count-up
      let current = 0.0;
      const target = 8.7;
      const step = 0.1;
      const interval = setInterval(() => {
        current += step;
        if (current >= target) {
          current = target;
          clearInterval(interval);
        }
        gaugeNumber.textContent = current.toFixed(1);
      }, 25);
    }

    if ('IntersectionObserver' in window) {
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            runAnimation();
          }
        });
      }, { threshold: 0.4 });

      const gaugeContainer = document.querySelector('.grade-gauge-container');
      if (gaugeContainer) observer.observe(gaugeContainer);
    } else {
      runAnimation();
    }
  }

  // 3. GSAP ScrollTrigger Integration for Zig-Zag Rows & Floating Popouts
  function initGSAPShowcase() {
    if (typeof gsap === 'undefined' || typeof ScrollTrigger === 'undefined') {
      return;
    }

    gsap.registerPlugin(ScrollTrigger);

    const isReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (isReducedMotion) return;

    // Popout items explosion from chest
    gsap.from('.popout-item', {
      scale: 0,
      opacity: 0,
      duration: 0.8,
      stagger: 0.15,
      ease: 'back.out(1.7)',
      scrollTrigger: {
        trigger: '.magic-box-showcase',
        start: 'top 70%'
      }
    });

    // Alternating Zig-zag rows: Slide in with spring physics
    const rows = document.querySelectorAll('.dynamic-feature-row');
    rows.forEach((row, i) => {
      const visual = row.querySelector('.feature-visual');
      const info = row.querySelector('.feature-info');
      const isReverse = row.classList.contains('row-reverse');

      // Slide visual from left or right
      if (visual) {
        gsap.from(visual, {
          x: isReverse ? -60 : 60,
          opacity: 0,
          duration: 0.9,
          ease: 'power2.out',
          scrollTrigger: {
            trigger: row,
            start: 'top 75%'
          }
        });
      }

      if (info) {
        gsap.from(info, {
          x: isReverse ? 40 : -40,
          opacity: 0,
          duration: 0.9,
          ease: 'power2.out',
          scrollTrigger: {
            trigger: row,
            start: 'top 75%'
          }
        });
      }
    });

    // Subjects cards stagger
    gsap.from('.subject-feature-card', {
      y: 40,
      opacity: 0,
      duration: 0.7,
      stagger: 0.12,
      ease: 'back.out(1.4)',
      scrollTrigger: {
        trigger: '.subjects-gallery-section',
        start: 'top 80%'
      }
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    setupMagicChest();
    setupGradeGauge();
    initGSAPShowcase();
  });
})();
