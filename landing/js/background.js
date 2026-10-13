/**
 * NotiFast 3.0 — Dynamic Background Particle System
 * Strictly mirrors NotiFastStudyBackground.kt icons:
 * MenuBook, Edit, School, HistoryEdu, Science, AutoStories, Lightbulb,
 * Calculate, Functions, Psychology, Biotech, EmojiEvents.
 * 
 * Respects prefers-reduced-motion for complete accessibility.
 */

(function () {
  'use strict';

  const container = document.getElementById('bg-particles-container');
  if (!container) return;

  const isReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  const ICONS = [
    'icon-menubook',
    'icon-school',
    'icon-science',
    'icon-calculate',
    'icon-lightbulb',
    'icon-functions',
    'icon-emojievents',
    'icon-edit',
    'icon-historyedu',
    'icon-autostories',
    'icon-psychology',
    'icon-biotech'
  ];

  const COLOR_CLASSES = [
    'sticker-ruby',
    'sticker-aqua',
    'sticker-sage',
    'sticker-amber',
    'sticker-blue'
  ];

  const PARTICLE_COUNT = 16;
  const particles = [];

  // Generate particles distributed across the page height
  for (let i = 0; i < PARTICLE_COUNT; i++) {
    const el = document.createElement('div');
    el.className = `study-sticker ${COLOR_CLASSES[i % COLOR_CLASSES.length]}`;

    // Randomize initial positions (spread throughout viewport and below fold)
    const leftPercent = 5 + (i * 21) % 88; // 5% to 93%
    const topPercent = (i * 7.5) % 110;     // distributed along page
    const size = 38 + ((i * 7) % 24);      // 38px to 62px
    const iconId = ICONS[i % ICONS.length];

    el.style.left = `${leftPercent}%`;
    el.style.top = `${topPercent}%`;
    el.style.width = `${size}px`;
    el.style.height = `${size}px`;

    // SVG icon reference from inline sprite
    el.innerHTML = `
      <svg class="sticker-svg" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <use href="#${iconId}"></use>
      </svg>
    `;

    container.appendChild(el);

    particles.push({
      el,
      baseTop: topPercent,
      speed: 0.08 + (i % 5) * 0.04,
      amplitude: 12 + (i % 4) * 6,
      phase: (i * 1.3),
      rotationSpeed: (i % 2 === 0 ? 1 : -1) * (0.015 + (i % 3) * 0.008)
    });
  }

  // Animation Loop (only if reduced motion is false)
  if (!isReducedMotion) {
    let lastScrollY = window.scrollY;
    let targetScrollY = window.scrollY;
    let currentScrollY = window.scrollY;
    let time = 0;

    window.addEventListener('scroll', () => {
      targetScrollY = window.scrollY;
    }, { passive: true });

    function tick() {
      time += 0.02;
      currentScrollY += (targetScrollY - currentScrollY) * 0.1;

      for (let i = 0; i < particles.length; i++) {
        const p = particles[i];
        // Vertical float wave
        const floatY = Math.sin(time + p.phase) * p.amplitude;
        // Parallax shift from scroll
        const scrollShift = currentScrollY * p.speed * -0.3;
        // Gentle rotation
        const rot = Math.sin(time * 0.5 + p.phase) * 12;

        p.el.style.transform = `translate3d(0, ${floatY + scrollShift}px, 0) rotate(${rot}deg)`;
      }

      requestAnimationFrame(tick);
    }

    requestAnimationFrame(tick);
  }
})();
