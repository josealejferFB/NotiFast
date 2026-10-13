/**
 * NotiFast 3.0 — Interactive Showcase Script
 * Handles:
 * - Studio Box opening / unboxing animation
 * - Live Class beacon interactions & "Tomar apuntes" quick action jump
 * - Markdown editor preview toggle and interactive task checklist
 * - GSAP ScrollTrigger animations
 */

(function () {
  'use strict';

  // 1. Studio Box Unboxing
  function setupStudioBox() {
    const chestContainer = document.querySelector('.study-chest-container');
    if (!chestContainer) return;

    // Toggle on click
    chestContainer.addEventListener('click', () => {
      chestContainer.classList.toggle('chest-open');
    });

    // Auto-open on scroll into view
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

  // 2. "Tomar Apuntes" Quick Action from Schedule Card
  function setupTakeNotesAction() {
    const takeNotesBtn = document.getElementById('btn-take-notes-demo');
    const editorSection = document.getElementById('apuntes');
    const editorSimulator = document.querySelector('.note-editor-simulator');

    if (!takeNotesBtn || !editorSection) return;

    takeNotesBtn.addEventListener('click', (e) => {
      e.preventDefault();
      editorSection.scrollIntoView({ behavior: 'smooth', block: 'center' });

      if (editorSimulator) {
        editorSimulator.style.transition = 'box-shadow 0.4s, transform 0.4s';
        editorSimulator.style.boxShadow = '0 0 0 4px rgba(158, 27, 50, 0.4), 0 20px 40px rgba(0,0,0,0.12)';
        editorSimulator.style.transform = 'scale(1.02)';

        setTimeout(() => {
          editorSimulator.style.boxShadow = '';
          editorSimulator.style.transform = '';
        }, 1500);
      }
    });
  }

  // 3. Interactive Markdown Editor Simulator
  function setupEditorSimulator() {
    const toggleViewBtn = document.getElementById('btn-toggle-editor-view');
    const editorSheet = document.querySelector('.rich-editor-sheet');
    const toolbarButtons = document.querySelectorAll('.markdown-toolbar .tb-btn');

    if (toggleViewBtn && editorSheet) {
      let isPreview = true;
      toggleViewBtn.addEventListener('click', () => {
        isPreview = !isPreview;
        if (isPreview) {
          toggleViewBtn.textContent = 'Vista previa';
          toggleViewBtn.classList.add('active');
          editorSheet.innerHTML = `
            <div class="rendered-markdown-content">
              <h5># Teorema de Lagrange</h5>
              <p>Sea una función continua en [a, b] y diferenciable en (a, b). Existe un punto <em>c</em> tal que:</p>
              <div class="math-latex-card">
                <code>f'(c) = [f(b) - f(a)] / (b - a)</code>
              </div>
              <div class="interactive-todo-list">
                <label class="todo-item checked">
                  <input type="checkbox" checked disabled>
                  <span>Demostración con el Teorema de Rolle</span>
                </label>
                <label class="todo-item checked">
                  <input type="checkbox" checked disabled>
                  <span>Ejercicios resueltos de optimización de costos</span>
                </label>
                <label class="todo-item">
                  <input type="checkbox" disabled>
                  <span>Revisar guía para el examen del viernes</span>
                </label>
              </div>
              <blockquote class="editor-quote-card">
                "La recta secante es paralela a la recta tangente en el punto c."
              </blockquote>
            </div>
          `;
        } else {
          toggleViewBtn.textContent = 'Modo edición';
          toggleViewBtn.classList.remove('active');
          editorSheet.innerHTML = `
            <div style="font-family: var(--font-mono); font-size: 0.8125rem; line-height: 1.6; color: var(--text-primary);">
              <p style="color: var(--brand-primary); font-weight: 700;"># Teorema de Lagrange</p>
              <p>Sea una función continua en [a, b] y diferenciable en (a, b). Existe un punto *c* tal que:</p>
              <p style="color: #7C3AED; background: #F5F3FF; padding: 4px 8px; border-radius: 4px;">$$ f'(c) = \\frac{f(b) - f(a)}{b - a} $$</p>
              <p>- [x] Demostración con el Teorema de Rolle<br>- [x] Ejercicios resueltos de optimización de costos<br>- [ ] Revisar guía para el examen del viernes</p>
              <p style="color: #881337;">> "La recta secante es paralela a la recta tangente en el punto c."</p>
            </div>
          `;
        }
      });
    }

    // Microfeedback on toolbar button clicks
    toolbarButtons.forEach(btn => {
      btn.addEventListener('click', () => {
        btn.style.transform = 'scale(0.88)';
        setTimeout(() => { btn.style.transform = ''; }, 150);
      });
    });
  }

  // 4. Hub 3D Magnetic Parallax, Beam Highlights & Click-to-Fly Spotlight
  function setupHubInteractions() {
    const stage = document.getElementById('hub-interactive-stage');
    const brandCore = document.querySelector('.notifast-floating-brand');
    const popoutItems = document.querySelectorAll('.popout-item');
    const aura = document.querySelector('.notifast-icon-aura');

    if (!stage) return;

    const isReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // A. 3D Magnetic Parallax on Mousemove
    if (!isReducedMotion) {
      stage.addEventListener('mousemove', (e) => {
        const rect = stage.getBoundingClientRect();
        const nx = (e.clientX - rect.left - rect.width / 2) / (rect.width / 2);
        const ny = (e.clientY - rect.top - rect.height / 2) / (rect.height / 2);

        if (typeof gsap !== 'undefined') {
          gsap.to(stage, {
            rotateX: -ny * 6,
            rotateY: nx * 6,
            duration: 0.4,
            ease: 'power1.out'
          });

          if (brandCore) {
            gsap.to(brandCore, {
              x: nx * 10,
              y: ny * 10,
              duration: 0.4,
              ease: 'power1.out'
            });
          }

          popoutItems.forEach((item, idx) => {
            const factor = (idx % 2 === 0) ? 1.3 : -1.1;
            gsap.to(item, {
              x: nx * 8 * factor,
              y: ny * 8 * factor,
              duration: 0.45,
              ease: 'power1.out'
            });
          });
        }
      });

      stage.addEventListener('mouseleave', () => {
        if (typeof gsap !== 'undefined') {
          gsap.to(stage, { rotateX: 0, rotateY: 0, duration: 0.7, ease: 'power2.out' });
          if (brandCore) gsap.to(brandCore, { x: 0, y: 0, duration: 0.7, ease: 'power2.out' });
          popoutItems.forEach(item => {
            gsap.to(item, { x: 0, y: 0, duration: 0.7, ease: 'power2.out' });
          });
        }
      });
    }

    // B. Beam Highlight & Aura Reaction on Popout Hover
    const BEAM_MAP = {
      'popout-notes': '#beam-notes',
      'popout-grades': '#beam-grades',
      'popout-agenda': '#beam-agenda',
      'popout-nearby': '#beam-nearby',
      'popout-subjects': '#beam-subjects',
      'popout-alerts': '#beam-alerts'
    };

    popoutItems.forEach(item => {
      // Find matching beam
      let targetBeamId = null;
      for (const [cls, beamId] of Object.entries(BEAM_MAP)) {
        if (item.classList.contains(cls)) {
          targetBeamId = beamId;
          break;
        }
      }

      const beamEl = targetBeamId ? document.querySelector(targetBeamId) : null;

      item.addEventListener('mouseenter', () => {
        if (beamEl) beamEl.style.stroke = 'rgba(158, 27, 50, 0.35)';
        if (aura) {
          aura.style.transform = 'translate(-50%, -50%) scale(1.15)';
          aura.style.opacity = '0.9';
        }
      });

      item.addEventListener('mouseleave', () => {
        if (beamEl) beamEl.style.stroke = '';
        if (aura) {
          aura.style.transform = '';
          aura.style.opacity = '';
        }
      });

      // C. Click-to-Fly Spotlight Navigation
      function triggerNavigation(e) {
        e.preventDefault();
        const targetSelector = item.getAttribute('data-target');
        if (!targetSelector) return;

        const targetSection = document.querySelector(targetSelector);
        if (!targetSection) return;

        targetSection.scrollIntoView({ behavior: 'smooth', block: 'center' });

        // Highlight main visual feature card
        const spotlightTarget = targetSection.querySelector(
          '.schedule-live-card, .note-editor-simulator, #main-average-hero-card, .nearby-transfer-card, .subjects-grid'
        );

        if (spotlightTarget) {
          spotlightTarget.classList.remove('section-spotlight-active');
          void spotlightTarget.offsetWidth; // Force reflow
          spotlightTarget.classList.add('section-spotlight-active');

          setTimeout(() => {
            spotlightTarget.classList.remove('section-spotlight-active');
          }, 1800);
        }
      }

      item.addEventListener('click', triggerNavigation);
      item.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          triggerNavigation(e);
        }
      });
    });
  }

  // 5. Persistent Scrollytelling Radar Dock
  function setupFloatingRadarDock() {
    const radar = document.getElementById('hub-floating-radar');
    const chips = document.querySelectorAll('.radar-chip');
    if (!radar) return;

    // Show/hide radar dock on scroll
    function handleScroll() {
      const scrollY = window.scrollY || window.pageYOffset;
      const docHeight = document.documentElement.scrollHeight;
      const winHeight = window.innerHeight;

      // Show after scrolling past hub stage (approx 550px) and hide near footer
      const isPastHub = scrollY > 520;
      const isNearFooter = (scrollY + winHeight) > (docHeight - 400);

      if (isPastHub && !isNearFooter) {
        radar.classList.add('visible');
      } else {
        radar.classList.remove('visible');
      }
    }

    window.addEventListener('scroll', handleScroll, { passive: true });
    handleScroll();

    // Smooth click handler for radar chips
    chips.forEach(chip => {
      chip.addEventListener('click', (e) => {
        e.preventDefault();
        const targetSelector = chip.getAttribute('data-target');
        const targetSection = targetSelector ? document.querySelector(targetSelector) : null;
        if (!targetSection) return;

        targetSection.scrollIntoView({ behavior: 'smooth', block: 'center' });

        // Highlight target feature card
        const spotlightTarget = targetSection.querySelector(
          '.schedule-live-card, .note-editor-simulator, #main-average-hero-card, .nearby-transfer-card, .subjects-grid'
        );
        if (spotlightTarget) {
          spotlightTarget.classList.remove('section-spotlight-active');
          void spotlightTarget.offsetWidth;
          spotlightTarget.classList.add('section-spotlight-active');
          setTimeout(() => { spotlightTarget.classList.remove('section-spotlight-active'); }, 1800);
        }
      });
    });

    // Active Section Spy using IntersectionObserver
    const sectionIds = ['#agenda', '#apuntes', '#calificaciones', '#compartir', '#materias'];
    if ('IntersectionObserver' in window) {
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const id = '#' + entry.target.id;
            chips.forEach(chip => {
              const matches = chip.getAttribute('data-target') === id;
              chip.classList.toggle('active', matches);
            });
          }
        });
      }, { threshold: 0.35 });

      sectionIds.forEach(id => {
        const sec = document.querySelector(id);
        if (sec) observer.observe(sec);
      });
    }
  }

  // 6. GSAP Scrollytelling Choreography
  function initGSAPAnimations() {
    if (typeof gsap === 'undefined' || typeof ScrollTrigger === 'undefined') {
      return;
    }

    gsap.registerPlugin(ScrollTrigger);

    const isReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (isReducedMotion) return;

    // A. Initial entry explosion of popout items
    gsap.from('.popout-item', {
      scale: 0,
      opacity: 0,
      duration: 0.8,
      stagger: 0.1,
      ease: 'back.out(1.7)',
      scrollTrigger: {
        trigger: '.magic-box-showcase',
        start: 'top 75%'
      }
    });

    // B. Scrollytelling Choreography: As user scrolls down, items disperse outward & down towards features
    ScrollTrigger.create({
      trigger: '.notifast-hub-showcase',
      start: 'top 20%',
      end: 'bottom top',
      scrub: 1.2,
      onUpdate: (self) => {
        const p = self.progress;
        // Subtle flight trajectories: modules migrate towards destination sections
        gsap.to('.popout-notes', { x: -45 * p, y: 70 * p, opacity: 1 - 0.6 * p, scale: 1 - 0.15 * p, overwrite: 'auto' });
        gsap.to('.popout-grades', { x: 45 * p, y: 70 * p, opacity: 1 - 0.6 * p, scale: 1 - 0.15 * p, overwrite: 'auto' });
        gsap.to('.popout-agenda', { x: -55 * p, y: 110 * p, opacity: 1 - 0.6 * p, scale: 1 - 0.15 * p, overwrite: 'auto' });
        gsap.to('.popout-nearby', { x: 55 * p, y: 110 * p, opacity: 1 - 0.6 * p, scale: 1 - 0.15 * p, overwrite: 'auto' });
        gsap.to('.popout-subjects', { y: 50 * p, opacity: 1 - 0.7 * p, scale: 1 - 0.15 * p, overwrite: 'auto' });
        gsap.to('.popout-alerts', { y: 120 * p, opacity: 1 - 0.7 * p, scale: 1 - 0.15 * p, overwrite: 'auto' });
        gsap.to('.notifast-floating-brand', { scale: 1 - 0.12 * p, opacity: 1 - 0.25 * p, overwrite: 'auto' });
      }
    });

    // C. Zig-zag rows animation with section landing triggers
    const rows = document.querySelectorAll('.dynamic-feature-row');
    rows.forEach((row) => {
      const visual = row.querySelector('.feature-visual');
      const info = row.querySelector('.feature-info');
      const isReverse = row.classList.contains('row-reverse');

      if (visual) {
        gsap.from(visual, {
          x: isReverse ? -50 : 50,
          opacity: 0,
          duration: 0.85,
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
          duration: 0.85,
          ease: 'power2.out',
          scrollTrigger: {
            trigger: row,
            start: 'top 75%'
          }
        });
      }
    });

    // D. Animated Live Status progress bar in Agenda
    ScrollTrigger.create({
      trigger: '#agenda',
      start: 'top 70%',
      once: true,
      onEnter: () => {
        const prog = document.querySelector('.schedule-progress-bar .progress-fill');
        if (prog) {
          prog.style.width = '0%';
          setTimeout(() => {
            prog.style.transition = 'width 1.2s cubic-bezier(0.34, 1.56, 0.64, 1)';
            prog.style.width = '62.5%';
          }, 100);
        }
      }
    });

    // E. Subjects cards stagger
    gsap.from('.subject-feature-card', {
      y: 35,
      opacity: 0,
      duration: 0.65,
      stagger: 0.1,
      ease: 'back.out(1.4)',
      scrollTrigger: {
        trigger: '.subjects-gallery-section',
        start: 'top 80%'
      }
    });
  }

  // 4. Adaptable Grading Scale Switcher (Decimal 0-10, Vigesimal 0-20, Porcentual 0-100%, Letras A-F)
  const GRADE_SCALES = [
    {
      id: '0-10',
      label: 'PROMEDIO GLOBAL • ESCALA DECIMAL (0 - 10)',
      score: '8.70',
      maxText: 'Escala 0.0 - 10.0 pts',
      progress: 87,
      status: '¡Excelente rendimiento!',
      scores: { calculus: '8.8', physics: '8.5', literature: '9.1' }
    },
    {
      id: '0-20',
      label: 'PROMEDIO GLOBAL • ESCALA VIGESIMAL (0 - 20)',
      score: '17.4',
      maxText: 'Escala 0.0 - 20.0 pts',
      progress: 87,
      status: '¡Aprobado con Distinción!',
      scores: { calculus: '17.6', physics: '17.0', literature: '18.2' }
    },
    {
      id: '0-100',
      label: 'PROMEDIO GLOBAL • ESCALA PORCENTUAL (100%)',
      score: '87%',
      maxText: 'Escala 0% - 100%',
      progress: 87,
      status: '¡Desempeño Sobresaliente!',
      scores: { calculus: '88%', physics: '85%', literature: '91%' }
    },
    {
      id: 'A-F',
      label: 'PROMEDIO GLOBAL • ESCALA ALFABÉTICA (A - F)',
      score: 'A',
      maxText: 'Escala GPA 3.7 / 4.0',
      progress: 87,
      status: 'Honors / Dean\'s List',
      scores: { calculus: 'A', physics: 'B+', literature: 'A' }
    }
  ];

  function setupGradeScaleSwitcher() {
    const chips = document.querySelectorAll('.scale-chip');
    const heroCard = document.getElementById('main-average-hero-card');
    const titleEl = document.getElementById('average-scale-title');
    const scoreEl = document.getElementById('average-score-display');
    const maxEl = document.getElementById('average-scale-max');
    const progressEl = document.getElementById('grades-progress-fill');
    const statusEl = document.getElementById('average-status-text');
    const calculusScoreEl = document.getElementById('score-calculus');
    const physicsScoreEl = document.getElementById('score-physics');
    const literatureScoreEl = document.getElementById('score-literature');

    if (!chips.length || !scoreEl) return;

    let currentIndex = 0;
    let autoInterval = null;
    let isUserPaused = false;

    function applyScale(scaleId, triggerAnimation = true) {
      const scaleObj = GRADE_SCALES.find(s => s.id === scaleId) || GRADE_SCALES[0];
      currentIndex = GRADE_SCALES.findIndex(s => s.id === scaleId);

      // Update active chip
      chips.forEach(chip => {
        const isActive = chip.getAttribute('data-scale') === scaleId;
        chip.classList.toggle('active', isActive);
        chip.setAttribute('aria-selected', isActive ? 'true' : 'false');
      });

      // Update values
      if (titleEl) titleEl.textContent = scaleObj.label;
      if (scoreEl) {
        scoreEl.textContent = scaleObj.score;
        if (triggerAnimation) {
          scoreEl.classList.remove('scale-value-morph');
          void scoreEl.offsetWidth; // Trigger reflow
          scoreEl.classList.add('scale-value-morph');
        }
      }
      if (maxEl) maxEl.textContent = scaleObj.maxText;
      if (progressEl) progressEl.style.width = `${scaleObj.progress}%`;
      if (statusEl) statusEl.textContent = scaleObj.status;

      if (calculusScoreEl) {
        calculusScoreEl.textContent = scaleObj.scores.calculus;
        if (triggerAnimation) {
          calculusScoreEl.classList.remove('scale-value-morph');
          void calculusScoreEl.offsetWidth;
          calculusScoreEl.classList.add('scale-value-morph');
        }
      }
      if (physicsScoreEl) physicsScoreEl.textContent = scaleObj.scores.physics;
      if (literatureScoreEl) literatureScoreEl.textContent = scaleObj.scores.literature;
    }

    // Auto-cycle through scales
    function startAutoCycle() {
      if (autoInterval) clearInterval(autoInterval);
      autoInterval = setInterval(() => {
        if (!isUserPaused) {
          currentIndex = (currentIndex + 1) % GRADE_SCALES.length;
          applyScale(GRADE_SCALES[currentIndex].id);
        }
      }, 3500);
    }

    // Manual click on scale chip
    chips.forEach(chip => {
      chip.addEventListener('click', () => {
        const scaleId = chip.getAttribute('data-scale');
        isUserPaused = true;
        applyScale(scaleId);

        // Resume auto-cycle after 12 seconds of inactivity
        setTimeout(() => {
          isUserPaused = false;
        }, 12000);
      });
    });

    // Pause on hover over hero card
    if (heroCard) {
      heroCard.addEventListener('mouseenter', () => { isUserPaused = true; });
      heroCard.addEventListener('mouseleave', () => { isUserPaused = false; });
    }

    // Start auto-cycle
    startAutoCycle();
  }

  document.addEventListener('DOMContentLoaded', () => {
    setupStudioBox();
    setupHubInteractions();
    setupFloatingRadarDock();
    setupTakeNotesAction();
    setupEditorSimulator();
    setupGradeScaleSwitcher();
    initGSAPAnimations();
  });
})();
