# ADR-0005: Motor Matemático Multiescala Unificado con `GradePolicy`

- **Estado:** Aceptado
- **Fecha:** 2026-10-06
- **Módulo:** Dominio y Políticas (`domain/policy`, `domain/model`)

---

## Contexto y Planteamiento del Problema

En las primeras versiones del proyecto existían tres deficiencias matemáticas graves:
1. **El error de "promedio de promedios":** El promedio global de la carrera se calculaba sumando los promedios de cada materia y dividiendo entre el número de materias, ignorando que las materias tienen cargas horarias y pesos de créditos diferentes.
2. **Duplicación de código:** El cálculo de notas ponderadas estaba repetido 4 veces en distintos ViewModels y composables, con discrepancias sutiles de redondeo.
3. **Escala fija artificial:** El sistema forzaba una escala de 0 a 10 puntos, incompatible con el sistema universitario de Venezuela y Perú (0 a 20 puntos), de Colombia y México (0 a 100%) o el sistema anglosajón (GPA 0.0 a 4.0).

Adicionalmente, los estudiantes no disponían de una herramienta que les permitiera calcular con rigor cuánto puntaje necesitaban en las evaluaciones pendientes para alcanzar su meta académica deseada.

## Decisión

Se decide unificar toda la lógica aritmética y de calificaciones en un **objeto de dominio puro y libre de dependencias de Android**: `GradePolicy.kt`:

1. **Centralización Matemática Estricta:** Cero lógica de cálculo en ViewModels ni composables. Toda consulta invoca las funciones puras de `GradePolicy`:
   - `calculateSubjectAverage(evaluations: List<Evaluation>): Double?`
   - `calculateWeightedContribution(grade: Double, weight: Double): Double`
   - `calculateRequiredGrade(currentScore: Double, evaluatedWeight: Double, targetGrade: Double, remainingWeight: Double): Double?`
2. **Soporte de 4 Escalas Oficiales (`GradeScale`):**
   - `BASE_20`: Rango $0.0$ a $20.0$, aprobatoria $\ge 9.5$ (redondeada a $10.0$ según reglamento UNERG).
   - `BASE_10`: Rango $0.0$ a $10.0$, aprobatoria $\ge 4.0$ o $5.0$.
   - `BASE_100`: Rango $0.0\%$ a $100.0\%$, aprobatoria $\ge 60.0\%$.
   - `GPA_4`: Rango $0.0$ a $4.0$, con letras equivalentes (A, B, C, D, F).
3. **Ponderación sobre Puntos Evaluados:** El promedio acumulado de una materia con evaluaciones pendientes se calcula sobre el porcentaje ya calificado:
   $$\text{Promedio Acumulado} = \frac{\sum_{i=1}^k (\text{Nota}_i \times \text{Peso}_i)}{\sum_{i=1}^k \text{Peso}_i}$$
4. **Simulador Predictivo de Meta Deseada:** Resuelve la nota requerida en el porcentaje restante y categoriza el resultado en: *Alcanzada con éxito*, *Nota viable requerida* o *Meta matemáticamente inalcanzable*.
5. **Tipografía Tabular:** Toda cifra de nota se muestra en Compose con `JetBrains Mono` tabular para garantizar que los decimales no desalineen los elementos visuales.

## Alternativas Consideradas

| Alternativa | Motivo de Descarte |
|---|---|
| **Cálculo dentro de cada ViewModel** | Viola el principio DRY, dificulta la cobertura de pruebas unitarias y propicia inconsistencias entre pantallas. |
| **Escala Única Fija a 20 Puntos** | Restringe la portabilidad del producto a estudiantes de otros países y sistemas educativos. |
| **Promedio Simple No Ponderado** | Inaceptable en el ámbito universitario donde un examen parcial pesa 30% y un laboratorio 15%. |

## Consecuencias

### Positivas
- ✅ **Cero Errores Aritméticos:** Cálculos verificados y blindados por decenas de pruebas unitarias en `GradePolicyTest.kt`.
- ✅ **Tranquilidad Estudiantil:** Si una materia no tiene notas, muestra un estado sobrio en lugar de un `0.0` penalizador.
- ✅ **Planificación Estratégica:** El estudiante sabe exactamente qué nota buscar en su examen final.
- ✅ **Flexibilidad Institucional:** Soporte fluido tanto para bachillerato como para cualquier universidad.

### Negativas y Mitigaciones
- ⚠️ *Manejo de divisiones por cero en materias sin evaluaciones calificadas:* Mitigado retornando `null` o valores seguros `GradeResult.NotGradedYet` manejados explícitamente en la UI.
