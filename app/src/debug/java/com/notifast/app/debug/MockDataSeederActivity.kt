package com.notifast.app.debug

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.notifast.app.data.local.dao.EvaluationDao
import com.notifast.app.data.local.dao.NoteDao
import com.notifast.app.data.local.dao.ScheduleDao
import com.notifast.app.data.local.dao.StudyDocumentDao
import com.notifast.app.data.local.dao.SubjectDao
import com.notifast.app.data.local.dao.TeacherDao
import com.notifast.app.data.local.dao.UserDao
import com.notifast.app.data.local.entity.EvaluationEntity
import com.notifast.app.data.local.entity.NoteEntity
import com.notifast.app.data.local.entity.ScheduleEntity
import com.notifast.app.data.local.entity.StudyDocumentEntity
import com.notifast.app.data.local.entity.SubjectEntity
import com.notifast.app.data.local.entity.TeacherEntity
import com.notifast.app.data.local.entity.UserEntity
import com.notifast.app.data.model.CheckItem
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.domain.util.NoteBlockCodec
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * DEBUG-ONLY mock data seeder.
 *
 * Launch with:
 *   adb shell am start -n com.notifast.app/com.notifast.app.debug.MockDataSeederActivity
 *
 * Optional modes:
 *   adb shell am start -n com.notifast.app/com.notifast.app.debug.MockDataSeederActivity --es mode activate
 *   adb shell am start -n com.notifast.app/com.notifast.app.debug.MockDataSeederActivity --es mode adjust-live
 *
 * To remove the seeder: delete app/src/debug/AndroidManifest.xml and this file.
 * No release source is modified.
 */
@AndroidEntryPoint
class MockDataSeederActivity : ComponentActivity() {

    @Inject lateinit var userDao: UserDao
    @Inject lateinit var subjectDao: SubjectDao
    @Inject lateinit var evaluationDao: EvaluationDao
    @Inject lateinit var scheduleDao: ScheduleDao
    @Inject lateinit var noteDao: NoteDao
    @Inject lateinit var teacherDao: TeacherDao
    @Inject lateinit var studyDocumentDao: StudyDocumentDao
    @Inject lateinit var dataStore: DataStore<Preferences>

    private val statusView by lazy {
        TextView(this).apply {
            textSize = 18f
            setPadding(56, 240, 56, 56)
            text = "Sembrando datos de demostración…"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(statusView)

        val mode = intent.getStringExtra("mode") ?: "seed"
        lifecycleScope.launch {
            val result = runCatching {
                when (mode) {
                    "activate" -> {
                        activateExistingUser()
                        "Perfil de demostración activado"
                    }
                    "adjust-live" -> {
                        adjustLiveClass()
                        "Horario en vivo ajustado a la hora actual"
                    }
                    "seed" -> {
                        seedAll()
                        "Datos de demostración sembrados"
                    }
                    else -> "Modo desconocido: $mode"
                }
            }
            val message = result.getOrElse { error ->
                Log.e(TAG, "Seeder failed", error)
                "Error: ${error.message}"
            }
            Log.i(TAG, message)
            statusView.text = message
            delay(1200)
            finish()
        }
    }

    private suspend fun activateExistingUser() {
        val user = userDao.getUserById(USER_ID)
            ?: error("No existe el perfil $USER_ID")
        writePreferences()
        Log.i(TAG, "Activated ${user.firstName} ${user.lastName}")
    }

    private suspend fun adjustLiveClass() {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        val day = if (calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY) {
            1
        } else {
            calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
        }
        val startMinutes = ((calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calendar.get(java.util.Calendar.MINUTE) - 20).coerceIn(0, 23 * 60))
        val endMinutes = (startMinutes + 150).coerceAtMost(23 * 60 + 59)
        scheduleDao.insertSchedule(
            ScheduleEntity(
                id = "sch-live-current",
                userId = USER_ID,
                subjectId = "sub-calculo-ii",
                subjectName = "Cálculo II",
                modality = "presencial",
                dayOfWeek = day,
                startTime = formatMinutes(startMinutes),
                endTime = formatMinutes(endMinutes),
                classroom = "Lab. 204",
                teacher = "Prof. Ana Méndez",
                meetingUrl = null,
                color = 0xFF1D4ED8,
                createdAt = Date(now),
                updatedAt = Date(now)
            )
        )
    }

    private suspend fun seedAll() {
        val now = System.currentTimeMillis()
        val today = Date(now)

        // Idempotent: wipe only this fixture user's rows before inserting.
        noteDao.deleteNotesByUserId(USER_ID)
        evaluationDao.deleteEvaluationsByUserId(USER_ID)
        scheduleDao.deleteSchedulesByUserId(USER_ID)
        teacherDao.deleteTeachersByUserId(USER_ID)
        studyDocumentDao.getAllDocuments(USER_ID).forEach {
            studyDocumentDao.deleteDocument(USER_ID, it.id)
        }
        subjectDao.deleteSubjectsByUserId(USER_ID)
        userDao.deleteUser(USER_ID)

        userDao.insertUser(
            UserEntity(
                uid = USER_ID,
                email = "valentina.rios@local.notifast",
                firstName = "Valentina",
                lastName = "Ríos",
                profilePictureUrl = null,
                createdAt = today,
                updatedAt = today
            )
        )

        subjectDao.insertSubjects(subjects(now))
        teacherDao.insertTeachers(teachers(now))
        scheduleDao.insertSchedules(schedules(now))
        evaluationDao.insertEvaluations(evaluations(now))
        noteDao.insertNotes(notes(now))
        seedStudyDocuments(now)
        writePreferences()

        Log.i(TAG, "Seeded: 5 subjects, 3 teachers, 12 schedules, 15 evaluations, 6 notes, 3 documents")
    }

    private fun subjects(now: Long): List<SubjectEntity> {
        fun subject(id: String, name: String, icon: String, color: String, section: String) =
            SubjectEntity(
                userId = USER_ID,
                id = id,
                name = name,
                icon = icon,
                colorHex = color,
                cycle = CYCLE,
                section = section,
                createdAt = Date(now)
            )

        return listOf(
            subject("sub-calculo-ii", "Cálculo II", "📐", "#1D4ED8", "Sección 01"),
            subject("sub-fisica-i", "Física I", "⚛️", "#0F766E", "Sección 02"),
            subject("sub-programacion", "Programación", "💻", "#7C3AED", "Sección 01"),
            subject("sub-estadistica", "Estadística", "📊", "#B45309", "Sección 03"),
            subject("sub-ingles-tecnico", "Inglés Técnico", "🌐", "#BE185D", "Sección 01")
        )
    }

    private fun teachers(now: Long): List<TeacherEntity> = listOf(
        TeacherEntity(
            userId = USER_ID,
            id = "teacher-ana-mendez",
            firstName = "Ana",
            lastName = "Méndez",
            phoneNumber = "0414-000-0001",
            email = "ana.mendez@notifast.local",
            subjectNamesJson = """["Cálculo II","Física I"]""",
            notes = "Oficina 2-12 · jueves de 11:00 a 12:00",
            createdAt = Date(now),
            updatedAt = Date(now)
        ),
        TeacherEntity(
            userId = USER_ID,
            id = "teacher-luis-perez",
            firstName = "Luis",
            lastName = "Pérez",
            phoneNumber = "0414-000-0002",
            email = "luis.perez@notifast.local",
            subjectNamesJson = """["Programación","Estadística"]""",
            notes = "Laboratorio de Sistemas 2 · martes de 14:00 a 16:00",
            createdAt = Date(now),
            updatedAt = Date(now)
        ),
        TeacherEntity(
            userId = USER_ID,
            id = "teacher-carmen-silva",
            firstName = "Carmen",
            lastName = "Silva",
            phoneNumber = "0414-000-0003",
            email = "carmen.silva@notifast.local",
            subjectNamesJson = """["Inglés Técnico"]""",
            notes = "Aula 105 · viernes de 12:00 a 13:00",
            createdAt = Date(now),
            updatedAt = Date(now)
        )
    )

    private fun schedules(now: Long): List<ScheduleEntity> {
        data class Slot(
            val id: String,
            val day: Int,
            val start: String,
            val end: String,
            val subjectId: String,
            val subject: String,
            val room: String,
            val teacher: String,
            val color: Long
        )

        val slots = listOf(
            Slot("sch-lun-calculo", 1, "08:00", "10:00", "sub-calculo-ii", "Cálculo II", "Aula 302", "Prof. Ana Méndez", 0xFF1D4ED8),
            Slot("sch-lun-prog", 1, "10:00", "12:00", "sub-programacion", "Programación", "Lab. Sistemas 2", "Prof. Luis Pérez", 0xFF7C3AED),
            Slot("sch-lun-ingles", 1, "14:00", "16:00", "sub-ingles-tecnico", "Inglés Técnico", "Aula 105", "Prof. Carmen Silva", 0xFFBE185D),
            Slot("sch-mar-fisica", 2, "08:00", "10:00", "sub-fisica-i", "Física I", "Lab. Física 1", "Prof. Ana Méndez", 0xFF0F766E),
            Slot("sch-mar-estadistica", 2, "10:00", "12:00", "sub-estadistica", "Estadística", "Aula 210", "Prof. Luis Pérez", 0xFFB45309),
            Slot("sch-mie-calculo", 3, "08:00", "10:00", "sub-calculo-ii", "Cálculo II", "Aula 302", "Prof. Ana Méndez", 0xFF1D4ED8),
            Slot("sch-mie-prog", 3, "14:00", "17:00", "sub-programacion", "Programación", "Lab. Sistemas 2", "Prof. Luis Pérez", 0xFF7C3AED),
            Slot("sch-jue-calculo", 4, "08:00", "12:00", "sub-calculo-ii", "Cálculo II", "Lab. 204", "Prof. Ana Méndez", 0xFF1D4ED8),
            Slot("sch-jue-fisica", 4, "14:00", "16:00", "sub-fisica-i", "Física I", "Aula 208", "Prof. Ana Méndez", 0xFF0F766E),
            Slot("sch-vie-estadistica", 5, "08:00", "10:00", "sub-estadistica", "Estadística", "Aula 210", "Prof. Luis Pérez", 0xFFB45309),
            Slot("sch-vie-ingles", 5, "10:00", "12:00", "sub-ingles-tecnico", "Inglés Técnico", "Aula 105", "Prof. Carmen Silva", 0xFFBE185D),
            Slot("sch-vie-prog", 5, "14:00", "16:00", "sub-programacion", "Programación", "Lab. Sistemas 2", "Prof. Luis Pérez", 0xFF7C3AED)
        )

        return slots.map { slot ->
            ScheduleEntity(
                id = slot.id,
                userId = USER_ID,
                subjectId = slot.subjectId,
                subjectName = slot.subject,
                modality = "presencial",
                dayOfWeek = slot.day,
                startTime = slot.start,
                endTime = slot.end,
                classroom = slot.room,
                teacher = slot.teacher,
                meetingUrl = null,
                color = slot.color,
                createdAt = Date(now),
                updatedAt = Date(now)
            )
        }
    }

    private fun evaluations(now: Long): List<EvaluationEntity> {
        fun eval(
            id: String,
            subjectId: String,
            subjectName: String,
            title: String,
            type: String,
            grade: Double?,
            weight: Double,
            daysFromNow: Int,
            presented: Boolean,
            description: String
        ) = EvaluationEntity(
            id = id,
            userId = USER_ID,
            subjectId = subjectId,
            subjectName = subjectName,
            title = title,
            description = description,
            date = Date(now + daysFromNow * DAY_MILLIS),
            noteIds = emptyList(),
            imageUrl = null,
            quickTips = "",
            type = type,
            grade = grade,
            weight = weight,
            isPresented = presented,
            gradeScaleId = "0-20",
            createdAt = Date(now),
            updatedAt = Date(now)
        )

        return listOf(
            eval("eval-cal-parcial-1", "sub-calculo-ii", "Cálculo II", "Parcial I", "EXAM", 16.0, 35.0, -24, true, "Derivadas, límites y continuidad."),
            eval("eval-cal-quiz", "sub-calculo-ii", "Cálculo II", "Quiz", "EXAM", 18.0, 15.0, -12, true, "Quiz corto de reglas de derivación."),
            eval("eval-cal-proyecto", "sub-calculo-ii", "Cálculo II", "Proyecto", "PROJECT", null, 20.0, 9, false, "Modelado de optimización con derivadas."),
            eval("eval-cal-final", "sub-calculo-ii", "Cálculo II", "Final", "EXAM", null, 30.0, 32, false, "Examen final acumulativo."),

            eval("eval-fis-lab", "sub-fisica-i", "Física I", "Laboratorio", "PROJECT", 17.0, 20.0, -18, true, "Informe de cinemática y dinámica."),
            eval("eval-fis-parcial", "sub-fisica-i", "Física I", "Parcial", "EXAM", 14.0, 30.0, -9, true, "Leyes de Newton y trabajo."),
            eval("eval-fis-final", "sub-fisica-i", "Física I", "Final", "EXAM", null, 50.0, 28, false, "Evaluación final de mecánica."),

            eval("eval-prog-proyecto", "sub-programacion", "Programación", "Proyecto", "PROJECT", 19.0, 40.0, -20, true, "Aplicación de consola con POO."),
            eval("eval-prog-parcial", "sub-programacion", "Programación", "Parcial", "EXAM", 18.0, 30.0, -10, true, "Estructuras de control y arreglos."),
            eval("eval-prog-final", "sub-programacion", "Programación", "Final", "EXAM", null, 30.0, 26, false, "Proyecto final y defensa."),

            eval("eval-est-parcial", "sub-estadistica", "Estadística", "Parcial", "EXAM", 15.0, 30.0, -15, true, "Estadística descriptiva."),
            eval("eval-est-taller", "sub-estadistica", "Estadística", "Taller", "PROJECT", 17.0, 20.0, -7, true, "Taller de probabilidad."),
            eval("eval-est-final", "sub-estadistica", "Estadística", "Final", "EXAM", null, 50.0, 30, false, "Distribuciones y estimación."),

            eval("eval-ing-oral", "sub-ingles-tecnico", "Inglés Técnico", "Oral", "PRESENTATION", 18.0, 30.0, -14, true, "Presentación técnica individual."),
            eval("eval-ing-escrito", "sub-ingles-tecnico", "Inglés Técnico", "Escrito", "EXAM", 16.0, 30.0, -6, true, "Reading and writing technical texts."),
            eval("eval-ing-final", "sub-ingles-tecnico", "Inglés Técnico", "Final", "EXAM", null, 40.0, 22, false, "Evaluación final integradora.")
        )
    }

    private fun notes(now: Long): List<NoteEntity> {
        data class NoteFixture(
            val id: String,
            val subjectId: String,
            val subject: String,
            val title: String,
            val blocks: List<NoteBlock>,
            val tags: List<String>,
            val daysAgo: Int
        )

        val fixtures = listOf(
            NoteFixture(
                "note-lagrange", "sub-calculo-ii", "Cálculo II", "Teorema de Lagrange",
                listOf(
                    NoteBlock.Heading("lag-h1", "Teorema del valor medio", 2),
                    NoteBlock.Paragraph(
                        "lag-p1",
                        "Si f es continua en [a, b] y derivable en (a, b), existe al menos un c en (a, b) donde la derivada instantánea iguala la pendiente media."
                    ),
                    NoteBlock.MathBlock("lag-m1", "f'(c) = \\frac{f(b) - f(a)}{b - a}"),
                    NoteBlock.Checklist(
                        "lag-c1",
                        listOf(
                            CheckItem("Verificar continuidad en [a, b]", true),
                            CheckItem("Verificar derivabilidad en (a, b)", true),
                            CheckItem("Despejar c igualando la pendiente media", false)
                        )
                    ),
                    NoteBlock.Quote("lag-q1", "Ideal para demostrar desigualdades y analizar monotonía.")
                ),
                listOf("teorema", "derivadas"), 1
            ),
            NoteFixture(
                "note-newton", "sub-fisica-i", "Física I", "Leyes de Newton",
                listOf(
                    NoteBlock.Heading("new-h1", "Dinámica clásica", 2),
                    NoteBlock.BulletList(
                        "new-b1",
                        listOf(
                            com.notifast.app.data.model.ListItem("1.ª ley: inercia; sin fuerza neta no cambia el estado de movimiento."),
                            com.notifast.app.data.model.ListItem("2.ª ley: F = m · a."),
                            com.notifast.app.data.model.ListItem("3.ª ley: acción y reacción sobre cuerpos distintos.")
                        )
                    ),
                    NoteBlock.Paragraph("new-p1", "Los diagramas de cuerpo libre son obligatorios antes de escribir cualquier ecuación."),
                    NoteBlock.Checklist(
                        "new-c1",
                        listOf(
                            CheckItem("Dibujar FBD del bloque", true),
                            CheckItem("Elegir ejes y signos", true),
                            CheckItem("Resolver el sistema de ecuaciones", false)
                        )
                    )
                ),
                listOf("newton", "dinámica"), 2
            ),
            NoteFixture(
                "note-complejidad", "sub-programacion", "Programación", "Complejidad algorítmica",
                listOf(
                    NoteBlock.Heading("com-h1", "Notación asintótica", 2),
                    NoteBlock.Paragraph("com-p1", "Big-O describe el crecimiento del tiempo o espacio cuando n tiende a infinito."),
                    NoteBlock.BulletList(
                        "com-b1",
                        listOf(
                            com.notifast.app.data.model.ListItem("O(1): acceso directo."),
                            com.notifast.app.data.model.ListItem("O(log n): búsqueda binaria."),
                            com.notifast.app.data.model.ListItem("O(n log n): mergesort."),
                            com.notifast.app.data.model.ListItem("O(n²): bucles anidados.")
                        )
                    ),
                    NoteBlock.Checklist(
                        "com-c1",
                        listOf(
                            CheckItem("Identificar el bucle dominante", true),
                            CheckItem("Ignorar constantes y términos menores", true),
                            CheckItem("Comparar dos implementaciones", false)
                        )
                    )
                ),
                listOf("algoritmos", "big-o"), 3
            ),
            NoteFixture(
                "note-normal", "sub-estadistica", "Estadística", "Distribución normal",
                listOf(
                    NoteBlock.Heading("nor-h1", "Campana de Gauss", 2),
                    NoteBlock.Paragraph("nor-p1", "Una variable normal se estandariza como z = (x − μ) / σ para usar la tabla Z."),
                    NoteBlock.MathBlock("nor-m1", "f(x) = \\frac{1}{\\sigma\\sqrt{2\\pi}} e^{-\\frac{(x-\\mu)^2}{2\\sigma^2}}"),
                    NoteBlock.Checklist(
                        "nor-c1",
                        listOf(
                            CheckItem("Calcular media y desviación estándar", true),
                            CheckItem("Estandarizar el valor de interés", false),
                            CheckItem("Buscar el área en la tabla Z", false)
                        )
                    )
                ),
                listOf("probabilidad", "normal"), 4
            ),
            NoteFixture(
                "note-phrasal", "sub-ingles-tecnico", "Inglés Técnico", "Phrasal verbs",
                listOf(
                    NoteBlock.Heading("phr-h1", "Verbos compuestos para ingeniería", 2),
                    NoteBlock.BulletList(
                        "phr-b1",
                        listOf(
                            com.notifast.app.data.model.ListItem("set up: configurar un entorno o equipo."),
                            com.notifast.app.data.model.ListItem("run out of: quedarse sin recursos."),
                            com.notifast.app.data.model.ListItem("look into: investigar un problema."),
                            com.notifast.app.data.model.ListItem("back up: respaldar información.")
                        )
                    ),
                    NoteBlock.Checklist(
                        "phr-c1",
                        listOf(
                            CheckItem("Escribir una oración con set up", true),
                            CheckItem("Usar look into en un correo", false)
                        )
                    )
                ),
                listOf("english", "vocabulario"), 5
            ),
            NoteFixture(
                "note-partes", "sub-calculo-ii", "Cálculo II", "Integración por partes",
                listOf(
                    NoteBlock.Heading("par-h1", "Regla del producto inversa", 2),
                    NoteBlock.Paragraph("par-p1", "Se elige u por la regla ILATE y dv por lo que sea fácil de integrar."),
                    NoteBlock.MathBlock("par-m1", "\\int u\\,dv = uv - \\int v\\,du"),
                    NoteBlock.Checklist(
                        "par-c1",
                        listOf(
                            CheckItem("Elegir u y dv", true),
                            CheckItem("Calcular du y v", false),
                            CheckItem("Aplicar la fórmula y simplificar", false)
                        )
                    )
                ),
                listOf("integrales", "cálculo"), 6
            )
        )

        return fixtures.map { fixture ->
            val blocksJson = NoteBlockCodec.toJson(fixture.blocks)
            val content = NoteBlockCodec.blocksToMarkdown(fixture.blocks)
            val timestamp = Date(now - fixture.daysAgo * DAY_MILLIS)
            NoteEntity(
                id = fixture.id,
                userId = USER_ID,
                subjectId = fixture.subjectId,
                subject = fixture.subject,
                title = fixture.title,
                content = content,
                imageUrl = null,
                tags = fixture.tags,
                timestamp = timestamp,
                updatedAt = timestamp,
                blocksJson = blocksJson,
                schemaVersion = 2
            )
        }
    }

    private suspend fun seedStudyDocuments(now: Long) {
        val docsDir = File(filesDir, "study_docs").apply { mkdirs() }
        val fixtures = listOf(
            Triple(
                Triple("doc-guia-derivadas", "sub-calculo-ii", "Cálculo II"),
                "Guía de derivadas",
                "Reglas de derivación, ejemplos y ejercicios propuestos."
            ),
            Triple(
                Triple("doc-formulario-fisica", "sub-fisica-i", "Física I"),
                "Formulario de física",
                "Cinética, dinámica, trabajo y energía en una sola hoja."
            ),
            Triple(
                Triple("doc-apuntes-poo", "sub-programacion", "Programación"),
                "Apuntes de POO",
                "Clases, objetos, herencia y polimorfismo con ejemplos."
            )
        )

        fixtures.forEachIndexed { index, (ids, title, body) ->
            val (id, subjectId, subjectName) = ids
            val file = File(docsDir, "$id.pdf")
            writeMinimalPdf(file, title, body)
            studyDocumentDao.upsertDocument(
                StudyDocumentEntity(
                    id = id,
                    userId = USER_ID,
                    subjectId = subjectId,
                    subjectName = subjectName,
                    title = title,
                    fileName = file.name,
                    docType = "PDF",
                    sizeBytes = file.length(),
                    pageCount = 1,
                    sourceType = "IMPORTED",
                    googleDocId = null,
                    googleWebViewLink = null,
                    createdAt = now - (index + 1) * DAY_MILLIS,
                    updatedAt = now - (index + 1) * DAY_MILLIS
                )
            )
        }
    }

    private suspend fun writeMinimalPdf(file: File, title: String, body: String) {
        val safeTitle = title.replace("(", "").replace(")", "")
        val safeBody = body.replace("(", "").replace(")", "")
        val stream = "BT /F1 20 Tf 60 780 Td ($safeTitle) Tj 0 -30 Td /F1 12 Tf ($safeBody) Tj ET"
        val objects = listOf(
            "<< /Type /Catalog /Pages 2 0 R >>",
            "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>",
            "<< /Length ${stream.length} >>\nstream\n$stream\nendstream",
            "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"
        )
        val builder = StringBuilder("%PDF-1.4\n")
        val offsets = mutableListOf<Int>()
        objects.forEachIndexed { index, obj ->
            offsets += builder.length
            builder.append("${index + 1} 0 obj\n$obj\nendobj\n")
        }
        val xrefOffset = builder.length
        builder.append("xref\n0 ${objects.size + 1}\n0000000000 65535 f \n")
        offsets.forEach { offset ->
            builder.append(String.format(Locale.US, "%010d 00000 n \n", offset))
        }
        builder.append("trailer\n<< /Size ${objects.size + 1} /Root 1 0 R >>\nstartxref\n$xrefOffset\n%%EOF\n")
        withContext(Dispatchers.IO) {
            file.writeBytes(builder.toString().toByteArray(Charsets.US_ASCII))
        }
    }

    private suspend fun writePreferences() {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("active_local_user_id")] = USER_ID
            prefs[stringPreferencesKey("default_grade_scale")] = "0-20"
            prefs[floatPreferencesKey("min_passing_grade")] = 11f
            prefs[booleanPreferencesKey("has_dark_mode_set")] = true
            prefs[booleanPreferencesKey("is_dark_mode")] = false
            prefs[stringPreferencesKey("theme_preset_id")] = "classic"
            prefs[stringPreferencesKey("background_preset_id")] = "classic"
            prefs[stringPreferencesKey("active_academic_cycle")] = CYCLE
        }
    }

    private fun formatMinutes(totalMinutes: Int): String =
        String.format(Locale.US, "%02d:%02d", totalMinutes / 60, totalMinutes % 60)

    private companion object {
        const val TAG = "MockSeeder"
        const val USER_ID = "valentina-rios-local"
        const val CYCLE = "2026-II"
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}
