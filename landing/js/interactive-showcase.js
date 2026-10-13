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

  // 4. GSAP Animations for Zig-zag Rows and Popouts
  function initGSAPAnimations() {
    if (typeof gsap === 'undefined' || typeof ScrollTrigger === 'undefined') {
      return;
    }

    gsap.registerPlugin(ScrollTrigger);

    const isReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (isReducedMotion) return;

    // Popout items explosion from studio box
    gsap.from('.popout-item', {
      scale: 0,
      opacity: 0,
      duration: 0.8,
      stagger: 0.12,
      ease: 'back.out(1.7)',
      scrollTrigger: {
        trigger: '.magic-box-showcase',
        start: 'top 70%'
      }
    });

    // Zig-zag rows animation
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

    // Subjects cards stagger
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

  document.addEventListener('DOMContentLoaded', () => {
    setupStudioBox();
    setupTakeNotesAction();
    setupEditorSimulator();
    initGSAPAnimations();
  });
})();
