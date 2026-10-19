package com.notifast.app.domain.usecase.study

import com.notifast.app.data.model.Teacher
import com.notifast.app.data.model.User
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.StudyDocumentRepository
import com.notifast.app.domain.repository.TeacherRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StudyHubUseCasesTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var teacherRepository: TeacherRepository
    private lateinit var studyDocRepository: StudyDocumentRepository
    private lateinit var prefillUseCase: PrefillCoverDraftForSubjectUseCase

    @Before
    fun setUp() {
        authRepository = mockk(relaxed = true)
        teacherRepository = mockk(relaxed = true)
        studyDocRepository = mockk(relaxed = true)

        prefillUseCase = PrefillCoverDraftForSubjectUseCase(
            authRepository = authRepository,
            teacherRepository = teacherRepository,
            studyDocumentRepository = studyDocRepository
        )
    }

    @Test
    fun `PrefillCoverDraftForSubjectUseCase populates author from user, teacher from subject, and institution from latest draft`() = runTest {
        val user = User(uid = "u1", email = "maria@notifast.com", firstName = "María", lastName = "González")
        coEvery { authRepository.getUserProfile("u1") } returns Result.success(user)

        val teacher = Teacher(
            id = "t1",
            userId = "u1",
            firstName = "Isaac",
            lastName = "Newton",
            email = "newton@cambridge.ac.uk",
            subjectNames = listOf("Física Clásica", "Cálculo I")
        )
        coEvery { teacherRepository.getTeachers("u1") } returns flowOf(listOf(teacher))

        val previousDraft = CoverDraft(
            id = "prev-d",
            userId = "u1",
            templateType = CoverTemplateType.APA_7,
            institutionName = "Universidad Simón Bolívar",
            facultyOrDepartment = "Departamento de Matemáticas y Física",
            city = "Caracas"
        )
        coEvery { studyDocRepository.getLatestCoverDraft("u1") } returns previousDraft

        val prefilled = prefillUseCase(
            userId = "u1",
            templateType = CoverTemplateType.APA_7,
            subjectId = "s1",
            subjectName = "Física Clásica"
        )

        assertEquals("María González", prefilled.authors.firstOrNull())
        assertEquals("Prof. Isaac Newton", prefilled.teacherName)
        assertEquals("Universidad Simón Bolívar", prefilled.institutionName)
        assertEquals("Departamento de Matemáticas y Física", prefilled.facultyOrDepartment)
        assertEquals("Caracas", prefilled.city)
        assertEquals("Física Clásica", prefilled.subjectName)
        assertTrue(prefilled.formattedDate.isNotBlank())
    }

    @Test
    fun `PrefillCoverDraftForSubjectUseCase populates UNERG institutional defaults and letterhead`() = runTest {
        coEvery { authRepository.getUserProfile("u1") } returns Result.success(User(uid = "u1", email = "test@unerg.edu", firstName = "José", lastName = "Fernandez"))
        val teacher = Teacher(id = "t2", userId = "u1", firstName = "Miguel", lastName = "Padrón", subjectNames = listOf("Proyecto de Grado I"))
        coEvery { teacherRepository.getTeachers("u1") } returns flowOf(listOf(teacher))
        coEvery { studyDocRepository.getLatestCoverDraft("u1") } returns null

        val prefilled = prefillUseCase(
            userId = "u1",
            templateType = CoverTemplateType.UNERG_DEGREE,
            subjectId = "sub-1",
            subjectName = "Proyecto de Grado I"
        )

        assertEquals(CoverTemplateType.UNERG_DEGREE, prefilled.templateType)
        assertEquals("José Fernandez", prefilled.authors.firstOrNull())
        assertEquals("Prof. Miguel Padrón", prefilled.teacherName)
        assertEquals("UNIVERSIDAD NACIONAL EXPERIMENTAL DE LOS LLANOS CENTRALES RÓMULO GALLEGOS", prefilled.institutionName)
        assertEquals("ÁREA DE INGENIERÍA DE SISTEMAS", prefilled.facultyOrDepartment)
        assertEquals("San Juan de los Morros", prefilled.city)
        assertEquals("REPÚBLICA BOLIVARIANA DE VENEZUELA", prefilled.countryLine)
        assertEquals("MINISTERIO DEL PODER POPULAR PARA LA EDUCACIÓN UNIVERSITARIA", prefilled.ministryLine)
        assertEquals("PROGRAMA DE INGENIERÍA EN INFORMÁTICA", prefilled.programLine)
        assertEquals("UNIDAD CURRICULAR: PROYECTO DE GRADO I", prefilled.curricularUnit)
    }
}
