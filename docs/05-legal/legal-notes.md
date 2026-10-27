# Notas Legales, Propiedad Intelectual e Independencia Institucional: NotiFast

| Metadato | Valor |
|---|---|
| **Licencia de Código Fuente** | Apache License, Version 2.0 |
| **Naturaleza del Proyecto** | Desarrollo de Software Libre Independiente y Proyecto Especial de Grado (Tesis) |
| **Institución Académica de Vinculación** | Universidad Nacional Experimental Rómulo Gallegos (UNERG) |
| **Área Académica** | Área de Ingeniería de Sistemas (AIS) — Escuela de Ingeniería en Informática |
| **Documentos Relacionados** | [Visión del Producto](../00-vision/product-vision.md) · [Privacidad](../04-quality-privacy/privacy-offline-first.md) |

---

## 1. Declaración Formal de Independencia Institucional

> **NotiFast es un proyecto de software de autor privado, independiente y de código abierto. No está afiliado, respaldado, patrocinado, mantenido ni operado oficialmente por la Universidad Nacional Experimental Rómulo Gallegos (UNERG), el Departamento de Admisión y Control de Estudios (DACE), ni ninguna otra entidad gubernamental o educativa.**

Las referencias a "UNERG" y "DACE" en el código fuente, la interfaz de usuario y la documentación técnica corresponden exclusivamente a la compatibilidad con el formato de comprobante de horario en PDF emitido por el sistema DACE-UNERG, concebido como una utilidad de interoperabilidad técnica para la comunidad universitaria de San Juan de los Morros (Guárico, Venezuela).

---

## 2. Licencia de Software: Apache License 2.0

NotiFast se distribuye bajo los términos de la **Apache License, Version 2.0** (la "Licencia"). El código fuente puede ser consultado, modificado y distribuido de acuerdo con las condiciones estipuladas en la misma.

```
Copyright 2026 Equipo de Desarrollo NotiFast

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## 3. Política de Privacidad Estudiantil y Protección a Menores de Edad

NotiFast está diseñado para ser utilizado tanto por estudiantes universitarios como por alumnos de educación secundaria y media general (adolescentes a partir de los 13–14 años):

1. **Principio de Minimización Absoluta de Datos:**
   - La aplicación no recopila datos biométricos, números de cédula, direcciones residenciales ni métodos de pago.
   - El nombre o apodo que el estudiante registra en el Onboarding se almacena exclusivamente de forma local en su dispositivo.
2. **Cero Publicidad Dirigida:**
   - NotiFast no incluye redes publicitarias de terceros (como Google AdMob, Unity Ads o Facebook Audience Network) que rastreen el comportamiento o la identidad de los estudiantes.
3. **Control Parental y Educativo Transparente:**
   - El código fuente es público y auditable, permitiendo a padres, docentes y directores de instituciones educativas verificar que la herramienta no extrae ni monetiza la información de los jóvenes.

---

## 4. Notas de Tesis de Grado y Propiedad Intelectual

1. **Proyecto de Tesis:** NotiFast constituye el núcleo técnico y práctico del Trabajo Especial de Grado para optar al título de **Ingeniero en Informática** en la Universidad Nacional Experimental Rómulo Gallegos.
2. **Autoría Intelectual:** La autoría metodológica, el diseño de la arquitectura Clean Architecture + MVI, el desarrollo de los algoritmos de `GradePolicy`, `ThemePaletteGenerator` y `UnergPdfScheduleParser`, así como la suite de 805 pruebas unitarias automatizadas corresponden al autor del proyecto.
3. **Uso Académico y Reproducibilidad:** La comunidad académica universitaria queda facultada para emplear este repositorio como caso de estudio de ingeniería de software en Android, sujeto al reconocimiento explícito de la autoría original bajo los términos de la Licencia Apache 2.0.

---

## 5. Atribuciones y Créditos de Código Abierto

NotiFast se construye sobre el ecosistema abierto de software y rinde tributo a las siguientes obras y proyectos:

| Biblioteca / Recurso | Licencia | Propósito en NotiFast |
|---|---|---|
| **Kotlin & Android Jetpack** (Compose, Room, DataStore, WorkManager, Hilt) | Apache 2.0 | Núcleo del lenguaje, persistencia, arquitectura y UI reactiva. |
| **Google Fonts: Plus Jakarta Sans** | SIL Open Font License (OFL) | Tipografía principal de interfaz y lectura editorial. |
| **Google Fonts: JetBrains Mono** | SIL Open Font License (OFL) | Tipografía monoespaciada tabular para notas, porcentajes y horarios. |
| **ZXing ("Zebra Crossing")** | Apache 2.0 | Codificación matricial de códigos QR para transferencia P2P. |
| **OkHttp** (Square, Inc.) | Apache 2.0 | Cliente HTTP para llamadas REST v3 multipart en streaming a Google Drive. |
| **Coil** | Apache 2.0 | Carga eficiente y almacenamiento en caché de imágenes locales. |
| **MockK, Turbine & Robolectric** | Apache 2.0 / MIT | Infraestructura de pruebas unitarias y verificación de coroutines. |
