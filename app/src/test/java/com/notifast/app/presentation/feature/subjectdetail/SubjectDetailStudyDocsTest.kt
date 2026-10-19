package com.notifast.app.presentation.feature.subjectdetail

import androidx.lifecycle.SavedStateHandle
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.study.StudyDocSource
import com.notifast.app.domain.model.study.StudyDocType
import com.notifast.app.domain.model.study.StudyDocument
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.TeacherRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.CalculateRequiredGradeUseCase
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
import com.notifast.app.domain.usecase.ExportSubjectPackageUseCase
import com.notifast.app.domain.usecase.study.ObserveStudyDocumentsBySubjectUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubjectDetailStudyDocsTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var noteRepository: NoteRepository
    private lateinit var evaluationRepository: EvaluationRepository
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var teacherRepository: TeacherRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var userPrefsRepository: UserPrefsRepository
    private lateinit var calculateWeightedAverageUseCase: CalculateWeightedAverageUseCase
    private lateinit var calculateRequiredGradeUseCase: CalculateRequiredGradeUseCase
    private lateinit var exportSubjectPackageUseCase: ExportSubjectPackageUseCase
    private lateinit var observeStudyDocumentsBySubjectUseCase: ObserveStudyDocumentsBySubjectUseCase

    private val sampleDoc = StudyDocument(
        id = "d-sub-1",
        userId = "u1",
        subjectId = "s1",
        subjectName = "Física",
        title = "Guía Laboratorio",
        fileName = "lab.pdf",
        docType = StudyDocType.PDF,
        sizeBytes = 1024L,
        pageCount = 3,
        sourceType = StudyDocSource.IMPORTED
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        noteRepository = mockk(relaxed = true)
        evaluationRepository = mockk(relaxed = true)
        scheduleRepository = mockk(relaxed = true)
        teacherRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        userPrefsRepository = mockk(relaxed = true)
        calculateWeightedAverageUseCase = mockk(relaxed = true)
        calculateRequiredGradeUseCase = mockk(relaxed = true)
        exportSubjectPackageUseCase = mockk(relaxed = true)
        observeStudyDocumentsBySubjectUseCase = mockk(relaxed = true)

        coEvery { authRepository.getCurrentUserId() } returns "u1"
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-10")
        coEvery { userPrefsRepository.minPassingGradeFlow } returns flowOf(5.0f)
        coEvery { noteRepository.getNotesBySubject("u1", "Física") } returns flowOf(emptyList())
        coEvery { evaluationRepository.getEvaluations("u1") } returns flowOf(emptyList())
        coEvery { scheduleRepository.getSchedules("u1") } returns flowOf(emptyList())
        coEvery { noteRepository.getSubjects("u1") } returns flowOf(listOf(SubjectData("s1", "Física", "🔬", "#FF0000", "Ciclo 1")))
        coEvery { teacherRepository.getTeachersBySubject("u1", "Física") } returns flowOf(emptyList())
        coEvery { observeStudyDocumentsBySubjectUseCase("u1", "Física") } returns flowOf(listOf(sampleDoc))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SubjectDetailViewModel loads studyDocuments linked to subjectName`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("subjectName" to "Física"))
        val viewModel = SubjectDetailViewModel(
            savedStateHandle = savedStateHandle,
            noteRepository = noteRepository,
            evaluationRepository = evaluationRepository,
            scheduleRepository = scheduleRepository,
            teacherRepository = teacherRepository,
            authRepository = authRepository,
            userPrefsRepository = userPrefsRepository,
            calculateWeightedAverageUseCase = calculateWeightedAverageUseCase,
            calculateRequiredGradeUseCase = calculateRequiredGradeUseCase,
            exportSubjectPackageUseCase = exportSubjectPackageUseCase,
            observeStudyDocumentsBySubjectUseCase = observeStudyDocumentsBySubjectUseCase
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.studyDocuments.size)
        assertEquals("Guía Laboratorio", state.studyDocuments.first().title)
    }
}
