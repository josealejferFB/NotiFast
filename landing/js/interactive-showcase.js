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
              <h4># Teorema de Lagrange</h4>
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
              <p style="color: #0F766E; background: #F0FDFA; padding: 4px 8px; border-radius: 4px;">$$ f'(c) = \\frac{f(b) - f(a)}{b - a} $$</p>
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
          '.schedule-live-card, .note-editor-simulator, #main-average-hero-card, .evaluations-calendar-container, .analytics-dashboard-container, .theme-studio-workbench, .notifications-simulator-card, .nearby-transfer-card, .subjects-grid'
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
    const sectionIds = ['#agenda', '#apuntes', '#calificaciones', '#evaluaciones', '#analiticas', '#temas', '#notificaciones', '#compartir', '#materias'];
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

    // A. Initial entry of popout items (if hub section exists)
    if (document.querySelector('.magic-box-showcase')) {
      gsap.from('.popout-item', {
        scale: 0.92,
        opacity: 0,
        duration: 0.65,
        stagger: 0.08,
        ease: 'power2.out',
        scrollTrigger: {
          trigger: '.magic-box-showcase',
          start: 'top 80%'
        }
      });

      // B. Scrollytelling Choreography (Desktop orbital layout > 860px only)
      if (window.innerWidth > 860) {
        ScrollTrigger.create({
          trigger: '.notifast-hub-showcase',
          start: 'top 20%',
          end: 'bottom top',
          scrub: 1.2,
          onUpdate: (self) => {
            if (window.innerWidth <= 860) return;
            const p = self.progress;
            gsap.to('.popout-notes', { x: -45 * p, y: 70 * p, opacity: 1 - 0.6 * p, scale: 1 - 0.15 * p, overwrite: 'auto' });
            gsap.to('.popout-grades', { x: 45 * p, y: 70 * p, opacity: 1 - 0.6 * p, scale: 1 - 0.15 * p, overwrite: 'auto' });
            gsap.to('.popout-agenda', { x: -55 * p, y: 110 * p, opacity: 1 - 0.6 * p, scale: 1 - 0.15 * p, overwrite: 'auto' });
            gsap.to('.popout-nearby', { x: 55 * p, y: 110 * p, opacity: 1 - 0.6 * p, scale: 1 - 0.15 * p, overwrite: 'auto' });
            gsap.to('.popout-subjects', { xPercent: -50, y: 50 * p, opacity: 1 - 0.7 * p, scale: 1 - 0.15 * p, overwrite: 'auto' });
            gsap.to('.popout-alerts', { xPercent: -50, y: 120 * p, opacity: 1 - 0.7 * p, scale: 1 - 0.15 * p, overwrite: 'auto' });
            gsap.to('.notifast-floating-brand', { scale: 1 - 0.12 * p, opacity: 1 - 0.25 * p, overwrite: 'auto' });
          }
        });
      }
    }

    // C. Zig-zag rows animation with section landing triggers (vertical-only on mobile)
    const rows = document.querySelectorAll('.dynamic-feature-row');
    const isDesktop = window.innerWidth > 991;
    rows.forEach((row) => {
      const visual = row.querySelector('.feature-visual');
      const info = row.querySelector('.feature-info');
      const isReverse = row.classList.contains('row-reverse');

      if (visual) {
        gsap.from(visual, {
          x: isDesktop ? (isReverse ? -36 : 36) : 0,
          y: isDesktop ? 0 : 24,
          opacity: 0,
          duration: 0.75,
          ease: 'power2.out',
          scrollTrigger: {
            trigger: row,
            start: 'top 80%'
          }
        });
      }

      if (info) {
        gsap.from(info, {
          x: isDesktop ? (isReverse ? 28 : -28) : 0,
          y: isDesktop ? 0 : 20,
          opacity: 0,
          duration: 0.75,
          ease: 'power2.out',
          scrollTrigger: {
            trigger: row,
            start: 'top 80%'
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
            prog.style.transition = 'width 1.2s cubic-bezier(0.16, 1, 0.3, 1)';
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

  // 7. Interactive Evaluations Calendar (AgendaMonthCalendarView.kt & AgendaEvaluationCard.kt)
  const EVALUATIONS_DATA = [
    {
      id: 'eval-1',
      day: 8,
      dateStr: '8 de Octubre',
      title: 'Quiz I: Equilibrios Ácido-Base',
      type: 'EXAM',
      icon: '#icon-quiz',
      subject: 'Química Orgánica',
      subjectColor: '#047857',
      weight: 15,
      isPresented: true,
      grade: '9.8 / 10',
      timeText: 'Jueves 11:00 AM • Lab 102',
      statusText: '✓ Calificada • 9.8 pts',
      isGraded: true,
      tintBg: '#ECFDF5',
      iconTint: '#047857',
      dotClass: 'dot-aqua-eval'
    },
    {
      id: 'eval-2',
      day: 15,
      dateStr: '15 de Octubre',
      title: 'Parcial II: Derivadas y Optimización',
      type: 'EXAM',
      icon: '#icon-quiz',
      subject: 'Cálculo Diferencial',
      subjectColor: '#2563EB',
      weight: 35,
      isPresented: false,
      grade: null,
      timeText: 'Jueves 08:00 AM • Aula 204',
      statusText: 'En 2 días • Alta Ponderación',
      isGraded: false,
      tintBg: '#FFF1F2',
      iconTint: '#9E1B32',
      dotClass: 'dot-ruby-eval'
    },
    {
      id: 'eval-3',
      day: 22,
      dateStr: '22 de Octubre',
      title: 'Entrega de Proyecto: Péndulo Doble',
      type: 'PROJECT',
      icon: '#icon-assignment',
      subject: 'Física Mecánica',
      subjectColor: '#0F766E',
      weight: 30,
      isPresented: false,
      grade: null,
      timeText: 'Jueves 14:00 • Informe + Código',
      statusText: 'En 9 días • Trabajo Grupal',
      isGraded: false,
      tintBg: '#F0FDFA',
      iconTint: '#2A7B7B',
      dotClass: 'dot-ruby-eval'
    },
    {
      id: 'eval-4',
      day: 28,
      dateStr: '28 de Octubre',
      title: 'Exposición: Narrativa Contemporánea',
      type: 'PRESENTATION',
      icon: '#icon-school',
      subject: 'Literatura Universal',
      subjectColor: '#92400E',
      weight: 20,
      isPresented: false,
      grade: null,
      timeText: 'Miércoles 10:00 AM • Auditorio',
      statusText: 'En 15 días • Diapositivas',
      isGraded: false,
      tintBg: '#FFFBEB',
      iconTint: '#92400E',
      dotClass: 'dot-amber-eval'
    }
  ];

  function setupEvaluationsCalendar() {
    const calendarGrid = document.getElementById('calendar-days-grid');
    const panelTitle = document.getElementById('daily-evals-title');
    const panelCount = document.getElementById('daily-evals-count');
    const panelHint = document.getElementById('daily-status-hint');
    const cardsList = document.getElementById('eval-cards-list');
    const filterChips = document.querySelectorAll('.eval-filter-chip');
    const btnToday = document.getElementById('btn-cal-today');
    const btnPrev = document.getElementById('btn-cal-prev');
    const btnNext = document.getElementById('btn-cal-next');

    if (!calendarGrid || !cardsList) return;

    let selectedDay = 15; // Default selected day (Parcial II de Cálculo)
    let isMonthView = false;

    // October 2026 starts on Thursday (index 3 with Monday=0, so 3 empty padding cells)
    const paddingDaysCount = 3;
    const totalDaysInMonth = 31;

    // Render Calendar Grid
    function renderCalendarGrid() {
      calendarGrid.innerHTML = '';

      // Padding cells
      for (let p = 0; p < paddingDaysCount; p++) {
        const emptyCell = document.createElement('div');
        emptyCell.className = 'cal-day-cell cal-day-empty';
        emptyCell.setAttribute('aria-hidden', 'true');
        calendarGrid.appendChild(emptyCell);
      }

      // Day cells
      for (let d = 1; d <= totalDaysInMonth; d++) {
        const dayBtn = document.createElement('button');
        dayBtn.className = 'cal-day-cell';
        dayBtn.setAttribute('data-day', d);
        dayBtn.setAttribute('aria-label', `Día ${d} de Octubre`);

        if (d === selectedDay && !isMonthView) {
          dayBtn.classList.add('cal-day-selected');
        } else if (d === 15) {
          dayBtn.classList.add('cal-day-today');
        }

        const numSpan = document.createElement('span');
        numSpan.className = 'cal-day-num';
        numSpan.textContent = d;
        dayBtn.appendChild(numSpan);

        // Check if there are evaluations on this day
        const dayEvals = EVALUATIONS_DATA.filter(ev => ev.day === d);
        if (dayEvals.length > 0) {
          const dot = document.createElement('span');
          dot.className = `cal-day-dot ${dayEvals[0].dotClass}`;
          dayBtn.appendChild(dot);
          dayBtn.title = dayEvals.map(ev => `${ev.title} (${ev.weight}%)`).join(' • ');
        }

        dayBtn.addEventListener('click', () => {
          selectDay(d);
        });

        calendarGrid.appendChild(dayBtn);
      }
    }

    // Helper to generate an evaluation card HTML
    function buildEvalCardHtml(ev) {
      return `
        <div class="agenda-eval-card" data-eval-id="${ev.id}" role="button" tabindex="0">
          <div class="eval-icon-circle" style="background: ${ev.tintBg}; color: ${ev.iconTint};">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><use href="${ev.icon}"></use></svg>
          </div>
          <div class="eval-body">
            <div class="eval-top-row">
              <span class="eval-card-title">${ev.title}</span>
              <span class="eval-weight-chip">${ev.weight}%</span>
            </div>
            <div class="eval-bottom-row">
              <span class="eval-subject-row">
                <span class="eval-subject-dot" style="background: ${ev.subjectColor};"></span>
                <span>${ev.subject}</span>
              </span>
              <span class="eval-time-status ${ev.isGraded ? 'status-graded' : ''}">
                ${ev.statusText}
              </span>
            </div>
          </div>
        </div>
      `;
    }

    // Render cards list for a specific day or all
    function renderCards(dayNumber, showAll = false) {
      isMonthView = showAll;

      if (showAll) {
        if (panelTitle) panelTitle.textContent = 'Todas las Evaluaciones de Octubre';
        if (panelCount) panelCount.textContent = `(${EVALUATIONS_DATA.length})`;
        if (panelHint) panelHint.textContent = '4 pruebas distribuidas en el semestre';

        cardsList.innerHTML = EVALUATIONS_DATA.map(buildEvalCardHtml).join('');
      } else {
        const evals = EVALUATIONS_DATA.filter(ev => ev.day === dayNumber);

        if (panelTitle) panelTitle.textContent = `Evaluaciones del ${dayNumber} de Octubre`;
        if (panelCount) panelCount.textContent = `(${evals.length})`;

        if (evals.length > 0) {
          if (panelHint) panelHint.textContent = evals[0].timeText;
          cardsList.innerHTML = evals.map(buildEvalCardHtml).join('');
        } else {
          if (panelHint) panelHint.textContent = 'Sin exámenes ni entregas';
          cardsList.innerHTML = `
            <div class="eval-empty-card">
              <svg class="eval-empty-icon" width="36" height="36" viewBox="0 0 24 24" fill="currentColor"><use href="#icon-event"></use></svg>
              <span class="eval-empty-title">Sin evaluaciones este día</span>
              <span class="eval-empty-desc">Día libre de exámenes y entregas. ¡Buen momento para repasar apuntes!</span>
            </div>
          `;
        }
      }

      // Add tactile press microfeedback
      cardsList.querySelectorAll('.agenda-eval-card').forEach(card => {
        card.addEventListener('click', () => {
          card.style.transform = 'scale(0.97)';
          setTimeout(() => { card.style.transform = ''; }, 180);
        });
      });
    }

    // Select a specific day
    function selectDay(dayNumber) {
      selectedDay = dayNumber;
      isMonthView = false;

      // Update grid selection classes
      const allDayCells = calendarGrid.querySelectorAll('.cal-day-cell');
      allDayCells.forEach(cell => {
        const cellDay = parseInt(cell.getAttribute('data-day'), 10);
        cell.classList.toggle('cal-day-selected', cellDay === selectedDay);
      });

      // Update filter chips active state
      filterChips.forEach(chip => {
        const chipDay = parseInt(chip.getAttribute('data-day'), 10);
        chip.classList.toggle('active', chipDay === selectedDay);
        chip.setAttribute('aria-selected', chipDay === selectedDay ? 'true' : 'false');
      });

      // Render cards with smooth transition
      cardsList.style.opacity = '0';
      setTimeout(() => {
        renderCards(selectedDay, false);
        cardsList.style.transition = 'opacity 0.25s ease';
        cardsList.style.opacity = '1';
      }, 80);
    }

    // Filter Chips Handlers
    filterChips.forEach(chip => {
      chip.addEventListener('click', () => {
        filterChips.forEach(c => {
          c.classList.remove('active');
          c.setAttribute('aria-selected', 'false');
        });
        chip.classList.add('active');
        chip.setAttribute('aria-selected', 'true');

        const filterType = chip.getAttribute('data-filter');
        const dayVal = chip.getAttribute('data-day');

        if (filterType === 'all') {
          // Deselect day in grid, show all cards
          calendarGrid.querySelectorAll('.cal-day-cell').forEach(c => c.classList.remove('cal-day-selected'));
          renderCards(null, true);
        } else if (dayVal) {
          selectDay(parseInt(dayVal, 10));
        }
      });
    });

    // "Hoy" button handler
    if (btnToday) {
      btnToday.addEventListener('click', () => {
        selectDay(15);
      });
    }

    // Month Navigation mock triggers
    function monthNavFeedback(direction) {
      const title = document.getElementById('cal-month-title');
      if (title) {
        title.style.transition = 'transform 0.15s, opacity 0.15s';
        title.style.transform = `translateX(${direction * 15}px)`;
        title.style.opacity = '0.4';
        setTimeout(() => {
          title.style.transform = '';
          title.style.opacity = '';
        }, 200);
      }
    }

    if (btnPrev) btnPrev.addEventListener('click', () => monthNavFeedback(-1));
    if (btnNext) btnNext.addEventListener('click', () => monthNavFeedback(1));

    // Initial render
    renderCalendarGrid();
    renderCards(15, false);
  }

  // 8. Interactive Academic Analytics & Grade Simulator (AcademicAnalyticsContent.kt & GradeSimulatorCard.kt)
  function setupAnalyticsDashboard() {
    const slider = document.getElementById('sim-target-slider');
    const badge = document.getElementById('sim-slider-badge');
    const statRequired = document.getElementById('sim-stat-required');
    const statWeightSub = document.getElementById('sim-stat-weight-sub');
    const statusBanner = document.getElementById('sim-status-banner');
    const statusText = document.getElementById('sim-status-text');
    const statusIcon = document.getElementById('sim-status-icon');
    const presetChips = document.querySelectorAll('.sim-preset-chip');
    const diagFill = document.getElementById('diag-progress-fill');
    const histFills = document.querySelectorAll('.hist-bar-fill');

    if (!slider || !statRequired) return;

    // Simulation constants for Cálculo Diferencial (mirroring RequiredGradeResult)
    const ACCUMULATED = 5.70; // 65% evaluated with weighted score 5.70
    const REMAINING_WEIGHT = 0.35; // 35% remaining in Parcial II

    function calculateRequiredGrade(target) {
      // Formula: Required = (Target - Accumulated) / RemainingWeight
      return (target - ACCUMULATED) / REMAINING_WEIGHT;
    }

    function updateSimulation(target) {
      const required = calculateRequiredGrade(target);
      const formattedRequired = Math.max(0, required).toFixed(2);

      // Update slider and badge
      slider.value = target.toFixed(1);
      if (badge) badge.textContent = `${target.toFixed(1)} pts`;

      // Update required score display
      if (statRequired) {
        if (required <= 0) {
          statRequired.innerHTML = `0.00 <small>/ 10</small>`;
          statRequired.style.color = 'var(--brand-secondary)';
        } else if (required > 10.0) {
          statRequired.innerHTML = `${formattedRequired} <small>/ 10</small>`;
          statRequired.style.color = 'var(--brand-primary)';
        } else {
          statRequired.innerHTML = `${formattedRequired} <small>/ 10</small>`;
          statRequired.style.color = required >= 8.5 ? '#92400E' : 'var(--brand-secondary)';
        }
      }

      // Update status banner
      if (statusBanner && statusText && statusIcon) {
        statusBanner.className = 'sim-status-banner';

        if (required <= 0) {
          statusBanner.classList.add('status-achievable');
          statusText.textContent = '¡Materia ya aprobada! Todo lo demás sumará a tu promedio final';
        } else if (required <= 8.0) {
          statusBanner.classList.add('status-achievable');
          statusText.textContent = `Meta alcanzable • Necesitas sacar ${formattedRequired} en el 35% restante`;
        } else if (required <= 10.0) {
          statusBanner.classList.add('status-at-risk');
          statusText.textContent = `Atención • Requiere ${formattedRequired} pts (estudio intensivo recomendado)`;
        } else {
          statusBanner.classList.add('status-impossible');
          statusText.textContent = `Meta inalcanzable (${formattedRequired} supera la escala máxima de 10.0)`;
        }
      }

      // Sync preset chips
      presetChips.forEach(chip => {
        const chipVal = parseFloat(chip.getAttribute('data-target-grade'));
        const matches = Math.abs(chipVal - target) < 0.05;
        chip.classList.toggle('active', matches);
        chip.setAttribute('aria-selected', matches ? 'true' : 'false');
      });
    }

    // Slider input event
    slider.addEventListener('input', (e) => {
      const target = parseFloat(e.target.value);
      updateSimulation(target);
    });

    // Preset chips click
    presetChips.forEach(chip => {
      chip.addEventListener('click', () => {
        const target = parseFloat(chip.getAttribute('data-target-grade'));
        updateSimulation(target);
      });
    });

    // Animate diagnostic bar when in view
    if (typeof ScrollTrigger !== 'undefined' && diagFill) {
      ScrollTrigger.create({
        trigger: '#analiticas',
        start: 'top 75%',
        once: true,
        onEnter: () => {
          diagFill.style.width = '0%';
          setTimeout(() => {
            diagFill.style.width = '94%';
          }, 150);

          // Histogram bounce animation
          histFills.forEach((fill, idx) => {
            const h = fill.style.height;
            fill.style.height = '0%';
            setTimeout(() => {
              fill.style.height = h;
            }, 200 + idx * 80);
          });
        }
      });
    }

    // Initial calculation with 8.5
    updateSimulation(8.5);
  }

  // 9. Interactive Academic Hero Logo Motion & 3D Tactile Stage
  function setupHeroLogoMotion() {
    const stage = document.getElementById('hero-logo-stage');
    const shield = document.getElementById('emblem-shield');
    if (!stage || !shield) return;

    let isHovering = false;
    let targetX = 0;
    let targetY = 0;
    let currentX = 0;
    let currentY = 0;
    let rafId = null;

    function onMouseMove(e) {
      const rect = stage.getBoundingClientRect();
      const x = e.clientX - rect.left - rect.width / 2;
      const y = e.clientY - rect.top - rect.height / 2;
      // Damped 3D tilt: max 5 degrees for formal academic feel
      targetX = -(y / (rect.height / 2)) * 5;
      targetY = (x / (rect.width / 2)) * 5;
      isHovering = true;
      if (!rafId) {
        rafId = requestAnimationFrame(updateParallax);
      }
    }

    function onMouseLeave() {
      targetX = 0;
      targetY = 0;
      isHovering = false;
    }

    function updateParallax() {
      // Smooth exponential dampening
      currentX += (targetX - currentX) * 0.12;
      currentY += (targetY - currentY) * 0.12;

      shield.style.transform = 'perspective(1000px) rotateX(' + currentX.toFixed(2) + 'deg) rotateY(' + currentY.toFixed(2) + 'deg)';

      if (isHovering || Math.abs(targetX - currentX) > 0.04 || Math.abs(targetY - currentY) > 0.04) {
        rafId = requestAnimationFrame(updateParallax);
      } else {
        shield.style.transform = '';
        rafId = null;
      }
    }

    stage.addEventListener('mousemove', onMouseMove, { passive: true });
    stage.addEventListener('mouseleave', onMouseLeave, { passive: true });

    // Expose fallback helper so notification simulator triggers a graceful emblem pulse
    window.__notifastTriggerHeroToast = function() {
      const glow = shield.querySelector('.emblem-logo-glow');
      if (glow) {
        glow.style.transform = 'scale(1.35)';
        glow.style.opacity = '1';
        setTimeout(() => {
          glow.style.transform = '';
          glow.style.opacity = '';
        }, 800);
      }
    };
  }

  // 10. Live Theme Customizer Studio (Exact Presets from ThemePreset.kt & BackgroundPreset.kt)
  const ACCENT_PRESETS = {
    classic: {
      name: 'NotiFast Classic',
      light: { primary: '#9E1B32', secondary: '#2A7B7B', primaryLight: '#FFF1F2', secondaryLight: '#F0FDFA' },
      dark:  { primary: '#E05A6E', secondary: '#38B2AC', primaryLight: 'rgba(224,90,110,0.16)', secondaryLight: 'rgba(56,178,172,0.16)' }
    },
    forest_sage: {
      name: 'Campus Verde',
      light: { primary: '#15803D', secondary: '#854D0E', primaryLight: '#DCFCE7', secondaryLight: '#FEF9C3' },
      dark:  { primary: '#34D399', secondary: '#FBBF24', primaryLight: 'rgba(52,211,153,0.16)', secondaryLight: 'rgba(251,191,36,0.16)' }
    },
    midnight_indigo: {
      name: 'Cyber Nocturno',
      light: { primary: '#4338CA', secondary: '#0891B2', primaryLight: '#E0E7FF', secondaryLight: '#CFFAFE' },
      dark:  { primary: '#818CF8', secondary: '#22D3EE', primaryLight: 'rgba(129,140,248,0.16)', secondaryLight: 'rgba(34,211,238,0.16)' }
    },
    warm_mocha: {
      name: 'Café Nórdico',
      light: { primary: '#C2410C', secondary: '#B45309', primaryLight: '#FFEDD5', secondaryLight: '#FEF3C7' },
      dark:  { primary: '#FB923C', secondary: '#FCD34D', primaryLight: 'rgba(251,146,60,0.16)', secondaryLight: 'rgba(252,211,77,0.16)' }
    },
    sakura_zen: {
      name: 'Sakura Zen',
      light: { primary: '#0F766E', secondary: '#BE185D', primaryLight: '#F3E8FF', secondaryLight: '#FCE7F3' },
      dark:  { primary: '#A78BFA', secondary: '#F472B6', primaryLight: 'rgba(167,139,250,0.16)', secondaryLight: 'rgba(244,114,182,0.16)' }
    },
    ocean_cyan: {
      name: 'Minimal Nórdico',
      light: { primary: '#0369A1', secondary: '#0F766E', primaryLight: '#E0F2FE', secondaryLight: '#CCFBF1' },
      dark:  { primary: '#38BDF8', secondary: '#2DD4BF', primaryLight: 'rgba(56,189,248,0.16)', secondaryLight: 'rgba(45,212,191,0.16)' }
    }
  };

  const CANVAS_PRESETS = {
    classic: {
      name: 'Pergamino',
      light: { canvas: '#FAF9F6', surface: '#FFFFFF', container: '#F3EFEA' },
      dark:  { canvas: '#0E131F', surface: '#161E2D', container: '#1C2638' }
    },
    forest_sage: {
      name: 'Menta',
      light: { canvas: '#EAF4ED', surface: '#F6FAF7', container: '#D7EADC' },
      dark:  { canvas: '#0A1610', surface: '#12241B', container: '#1A3527' }
    },
    midnight_indigo: {
      name: 'Polar',
      light: { canvas: '#E6F0F8', surface: '#F5F9FD', container: '#D1E4F3' },
      dark:  { canvas: '#09101D', surface: '#111E35', container: '#192B4B' }
    },
    warm_mocha: {
      name: 'Papiro',
      light: { canvas: '#F5ECE1', surface: '#FCFAF6', container: '#E8DAC8' },
      dark:  { canvas: '#150F0B', surface: '#221913', container: '#32251D' }
    },
    sakura_zen: {
      name: 'Lavanda',
      light: { canvas: '#E8EEF5', surface: '#FAF6FC', container: '#E4D3EE' },
      dark:  { canvas: '#130B1C', surface: '#20122E', container: '#2F1B43' }
    },
    ocean_cyan: {
      name: 'Glaciar',
      light: { canvas: '#E5F3F5', surface: '#F5FAFB', container: '#CCE8EB' },
      dark:  { canvas: '#061418', surface: '#0D2026', container: '#142E37' }
    }
  };

  function hexToRgbString(hex) {
    const clean = hex.replace('#', '');
    if (clean.length !== 6) return '158, 27, 50';
    const r = parseInt(clean.substring(0, 2), 16);
    const g = parseInt(clean.substring(2, 4), 16);
    const b = parseInt(clean.substring(4, 6), 16);
    if (isNaN(r) || isNaN(g) || isNaN(b)) return '158, 27, 50';
    return `${r}, ${g}, ${b}`;
  }

  function hslToHex(h, s, l) {
    l /= 100;
    const a = s * Math.min(l, 1 - l) / 100;
    const f = n => {
      const k = (n + h / 30) % 12;
      const color = l - a * Math.max(Math.min(k - 3, 9 - k, 1), -1);
      return Math.round(255 * color).toString(16).padStart(2, '0');
    };
    return `#${f(0)}${f(8)}${f(4)}`.toUpperCase();
  }

  function setupLiveThemeStudio() {
    const root = document.documentElement;
    const accentBtns = document.querySelectorAll('.accent-swatch-btn');
    const canvasBtns = document.querySelectorAll('.canvas-swatch-chip');
    const modeBtns = document.querySelectorAll('[data-theme-mode-btn]');
    const navQuickModeBtn = document.getElementById('btn-nav-theme-mode');
    const navModeLabel = document.getElementById('nav-theme-mode-label');
    const summaryEl = document.getElementById('studio-active-summary');
    const previewTitleEl = document.getElementById('preview-mini-title');
    const previewSubEl = document.getElementById('preview-mini-sub');
    const hueSlider = document.getElementById('studio-hue-slider');
    const hexInput = document.getElementById('studio-hex-input');
    const hexReadout = document.getElementById('studio-hex-readout');
    const resetBtn = document.getElementById('btn-studio-reset');

    if (!accentBtns.length) return;

    let currentMode = 'light';
    let currentAccentId = 'classic';
    let currentCanvasId = 'classic';
    let customPrimaryHex = null;

    function applyThemeToDOM() {
      const accentObj = ACCENT_PRESETS[currentAccentId] || ACCENT_PRESETS.classic;
      const canvasObj = CANVAS_PRESETS[currentCanvasId] || CANVAS_PRESETS.classic;

      const accentPalette = currentMode === 'dark' ? accentObj.dark : accentObj.light;
      const canvasPalette = currentMode === 'dark' ? canvasObj.dark : canvasObj.light;

      const primaryColor = customPrimaryHex || accentPalette.primary;
      const secondaryColor = accentPalette.secondary;

      // Apply data-theme-mode attribute for Dark/Light tokens
      root.setAttribute('data-theme-mode', currentMode);

      // Update reactive CSS custom properties across the entire page
      root.style.setProperty('--brand-primary', primaryColor);
      root.style.setProperty('--brand-primary-hover', primaryColor);
      root.style.setProperty('--brand-primary-rgb', hexToRgbString(primaryColor));
      root.style.setProperty('--brand-primary-light', customPrimaryHex ? `rgba(${hexToRgbString(primaryColor)}, 0.14)` : accentPalette.primaryLight);
      root.style.setProperty('--brand-secondary', secondaryColor);
      root.style.setProperty('--brand-secondary-rgb', hexToRgbString(secondaryColor));
      root.style.setProperty('--brand-secondary-light', accentPalette.secondaryLight);

      root.style.setProperty('--bg-canvas', canvasPalette.canvas);
      root.style.setProperty('--bg-surface', canvasPalette.surface);
      root.style.setProperty('--bg-surface-container', canvasPalette.container);

      // Update canvas swatch preview circles to reflect light/dark mode
      canvasBtns.forEach(chip => {
        const presetKey = chip.getAttribute('data-bg-preset');
        const dot = chip.querySelector('.canvas-chip-preview');
        if (dot && CANVAS_PRESETS[presetKey]) {
          dot.style.background = currentMode === 'dark'
            ? CANVAS_PRESETS[presetKey].dark.canvas
            : CANVAS_PRESETS[presetKey].light.canvas;
        }
      });

      // Update UI labels
      const modeName = currentMode === 'dark' ? 'Modo Oscuro' : 'Modo Claro';
      const accentLabel = customPrimaryHex ? `Hex Studio (${primaryColor})` : accentObj.name;
      if (summaryEl) {
        summaryEl.textContent = `${accentLabel} • Lienzo ${canvasObj.name} • ${modeName}`;
      }
      if (previewTitleEl) {
        previewTitleEl.textContent = `Vista previa: ${accentLabel} + ${canvasObj.name}`;
      }
      if (previewSubEl) {
        previewSubEl.textContent = `${modeName} activo • Contraste verificado WCAG AAA en toda la web`;
      }
      if (hexReadout) hexReadout.textContent = primaryColor.toUpperCase();
      if (hexInput && document.activeElement !== hexInput) {
        hexInput.value = primaryColor.replace('#', '').toUpperCase();
      }
      if (navModeLabel) {
        navModeLabel.textContent = currentMode === 'dark' ? 'Modo Oscuro' : 'Modo Claro';
      }
    }

    // Accent preset buttons
    accentBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        currentAccentId = btn.getAttribute('data-accent-preset');
        customPrimaryHex = null;
        accentBtns.forEach(b => {
          const active = b === btn;
          b.classList.toggle('active', active);
          b.setAttribute('aria-checked', active ? 'true' : 'false');
        });
        applyThemeToDOM();
      });
    });

    // Canvas preset buttons
    canvasBtns.forEach(chip => {
      chip.addEventListener('click', () => {
        currentCanvasId = chip.getAttribute('data-bg-preset');
        canvasBtns.forEach(c => {
          const active = c === chip;
          c.classList.toggle('active', active);
          c.setAttribute('aria-checked', active ? 'true' : 'false');
        });
        applyThemeToDOM();
      });
    });

    // Light / Dark segmented buttons
    function setMode(newMode) {
      currentMode = newMode;
      modeBtns.forEach(b => {
        const active = b.getAttribute('data-theme-mode-btn') === newMode;
        b.classList.toggle('active', active);
        b.setAttribute('aria-pressed', active ? 'true' : 'false');
      });
      applyThemeToDOM();
    }

    modeBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        setMode(btn.getAttribute('data-theme-mode-btn'));
      });
    });

    // Header quick mode toggle
    if (navQuickModeBtn) {
      navQuickModeBtn.addEventListener('click', () => {
        setMode(currentMode === 'light' ? 'dark' : 'light');
      });
    }

    // Custom HSL Rainbow Slider
    if (hueSlider) {
      hueSlider.addEventListener('input', (e) => {
        const hue = parseInt(e.target.value, 10);
        const lightness = currentMode === 'dark' ? 66 : 38;
        customPrimaryHex = hslToHex(hue, 76, lightness);
        accentBtns.forEach(b => {
          b.classList.remove('active');
          b.setAttribute('aria-checked', 'false');
        });
        applyThemeToDOM();
      });
    }

    // Custom Hex Text Input
    if (hexInput) {
      hexInput.addEventListener('input', (e) => {
        const cleaned = e.target.value.replace(/[^0-9A-Fa-f]/g, '').toUpperCase();
        e.target.value = cleaned;
        if (cleaned.length === 6) {
          customPrimaryHex = `#${cleaned}`;
          accentBtns.forEach(b => {
            b.classList.remove('active');
            b.setAttribute('aria-checked', 'false');
          });
          applyThemeToDOM();
        }
      });
    }

    // Reset button
    if (resetBtn) {
      resetBtn.addEventListener('click', () => {
        currentAccentId = 'classic';
        currentCanvasId = 'classic';
        currentMode = 'light';
        customPrimaryHex = null;
        accentBtns.forEach(b => {
          const active = b.getAttribute('data-accent-preset') === 'classic';
          b.classList.toggle('active', active);
          b.setAttribute('aria-checked', active ? 'true' : 'false');
        });
        canvasBtns.forEach(c => {
          const active = c.getAttribute('data-bg-preset') === 'classic';
          c.classList.toggle('active', active);
          c.setAttribute('aria-checked', active ? 'true' : 'false');
        });
        setMode('light');
      });
    }

    applyThemeToDOM();
  }

  // 11. Interactive Proactive Notifications Engine Simulator (SettingsNotificationsSection.kt & NotificationSender.kt)
  function setupNotificationsSimulator() {
    const channelRows = document.querySelectorAll('.notif-channel-row');
    const timingChips = document.querySelectorAll('.timing-chip');
    const banner = document.getElementById('live-android-notif-banner');
    const channelTag = document.getElementById('notif-channel-tag');
    const bannerTitle = document.getElementById('notif-banner-title');
    const bannerBody = document.getElementById('notif-banner-body');
    const typeIcon = document.getElementById('notif-type-icon');
    const primaryAction = document.getElementById('notif-action-primary');
    const testBtn = document.getElementById('btn-test-notification');

    if (!channelRows.length || !banner) return;

    let currentChannel = 'class';
    let classLead = '15 min';
    let examLead = '2 días';

    function getChannelPayload(channel) {
      if (channel === 'class') {
        return {
          tag: 'CANAL CLASES • WORKMANAGER',
          icon: '#icon-school',
          title: `Clase en ${classLead}: Cálculo Diferencial`,
          body: '08:00 - 10:00 • Aula 204 (Edificio Ciencias). Toca para abrir el cuaderno de la sesión.',
          action: 'Abrir apunte de clase'
        };
      }
      if (channel === 'exam') {
        return {
          tag: 'CANAL EVALUACIONES • ALARMA EXACTA',
          icon: '#icon-quiz',
          title: `Evaluación en ${examLead}: Parcial II Optimización`,
          body: 'Cálculo Diferencial • Representa el 35% de tu nota final. ¡Buen momento para repasar!',
          action: 'Simular nota requerida'
        };
      }
      return {
        tag: 'RESUMEN MATUTINO • 07:00 AM',
        icon: '#icon-lightmode',
        title: 'Buenos días: Tu agenda de hoy en NotiFast',
        body: 'Tienes 3 clases programadas (primera a las 08:00 AM) y 1 entrega próxima esta semana.',
        action: 'Ver agenda del día'
      };
    }

    function updateNotifPreview(channel, pulse = true) {
      currentChannel = channel;
      const data = getChannelPayload(channel);

      channelRows.forEach(row => {
        const active = row.getAttribute('data-notif-channel') === channel;
        row.classList.toggle('active-preview', active);
        row.setAttribute('aria-selected', active ? 'true' : 'false');
      });

      if (channelTag) channelTag.textContent = data.tag;
      if (bannerTitle) bannerTitle.textContent = data.title;
      if (bannerBody) bannerBody.textContent = data.body;
      if (primaryAction) primaryAction.textContent = data.action;
      if (typeIcon) {
        typeIcon.innerHTML = `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><use href="${data.icon}"></use></svg>`;
      }

      if (pulse) {
        banner.classList.remove('notif-pulse-trigger');
        void banner.offsetWidth;
        banner.classList.add('notif-pulse-trigger');
      }
    }

    channelRows.forEach(row => {
      row.addEventListener('click', (e) => {
        if (e.target.closest('.timing-chip')) return;
        const ch = row.getAttribute('data-notif-channel');
        updateNotifPreview(ch, true);
      });
      row.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          const ch = row.getAttribute('data-notif-channel');
          updateNotifPreview(ch, true);
        }
      });
    });

    timingChips.forEach(chip => {
      chip.addEventListener('click', (e) => {
        e.stopPropagation();
        const ch = chip.getAttribute('data-channel');
        const lead = chip.getAttribute('data-lead');

        document.querySelectorAll(`.timing-chip[data-channel="${ch}"]`).forEach(c => {
          c.classList.toggle('active', c === chip);
        });

        if (ch === 'class') classLead = lead;
        if (ch === 'exam') examLead = lead;

        updateNotifPreview(ch, true);
      });
    });

    if (testBtn) {
      testBtn.addEventListener('click', () => {
        const data = getChannelPayload(currentChannel);
        updateNotifPreview(currentChannel, true);

        const spanEl = testBtn.querySelector('span');
        if (spanEl) {
          const prevText = spanEl.textContent;
          spanEl.textContent = '¡Notificación disparada!';
          setTimeout(() => { spanEl.textContent = prevText; }, 1800);
        }

        if (typeof window.__notifastTriggerHeroToast === 'function') {
          window.__notifastTriggerHeroToast(data.title, data.body);
        }
      });
    }
  }

  // 12. Mobile Navigation Drawer Toggle (< 992px)
  function setupMobileNavigation() {
    const toggleBtn = document.getElementById('btn-mobile-menu');
    const drawer = document.getElementById('mobile-nav-drawer');
    if (!toggleBtn || !drawer) return;

    function closeMenu() {
      toggleBtn.setAttribute('aria-expanded', 'false');
      toggleBtn.classList.remove('open');
      drawer.hidden = true;
    }

    toggleBtn.addEventListener('click', () => {
      const isOpen = toggleBtn.getAttribute('aria-expanded') === 'true';
      if (isOpen) {
        closeMenu();
      } else {
        toggleBtn.setAttribute('aria-expanded', 'true');
        toggleBtn.classList.add('open');
        drawer.hidden = false;
      }
    });

    drawer.querySelectorAll('a').forEach(link => {
      link.addEventListener('click', () => {
        closeMenu();
      });
    });

    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && !drawer.hidden) {
        closeMenu();
        toggleBtn.focus();
      }
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    setupMobileNavigation();
    setupFloatingRadarDock();
    setupTakeNotesAction();
    setupEditorSimulator();
    setupGradeScaleSwitcher();
    setupEvaluationsCalendar();
    setupAnalyticsDashboard();
    setupHeroLogoMotion();
    setupLiveThemeStudio();
    setupNotificationsSimulator();
    initGSAPAnimations();
  });
})();
