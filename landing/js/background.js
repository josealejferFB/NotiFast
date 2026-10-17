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

  const PARTICLE_COUNT = 8;
  const particles = [];

  // Generate particles distributed across the page height
  for (let i = 0; i < PARTICLE_COUNT; i++) {
    const el = document.createElement('div');
    el.className = `study-sticker ${COLOR_CLASSES[i % COLOR_CLASSES.length]}`;

    // Randomize initial positions within fixed viewport
    const leftPercent = 8 + (i * 24) % 84; // 8% to 92%
    const topPercent = 10 + (i * 22) % 78; // 10% to 88%
    const size = 32 + ((i * 6) % 18);      // 32px to 50px
    const iconId = ICONS[i % ICONS.length];

    el.style.position = 'absolute';
    el.style.left = `${leftPercent}%`;
    el.style.top = `${topPercent}%`;
    el.style.width = `${size}px`;
    el.style.height = `${size}px`;
    el.style.opacity = '0.035';
    el.style.pointerEvents = 'none';

    // SVG icon reference from inline sprite with defensive inline dimensions
    el.innerHTML = `
      <svg class="sticker-svg" width="${size}" height="${size}" style="width: 100%; height: 100%;" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <use href="#${iconId}"></use>
      </svg>
    `;

    container.appendChild(el);

    particles.push({
      el,
      baseTop: topPercent,
      speed: 0.04 + (i % 4) * 0.02,
      amplitude: 6 + (i % 3) * 3,
      phase: (i * 1.3),
      rotationSpeed: (i % 2 === 0 ? 1 : -1) * (0.008 + (i % 3) * 0.004)
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
