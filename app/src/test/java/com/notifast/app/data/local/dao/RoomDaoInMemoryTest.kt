package com.notifast.app.data.local.dao

import com.notifast.app.data.local.NotiFastDatabase
import com.notifast.app.data.local.entity.EvaluationEntity
import com.notifast.app.data.local.entity.NoteEntity
import com.notifast.app.data.local.entity.ScheduleEntity
import com.notifast.app.data.local.entity.SubjectEntity
import com.notifast.app.data.local.entity.UserEntity
import com.notifast.app.di.DatabaseModule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RoomDaoInMemoryTest {

    private lateinit var db: NotiFastDatabase
    private lateinit var userDao: UserDao
    private lateinit var subjectDao: SubjectDao
    private lateinit var noteDao: NoteDao
    private lateinit var evaluationDao: EvaluationDao
    private lateinit var scheduleDao: ScheduleDao

    @Before
    fun setUp() {
        db = mockk(relaxed = true)
        userDao = mockk(relaxed = true)
        subjectDao = mockk(relaxed = true)
        noteDao = mockk(relaxed = true)
        evaluationDao = mockk(relaxed = true)
        scheduleDao = mockk(relaxed = true)

        every { db.userDao() } returns userDao
        every { db.subjectDao() } returns subjectDao
        every { db.noteDao() } returns noteDao
        every { db.evaluationDao() } returns evaluationDao
        every { db.scheduleDao() } returns scheduleDao
    }

    @Test
    fun insertAndRetrieveUser() = runTest {
        val user = UserEntity("u1", "test@test.com", "Juan", "Perez", null, 1000L, 1000L)
        coEvery { userDao.getUserById("u1") } returns user

        userDao.insertUser(user)
        coVerify { userDao.insertUser(user) }

        val retrieved = userDao.getUserById("u1")
        assertEquals("Juan", retrieved?.firstName)
        assertEquals("Perez", retrieved?.lastName)
    }

    @Test
    fun observeUserFlow() = runTest {
        val user = UserEntity("u1", "test@test.com", "Juan", "Perez", null, 1000L, 1000L)
        every { userDao.observeUser("u1") } returns flowOf(user)

        val flowResult = userDao.observeUser("u1").first()
        assertNotNull(flowResult)
        assertEquals("u1", flowResult?.uid)
    }

    @Test
    fun updateUserAndDeleteUser() = runTest {
        val user = UserEntity("u1", "updated@test.com", "Juan", "Perez", null, 1000L, 2000L)
        userDao.updateUser(user)
        coVerify { userDao.updateUser(user) }

        userDao.deleteUser("u1")
        coVerify { userDao.deleteUser("u1") }
    }

    @Test
    fun insertAndObserveSubjects() = runTest {
        val subject = SubjectEntity("u1", "Matemáticas", "📐", "#BF092F", 1000L)
        every { subjectDao.getSubjects("u1") } returns flowOf(listOf(subject))

        subjectDao.insertSubject(subject)
        coVerify { subjectDao.insertSubject(subject) }

        val list = subjectDao.getSubjects("u1").first()
        assertEquals(1, list.size)
        assertEquals("Matemáticas", list.first().name)
    }

    @Test
    fun getSubjectByNameAndDelete() = runTest {
        val subject = SubjectEntity("u1", "Historia", "📜", "#3B9797", 1000L)
        coEvery { subjectDao.getSubjectByName("u1", "Historia") } returns subject

        val retrieved = subjectDao.getSubjectByName("u1", "Historia")
        assertEquals("Historia", retrieved?.name)

        subjectDao.deleteSubject("u1", "Historia")
        coVerify { subjectDao.deleteSubject("u1", "Historia") }
    }

    @Test
    fun noteDaoCrudAndQueries() = runTest {
        val note = NoteEntity("n1", "u1", "Física", "Leyes Newton", "F=ma", null, listOf("Ley1"), 1000L, 1000L)
        every { noteDao.getNotes("u1") } returns flowOf(listOf(note))
        every { noteDao.getNotesBySubject("u1", "Física") } returns flowOf(listOf(note))
        coEvery { noteDao.getNoteById("u1", "n1") } returns note

        noteDao.insertNote(note)
        coVerify { noteDao.insertNote(note) }

        val notes = noteDao.getNotes("u1").first()
        assertEquals(1, notes.size)
        assertEquals("Leyes Newton", notes.first().title)

        val subjectNotes = noteDao.getNotesBySubject("u1", "Física").first()
        assertEquals(1, subjectNotes.size)

        val retrieved = noteDao.getNoteById("u1", "n1")
        assertEquals("F=ma", retrieved?.content)

        val updatedNote = note.copy(content = "F=m*a")
        noteDao.updateNote(updatedNote)
        coVerify { noteDao.updateNote(updatedNote) }

        noteDao.deleteNoteById("u1", "n1")
        coVerify { noteDao.deleteNoteById("u1", "n1") }

        noteDao.deleteNotesBySubject("u1", "Física")
        coVerify { noteDao.deleteNotesBySubject("u1", "Física") }
    }

    @Test
    fun evaluationDaoCrudAndQueries() = runTest {
        val eval = EvaluationEntity(
            "e1", "u1", "Química", "Parcial 1", "Orgánica",
            1000L, emptyList(), null, "Estudiar hidrocarburos",
            "EXAM", 18.0, 25.0, true, 1000L, 1000L
        )
        every { evaluationDao.getEvaluations("u1") } returns flowOf(listOf(eval))
        every { evaluationDao.getEvaluationById("u1", "e1") } returns flowOf(eval)

        evaluationDao.insertEvaluation(eval)
        coVerify { evaluationDao.insertEvaluation(eval) }

        val evals = evaluationDao.getEvaluations("u1").first()
        assertEquals(1, evals.size)
        assertEquals("Parcial 1", evals.first().title)

        val observed = evaluationDao.getEvaluationById("u1", "e1").first()
        assertEquals(18.0, observed?.grade)

        evaluationDao.updateEvaluation(eval.copy(grade = 20.0))
        coVerify { evaluationDao.updateEvaluation(match { it.grade == 20.0 }) }

        evaluationDao.deleteEvaluationById("u1", "e1")
        coVerify { evaluationDao.deleteEvaluationById("u1", "e1") }

        evaluationDao.deleteEvaluationsBySubject("u1", "Química")
        coVerify { evaluationDao.deleteEvaluationsBySubject("u1", "Química") }
    }

    @Test
    fun scheduleDaoCrudAndQueries() = runTest {
        val schedule = ScheduleEntity(
            "s1", "u1", "Cálculo", "presencial", 1,
            "08:00", "10:00", "Aula 101", "Dr. Gauss",
            null, 0xFFBF092FL, 1000L, 1000L
        )
        every { scheduleDao.getSchedules("u1") } returns flowOf(listOf(schedule))
        coEvery { scheduleDao.getScheduleById("u1", "s1") } returns schedule

        scheduleDao.insertSchedule(schedule)
        coVerify { scheduleDao.insertSchedule(schedule) }

        val schedules = scheduleDao.getSchedules("u1").first()
        assertEquals(1, schedules.size)
        assertEquals("Cálculo", schedules.first().subjectName)

        val retrieved = scheduleDao.getScheduleById("u1", "s1")
        assertEquals("08:00", retrieved?.startTime)

        scheduleDao.updateSchedule(schedule.copy(endTime = "11:00"))
        coVerify { scheduleDao.updateSchedule(match { it.endTime == "11:00" }) }

        scheduleDao.deleteScheduleById("u1", "s1")
        coVerify { scheduleDao.deleteScheduleById("u1", "s1") }

        scheduleDao.deleteSchedulesBySubject("u1", "Cálculo")
        coVerify { scheduleDao.deleteSchedulesBySubject("u1", "Cálculo") }
    }

    @Test
    fun databaseModuleProvidesDaos() {
        assertEquals(userDao, DatabaseModule.provideUserDao(db))
        assertEquals(subjectDao, DatabaseModule.provideSubjectDao(db))
        assertEquals(noteDao, DatabaseModule.provideNoteDao(db))
        assertEquals(evaluationDao, DatabaseModule.provideEvaluationDao(db))
        assertEquals(scheduleDao, DatabaseModule.provideScheduleDao(db))
    }

    @Test
    fun insertAndRetrieveTeacher() = runTest {
        val teacherDao: TeacherDao = mockk(relaxed = true)
        every { db.teacherDao() } returns teacherDao

        val teacher = com.notifast.app.data.local.entity.TeacherEntity(
            userId = "u1",
            id = "t1",
            firstName = "Carlos",
            lastName = "Mendoza",
            phoneNumber = "+584121234567",
            email = "carlos@universidad.edu",
            subjectNamesJson = "[\"Cálculo I\"]"
        )

        coEvery { teacherDao.getTeachers("u1") } returns flowOf(listOf(teacher))
        coEvery { teacherDao.getTeacherById("u1", "t1") } returns flowOf(teacher)

        teacherDao.insertTeacher(teacher)
        coVerify { teacherDao.insertTeacher(teacher) }

        val teachers = teacherDao.getTeachers("u1").first()
        assertEquals(1, teachers.size)
        assertEquals("Carlos", teachers.first().firstName)
        assertEquals("+584121234567", teachers.first().phoneNumber)

        val singleTeacher = teacherDao.getTeacherById("u1", "t1").first()
        assertEquals("Mendoza", singleTeacher?.lastName)

        teacherDao.updateTeacher(teacher.copy(phoneNumber = "+584129999999"))
        coVerify { teacherDao.updateTeacher(match { it.phoneNumber == "+584129999999" }) }

        teacherDao.deleteTeacher("u1", "t1")
        coVerify { teacherDao.deleteTeacher("u1", "t1") }

        assertEquals(teacherDao, DatabaseModule.provideTeacherDao(db))
    }

    @Test
    fun insertAndRetrieveSubjectCloudState() = runTest {
        val cloudDao: com.notifast.app.data.local.dao.SubjectCloudStateDao = mockk(relaxed = true)
        every { db.subjectCloudStateDao() } returns cloudDao

        val state = com.notifast.app.data.local.entity.SubjectCloudStateEntity(
            accountId = "acc-1",
            subjectId = "sub-1",
            driveFileId = "drive-1",
            lastUploadedHash = "hash-1",
            lastSyncedAt = 12345L
        )

        coEvery { cloudDao.getAll("acc-1") } returns listOf(state)
        coEvery { cloudDao.observe("acc-1") } returns flowOf(listOf(state))

        cloudDao.upsert(state)
        coVerify { cloudDao.upsert(state) }

        val list = cloudDao.getAll("acc-1")
        assertEquals(1, list.size)
        assertEquals("drive-1", list.first().driveFileId)

        val flowList = cloudDao.observe("acc-1").first()
        assertEquals("hash-1", flowList.first().lastUploadedHash)

        cloudDao.deleteBySubject("sub-1")
        coVerify { cloudDao.deleteBySubject("sub-1") }

        cloudDao.deleteByAccount("acc-1")
        coVerify { cloudDao.deleteByAccount("acc-1") }

        assertEquals(cloudDao, DatabaseModule.provideSubjectCloudStateDao(db))
    }

    @Test
    fun insertAndRetrieveStudyDocument() = runTest {
        val studyDocDao: com.notifast.app.data.local.dao.StudyDocumentDao = mockk(relaxed = true)
        every { db.studyDocumentDao() } returns studyDocDao

        val doc = com.notifast.app.data.local.entity.StudyDocumentEntity(
            id = "doc-1",
            userId = "u1",
            subjectId = "sub-1",
            subjectName = "Física",
            title = "Guía Lab 1",
            fileName = "doc-1.pdf",
            docType = "PDF",
            sizeBytes = 20480L,
            pageCount = 5,
            sourceType = "IMPORTED",
            googleDocId = null,
            googleWebViewLink = null,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        coEvery { studyDocDao.getDocumentById("u1", "doc-1") } returns doc
        every { studyDocDao.observeDocuments("u1") } returns flowOf(listOf(doc))

        studyDocDao.upsertDocument(doc)
        coVerify { studyDocDao.upsertDocument(doc) }

        val retrieved = studyDocDao.getDocumentById("u1", "doc-1")
        assertEquals("Guía Lab 1", retrieved?.title)
        assertEquals("PDF", retrieved?.docType)

        val flowDocs = studyDocDao.observeDocuments("u1").first()
        assertEquals(1, flowDocs.size)

        studyDocDao.deleteDocument("u1", "doc-1")
        coVerify { studyDocDao.deleteDocument("u1", "doc-1") }

        assertEquals(studyDocDao, DatabaseModule.provideStudyDocumentDao(db))
    }

    @Test
    fun insertAndRetrieveCoverDraft() = runTest {
        val draftDao: com.notifast.app.data.local.dao.CoverDraftDao = mockk(relaxed = true)
        every { db.coverDraftDao() } returns draftDao

        val draft = com.notifast.app.data.local.entity.CoverDraftEntity(
            id = "draft-1",
            userId = "u1",
            templateId = "APA_7",
            subjectId = "sub-1",
            subjectName = "Física",
            workTitle = "Informe Péndulo",
            subtitle = "Lab 1",
            authorsJson = "[\"Juan\"]",
            teacherName = "Prof. Newton",
            institutionName = "UCV",
            facultyOrDepartment = "Ciencias",
            city = "Caracas",
            formattedDate = "19 de octubre de 2026",
            abstractText = "",
            keywordsText = "",
            generatedLocalDocId = null,
            googleDocId = null,
            googleWebViewLink = null,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        coEvery { draftDao.getDraftById("u1", "draft-1") } returns draft
        coEvery { draftDao.getLatestDraft("u1") } returns draft
        every { draftDao.observeDrafts("u1") } returns flowOf(listOf(draft))

        draftDao.upsertDraft(draft)
        coVerify { draftDao.upsertDraft(draft) }

        val retrieved = draftDao.getDraftById("u1", "draft-1")
        assertEquals("Informe Péndulo", retrieved?.workTitle)
        assertEquals("APA_7", retrieved?.templateId)

        val latest = draftDao.getLatestDraft("u1")
        assertEquals("UCV", latest?.institutionName)

        val flowDrafts = draftDao.observeDrafts("u1").first()
        assertEquals(1, flowDrafts.size)

        draftDao.deleteDraft("u1", "draft-1")
        coVerify { draftDao.deleteDraft("u1", "draft-1") }

        assertEquals(draftDao, DatabaseModule.provideCoverDraftDao(db))
    }
}

