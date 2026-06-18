package com.example.notifast.presentation.feature.grades

import app.cash.turbine.test
import com.example.notifast.data.model.Evaluation
import com.example.notifast.data.model.SubjectData
import com.example.notifast.domain.repository.AuthRepository
import com.example.notifast.domain.repository.EvaluationRepository
import com.example.notifast.domain.repository.NoteRepository
import com.example.notifast.domain.usecase.CalculateWeightedAverageUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class GradesViewModelTest {

    private val authRepository = mockk<AuthRepository>()
    private val evaluationRepository = mockk<EvaluationRepository>()
    private val noteRepository = mockk<NoteRepository>()
    private val useCase = CalculateWeightedAverageUseCase()
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadData populates uiState correctly`() = runTest {
        // Arrange
        val userId = "test_user_id"
        val subject1 = "Math"
        val subject2 = "Science"
        
        coEvery { authRepository.getCurrentUserId() } returns userId
        
        val evaluations = listOf(
            Evaluation(id = "1", userId = userId, subjectName = subject1, title = "T1", description = "", date = Date(), grade = 10.0, weight = 1.0, isPresented = true),
            Evaluation(id = "2", userId = userId, subjectName = subject1, title = "T2", description = "", date = Date(), grade = 5.0, weight = 1.0, isPresented = true),
            Evaluation(id = "3", userId = userId, subjectName = subject2, title = "T3", description = "", date = Date(), grade = 8.0, weight = 1.0, isPresented = true)
        )
        val subjects = listOf(
            SubjectData(name = subject1, icon = "", colorHex = ""),
            SubjectData(name = subject2, icon = "", colorHex = "")
        )
        
        every { evaluationRepository.getEvaluations(userId) } returns flowOf(evaluations)
        every { noteRepository.getSubjects(userId) } returns flowOf(subjects)
        
        val viewModel = GradesViewModel(evaluationRepository, noteRepository, authRepository, useCase)
        
        // Act & Assert
        viewModel.uiState.test {
            // First emission is initial state
            val initial = awaitItem()
            assertTrue(initial.evaluationsBySubject.isEmpty())
            
            // Second emission is with isLoading = true (from loadData)
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            
            // Third emission is the loaded data
            val loaded = awaitItem()
            
            assertEquals(2, loaded.evaluationsBySubject.size)
            assertEquals(2, loaded.subjects.size)
            
            // Math avg = (10+5)/2 = 7.5. Science avg = 8.0.
            // Global avg = (10+5+8)/3 = 23/3 = 7.666...
            assertEquals(7.666, loaded.globalAverage, 0.001)
            
            assertEquals(2, loaded.subjectGrades.size)
            assertEquals(subject1, loaded.subjectGrades[0].subject)
            assertEquals(7.5, loaded.subjectGrades[0].average, 0.001)
            
            assertEquals(subject2, loaded.subjectGrades[1].subject)
            assertEquals(8.0, loaded.subjectGrades[1].average, 0.001)
            
            // We expect no more items before we cancel
            cancelAndIgnoreRemainingEvents()
        }
    }
}
